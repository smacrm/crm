package gnext.util;

import au.com.bytecode.opencsv.CSVReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

/**
 *
 * @author hungpham
 * @since May 31, 2017
 */
public class CSVToExcel {

    public static HSSFWorkbook convert(InputStream is) throws Exception {

        /* Step -1 : Read input CSV from InputStream */
        CSVReader reader = new CSVReader(new InputStreamReader(is));
        /* Variables to loop through the CSV File */
        String[] nextLine;
        /* for every line in the file */
        int lnNum = 0;
        /* line number */
        /* Step -2 : Define POI Spreadsheet objects */
        HSSFWorkbook new_workbook = new HSSFWorkbook(); //create a blank workbook object
        HSSFSheet sheet = new_workbook.createSheet("Sheet1");  //create a worksheet with caption score_details
        /* Step -3: Define logical Map to consume CSV file data into excel */
        Map<String, Object[]> excel_data = new HashMap<>(); //create a map and define data
        /* Step -4: Populate data into logical Map */
        while ((nextLine = reader.readNext()) != null) {
            lnNum++;
            excel_data.put(Integer.toString(lnNum), nextLine);
        }
        /* Step -5: Create Excel Data from the map using POI */
        Set<String> keyset = excel_data.keySet();
        int rownum = 0;
        for (String key : keyset) { //loop through the data and add them to the cell
            Row row = sheet.createRow(rownum++);
            Object[] objArr = excel_data.get(key);
            int cellnum = 0;
            for (Object obj : objArr) {
                Cell cell = row.createCell(cellnum++);
                if (obj instanceof String) {
                    cell.setCellValue((String) obj);
                }
            }
        }
        /* Write XLS converted CSV file to the output file */
        return new_workbook;
    }
}
