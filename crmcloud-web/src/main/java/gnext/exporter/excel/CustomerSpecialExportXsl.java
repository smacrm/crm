/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.exporter.excel;

import gnext.util.StringUtil;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

/**
 *
 * @author daind
 */
public class CustomerSpecialExportXsl extends AbstractExportXls {
    private final List<Map<String, String>> results;
    public CustomerSpecialExportXsl(List<Map<String, String>> results, String fileName) {
        super(fileName);
        this.results = results;
    }

    @Override
    protected void fillHeader(Workbook workbook, Sheet sheet) throws Exception {
        String[] headers = {
                issueBundle.getString("label.cust_cooperation_name")
                , issueBundle.getString("label.cust_code")
                , issueBundle.getString("label.cust_special_name")
                , issueBundle.getString("label.sex")
                , issueBundle.getString("label.age")
                , issueBundle.getString("label.memo")
                , issueBundle.getString("label.memo")
        };
        Row row = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) createCell(row, i, headers[i], createHeaderCellStyle(workbook));
    }
    
    @Override
    protected void fillBody(Workbook workbook, Sheet sheet) throws Exception {
        CellStyle style = creatBodyCellStyle(workbook);
        int rowIndex = 1;
        for(Map<String, String> result : results) {
            Row row = sheet.createRow(rowIndex++); int col = 0;
            
            createCell(row, col++, StringUtil.getStringNullToEmpty(result.get("cust_cooperation_name")), style);
            createCell(row, col++, StringUtil.getStringNullToEmpty(result.get("cust_code")), style);
            createCell(row, col++, StringUtil.getStringNullToEmpty(result.get("cust_special_name")), style);
            createCell(row, col++, StringUtil.getStringNullToEmpty(result.get("cust_sex_name")), style);
            createCell(row, col++, StringUtil.getStringNullToEmpty(result.get("cust_age_name")), style);
            createCell(row, col++, StringUtil.getStringNullToEmpty(result.get("cust_memo")), style);
            
        }
    }

    @Override
    protected void fillFooter(Workbook workbook, Sheet sheet) throws Exception { }
    
    ///////////// PRIVATE METHODS /////////////
    private CellStyle creatBodyCellStyle(Workbook workbook) {
        CellStyle cellStyle = workbook.createCellStyle();

        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);

        return cellStyle;
    }
    
    private CellStyle createHeaderCellStyle(Workbook workbook) {
        CellStyle cellStyle = workbook.createCellStyle();

        cellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index);
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        cellStyle.setBorderBottom(BorderStyle.MEDIUM);
        cellStyle.setBorderTop(BorderStyle.MEDIUM);
        cellStyle.setBorderLeft(BorderStyle.MEDIUM);
        cellStyle.setBorderRight(BorderStyle.MEDIUM);
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        cellStyle.setFont(headerFont);

        return cellStyle;
    }
}
