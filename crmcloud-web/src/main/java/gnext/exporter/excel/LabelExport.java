/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.exporter.excel;

import gnext.bean.label.PropertyItemLabel;
import gnext.exporter.Export;
import gnext.util.DateUtil;
import gnext.util.JsfUtil;
import gnext.util.ResourceUtil;
import gnext.util.WebFileUtil;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;

/**
 *
 * @author daind
 */
public class LabelExport implements Export {
    private final Map<String, String> resourceList;
    private final Map<String, String> moduleList;
    private final List<PropertyItemLabel> list;
    private final int companyId;
    private final String exportFileName;
    
    public LabelExport(Map<String, String> resourceList, Map<String, String> moduleList, List<PropertyItemLabel> list,
            int companyId, String exportFileName) {
        this.resourceList = resourceList;
        this.moduleList = moduleList;
        this.list = list;
        this.companyId = companyId;
        this.exportFileName = exportFileName;
    }
    
    @Override
    public void execute() throws Exception {
        HSSFWorkbook wb = new HSSFWorkbook();
        Map<String, HSSFSheet> sheets = new HashMap<>();
        
        HSSFFont headerFont = wb.createFont();
        headerFont.setFontHeightInPoints((short)12);
        headerFont.setBold(true);
        
        HSSFCellStyle headerStyle = wb.createCellStyle();
        headerStyle.setFont(headerFont);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        
        Map<String, Integer>  startRows = new HashMap<>();
        for(PropertyItemLabel item : this.list){
            HSSFSheet sheet;
            if(sheets.containsKey(item.getLabelLanguage())){
                sheet = sheets.get(item.getLabelLanguage());
            }else{
                sheet = wb.createSheet(JsfUtil.getResource().message(ResourceUtil.BUNDLE_MSG, "language_" + item.getLabelLanguage()));
                sheets.put(item.getLabelLanguage(), sheet);
                
                HSSFRow headerRow = sheet.createRow(0);
                headerRow.createCell(0).setCellValue("");
                headerRow.createCell(1).setCellValue("");
                headerRow.createCell(2).setCellValue("");

                headerRow.createCell(3).setCellValue(JsfUtil.getResource().message(companyId, ResourceUtil.BUNDLE_LABEL, "label.manager.table.module"));
                headerRow.createCell(4).setCellValue(JsfUtil.getResource().message(companyId, ResourceUtil.BUNDLE_LABEL, "label.manager.table.text"));
                headerRow.createCell(5).setCellValue(JsfUtil.getResource().message(companyId, ResourceUtil.BUNDLE_LABEL, "label.manager.table.language"));
                headerRow.createCell(6).setCellValue(JsfUtil.getResource().message(companyId, ResourceUtil.BUNDLE_LABEL, "label.manager.table.new_text"));
                
                headerRow.setRowStyle(headerStyle);
            }
            Integer startRow = 1;
            if(startRows.containsKey(item.getLabelLanguage())){
                startRow = startRows.get(item.getLabelLanguage());
            }
            HSSFRow row = sheet.createRow(startRow++);
            
            row.createCell(0).setCellValue(item.getModule());
            row.createCell(1).setCellValue(item.getPk().getItemCode());
            row.createCell(2).setCellValue(item.getLabelLanguage());
            
            row.createCell(3).setCellValue(moduleList.get(item.getModule()));
            row.createCell(4).setCellValue(resourceList.get(item.getPk().getItemCode()));
            row.createCell(5).setCellValue(JsfUtil.getResource().message(ResourceUtil.BUNDLE_MSG, "language_" + item.getLabelLanguage()));
            row.createCell(6).setCellValue(item.getLabelName());
            
            startRows.put(item.getLabelLanguage(), startRow);
        }
        sheets.forEach((langCode, sheet) -> {
            sheet.setColumnHidden(0, true); //hide module column
            sheet.setColumnHidden(1, true); //hide code column
            sheet.setColumnHidden(2, true); //hide language code column
            
            sheet.setColumnWidth(4, 17500);
            sheet.setColumnWidth(6, 17500);
        });
        
        WebFileUtil.forceDownload(String.format("%s_%s.xls", exportFileName, new SimpleDateFormat(DateUtil.PATTERN_CSV_EXPORT_DATE).format(new Date())), wb);
    }
}
