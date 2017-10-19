/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.service.impl;

import gnext.bean.Company;
import gnext.bean.CompanyTargetInfo;
import gnext.bean.Group;
import gnext.bean.Member;
import gnext.multitenancy.ProxyMasterEntityManager;
import gnext.service.CompanyTargetInfoService;
import gnext.service.GroupService;
import gnext.service.MemberImportService;
import gnext.service.MemberService;
import gnext.utils.EncoderUtil;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import gnext.utils.JPAUtils;
import javax.persistence.EntityTransaction;

/**
 *
 * @author tungdt
 */
@Stateless
public class MemberImportServiceImpl extends AbstractService<Member> implements MemberImportService {
    private final Logger LOGGER = LoggerFactory.getLogger(MemberImportServiceImpl.class);

    @EJB private GroupService groupService;
    @Inject ProxyMasterEntityManager masterEntityManager;
    @Override protected EntityManager getEntityManager() {
        return masterEntityManager.getEntityManager();
    }
    
    @EJB private CompanyTargetInfoService companyTargetInfoService;
    @EJB private MemberService memberService;

    public MemberImportServiceImpl() { super(Member.class); }

    @Override
    public void batchUpdate(Map<Member, List<CompanyTargetInfo>> importData, Member userModel) throws Exception {
        EntityManager em_master = null;
        EntityTransaction tx_master = null;
        try {
            em_master = masterEntityManager.getEntityManager();
            tx_master = beginTransaction(em_master);
            
            for (Member member : importData.keySet()) {
                if (member.getGroup() == null) { // check group is exist if not create group
                    int listGroupNameLength = member.getListGroupName().size();
                    int currentGroupId = -1;
                    for (int j = 1; j <= listGroupNameLength; j++) {
                        StringBuilder mSbGroupName = new StringBuilder();
                        String mPrefix = "";
                        String mGroupName = member.getListGroupName().get(j - 1);
                        for (int k = 0; k < j; k++) {
                            mSbGroupName.append(mPrefix);
                            mSbGroupName.append(member.getListGroupName().get(k));
                            mPrefix = ",";
                        }
                        List<Group> mListGroup = groupService.findByGroupName(mGroupName, userModel.getMemberId());
                        Group mGroup = getMemberGroup(mListGroup, mSbGroupName.toString());
                        if (mGroup == null) {
                            mGroup = new Group();
                            mGroup.setGroupName(mGroupName);
                            if (currentGroupId > 0) {
                                Group parentGroup = new Group();
                                parentGroup.setGroupId(currentGroupId);
                                mGroup.setParent(parentGroup);
                            }
                            mGroup.setCompany(new Company(userModel.getGroup().getCompany().getCompanyId()));
                            Group saved = groupService.insert(mGroup, userModel, null);
                            currentGroupId = saved.getGroupId();
                            member.setGroup(saved);
                        } else {
                            currentGroupId = mGroup.getGroupId();
                            member.setGroup(mGroup);
                        }
                    }
                }
                EncoderUtil encoder = EncoderUtil.getPassEncoder();
                
                if (!_CheckMemberIsExist(member.getMemberLoginId(), userModel.getMemberId(), em_master)) { // case insert
                    if (member.getMemberPassword().isEmpty()) {
                        String rawMemberPassword = "Admin" + userModel.getGroup().getCompany().getCompanyId();
                        String memberPassword = encoder.encode(rawMemberPassword);
                        member.setMemberPassword(memberPassword);
                    } else {
                        String rawMemberPassword = member.getMemberPassword();
                        String memberPassword = encoder.encode(rawMemberPassword);
                        member.setMemberPassword(memberPassword);
                    }
                    Member saved = JPAUtils.create(member, em_master, false);
                    List<CompanyTargetInfo> ctis = importData.get(member);
                    for (CompanyTargetInfo companyTargetInfo : ctis) {
                        if (companyTargetInfo.getCompanyTargetInfoPK() != null) {
                            companyTargetInfo.getCompanyTargetInfoPK().setCompanyTargetId(saved.getMemberId());
                            memberService._CreateMemberInfo(companyTargetInfo.getCompanyTargetInfoPK(), companyTargetInfo, saved, em_master);
                        }
                    }
                } // case edit
                else {
                    Member memberToEdit = findByUsername(member.getMemberLoginId(), userModel.getMemberId(), em_master);
                    List<CompanyTargetInfo> memberTargetInfos = companyTargetInfoService.find(CompanyTargetInfo.COMPANY_TARGET_MEMBER, memberToEdit.getMemberId(), (short) 0);
                    for (CompanyTargetInfo companyTargetInfo : memberTargetInfos) {
                        companyTargetInfo.setCompanyTargetDeleted((short) 1);
                        JPAUtils.edit(companyTargetInfo, em_master, false);
                    }
                    List<CompanyTargetInfo> ctis = importData.get(member);
                    member.setMemberId(memberToEdit.getMemberId());
                    if (member.getMemberPassword().isEmpty()) {
                        member.setMemberPassword(memberToEdit.getMemberPassword());
                    } else {
                        String rawMemberPassword = member.getMemberPassword();
                        String memberPassword = encoder.encode(rawMemberPassword);
                        member.setMemberPassword(memberPassword);
                    }
                    Member edited = JPAUtils.edit(member, em_master, false);
                    for (CompanyTargetInfo companyTargetInfo : ctis) {
                        if (companyTargetInfo.getCompanyTargetInfoPK() != null) {
                            companyTargetInfo.getCompanyTargetInfoPK().setCompanyTargetId(edited.getMemberId());
                            memberService._CreateMemberInfo(companyTargetInfo.getCompanyTargetInfoPK(), companyTargetInfo, edited, em_master);
                        }
                    }

                }
            }
            commitAndCloseTransaction(tx_master);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            rollbackAndCloseTransaction(tx_master);
            throw e;
        } finally {
            JPAUtils.release(em_master, true);
        }
    }
    
    private Member findByUsername(String username, Integer companyId, EntityManager em_master) {
        try{
            String queryStr = "SELECT m FROM Member m WHERE m.memberLoginId = :username AND m.group.company.companyId = :companyId";
            Query query = em_master.createQuery(queryStr,Member.class)
                                    .setParameter("username", username)
                                    .setParameter("companyId", companyId);
            List<Member> members = query.getResultList();
             if(members != null && !members.isEmpty()) return members.get(0);
        }catch(Exception ex){
            LOGGER.error(ex.getMessage(), ex);
        }
        return null;
    }
    
    private boolean _CheckMemberIsExist(String memberLoginId, Integer companyId, EntityManager em_master){
        if(findByUsername(memberLoginId, companyId, em_master) != null){
            return true;
        }
        return false;
    }

    @Override
    public Group getMemberGroup(List<Group> groups, String strGroupName) {
        for (Group group : groups) {
            Stack<String> groupName = new Stack<>();
            groupName.add(group.getGroupName());
            groupName = getGroupParentName(group, groupName);
            int groupNameSize = groupName.size();
            String prefix = "";
            StringBuilder sbGroupName = new StringBuilder();
            for (int i = 0; i < groupNameSize; i++) {
                sbGroupName.append(prefix);
                prefix = ",";
                sbGroupName.append(groupName.pop());
            }
            if (strGroupName.equals(sbGroupName.toString())) {
                return group;
            }
        }
        return null;
    }
    
    private Stack<String> getGroupParentName(Group group, Stack<String> listGroupParent) {
        if (group.getParent() != null) {
            listGroupParent.add(String.valueOf(group.getParent().getGroupName()));
            group = group.getParent();
            return getGroupParentName(group, listGroupParent);
        } else {
            return listGroupParent;
        }
    }
}
