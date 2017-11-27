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
import gnext.util.WebFileUtil;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;

/**
 *
 * @author daind
 */
public class ProposalExportXls implements Export {
    
    private final String locale;
    private final int companyId;
    private final MenteService menteServiceImpl;
    
    public ProposalExportXls(String locale, int companyId, MenteService menteServiceImpl) {
        this.locale = locale;
        this.companyId = companyId;
        this.menteServiceImpl = menteServiceImpl;
    }
    
    @Override
    public void execute() throws Exception {
        if(StringUtils.isBlank(locale)) return;
        Locale newLocale = new Locale(locale, "");
        
        String groupHeaderLabel = "申出分類";
        String[] headers = {
            "ID",
            "PARENT",
            "名前",
            "順番",
            "リスクセンサ"
        };
        
        int row = 0;
        
        // INIT
        Workbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet("Proposal");
        sheet.setDefaultColumnWidth(14);
        
        // HEADERS
        Row headerGroupRow = sheet.createRow(row++);
        createCell(headerGroupRow, 0, groupHeaderLabel, createHeaderCellStyle(workbook));
                
        Row headerRow = sheet.createRow(row++);
        for (int i = 0; i < headers.length; i++)
            createCell(headerRow, i, headers[i], createHeaderCellStyle(workbook));
        
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, headers.length - 1));
        
        // DATA
        List<MenteItem> menteItems = menteServiceImpl.findByName("issue_proposal_id", companyId);
        CellStyle cellStyle = creatBodyCellStyle(workbook);
        for(MenteItem menteItem : menteItems) {
            String id = String.valueOf(menteItem.getItemId());
            String parent = menteItem.getItemParent() == null ? "" : String.valueOf(menteItem.getItemParent().getItemId());
            String name = menteItem.getItemViewData(locale);
            String order = String.valueOf(menteItem.getItemOrder());
            String sensor = (menteItem.getItemRiskSensor() != null && menteItem.getItemRiskSensor() == true) ? "1" : "0";
            
            Row rowData = sheet.createRow(row++);
            int col = 0;
            createCell(rowData, col++, id, cellStyle);
            createCell(rowData, col++, parent, cellStyle);
            createCell(rowData, col++, name, cellStyle);
            createCell(rowData, col++, order, cellStyle);
            createCell(rowData, col++, sensor, cellStyle);
        }
        
        // EXPORT
        WebFileUtil.forceDownload(String.format("%s_%s.xls", "Proposal", new SimpleDateFormat(DateUtil.PATTERN_CSV_EXPORT_DATE).format(new Date())), workbook);
    }
    
    private CellStyle creatBodyCellStyle(Workbook workbook) {
        CellStyle cellStyle = workbook.createCellStyle();

        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);

        return cellStyle;
    }
    
    private Cell createCell(Row row, int col, Object data, CellStyle style) {
        Cell cell = row.createCell(col);
        
        if(data == null) {
            cell.setCellValue(StringUtils.EMPTY);
        } else if (data instanceof String) {
            cell.setCellValue((String) data);
        } else if (data instanceof Number) {
            cell.setCellValue(((Number) data).doubleValue());
        } else if (data instanceof Date) {
            cell.setCellValue((Date) data);
        } else if (data instanceof Calendar) {
            cell.setCellValue((Calendar)data);
        } else if (data instanceof Boolean) {
            cell.setCellValue((Boolean)data ? "1" : "0");
        } else if (data instanceof RichTextString) {
            cell.setCellValue((RichTextString)data);
        } else {
            cell.setCellValue(data.toString());
        }
        
        cell.setCellStyle(style);
        return cell;
    }
    
    private CellStyle createHeaderCellStyle(Workbook workbook) {
        CellStyle headerStyle = workbook.createCellStyle();

        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index);
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.MEDIUM);
        headerStyle.setBorderTop(BorderStyle.MEDIUM);
        headerStyle.setBorderLeft(BorderStyle.MEDIUM);
        headerStyle.setBorderRight(BorderStyle.MEDIUM);
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        return headerStyle;
    }
}
