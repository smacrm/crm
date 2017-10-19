package gnext.controller.issue;

import gnext.bean.project.DynamicColumn;
import gnext.controller.common.LayoutController;
import gnext.bean.mente.MenteItem;
import gnext.controller.common.LocaleController;
import gnext.model.authority.UserModel;
import gnext.security.annotation.SecureMethod;
import gnext.security.annotation.SecurePage;
import gnext.util.CSVToExcel;
import gnext.util.DateUtil;
import gnext.util.ExcelToCSV;
import gnext.util.JsfUtil;
import gnext.util.ResourceUtil;
import gnext.util.WebFileUtil;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.model.SelectItem;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.primefaces.event.CloseEvent;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hungpham
 * @since Apr 27, 2017
 */
@ManagedBean(name = "issueImportController")
@SessionScoped
@SecurePage(module = SecurePage.Module.ISSUE, require = false)
public class IssueImportController implements Serializable {

    private static final long serialVersionUID = -2233731569078571172L;
    private final Logger LOGGER = LoggerFactory.getLogger(IssueImportController.class);

    @ManagedProperty(value = "#{layout}")
    @Getter @Setter private LayoutController layout;
    
    @ManagedProperty(value = "#{localeController}")
    @Getter @Setter private LocaleController locale;
    
    @ManagedProperty(value = "#{projectController}")
    @Getter @Setter private ProjectController projectController;
    
    @ManagedProperty(value = "#{issueController}")
    @Getter @Setter private IssueController issueController;
 
    @Setter @Getter private UploadedFile uploadedFile;
    @Getter private HSSFSheet sheet;
    @Getter private HSSFWorkbook wb;
    @Getter @Setter private Map<Integer, String> uploadHeaders = new HashMap<>();
    @Getter @Setter private List<DynamicColumn> availableColumns = new ArrayList<>();
    @Getter @Setter private String strMappingField;
    private Map<String, String> mappingFields = new HashMap<>();
    
    @Setter
    private String reportFileType;
    
    @Getter
    private Integer maxRow;
    
    public void reload(){
        availableColumns = projectController.getListAllVisibleColumn();
    }
    
    public void fileUploadListener(FileUploadEvent event) {
        this.uploadedFile = event.getFile();
    }
    
    /**
     * Import data for product only
     * @throws IOException 
     */
    @SecureMethod(value = SecureMethod.Method.UPLOAD, require = false)
    public void importData() throws IOException{
        if(this.uploadedFile == null){
            //upload failed
            JsfUtil.addErrorMessage(JsfUtil.getResource().message(UserModel.getLogined().getCompanyId(), ResourceUtil.BUNDLE_MSG, "msg.system.error"));
            return;
        }
        try{
            issueController.loadContextData();
            uploadHeaders.clear();
            String fileName = this.uploadedFile.getFileName().toLowerCase();
            if(fileName.endsWith(".csv")){
                wb = CSVToExcel.convert(this.uploadedFile.getInputstream());
            }else{
                wb = new HSSFWorkbook(this.uploadedFile.getInputstream());
            }
            sheet = wb.getSheetAt(0);
            maxRow = sheet.getLastRowNum();
            for(int r = 0; r <= maxRow; r++){
                HSSFRow row = sheet.getRow(r);
                Short celNum = row.getLastCellNum();
                for(int c = 0; celNum != null && c< celNum; c++){
                    HSSFCell cell = row.getCell(c);
                    if(r == 0) uploadHeaders.put(c, cell.getStringCellValue()); //add header to variables
                }
            }
            
            //reload form
            reload();
            layout.setCenter("/modules/issue/import/import.xhtml");
        }catch(Exception e){
            JsfUtil.addErrorMessage(JsfUtil.getResource().message(UserModel.getLogined().getCompanyId(), ResourceUtil.BUNDLE_MSG, "msg.system.error"));
        }
    }
    
    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void downloadResult(){
        // force download result file
        SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.PATTERN_CSV_EXPORT_DATE);
        String resultFileName = uploadedFile.getFileName();
        resultFileName = resultFileName.substring(0, resultFileName.lastIndexOf(".") + 1);
        try{
            switch(this.reportFileType){
                case "xls":
                    WebFileUtil.forceDownload(String.format("%s_RESULT_%s." + this.reportFileType, resultFileName, sdf.format(new Date())), wb);
                    break;
                case "csv":
                    WebFileUtil.forceDownload(String.format("%s_RESULT_%s." + this.reportFileType, resultFileName, sdf.format(new Date())), new ExcelToCSV().convertExcelToCSV(wb));
                    break;
            }
        }catch(IOException ioe){
            LOGGER.error(ioe.getMessage(), ioe);
        }
        
    }
    
    public boolean isSelectHeader(String entryId){
        return NumberUtils.isNumber(entryId);
    }
    
    public void handleCloseDialog(CloseEvent event) {
        uploadedFile = null;
    }
    
    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public List<SelectItem>  filterLevel(int level, List<SelectItem> items){
        List<SelectItem> filteredItems = new ArrayList<>();
        items.forEach((item) -> {
            if( item.getValue() instanceof MenteItem ){
                MenteItem o = (MenteItem) item.getValue();
                if(o.getItemLevel() == level){
                    filteredItems.add(item);
                }
            }
        });
        return filteredItems;
        
    }
    
    public List<SelectItem> filterItemByParent(List<MenteItem> parents){
        List<SelectItem> filteredItems = new ArrayList<>();
        parents.forEach((item) -> {
            item.getItemChilds().forEach((child) -> {
                String code = child.getItemName();
                String label = child.getItemViewData(locale.getLocale());
                filteredItems.add(new SelectItem(child, label));
            });
        });
        return filteredItems;
    }
}