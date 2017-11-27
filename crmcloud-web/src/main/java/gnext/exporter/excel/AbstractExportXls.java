/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.exporter.excel;

import gnext.exporter.Export;
import gnext.resource.bundle.CompanyBundle;
import gnext.resource.bundle.IssueBundle;
import gnext.resource.bundle.MailBundle;
import gnext.resource.bundle.MsgBundle;
import gnext.resource.bundle.ValidationBundle;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tungdt
 */
public abstract class AbstractExportXls implements Export {
    private static final Logger LOGGER = LoggerFactory.getLogger(MemberExportXls.class);
    public MsgBundle msgBundle = new MsgBundle();
    public ValidationBundle validationBundle = new ValidationBundle();
    public MailBundle mailBundle = new MailBundle();
    public IssueBundle issueBundle = new IssueBundle();
    public CompanyBundle companyBundle = new CompanyBundle();
    
    protected String outputFileName;
    
    public AbstractExportXls(String outputFileName) {
        this.outputFileName = outputFileName;
    }
    
    @Override
    public void execute() throws Exception {
        // SXSSF achieves its low memory footprint by limiting access to the rows that are within a sliding window
        // Once you have a Workbook, you can use the interfaces Sheet, Row, Cell, CellStyle to avoid tying your application to the version-specific interfaces.
        Workbook workbook = new SXSSFWorkbook();
        Sheet sheet = workbook.createSheet("Data");
        sheet.setDefaultColumnWidth(14);

        this.fillHeader(workbook, sheet);
        this.fillBody(workbook, sheet);
        this.fillFooter(workbook, sheet);
        
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMdd");
        download(String.format("%s_%s.xls", outputFileName, sdf2.format(new Date())), workbook);
    }
    
    /**
     * 
     * @param workbook
     * @param sheet
     * @throws Exception 
     * Fill dữ liệu vào header  
     */
    protected abstract void fillHeader(Workbook workbook, Sheet sheet) throws Exception;
    
    /**
     * 
     * @param workbook
     * @param sheet
     * @throws Exception 
     * Fill dữ liệu vào body 
     */
    protected abstract void fillBody(Workbook workbook, Sheet sheet) throws Exception;
    
    /**
     * 
     * @param workbook
     * @param sheet
     * @throws Exception 
     * Fill dữ liệu vào footer 
     */
    protected abstract void fillFooter(Workbook workbook, Sheet sheet) throws Exception;

    private void download(String fileName, Workbook workbook) throws IOException {
        OutputStream responseOutputStream = null;
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(); workbook.write(byteArrayOutputStream);
            FacesContext fc = FacesContext.getCurrentInstance();
            HttpServletResponse response = (HttpServletResponse) fc.getExternalContext().getResponse();
            response.reset();
            response.setHeader("Content-Type", "application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + getFileNameBaseUtf8(fileName) + "\"");
            response.setContentLength( byteArrayOutputStream.size() );
            responseOutputStream = response.getOutputStream();
            responseOutputStream.write(byteArrayOutputStream.toByteArray());
            responseOutputStream.flush();
            responseOutputStream.close();
            fc.responseComplete(); // Important! Otherwise JSF will attempt to render the response which obviously will fail since it's already written with a file and closed.
        } finally {
            try { responseOutputStream.close(); } catch (Exception e) { }
        }
    }

    private String getFileNameBaseUtf8(String fileName) {
        try {
            return URLEncoder.encode(fileName, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        return fileName;
    }
    
    protected Cell createCell(Row row, int col, Object data, CellStyle style) {
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
}
