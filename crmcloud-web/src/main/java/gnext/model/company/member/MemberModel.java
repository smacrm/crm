/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.model.company.member;

import gnext.bean.CompanyTargetInfo;
import gnext.bean.Group;
import gnext.bean.Member;
import gnext.bean.MultipleMemberGroupRel;
import gnext.bean.Prefecture;
import gnext.bean.role.Role;
import gnext.bean.role.SystemUseAuthRel;
import gnext.controller.company.MemberController;
import gnext.model.BaseModel;
import gnext.model.authority.UserModel;
import gnext.multitenancy.service.MultitenancyService;
import gnext.security.SecurityService;
import gnext.service.CompanyTargetInfoService;
import gnext.service.MultipleMemberGroupRelService;
import gnext.service.role.RoleService;
import gnext.service.role.SystemUseAuthRelService;
import gnext.util.DateUtil;
import gnext.util.JsfUtil;
import gnext.util.ResourceUtil;
import gnext.util.StatusUtil;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

/**
 *
 * @author havd
 */
public class MemberModel extends BaseModel {
    private static final Logger LOGGER = LoggerFactory.getLogger(MemberModel.class);
    
    public static final String PASS_ENCODE ="...............";
    @Getter @Setter private String memberPassword="";
    
    @Getter @Setter private Member member;
    private Prefecture prefecture;
    
    @Getter @Setter private String displaymemberGroupRelIds;
    @Getter @Setter Boolean displaymemberGroupRelIdsFlag = true;
    @Getter @Setter private Integer[] memberGroupRelIds;
    
    // hiển thị danh sách tên role đã lựa chọn.
    @Getter @Setter private String displayRoleName;
    @Getter private String viewRoleName;
    private Integer[] roleIds;
    
    private String displayMemberCity;
    private List<CompanyTargetInfo> memberPhones; private List<CompanyTargetInfo> memberPhonesDeleted;
    private List<CompanyTargetInfo> memberMobilePhones; private List<CompanyTargetInfo> memberMobilePhonesDeleted;
    private List<CompanyTargetInfo> memberEmails; private List<CompanyTargetInfo> memberEmailsDeleted;
    
    private Short memberFirewall; private boolean asMemberFirewallFlag;
    private Short memberGlobalFlag; private boolean asMemberGlobalFlag;
    
    @Getter private Integer oldCompany = null;
    
    public MemberModel() {
        memberPhones = new ArrayList<>();
        memberMobilePhones = new ArrayList<>();
        memberEmails = new ArrayList<>();
    }
    
    public MemberModel(final Member member) {
        if(member.getMemberId() != null && member.getMemberId() > 0) oldCompany = member.getGroup().getCompany().getCompanyId();
        this.member = member;
        memberPhones = new ArrayList<>();
        memberMobilePhones = new ArrayList<>();
        memberEmails = new ArrayList<>();
        displaymemberGroupRelIds = JsfUtil.getResource().message(ResourceUtil.BUNDLE_COMPANY, "label.member.manager.group", "");
    }
    
    public void updateExtraInfors(MemberController memberController) {
        if(member == null || member.getMemberId() == null) return;
        
        String lang = UserModel.getLogined().getLanguage();
        Integer currentCompanyId = this.member.getGroup().getCompany().getCompanyId();
        
        this.setMemberFirewall(member.getMemberFirewall());
        this.setMemberGlobalFlag(member.getMemberGlobalFlag());
        
        if(member.getMemberCity() != null)
            prefecture = memberController.getPrefectureService().findByPrefectureCode(lang, String.valueOf(member.getMemberCity()));
        
        SystemUseAuthRelService authRelService = memberController.getSystemUseAuthRelService();
        List<SystemUseAuthRel> systemUseAuthRels = authRelService.findByRoleFlag(currentCompanyId, member.getMemberId(), SystemUseAuthRel.MEMBER_FLAG, Role.ROLE_UN_HIDDEN);
        if (systemUseAuthRels != null && !systemUseAuthRels.isEmpty()) {
            roleIds = new Integer[systemUseAuthRels.size()];
            for (int i = 0; i < systemUseAuthRels.size(); i++) {
                this.roleIds[i] = systemUseAuthRels.get(i).getSystemUseAuthRelPK().getRoleId();
            }
        }
    }
    
    public void updateMemberGroupRelInfos(MemberController memberController) {
        try {
            if(member == null || member.getMemberId() == null) return;

            SecurityService sec = memberController.getSec();
            MultipleMemberGroupRelService multipleMemberGroupRelService = memberController.getMmgrs();
            MultitenancyService multitenancyService = memberController.getMultitenancyService();
            
            List<MultipleMemberGroupRel> memberGroupRels =  null;
            if(sec.isLoggedInOtherCompany(member.getMemberId())) {
                memberGroupRels = multitenancyService.findMultipleMemberGroupRelByMemberIdOnSlave(member.getGroup().getCompanyId(), member.getMemberId());
            } else {
                memberGroupRels = multipleMemberGroupRelService.findByMemberId(member.getMemberId());
            }
            if(memberGroupRels == null || memberGroupRels.isEmpty()) return;

            memberGroupRelIds = new Integer[memberGroupRels.size()];
            for (int i = 0; i < memberGroupRels.size(); i++) {
                this.memberGroupRelIds[i] = memberGroupRels.get(i).getMultipleMemberGroupRelPK().getGroupId();
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public void initInfos(CompanyTargetInfoService companyTargetInfoService) {
        List<CompanyTargetInfo> memberTargetInfosPhone = companyTargetInfoService .find(CompanyTargetInfo.COMPANY_TARGET_MEMBER, member.getMemberId(), CompanyTargetInfo.COMPANY_FLAG_TYPE_PHONE, StatusUtil.UN_DELETED);
        setMemberPhones(memberTargetInfosPhone);

        List<CompanyTargetInfo> memberTargetInfosMobilePhone = companyTargetInfoService .find(CompanyTargetInfo.COMPANY_TARGET_MEMBER, member.getMemberId(), CompanyTargetInfo.COMPANY_FLAG_TYPE_MOBILE, StatusUtil.UN_DELETED);
        setMemberMobilePhones(memberTargetInfosMobilePhone);

        List<CompanyTargetInfo> memberTargetInfosEmail = companyTargetInfoService .find(CompanyTargetInfo.COMPANY_TARGET_MEMBER, member.getMemberId(), CompanyTargetInfo.COMPANY_FLAG_TYPE_EMAIL, StatusUtil.UN_DELETED);
        setMemberEmails(memberTargetInfosEmail);
    }
    
    public void initDeletedInfos() {
        setMemberPhonesDeleted(new ArrayList<>());
        setMemberMobilePhonesDeleted(new ArrayList<>());
        setMemberEmailsDeleted(new ArrayList<>());
    }
    
    public void update(final Member member) {
        BeanUtils.copyProperties(this, member);
    }
    
    public void addEmptyInfos() {
        if (getMemberPhones().isEmpty()) { getMemberPhones().add(new CompanyTargetInfo()); }
        if (getMemberMobilePhones().isEmpty()) { getMemberMobilePhones().add(new CompanyTargetInfo()); }
        if (getMemberEmails().isEmpty()) { getMemberEmails().add(new CompanyTargetInfo()); }
    }
    
    public List<CompanyTargetInfo> getExtraInfo(String type) {
        if ("phone".equals(type)) {
            return this.getMemberPhones();
        } else if ("email".equals(type)) {
            return this.getMemberEmails();
        } else if ("mobilephone".equals(type)) {
            return this.getMemberMobilePhones();
        }
        return new ArrayList<>();
    }

    public List<CompanyTargetInfo> getExtraInfoMarkDeleted(String type) {
        if ("phone".equals(type)) {
            return this.getMemberPhonesDeleted();
        } else if ("email".equals(type)) {
            return this.getMemberEmailsDeleted();
        } else if ("mobilephone".equals(type)) {
            return this.getMemberMobilePhonesDeleted();
        }
        return new ArrayList<>();
    }
    
    public String getDisplayCreateTime() {
        try {
            return DateUtil.getDateToString(member.getCreatedTime(), DateUtil.PATTERN_JP_SLASH);
        } catch (Exception e) {
            return StringUtils.EMPTY;
        }
    }

    public String getDisplayUpdateTime() {
        try {
            return DateUtil.getDateToString(member.getUpdatedTime(), DateUtil.PATTERN_JP_SLASH);
        } catch (Exception e) {
            return StringUtils.EMPTY;
        }
    }
    
    public void displayRole(MemberController memberController){
        this.populateDisplayRoleName(memberController);
        this.viewRoleName = _BuildRoleName(memberController);
    }
    
    public void displayMemberGroupRel(MemberController memberController){
        this.populateDisplayMemberGroupRel(memberController);
    }
    
    public void populateDisplayRoleName(MemberController memberController) {
        this.displayRoleName = _BuildRoleName(memberController);
        if (StringUtils.isEmpty(this.displayRoleName)) {
            displayRoleName = "ロール";
        }
    }
    
    public void populateDisplayMemberGroupRel(MemberController memberController) {
        this.displaymemberGroupRelIds = _BuildMemberGroupRel(memberController);
        if(StringUtils.isEmpty(this.displaymemberGroupRelIds)){
            displaymemberGroupRelIdsFlag = false;
            displaymemberGroupRelIds = JsfUtil.getResource().message(ResourceUtil.BUNDLE_COMPANY, "label.member.manager.group", "");
        }
    }

    private String _BuildRoleName(MemberController memberController) {
        StringBuilder sb = new StringBuilder();
        if (roleIds != null && roleIds.length > 0) {
            for (int i = 0; i < roleIds.length; i++) {
                Role role = memberController.getRoleService().find(roleIds[i]);
                sb.append(role.getRoleName()).append(",");
            }
            if (sb.indexOf(",") > 0) sb.deleteCharAt(sb.lastIndexOf(","));
        }
        return sb.toString();
    }
    
    private String _BuildMemberGroupRel(MemberController memberController) {
        StringBuilder sb = new StringBuilder();
        if(memberGroupRelIds != null && memberGroupRelIds.length > 0 ){
            for (Integer memberGroupRelId : memberGroupRelIds) {
                Group mMemberGroupRel = memberController.getGroupService().findByGroupId(memberGroupRelId);
                sb.append(mMemberGroupRel.getGroupName()).append(",");
            }
        }
        if(sb.indexOf(",") > 0) sb.deleteCharAt(sb.lastIndexOf(","));
        return sb.toString();
    }
    
    /**
     * Hàm kiểm tra Member có thay đổi công ty không(trường hợp cho Super-Admin)
     * @return 
     */
    public boolean isChangeCompany() {
        if(oldCompany != null && !oldCompany.equals(this.member.getGroup().getCompany().getCompanyId()))
            return true;
        return false;
    }
    
    public boolean isDeleted() { return this.member.getMemberDeleted() == 1; }
    
    public List<CompanyTargetInfo> getMemberPhones() { return memberPhones; }
    public void setMemberPhones(List<CompanyTargetInfo> memberPhones) { this.memberPhones = memberPhones; }

    public List<CompanyTargetInfo> getMemberMobilePhones() { return memberMobilePhones; }
    public void setMemberMobilePhones(List<CompanyTargetInfo> memberMobilePhones) { this.memberMobilePhones = memberMobilePhones; }

    public List<CompanyTargetInfo> getMemberEmails() { return memberEmails; }
    public void setMemberEmails(List<CompanyTargetInfo> memberEmails) { this.memberEmails = memberEmails; }

    public String getDisplayMemberCity() { return this.displayMemberCity; }
    public void setDisplayMemberCity(String displayMemberCity) { this.displayMemberCity = displayMemberCity; }

    public Integer[] getRoleIds() { return roleIds; }
    public void setRoleIds(Integer[] roleIds) { this.roleIds = roleIds; }

    public List<CompanyTargetInfo> getMemberPhonesDeleted() { return memberPhonesDeleted; }
    public void setMemberPhonesDeleted(List<CompanyTargetInfo> memberPhonesDeleted) { this.memberPhonesDeleted = memberPhonesDeleted; }

    public List<CompanyTargetInfo> getMemberMobilePhonesDeleted() { return memberMobilePhonesDeleted; }
    public void setMemberMobilePhonesDeleted(List<CompanyTargetInfo> memberMobilePhonesDeleted) { this.memberMobilePhonesDeleted = memberMobilePhonesDeleted; }

    public List<CompanyTargetInfo> getMemberEmailsDeleted() { return memberEmailsDeleted; }
    public void setMemberEmailsDeleted(List<CompanyTargetInfo> memberEmailsDeleted) { this.memberEmailsDeleted = memberEmailsDeleted; }

    public boolean isAsMemberFirewallFlag() { return asMemberFirewallFlag; }
    public void setAsMemberFirewallFlag(boolean asMemberFirewallFlag) {
        this.asMemberFirewallFlag = asMemberFirewallFlag;
        if(this.asMemberFirewallFlag){
            this.memberFirewall = (short)1;
        }else{
            this.memberFirewall = (short)0;
        }
    }

    public boolean isAsMemberGlobalFlag() {
        if (memberGlobalFlag == null) {
            return false;
        }
        return memberGlobalFlag == 1;
    }
    public void setAsMemberGlobalFlag(boolean asMemberGlobalFlag) {
        this.asMemberGlobalFlag = asMemberGlobalFlag;
        if(this.asMemberGlobalFlag){
            this.memberGlobalFlag = (short)1;
        }else{
            this.memberGlobalFlag = (short)0;
        }
    }

    public Short getMemberFirewall() { return memberFirewall; }
    public void setMemberFirewall(Short memberFirewall) {
        this.memberFirewall = memberFirewall;
        if(memberFirewall!=null&&this.memberFirewall==1){
            this.asMemberFirewallFlag = true;
        }else{
            this.asMemberFirewallFlag = false;
        }
    }

    public Short getMemberGlobalFlag() { return memberGlobalFlag; }
    public void setMemberGlobalFlag(Short memberGlobalFlag) {
        this.memberGlobalFlag = memberGlobalFlag;
        if(memberGlobalFlag!=null&&this.memberGlobalFlag==1){
            this.asMemberGlobalFlag = true;
        }else{
            this.asMemberGlobalFlag = false;
        }
    }

    public Prefecture getPrefecture() { return prefecture; }
    public void setPrefecture(Prefecture prefecture) { this.prefecture = prefecture; }
}