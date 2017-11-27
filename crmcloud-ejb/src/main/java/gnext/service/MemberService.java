/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.service;

import gnext.bean.Company;
import gnext.bean.CompanyTargetInfo;
import gnext.bean.CompanyTargetInfoPK;
import gnext.bean.Group;
import gnext.bean.Member;
import gnext.bean.MemberAuth;
import java.util.List;
import java.util.Map;
import javax.ejb.Local;
import javax.persistence.EntityManager;


/**
 *
 * @author hungpham
 */
@Local
public interface MemberService extends EntityService<Member>{
    public Member findByUsername(String username);
    public Member findByUsername(String username, Integer companyId);
    
    public List<Member> findByCompanyId(int companyId);
    public List<Member> findByGroupId(final int groupId);
    
    public List<Member> findMailMemberList(final int groupId, final int memberId);
    
    public Member findByMemberId(final int memberId);
    public Member findByMemberName(String memberFullName, Integer companyId);
    
    public List<MemberAuth> getRoleListV2(int memberId, int companyId) throws Exception;
    public List<MemberAuth> getRoleList(int memberId, int companyId);
    public Member findByLoginIdPassword(final String memberLoginId, final String memberPassword);

    public List<Member> findByGroupList(List<Group> groupList, short isDeleted, List<Integer> memberIds);
    
    public List<Member> find(Integer first, Integer pageSize, String sortField, String sortOrder, String where);
    public int total(final String where);

    public Member edit(Company logined, Member m, Member memModel, byte[] bytes,
            List<CompanyTargetInfo> phones, List<CompanyTargetInfo> phoneDeleted,
            List<CompanyTargetInfo> mobilePhones, List<CompanyTargetInfo> mobilePhonesDelete,
            List<CompanyTargetInfo> email, List<CompanyTargetInfo> emailDeleted, Integer[] memberGroupRelIds,
            Integer[] roleIds,
            boolean isAsMemberGlobalFlag, List<Company> companyBelongGroup,
            boolean isChangeCompany, Map<Integer, Integer> mapCompanyGroupIds) throws Exception;
    public Member insert(Company logined, Member m, Member memModel, byte[] bytes,
            List<CompanyTargetInfo> phones, List<CompanyTargetInfo> phoneDeleted,
            List<CompanyTargetInfo> mobilePhones, List<CompanyTargetInfo> mobilePhonesDelete,
            List<CompanyTargetInfo> email, List<CompanyTargetInfo> emailDeleted, Integer[] memberGroupRelIds,
            Integer[] roleIds,
            boolean isAsMemberGlobalFlag, List<Company> companyBelongGroup, Map<Integer, Integer> mapCompanyGroupIds)
            throws Exception;

    /**
    * Basic Auth for Company
    * @param loginId
    * @param password
    * @return 
    */
    public boolean basicAuth(String loginId, String password);
    public Object[] findBasicAuth(String loginId);
    public void persitCompanyIdAfterBasicAuth(Integer companyId);
    
    /**
     * Kiem tra dia chi IP cua client co bi han che khong
     * @param globalIP
     * @param basicLoginId
     * @return 
     */
    public boolean isGlobalIpPass(String globalIP, String basicLoginId);
    public boolean isGlobalIpUserPass(String globalIP, String basicLoginId, String loginId);

    public void updateLastLoginTime(Member m) throws Exception;
    public void updateLastLogoutTime(int memberId) throws Exception;

    public String getUserMailFristByUserId(int userId);
    public void _CreateMemberInfo(CompanyTargetInfoPK ctiPk, CompanyTargetInfo cti, Member m, EntityManager em) throws Exception;
    public List<Member> findListByIds(List<Integer> memberIdList);
}