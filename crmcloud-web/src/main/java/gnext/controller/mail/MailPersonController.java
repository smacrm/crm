/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.controller.mail;

import gnext.bean.Group;
import gnext.bean.Member;
import gnext.bean.mail.MailFolder;
import gnext.bean.mail.MailPerson;
import gnext.controller.AbstractController;
import gnext.controller.common.LayoutController;
import gnext.model.authority.UserModel;
import gnext.model.mail.MailFolderModel;
import gnext.model.search.SearchFilter;
import gnext.security.annotation.SecureMethod;
import gnext.security.annotation.SecurePage;
import gnext.service.GroupService;
import gnext.service.MemberService;
import gnext.service.mail.MailFolderService;
import gnext.service.mail.MailPersonService;
import gnext.util.JsfUtil;
import gnext.util.ResourceUtil;
import gnext.util.StatusUtil;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.event.TransferEvent;
import org.primefaces.model.DualListModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tungdt
 */
@ManagedBean(name = "mailPersonController", eager = true)
@ViewScoped()
@SecurePage(module = SecurePage.Module.MAIL, require = true)
public class MailPersonController extends AbstractController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MailPersonController.class);
    
    @EJB private GroupService groupService;
    @EJB private MemberService memberService;
    @EJB private MailPersonService mailPersonService;
    @EJB private MailFolderService mailFolderService;
    
    @ManagedProperty(value = "#{layout}")
    @Getter @Setter private LayoutController layout;
    @Getter @Setter private List<Group> groups;
    @Getter @Setter private DualListModel<Member> members;
    @Getter @Setter private Group group;
    @Getter @Setter private List<MailFolderModel> mailFolders = new ArrayList<>();
    @Getter @Setter private Set<Member> memberSourceSet = new HashSet<>();
    @Getter @Setter private Integer folderSelectedId;
    @Getter @Setter private Boolean isMailPersonTargetFolder;
    @Getter @Setter private Boolean isMailPersonRechangeFolder;
    
    @PostConstruct
    public void init() {
        groups = groupService.findGroupTree(getCurrentCompanyId(), null);
        group = new Group();
        members = new DualListModel<>();
        isMailPersonTargetFolder = false;
        isMailPersonRechangeFolder = false;
        folderSelectedId = null;
        
        List<MailPerson> mps = mailPersonService.search(getCurrentCompanyId(),(short) 0);
        if (!mps.isEmpty()) {
            for (MailPerson mp : mps) {
                Short mailPersonRechangeFolder = mp.getMailPersonIsRechangeFolder();
                isMailPersonRechangeFolder = StatusUtil.getBoolean(mailPersonRechangeFolder);
                if (mp.getMailPersonTargetFolder() != null) {
                    isMailPersonTargetFolder = true;
                    Integer mailPersonTargetFolder = mp.getMailPersonTargetFolder();
                    MailFolder folderSelected = mailFolderService.search(getCurrentCompanyId(), mailPersonTargetFolder);
                    if (folderSelected != null) {
                        folderSelectedId = folderSelected.getMailFolderId();
                    } else {
                        folderSelectedId = mailPersonTargetFolder;
                    }
                    break;
                }
            }
        }

        List<Member> mMemberTarget = new ArrayList<>();
        mps.forEach((mp) -> {
            mMemberTarget.add(mp.getMailPersonInCharge());
        });
        members.setTarget(mMemberTarget);
        //
        _LoadAllMailFolder();
    }
 
    @SecureMethod(SecureMethod.Method.VIEW)
    @Override
    public void show(ActionEvent event) {
    }

    @SecureMethod(SecureMethod.Method.UPDATE)
    @Override
    public void update(ActionEvent event) {
        try {
            List<MailPerson> mailPersonTargets = new ArrayList<>();
            List<MailPerson> mailPersonSources = new ArrayList<>();
            Integer mFolderSelectedId = folderSelectedId;
            if(!isMailPersonTargetFolder){
                mFolderSelectedId = null;
            }
            List<String> listUpdated = new ArrayList<>();
            for (Member mMemberTarget : members.getTarget()) {
                MailPerson mp = mailPersonService.searchMailPersonByMemberId(getCurrentCompanyId(), mMemberTarget.getMemberId());
                if (mp != null) {
                    mp.setUpdatedId(UserModel.getLogined().getUserId());
                    mp.setUpdatedTime(Calendar.getInstance().getTime());
                    mp.setMailPersonIsDeleted(StatusUtil.UN_DELETED);
                    mp.setMailPersonTargetFolder(mFolderSelectedId);
                    mp.setMailPersonIsRechangeFolder(StatusUtil.getShort(isMailPersonRechangeFolder));
                    mailPersonSources.add(mp);
                } else {
                    mp = new MailPerson();
                    mp.setCreatorId(UserModel.getLogined().getUserId());
                    mp.setCreatedTime(Calendar.getInstance().getTime());
                    mp.setMailPersonInCharge(mMemberTarget);
                    mp.setCompany(UserModel.getLogined().getCompany());
                    mp.setMailPersonTargetFolder(mFolderSelectedId);
                    mp.setMailPersonIsRechangeFolder(StatusUtil.getShort(isMailPersonRechangeFolder));
                    mp.setMailPersonIsDeleted(StatusUtil.UN_DELETED);
                    mailPersonTargets.add(mp);
                }
                listUpdated.add(mMemberTarget.getMemberNameFirst());
            }
           for (Member mMemberSource : memberSourceSet) {
               MailPerson mp = mailPersonService.searchMailPersonByMemberId(getCurrentCompanyId(), mMemberSource.getMemberId());
               if (mp != null) {
                   mp.setUpdatedId(UserModel.getLogined().getUserId());
                   mp.setUpdatedTime(Calendar.getInstance().getTime());
                   mp.setMailPersonIsDeleted(StatusUtil.DELETED);
                   mp.setMailPersonTargetFolder(null);
                   mp.setMailPersonIsRechangeFolder((short) 0);
                   mailPersonSources.add(mp);
               }
               listUpdated.add(mMemberSource.getMemberNameFirst());
           }
           mailPersonService.batchUpdate(mailPersonSources, mailPersonTargets);
           memberSourceSet.clear();
           JsfUtil.addSuccessMessage(
                    JsfUtil.getResource().message(UserModel.getLogined().getCompanyId(),
                            ResourceUtil.BUNDLE_MAIL_NAME, "label.mail.person.update.success",listUpdated));
       } catch (Exception e) {
           LOGGER.equals(e.getLocalizedMessage());
       }
    }

    @SecureMethod(SecureMethod.Method.CREATE)
    @Override
    public void save(ActionEvent event) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @SecureMethod(SecureMethod.Method.DELETE)
    @Override
    public void delete(ActionEvent event) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void doSearch(SearchFilter filter) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void onChangeGroup(ActionEvent event) {
        Integer groupId = (Integer) event.getComponent().getAttributes().get("groupId");
        group.setGroupId(groupId);
        List<Member> mMemberSources = memberService.findByGroupId(groupId);
        mMemberSources = _RemoveDuplicateMember(mMemberSources, members.getTarget());
        members.setSource(mMemberSources);
    }
    
    private List<Member> _RemoveDuplicateMember(List<Member> memberSources, List<Member> mps){
        Iterator<Member> memberSourcesIter = memberSources.iterator();
        while(memberSourcesIter.hasNext()){
            Member member = memberSourcesIter.next();
            for(Member mp : mps){
                if(member.equals(mp)){
                    memberSourcesIter.remove();
                }
            }
        }
        return memberSources;
    }
    
    public void handleTransfer(TransferEvent event) {
       if(event.isAdd()){
           if(!memberSourceSet.isEmpty()){
               List<Member> memberAdded = (List<Member>) event.getItems();
               memberSourceSet.removeAll(memberAdded);
           }
       }
       if(event.isRemove()){
           List<Member> memberRemoved = (List<Member>) event.getItems();
           memberSourceSet.addAll(memberRemoved);
           List<Member> mMemberSources = members.getSource();
           Iterator<Member> iter = mMemberSources.iterator();
           while(iter.hasNext()){
               Member m = iter.next();
               if(!(group.getGroupId() == m.getGroup().getGroupId())){
                   iter.remove();
               }
           }
           members.setSource(mMemberSources);
       }
    }
    
    private  void _LoadAllMailFolder(){
        mailFolders.clear();
        mailFolders.addAll(MailFolderModel.getFixedFolders());
        
        List<MailFolder> dynamicFolders = mailFolderService.search(getCurrentCompanyId(), (short) 0);
        dynamicFolders.forEach((mailFolder) -> {
            MailFolderModel mfm = new MailFolderModel(mailFolder, "fa fa-folder-o");
            mailFolders.add(mfm);
        });
    }
}
