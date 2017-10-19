/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.exporter.excel;

import gnext.bean.mente.Products;
import gnext.exporter.Export;
import gnext.model.authority.UserModel;
import gnext.service.mente.ProductService;
import gnext.util.DateUtil;
import gnext.util.JsfUtil;
import gnext.util.ResourceUtil;
import gnext.util.WebFileUtil;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
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
import org.springframework.beans.PropertyAccessorFactory;

/**
 *
 * @author hungpham
 */
public class ProductExport implements Export {
    private final ProductService productService;
    private final int companyId;
    private final String locale;
    
    public ProductExport(String locale, int companyId, ProductService productService) {
        this.locale = locale;
        this.companyId = companyId;
        this.productService = productService;
    }
    
    @Override
    public void execute() throws Exception {
        exportProduct();
    }
    
    private void exportProduct() throws Exception {
        if(StringUtils.isBlank(locale)) return;
        Locale newLocale = new Locale(locale, "");
         //fetch data
        List<List<String>> data = new ArrayList<>();
        List<String> headers = this.getExportProductHeaders();
        
        data.add(new ArrayList());
        int companyId = UserModel.getLogined().getCompanyId();
        headers.forEach((h) -> {
            data.get(0).add(JsfUtil.getResource().message(companyId, ResourceUtil.BUNDLE_MAINTE_NAME, "label.mainte.export.header."+h));
        });
        
        List<Products> dataItems = productService.getAllProducts(companyId);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        
        dataItems.forEach((item) -> {
            List<String> dataItem = new ArrayList<>();
            headers.forEach((property) -> {
                String p = WordUtils.capitalizeFully(property, '_').replaceAll("_", "");
                p = Character.toLowerCase(p.charAt(0)) + p.substring(1);
                Object o = PropertyAccessorFactory.forBeanPropertyAccess(item).getPropertyValue(p);
                
                String v = "";
                if(o != null){
                    if(o instanceof Date){
                        v = sdf.format(o);
                    }else if(o instanceof Boolean){
                        v = (Boolean)o ? "1": "0";
                    }else{
                        v = String.valueOf(o);
                    }
                }
                
                dataItem.add(v);
            });
            data.add(dataItem);
        });
  
        String sheetName = JsfUtil.getResource().message(this.companyId, ResourceUtil.BUNDLE_MSG, newLocale, "label.goods.master");
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
        
        // Auto size the column widths
        for(int i = 0; i < headers.size(); i++){
            sheet.autoSizeColumn(i);
        }
        
        SimpleDateFormat sdf2 = new SimpleDateFormat(DateUtil.PATTERN_CSV_EXPORT_DATE);
        WebFileUtil.forceDownload(String.format("%s_%s.xls", sheetName, sdf2.format(new Date())), wb);
    }
    
    /**
     * Configurate excel product headers
     * @return 
     */
    private List<String> getExportProductHeaders(){
        return Arrays.asList(
                "products_code",
                // TOP block
                "products_category_small_id",
                "products_public_start_day",
                "products_public_end_day",
                "products_order",
                "products_is_public",
                "products_name",
                "products_description",

                // 商品概要 block
                "products_real_code",
                "products_jan_code",
                "products_itf_code",
                "products_sale_start_date",
                "products_sale_end_date",
                "products_capacity",
                "products_type_package",
                "products_expiration_date",
                "products_expiration_position",
                "products_process_method",
                "products_price",
                "products_factory",
                "products_provider",
                "products_other_size",
                "products_dept_in_charge",

                // 成分詳細 block
                "products_details_type_name",
                "products_details_material_name",
                "products_details_material_made_position",
                "products_details_additives",
                "products_details_modify_gen",
                "products_details_caffeine",
                "products_details_alcohol_name",
                "products_details_note",
                "products_details_unit_display",
                "products_details_energy",
                "products_details_protein",
                "products_details_lipid",
                "products_details_natri",
                "products_details_gluxit",
                "products_details_calcium",
                "products_details_salt",
                "products_details_other_component_1_name",
                "products_details_other_component_1_percent",
                "products_details_other_component_2_name",
                "products_details_other_component_2_percent",
                "products_details_other_component_3_name",
                "products_details_other_component_3_percent",
                "products_details_other_component_4_name",
                "products_details_other_component_4_percent",
                "products_details_other_component_5_name",
                "products_details_other_component_5_percent",
                "products_details_other_component_6_name",
                "products_details_other_component_6_percent",
                "products_details_other_component_7_name",
                "products_details_other_component_7_percent",
                "products_details_other_component_8_name",
                "products_details_other_component_8_percent",
                "products_details_component",

                // パッケージ block
                "products_package_material",
                "products_package_size",
                "products_package_note",

                // アレルギー物質 block
                "products_allergen_flag_egg",
                "products_allergen_flag_milk",
                "products_allergen_flag_wheat",
                "products_allergen_flag_soba",
                "products_allergen_flag_nuts",
                "products_allergen_flag_shrimp",
                "products_allergen_flag_crab",

                "products_allergen_flag_abalone",
                "products_allergen_flag_squid",
                "products_allergen_flag_ikura",
                "products_allergen_flag_sake",
                "products_allergen_flag_mackerel",
                "products_allergen_flag_orange",
                "products_allergen_flag_kiwi",
                "products_allergen_flag_banana",
                "products_allergen_flag_yam",
                "products_allergen_flag_apple",
                "products_allergen_flag_beef",
                "products_allergen_flag_chicken",
                "products_allergen_flag_pork",
                "products_allergen_flag_gelatin",
                "products_allergen_flag_kurumi",
                "products_allergen_flag_soy",
                "products_allergen_flag_matsutake",
                "products_allergen_flag_taro",
                "products_allergen_flag_sesame",
                "products_allergen_flag_cashew_nuts",

                "products_allergen_contamination",
                "products_self_management_component",
                "products_self_management_note",

                // その他 block
                "products_sales_catch",
                "products_memo",
                "products_other");
    }
}
