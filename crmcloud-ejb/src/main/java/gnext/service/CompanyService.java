/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.service;

import gnext.bean.Company;
import gnext.bean.CompanyTargetInfo;
import gnext.bean.Member;
import gnext.bean.UnionCompanyRelModel;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * @author daind
 */
@Local
public interface CompanyService extends EntityService<Company> {
    public List<Company> find(int first, int pageSize, String sortField, String sortOrder, String where);
    public int total(String where);
    
    public List<Company> findWithExecludeIds(final List<Integer> ids);
    public List<Company> findCompanyExecludeGroup(int cid,final List<Integer> ids);
    public List<Company> findByCompanyBelongGroup(int cid);
    
    public Company edit(Company logined, Company c, Member m, byte[] bytes,
            List<CompanyTargetInfo> phones, List<CompanyTargetInfo> phoneDeleted,
            List<CompanyTargetInfo> fax, List<CompanyTargetInfo> faxDeleted,
            List<CompanyTargetInfo> email, List<CompanyTargetInfo> emailDeleted,
            List<CompanyTargetInfo> homepage, List<CompanyTargetInfo> homepageDeleted,
            List<UnionCompanyRelModel> unionCompanyRelModels) throws Exception;
    public Company insert(Company logined, Company c, Member m, byte[] bytes,
            List<CompanyTargetInfo> phones, List<CompanyTargetInfo> phoneDeleted,
            List<CompanyTargetInfo> fax, List<CompanyTargetInfo> faxDeleted,
            List<CompanyTargetInfo> email, List<CompanyTargetInfo> emailDeleted,
            List<CompanyTargetInfo> homepage, List<CompanyTargetInfo> homepageDeleted,
            List<UnionCompanyRelModel> unionCompanyRelModels) throws Exception;
    
    public Boolean checkExistBasicAuth(Integer companyId, String loginId);
    public Company findByCompanyBasicLoginId(final String companyBasicLoginId);
    public Integer saveCompanyBasicAuth(Integer companyId, String loginId, String encodePassword) throws Exception;
    public List<Company> findAllCompanyIsExist();
}
