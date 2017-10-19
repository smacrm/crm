/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.model.company;

import gnext.bean.Company;
import gnext.bean.CompanyTargetInfo;
import gnext.bean.CompanyTargetInfoPK;
import gnext.bean.Prefecture;
import gnext.model.BaseModel;
import gnext.service.CompanyTargetInfoService;
import gnext.service.PrefectureService;
import gnext.util.DateUtil;
import gnext.util.SelectUtil;
import gnext.util.StatusUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.faces.model.SelectItem;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author daind
 */
public final class CompanyModel extends BaseModel<Company> {
    private static final long serialVersionUID = 49043424582740219L;

    @Getter @Setter private Company company;
    @Getter @Setter private Integer viewCompanyId;
    @Getter @Setter private String displayCompanyCity;

    @Getter @Setter private List<CompanyTargetInfo> companyPhones;
    @Getter @Setter private List<CompanyTargetInfo> companyPhonesDeleted;

    @Getter @Setter private List<CompanyTargetInfo> companyFaxs;
    @Getter @Setter private List<CompanyTargetInfo> companyFaxsDeleted;

    @Getter @Setter private List<CompanyTargetInfo> companyHomepages;
    @Getter @Setter private List<CompanyTargetInfo> companyHomepagesDeleted;

    @Getter @Setter private List<CompanyTargetInfo> companyEmails;
    @Getter @Setter private List<CompanyTargetInfo> companyEmailsDeleted;
    
    @Getter @Setter private List<CompanyTargetInfo> companyIps;
    @Getter @Setter private List<CompanyTargetInfo> companyIpsDeleted;

    public CompanyModel() {
        company = new Company();
        
        companyPhones = new ArrayList<>();
        companyFaxs = new ArrayList<>();
        companyHomepages = new ArrayList<>();
        companyEmails = new ArrayList<>();
        companyIps = new ArrayList<>();
        
        companyPhonesDeleted = new ArrayList<>();
        companyFaxsDeleted = new ArrayList<>();
        companyHomepagesDeleted = new ArrayList<>();
        companyEmailsDeleted = new ArrayList<>();
        companyIpsDeleted = new ArrayList();
    }

    public CompanyModel(final Company company) {
        this();
        this.company = company;
        this.viewCompanyId = getCompany().getCompanyId();
    }
    
    public void cleanExtraInfo() {
        getCompanyPhones().clear();
        getCompanyFaxs().clear();
        getCompanyEmails().clear();
        getCompanyHomepages().clear();
        getCompanyIps().clear();
        
        getCompanyPhonesDeleted().clear();
        getCompanyFaxsDeleted().clear();
        getCompanyEmailsDeleted().clear();
        getCompanyHomepagesDeleted().clear();
        getCompanyIpsDeleted().clear();;
    }
    
    public void addEmptyInfos() {
        if (getCompanyPhones().isEmpty()) getCompanyPhones().add(new CompanyTargetInfo(new CompanyTargetInfoPK(-1)));
        if (getCompanyFaxs().isEmpty()) getCompanyFaxs().add(new CompanyTargetInfo(new CompanyTargetInfoPK(-1)));
        if (getCompanyEmails().isEmpty()) getCompanyEmails().add(new CompanyTargetInfo(new CompanyTargetInfoPK(-1)));
        if (getCompanyHomepages().isEmpty()) getCompanyHomepages().add(new CompanyTargetInfo(new CompanyTargetInfoPK(-1)));
        if (getCompanyIps().isEmpty()) getCompanyIps().add(new CompanyTargetInfo(new CompanyTargetInfoPK(-1)));
    }
    
    public void initInfos(CompanyTargetInfoService companyTargetInfoService) {
        List<CompanyTargetInfo> companyTargetInfosPhone = companyTargetInfoService
                .find(CompanyTargetInfo.COMPANY_TARGET_COMPANY, getCompany().getCompanyId(),
                        CompanyTargetInfo.COMPANY_FLAG_TYPE_PHONE, StatusUtil.UN_DELETED);
        getCompanyPhones().addAll(companyTargetInfosPhone);

        List<CompanyTargetInfo> companyTargetInfosFax = companyTargetInfoService
                .find(CompanyTargetInfo.COMPANY_TARGET_COMPANY, getCompany().getCompanyId(),
                        CompanyTargetInfo.COMPANY_FLAG_TYPE_FAX, StatusUtil.UN_DELETED);
        getCompanyFaxs().addAll(companyTargetInfosFax);

        List<CompanyTargetInfo> companyTargetInfosEmail = companyTargetInfoService
                .find(CompanyTargetInfo.COMPANY_TARGET_COMPANY, getCompany().getCompanyId(),
                        CompanyTargetInfo.COMPANY_FLAG_TYPE_EMAIL, StatusUtil.UN_DELETED);
        getCompanyEmails().addAll(companyTargetInfosEmail);

        List<CompanyTargetInfo> companyTargetInfosHomepage = companyTargetInfoService
                .find(CompanyTargetInfo.COMPANY_TARGET_COMPANY, getCompany().getCompanyId(),
                        CompanyTargetInfo.COMPANY_FLAG_TYPE_HOMEPAGE, StatusUtil.UN_DELETED);
        getCompanyHomepages().addAll(companyTargetInfosHomepage);
        
        if(!StringUtils.isEmpty(getCompany().getCompanyGlobalIp())) {
            List<String> globalIps = Arrays.asList(StringUtils.split(getCompany().getCompanyGlobalIp(), ","));
            for(String gi : globalIps) {
                CompanyTargetInfo cti = new CompanyTargetInfo(new CompanyTargetInfoPK(0-(getCompanyIps().size() + 1)));
                cti.setCompanyTargetData(gi);
                getCompanyIps().add(cti);
            }
        }
    }

    public void convertExtraInfoToNew() {
        for (CompanyTargetInfo info : getCompanyPhones()) info.setCompanyTargetInfoPK(null);
        for (CompanyTargetInfo info : getCompanyFaxs()) info.setCompanyTargetInfoPK(null);
        for (CompanyTargetInfo info : getCompanyEmails()) info.setCompanyTargetInfoPK(null);
        for (CompanyTargetInfo info : getCompanyHomepages()) info.setCompanyTargetInfoPK(null);
        for (CompanyTargetInfo info : getCompanyIps()) info.setCompanyTargetInfoPK(null);
        
    }
    
    public void updateCity(String locale, PrefectureService ps) {
        if (this.getCompany().getCompanyCity() != null) {
            String ccity = String.valueOf(this.getCompany().getCompanyCity());
            Prefecture prefecture = ps.findByPrefectureCode(locale, ccity);
            if (prefecture != null) {
                this.setDisplayCompanyCity(prefecture.getPrefectureName());
            }
        }
    }
    
    public List<CompanyTargetInfo> getExtraInfo(String type) {
        if ("phone".equals(type)) {
            return this.getCompanyPhones();
        } else if ("fax".equals(type)) {
            return this.getCompanyFaxs();
        } else if ("email".equals(type)) {
            return this.getCompanyEmails();
        } else if ("homepage".equals(type)) {
            return this.getCompanyHomepages();
        }  else if ("Ip".equals(type)) {
            return this.getCompanyIps();
        }
        
        return new ArrayList<>();        
    }
    
    public List<CompanyTargetInfo> getExtraInfoMarkDeleted(String type) {
        if ("phone".equals(type)) {
            return this.getCompanyPhonesDeleted();
        } else if ("fax".equals(type)) {
            return this.getCompanyFaxsDeleted();
        } else if ("email".equals(type)) {
            return this.getCompanyEmailsDeleted();
        } else if ("homepage".equals(type)) {
            return this.getCompanyHomepagesDeleted();
        } else if ("Ip".equals(type)) {
            return this.getCompanyIpsDeleted();
        }
        
        return new ArrayList<>();        
    }
    
    public String getBussinessName() {
        if(company.getCompanyBusinessFlag() != null) {
            for(SelectItem item : SelectUtil.getBussiness()) {
                if(Short.parseShort(item.getValue().toString())
                        == company.getCompanyBusinessFlag()) {
                     return item.getLabel();
                }
            }
        }
        return StringUtils.EMPTY;
    }

    public String getDisplayCreateDate() { return DateUtil.getDateToString(getCompany().getCreatedTime(), DateUtil.PATTERN_JP_SLASH); }
    public String getDisplayUpdateDate() { return DateUtil.getDateToString(getCompany().getUpdatedTime(), DateUtil.PATTERN_JP_SLASH); }
    public List<SelectItem> getBusinessTypes() { return SelectUtil.getBussiness(); }
    
    ///////////////////////////////////////////////////////////////
    //////////////////// MARK ANY SHORT FIELDS ////////////////////
    ///////////////////////////////////////////////////////////////
    private boolean companyGlobalLocale;
    public boolean isCompanyGlobalLocale() {
        return StatusUtil.getBoolean(getCompany().getCompanyGlobalLocale());
    }
    public void setCompanyGlobalLocale(boolean companyGlobalLocale) {
        this.companyGlobalLocale = companyGlobalLocale;
        getCompany().setCompanyGlobalLocale(StatusUtil.getShort(companyGlobalLocale));
    }
}
