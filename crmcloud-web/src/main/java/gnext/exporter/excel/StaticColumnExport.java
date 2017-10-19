/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.exporter.excel;

import gnext.bean.mente.MenteItem;
import gnext.exporter.Export;
import gnext.util.DateUtil;
import gnext.util.WebFileUtil;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
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

/**
 *
 * @author hungpham
 */
public class StaticColumnExport implements Export {
    private final List<MenteItem> staticColumnsDataLvl2;
    private final String staticColumnsDataHeaderLvl2;
    
    public StaticColumnExport(String staticColumnsDataHeaderLvl2, List<MenteItem> staticColumnsDataLvl2) {
        this.staticColumnsDataHeaderLvl2 = staticColumnsDataHeaderLvl2;
        this.staticColumnsDataLvl2 = staticColumnsDataLvl2;
    }
    
    @Override
    public void execute() throws Exception {
        List<List<String>> data = new ArrayList<>();
        data.add(Arrays.asList("ID", "名称", "編集", "表示", "検索")); //header column
        
        staticColumnsDataLvl2.forEach((item) -> {
            if(item.getItemDeleted() == Boolean.FALSE){
                data.add(Arrays.asList(
                        item.getItemId().toString(), 
                        item.getItemData(), 
                        item.getItemEditAddFlag() ? "1" : "0", 
                        item.getItemShowFlag() ? "1" : "0",
                        item.getItemSearchFlag() ? "1" : "0"
                ));
            }
        });
        
         //create excel
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet(staticColumnsDataHeaderLvl2);
        
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
                    default:
                        cell.setCellStyle(style);
                }
            });
        });
        
        SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.PATTERN_CSV_EXPORT_DATE);
        WebFileUtil.forceDownload(String.format("%s_%s.xls", staticColumnsDataHeaderLvl2, sdf.format(new Date())), wb);
    }
}
