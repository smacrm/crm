/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.importer.excel;

import gnext.bean.label.PropertyItemLabel;
import gnext.exporter.excel.MemberExportXls;
import gnext.importer.Import;
import gnext.model.authority.UserModel;
import gnext.service.label.LabelService;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author daind
 */
public class LabelImport implements Import {
    private static final Logger LOGGER = LoggerFactory.getLogger(MemberExportXls.class);

    private final Map<String, String> resourceList;
    private final List<String> dbKeys;
    private final LabelService labelServiceImpl;
    private final int companyId;
    
    public LabelImport(Map<String, String> resourceList, List<String> dbKeys, LabelService labelServiceImpl, int companyId) {
        this.resourceList = resourceList;
        this.dbKeys = dbKeys;
        this.labelServiceImpl = labelServiceImpl;
        this.companyId = companyId;
    }
    
    @Override
    public void execute(InputStream is) throws Exception {
        HSSFWorkbook wb = new HSSFWorkbook(is);
        for (int sheetIndex = 0; sheetIndex < wb.getNumberOfSheets(); sheetIndex++) {
            HSSFSheet sheet = wb.getSheetAt(sheetIndex);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                HSSFRow row = sheet.getRow(i);

                String module = row.getCell(0).getStringCellValue();
                String code = row.getCell(1).getStringCellValue();
                String language = row.getCell(2, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
                String oldText = row.getCell(4, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
                String text = row.getCell(6, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();

                if (oldText.equals(text)) {
                    continue; //ignore non edited value
                }
                PropertyItemLabel lbl = new PropertyItemLabel(code, companyId);
                lbl.setModule(module);
                lbl.setLabelLanguage(language);
                lbl.setLabelName(text);
                lbl.setCompany(UserModel.getLogined().getCompany());

                resourceList.put(code, text);

                if (dbKeys.contains(code)) {
                    labelServiceImpl.edit(lbl);
                } else {
                    labelServiceImpl.create(lbl);
                    dbKeys.add(code);
                }
            }
        }
    }

}
