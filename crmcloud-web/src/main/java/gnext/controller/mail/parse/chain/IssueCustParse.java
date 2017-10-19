package gnext.controller.mail.parse.chain;

import gnext.bean.Company;
import gnext.bean.Member;
import gnext.bean.Prefecture;
import gnext.controller.issue.IssueController;
import gnext.controller.mail.parse.MailParse;
import gnext.bean.issue.CustTargetInfo;
import gnext.bean.issue.Customer;
import gnext.bean.issue.Issue;
import gnext.bean.mente.MenteItem;
import gnext.bean.mente.MenteOptionDataValue;
import gnext.controller.mail.parse.MailCustParse;
import gnext.model.authority.UserModel;
import gnext.service.MemberService;
import gnext.service.issue.IssueCustomerService;
import gnext.util.DateUtil;
import gnext.util.IssueUtil.ALLOW_SEARCH_COL;
import gnext.util.JsfUtil;
import gnext.util.StringUtil;
import gnext.utils.InterfaceUtil.COLS;
import gnext.utils.InterfaceUtil.TARGET;
import gnext.validator.BaseValidator;
import gnext.validator.EmailValidator;
import gnext.validator.PhoneFaxValidator;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.model.SelectItem;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

/**
 *
 * @author daind
 */
public class IssueCustParse implements MailParse, MailCustParse {
    IssueController issueController;
    UserModel isLogin;
    public IssueCustParse() {
        isLogin = UserModel.getLogined();
    }

    @Override
    public void parse(Issue issue, Map<String, String> mappings, Map<String, String> params) throws Exception {
        if(issue == null || isLogin == null) return;
        if(mappings == null || mappings.isEmpty()) return;
        
        this.issueController = JsfUtil.getManagedBean("issueController", IssueController.class);
        // phân tích dữ liệu đối với customer name.
        remainningCustInfor(mappings, issue, params);
    }
    
    private void remainingCustHiraKana(Map<String, String> remaining, Customer customer, String type) {
        if(remaining == null || remaining.isEmpty()) return;
        String key = null;
        
        Set<String> remainingKeys = remaining.keySet();
        for(String remainingKey : remainingKeys) {
            if(remainingKey.contains(type)) {
                key = remainingKey; break;
            }
        }
        if(StringUtils.isEmpty(key)) return;
        String val = remaining.get(key);
        if(StringUtils.isEmpty(val)) return;
        val = StringUtils.strip(val);
        
        String[] separatorName = val.split("\\s+");
        if(separatorName.length == 1) separatorName = val.replaceAll("　", " ").split(" ", 0);
        String first = null; String last = null;
        if(separatorName.length == 1) {
            first = separatorName[0];
            last = "";
        } else {
            first = separatorName[0];
            last = separatorName[1];
        }
        if(StringUtils.isEmpty(first) && StringUtils.isEmpty(last)) return;
        
        if("cust_name_hira".equals(type)) {
            customer.setCustFirstHira(first);
            customer.setCustLastHira(last);
        }
        if("cust_name_kana".equals(type)) {
            customer.setCustFirstKana(first);
            customer.setCustLastKana(last);
        }
    }

    private void remainningCustInfor(Map<String, String> mappings, Issue issue, Map<String, String> params) {
        if(mappings == null || mappings.isEmpty()) return;
        if(issue == null) return;

        ///////// Create customer object
        Customer cust = createCustomer(mappings, issue, params, false);
        if(issue != null) {
            if("remove".equals(params.get("cust_import_type"))){
                issue.getCustomerList().clear();
            }else if("keep".equals(params.get("cust_import_type"))){
                for (Iterator<Customer> iterator = issue.getCustomerList().iterator(); iterator.hasNext();) {
                    Customer o = iterator.next();
                    try{
                        if (!StringUtils.isEmpty(cust.getCustFullHira()) && !StringUtils.isEmpty(cust.getCustAddress()) &&
                                (o.getCustFullHira().equals(cust.getCustFullHira()) || o.getCustFullKana().equals(cust.getCustFullHira())) 
                                && (o.getCustAddress().equals(cust.getCustAddress()) || o.getCustAddress().equals(cust.getCustAddress())) 
                                && o.isSameCustTarget(cust)) {
                            iterator.remove();
                        }
                    }catch(NullPointerException e){
                        // No thing TODO
                    }
                }
            }
        }
        // uploadCustomer.getCustTargetInfoList().clear();
        if(issue.getCustomerList().isEmpty()){
            issue.getCustomerList().add(cust);
        }else{
            issue.getCustomerList().set(0, cust);
        }
    }
    
    public Integer getMemberIdByName(String name) {
        MemberService memberService = issueController.getMemberService();
        String queryMemberId = " m.memberNameFirst = '" + name +"'";
        Integer memberId = null;
        List<Member> results = memberService.find(null, null, null, null, queryMemberId);
        if (null != results && !results.isEmpty()) {
            memberId = results.get(0).getMemberId();
        }
        return memberId;
    }
    
    private void buildTargetInfo(String targetInfo, List<CustTargetInfo> infos, String type,Company company, Customer cust) {
        String[] targetInfoSplits = targetInfo.split(",");
        for (String targetInfoSplit : targetInfoSplits) {
            if(StringUtils.isEmpty(targetInfoSplit)) continue;
            CustTargetInfo target = new CustTargetInfo();
            target.setCompany(company);
            target.setCustomer(cust);
            target.setCustTargetData(targetInfoSplit);
            if (null != type) switch (type) {
                case "PHONE":
                    target.setCustFlagType(TARGET.TEL);
                    break;
                case "MOBILE":
                    target.setCustFlagType(TARGET.MOBILE);
                    break;
                case "FAX":
                    target.setCustFlagType(TARGET.FAX);
                    break;
                case "EMAIL":
                    target.setCustFlagType(TARGET.MAIL);
                    break;
                default:
                    break;
            }
            infos.add(target);
        }
    }
    
    private boolean phoneFaxValidator(String value) {
        if (value.isEmpty()) {
            return true;
        }
        String[] phoneFaxSplit = value.split(",");
        BaseValidator validator = new PhoneFaxValidator();
        for (String phoneFax : phoneFaxSplit) {
            if(!validator.doValidate(phoneFax)) {
                return false;
            }
        }
        return true;
    }
    
    private boolean emailValidator(String value) {
        if (value.isEmpty()) {
            return true;
        }
        String[] emailSplits = value.split(",");
        BaseValidator validator = new EmailValidator();
        for (String email : emailSplits) {
            if(!validator.doValidate(email)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void parseCustomer(Customer cust, Map<String, String> mappings, Map<String, String> params) throws Exception {
        this.issueController = JsfUtil.getManagedBean("issueController", IssueController.class);
        cust = createCustomer(mappings, null, params, true);
    }

    private Customer createCustomer(Map<String, String> mappings, Issue issue, Map<String, String> params, boolean parseCust) {
        String custCooperationName = StringUtils.defaultString(mappings.get(COLS.COOPERATION), "");
        String custSexName = StringUtils.defaultString(mappings.get(COLS.SEX), "");
        String custAgeName = StringUtils.defaultString(mappings.get(COLS.AGE), "");
        String custSpecialName = StringUtils.defaultString(mappings.get(COLS.SPECIAL), "");
        
        MenteItem custCooperation = null, custSex = null, custAge = null, custSpecial = null;
        List<SelectItem> checkMenteList = new ArrayList<>();
        try{
            checkMenteList.addAll(issueController.getSelect().get(COLS.COOPERATION));
        }catch(NullPointerException npe){}
        try{
            checkMenteList.addAll(issueController.getSelect().get(COLS.SPECIAL));
        }catch(NullPointerException npe){}
        try{
            checkMenteList.addAll(issueController.getSelect().get(COLS.SEX));
        }catch(NullPointerException npe){}
        try{
            checkMenteList.addAll(issueController.getSelect().get(COLS.AGE));
        }catch(NullPointerException npe){}
        
        for(SelectItem si : checkMenteList){
            Object o = si.getValue();
            if(o instanceof MenteItem){
                MenteItem tmp = (MenteItem)o;
                if(custCooperationName.equals(tmp.getItemViewData(issueController.getLocaleController().getLocale()))){
                    custCooperation = tmp;
                } else if(custSexName.equals(tmp.getItemViewData(issueController.getLocaleController().getLocale()))){
                    custSex = tmp;
                } else if(custAgeName.equals(tmp.getItemViewData(issueController.getLocaleController().getLocale()))){
                    custAge = tmp;
                } else if(custSpecialName.equals(tmp.getItemViewData(issueController.getLocaleController().getLocale()))){
                    custSpecial = tmp;
                }
            }
        }

        String post = mappings.get(ALLOW_SEARCH_COL.CUSTOMER.cust_post.name());
        String custCityStr = mappings.get(ALLOW_SEARCH_COL.CUSTOMER.cust_city.name());
        Prefecture city = null;
        if ( !StringUtils.isEmpty(custCityStr) ) {
            for( Prefecture c : issueController.getPrefectures()){
                if(c.getPrefectureName().equals(custCityStr)){
                    city = c;
                    break;
                }
            }
        }
        String custAddress = mappings.get(ALLOW_SEARCH_COL.CUSTOMER.cust_address.name());
        String custAddressKana = mappings.get(ALLOW_SEARCH_COL.CUSTOMER.cust_address_kana.name());
        String memo = mappings.get(ALLOW_SEARCH_COL.CUSTOMER.cust_memo.name());
        String createdTimeStr = mappings.get("cust_created_time");
        createdTimeStr = StringUtil.nl2br(createdTimeStr, "");
        Date createdTime = null;
        if(!StringUtils.isEmpty(createdTimeStr)){
            createdTime = DateUtil.isDate(createdTimeStr, "yyyy-MM-dd HH:mm:ss");
        }
        String createdName = mappings.get("cust_created_name");
        createdName = StringUtil.nl2br(createdName, "");
        Integer createdId = null;
        if(!StringUtils.isEmpty(createdName)){
            createdId = getMemberIdByName(createdName);
        }
        
        String updatedTimeStr = mappings.get("cust_updated_time");
        updatedTimeStr = StringUtil.nl2br(updatedTimeStr, "");
        Date updatedTime = null;
        if(!StringUtils.isEmpty(updatedTimeStr)){
           updatedTime = DateUtil.isDate(updatedTimeStr,"yyyy-MM-dd HH:mm:ss");
        }
        String updatedName = mappings.get("cust_updated_name");
        updatedName = StringUtil.nl2br(updatedName, "");
        Integer updatedId = null;
        if(!StringUtils.isEmpty(updatedName)){
            updatedId = getMemberIdByName(updatedName);
        }
        String custTel = mappings.get(ALLOW_SEARCH_COL.CUSTOMER.cust_tel.name());
        custTel = StringUtil.nl2br(custTel, "");
        String custMobile = mappings.get(ALLOW_SEARCH_COL.CUSTOMER.cust_mobile.name());
        custMobile = StringUtil.nl2br(custMobile, "");
        String custFax = mappings.get("cust_fax");
        custFax = StringUtil.nl2br(custFax, "");
        String custMail = mappings.get(ALLOW_SEARCH_COL.CUSTOMER.cust_mail.name());
        custMail = StringUtil.nl2br(custMail, "");

        IssueCustomerService customerService = issueController.getIssueCustomerService();
        ///////// Create customer object
        Customer cust = new Customer(isLogin.getMember(), null);
        remainingCustHiraKana(mappings, cust, ALLOW_SEARCH_COL.CUSTOMER.cust_name_hira.name());
        remainingCustHiraKana(mappings, cust, ALLOW_SEARCH_COL.CUSTOMER.cust_name_kana.name());
        cust.setCustAddress(custAddress);
        
        List<CustTargetInfo> custTels = new ArrayList<>();
        if (!StringUtils.isEmpty(custTel) && phoneFaxValidator(custTel)) {
            buildTargetInfo(custTel, custTels, "PHONE", isLogin.getCompany(), cust);
        }
        List<CustTargetInfo> custMobiles = new ArrayList<>();
        if (!StringUtils.isEmpty(custMobile) && phoneFaxValidator(custMobile)) {
            buildTargetInfo(custMobile, custMobiles, "MOBILE", isLogin.getCompany(), cust);
        }
        List<CustTargetInfo> custFaxs = new ArrayList<>();
        if (!StringUtils.isEmpty(custFax) && phoneFaxValidator(custFax)) {
            buildTargetInfo(custFax, custFaxs, "FAX", isLogin.getCompany(), cust);
        }
        List<CustTargetInfo> custMails = new ArrayList<>();
        if (!StringUtils.isEmpty(custMail) && emailValidator(custMail)) {
            buildTargetInfo(custMail, custMails, "EMAIL", isLogin.getCompany(), cust);
        }
        List<CustTargetInfo> customerTargetInfos = new ArrayList<>();
        customerTargetInfos.addAll(custTels);
        customerTargetInfos.addAll(custMobiles);
        customerTargetInfos.addAll(custFaxs);
        customerTargetInfos.addAll(custMails);
        cust.setCustTargetInfoList(customerTargetInfos);

        if(parseCust && (custSpecial == null || custSpecial.getItemId() == null)) {
            List<MenteItem> list = issueController.getMenteService().findByName(COLS.SPECIAL, isLogin.getCompanyId());
            if(list != null && list.size() > 0) {
                custSpecial = list.get(0);
            } else {
                    MenteItem item = new MenteItem(isLogin.getMember(), COLS.SPECIAL, 1, null);
                    MenteOptionDataValue lang = new MenteOptionDataValue(isLogin.getLanguage(), "消費者", item);
                    lang.setCompany(isLogin.getCompany());
                    lang.setCreatorId(isLogin.getUserId());
                    lang.setCreatedTime(new Date());
                    lang.setUpdatedId(isLogin.getUserId());
                    lang.setUpdatedTime(new Date());
                    lang.setMenteItem(item);
                    item.getLangs().add(lang);
                try {
                    issueController.getMenteService().create(item);
                } catch (Exception ex) {
                    Logger.getLogger(IssueCustParse.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    custSpecial = item;
                    if(issueController.getSelect().get(COLS.SPECIAL) == null) {
                        Map<String, List<SelectItem>> select = new HashMap<>();
                        List<SelectItem> items = new ArrayList<>();
                        items.add(new SelectItem(item.getItemId(), item.getItemViewData(isLogin.getLanguage())));
                        select.put(COLS.SPECIAL, items);
                    } else {
                        issueController.getSelect().get(COLS.SPECIAL).add(new SelectItem(item.getItemId(), item.getItemViewData(isLogin.getLanguage())));
                    }
                }
            }
        }
        String custCode = mappings.get(ALLOW_SEARCH_COL.CUSTOMER.cust_code.name());
        cust.setCustCode(custCode);
        cust.setCustCooperationId(custCooperation);
        cust.setCustSpecialId(custSpecial);
        cust.setCustPost(post);
        cust.setCustCity(city);
        cust.setCustAddress(custAddress);
        cust.setCustAddressKana(custAddressKana);
        cust.setCustSexId(custSex);
        cust.setCustAgeId(custAge);
        cust.setCustMemo(memo);
        cust.setCompany(isLogin.getCompany());

        List<Customer> nearSameCustomerList = customerService.findNearSameCustomer(
                isLogin.getCompanyId()
                ,custCode
                ,cust.getCustFullHira()
                ,cust.getCustFullKana()
                ,cust.getCustAddress()
                ,custCityStr);
        for(Customer c : nearSameCustomerList){
            if(c.isSameCustTarget(c)){
                cust.setCustId(c.getCustId());
                cust.setCreatorId(c.getCreatorId());
                cust.setCreatedTime(c.getCreatedTime());
                BeanUtils.copyProperties(cust, c);
                cust = c;
                break;
            }
        }
        
//        cust.setCustCooperationId(custCooperation);
//        cust.setCustSexId(custSex);
//        cust.setCustAgeId(custAge);
//        cust.setCustMemo(memo);
//        cust.setCompany(company);

        if(issue != null) cust.getIssueList().add(issue);
        if(cust.getCustId() == null){
//            cust.setCustPost(post);
//            cust.setCustCity(city);
//            cust.setCustAddress(custAddress);
//            cust.setCustAddressKana(custAddressKana);

            if(createdId != null) cust.setCreatorId(createdId);
            if(updatedId != null) cust.setUpdatedId(updatedId);
            if(createdTime != null) cust.setCreatedTime(createdTime);
            if(updatedTime != null) cust.setUpdatedTime(updatedTime);
            cust.setCustDeleted(Boolean.FALSE);
        }
        return cust;
    }
}
