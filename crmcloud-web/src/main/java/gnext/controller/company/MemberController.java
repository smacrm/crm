package gnext.controller.company;

import gnext.bean.Company;
import gnext.bean.CompanyTargetInfo;
import gnext.bean.CompanyTargetInfoPK;
import gnext.bean.Group;
import gnext.bean.Member;
import gnext.bean.Prefecture;
import gnext.bean.ZipCode;
import gnext.bean.attachment.Attachment;
import gnext.bean.attachment.Server;
import gnext.bean.role.Role;
import gnext.controller.AbstractController;
import gnext.controller.common.LayoutController;
import gnext.controller.common.LoginController;
import gnext.dbutils.model.enums.AttachmentTargetType;
import gnext.dbutils.model.enums.ServerFlag;
import gnext.dbutils.util.FileUtil;
import gnext.exporter.Export;
import gnext.exporter.excel.MemberExportXls;
import gnext.filetransfer.FileTransferFactory;
import gnext.filetransfer.Parameter;
import gnext.filetransfer.TransferType;
import gnext.importer.Import;
import gnext.importer.excel.MemberImport;
import gnext.model.FileUploader;
import gnext.model.authority.UserModel;
import gnext.model.company.member.MemberLazyList;
import gnext.model.company.member.MemberModel;
import gnext.model.search.SearchFilter;
import gnext.multitenancy.service.MultitenancyService;
import gnext.security.SecurityService;
import gnext.security.annotation.SecureMethod;
import gnext.security.annotation.SecurePage;
import gnext.security.notification.DeviceSessionHandler;
import gnext.security.twofa.TimeBasedOneTimePasswordUtil;
import gnext.service.CompanyService;
import gnext.service.CompanyTargetInfoService;
import gnext.service.GroupService;
import gnext.service.MemberImportService;
import gnext.service.MemberService;
import gnext.service.MultipleMemberGroupRelService;
import gnext.service.PrefectureService;
import gnext.service.UnionCompanyRelService;
import gnext.service.ZipCodeService;
import gnext.service.attachment.AttachmentService;
import gnext.service.attachment.ServerService;
import gnext.service.role.RolePageMethodService;
import gnext.service.role.RoleService;
import gnext.service.role.SystemUseAuthRelService;
import gnext.util.JsfUtil;
import gnext.util.ResourceUtil;
import gnext.util.StatusUtil;
import gnext.util.StringUtil;
import gnext.utils.EncoderUtil;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;
import javax.inject.Inject;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jboss.aerogear.security.otp.api.Base32;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author havd
 */
@ManagedBean(name = "memberController", eager = true)
@SessionScoped()
@SecurePage(module = SecurePage.Module.COMPANY, require = true)
public class MemberController extends AbstractController {
    private static final long serialVersionUID = 9191046182247730204L;
    private static final Logger LOGGER = LoggerFactory.getLogger(MemberController.class);

    @EJB @Getter @Setter private MemberService memberService;
    @EJB @Getter @Setter private MultipleMemberGroupRelService mmgrs;
    @EJB @Getter @Setter private MemberImportService memberImportService;
    @EJB @Getter @Setter private ServerService serverService;
    @EJB @Getter @Setter private AttachmentService attachmentService;
    @EJB @Getter @Setter private SystemUseAuthRelService systemUseAuthRelService;
    @EJB @Getter @Setter private RoleService roleService;
    @EJB @Getter @Setter private GroupService groupService;
    @EJB @Getter @Setter private CompanyTargetInfoService companyTargetInfoService;
    @EJB @Getter @Setter private UnionCompanyRelService unionCompanyRelService;
    @EJB @Getter @Setter private CompanyService companyService;
    @EJB @Getter @Setter private RolePageMethodService rolePageMethodService;
    @EJB @Getter @Setter private PrefectureService prefectureService;
    @EJB @Getter @Setter private ZipCodeService zipCodeService;
    @EJB @Getter @Setter private MultitenancyService multitenancyService;
    
    @ManagedProperty(value = "#{layout}")  @Getter @Setter private LayoutController layout;
    @ManagedProperty(value = "#{sec}") @Getter @Setter private SecurityService sec;
    @ManagedProperty(value = "#{loginController}")  @Getter @Setter private LoginController loginController;

    @Getter @Setter private String query; // chỉ lấy member thuộc công ty đăng nhập.
    @Getter @Setter private LazyDataModel<MemberModel> memberModels; // danh sách member thuộc công ty đăng nhập.
    @Getter @Setter private List<Member> members;
    
    @Getter @Setter private MemberModel memberModel; // model lưu trữ thông tin người dùng nhập.
    @Getter @Setter private UploadedFile logo; // logo của member.
    
    @Setter @Getter private String oldMemberLoginIdWhenEdit; // lưu password cũ dùng so sánh khi chỉnh sửa nếu # nhau thì cập nhật.
    @Getter @Setter private String memberLoginIdDisplay; // hiển thị mask mật khẩu của member.
    
    @Getter @Setter private List<Role> roles; // danh sách roles lấy từ công ty đăng nhập hoặc được refresh khi super-admin chọn công ty.
    @Getter @Setter private List<Group> groups; // danh sách groups lấy từ công ty đăng nhập hoặc được refresh khi super-admin chọn công ty.
    
    @Getter @Setter private Integer selectMemberId;
    
    @Getter @Setter private List<Company> companies; // danh sách toàn bộ công ty cho super-admin sử dụng.
    @Getter @Setter private Integer companyId; // id công ty của member lựa chọn từ danh sách được dùng để so sánh với công ty super-admin lựa chọn.
    
    @Getter @Setter private boolean exclusiveCompanyFlag = false; // nếu member thuộc công ty group thì hiển thị checkbox cho phép logined các công ty groups.
    @Getter @Setter private List<Company> companyBelongGroup; // danh sách các công ty group với công ty logined.
    
    @Getter @Setter private List<Prefecture> prefectures; // danh sách tỉnh quận..

    @Setter @Getter private FileUploader fileUploader;
    @Setter @Getter private UploadedFile uploadedFile;
    
    @Inject private DeviceSessionHandler sessionHandler;
    
    @SecureMethod(value = SecureMethod.Method.INDEX, require = false)
    public void load() {
        if (sec.hasMethod("MemberController", "search")) {
            this.layout.setCenter("/modules/company/member/index.xhtml");
        } else {
            _Load(UserModel.getLogined().getUserId(), "view");
            this.layout.setCenter("/modules/company/member/view.xhtml");
        }
    }
    
    @PostConstruct
    public void init() {
        query = "1=1 AND m.group.company.companyId=" + getCurrentCompanyId();
        memberModels = new MemberLazyList(this);
        
        prefectures = new ArrayList<>();
        fileUploader = new FileUploader();
    }

    private void prepareViewMember(final String showType) {
        String rowNum = this.getParameter("rowNum");
        if(NumberUtils.isDigits(rowNum)) setCurrentRowNum(Integer.parseInt(rowNum));
        _Load(Integer.parseInt(this.getParameter("memberId")), showType);
        this.layout.setCenter("/modules/company/member/view.xhtml");
    }
    
    private void prepareEditMember(final String showType) {
        loadAllCompanys();
        _Load(Integer.parseInt(this.getParameter("memberId")), showType);
        this.layout.setCenter("/modules/company/member/edit.xhtml");
    }
    
    private void prepareCreateMember(final String showType) {
        loadAllCompanys();
        _Load(null, showType);
        this.layout.setCenter("/modules/company/member/create.xhtml");
    }
    
    @SecureMethod(SecureMethod.Method.VIEW)
    @Override
    public void show(ActionEvent event) {
        try {
            show2FAAsPopUp = false; // hungpd 20161215 2FA
            final String showType = this.getParameter("showType");
            if ("view".equals(showType)) {
                prepareViewMember(showType);
            } else if ("edit".equals(showType)) {
                prepareEditMember(showType);
            } else if ("create".equals(showType)) {
                prepareCreateMember(showType);
            } else if ("list".equals(showType)) {
                this.layout.setCenter("/modules/company/member/index.xhtml");
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            JsfUtil.addErrorMessage(e, e.getMessage());
        }
    }

    private void loadAllCompanys() {
        this.companies = companyService.findAll();
    }
    private void loadPrefectures() {
        if(prefectures == null) prefectures = new ArrayList<>();
        else prefectures.clear();
        prefectures.addAll(prefectureService.findCities(getLocale()));
    }
    
    private void _Load(Integer memberId, String type) {
        try {
            loadPrefectures();
            
            if ("view".equals(type) || "edit".equals(type)) {
                Member member = null;
                if(sec.isLoggedInOtherCompany(memberId)) {
                    member = multitenancyService.findMemberOnSlaveById(UserModel.getLogined().getCompanyId(), memberId);
                } else {
                    member = memberService.findByMemberId(memberId);
                }
                
                oldMemberLoginIdWhenEdit = member.getMemberLoginId();
                memberLoginIdDisplay = member.getMemberLoginId().substring(5, member.getMemberLoginId().length());
                companyId = member.getGroup().getCompany().getCompanyId();
                
                this.memberModel = new MemberModel(member);
                this.memberModel.updateExtraInfors(this);
                this.memberModel.updateMemberGroupRelInfos(this);
                
                // kiểm tra công ty logined có group không nếu có lấy danh sách các công ty đó.
                if (isCheckCompanyGroup(this.companyId, new ArrayList<>()))
                    this.exclusiveCompanyFlag = true;
            } else {
                Member member = new Member();
                member.setGroup(new Group());
                member.getGroup().setCompany(new Company());
                
                oldMemberLoginIdWhenEdit = StringUtils.EMPTY;
                memberLoginIdDisplay = StringUtils.EMPTY;
                        
                this.memberModel = new MemberModel(member);
                
                // kiểm tra công ty logined có group không nếu có lấy danh sách các công ty đó.
                if (isCheckCompanyGroup(getCurrentCompanyId(), new ArrayList<>()))
                    exclusiveCompanyFlag = true;
            }

            // lấy danh sách group theo công ty đăng nhập hoặc compnay đựoc chọn từ super-admin.
            Integer companyIdOfMemberCurrent = this.memberModel.getMember().getGroup().getCompany().getCompanyId();
            Integer memberIdCurrent = this.memberModel.getMember().getMemberId();
            if("view".equals(type)) {
                _FindGroupByCompany(memberIdCurrent, companyIdOfMemberCurrent);
            } else {
                _FindGroupByCompany(memberIdCurrent, companyIdOfMemberCurrent);
            }
            
            // lấy danh sách role theo công ty đăng nhập hoặc compnay đựoc chọn từ super-admin.
            _FindRoleByCompany(companyIdOfMemberCurrent);
            this.memberModel.displayRole(this);
            this.memberModel.displayMemberGroupRel(this);

            _UpdateMemberInfor(type);
            memberModel.addEmptyInfos();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
    
    private void focusPasswordValidate() {
        UIComponent component = JsfUtil.findComponent("j_password");
        if(component == null) return;
        UIInput uiInput = (UIInput) component;
        uiInput.setValid(false);
    }

    private void _FindGroupByCompany(Integer memberId, Integer companyGroupId) {
        try {
            // trường hợp member logined vào công ty khác các group sẽ lấy trên slave.
            if(sec.isLoggedInOtherCompany(memberId)) {
                groups = multitenancyService.findAllGroupUnderSlave(companyGroupId); return;
            }
            // trường hợp login là super-admin.
            if(sec.isMaster() && companyGroupId != null) {
                groups = groupService.findGroupTree(companyGroupId, null); return;
            }
            if(sec.isMaster() && companyGroupId == null) {
                groups = new ArrayList<>(); return;
            }
            if(!sec.isMaster()) {
                int _companyId = UserModel.getLogined().getCompanyId();
                groups = groupService.findGroupTree(_companyId, null); return;
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            groups = new ArrayList<>();
        }
    }
    private List<Role> _FindRoleByCompany(Integer companyRoleId) {
        if (sec.isMaster()) {
            if (companyRoleId != null) {
                roles = roleService.search(companyRoleId, null);
            } else {
                roles = new ArrayList<>();
            }
        } else {
            roles = roleService.search(UserModel.getLogined().getCompanyId(), null);
        }
        return this.roles;
    }
    private void _UpdateMemberInfor(String type) {
        fileUploader.reset();
        
        if ("edit".equals(type) || "view".equals(type))
            memberModel.initInfos(companyTargetInfoService);
        
        memberModel.initDeletedInfos();
        
        if (memberModel.getMember().getMemberCity() != null) {
            Prefecture prefecture = prefectureService.findByPrefectureCode(getLocale(), String.valueOf(memberModel.getMember().getMemberCity()));
            if (prefecture != null) memberModel.setDisplayMemberCity(prefecture.getPrefectureName());
        }
        
        loadLogoRemote();
        // _BuildPrefectureFromZipCode(memberModel.getMember().getMemberPost());
    }
    
    private void loadLogoRemote() {
        try {
            Integer memberId = memberModel.getMember().getMemberId();
            
            if(memberId == null) return;
            
            int memberCompanyId = memberModel.getMember().getGroup().getCompany().getCompanyId();
            
            List<Server> servers = serverService.getAvailable(memberCompanyId, TransferType.FTP.getType(), ServerFlag.COMMON.getId());
            if(servers == null || servers.isEmpty()) return;
            
            int attachmentTargetType = AttachmentTargetType.MEMBER.getId();
            
            List<Attachment> attachments = attachmentService.search(memberCompanyId, attachmentTargetType, memberId, (short) 0);
            
            if(attachments == null || attachments.isEmpty()) return;
            
            Server server = servers.get(0);
            Attachment attachment = attachments.get(0);
            
            String host = server.getServerHost();
            int port = server.getServerPort();
            String username = server.getServerUsername();
            String password = server.getDecryptServerPassword();
            boolean security = StatusUtil.getBoolean(server.getServerSsl());
            String protocol = server.getServerProtocol();
            String servertype = server.getServerType();
            
            TransferType tt = TransferType.getTransferType(servertype);
            Parameter param_ftp = Parameter.getInstance(tt).manualconfig(true).storeDb(false);;
            param_ftp.host(host).port(port).username(username).password(password).security(security).protocol(protocol);
            
            String remotePath2File = attachment.getAttachmentPath() + File.separator + attachment.getAttachmentName();
            InputStream input = FileTransferFactory.getTransfer(param_ftp).download(remotePath2File);
            fileUploader.setStreamBytes(FileUtil.copyFromInputStream(input));
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }
    }

    public void onBlurMemberPost(AjaxBehaviorEvent event) {
        UIInput input = (UIInput) event.getSource();
        if (input.getValue() != null) {
            String zipCode = input.getValue().toString();
            if(StringUtils.isEmpty(zipCode)) return;
            
            ZipCode zc = zipCodeService.findByZipCode(zipCode, getLocale());
            if(zc == null || zc.getCodeId() == null) return;
            
            String cityKaneJii = zc.getCityKannji();
            List<Prefecture> listPrefecture = prefectureService.findCities(getLocale(), cityKaneJii);
            if(!listPrefecture.isEmpty()) memberModel.getMember().setMemberCity(listPrefecture.get(0).getPrefectureId());
            if(StringUtils.isEmpty(memberModel.getMember().getMemberAddress())){
                memberModel.getMember().setMemberAddress(String.format("%s%s", zc.getAddressKannji(), zc.getDistrictKanj()));
            }
            if(StringUtils.isEmpty(memberModel.getMember().getMemberAddressKana())){
                memberModel.getMember().setMemberAddressKana(String.format("%s%s%s", zc.getCityKana(), zc.getAddressKana(), zc.getDistrictKana()));
            }
            
        }
    }
    
    @SecureMethod(value=SecureMethod.Method.NONE,require = false)
    public void addInfo(ActionEvent event){
        String type = getParameter("type");
        List<CompanyTargetInfo> ctis = memberModel.getExtraInfo(type);
        ctis.add(new CompanyTargetInfo(new CompanyTargetInfoPK(0 - (ctis.size()+1))));
    }
    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void removeInfos(CompanyTargetInfo cti){
        cti.setCompanyTargetDeleted((short)1);
        String type = getParameter("type");
        List<CompanyTargetInfo> companyTargetInfos = memberModel.getExtraInfo(type);
        List<CompanyTargetInfo> companyTargetInfosDeleted = memberModel.getExtraInfoMarkDeleted(type);
        companyTargetInfosDeleted.add(cti);
        companyTargetInfos.remove(cti);
    }

    @Override
    protected void afterPaging() {
        memberModels.setRowIndex(currentRowNum);
        memberModel = memberModels.getRowData();
        memberModel.updateExtraInfors(this);
        memberModel.addEmptyInfos();
        fileUploader.reset();
        loadLogoRemote();
    }

//    @SecureMethod(SecureMethod.Method.DELETE)
//    @Override
//    public void delete(ActionEvent event) {
//        try {
//            String memberId = this.getParameter("memberId");
//            Member member = memberService.findByMemberId(Integer.valueOf(memberId));
//            member.setMemberDeleted(StatusUtil.DELETED);
//            memberService.edit(member);
//            forceCloseSession(member);
//
//            this.layout.setCenter("/modules/company/member/index.xhtml");
//            JsfUtil.addSuccessMessage(ResourceUtil.message(UserModel.getLogined().getCompanyId(), ResourceUtil.BUNDLE_MSG, "msg.action.delete.success", member.getMemberLoginId()));
//        } catch (Exception e) {
//            LOGGER.error(e.getMessage(), e);
//            JsfUtil.addErrorMessage(ResourceUtil.message(UserModel.getLogined().getCompanyId(), ResourceUtil.BUNDLE_MSG, "msg.system.error"));
//        }
//    }
    
    /**
     * Close session cua user dang dang nhap
     * @param memberId 
     */
    private void forceCloseSession(Member member){
        if(sessionHandler != null && member != null){
            sessionHandler.forceCloseIfExistsSession(member.getGroup().getCompany().getCompanyId(), member.getMemberLoginId());
        }
    }

    public void onChangeSelectMemberId(ActionEvent event) {
        String memberId = getParameter("memberId");
        setSelectMemberId(Integer.parseInt(memberId));
    }

    private boolean parseLoginId(String _memberLoginIdDisplay, Member member, Group group) {
        if(StringUtils.isEmpty(_memberLoginIdDisplay)) return false;
        
        String memberLoginId = _memberLoginIdDisplay;
        member.setMemberLoginId(StringUtil.generatePrefixLoginId(memberLoginId, group.getCompany().getCompanyId()));
        return true;
    }
    
    private void parse2FA(Member member) {
        if(member == null) return;
        if(member.getGroup().getCompany().isUsingTwoFactor() && member.isUsing2FA()) {
            if(StringUtils.isEmpty(member.getSecret())) member.setSecret(Base32.random());
        } else {
            member.setUsing2FA(false);
            member.setSecret(null);    
        }
    }
    
    @SecureMethod(SecureMethod.Method.CREATE)
    @Override public void save(ActionEvent event) {
        try {
            Member member = this.memberModel.getMember();
            if(!StringUtil.validatePassword(member.getMemberPassword())) { focusPasswordValidate(); return; }
            
            Group group = groupService.findByGroupId(member.getGroup().getGroupId());
            member.setMemberLoginId(StringUtil.generatePrefixLoginId(memberLoginIdDisplay, group.getCompany().getCompanyId()));
            
            if (checkDuplicateMemberLoginId(memberLoginIdDisplay, group.getCompany().getCompanyId())) {
                focusMemberLoginId(member.getMemberLoginId().substring(5, member.getMemberLoginId().length())); return;
            }
            
            member.setMemberPassword(EncoderUtil.getPassEncoder().encode(member.getMemberPassword()));
            member.setMemberResetPwdDatetime(Calendar.getInstance().getTime());
            member.setMemberFirewall(this.memberModel.getMemberFirewall());
            member.setMemberGlobalFlag(this.memberModel.getMemberGlobalFlag());
            if(member.getMemberManagerFlag() != 1) memberModel.setMemberGroupRelIds(new Integer[0]);
            
            parse2FA(member);
            
            memberService.insert(UserModel.getLogined().getCompany(), member, UserModel.getLogined().getMember(), fileUploader.getBytes(), memberModel.getMemberPhones(), memberModel.getMemberPhonesDeleted(),
                    memberModel.getMemberMobilePhones(), memberModel.getMemberMobilePhonesDeleted(),
                    memberModel.getMemberEmails(), memberModel.getMemberEmailsDeleted(), memberModel.getMemberGroupRelIds(),
                    memberModel.getRoleIds(),
                    memberModel.isAsMemberGlobalFlag(), companyBelongGroup, mapCompanyGroupIds);
            
            this.layout.setCenter("/modules/company/member/index.xhtml");
            JsfUtil.addSuccessMessage(msgBundle.getString("msg.action.create.success", memberLoginIdDisplay));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            JsfUtil.addErrorMessage(msgBundle.getString("msg.system.error"));
        }
    }
    
    @SecureMethod(SecureMethod.Method.UPDATE)
    @Override public void update(ActionEvent event) {
        try {
            if(sec.isLoggedInOtherCompany(this.memberModel.getMember().getMemberId())) {
                updateOnGroup(event);
                return;
            }
            
            Member member = memberModel.getMember();
            if (!StringUtils.isEmpty(this.memberModel.getMemberPassword())) {
                if(!StringUtil.validatePassword(this.memberModel.getMemberPassword())) {
                    focusPasswordValidate();
                    return;
                } else {
                    member.setMemberResetPwdDatetime(Calendar.getInstance().getTime());
                    member.setMemberPassword(EncoderUtil.getPassEncoder().encode(this.memberModel.getMemberPassword()));
                }
            }
            Group group = groupService.findByGroupId(member.getGroup().getGroupId());
            member.setMemberLoginId(StringUtil.generatePrefixLoginId(memberLoginIdDisplay, group.getCompany().getCompanyId()));
            
            if (!oldMemberLoginIdWhenEdit.equals(member.getMemberLoginId()) && checkDuplicateMemberLoginId(memberLoginIdDisplay, group.getCompany().getCompanyId())) {
                focusMemberLoginId(member.getMemberLoginId().substring(5, member.getMemberLoginId().length()));
                return;
            }
            parse2FA(member);
            
            member.setMemberFirewall(this.memberModel.getMemberFirewall());
            member.setMemberGlobalFlag(this.memberModel.getMemberGlobalFlag());
            if(member.getMemberManagerFlag() != 1) memberModel.setMemberGroupRelIds(new Integer[0]);
            
            member = memberService.edit(UserModel.getLogined().getCompany(), member, UserModel.getLogined().getMember(), fileUploader.getBytes(), memberModel.getMemberPhones(), memberModel.getMemberPhonesDeleted(),
                    memberModel.getMemberMobilePhones(), memberModel.getMemberMobilePhonesDeleted(), 
                    memberModel.getMemberEmails(), memberModel.getMemberEmailsDeleted(), memberModel.getMemberGroupRelIds(),
                    memberModel.getRoleIds(),
                    memberModel.isAsMemberGlobalFlag(), companyBelongGroup,
                    memberModel.isChangeCompany(), mapCompanyGroupIds);
            
            // trường hợp sửa avatar của member trùng với member đăng nhập thì thực hiện render lại avatar của member đó.
            if(UserModel.getLogined().getMember() != null && UserModel.getLogined().getMember().getMemberId().equals(member.getMemberId())){
                UserModel.getLogined().setStreamBytesForMemberImage(fileUploader.getBytes());
            }
            
            
            if(member.getMemberDeleted() == StatusUtil.DELETED){
                forceCloseSession(member);
            }
            
            this.load();
            JsfUtil.addSuccessMessage(msgBundle.getString("msg.action.update.success", memberLoginIdDisplay));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            JsfUtil.addErrorMessage(msgBundle.getString("msg.system.error"));
        }
    }
    private void updateOnGroup(ActionEvent event) {
        try {
            Member member = memberModel.getMember();
            member.setGroupId(member.getGroup().getGroupId());
            multitenancyService.updateMemberOnGroup(UserModel.getLogined().getCompanyId(), member, memberModel.getMemberGroupRelIds());
            this.load();
            JsfUtil.addSuccessMessage(msgBundle.getString("msg.action.update.success", memberLoginIdDisplay));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            JsfUtil.addErrorMessage(msgBundle.getString("msg.system.error"));
        }
    }
    
    public void onSelectMemberIdOnListForPrepareDelete(ActionEvent event) {
        String memberId = getParameter("memberId");
        setSelectMemberId(Integer.parseInt(memberId));
    }
    
    @SecureMethod(SecureMethod.Method.DELETE)
    @Override 
    public void delete(ActionEvent event) {
        try {
            String memberId = this.getParameter("memberId");
            Member member = memberService.findByMemberId(Integer.valueOf(memberId));
            multitenancyService.deleteMemberOnSlaveDB(UserModel.getLogined().getCompanyId(), member);
            
            this.layout.setCenter("/modules/company/member/index.xhtml");
            JsfUtil.addSuccessMessage(JsfUtil.getResource().message(UserModel.getLogined().getCompanyId(), ResourceUtil.BUNDLE_MSG, "msg.action.delete.success", member.getMemberLoginId()));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            JsfUtil.addErrorMessage(JsfUtil.getResource().message(UserModel.getLogined().getCompanyId(), ResourceUtil.BUNDLE_MSG, "msg.system.error"));
        }
    }
    
    public void handleFileUpload(FileUploadEvent event) {
        fileUploader.load(event.getFile());
        memberModel.getMember().setMemberImage( event.getFile().getFileName());
        JsfUtil.addSuccessMessage(JsfUtil.getResource().message(UserModel.getLogined().getCompanyId(), ResourceUtil.BUNDLE_MSG, "msg.action.upload.success", event.getFile().getFileName()));
    }
    public void fileUploadListener(FileUploadEvent event) {
        this.uploadedFile = event.getFile();
    }
    @SecureMethod(value = SecureMethod.Method.NONE,require = false)
    public void cancelUpload(){
        uploadedFile = null;
    }
    
    @SecureMethod(SecureMethod.Method.UPLOAD)
    public void importMemberData() throws IOException{
        try {
            if(this.uploadedFile == null){ JsfUtil.addErrorMessage("エラーがあるので、完了ができない。"); return; }
            Import mImport = new MemberImport(this);
            mImport.execute(uploadedFile.getInputstream());
        } catch(Exception ex) {
            LOGGER.error(ex.getLocalizedMessage());
            JsfUtil.addErrorMessage( JsfUtil.getResource().message(UserModel.getLogined().getCompanyId(), ResourceUtil.BUNDLE_MSG, "msg.system.error"));
        }
    }
    @SecureMethod(SecureMethod.Method.DOWNLOAD)
    public void exportMemberData(String fileName) {
        try {
            Export exporter = new MemberExportXls(this,fileName);
            exporter.execute();
        } catch (Exception ex) {
            LOGGER.error(ex.getLocalizedMessage());
        }
    }
    
    @SecureMethod(value=SecureMethod.Method.SEARCH) public void reload() {}
    @Override
    protected void doSearch(SearchFilter filter) {
        query = "1=1 AND m.group.company.companyId= " + getCurrentCompanyId() + " AND ";
        query = query + (filter != null && !StringUtils.isEmpty(filter.getQuery()) ? filter.getQuery() : "1=1");
    }
    
    public boolean checkDuplicateMemberLoginId(String memberLoginId, Integer companyId) {
        return memberService.findByUsername(memberLoginId, companyId) != null;
    }
    
    private void focusMemberLoginId(String value){
        UIComponent component = JsfUtil.findComponent("j_member_login_id");
        if(component == null) return;
        UIInput uIInput = (UIInput) component;
        uIInput.setValid(false);
        uIInput.setValue(value);
        String title = uIInput.getAttributes().get("title").toString();
        JsfUtil.addErrorMessage(JsfUtil.getResource().message(ResourceUtil.BUNDLE_VALIDATOR_NAME, "validator.duplicate", title));
    }
    public void onSuperAdminChangeCompany() throws Exception {
        // label mặc định cho role member-manager-group.
        this.memberModel.setDisplaymemberGroupRelIds(JsfUtil.getResource().message(ResourceUtil.BUNDLE_COMPANY, "label.member.manager.group", ""));
        
        // xóa các member-manager-group.
        if(this.memberModel.getMemberGroupRelIds() != null) Arrays.fill(this.memberModel.getMemberGroupRelIds(), null);             
        
        // công ty id đang lựa chọn.
        Integer cid = this.memberModel.getMember().getGroup().getCompany().getCompanyId();
        
        // danh sách goup công ty lựa chọn.
        _FindGroupByCompany(this.memberModel.getMember().getMemberId(), cid);
        
        // danh sách role công ty lựa chọn.
        _FindRoleByCompany(cid);
        
        // label mặc định cho role.
        this.memberModel.setDisplayRoleName("ロール");
        
        // reset các role đã chọn trước đó.
        if(this.memberModel.getRoleIds() != null)
            Arrays.fill(this.memberModel.getRoleIds(),null);
        
        // kiểm tra nếu công ty lựa chọn là group thì hiên thị checkbox global_flag.
        List<Integer> includeIds = new ArrayList<>();
        if (cid != null && isCheckCompanyGroup(cid, includeIds) && this.memberModel.getMember().getMemberId() != null && this.memberModel.getMember().getMemberId() > 0) {
            this.exclusiveCompanyFlag = true;
            if (Objects.equals(this.companyId, cid)) {
                _Load(this.memberModel.getMember().getMemberId(), "edit");
            } else {
                this.memberModel.setAsMemberGlobalFlag(false);
            }
        } else if (cid != null && isCheckCompanyGroup(cid, includeIds)) {
            this.exclusiveCompanyFlag = true;
            this.memberModel.setAsMemberGlobalFlag(false);
        } else if (cid != null && !isCheckCompanyGroup(cid, includeIds)) {
            this.exclusiveCompanyFlag = false;
            this.memberModel.updateMemberGroupRelInfos(this);
        }
    }

    public boolean isCheckCompanyGroup(Integer cid, List<Integer> includeIds) throws Exception {
        mapCompanyGroupIds.clear();
        selectCompanyGroupId = 0;
        selectGroupOnCompanyGroupId = 0;
        
        boolean check = false;
        List<Company> listUnionCompany = companyService.findByCompanyBelongGroup(cid);
        if (listUnionCompany != null && listUnionCompany.size() > 1) {
            for (Company company : listUnionCompany) includeIds.add(company.getCompanyId());
            companyBelongGroup = companyService.findCompanyExecludeGroup(cid, includeIds);
            check = true;
        } else {
            check = false;
        }
        
        if(check) {
            if(memberModel.isAsMemberGlobalFlag()) {
                boolean runfirst = true;
                for(Company company : companyBelongGroup) {
                    Member memberOnGroup = multitenancyService.findMemberOnSlaveById(company.getCompanyId(), memberModel.getMember().getMemberId());
                    if(memberOnGroup != null) {
                        mapCompanyGroupIds.put(company.getCompanyId(), memberOnGroup.getGroupId());
                        if(runfirst) {
                            selectCompanyGroupId = company.getCompanyId();
                            selectGroupOnCompanyGroupId = memberOnGroup.getGroupId();
                            groupOnCompanyGroups = groupService.findByCompanyId(selectCompanyGroupId);
                            runfirst = false;
                        }
                    } else {
                        mapCompanyGroupIds.put(company.getCompanyId(), 0);
                    }
                }
            }
        }
        
        return check;
    }

    private Map<Integer, Integer> mapCompanyGroupIds = new HashMap<>();
    @Getter @Setter private Integer selectCompanyGroupId;
    public void onChangeCheckboxMemberGlobalChange() {}
    
    @Getter @Setter private List<Group> groupOnCompanyGroups;
    public void onChangeCompanyGroup() throws Exception {
        if(selectCompanyGroupId == null || selectCompanyGroupId < 0) {
            selectCompanyGroupId = 0;
            groupOnCompanyGroups = new ArrayList<>();
        } else {
            groupOnCompanyGroups = groupService.findByCompanyId(selectCompanyGroupId);
            if(mapCompanyGroupIds.containsKey(selectCompanyGroupId)) {
                selectGroupOnCompanyGroupId = mapCompanyGroupIds.get(selectCompanyGroupId);
            }
        }
    }
    
    @Getter @Setter private Integer selectGroupOnCompanyGroupId;
    public void onChangeGroupOfCompanyGroup() throws Exception {
        mapCompanyGroupIds.put(selectCompanyGroupId, selectGroupOnCompanyGroupId);
    }
    
    public void handleChangeRole() { this.memberModel.populateDisplayRoleName(this); }
    public void handleChangeMemberGroupRel() { this.memberModel.populateDisplayMemberGroupRel(this); }

    public String generateQRUrl() throws Exception { return TimeBasedOneTimePasswordUtil.qrImageUrl(memberModel.getMember()); }
    
    @Getter public boolean show2FAAsPopUp = false;
    public void change2FAMode(){ show2FAAsPopUp = memberModel.getMember().isUsing2FA(); }
    
    public String viewGroupNameOfMember(int memberId) {
        try {
            Member memberOnSlave = multitenancyService.findMemberOnSlaveById(UserModel.getLogined().getCompanyId(), memberId);
            if(memberOnSlave == null) return StringUtils.EMPTY;
            return memberOnSlave.getGroup().getGroupName();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return StringUtils.EMPTY;
    }
}
