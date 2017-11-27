/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.importer.excel;

import gnext.importer.Import;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.Cell;

/**
 *
 * @author daind
 */
public abstract class AbstractImportXls implements Import {

    protected String getAsString(Cell cell) {
        if(cell == null) return null;
        switch (cell.getCellType()) {
            case HSSFCell.CELL_TYPE_BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case HSSFCell.CELL_TYPE_NUMERIC:
                Double d = cell.getNumericCellValue();
                return String.valueOf(d.intValue());
            case HSSFCell.CELL_TYPE_STRING:
                return cell.getStringCellValue();
            default:
                if (HSSFDateUtil.isCellDateFormatted(cell)) return String.valueOf(cell.getDateCellValue());
        }
        return null;
    }
}
