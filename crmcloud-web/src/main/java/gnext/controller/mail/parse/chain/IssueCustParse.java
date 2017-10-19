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
import gnext.controller.mail.parse.MailCustParse;
import gnext.model.authority.UserModel;
import gnext.service.MemberService;
import gnext.service.issue.IssueCustomerService;
import gnext.util.DateUtil;
import gnext.util.JsfUtil;
import gnext.util.StringUtil;
import gnext.utils.InterfaceUtil;
import gnext.validator.BaseValidator;
import gnext.validator.EmailValidator;
import gnext.validator.PhoneFaxValidator;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.faces.model.SelectItem;
import org.apache.commons.lang3.StringUtils;

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
        remainningCustInfor(mappings,issue, params);
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
        Customer uploadCustomer = new Customer(isLogin.getMember(), null);
        createCustomer(mappings, issue, params, issue.getCompany(), uploadCustomer);

        // uploadCustomer.getCustTargetInfoList().clear();
        if(issue.getCustomerList().isEmpty()){
            issue.getCustomerList().add(uploadCustomer);
        }else{
            issue.getCustomerList().set(0, uploadCustomer);
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
                    target.setCustFlagType(InterfaceUtil.TARGET.TEL);
                    break;
                case "MOBILE":
                    target.setCustFlagType(InterfaceUtil.TARGET.MOBILE);
                    break;
                case "FAX":
                    target.setCustFlagType(InterfaceUtil.TARGET.FAX);
                    break;
                case "EMAIL":
                    target.setCustFlagType(InterfaceUtil.TARGET.MAIL);
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
        createCustomer(mappings, null, params, isLogin.getCompany(), cust);
    }

    private void createCustomer(Map<String, String> mappings, Issue issue, Map<String, String> params, Company company, Customer cust) {
        String custCooperationName = StringUtils.defaultString(mappings.get("cust_cooperation_id"), "");
        String custSexName = StringUtils.defaultString(mappings.get("cust_sex_id"), "");
        String custAgeName = StringUtils.defaultString(mappings.get("cust_age_id"), "");
        
        MenteItem custCooperation = null, custSex = null, custAge = null;
        List<SelectItem> checkMenteList = new ArrayList<>();
        try{
            checkMenteList.addAll(issueController.getSelect().get("cust_cooperation_id"));
        }catch(NullPointerException npe){}
        try{
            checkMenteList.addAll(issueController.getSelect().get("cust_sex_id"));
        }catch(NullPointerException npe){}
        try{
            checkMenteList.addAll(issueController.getSelect().get("cust_age_id"));
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
                }
            }
        }

        String post = mappings.get("cust_post");
        String custCityStr = mappings.get("cust_city");
        Prefecture city = null;
        if ( !StringUtils.isEmpty(custCityStr) ) {
            for( Prefecture c : issueController.getPrefectures()){
                if(c.getPrefectureName().equals(custCityStr)){
                    city = c;
                    break;
                }
            }
        }
        String custAddress = mappings.get("cust_address");
        String custAddressKana = mappings.get("cust_address_kana");
        String memo = mappings.get("cust_memo");
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
        String custTel = mappings.get("cust_tel");
        custTel = StringUtil.nl2br(custTel, "");
        String custMobile = mappings.get("cust_mobile");
        custMobile = StringUtil.nl2br(custMobile, "");
        String custFax = mappings.get("cust_fax");
        custFax = StringUtil.nl2br(custFax, "");
        String custMail = mappings.get("cust_mail");
        custMail = StringUtil.nl2br(custMail, "");

        IssueCustomerService customerService = issueController.getIssueCustomerService();
        ///////// Create customer object
//        Customer cust = new Customer();
        remainingCustHiraKana(mappings, cust, "cust_name_hira");
        remainingCustHiraKana(mappings, cust, "cust_name_kana");
        cust.setCustAddress(custAddress);
        
        List<CustTargetInfo> custTels = new ArrayList<>();
        if (!StringUtils.isEmpty(custTel) && phoneFaxValidator(custTel)) {
            buildTargetInfo(custTel, custTels, "PHONE", company, cust);
        }
        List<CustTargetInfo> custMobiles = new ArrayList<>();
        if (!StringUtils.isEmpty(custMobile) && phoneFaxValidator(custMobile)) {
            buildTargetInfo(custMobile, custMobiles, "MOBILE", company, cust);
        }
        List<CustTargetInfo> custFaxs = new ArrayList<>();
        if (!StringUtils.isEmpty(custFax) && phoneFaxValidator(custFax)) {
            buildTargetInfo(custFax, custFaxs, "FAX", company, cust);
        }
        List<CustTargetInfo> custMails = new ArrayList<>();
        if (!StringUtils.isEmpty(custMail) && emailValidator(custMail)) {
            buildTargetInfo(custMail, custMails, "EMAIL", company, cust);
        }
        List<CustTargetInfo> customerTargetInfos = new ArrayList<>();
        customerTargetInfos.addAll(custTels);
        customerTargetInfos.addAll(custMobiles);
        customerTargetInfos.addAll(custFaxs);
        customerTargetInfos.addAll(custMails);
        cust.setCustTargetInfoList(customerTargetInfos);

        List<Customer> nearSameCustomerList = customerService.findNearSameCustomer(isLogin.getCompanyId()
                ,cust.getCustFullHira()
                ,cust.getCustAddress()
                ,custCityStr);
        for(Customer c : nearSameCustomerList){
            if(c.isSameCustTarget(c)){
                cust = c;
                break;
            }
        }
        
        cust.setCustCooperationId(custCooperation);
        cust.setCustSexId(custSex);
        cust.setCustAgeId(custAge);
        cust.setCustPost(post);
        cust.setCustMemo(memo);
        cust.setCompany(company);

        if(issue != null) cust.getIssueList().add(issue);
        if(cust.getCustId() == null){
            cust.setCustCity(city);
            cust.setCustAddress(custAddress);
            cust.setCustAddressKana(custAddressKana);

            if(createdId != null) cust.setCreatorId(createdId);
            if(updatedId != null) cust.setUpdatedId(updatedId);
            if(createdTime != null) cust.setCreatedTime(createdTime);
            if(updatedTime != null) cust.setUpdatedTime(updatedTime);
            cust.setCustDeleted(Boolean.FALSE);
        }

        if(issue != null) {
            if("remove".equals(params.get("cust_import_type"))){
                issue.getCustomerList().clear();
            }else if("keep".equals(params.get("cust_import_type"))){
                for (Iterator<Customer> iterator = issue.getCustomerList().iterator(); iterator.hasNext();) {
                    Customer o = iterator.next();
                    try{
                        if ( !StringUtils.isEmpty(cust.getCustFullHira()) && !StringUtils.isEmpty(cust.getCustAddress()) &&
                                ( o.getCustFullHira().equals(cust.getCustFullHira()) || o.getCustFullKana().equals(cust.getCustFullHira()) ) 
                                && ( o.getCustAddress().equals(cust.getCustAddress()) || o.getCustAddress().equals(cust.getCustAddress()) ) 
                                && o.isSameCustTarget(cust) ) {
                            iterator.remove();
                        }
                    }catch(NullPointerException e){
                        // No thing TODO
                    }
                }
            }
        }
    }
}
