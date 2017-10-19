/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.importer.excel;

import gnext.bean.mente.MenteItem;
import gnext.bean.mente.MenteOptionDataValue;
import gnext.controller.system.MaintenanceController;
import gnext.importer.Import;
import gnext.model.authority.UserModel;
import gnext.service.mente.MenteService;
import gnext.util.JsfUtil;
import gnext.utils.InterfaceUtil;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hungpham
 */
public class MaintenanceExceptProductImport implements Import {
    private static final Logger LOGGER = LoggerFactory.getLogger(MaintenanceExceptProductImport.class);
    
    private final MenteService menteServiceImpl;
    private final int companyId;
    private final String currentStaticParent;
    private final String locale;
    private final MaintenanceController maintenanceController;
    private final Integer tabIndex;
    
    public MaintenanceExceptProductImport(MaintenanceController maintenanceController) {
        this.maintenanceController = maintenanceController;
        
        this.currentStaticParent = maintenanceController.getCurrentStaticParent();
        this.menteServiceImpl = maintenanceController.getMenteService();
        this.companyId = maintenanceController.getCompanyId();
        this.locale = maintenanceController.getLocale();
        this.tabIndex = maintenanceController.getTabIndex();
    }

    @Override
    public void execute(InputStream is) throws Exception {
        try{
            HSSFWorkbook wb = new HSSFWorkbook(is);
            HSSFSheet sheet = wb.getSheetAt(0);
            
            if(this.tabIndex != null && this.tabIndex == 1){ // static tab
                List<Integer> importedIdList = new ArrayList<>();
                for(int i = 1; i <= sheet.getLastRowNum(); i++){
                    Row row = sheet.getRow(i);
                    int id = NumberUtils.toInt(getCellValue(row.getCell(0)), 0);
                    String name = row.getCell(1).getStringCellValue();
                    boolean editFlag = NumberUtils.toDouble(getCellValue(row.getCell(2)), 0d) == 1d;
                    boolean showFlag = NumberUtils.toDouble(getCellValue(row.getCell(3)), 0d) == 1d;
                    boolean searchFlag = NumberUtils.toDouble(getCellValue(row.getCell(4)), 0d) == 1d;
                    if(!StringUtils.isEmpty(name)){
                        id = mergeMenteItem(id, name, editFlag, showFlag, searchFlag);
                        importedIdList.add(id);
                    }
                }
                
                if(importedIdList.size() > 0) menteServiceImpl.removeItemNotInList(companyId, currentStaticParent, importedIdList);
                maintenanceController.showStaticColumnLvl2(currentStaticParent);
            }else{ //product category item: issue_issue_product_id
                final String TAG = InterfaceUtil.COLS.PRODUCT;
                List<MenteItem> importList = new ArrayList<>();
                List<MenteItem> tmpImportListLvl = new ArrayList<>();
                if(sheet.getLastRowNum() > 2){ //change all recents to DELTE mode
                    menteServiceImpl.removeAll(companyId, TAG);
                }
                for(int i = 2; i <= sheet.getLastRowNum(); i++){
                    Row row = sheet.getRow(i);
                    int cellNum = row.getLastCellNum();
                    for(int j=0, lvl = 1; j< cellNum; j+=5, lvl++){
                        int id = NumberUtils.toInt(getCellValue(row.getCell(j+0)), 0);
                        String name = StringUtils.defaultIfEmpty(row.getCell(j+1).getStringCellValue(), null);
                        boolean editFlag = NumberUtils.toDouble(getCellValue(row.getCell(j+2)), 0d) == 1d;
                        boolean showFlag = NumberUtils.toDouble(getCellValue(row.getCell(j+3)), 0d) == 1d;
                        boolean searchFlag = NumberUtils.toDouble(getCellValue(row.getCell(j+4)), 0d) == 1d;

                        MenteItem item;
                        MenteOptionDataValue lang = null;

                        if(!StringUtils.isEmpty(name) && id > 0 && (item = menteServiceImpl.find(id)) != null){
                            item.getItemChilds().clear();
                            for (Iterator<MenteOptionDataValue> iter = item.getLangs().listIterator(); iter.hasNext(); ) {
                                MenteOptionDataValue l = iter.next();
                                if(l.getMenteOptionDataValuePK().getItemLanguage().equals(locale)){
                                    lang = l;
                                    item.getLangs().remove(l);
                                    break;
                                }
                            }
                            item.setUpdatedTime(new Date());
                            item.setUpdatedId(UserModel.getLogined().getCompanyId());
                        }else{
                            if(!StringUtils.isEmpty(name)){
                                item = new MenteItem();
                                item.setItemName(TAG);
                                item.setCreatorId(UserModel.getLogined().getCompanyId());
                                item.setCreatedTime(new Date());
                            }else{
                                continue;
                            }
                        }
                        item.setItemLevel(lvl);
                        item.setItemEditAddFlag(editFlag);
                        item.setItemShowFlag(showFlag);
                        item.setItemSearchFlag(searchFlag);
                        item.setCompany(UserModel.getLogined().getCompany());
                        item.setItemDeleted(Boolean.FALSE);

                        if(lang == null){
                            lang = new MenteOptionDataValue();
                            lang.getMenteOptionDataValuePK().setItemLanguage(locale);
                            lang.setCompany(UserModel.getLogined().getCompany());
                            lang.setCreatorId(UserModel.getLogined().getUserId());
                            lang.setCreatedTime(new Date());
                        }else{
                            lang.setUpdatedId(UserModel.getLogined().getUserId());
                            lang.setUpdatedTime(new Date());
                        }
                        lang.setItemData(name);
                        lang.setMenteItem(item);
                        item.getLangs().add(lang);

                        if(lvl > 1){
                            MenteItem parent = tmpImportListLvl.get(lvl-2);
                            item.setItemParent(parent);
                            parent.getItemChilds().add(item);
                        }else{
                            boolean itemExists = false;
                            for(MenteItem p : importList){
                                if(item.getItemId()!= null && p.getItemId() == item.getItemId()){
                                    itemExists = true;
                                    break;
                                }
                            }
                            if(!itemExists) importList.add(item);
                        }

                        if(tmpImportListLvl.size() >= lvl) tmpImportListLvl.set(lvl-1, item);
                        else tmpImportListLvl.add(item);
                    }
                }
                for(MenteItem bean : importList) {
                    if(bean.getItemId() == null || bean.getItemId() == 0){
                        menteServiceImpl.create(bean);
                    }else{
                        menteServiceImpl.edit(bean);
                    }
                }
            }
            // Reload cache
            menteServiceImpl.reloadCache(companyId);
            JsfUtil.addSuccessMessage("完了しました。");
        }catch(Exception e){
            JsfUtil.addErrorMessage("エラーがあるので、完了ができない。");
        }
    }
    
    private String getCellValue(Cell cell){
        if(cell != null) return cell.toString();
        return "";
    }
    
    private int mergeMenteItem(Integer id, String name, Boolean editFlag, Boolean showFlag, Boolean searchFlag){
        try {
            MenteItem item = menteServiceImpl.find(id);
            MenteOptionDataValue lang = null;

            boolean addFlag = true;

            //if wrong item id
            if(item != null && !item.getItemName().equals(currentStaticParent)) item = null;

            if(item == null){
                item = new MenteItem();
                item.setItemId(id);

                item.setCreatorId(UserModel.getLogined().getCompanyId());
                item.setCreatedTime(new Date());
            }else{
                addFlag = false;
                for(MenteOptionDataValue l : item.getLangs()){
                    if(l.getMenteOptionDataValuePK().getItemLanguage().equals(locale)){
                        lang = l;
                        item.getLangs().remove(l);
                        break;
                    }
                }

                item.setUpdatedTime(new Date());
                item.setUpdatedId(UserModel.getLogined().getCompanyId());
            }

            item.setItemName(currentStaticParent);
            item.setItemLevel(1);
            item.setItemEditAddFlag(editFlag);
            item.setItemShowFlag(showFlag);
            item.setItemSearchFlag(searchFlag);
            item.setCompany(UserModel.getLogined().getCompany());
            item.setItemDeleted(Boolean.FALSE);

            if(lang == null){
                lang = new MenteOptionDataValue();
                lang.getMenteOptionDataValuePK().setItemLanguage(locale);
                lang.setCompany(UserModel.getLogined().getCompany());
                lang.setCreatorId(UserModel.getLogined().getUserId());
                lang.setCreatedTime(new Date());
            }else{
                lang.setUpdatedId(UserModel.getLogined().getUserId());
                lang.setUpdatedTime(new Date());
            }
            lang.setItemData(name);
            lang.setMenteItem(item);

            item.getLangs().add(lang);

            if(addFlag) menteServiceImpl.create(item); 
            else menteServiceImpl.edit(item);

            return item.getItemId();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return 0;
    }
}
