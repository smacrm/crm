package gnext.controller.report;

import gnext.bean.report.NestProduct;
import gnext.bean.report.ReportItem;
import gnext.controller.common.LayoutController;
import gnext.controller.common.LocaleController;
import gnext.model.authority.UserModel;
import gnext.model.report.BarChartData;
import gnext.model.report.LineChartData;
import gnext.model.report.PieChartData;
import gnext.model.report.export.DataMapping;
import gnext.model.report.export.Month;
import gnext.model.report.export.ReportRow;
import gnext.security.annotation.SecureMethod;
import gnext.security.annotation.SecurePage;
import gnext.service.report.ReportService;
import gnext.util.JsfUtil;
import gnext.util.ResourceUtil;
import java.awt.Color;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.BorderStyle;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.primefaces.component.tabview.TabView;
import org.primefaces.event.DateViewChangeEvent;
import org.primefaces.event.TabChangeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hungpham
 * @since Nov 28, 2016
 */
@ManagedBean(name = "reportController")
@ViewScoped
@SecurePage(module = SecurePage.Module.REPORT)
public class ReportController implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private static final Logger logger = LoggerFactory.getLogger(ReportController.class);

    @ManagedProperty(value = "#{layout}")
    @Getter @Setter private LayoutController layout;
    
    @ManagedProperty(value = "#{localeController}")
    @Getter @Setter private LocaleController locale;
    
    @EJB private ReportService reportServiceImpl;
  
    @Getter @Setter private transient TabView tabView;
    @Setter @Getter private int reportType;
    @Setter @Getter private Date fromDate1;
    @Setter @Getter private Date fromDate2;
    
    @Getter private final Map<Integer, String> labelMapping = new HashMap<>();
    @Getter private final LineChartData lineChartData = new LineChartData();
    @Getter private final PieChartData pieChartData = new PieChartData();
    @Getter private final BarChartData barChartData = new BarChartData();
    @Getter private final DataMapping dataMapping = new DataMapping();
    
    @Getter private Boolean displayReportFlag = Boolean.FALSE;
    
    @Getter List<ReportItem> reportData = new ArrayList<>();
    
    @Getter private final List<String> headerColumns = new ArrayList<>();
    @Getter private final List<Integer> monthColumns = new ArrayList<>();
    
    private List<NestProduct> nestProductList = new ArrayList<>();
    @Getter private int maxHeaderColumn = 0;
    
    @Getter ReportRow report;
    
    private String selectedTitle;
    private String exportedTitle;
    
    //report property
    private final Short REPORT_HEADER_FONT_SIZE = 12;
    private final Short REPORT_FONT_SIZE = 10;
    private final Color REPORT_HEADER_BACKGROUND = new Color(0x4FC3F7);
    private final Color REPORT_SUMMARY_BACKGROUND = new Color(0xFFF59D);
    private final Color REPORT_FOOTER_BACKGROUND = new Color(0x4FC3F7);
    
    private final String DECIMAL_PATTERN = "([0-9]*)\\.([0-9]*)"; 
    
    private final Map<String, String> imageChart = new HashMap<>();
    
    @PostConstruct
    public void init(){
        reportType = 1;
        
        
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.set(Calendar.DATE, 1);
        
        fromDate1 = c.getTime();
        fromDate2 = c.getTime();
    }

    public String getSelectedTitle() {
        if(selectedTitle == null){
            String tabTitle = ((org.primefaces.component.tabview.Tab)tabView.getChildren().get(0)).getTitle();
            Calendar c = Calendar.getInstance();
            c.setTime(fromDate1);
            selectedTitle = String.format("%s %d", tabTitle, c.get(Calendar.YEAR));
        }
        return selectedTitle;
    }
    
    @SecureMethod(value=SecureMethod.Method.INDEX, require = false)
    public void load(){
        init();
        layout.setCenter("/modules/report/report.xhtml");
    }
    
    @SecureMethod(value=SecureMethod.Method.SEARCH)
    public void search(Boolean displayReportFlag){
        labelMapping.clear();
        lineChartData.clear();
        pieChartData.clear();
        barChartData.clear();
        dataMapping.clear();
        
        int monthStart = seedDataReport();
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DATE, 1);
        c.set(Calendar.MONTH, monthStart);
        c.add(Calendar.MONTH, -1);
        
        int prevMonth = c.get(Calendar.MONTH);
        
        lineChartData.setStartMonth(monthStart);
        barChartData.setStartMonth(monthStart);
        dataMapping.setStartMonth(monthStart);
        
        reportData.forEach((item) -> {
            int month = item.getKey().getMonth();
            String itemId = item.getKey().getId();
            
            itemId = itemId.substring(itemId.lastIndexOf("-")+1);
            //line chart
            if( !lineChartData.containsKey(itemId) ) lineChartData.put(itemId, new HashMap<>());
            Map<Integer, Integer> pData = lineChartData.get(itemId);
            pData.put(month, item.getCurrent() + (pData.containsKey(month) ? pData.get(month) : 0));
            lineChartData.put(itemId, pData);
            
            //pie chart
            if(month == prevMonth+1){ //Chi hien thi ket qua cua thang truoc
                if( !pieChartData.containsKey(itemId) ) pieChartData.put(itemId, 0);
                pieChartData.put(itemId, pieChartData.get(itemId) + item.getCurrent());
            }
            
            //bar chart
            if( !barChartData.containsKey(month) ) barChartData.put(month, new AbstractMap.SimpleEntry<>(0, 0));
            AbstractMap.SimpleEntry<Integer, Integer> entry = barChartData.get(month);
            barChartData.put(month, new AbstractMap.SimpleEntry<>(entry.getKey() + item.getCurrent(), entry.getValue() + item.getLast()));
            
            //datatable
            dataMapping.push(item);
            
        });
        
        lineChartData.setLabelMapping(labelMapping);
        pieChartData.setLabelMapping(labelMapping);
        
        if(displayReportFlag != null){
            //for display report
            this.displayReportFlag = displayReportFlag;
        }
    }
    
    private Integer seedDataReport(){
        Calendar c = Calendar.getInstance();
        Date date = new Date();
        String strFDate, strTDate;
        SimpleDateFormat sdf =  new SimpleDateFormat("yyyy-MM-dd");
        if(getReportType() == 1){
            date = getFromDate1();
        }else if(getReportType() == 2){
            date = getFromDate2();
        }
        //Set lai time tim kiem
        //VD, chon thang 12/2017
        // -> Tim kiem tu thang 12/2016 -> 12/2018
        c.setTime(date);
        c.set(Calendar.DATE, 1);
        
        int expectedYear = c.get(Calendar.YEAR);
        int expectedMonth = c.get(Calendar.MONTH);
        
        c.add(Calendar.YEAR, -1);
        strFDate = sdf.format(c.getTime());
        
        c.add(Calendar.YEAR, 2);
        strTDate = sdf.format(c.getTime());
        
        monthColumns.clear();
        for(int i = 0; i< 12; i++){
            monthColumns.add((c.get(Calendar.MONTH)+1));
            c.add(Calendar.MONTH, 1);
        }
        
        // lay nhung item co data cho report
        reportData = reportServiceImpl.getReportData(getReportType(), strFDate, strTDate, expectedYear, expectedMonth, locale.getLocale(), UserModel.getLogined().getCompanyId());
        
        // lay header mapping
        reportServiceImpl.getFullLabelMapping(getReportType(), locale.getLocale(), UserModel.getLogined().getCompanyId())
        .forEach((item) -> {
            labelMapping.put(item.getMenteOptionDataValuePK().getItemId(), item.getItemData());
        });
        labelMapping.put(0, getLabel("label.other"));
        
        maxHeaderColumn = 0;
        
        return c.get(Calendar.MONTH);
    }
    
    @SecureMethod(value=SecureMethod.Method.DOWNLOAD)
    public void export(){
        exportedTitle = getSelectedTitle();
        monthColumns.clear();
        
        this.search(null);
        
        // danh sach cac row cho bao cao (khong co data)
        nestProductList = reportServiceImpl.getNestProduct(getReportType(), locale.getLocale(), UserModel.getLogined().getCompanyId());
        
        // revert duplicate label on data table
        List<String> pushedHeaders = new ArrayList<>();
        
        report = new ReportRow(getReportType());
        for(int i = 0; i < nestProductList.size(); i++){
            NestProduct np = nestProductList.get(i);
            
            String itemId = np.getId();
            maxHeaderColumn = StringUtils.split(itemId, "-").length;
            
            Map<Integer, String> headers = new HashMap<>();

            List<String> arrPath = Arrays.asList(StringUtils.split(itemId, "-"));
            int level = 1;
            List<String> tmpPath = new ArrayList<>();
            for(String strPath : arrPath){
                String label  = "";
                Integer idPath = Integer.parseInt(strPath);
                
                tmpPath.add(strPath);
                if(!pushedHeaders.contains(StringUtils.join(tmpPath))){
                    pushedHeaders.add(StringUtils.join(tmpPath));
                    label = StringUtils.defaultIfEmpty(labelMapping.get(idPath), getLabel("label.other"));
                }
                
                headers.put(level++, label);
            }
            ReportRow r = new ReportRow(itemId, getReportType());
            r.setHeaders(headers);
            Map<Integer, Month> rowData = this.dataMapping.get(itemId);
            if(rowData != null) r.setData(rowData);
            report.push(r);
        }
    }
    
    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void onSPTabChange(TabChangeEvent event){   
        reportType = tabView.getChildren().indexOf(event.getTab())+1;
        Calendar c = Calendar.getInstance();
        c.setTime(fromDate1);
        selectedTitle = String.format("%s %d", event.getTab().getTitle(), c.get(Calendar.YEAR));
    }
    
    public void calendarViewChange(DateViewChangeEvent e) { 
        Calendar c = Calendar.getInstance();
        c.set(e.getYear(), e.getMonth()-1, 1);
        if(reportType == 1){
            this.setFromDate1(c.getTime());
        }else if(reportType == 2){
            this.setFromDate2(c.getTime());
        }
    }
    
    public void saveChartImage(){
        imageChart.put("line", FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("line"));
        imageChart.put("pie", FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("pie"));
        imageChart.put("bar", FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("bar"));
    }

    public String getExportedTitle() throws UnsupportedEncodingException {
        return URLEncoder.encode(StringUtils.defaultIfEmpty(this.exportedTitle, "").replace(" ", "_"), "UTF-8");
    }
    
    public void postProcessXLS(Object document) {
        XSSFWorkbook wb = (XSSFWorkbook) document;
        XSSFSheet sheet = wb.getSheetAt(0);
        wb.setSheetName(0, selectedTitle);
        sheet.shiftRows(5, sheet.getLastRowNum(), -1); //rowNum() + 1
        
        XSSFFont headerFont = wb.createFont();
        headerFont.setFontHeightInPoints(REPORT_HEADER_FONT_SIZE);
        headerFont.setBold(true);
        
        XSSFCellStyle headerStyle = wb.createCellStyle();
        headerStyle.setFont(headerFont);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        
        Row headerRow = sheet.getRow(1);
        headerRow.setHeightInPoints(34);
        headerRow.getCell(0).setCellStyle(headerStyle);
        
        for (Row row : sheet) {
            if(row.getRowNum() >= 2){
                row.setHeightInPoints(17);
                boolean isTotalRow = false;
                for (Cell cell : row) {
                    if(row.getRowNum() <=3 ){
                        applyCellHeaderStyle(cell, wb);
                    }else{
                        if(row.getRowNum() != sheet.getLastRowNum()){
                            if(cell.getStringCellValue().equals(getLabel("label.total"))){
                                isTotalRow = true;
                                int spanToColIndex = findNextNotBlankAndEqualCell(row, cell.getAddress().getColumn());
                                if (spanToColIndex > cell.getAddress().getColumn()
                                        && row.getRowNum() < sheet.getLastRowNum()) {
                                    sheet.addMergedRegion(new CellRangeAddress(cell.getAddress().getRow(), cell.getAddress().getRow(), cell.getAddress().getColumn(), spanToColIndex));
                                }
                            }
                            if(isTotalRow){
                                applyCellTotalStyle(cell, wb);
                            }else{
                                applyCellNormalStyle(cell, wb);
                            }
                        }else{
                            applyCellFooterStyle(cell, wb);
                        }
                    }
                }
            }
        }
        for(int i = 0; i<= sheet.getRow(3).getLastCellNum(); i++){
            sheet.autoSizeColumn(i);
        }
        
        //write image
        XSSFSheet sheetChart = wb.createSheet(getLabel("label.chart"));
        sheetChart.setDisplayGridlines(false);
        
        XSSFRow r;
        XSSFCell c;
        XSSFFont font = wb.createFont();
        font.setColor(IndexedColors.BLACK.getIndex());
        font.setFontHeightInPoints(REPORT_HEADER_FONT_SIZE);
        font.setBold(true);
        
        XSSFCellStyle style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFont(font);
        
        
        addImageToSheet("line", 2, 1, sheetChart, wb);
        r = sheetChart.createRow(1);
        c = r.createCell(1);
        c.setCellValue(getLabel("label.calc.current.month"));
        c.setCellStyle(style);
        
        r = sheetChart.createRow(19);
        boolean hasPieChart = addImageToSheet("pie", 20, 1, sheetChart, wb);
        if(hasPieChart){
            c = r.createCell(1);
            c.setCellValue(getLabel("label.calc.current.month2"));
            c.setCellStyle(style);
        }
        
        addImageToSheet("bar", 20, hasPieChart ? 14 : 1, sheetChart, wb);
        c = r.createCell(hasPieChart ? 14 : 1);
        c.setCellValue(getLabel("label.calc.current.year"));
        c.setCellStyle(style);
        
    }
    
    private int findNextNotBlankAndEqualCell(Row row, int start){
        String value = row.getCell(start).getStringCellValue();
        for (int i = start+1; i < row.getPhysicalNumberOfCells(); i++){
            if( row.getCell(i) != null ) {
                String nextValue = row.getCell(i).getStringCellValue();
                if( !StringUtils.isEmpty(nextValue) && !value.equals(nextValue) ){
                    return i-1;
                }
            }
        }
        return start;
    }
    
    private boolean addImageToSheet(String type, int row, int col, XSSFSheet sheet, XSSFWorkbook wb){
        byte[] bytes = getImage(type);
        if(bytes != null){
            int pictureureIdx = wb.addPicture(bytes, XSSFWorkbook.PICTURE_TYPE_JPEG);

            CreationHelper helper = wb.getCreationHelper();

            Drawing drawing = sheet.createDrawingPatriarch();

            ClientAnchor anchor = helper.createClientAnchor();

            anchor.setCol1(col);
            anchor.setRow1(row);

            Picture pict = drawing.createPicture(anchor, pictureureIdx);
            pict.resize();
            return true;
        }
        return false;
    }
    
    private void applyBorderStyle(XSSFCellStyle style){
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
    }
    
    private byte[] getImage(String type){
        try{
            if(imageChart.containsKey(type)){
                String base64Image = imageChart.get(type).split(",")[1];
                return javax.xml.bind.DatatypeConverter.parseBase64Binary(base64Image);
            }
        }catch(ArrayIndexOutOfBoundsException e){}
        return null;
    }
    
    private boolean isValuableCell(Cell cell){
        String value = cell.getStringCellValue().trim();
        return (org.apache.commons.lang3.StringUtils.isNumeric(value) || "-".equals(value) || Pattern.matches(DECIMAL_PATTERN, value));
    }
    
    private void applyCellHeaderStyle(Cell cell, XSSFWorkbook wb){
        XSSFCellStyle style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setFillForegroundColor(new XSSFColor(REPORT_HEADER_BACKGROUND));
        XSSFFont font = wb.createFont();
        font.setColor(IndexedColors.BLACK.getIndex());
        font.setFontHeightInPoints(REPORT_FONT_SIZE);
        font.setBold(true);
        style.setFont(font);
        applyBorderStyle(style);
        
        cell.setCellStyle(style);
    }
    
    private void applyCellFooterStyle(Cell cell, XSSFWorkbook wb){
        XSSFCellStyle style = wb.createCellStyle();
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setAlignment(isValuableCell(cell) ? HorizontalAlignment.RIGHT: HorizontalAlignment.LEFT);
        style.setFillForegroundColor(new XSSFColor(REPORT_FOOTER_BACKGROUND));
        XSSFFont font = wb.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.BLACK.getIndex());
        font.setFontHeightInPoints(REPORT_FONT_SIZE);
        style.setFont(font);
        applyBorderStyle(style);
        
        cell.setCellStyle(style);
    }
    
    private void applyCellTotalStyle(Cell cell, XSSFWorkbook wb){
        XSSFCellStyle style = wb.createCellStyle();
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setAlignment(isValuableCell(cell) ? HorizontalAlignment.RIGHT: HorizontalAlignment.LEFT);
        style.setFillForegroundColor(new XSSFColor(REPORT_SUMMARY_BACKGROUND));
        XSSFFont font = wb.createFont();
        font.setColor(IndexedColors.BLACK.getIndex());
        font.setFontHeightInPoints(REPORT_FONT_SIZE);
        style.setFont(font);
        applyBorderStyle(style);
        
        cell.setCellStyle(style);
    }
    
    private void applyCellNormalStyle(Cell cell, XSSFWorkbook wb){
        XSSFCellStyle style = wb.createCellStyle();
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setAlignment(isValuableCell(cell) ? HorizontalAlignment.RIGHT: HorizontalAlignment.LEFT);
        XSSFFont font = wb.createFont();
        font.setColor(IndexedColors.BLACK.getIndex());
        font.setFontHeightInPoints(REPORT_FONT_SIZE);
        style.setFont(font);
        applyBorderStyle(style);
        
        cell.setCellStyle(style);
    }

    public String getHeaderText(int index){
        String text = String.format(getLabel("label.level") + " %d",index);
        switch(index){
            case 1:
                text = getLabel("label.product.large");
                break;
            case 2:
                text = getLabel("label.product.middle");
                break;
            case 3:
                text = getLabel("label.product.small");
                break;
        }
        return text;
    }
    
    public String getLabel(String key){
        return JsfUtil.getResource().message(UserModel.getLogined().getCompanyId(), ResourceUtil.BUNDLE_REPORT_NAME, key);
    }
}