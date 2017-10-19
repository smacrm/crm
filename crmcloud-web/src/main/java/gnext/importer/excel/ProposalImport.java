/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.importer.excel;

import gnext.bean.Company;
import gnext.bean.Member;
import gnext.bean.mente.MenteItem;
import gnext.bean.mente.MenteOptionDataValue;
import gnext.importer.Import;
import gnext.model.authority.UserModel;
import gnext.service.mente.MenteService;
import gnext.util.DateUtil;
import java.io.InputStream;
import java.util.Date;
import java.util.Iterator;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.util.StringUtils;

/**
 *
 * @author daind
 */
public class ProposalImport implements Import {
    
    private final String locale;
    private final Company companyLogined;
    private final Member memberLogined;
    private final MenteService menteServiceImpl;
    
    public ProposalImport(String locale, Company companyLogined, Member memberLogined, MenteService menteServiceImpl) {
        this.locale = locale;
        this.companyLogined = companyLogined;
        this.memberLogined = memberLogined;
        this.menteServiceImpl = menteServiceImpl;
    }
    
    @Override
    public void execute(InputStream is) throws Exception {
        Workbook workbook = new HSSFWorkbook(is);
        for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
            Sheet sheet = workbook.getSheetAt(sheetIndex);
            for (int i = 2; i <= sheet.getLastRowNum(); i++) {
                try {
                    Row row = sheet.getRow(i);
                    if(row == null) continue;
                    if(row.getCell(0) == null) continue;
                    
                    int col = 0;

                    String id = row.getCell(col++).getStringCellValue();
                    String parent = row.getCell(col++).getStringCellValue();
                    String data = row.getCell(col++).getStringCellValue();
                    String order = row.getCell(col++).getStringCellValue();
                    String sensor = row.getCell(col++).getStringCellValue();

                    // id
                    MenteItem menteItem = null;
                    boolean forUpdate = false;
                    if(!StringUtils.isEmpty(id) && NumberUtils.isDigits(id)) {
                        Integer itemId = Integer.parseInt(id);
                        menteItem = menteServiceImpl.find(itemId);
//                        menteItem.setUpdatedId(memberLogined.getMemberId());
//                        menteItem.setUpdatedTime(DateUtil.now());
                        forUpdate = true;
                    }
                    if(menteItem == null) 
                        menteItem = new MenteItem();

                    // parent
                    if(!StringUtils.isEmpty(parent) && NumberUtils.isDigits(parent)) {
                        Integer parentItemId = Integer.parseInt(parent);
                        MenteItem parentMenteItem =  menteServiceImpl.find(parentItemId);
                        if(parentMenteItem != null) {
                            menteItem.setItemParent(parentMenteItem);
                        }
                    }

                    // data
                    MenteOptionDataValue lang = null;
                    for (Iterator<MenteOptionDataValue> iter = menteItem.getLangs().listIterator(); iter.hasNext(); ) {
                        MenteOptionDataValue it = iter.next();
                        if(it.getMenteOptionDataValuePK().getItemLanguage().equals(locale)){
                            lang = it;
                            iter.remove();
                            break;
                        }
                    }
                    if(lang != null){
                        lang.setUpdatedId(UserModel.getLogined().getUserId());
                        lang.setUpdatedTime(new Date());
                    }else{
                        lang = new MenteOptionDataValue();
                        lang.setMenteItem(menteItem);
                        lang.getMenteOptionDataValuePK().setItemLanguage(locale);
                        lang.setCompany(UserModel.getLogined().getCompany());
                        lang.setCreatorId(UserModel.getLogined().getUserId());
                        lang.setCreatedTime(new Date());
                    }
                    lang.setItemData(data);
                    menteItem.getLangs().add(lang);
                    
                    // order
                    if(!StringUtils.isEmpty(order) && NumberUtils.isDigits(order)) {
                        int itemOrder = Integer.parseInt(order);
                        menteItem.setItemOrder(itemOrder);
                    } else {
                        menteItem.setItemOrder(1);
                    }

                    // sensor
                    if(!StringUtils.isEmpty(sensor) && NumberUtils.isDigits(sensor)) {
                        int itemSensor = Integer.parseInt(sensor);
                        menteItem.setItemRiskSensor((itemSensor != 0));
                    }

                    // company
                    menteItem.setCompany(companyLogined);

                    // time
                    if(forUpdate) {
                        menteItem.setUpdatedId(memberLogined.getMemberId());
                        menteItem.setUpdatedTime(DateUtil.now());
                    } else {
                        menteItem.setCreatorId(memberLogined.getMemberId());
                        menteItem.setCreatedTime(DateUtil.now());
                    }

                    // level
                    int level = 1;
                    if(menteItem.getItemParent() != null) {
                        level = menteItem.getItemParent().getItemLevel() + 1;
                    }
                    menteItem.setItemLevel(level);

                    // name
                    menteItem.setItemName("issue_proposal_id");
                    
                    // status
                    menteItem.setItemDeleted(false);
                    
                    if(forUpdate) {
                        menteServiceImpl.edit(menteItem);
                    } else {
                        menteServiceImpl.create(menteItem);
                    }
                } catch (Exception e) {
                    // Donothing here!!!
                    
                    e.printStackTrace();
                }
            }
        }
    }
    
}
