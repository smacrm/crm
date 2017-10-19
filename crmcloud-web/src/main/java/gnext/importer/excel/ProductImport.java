/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.importer.excel;

import gnext.bean.mente.Products;
import gnext.importer.Import;
import gnext.model.authority.UserModel;
import gnext.service.mente.ProductService;
import gnext.util.JsfUtil;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.PropertyAccessorFactory;

/**
 *
 * @author hungpham
 */
public class ProductImport implements Import {
    private final ProductService productService;
    private final int companyId;
    
    public ProductImport(ProductService productService, int companyId) {
        this.productService = productService;
        this.companyId = companyId;
    }
    
    @Override
    public void execute(InputStream is) throws Exception {
        try{
            HSSFWorkbook wb = new HSSFWorkbook(is);
            HSSFSheet sheet = wb.getSheetAt(0);
            FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
            List<String> headers = this.getExportProductHeaders();
            List<String> headerAsProperties = new ArrayList<>();

            headers.forEach((t) -> {
                String p = WordUtils.capitalizeFully(t, '_').replaceAll("_", "");
                p = Character.toLowerCase(p.charAt(0)) + p.substring(1);
                headerAsProperties.add(p);
            });

            List<Products> importedProductList = new ArrayList<>();
            List<String> importedCodeList = new ArrayList<>();
            for(int i = 1; i <= sheet.getLastRowNum(); i++){
                HSSFRow row = sheet.getRow(i);
                int celNum = row.getLastCellNum();
                Products item = new Products();
                for(int j = 0; j< celNum; j++){
                    HSSFCell cell = row.getCell(j);
                    Object v = null;
                    if (cell != null) {
                        switch (cell.getCellType()) {
                            case HSSFCell.CELL_TYPE_BOOLEAN:
                                v = cell.getBooleanCellValue();
                                break;
                            case HSSFCell.CELL_TYPE_NUMERIC:
                                v = cell.getNumericCellValue();
                                break;
                            case HSSFCell.CELL_TYPE_STRING:
                                v = cell.getStringCellValue();
                                break;
                            default:
                                if(HSSFDateUtil.isCellDateFormatted(cell)){
                                    v = cell.getDateCellValue();
                                }
                        }
                    }
                    String p = WordUtils.capitalizeFully(headers.get(j), '_').replaceAll("_", "");
                    p = Character.toLowerCase(p.charAt(0)) + p.substring(1);
                    try{
                        PropertyAccessorFactory.forBeanPropertyAccess(item).setPropertyValue(p, v);
                    }catch(Exception e){}
                }
                if(!StringUtils.isEmpty(item.getProductsCode())){
                    importedProductList.add(item);
                    importedCodeList.add(item.getProductsCode());
                }
            }

            List<String> ignoredProperties = new ArrayList<>();
            Arrays.asList(BeanUtils.getPropertyDescriptors(Products.class)).forEach((property) -> {
                ignoredProperties.add(property.getName());
            });

            ignoredProperties.removeAll(headerAsProperties);

            int userId = UserModel.getLogined().getUserId();
            importedProductList.forEach((item) -> {
                Products bean = productService.getProductByCode(companyId, item.getProductsCode(), Boolean.TRUE);
                if(bean == null){
                    bean = item;
                    bean.setProductsCreatedDatetime(new Date());
                    bean.setProductsCreatorId(userId);
                }else{
                    BeanUtils.copyProperties(item, bean, ignoredProperties.toArray(new String[0]));
                    bean.setProductsUpdatedDatetime(new Date());
                    bean.setProductsUpdaterId(userId);
                }
                bean.setCompanyId(companyId);
                bean.setProductsIsDeleted(Boolean.FALSE);
                try{
                    productService.saveProduct(bean);
                }catch (Exception cause) {
                    if (cause instanceof ConstraintViolationException) {
                        ConstraintViolationException cve = (ConstraintViolationException) cause;
                        for (Iterator<ConstraintViolation<?>> it = cve.getConstraintViolations().iterator(); it.hasNext();) {
                            ConstraintViolation<? extends Object> v = it.next();
                        }
                    }
                }
            });
            productService.removeAllProductsExcept(companyId, importedCodeList);

            JsfUtil.addSuccessMessage("完了しました。");
        }catch(Exception e){
            JsfUtil.addErrorMessage("エラーがあるので、完了ができない。");
        }
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
