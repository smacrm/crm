package gnext.service.impl;

import gnext.bean.Company;
import gnext.bean.CompanyTargetInfo;
import gnext.bean.CompanyTargetInfoPK;
import gnext.bean.DatabaseServer;
import gnext.bean.DatabaseServerCompanyRel;
import gnext.bean.DatabaseServerCompanyRelPK;
import gnext.bean.Member;
import gnext.bean.UnionCompanyRel;
import gnext.bean.UnionCompanyRelModel;
import gnext.bean.UnionCompanyRelPK;
import gnext.bean.attachment.Attachment;
import gnext.bean.attachment.Server;
import gnext.dbutils.model.enums.AttachmentTargetType;
import gnext.dbutils.model.enums.ServerFlag;
import gnext.filetransfer.DeleteParameter;
import gnext.filetransfer.FileTransferFactory;
import gnext.filetransfer.Parameter;
import gnext.filetransfer.TransferType;
import gnext.interceptors.annotation.QuickSearchAction;
import gnext.multitenancy.DataSource;
import gnext.multitenancy.ProxyMasterEntityManager;
import gnext.multitenancy.ShellSeparateDbUtil;
import gnext.multitenancy.service.MultitenancyService;
import gnext.service.CompanyService;
import gnext.service.DatabaseServerService;
import gnext.service.UnionCompanyRelService;
import gnext.service.attachment.AttachmentService;
import gnext.service.attachment.ServerService;
import gnext.utils.EncoderUtil;
import gnext.utils.MapObjectUtil;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import gnext.utils.JPAUtils;
import javax.persistence.EntityTransaction;

/**
 *
 * @author daind
 */
// @Interceptors ({QuickSearchDbInterceptor.class})
@Stateless
public class CompanyServiceImpl extends AbstractService<Company> implements CompanyService {
    private static final long serialVersionUID = 8996468946987869268L;
    private static final Logger LOGGER = LoggerFactory.getLogger(CompanyServiceImpl.class);
    
    @EJB private UnionCompanyRelService unionCompanyRelService;
    @EJB private ServerService serverService;
    @EJB private AttachmentService attachmentService;
    @EJB private DatabaseServerService databaseServerService;
    @EJB private MultitenancyService multitenancyService;
    
    @Inject ProxyMasterEntityManager masterEntityManager;
    @Override protected EntityManager getEntityManager() {
        return masterEntityManager.getEntityManager();
    }
    
    public CompanyServiceImpl() { super(Company.class); }

    /**
     * Hàm xử lí chuyển đổi Short sang Boolean.
     * @param flag
     * @return 
     */
    private boolean getBoolean(Short flag) {
        if (flag != null && flag == 1) {
            return true;
        } else {
            return false;
        }
    }
    
    private Server getOneServer(final List<Server> servers) {
        if(servers == null || servers.isEmpty()) return null;
        for(Server server : servers)
            if(server.getServerDeleted() == null || server.getServerDeleted() == 0) return server;
        return null;
    }
    
    /***
     * Hàm xử lí đẩy logo lên ftp gnext server.
     * Logo của công ty được lưu trên FTP của GNEXT.
     * @param c
     * @param bytes
     * @throws Exception 
     */
    private Attachment uploadLogo2FtpServer(Company c, byte[] bytes) throws Exception {
        EntityManager em_slave = null;
        EntityTransaction tx_slave = null;
        try {
            if(c.getCompanyId() == null || c.getCompanyId() < 0) throw new Exception("The id of company is null or empty.");
            if(bytes == null || bytes.length < 0) return null;

            em_slave = JPAUtils.getSlaveEntityManager(c.getCompanyId());
            tx_slave = beginTransaction(em_slave);
            
            List<Server> servers = serverService.getAvailable(c.getCompanyId(), TransferType.FTP.getType(), ServerFlag.COMMON.getId());
            if(servers == null || servers.isEmpty()) throw new Exception("can't get server for upload logo the company.");

            Server server = getOneServer(servers);
            if(server == null) throw new Exception("can't get server for upload logo the company.");
            
            checkExistLogo(c, server, em_slave);

            String path = server.getServerFolder(); path = path + File.separator + c.getCompanyId() + File.separator;
            String host = server.getServerHost();
            int port = server.getServerPort();
            String username = server.getServerUsername();
            String password = server.getDecryptServerPassword();
            boolean security = getBoolean(server.getServerSsl());
            String protocol = server.getServerProtocol();
            String servertype = server.getServerType();

            TransferType tt = TransferType.getTransferType(servertype);
            Parameter param = Parameter.getInstance(tt).manualconfig(true).storeDb(false);
            param.host(host).port(port).username(username).password(password).security(security).protocol(protocol)
                    .uploadfilename(c.getCompanyLogo()).uploadpath(path).createfolderifnotexists();
            FileTransferFactory.getTransfer(param).upload(new ByteArrayInputStream(bytes));
            gnext.dbutils.model.Attachment attachment = param.getAttachment();
            if(attachment == null) throw new Exception("Upload logo error.");
            Attachment ea = MapObjectUtil.convert(attachment);
            ea.setAttachmentTargetType(AttachmentTargetType.COMPANY.getId());
            ea.setAttachmentTargetId(c.getCompanyId());
            ea.setServer(server);
            ea.setCompany(c);
            ea.setCreatorId(c.getCreator().getCreator().getMemberId());
            
            Attachment synAttachment = JPAUtils.create(ea, em_slave, false);
            
            commitAndCloseTransaction(tx_slave);
            return synAttachment;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            rollbackAndCloseTransaction(tx_slave);
            throw e;
        } finally {
            JPAUtils.release(em_slave, true);
        }
    }
    
    /***
     * Hàm xử lí kiểm tra logo của company, nếu có thực hiện xóa trong db và ftp server.
     * @param logined
     * @param c 
     */
    private void checkExistLogo(Company c, Server server, EntityManager em_slave)  throws Exception {
        if(c == null || c.getCompanyId() == null) return;
        int atId = AttachmentTargetType.COMPANY.getId();
        List<Attachment> attachments = attachmentService.search(c.getCompanyId(), atId, c.getCompanyId(), (short) 0);
        if(attachments != null && !attachments.isEmpty())
            for(Attachment attachment : attachments) {
                String delRemotePath = attachment.getAttachmentPath() + File.separator + attachment.getAttachmentName();
                delFileRemote(server, delRemotePath);
                JPAUtils.remove(attachment, em_slave, false);
            }
    }
    
    private void delFileRemote(Server server, String delRemotePath) throws Exception {
        String host = server.getServerHost();
        int port = server.getServerPort();
        String username = server.getServerUsername();
        String password = server.getDecryptServerPassword();
        boolean security = getBoolean(server.getServerSsl());
        String protocol = server.getServerProtocol();
        String servertype = server.getServerType();
        
        DeleteParameter delParam = DeleteParameter.getInstance(TransferType.getTransferType(servertype));
        delParam.type(TransferType.FTP).host(host).port(port).username(username).password(password).security(security).protocol(protocol);
        delParam.deletePath(delRemotePath).folder(false);

        FileTransferFactory.getTransfer(delParam).delete();
    }
    
    /***
     * Hàm tìm kiếm có xử lí cho việc phân trang.
     * @param first
     * @param pageSize
     * @param sortField
     * @param sortOrder
     * @param where
     * @return 
     */
    @Override
    public List<Company> find(int first, int pageSize, String sortField, String sortOrder, String where) {
        EntityManager em_master = null;
        try {
            em_master = masterEntityManager.getEntityManager();
            String sql = "SELECT c FROM Company c WHERE 1=1 AND " + where;
            Query query = JPAUtils.buildJQLQuery(em_master, sql);
            query.setFirstResult(first);
            query.setMaxResults(pageSize);
            List<Company> results = query.getResultList();
            return results;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_master, true);
        }
        return new ArrayList<>();
    }
    @Override
    public int total(String where) {
        EntityManager em_master = null;
        try {
            em_master = masterEntityManager.getEntityManager();
            String sql = "SELECT count(c.companyId) FROM Company c WHERE 1=1 AND " + where;
            Query query = em_master.createQuery(sql);
            Long countResults = (Long) query.getSingleResult();
            return countResults.intValue();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_master, true);
        }
        return 0;
    }

    /**
     * BasicログインIDから、会社情報を取得
     * @param companyBasicLoginId：BasicログインID
     * @return ：会社情報
     */
    @Override
    public Company findByCompanyBasicLoginId(String companyBasicLoginId) {
        EntityManager em_master = null;
        try{
            em_master = masterEntityManager.getEntityManager();
            String query = "SELECT c FROM Company c WHERE c.companyBasicLoginId = :companyBasicLoginId AND c.companyDeleted = :companyDeleted ";
            return em_master.createQuery(query, Company.class)
                    .setParameter("companyBasicLoginId", companyBasicLoginId)
                    .setParameter("companyDeleted", 0)
                    .getSingleResult();
        }catch(Exception e){
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_master, true);
        }
        return null;
    }

    /***
     * Tìm kiếm tất cả các company loại trừ các id có trong ids.
     * @param ids những company_id cần loại bỏ trong kết quả trả về.
     * @return 
     */
    @Override
    public List<Company> findWithExecludeIds(final List<Integer> ids) {
        EntityManager em_master = null;
        try {
            em_master = masterEntityManager.getEntityManager();
            String sql = "SELECT c from Company c WHERE c.companyId NOT IN :ids";
            Query query = em_master.createQuery(sql);
            query.setParameter("ids", ids);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_master, true);
        }
        return new ArrayList<>();
    }

    /***
     * Tìm kiếm tất cả các company có company_id tồn tại trong ids, và loại bỏ chính nó.
     * @param cid id của người dùng đăng nhập hệ thống.
     * @param ids những company_id cần có trong kết quả trả về.
     * @return 
     */
    @Override
    public List<Company> findCompanyExecludeGroup(int cid, List<Integer> ids) {
        EntityManager em_master = null;
        try {
            em_master = masterEntityManager.getEntityManager();
            String sql = "select c from Company c where c.companyId in "
                     + " (select co.companyId from Company co "
                     + " where co.companyId IN :ids and co.companyId!=:cid)";
            Query query = em_master.createQuery(sql);
            query.setParameter("ids", ids);
            query.setParameter("cid", cid);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_master, true);
        }
        return new ArrayList<>();
    }
    
    
    /***
     * Hàm xử lí lấy các company thuộc group với cid.
     * @param cid
     * @return 
     */
    @Override
    public List<Company> findByCompanyBelongGroup(int cid) {
        EntityManager em_master = null;
        try {
            em_master = masterEntityManager.getEntityManager();
            String sql = "select c from Company c"
                + " where c.companyUnionKey in"
                + " (select cr.companyUnionKey from Company cr"
                + " where cr.companyId=:cid) and c.companyDeleted = 0";
            Query query = em_master.createQuery(sql);
            query.setParameter("cid", cid);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_master, true);
        }
        return new ArrayList<>();
    }
    
    /***
     * Hàm xử lí chỉnh sửa thông tin company.
     * Sử dụng JTA transaction để bảo toàn dữ liệu.
     * @param logined
     * @param c
     * @param m
     * @param bytes
     * @param phones
     * @param phoneDeleted
     * @param fax
     * @param faxDeleted
     * @param email
     * @param emailDeleted
     * @param homepage
     * @param homepageDeleted
     * @param unionCompanyRelModels
     * @return
     * @throws Exception
     */
    @QuickSearchAction(action = QuickSearchAction.UPDATE)
    @Override
    public Company edit(Company logined, Company c, Member m, byte[] bytes,
            List<CompanyTargetInfo> phones, List<CompanyTargetInfo> phoneDeleted,
            List<CompanyTargetInfo> fax, List<CompanyTargetInfo> faxDeleted,
            List<CompanyTargetInfo> email, List<CompanyTargetInfo> emailDeleted,
            List<CompanyTargetInfo> homepage, List<CompanyTargetInfo> homepageDeleted,
            List<UnionCompanyRelModel> unionCompanyRelModels) throws Exception {
        EntityManager em_master = null;
        EntityTransaction tx = null;
        try {
            em_master = masterEntityManager.getEntityManager();
            tx = beginTransaction(em_master);
            // các công ty groups cũ.
            List<Integer> preCompanyGroupIds = unionCompanyRelService.findAllCompanyGroupIds(c.getCompanyId());
            preCompanyGroupIds.remove(c.getCompanyId());
            
            // chỉnh sửa thông tin company vào trong db.
            c.setUpdator(m);
            c.setUpdatedTime(Calendar.getInstance().getTime());
//            c.setCompanyDeleted((short)0);

            String metacontent = _StoreCompanyInfo(c, m, phones, phoneDeleted, fax,
                    faxDeleted, email, emailDeleted, homepage, homepageDeleted, em_master);
            _StoreLogo(c, bytes);
            _StoreUnionCompanys(c, unionCompanyRelModels, em_master);

            Company edited = JPAUtils.edit(c, em_master, false);
            edited.setPhoneFaxMailHomepage(metacontent);
            
//            Attachment attachment = uploadLogo2FtpServer(edited, bytes);
            
            commitAndCloseTransaction(tx);
            
            // đẩy logo công ty vào database slave.
            Attachment attachment = uploadLogo2FtpServer(edited, bytes);
            
            // các công ty groups mới.
            List<Integer> companyGroupIds = unionCompanyRelService.findAllCompanyGroupIds(edited.getCompanyId());
            companyGroupIds.remove(c.getCompanyId());
            
            // lấy danh sách các công ty tách khỏi công ty hiện tại.
            for(Integer companyGroupId : companyGroupIds) {
                if(preCompanyGroupIds.contains(companyGroupId)) {
                    preCompanyGroupIds.remove(companyGroupId);
                }
            }
            
            // lấy tất cả các member của công ty chính thuộc công ty group.
            List<Integer> memberOnCompanyGroups = multitenancyService.findAllMemberAllowLoginComapnyGroup(c.getCompanyId());
            for(Integer removeCompanyGroupId : preCompanyGroupIds) {
                List<Integer> removememberOnCompanyGroups = multitenancyService.findAllMemberIdsOnGroupByIds(memberOnCompanyGroups, removeCompanyGroupId);
                for(Integer removeMemberId : removememberOnCompanyGroups) {
                    multitenancyService.markDeletedMemberOnCompanyGroup(c.getCompanyId(), removeCompanyGroupId, removeMemberId, true);
                }
            }
            
            // Đồng bộ dữ liệu chính xuống công ty slave.
            
            
            return edited;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            rollbackAndCloseTransaction(tx);
            throw e;
        } finally {
            JPAUtils.release(em_master, true);
        }
    }
    
    /***
     * Hàm xử lí thêm mới 1 company.
     * Sử dụng JTA transaction để bảo toàn dữ liệu.
     * @param logined
     * @param c
     * @param m
     * @param bytes
     * @param phones
     * @param phoneDeleted
     * @param fax
     * @param faxDeleted
     * @param email
     * @param emailDeleted
     * @param homepage
     * @param homepageDeleted
     * @param unionCompanyRelModels
     * @return
     * @throws Exception
     */
    @Override
    @QuickSearchAction(action = QuickSearchAction.CREATE)
    public Company insert(Company logined, Company c, Member m, byte[] bytes,
            List<CompanyTargetInfo> phones, List<CompanyTargetInfo> phoneDeleted,
            List<CompanyTargetInfo> fax, List<CompanyTargetInfo> faxDeleted,
            List<CompanyTargetInfo> email, List<CompanyTargetInfo> emailDeleted,
            List<CompanyTargetInfo> homepage, List<CompanyTargetInfo> homepageDeleted,
            List<UnionCompanyRelModel> unionCompanyRelModels)
            throws Exception {
        EntityManager em_master = null;
        EntityTransaction tx_master = null;
        Integer realCompanySlaveId = null;
        try {
            em_master = masterEntityManager.getEntityManager();
            tx_master = beginTransaction(em_master);
            c.setCreator(m);
            c.setCreatedTime(Calendar.getInstance().getTime());
            c.setCompanyDeleted((short) 0);
            c.setCompanyBasicLoginId(String.valueOf(System.currentTimeMillis()));
            c.setCompanyBasicPassword(String.valueOf(System.currentTimeMillis()));
            Company saved = JPAUtils.create(c, em_master, false);
            
            saved.setManualId(saved.getCompanyId()); // sử dụng trong việc tách DB.
            realCompanySlaveId = saved.getCompanyId();
            
            String metacontent =
                    _StoreCompanyInfo(saved, m, phones, phoneDeleted, fax, faxDeleted,
                            email, emailDeleted, homepage, homepageDeleted, em_master);
            _StoreLogo(saved, bytes);
            _StoreBaseLogin(saved);
            _StoreUnionCompanys(saved, unionCompanyRelModels, em_master);

            Company edited = JPAUtils.edit(saved, em_master, false);
            edited.setPhoneFaxMailHomepage(metacontent);
            
//            Attachment attachment = uploadLogo2FtpServer(logined, edited, bytes, em_master);
            
            // Tìm datasource cho công ty mới.
            Integer dataSourceId = DatabaseServer.LOCAL_DATA_SOURCE;
            if(c.getDatabaseServerId() != null && c.getDatabaseServerId() > 1) {
                dataSourceId = c.getDatabaseServerId();
            }
            DatabaseServer databaseServer = databaseServerService.findById(dataSourceId);
            DataSource ds = new DataSource(databaseServer.getDatabaseServerDriver(),
                    databaseServer.getDatabaseServerUsername(), databaseServer.getDatabaseServerPassword(),
                    databaseServer.getDatabaseServerHost(), databaseServer.getDatabaseServerPort());
            
            DatabaseServerCompanyRelPK dscrpk = new DatabaseServerCompanyRelPK(dataSourceId, edited.getCompanyId());
            DatabaseServerCompanyRel dscr = new DatabaseServerCompanyRel(dscrpk);
            JPAUtils.create(dscr, em_master, false);
            
            commitAndCloseTransaction(tx_master);
            
            // khi tạo mới công ty, sẽ tạo mới schema tương ứng với id của công ty vừa thêm mới.
            // Hiện tại là chạy ngoài transaction tránh trường hợp lock. :((
            ShellSeparateDbUtil.splitSchemaWithNewCompany(edited, ds);
            
            // đẩy logo công ty vào database slave.
            Attachment attachment = uploadLogo2FtpServer(edited, bytes);
            
            // just for test going to error.
//            int a = 1/0;
            
            return edited;
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            
            // rollback lại thông tin trên master current connection.
            rollbackAndCloseTransaction(tx_master);
            
            // rollback lại toàn bộ thông tin đã làm :(
            rollbackWhenInsertCompanyFaile(realCompanySlaveId);
            
            throw e;
        } finally {
            JPAUtils.release(em_master, true);
        }
    }
    
    /**
     * 
     * @param companyId 
     */
    private void rollbackWhenInsertCompanyFaile(Integer companyId) {
        if(companyId == null) return;
        EntityManager em_master = null;
        EntityManager em_slave = null;
        Query query = null;
        String sql = null;
        try {
            DatabaseServer databaseServer = databaseServerService.findOneDatabaseServer(companyId);
            if(databaseServer == null) return; // không làm j cả.
            
            // 1. xóa schema công ty vừa tạo mới nếu tồn tại.
            DataSource ds = new DataSource(databaseServer.getDatabaseServerDriver(),
                    databaseServer.getDatabaseServerUsername(), databaseServer.getDatabaseServerPassword(),
                    databaseServer.getDatabaseServerHost(), databaseServer.getDatabaseServerPort());
            ShellSeparateDbUtil.deleteSlaveDb(companyId, ds);
            
            // 2. lấy một master connection.
            em_master = masterEntityManager.getEntityManager();
            
            // 3. xóa union.
            sql = "delete from crm_union_company_rel where company_id=?";
            query = em_master.createNativeQuery(sql).setParameter(1, companyId);
            JPAUtils.executeDeleteOrUpdateQuery(em_master, query);
            
            // 4. xóa dữ liệu thông tin ngoài(tel, mol,...).
            sql = "delete from crm_company_target_info where company_target=? and company_target_id=?";
            query = em_master.createNativeQuery(sql).setParameter(1, CompanyTargetInfo.COMPANY_TARGET_COMPANY).setParameter(2, companyId);
            JPAUtils.executeDeleteOrUpdateQuery(em_master, query);
            
            // 5. xóa dữ liệu công ty trên master.
            sql = "delete from crm_company where company_id=?";
            query = em_master.createNativeQuery(sql).setParameter(1, companyId);
            JPAUtils.executeDeleteOrUpdateQuery(em_master, query);
            
            // 6. xóa dữ liệu thông tin về database.
            sql = "delete from crm_database_server_company_rel where company_id=?";
            query = em_master.createNativeQuery(sql).setParameter(1, companyId);
            JPAUtils.executeDeleteOrUpdateQuery(em_master, query);
            
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_master, true);
            JPAUtils.release(em_slave, true);
            query = null;
            sql = null;
        }
    }
    
    /***
     * Hàm xử lí cập nhật tên công ty theo mẫu companyid_comapnylogo.
     * @param c 
     */
    private void _UpdateLogoName(Company c) {
        if(StringUtils.isEmpty(c.getCompanyLogo())) return;
        String cl = c.getCompanyLogo();
        String ext = FilenameUtils.getExtension(cl);
        c.setCompanyLogo("logo." + ext);
    }
    
    /***
     * Hàm xử lí lưu trữ logo trên local.
     * @param c
     * @param bytes
     * @throws Exception 
     */
    private void _StoreLogo(Company c, byte[] bytes) throws Exception {
        if (bytes == null) return;
        _UpdateLogoName(c);
    }
    
    /***
     * Cập nhật lại companybase id và password.
     * @param company 
     */
    private void _StoreBaseLogin(Company company) {
        String s = "admin" + company.getCompanyId();
        company.setCompanyBasicLoginId(s);
        company.setCompanyBasicPassword(EncoderUtil.getPassEncoder().encode(s));
    }
    
    /***
     * Hàm xử lí lưu thông tin phone, fax, mail, homepage.
     * @param c
     * @param m
     * @param phones
     * @param phoneDeleted
     * @param fax
     * @param faxDeleted
     * @param email
     * @param emailDeleted
     * @param homepage
     * @param homepageDeleted
     * @return
     * @throws Exception 
     */
    private String _StoreCompanyInfo(Company c, Member m,
            List<CompanyTargetInfo> phones, List<CompanyTargetInfo> phoneDeleted,
            List<CompanyTargetInfo> fax, List<CompanyTargetInfo> faxDeleted,
            List<CompanyTargetInfo> email, List<CompanyTargetInfo> emailDeleted,
            List<CompanyTargetInfo> homepage, List<CompanyTargetInfo> homepageDeleted
            , EntityManager em_master) throws Exception {
        StringBuilder metaContent = new StringBuilder();
        _SaveCompanyTargetInfo(phones, c, m, CompanyTargetInfo.COMPANY_FLAG_TYPE_PHONE, em_master);
        _DeleteCompanyTargetInfo(phoneDeleted, em_master);
        buildContentQuickSearch(metaContent, phones);

        _SaveCompanyTargetInfo(fax, c, m, CompanyTargetInfo.COMPANY_FLAG_TYPE_FAX, em_master);
        _DeleteCompanyTargetInfo(faxDeleted, em_master);
        buildContentQuickSearch(metaContent, fax);

        _SaveCompanyTargetInfo(email, c, m, CompanyTargetInfo.COMPANY_FLAG_TYPE_EMAIL, em_master);
        _DeleteCompanyTargetInfo(emailDeleted, em_master);
        buildContentQuickSearch(metaContent, email);

        _SaveCompanyTargetInfo(homepage, c, m, CompanyTargetInfo.COMPANY_FLAG_TYPE_HOMEPAGE, em_master);
        _DeleteCompanyTargetInfo(homepageDeleted, em_master);
        buildContentQuickSearch(metaContent, homepage);
        
        return metaContent.toString();
    }
    
    /***
     * Xây dựng nội dung content cho module QuickSearch. 
     * @param meta nội dung content sẽ đươc update.
     * @param ctis thông tin chung cho mail, phone, fax, hompage.
     */
    private void buildContentQuickSearch(StringBuilder meta, List<CompanyTargetInfo> ctis) {
        for(CompanyTargetInfo cti : ctis) {
            if(cti.getCompanyTargetData() != null && !cti.getCompanyTargetData().isEmpty())
                meta.append(cti.getCompanyTargetData()).append(";");
        }
    }
    
    /***
     * Duyệt tất cả các thông như phone-fax-email-hompage của company để lưu trữ.
     * @param cti
     * @param c
     * @param m
     * @param flagType 
     */
    private void _SaveCompanyTargetInfo(List<CompanyTargetInfo> cti, Company c, Member m, short flagType, EntityManager em_master) throws Exception {
        Integer cid = c.getCompanyId();
        for (CompanyTargetInfo companyTargetInfo : cti) {
            if(companyTargetInfo.getCompanyTargetInfoPK() != null && companyTargetInfo.getCompanyTargetInfoPK().getTargetId()< 0)
                companyTargetInfo.setCompanyTargetInfoPK(null);
            
            if (companyTargetInfo.getCompanyTargetInfoPK() == null && !StringUtils.isEmpty(companyTargetInfo.getCompanyTargetData())) {
                CompanyTargetInfoPK companyTargetInfoPK = new CompanyTargetInfoPK(CompanyTargetInfo.COMPANY_TARGET_COMPANY, cid, flagType);
                _CreateCompanyInfo(companyTargetInfoPK, companyTargetInfo, m, em_master);
            } else if (companyTargetInfo.getCompanyTargetInfoPK() != null && !StringUtils.isEmpty(companyTargetInfo.getCompanyTargetData())) {
                JPAUtils.edit(companyTargetInfo, em_master, false);
            } else if (companyTargetInfo.getCompanyTargetInfoPK() != null && StringUtils.isEmpty(companyTargetInfo.getCompanyTargetData())) {
                companyTargetInfo.setCompanyTargetDeleted((short) 1);
                JPAUtils.edit(companyTargetInfo, em_master, false);
            }
        }
    }
    
    /***
     * Tạo mới 1 {@link CompanyTargetInfo}
     * @param ctiPk
     * @param cti
     * @param m 
     */
    private void _CreateCompanyInfo(CompanyTargetInfoPK ctiPk, CompanyTargetInfo cti, Member m, EntityManager em_master) throws Exception {
        cti.setCompanyTargetInfoPK(ctiPk);
        cti.setCreatedTime(Calendar.getInstance().getTime());
        cti.setCompanyTargetDeleted((short) 0);
        cti.setCreatorId(m.getMemberId());
        JPAUtils.create(cti, em_master, false);
    }

    /***
     * Xóa 1 {@link CompanyTargetInfo}.
     * @param companyDeleted danh sách sẽ được đánh dấu là xóa.
     */
    private void _DeleteCompanyTargetInfo(List<CompanyTargetInfo> companyDeleted, EntityManager em_master) throws Exception {
        for (CompanyTargetInfo companyTargetInfo : companyDeleted) {
            if (companyTargetInfo.getCompanyTargetInfoPK() != null && companyTargetInfo.getCompanyTargetInfoPK().getTargetId() > 0)
                JPAUtils.edit(companyTargetInfo, em_master, false);
        }
    }
    
    private void deleteAllUnionCompanyKey(String unionCompanyKey, EntityManager em_master) throws Exception {
        String sql = "DELETE FROM UnionCompanyRel ucr WHERE ucr.crmUnionCompanyRelPK.companyUnionKey = :companyUnionKey";
        Query query = JPAUtils.buildJQLQuery(em_master, sql).setParameter("companyUnionKey", unionCompanyKey);
//        query.executeUpdate();
        JPAUtils.executeDeleteOrUpdateQuery(em_master, query);
    }
    
    private static final Integer MASK_COMPANYID = -1;
    private void _StoreUnionCompanys(Company c, List<UnionCompanyRelModel> unionCompanyRelModels, EntityManager em_master)
            throws Exception {
        if(unionCompanyRelModels.isEmpty()) return;
        
        // truong hop tao moi, thay doi mask id cua cong ty them moi thanh id that khi da luu cong ty vao db.
        for (UnionCompanyRelModel ucr : unionCompanyRelModels) {
            if (ucr.getIds().contains(MASK_COMPANYID)) {
                ucr.getIds().remove(MASK_COMPANYID);
                ucr.getIds().add(c.getCompanyId());
                break;
            }
        }
        
        // tam thoi de cong ty saving chua thuoc groups nao.
        c.setCompanyUnionKey(null); // preupdate set to null.
        // duyet tat ca cac items.
        for (UnionCompanyRelModel model : unionCompanyRelModels) {
            // xoa cac union-key ton tai trong db tu danh sach ITEMS.
            String ukey = model.getCompanyUnionKey();
            List<UnionCompanyRel> exists = unionCompanyRelService.findByUnionKey(ukey);
            if (exists != null && !exists.isEmpty())
                // delete the old unionkey, after that update it.
                deleteAllUnionCompanyKey(ukey, em_master);
            else // tao moi union-key.
                ukey = generalUnionCompanyGroup();
            
            // luu groups cong ty tu union-key moi.
            _StoreUnionCompanys(c, model.getIds(), ukey, em_master);
        }
    }
    
    private void _StoreUnionCompanys(Company c, List<Integer> ids, String ukey, EntityManager em_master)
            throws Exception {
        if(ids.size() <= 0) return;
        if(ids.size() == 1) { // neu danh sach chi co 1 cong ty thi can thiep lap cong ty do co union-key la NULL.
            Company ct = find(ids.get(0));
            // kiểm tra nếu tìm được công ty trong DB thì mới thực hiện.
            if(ct != null) {
                ct.setCompanyUnionKey(null);
                JPAUtils.edit(ct, em_master, false);
            }
        } else { // neu danh sach tu 2 cong ty tro nen, can nhom cac cong ty do lai voi nhau.
            for (Integer id : ids) {
                UnionCompanyRel ucr = new UnionCompanyRel(new UnionCompanyRelPK(ukey, id));
                JPAUtils.create(ucr, em_master, false);
                if(id.intValue() != c.getCompanyId().intValue()) {
                    Company ct = em_master.find(Company.class, id);
                    ct.setCompanyUnionKey(ukey);
                    JPAUtils.edit(ct, em_master, false);
                } else {
                    c.setCompanyUnionKey(ukey);
                }
            }
        }
    }
    
    /***
     * Hàm xử lí trả về mã company_union_key được sinh theo ngày tháng.
     * @return 
     */
    private String generalUnionCompanyGroup() {
        StringBuilder r = new StringBuilder();
        Calendar c = Calendar.getInstance();
        r.append(c.get(Calendar.YEAR))
                .append(c.get(Calendar.MONTH))
                .append(c.get(Calendar.DATE))
                .append(c.get(Calendar.HOUR_OF_DAY))
                .append(c.get(Calendar.MINUTE))
                .append(c.get(Calendar.SECOND));

        return r.toString();
    }
    
    /**
     * Luu mat khau basic authentication khi user thay doi trong man hinh quan ly cong ty
     * @param companyId
     * @param loginId
     * @param encodePassword 
     * @return  
     * @throws java.lang.Exception  
     */
    @Override
    public Integer saveCompanyBasicAuth(Integer companyId, String loginId, String encodePassword) throws Exception {
        int rowOfUpdated = 0;
        EntityManager em_master = null;
        EntityTransaction tx = null;
        try {
            em_master = masterEntityManager.getEntityManager();
            tx = beginTransaction(em_master);
            Query q = JPAUtils.buildJQLQuery(em_master, "UPDATE Company c SET c.companyBasicLoginId = :loginId, c.companyBasicPassword = :encodePassword WHERE c.companyId = :companyId");
            q.setParameter("companyId", companyId);
            q.setParameter("loginId", loginId);
            q.setParameter("encodePassword", encodePassword);
            rowOfUpdated = q.executeUpdate();
            commitAndCloseTransaction(tx);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            rollbackAndCloseTransaction(tx);
        } finally {
            JPAUtils.release(em_master, true);
        }
        return rowOfUpdated;
    }
     
    @Override
    public Boolean checkExistBasicAuth(Integer companyId, String loginId) {
        EntityManager em_master = null;
        try {
            em_master = masterEntityManager.getEntityManager();
            Query q = em_master.createQuery("SELECT CASE WHEN COUNT(c) > 0 THEN TRUE ELSE FALSE END FROM Company c WHERE c.companyId != :companyId AND c.companyBasicLoginId = :loginId");
            q.setParameter("loginId", loginId);
            q.setParameter("companyId", companyId);

            Object o = q.getSingleResult();
            if(o instanceof Boolean) return (Boolean) o;
            else if(o instanceof Long) return ((Long) o) > 0;
            return Boolean.TRUE;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_master, true);
        }
        return Boolean.FALSE;
    }

    @Override
    public List<Company> findAllCompanyIsExist() {
        EntityManager em_master = null;
        try{
            em_master = masterEntityManager.getEntityManager();
            String query = " select * from crm_company where company_id=1 "
                    + " union "
                    + " select cc.* from crm_company cc "
                    + " inner join crm_database_server_company_rel cr on cr.company_id=cc.company_id "
                    + " inner join crm_database_server cs on cs.database_server_id=cr.database_server_id "
                    + " where cc.company_deleted=0 ";
            List<Company> list = em_master.createNativeQuery(query, Company.class).getResultList();
            if(list == null) return new ArrayList<>();
            return list;
        }catch(Exception e){
            LOGGER.error(e.getMessage(), e);
        } finally {
            JPAUtils.release(em_master, true);
        }
        return new ArrayList<>();
    }
}
