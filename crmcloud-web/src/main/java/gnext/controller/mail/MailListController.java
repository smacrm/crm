/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.controller.mail;

import gnext.bean.mail.MailData;
import gnext.bean.mail.MailFilter;
import gnext.bean.mail.MailFolder;
import gnext.bean.mail.MailPerson;
import gnext.controller.AbstractController;
import gnext.controller.common.LayoutController;
import gnext.dbutils.model.enums.AttachmentTargetType;
import gnext.model.mail.MailDataModel;
import gnext.model.search.SearchFilter;
import gnext.security.annotation.SecureMethod;
import gnext.security.annotation.SecurePage;
import gnext.service.attachment.AttachmentService;
import gnext.service.issue.IssueService;
import gnext.service.mail.MailDataService;
import gnext.service.mail.MailFilterService;
import gnext.service.mail.MailPersonService;
import gnext.util.DateUtil;
import gnext.util.EmailUtil;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author daind
 */
@ManagedBean(name = "mailListController", eager = true)
@SessionScoped()
@SecurePage(module = SecurePage.Module.MAIL, require = true)
public class MailListController extends AbstractController<MailDataModel> {
    private static final long serialVersionUID = -4241562379673921191L;
    private static final Logger LOGGER = LoggerFactory.getLogger(MailListController.class);
    private static final String DEFAULT_SORT = "mail_data_datetime DESC";
    
    // những primefaces bean liên quan tới màn hình.
    @ManagedProperty(value = "#{layout}")
    @Getter @Setter private LayoutController layout;
    @ManagedProperty(value = "#{mailFolderController}")
    @Getter @Setter public MailFolderController mailFolderController;

    // những ejb bean liên quan tới màn hình.
    @EJB private MailFilterService mailFilterService;
    @EJB private MailDataService mailDataService;
    @EJB private AttachmentService attachmentService;
    @EJB private MailPersonService mailPersonService;
    @EJB private IssueService issueService;
    
    @Getter @Setter private List<MailDataModel> datas = new ArrayList<>(); // dữ liệu chính của màn hình sẽ thao tác.
    @Getter @Setter private String query;
    @Getter @Setter private String sort = DEFAULT_SORT; // tham số sort.
    @Getter @Setter private List<SelectItem> sortItems = new ArrayList<>();
    
    // tìm kiếm theo crm_mail_filter
    @Getter @Setter private String filter = StringUtils.EMPTY;
    @Getter @Setter private List<SelectItem> filterItems = new ArrayList<>();
    
    @Getter @Setter private boolean hasAttachment; // tìm kiếm mail chứa attachment.
    @Getter @Setter private boolean readed; // tìm kiếm mail chưa đọc.    
    @Getter @Setter private Map<Integer, Boolean> dataSelected = new HashMap<>(); // lưu trữ những mail người dùng đã chọn.
    @Getter @Setter private boolean checkItemAll; // 
    @Getter @Setter private boolean chkItem;
    @Getter @Setter private String searchKey = StringUtils.EMPTY; // search toàn bộ email theo key.
    
    // tìm kiếm mail theo người phụ trách issue.
    @Getter @Setter private Integer mailPersonId;
    @Getter @Setter private List<MailPerson> mailPersons = new ArrayList<>();
    
    // tìm kiếm theo ngày tháng.
    @Getter @Setter private String queryByDatetime = StringUtils.EMPTY;
    @Getter @Setter private Date from;
    @Getter @Setter private Date to;
    
    @Getter @Setter private boolean displayDetailToolPanel = false;
    @Getter @Setter private String sectionContent;
    
    private void _Load() {
        _ClearData();_LoadSortField();_LoadFilter();_LoadPersonCharge();
        _LoadData();
        _DecoratorChkItemComponent();
    }
    
    private void _LoadPersonCharge() {
        int cid = getCurrentCompanyId();
        mailPersons.addAll(mailPersonService.search(cid, (short) 0));
    }

    private void _ClearData() {
        datas.clear();
        sortItems.clear();
        filterItems.clear();
        mailPersons.clear();
    }

    private void _LoadData() {
        String customSql = buildQuery();
        List<MailData> datas = mailDataService.find(getCurrentCompanyId(), getPaginator().getFirst(), getPaginator().getLimit(), sort, customSql);
        for (int i = 0; i < datas.size(); i++) {
            MailDataModel model = new MailDataModel(datas.get(i));
            model.setRowNum(getPaginator().getFirst() + i);
            model.setAttachments(attachmentService.search(getCurrentCompanyId(), AttachmentTargetType.MAIL.getId(), datas.get(i).getMailDataId(), (short)0));
            this.datas.add(model);
            if (!dataSelected.containsKey(model.getMailData().getMailDataId())) dataSelected.put(model.getMailData().getMailDataId(), checkItemAll);
        } // end for::
        int total = mailDataService.total(getCurrentCompanyId(), customSql);
        getPaginator().setup(total, this.datas);
    }

    public String buildQuery() {
        query = "1=1 AND company_id=" + getCurrentCompanyId();
        
        StringBuilder suffixSql = new StringBuilder();
        if (!StringUtils.isEmpty(mailFolderController.getSelFolderCode())) {
            query = query + " AND mail_data_folder_code=" + mailFolderController.getSelFolderCode();
        }

        if (hasAttachment) {
            query = query + " AND mail_data_id IN (SELECT DISTINCT attachment_target_id FROM crm_attachment WHERE attachment_target_type = "
                    + AttachmentTargetType.MAIL.getId() + " AND company_id = " + getCurrentCompanyId() + ") ";
        }

        if (readed) {
            query = query + " AND (mail_data_is_read = 0 OR mail_data_is_read IS NULL) ";
        }

        if(mailPersonId != null) {
            query = query + " AND mail_data_person_id = " + mailPersonId;
        }
        
        if(!StringUtils.isEmpty(queryByDatetime)) {
            query = query + " AND " + queryByDatetime;
        }
        
        if (!StringUtils.isEmpty(searchKey)) {
            StringBuilder likeSql = new StringBuilder();
            
            // \p{Z} or \p{Separator}: any kind of whitespace or invisible separator.
            String part[] = searchKey.split("\\p{Z}+");
            for (int i = 0; i < MailData.SEARCH_FIELDS.length; i++) {
                String field = MailData.SEARCH_FIELDS[i];
                
                // cho mỗi field và danh sách key cần tìm kiếm sẽ xây dựng bộ query cho field đó.
                // ví dụ: mail_data_subject LIKE '%crm%' OR mail_data_subject LIKE '%cloud%'
                StringBuilder likeSqlForField = new StringBuilder();
                for (int j = 0; j < part.length; j++) {
                    String partSearchKey = part[j];
                    if( j==0 ) {
                        likeSqlForField.append("    ").append(getQueryWithPartSearchKey(field, partSearchKey));
                    } else {
                        likeSqlForField.append(" OR ").append(getQueryWithPartSearchKey(field, partSearchKey));
                    }
                }
                
                // sau khi phân tích mỗi field với danh sách keys, thông tin được đẩy vào câu lệnh bao bọc.
                if( i==0 ) {
                    likeSql.append("    ").append("(").append(likeSqlForField).append(")").append(" ");
                } else {
                    likeSql.append(" OR ").append("(").append(likeSqlForField).append(")").append(" ");
                }
            } // end for::
            
            query = query + " AND (" + likeSql.toString() + ") ";
        } // end if::
        
        return query;
    }

    private String getQueryWithPartSearchKey(String field, String partSearchKey) {
        StringBuilder sb = new StringBuilder();
        sb.append(field).append(" LIKE '%").append(StringUtils.strip(partSearchKey)).append("%' ");
        return sb.toString();
    }
    
    private void _LoadSortField() {
        sortItems.add(new SelectItem("mail_data_id DESC", "ID（降順）"));
        sortItems.add(new SelectItem("mail_data_id ASC", "ID（昇順）"));
        sortItems.add(new SelectItem(DEFAULT_SORT, "送信日（新しい順）"));
        sortItems.add(new SelectItem("mail_data_datetime ASC", "送信日（古い順）"));
    }

    private void _LoadFilter() {
        List<MailFilter> mailFilters = mailFilterService.search(getCurrentCompanyId(), (short) 0);
        for (MailFilter f : mailFilters)
            filterItems.add(new SelectItem(f.getMailFilterId().toString(), f.getMailFilterTitle()));
    }

    private void _DecoratorChkItemComponent() {
        chkItem = true;
        if(datas == null || datas.isEmpty())
            chkItem = false;
        else
            for (MailDataModel mdm : datas) {
                MailData md = mdm.getMailData();
                if (dataSelected.containsKey(md.getMailDataId()) && !dataSelected.get(md.getMailDataId())) chkItem = false;
            }
    }

    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void gotoListMail(String folderCode) {
        mailFolderController.changeFolder(EmailUtil.getExplodeType(folderCode));
        onRefresh(true);
        showOwnerPage();
    }
    
    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void onSortChange() {
        init();
    }

    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void onFilterChange() {
        init();
    }
    
    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void onPersonChange() {
        init();
    }
    
    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void onSentDateChange(Integer month, Integer week, Integer day) {
        queryByDatetime = StringUtils.EMPTY;
        if(month != null && month > 0) {
            queryByDatetime = MessageFormat.format(" DATE(mail_data_datetime) >= DATE(NOW() - INTERVAL {0} MONTH)", month);
            this.from = null; this.to = null;
        }
        if(week != null && week > 0) {
            queryByDatetime = MessageFormat.format(" DATE(mail_data_datetime) >= DATE(NOW() - INTERVAL {0} WEEK)", week);
            this.from = null; this.to = null;
        }
        if(day != null && day > 0) {
            queryByDatetime = MessageFormat.format(" DATE(mail_data_datetime) >= DATE(NOW() - INTERVAL {0} DAY)", day);
            this.from = null; this.to = null;
        }
        if(this.from != null && this.to != null) {
            String sdf = DateUtil.getDateToString(from, "yyy-MM-dd");
            String sdt = DateUtil.getDateToString(to, "yyy-MM-dd");
            queryByDatetime = MessageFormat.format(" mail_data_datetime >= ''{0}'' AND mail_data_datetime <= ''{1}'' ", sdf, sdt);
        }
        init();
    }

    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void onFilterAttachmentChange() {
        init();
    }

    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void onFilterMailReadedChange() {
        init();
    }

    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void onDataItemChange(Integer mailDataId) {
        _DecoratorChkItemComponent();
    }

    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void unCheckItemAll() {
        checkItemAll = Boolean.FALSE;
        for (Map.Entry<Integer, Boolean> entry : dataSelected.entrySet()) entry.setValue(checkItemAll);
        _DecoratorChkItemComponent();
    }

    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void checkItemOnPage() {
        checkItemAll = Boolean.FALSE;
        for (Map.Entry<Integer, Boolean> entry : dataSelected.entrySet()) entry.setValue(checkItemAll);
        for (MailDataModel mdm : datas) dataSelected.put(mdm.getMailData().getMailDataId(), Boolean.TRUE);
        _DecoratorChkItemComponent();
    }

    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void checkItemOnAll() {
        checkItemAll = Boolean.TRUE;
        for (Map.Entry<Integer, Boolean> entry : dataSelected.entrySet()) entry.setValue(checkItemAll);
        _DecoratorChkItemComponent();
    }

    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void onCheckBoxItemChange() {
        for (MailDataModel mdm : datas) dataSelected.put(mdm.getMailData().getMailDataId(), chkItem);
    }

    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void onRefresh(boolean loading) {
        getPaginator().reset();
        mailPersonId = null;
        queryByDatetime = StringUtils.EMPTY;
        this.from = null; this.to = null;
        filter = StringUtils.EMPTY;
        sort = DEFAULT_SORT;
        hasAttachment = false;
        readed = false;
        checkItemAll = false;
        chkItem = false;
        dataSelected.clear();
        searchKey = StringUtils.EMPTY;
        folderToChange = StringUtils.EMPTY;
        if(loading) init();
    }

    /***
     * Hàm xử lí thay đổi folder cho danh sách mail chọn.
     * Áp dụng cho màn hình danh sách và màn hình chi tiết.
     * @param actionMoveToFolder - Nếu khác trống thì thực hiện thay đổi, ngược lại chỉ thiết lập giá trị cho folderToChange.
     */
    @Getter @Setter private String folderToChange = StringUtils.EMPTY;
    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void moveMailToFolder() {
        try {
            String action = getParameter("actionMoveToFolder");
            if (!StringUtils.isEmpty(folderToChange) && !StringUtils.isEmpty(action) && "1".equals(action)) {
                List<Integer> mailIds = new ArrayList<>();

                if(checkItemAll) {
                    List<MailData> mds = mailDataService.find(getCurrentCompanyId(), null, null, sort, buildQuery());
                    for(MailData md : mds) mailIds.add(md.getMailDataId());
                } else {
                    for (Map.Entry<Integer, Boolean> entry : dataSelected.entrySet()) {
                        if (entry.getValue()) mailIds.add(entry.getKey());
                    }
                }

                mailDataService.moveToFolder(getCurrentCompany(), folderToChange, mailIds, null);
                this.init(); mailFolderController.init();
            }

            if("1".equals(action) || "0".equals(action)) {
                folderToChange = StringUtils.EMPTY;
                showOwnerPage();
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /***
     * Hàm xử lí kiểm tra nếu người dùng chọn ít nhất 1 mail.
     * @return 
     */
    public boolean checkSelectedItems() {
        if (checkItemAll || chkItem) return true;
        for (Map.Entry<Integer, Boolean> entry : dataSelected.entrySet()) if (entry.getValue()) return true;
        return false;
    }

    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void searchWithKey() {
//        if(StringUtils.isEmpty(searchKey)) return;
        mailFolderController.changeFolder(StringUtils.EMPTY);;dataSelected.clear(); init(); showOwnerPage();
    }
    
    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void onChangeRowsPerPageTemplate() {
        getPaginator().setCurrentPage(0);
        init();
    }

    @Override
    protected void afterPaging() {
        init();
    }

    @Override
    @SecureMethod(SecureMethod.Method.DELETE)
    public void delete(ActionEvent event) {
        try {
            List<Integer> ids = new ArrayList<>();
            if (checkItemAll) {
                List<MailData> mds = mailDataService.find(getCurrentCompanyId(), null, null, sort, buildQuery());
                for (MailData md : mds) ids.add(md.getMailDataId());
            } else {
                for (Map.Entry<Integer, Boolean> entry : dataSelected.entrySet()) {
                    if (entry.getValue()) ids.add(entry.getKey());
                }
            }
            if (mailFolderController.getSelFolderCode().equals(MailFolder.DATA_MAIL_FOLDER_TRASH)) {
                mailDataService.delete(getCurrentCompany(), ids);
            } else {
                mailDataService.moveToFolder(getCurrentCompany(), MailFolder.DATA_MAIL_FOLDER_TRASH, ids, null);
            }
            gotoListMail(mailFolderController.getSelFolderCode());
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public void init() {
        _Load();
    }

    /***
     * Hàm xử lí quay lại màn hình danh sách từ màn hình chi tiết mail.
     * Hàm sẽ xử lí load lại dữ liệu từ database.
     */
    @SecureMethod(value = SecureMethod.Method.SEARCH)
    public void load() {
//        String menuClick = getParameter("menuClick");
//        if("1".equals(menuClick)) {
//            showOwnerPage();
//        }
        mailFolderController.changeFolder(MailFolder.DATA_MAIL_FOLDER_INBOX);
        onRefresh(true);
        showOwnerPage();
    }
    
    /***
     * Hàm xử lí quay lại màn hình danh sách từ màn hình chi tiết mail.
     * Không load lại dữ liệu.
     */
    @SecureMethod(SecureMethod.Method.VIEW)
    public void view() {
        getPaginator().reset();
        checkItemAll = false;
        chkItem = false;
        // FIXME: Nếu không clear thì trong phần markedReadMail cần xóa bỏ
        // các mailId đã view đi để tránh lỗi khi chọn xóa.
        dataSelected.clear();
        init();
        showOwnerPage();
    }
    
    public void showOwnerPage() {
        layout.setCenter("/modules/mail/list/index.xhtml");
        displayList();
    }
    
    public void displayList(){
        sectionContent = "/modules/mail/list/list.xhtml";
        displayDetailToolPanel = false;
    }
    
    public void displayDetail(){
        sectionContent = "/modules/mail/list/detail.xhtml";
        displayDetailToolPanel = true;
    }

    @Override
    protected void doSearch(SearchFilter filter) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
