package gnext.controller.issue;

import com.google.gson.Gson;
import gnext.bean.issue.Customer;
import gnext.bean.issue.Issue;
import gnext.bean.mente.MenteItem;
import gnext.controller.mail.parse.StandardMailParse;
import gnext.model.authority.UserModel;
import gnext.security.annotation.SecureMethod;
import gnext.security.annotation.SecurePage;
import gnext.service.issue.IssueCustomerService;
import gnext.service.mente.MenteService;
import gnext.service.project.ProjectService;
import gnext.util.DateUtil;
import gnext.util.IssueUtil;
import gnext.util.JsfUtil;
import gnext.util.ResourceUtil;
import gnext.util.SelectUtil;
import gnext.utils.InterfaceUtil.COLS;
import gnext.utils.InterfaceUtil.COMPANY_TYPE;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hungpham
 * @since Jun 12, 2017
 */
@ManagedBean(name = "IssueExecuteImport")
@ViewScoped
@SecurePage(module = SecurePage.Module.ISSUE, require = false)
public class IssueExecuteImport implements Serializable{
    private static final long serialVersionUID = -3258776524040999047L;
    private final Logger LOGGER = LoggerFactory.getLogger(IssueExecuteImport.class);
 
    UserModel isLogin;
    public IssueExecuteImport() {
        isLogin = UserModel.getLogined();
    }

    @ManagedProperty(value = "#{issueImportController}")
    @Getter @Setter private IssueImportController issueImportController;
    
    @ManagedProperty(value = "#{issueController}")
    @Getter @Setter private IssueController issueController;
    
    @ManagedProperty(value = "#{projectController}")
    @Getter @Setter private ProjectController projectController;
    
    @EJB private ProjectService projectService;
    @EJB private MenteService menteService;
    @EJB private IssueCustomerService issueCustomerService;    

    @Getter @Setter private String strMappingField;
    
    List<HSSFRow> removeRows = new ArrayList<>();
    Map<Integer, List<String>> errorLogs = new HashMap<>();
    
    @Getter @Setter private Integer progress = 1;
    @Getter @Setter private Integer rawProgress = 1;
    @Getter @Setter private Integer rawProgressMax = 1;
    
    int successCount = 0;
    
    @SecureMethod(value = SecureMethod.Method.UPLOAD)
    public void doImportData(){
        if(isLogin == null) return;
        successCount = 0;
        List<String> mappingConds = new ArrayList<>();
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        for(int i = 0; i < 5; i++){
            String strMappingConds = params.get("mapping_conditions_" + i);
            if(!StringUtils.isEmpty(strMappingConds)) mappingConds.add(strMappingConds);
        }
        
        Map<String, String> mappingFields = new Gson().fromJson(strMappingField, Map.class);
        if(mappingFields == null){
            boolean hasAllRequired = true;
            // mapping required fields
            for(IssueUtil.ALLOW_SEARCH_COL.REQUIRED key: IssueUtil.ALLOW_SEARCH_COL.REQUIRED.values()){
                if( !params.containsKey(key.name()) ){
                    hasAllRequired = false;
                }
            }
            if(!hasAllRequired){
                JsfUtil.addErrorMessage(JsfUtil.getResource().message(isLogin.getCompanyId(), ResourceUtil.BUNDLE_ISSUE_NAME, "label.import.condition"));
                return;
            }
        }
        // System.out.println(mappingFields);
        // {1=cust_updated_name=1, 2=issue_view_code, 0=issue_receive_name}
        
        String reportContentType = params.containsKey("report_content_type") ? params.get("report_content_type") : "all"; //Output report type: all | minified
        issueImportController.setReportFileType(params.containsKey("report_file_type") ? params.get("report_file_type") : "xls"); //Output report file type: xls, csv
        
        
        HSSFSheet sheet = issueImportController.getSheet();
        HSSFWorkbook wb = issueImportController.getWb();
        
        int numberRows = sheet.getLastRowNum();
        final Map<String, String> fieldMappings = new HashMap<>();
        
        Map<String, String> addedCustomizeFields = new HashMap<>();
        
        removeRows.clear();
        
        rawProgressMax = numberRows;
        for(int r = 1; r <= numberRows; r++){
            HSSFRow row = sheet.getRow(r);
            int celNum = row.getLastCellNum();
            fieldMappings.clear(); 
            boolean isValid = true;
            HashMap<String, Integer> fieldIndexMap = new HashMap();
            List<Integer> errorCells = new ArrayList<>();
            for(int cellIdx = 0; cellIdx < celNum; cellIdx++){
                HSSFCell cell = row.getCell(cellIdx);
                // TODO: Save data
                cell.setCellType(HSSFCell.CELL_TYPE_STRING);
                String value = cell.getStringCellValue();
                String field = mappingFields.get(String.valueOf(cellIdx));
                if(StringUtils.isEmpty(field)) continue;
                //replace _name -> _id
                field = field.replaceAll("_name$", "_id").replace("_proposal_name", "_proposal_id").replace("_product_name", "_product_id");
                
                fieldMappings.put(field, value);
                //System.out.println(field+"="+value);
                
                fieldIndexMap.put(field, cellIdx);
            }
            
            // mapping required fields
            if(addedCustomizeFields.isEmpty()){
                for(IssueUtil.ALLOW_SEARCH_COL.REQUIRED key: IssueUtil.ALLOW_SEARCH_COL.REQUIRED.values()){
                    if( !fieldMappings.containsKey(key.name()) && params.containsKey(key.name())){
                        addedCustomizeFields.put(key.name().replaceAll("_name$", "_id").replace("_proposal_name", "_proposal_id").replace("_product_name", "_product_id"), params.get(key.name()));
                    }
                }
            }
            fieldMappings.putAll(addedCustomizeFields);

            boolean chkFlag;
            if(isLogin.getCompanyCustomerMode()) {
                System.err.println("<----- Company Customer Mode ----->");
            } else {
                // Check proposal
                if(isLogin.getCompanyBusinessFlag() != COMPANY_TYPE.STORE) {
                    isValid &= (chkFlag = fieldMappings.containsKey(COLS.PROPOSAL + "_1") && !StringUtils.isEmpty(fieldMappings.get(COLS.PROPOSAL + "_1")));
                    if( !chkFlag ) addErrorCell(r, errorLogs, errorCells, fieldIndexMap, COLS.PROPOSAL + "_1");
                }

                // check status
                isValid &= (chkFlag = fieldMappings.containsKey(COLS.STATUS) && !StringUtils.isEmpty(fieldMappings.get(COLS.STATUS)));
                if( !chkFlag ) addErrorCell(r, errorLogs, errorCells, fieldIndexMap, COLS.STATUS);

                // check public
                isValid &= (chkFlag = fieldMappings.containsKey(COLS.PUBLIC) && !StringUtils.isEmpty(fieldMappings.get(COLS.PUBLIC)));
                if( !chkFlag ) addErrorCell(r, errorLogs, errorCells, fieldIndexMap, COLS.PUBLIC);

                // check receive date
                if((chkFlag = fieldMappings.containsKey(IssueUtil.ALLOW_SEARCH_COL.ISSUE.issue_receive_date.name()))
                         && !StringUtils.isEmpty(fieldMappings.get(IssueUtil.ALLOW_SEARCH_COL.ISSUE.issue_receive_date.name()))){
                    String val = fieldMappings.get(IssueUtil.ALLOW_SEARCH_COL.ISSUE.issue_receive_date.name());
                    isValid &= (chkFlag = (DateUtil.parseDate(val) != null));
                }else{
                    isValid &= false;
                }
                if( !chkFlag ) addErrorCell(r, errorLogs, errorCells, fieldIndexMap, IssueUtil.ALLOW_SEARCH_COL.ISSUE.issue_receive_date.name());

                // check create date
                if((chkFlag = fieldMappings.containsKey(IssueUtil.ALLOW_SEARCH_COL.ISSUE.issue_created_time.name())
                        && !StringUtils.isEmpty(fieldMappings.get(IssueUtil.ALLOW_SEARCH_COL.ISSUE.issue_created_time.name())))){
                    String val = fieldMappings.get(IssueUtil.ALLOW_SEARCH_COL.ISSUE.issue_created_time.name());
                    isValid &= (chkFlag = (DateUtil.parseDate(val) != null));
                } else {
                    isValid &= false;
                }
                if( !chkFlag ) addErrorCell(r, errorLogs, errorCells, fieldIndexMap, IssueUtil.ALLOW_SEARCH_COL.ISSUE.issue_created_time.name());
            }
            
            try {
                if(isValid){
                    // sort mapping by key before parse data
                    Map<String, String> sortedResult = new LinkedHashMap<>();
                    fieldMappings.entrySet().stream()
                        .sorted(Map.Entry.<String, String>comparingByKey())
                        .forEachOrdered(x -> sortedResult.put(x.getKey(), x.getValue()));

                    if(isLogin.getCompanyCustomerMode()) {
                        System.err.println("<----- Company Customer Mode ----->");
                        Customer cust = new StandardMailParse().parseCustomer(sortedResult, mappingConds, projectService, params);

                        //check creator id
                        isValid &= (chkFlag = (cust.getCreatorId() != null));
                        if( !chkFlag ) addErrorCell(r, errorLogs, errorCells, fieldIndexMap, COLS.CUST_CREATOR_ID);

                        if(isValid){
                            if(cust.getCustId() != null){
                                successCount += (isValid &= issueCustomerService.updateCustomer(cust) != null) ? 1 : 0;
                            }else{
                                successCount += (isValid &= issueCustomerService.createCustomer(cust) != null) ? 1 : 0;
                            }
                        }

                    } else {
                        Issue issue = new StandardMailParse().parse(sortedResult, mappingConds, projectService, params);

                        //check creator id
                        isValid &= (chkFlag = (issue.getCreatorId() != null));
                        if( !chkFlag ) addErrorCell(r, errorLogs, errorCells, fieldIndexMap, COLS.CREATOR_ID);
                        //check receive persion id
                        isValid &= (chkFlag = (issue.getIssueReceivePerson() != null));
                        if( !chkFlag ) addErrorCell(r, errorLogs, errorCells, fieldIndexMap, COLS.PERSON_ID);

                        if(isValid){
                            if(issue.getIssueId() != null){
                                successCount += (isValid &= issueController.update(issue, false)) ? 1 : 0;
                            }else{
                                successCount += (isValid &= issueController.insert(issue, false)) ? 1 : 0;
                            }
                        }
                    }
                }
                
                if(isValid == false){ //error case
                    HSSFCellStyle rowStyle = wb.createCellStyle();
                    rowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                    rowStyle.setFillForegroundColor(HSSFColor.CORAL.index);
                    rowStyle.setBorderBottom(BorderStyle.THIN);
                    rowStyle.setBorderLeft(BorderStyle.THIN);
                    rowStyle.setBorderRight(BorderStyle.THIN);
                    rowStyle.setBorderTop(BorderStyle.THIN);
                    
                    if(errorCells.isEmpty()){
                        for(int cellIdx = 0; cellIdx < celNum; cellIdx++){
                            row.getCell(cellIdx).setCellStyle(rowStyle);
                        }
                    }else{
                        errorCells.forEach((cellIdx) -> {
                            row.getCell(cellIdx).setCellStyle(rowStyle);
                        });
                    }
                }else{ // success case
                    if("all".equals(reportContentType)){
                        HSSFCellStyle rowStyle = wb.createCellStyle();
                        rowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        rowStyle.setFillForegroundColor(wb.getCustomPalette().findSimilarColor(200, 230, 201).getIndex());
                        rowStyle.setBorderBottom(BorderStyle.THIN);
                        rowStyle.setBorderLeft(BorderStyle.THIN);
                        rowStyle.setBorderRight(BorderStyle.THIN);
                        rowStyle.setBorderTop(BorderStyle.THIN);
                        for(int cellIdx = 0; cellIdx < celNum; cellIdx++){
                            row.getCell(cellIdx).setCellStyle(rowStyle);
                        }
                    }else{
                        removeRows.add(row);
                    }
                }
                rawProgress = r;
                progress = rawProgress*100/rawProgressMax;
                if(progress > 100) progress = 100;
        
            } catch (Exception ex) {
                LOGGER.error(ex.getMessage(), ex);
            }
        }
        progress = 100;

        /** メンテがリストを追加 */
        List<MenteItem> allMenteItemList = this.menteService.setReloadCachedAllLevelList(isLogin.getCompanyId());
        SelectUtil.addSelectItems(this.issueController.getSelect(), allMenteItemList, isLogin.getLanguage());
    }
    
    private void addErrorCell(Integer row, Map<Integer, List<String>> errorLogs, List<Integer> errorCells, HashMap<String, Integer> fieldIndexMap, String key){
        Integer cellIdx = fieldIndexMap.get(key);
        if(cellIdx != null){
            errorCells.add(fieldIndexMap.get(key));
        }
        if(!errorLogs.containsKey(row)){
            errorLogs.put(row, new ArrayList<>());
        }
        String keyLabel = JsfUtil.getResource().message(isLogin.getCompanyId(), ResourceUtil.BUNDLE_ISSUE_NAME, "label." + key.replaceAll("_id", "_name"));
        
        String errorMsg = JsfUtil.getResource().message(isLogin.getCompanyId(), ResourceUtil.BUNDLE_ISSUE_NAME, "label.import.data.error", keyLabel);
        errorLogs.get(row).add("<span class=\"text-danger\">"+errorMsg+"</span>");
    }
    
    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void onImportComplete(){
        projectController.loadProjectList();
        
        HSSFSheet sheet = issueImportController.getSheet();
        
        //remove row if reporType = minified
        removeRows.forEach((row) -> {
            try{
                sheet.removeRow(row);
            }catch(Exception e){}
        });
        
        if(successCount < rawProgressMax){
            String msg = successCount+"/"+rawProgressMax+" 追加しました。";
            System.out.println("Import result: " + msg );
            JsfUtil.addErrorMessage(msg);
        }else{
            System.out.println("All issue Imported!");
            JsfUtil.addSuccessMessage("全て受付が追加しました。");
        }
        if(!errorLogs.isEmpty()){
            String dialogTitle = JsfUtil.getResource().message(isLogin.getCompanyId(), ResourceUtil.BUNDLE_ISSUE_NAME, "label.import.error.title");
            String errorTitle = JsfUtil.getResource().message(isLogin.getCompanyId(), ResourceUtil.BUNDLE_ISSUE_NAME, "label.import.error.table.row");
            String errorContent = JsfUtil.getResource().message(isLogin.getCompanyId(), ResourceUtil.BUNDLE_ISSUE_NAME, "label.import.error.table.content");
            String backButton = JsfUtil.getResource().message(isLogin.getCompanyId(), ResourceUtil.BUNDLE_MSG, "label.back");

            StringBuilder errorResultHtml = new StringBuilder();
            errorResultHtml.append("<div class=\"modal fade\" tabindex=\"-1\" role=\"dialog\"><div class=\"modal-dialog modal-lg\" role=\"document\"><div class=\"modal-content\"><div class=\"modal-header\"><h4 class=\"modal-title\">");
            errorResultHtml.append(dialogTitle);
            errorResultHtml.append("</h4></div><div class=\"modal-body\">");
            errorResultHtml.append("<table class=\"table table-bordered\" style=\"display:block\"><thead><tr><th width=\"60\">").append(errorTitle).append("</th><th>").append(errorContent).append("</th></th></thead><tbody style=\"display:block; overflow:auto; max-height: 400px\">");
            errorLogs.forEach((row, contents) -> {
                errorResultHtml.append("<tr>");
                errorResultHtml.append("<td>").append(row).append("</td>");
                errorResultHtml.append("<td>").append(StringUtils.join(contents, "<br/>")).append("</td>");
                errorResultHtml.append("</tr>");
            });        
            errorResultHtml.append("</tbody></table>");
            errorResultHtml.append("</div><div class=\"modal-footer\"><button type=\"button\" class=\"btn btn-sm btn-default\" data-dismiss=\"modal\">").append(backButton).append("</button></div></div></div></div>");
            JsfUtil.executeClientScript("$('"+errorResultHtml+"').modal({backdrop: 'static', keyboard: false});");
        }
        if( successCount < rawProgressMax ){ //Neu co row nao loi, thi moi export file ket qua
            JsfUtil.executeClientScript("$('#importResult').click();");
        }
    }
}
