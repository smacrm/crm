/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.importer.excel;

import gnext.bean.Company;
import gnext.bean.Prefecture;
import gnext.bean.issue.CustTargetInfo;
import gnext.bean.issue.Customer;
import gnext.bean.mente.MenteItem;
import gnext.service.PrefectureService;
import gnext.service.issue.CustomerService;
import gnext.service.mente.MenteService;
import gnext.util.DateUtil;
import gnext.util.IssueUtil;
import gnext.utils.InterfaceUtil.COLS;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author daind
 */
public class CustomerImport extends AbstractImportXls {

    private final PrefectureService prefectureService;
    private final CustomerService customerService;
    private final MenteService menteService;
    
    private final int loginId;
    private final Company company;
    private final String language;
    
    private final String fileUploadName;
    private List<MenteItem> menteItems;
    
    public CustomerImport(PrefectureService prefectureService, CustomerService customerService, MenteService menteService,
            int loginId, Company company, String language, String fileUploadName) {
        this.prefectureService = prefectureService;
        this.customerService = customerService;
        this.menteService = menteService;
        this.loginId = loginId;
        this.company = company;
        this.language = language;
        this.fileUploadName = fileUploadName;
        init();
    }
    
    private void init() {
        menteItems = this.menteService.getAllLevels(company.getCompanyId());
    }
    
    private Workbook getWorkbook(InputStream is) throws Exception {
        String ext = FilenameUtils.getExtension(this.fileUploadName);
        if("xls".equalsIgnoreCase(ext)) {
            return new HSSFWorkbook(is);
        }
        return new XSSFWorkbook(is);
    }
    
    @Override
    public void execute(InputStream is) throws Exception {
        List<Customer> customers = new ArrayList<>();
        Workbook wb = getWorkbook(is);
        for (int sheetIndex = 0; sheetIndex < wb.getNumberOfSheets(); sheetIndex++) {
            Sheet sheet = wb.getSheetAt(sheetIndex);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if(row == null) continue;
                int c = 0; if(row.getCell(c) == null) continue;
                String cus_offer = getAsString(row.getCell(c++));
                String cus_code = getAsString(row.getCell(c++));
                String cus_name_kana = getAsString(row.getCell(c++));
                String cus_name_hira = getAsString(row.getCell(c++));
                String cus_gender = getAsString(row.getCell(c++));
                String cus_age = getAsString(row.getCell(c++));
                String cus_post = getAsString(row.getCell(c++));
                String cus_city = getAsString(row.getCell(c++));
                String cus_address = getAsString(row.getCell(c++));
                String cus_address_kana = getAsString(row.getCell(c++));
                String cus_tel = getAsString(row.getCell(c++));
                String cus_tel_type = getAsString(row.getCell(c++));
                String cus_mobile = getAsString(row.getCell(c++));
                String cus_mobile_type = getAsString(row.getCell(c++));
                String cus_mail = getAsString(row.getCell(c++));
                String cus_meno = getAsString(row.getCell(c++));
                
                Customer cus = findCus(cus_code);
                cus.setCustAddress(cus_address);
                cus.setCustAddressKana(cus_address_kana);
                cus.setCustFirstHira(cus_name_hira);
                cus.setCustFirstKana(cus_name_kana);
                cus.setCustPost(cus_post);
                cus.setCustMemo(cus_meno);
                
                parseMailTelMobile(cus, cus_mail, cus_tel, cus_tel_type, cus_mobile, cus_mobile_type);
                parseCusPrefecture(cus_city, cus);
                parseCusMente(cus_gender, cus_age, cus_offer, cus);
                
                validation(cus, i);
                customers.add(cus);
            }
        }
        customerService.importXls(customers);
    }
    
    private void parseMailTelMobile(Customer cus, String cus_mail,
            String cus_tel, String cus_tel_type,
            String cus_mobile, String cus_mobile_type) {
        List<CustTargetInfo> custTargetInfoList = cus.getCustTargetInfoList();
        custTargetInfoList.clear();
        
        MenteItem custTargetClassTel = null;
        MenteItem custTargetClassMobile = null;
        for(MenteItem item : menteItems) {
            if(!item.getItemName().equals(COLS.SESSION)) continue;
            if(item.getItemViewData(language).equalsIgnoreCase(cus_tel_type)) custTargetClassTel = item;
            if(item.getItemViewData(language).equalsIgnoreCase(cus_mobile_type)) custTargetClassMobile = item;
        }
        
        // MAIL
        custTargetInfoList.add(IssueUtil.createCustTargetInfo(cus, (short)1, cus_mail));
        
        // TEL
        CustTargetInfo cti_tel = IssueUtil.createCustTargetInfo(cus, (short)2, cus_tel);
        cti_tel.setCustTargetClass(custTargetClassTel);
        custTargetInfoList.add(cti_tel);
        
        // MOBILE
        CustTargetInfo cti_mobile = IssueUtil.createCustTargetInfo(cus, (short)3, cus_mobile);
        cti_mobile.setCustTargetClass(custTargetClassMobile);
        custTargetInfoList.add(cti_mobile);
    }
        
    private void validation(Customer cus, int row) throws Exception {
        if(StringUtils.isEmpty(cus.getCustCode()))
            throw new Exception("ERROR_CUST_IMPORT:会員コードは空白にすることはできません。");
    }
    
    private Customer findCus(String cus_code) {
        Customer cus = customerService.search(cus_code, company.getCompanyId());
        if(cus == null) {
            cus = new Customer();
            cus.setCompany(company);
            cus.setCreatedTime(DateUtil.now());
            cus.setCreatorId(loginId);
            cus.setCustDeleted(Boolean.FALSE);
            cus.setCustCode(cus_code);
        } else {
            cus.setUpdatedId(loginId);
            cus.setUpdatedTime(DateUtil.now());
        }
        return cus;
    }
    
    private void parseCusPrefecture(String cus_city, Customer aNewCus) {
        Prefecture prefecture = prefectureService.findByPrefectureName(language, cus_city);
        if(prefecture != null) aNewCus.setCustCity(prefecture);
    }
    
    private void parseCusMente(String cus_gender, String cus_age, String cus_offer, Customer aNewCus) {
        if(menteItems == null || menteItems.isEmpty()) return;
        for(MenteItem item : menteItems) {
            if(!item.getItemName().equals(COLS.SEX)
                    && !item.getItemName().equals(COLS.AGE)
                    && !item.getItemName().equals(COLS.COOPERATION)
                    && !item.getItemName().equals(COLS.SPECIAL)) continue;
            if(item.getItemName().equals(COLS.SEX) && item.getItemViewData(language).equalsIgnoreCase(cus_gender))
                aNewCus.setCustSexId(item);
            
            if(item.getItemName().equals(COLS.AGE) && item.getItemViewData(language).equalsIgnoreCase(cus_age))
                aNewCus.setCustAgeId(item);
            
            if(item.getItemName().equals(COLS.COOPERATION) && item.getItemViewData(language).equalsIgnoreCase(cus_offer))
                aNewCus.setCustCooperationId(item);
            
            if(item.getItemName().equals(COLS.SPECIAL))
                aNewCus.setCustSpecialId(item);
        } // :::end for:::
    }
}
