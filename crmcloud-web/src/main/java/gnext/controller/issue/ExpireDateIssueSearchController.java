/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.controller.issue;

import gnext.bean.customize.AutoFormItem;
import gnext.bean.customize.AutoFormPageTabDivItemRel;
import gnext.bean.project.ProjectCustColumnWidth;
import gnext.controller.AbstractController;
import gnext.controller.common.LayoutController;
import gnext.controller.common.LoginController;
import gnext.bean.issue.IssueLamp;
import gnext.bean.project.DynamicColumn;
import gnext.bean.project.ProjectCustSearch;
import gnext.model.authority.UserModel;
import gnext.model.customize.Field;
import gnext.model.search.SearchFilter;
import gnext.security.annotation.SecureMethod;
import gnext.security.annotation.SecurePage;
import gnext.service.config.ConfigService;
import gnext.service.customize.AutoFormItemService;
import gnext.service.customize.AutoFormPageTabDivItemService;
import gnext.service.issue.IssueService;
import gnext.service.project.ProjectService;
import gnext.util.DateUtil;
import gnext.utils.InterfaceUtil;
import gnext.util.SelectUtil;
import gnext.util.StringUtil;
import gnext.util.WebFileUtil;
import gnext.utils.InterfaceUtil.FIELDS;
import gnext.utils.InterfaceUtil.PAGE;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.model.SelectItem;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tungdt
 */
@ManagedBean(name = "edis", eager = true)
@SessionScoped
@SecurePage(module = SecurePage.Module.ISSUE, require = false)
public class ExpireDateIssueSearchController extends AbstractController implements Serializable {
    private static final Logger logger = LoggerFactory.getLogger(ExpireDateIssueSearchController.class);
    private static final long serialVersionUID = 6975330674922107020L;

    @Setter @Getter private List<Map<String, Object>> issues = new ArrayList<>();
    @Getter @Setter private List<SelectItem> liveSearchVisibleColumns = new ArrayList<>();
    @Getter @Setter private IssueLamp il;
    @Getter @Setter private String issueExpireTitle;
    @Setter @Getter private Map<String, Integer> columnWidth = new HashMap<>();

    @ManagedProperty(value = "#{layout}")
    @Getter @Setter private LayoutController layout;
    
    @ManagedProperty(value = "#{loginController}")
    @Getter @Setter private LoginController loginController;
    
    @ManagedProperty(value = "#{projectController}")
    @Getter @Setter private ProjectController projectController;

    @EJB private ProjectService projectCusSearchServiceImpl;
    @EJB private ConfigService configServiceImpl;
    @EJB private AutoFormPageTabDivItemService pageTabServiceImpl;
    @EJB private IssueService issueService;
    @EJB private AutoFormItemService autoFormItemServiceImpl;
    
    @Getter @Setter private String keyword;
    @Getter @Setter private String keywordCondition = "OR";
    @Getter @Setter private Date fromDateSearch;
    @Getter @Setter private Date toDateSearch;
    
    @PostConstruct
    public void init(){
        Integer pageId = this.issueService.getCustomizePageId("IssueController", getCurrentCompanyId());
        List<AutoFormPageTabDivItemRel> dynamicColumns =
                this.pageTabServiceImpl.findRelList(pageId, PAGE.DYNAMIC, UserModel.getLogined().getCompanyId());
        List<DynamicColumn> allCols = SelectUtil.getViewDynamicColumns(dynamicColumns);
        for(DynamicColumn dc:allCols) {
            if(dc == null || dc.getId() == null
                    || (!StringUtils.isEmpty(dc.getName()) && dc.getName().startsWith("---"))) continue;
            SelectItem item = new SelectItem();
            String col = dc.getId();
            item.setValue(col);
            item.setLabel(dc.getName());
            if(StringUtil.isExistsInArray(col, SelectUtil.getViewMemoTextarea())
                        || (UserModel.getLogined().getCompanyCustomerMode() && StringUtil.isExistsInArray(col, SelectUtil.getViewMemoCustSpecialTextarea()))) {
                item.setDescription(FIELDS.MEMO_VIEW);
            } else if(col.startsWith(FIELDS.DYNAMIC)) {
                String itemId = col.substring(FIELDS.DYNAMIC.length(), col.length());
                if(NumberUtils.isDigits(itemId)) {
                    AutoFormItem m = autoFormItemServiceImpl.find(Integer.valueOf(itemId));
                    if(m != null && m.getItemType() == Field.Type.TEXTAREA) {
                        item.setDescription(FIELDS.MEMO_VIEW);
                    }
                }
            }
            liveSearchVisibleColumns.add(item);
        }
        columnWidth.clear();
        List<ProjectCustColumnWidth> widthList = projectCusSearchServiceImpl.getColumnWidthList(getCurrentCompanyId());
        for(ProjectCustColumnWidth w : widthList){
            columnWidth.put(w.getColumnId(), w.getColumnWidth());
        }
        
    }

    @SecureMethod(SecureMethod.Method.SEARCH)
    public void filterIssueByIssueLamp(IssueLamp issueLamp) {
        // Setting project value
        if (projectController.getProject().getListId() != null) {
            projectController.setProject(new ProjectCustSearch());
        }
        //
//        issueExpireTitle = issueLamp.getItemViewName(getLocale());
        this.il = issueLamp;

        Calendar c = Calendar.getInstance();

        this.toDateSearch = c.getTime();

        c.add(Calendar.MONTH, -6); //Mac dinh la view trong vong 6 thang
        this.fromDateSearch = c.getTime();

        searchIssue(null, null);
        load();
    }

    @Override
    protected void doSearch(SearchFilter filter) {
        String query = (filter != null && !StringUtils.isEmpty(filter.getQuery())
                ? filter.getQuery() : "1=1");
        List<String> conditionFields = filter != null ? filter.getConditionFields() : new ArrayList<>();
        searchIssue(query, conditionFields);
        load();
    }
    
//    private String buildQuery(){
//        List<IssueLamp> issueLamps = new ArrayList<>(loginController.getMember().getCompany().getIssueLampList());
//        Collections.sort(issueLamps, (a,b)-> a.getLampDates() < b.getLampDates() ? -1 : a.getLampDates() == b.getLampDates() ? 0 : 1);
//        IssueLamp issueLampNear = null;
//        for(IssueLamp issueLamp : issueLamps){
//             if(issueLamp.getLampDates() > il.getLampDates() && Objects.equals(issueLamp.getLampProposalId(), il.getLampProposalId())){
//                issueLampNear = issueLamp;
//                break;
//            }
//        }
//        String query = null;
//        Date date = new Date();
//        DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
//        df.setTimeZone(TimeZone.getDefault());
//        if(issueLampNear != null){
//            query = "datediff('"+df.format(date)+"',"+"issue_receive_date"+") >= "+il.getLampDates()+" AND datediff('"+df.format(date)+"',"+"issue_receive_date"+") < "+issueLampNear.getLampDates() + " AND issue_proposal_level_id = "+il.getLampProposalId();
//        }else {
//            query = "datediff('"+df.format(date)+"',"+"issue_receive_date"+") >= "+il.getLampDates()+" AND issue_proposal_level_id = "+il.getLampProposalId();
//        }
//        
//        if(query != null) return query;
        
//        return "";
//    }
    
    @SecureMethod(value = SecureMethod.Method.DOWNLOAD)
    public void dowload() throws IOException{
        if(this.issues == null) return;

        String name = issueExpireTitle;
        if(com.ocpsoft.pretty.faces.util.StringUtils.isBlank(name)) return;
        
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet(name);
        
        final int[] rownum = {0};
        final int[] headercellnum = {0};
        final Row headerRow = sheet.createRow(rownum[0]++);
        for(SelectItem item : liveSearchVisibleColumns) {
            Cell cell = headerRow.createCell(headercellnum[0]++);
            cell.setCellValue(SelectUtil.nullStringToEmpty(item.getLabel()));
        }
        List<Map<String, Object>> issues = this.issues;
        for (Map<String, Object> map : issues) {
            final Row row = sheet.createRow(rownum[0]++);
            final int[] cellnum = {0};
            for(SelectItem item:liveSearchVisibleColumns) {
                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    if(item == null || item.getValue() == null || !item.getValue().equals(entry.getKey())) continue;
                    Cell cell = row.createCell(cellnum[0]++);
                    cell.setCellValue(SelectUtil.nullStringToEmpty(String.valueOf(entry.getValue())));
                    break;
                }
            }
        }

        SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.PATTERN_CSV_EXPORT_DATE);
        WebFileUtil.forceDownload(String.format("%s_%s.xls", name, sdf.format(new Date())), wb);
    }
    
    /**
     * searchIssue
     * @param advanceQuery chứa query từ advanced search, nếu không có truyền vào null
     */
    private void searchIssue(String advanceQuery, List<String> conditionFields){
        boolean quickSearch = configServiceImpl.get(InterfaceUtil.SERVER_KEY.ELASTIC).equalsIgnoreCase("true");
//        String queryFromLampDate = buildQuery();
        String queryFromLampDate = (this.il!=null && this.il.getLampId()>0)?String.format("[lamp_id=%d]", this.il.getLampId()):"";
        if(advanceQuery != null && ! advanceQuery.isEmpty()){
//            advanceQuery = "("+queryFromLampDate+" && "+advanceQuery+")";
            advanceQuery = queryFromLampDate+" && "+advanceQuery;
        }else {
//            advanceQuery = "("+queryFromLampDate+")";
            advanceQuery = queryFromLampDate;
        }
        issues = projectCusSearchServiceImpl.advanceSearch(
                UserModel.getLogined().getCompanyBusinessFlag(),
                UserModel.getLogined().getCompanyId(),
                UserModel.getLogined().getUserId(),
                quickSearch,
                advanceQuery, (conditionFields == null ? new ArrayList<>() : conditionFields), this.keyword, this.keywordCondition,
                liveSearchVisibleColumns,
                this.fromDateSearch,
                this.toDateSearch,
                getLocale()
        );
    }

    @SecureMethod(value = SecureMethod.Method.INDEX, require = false)
    public void load() {
        this.layout.setCenter("/modules/issue/expire_list.xhtml");
    }
}
