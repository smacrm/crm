/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.exporter.excel;

import gnext.bean.mente.MenteItem;
import gnext.exporter.Export;
import gnext.service.mente.MenteService;
import gnext.util.DateUtil;
import gnext.util.JsfUtil;
import gnext.util.ResourceUtil;
import gnext.util.WebFileUtil;
import gnext.utils.InterfaceUtil;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;

/**
 *
 * @author hungpham
 */
public class CategoryExport implements Export {
    private final String locale;
    private final int companyId;
    private final MenteService menteServiceImpl;
    
    public CategoryExport(String locale, int companyId, MenteService menteServiceImpl) {
        this.locale = locale;
        this.companyId = companyId;
        this.menteServiceImpl = menteServiceImpl;
    }
    
    @Override
    public void execute() throws Exception {
        exportCategory();
    }
    
    /**
     * Export Product Category
     * 
     * @throws IOException 
     */
    private void exportCategory() throws Exception {
        if(StringUtils.isBlank(locale)) return;
        
        Locale newLocale = new Locale(locale, "");
        
        List<List<String>> data = new ArrayList<>();
        data.add(Arrays.asList(
                JsfUtil.getResource().message(this.companyId, ResourceUtil.BUNDLE_ISSUE_NAME, newLocale, "label.issue_product_name_1")
                , "", "", "", ""
                ,JsfUtil.getResource().message(this.companyId, ResourceUtil.BUNDLE_ISSUE_NAME, newLocale, "label.issue_product_name_2")
                , "", "", "", ""
                ,JsfUtil.getResource().message(this.companyId, ResourceUtil.BUNDLE_ISSUE_NAME, newLocale, "label.issue_product_name_3")
                , "", "", "", ""));
        
        String id = JsfUtil.getResource().message(this.companyId, ResourceUtil.BUNDLE_MSG, newLocale, "label.id");
        String name = JsfUtil.getResource().message(this.companyId, ResourceUtil.BUNDLE_MSG, newLocale, "label.name");
        String edit = JsfUtil.getResource().message(this.companyId, ResourceUtil.BUNDLE_MSG, newLocale, "label.edit");
        String show = JsfUtil.getResource().message(this.companyId, ResourceUtil.BUNDLE_MSG, newLocale, "label.show");
        String search = JsfUtil.getResource().message(this.companyId, ResourceUtil.BUNDLE_MSG, newLocale, "label.search");
        data.add(Arrays.asList(id, name, edit, show, search, id, name, edit, show, search, id, name, edit, show, search)); //header column
        
        int[] rowIndex = {data.size()};
        data = getProcessedData(rowIndex, menteServiceImpl.getAllDynamicLevel(InterfaceUtil.COLS.PRODUCT, companyId), data, locale);
        
        //fill blank cell
        for(int i=0; i<data.size(); i++){
            if(data.get(i).size() < 15){
                int missColNum = data.get(1).size() - data.get(i).size();
                for(int j = 0; j < missColNum; j++){
                    data.get(i).add("");
                }
            }
        }
        String sheetName = JsfUtil.getResource().message(this.companyId, ResourceUtil.BUNDLE_ISSUE_NAME, newLocale, "label.issue_product_id");
        //create excel
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet(sheetName);
        
        HSSFFont hfont = wb.createFont();
        hfont.setBold(true);
        hfont.setFontHeightInPoints((short)10);
        
        //normal row
        HSSFCellStyle style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        
        //row header 1
        HSSFCellStyle hStyle1 = wb.createCellStyle();
        hStyle1.setAlignment(HorizontalAlignment.CENTER);
        hStyle1.setVerticalAlignment(VerticalAlignment.CENTER);
        hStyle1.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        hStyle1.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
        hStyle1.setFont(hfont);
        
        hStyle1.setBorderBottom(BorderStyle.THIN);
        hStyle1.setBorderLeft(BorderStyle.THIN);
        hStyle1.setBorderRight(BorderStyle.THIN);
        hStyle1.setBorderTop(BorderStyle.THIN);
        
        //row header 2
        HSSFCellStyle hStyle2 = wb.createCellStyle();
        hStyle2.setAlignment(HorizontalAlignment.LEFT);
        hStyle2.setVerticalAlignment(VerticalAlignment.CENTER);
        hStyle2.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        hStyle2.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
        hStyle2.setFont(hfont);
        
        hStyle2.setBorderBottom(BorderStyle.THIN);
        hStyle2.setBorderLeft(BorderStyle.THIN);
        hStyle2.setBorderRight(BorderStyle.THIN);
        hStyle2.setBorderTop(BorderStyle.THIN);

        final int[] rownum = {0};
        data.forEach((objArr) -> {
            final Row row = sheet.createRow(rownum[0]++);
            final int[] cellnum = {0};
            objArr.forEach((value) -> {
                Cell cell = row.createCell(cellnum[0]++);
                cell.setCellValue(value);
                switch(rownum[0]){
                    case 1:
                        cell.setCellStyle(hStyle1);
                        break;
                    case 2:
                        cell.setCellStyle(hStyle2);
                        break;
                    default:
                        cell.setCellStyle(style);
                }
            });
        });
        
        //merge row header 1
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 5, 8));
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 10, 13));
        
        // Auto size the column widths
        sheet.autoSizeColumn(1);
        sheet.autoSizeColumn(6);
        sheet.autoSizeColumn(11);
  
        // カラム隠す
        sheet.setColumnHidden(2, true);
        sheet.setColumnHidden(3, true);
        sheet.setColumnHidden(4, true);
        sheet.setColumnHidden(7, true);
        sheet.setColumnHidden(8, true);
        sheet.setColumnHidden(9, true);
        sheet.setColumnHidden(12, true);
        sheet.setColumnHidden(13, true);
        sheet.setColumnHidden(14, true);
        
        SimpleDateFormat sdf2 = new SimpleDateFormat(DateUtil.PATTERN_CSV_EXPORT_DATE);
        WebFileUtil.forceDownload(String.format("%s_%s.xls", sheetName, sdf2.format(new Date())), wb);
    }
    
    private List<List<String>> getProcessedData(int[] rowIndex, List<MenteItem> data, List<List<String>> listOutput, String locale){
        data.forEach((item) -> {
            if(item.getItemDeleted() == Boolean.FALSE){
                if(listOutput.size() <= rowIndex[0]){
                    listOutput.add(new ArrayList<>());
                    for(int i = 0; i < (item.getItemLevel()-1) * 5; i++){
                        listOutput.get(rowIndex[0]).add("");
                    }
                }

                listOutput.get(rowIndex[0]).add(item.getItemId().toString());
                listOutput.get(rowIndex[0]).add(item.getItemViewData(locale));
                listOutput.get(rowIndex[0]).add(item.getItemEditAddFlag() ? "1" : "0");
                listOutput.get(rowIndex[0]).add(item.getItemShowFlag() ? "1" : "0");
                listOutput.get(rowIndex[0]).add(item.getItemSearchFlag()? "1" : "0");

                getProcessedData(rowIndex, item.getItemChilds(), listOutput, locale);
                if(item.getItemChilds().isEmpty()) rowIndex[0]++;
            }
        });
        return listOutput;
    }
}
