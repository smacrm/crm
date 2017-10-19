/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.controller.mail;

import gnext.bean.mail.MailFolder;
import gnext.controller.AbstractController;
import gnext.model.authority.UserModel;
import gnext.model.mail.MailFolderModel;
import gnext.model.search.SearchFilter;
import gnext.security.annotation.SecureMethod;
import gnext.security.annotation.SecurePage;
import gnext.service.mail.MailDataService;
import gnext.service.mail.MailFolderService;
import gnext.util.DateUtil;
import gnext.util.JsfUtil;
import gnext.util.ResourceUtil;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.event.ActionEvent;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author daind
 */
@ManagedBean(name = "mailFolderController", eager = true)
@SessionScoped()
@SecurePage(module = SecurePage.Module.MAIL, require = true)
public class MailFolderController extends AbstractController {
    private static final long serialVersionUID = 7336336257003916161L;
    private static final Logger LOGGER = LoggerFactory.getLogger(MailFolderController.class);

    @EJB private MailFolderService mailFolderService;
    @EJB private MailDataService mailDataService;

    @Getter @Setter private String selFolderCode = MailFolder.DATA_MAIL_FOLDER_INBOX; // mã code folder đang được chọn.
    @Getter @Setter private MailFolderModel folderModel; // model cho các hành động thêm mới, sửa, xóa.

    @Getter @Setter private List<MailFolderModel> fixedFolders = new ArrayList<>();
    @Getter @Setter private List<MailFolderModel> dynamicFolders = new ArrayList<>();
    @Getter @Setter private List<MailFolderModel> allFolders = new ArrayList<>();

    private void _Load() {
        _LoadFixedFolder();
        _LoadDynamicFolder();
        _LoadAllFolders();
    }

    private void _LoadFixedFolder() {
        fixedFolders.clear();
        fixedFolders.addAll(MailFolderModel.getFixedFolders());
        for (MailFolderModel model : fixedFolders) {
            String folderCode = model.getFolderCode();
            model.setSelected(selFolderCode.equals(folderCode));

            String sql = "company_id = {0} AND mail_data_folder_code = {1}";
            sql = MessageFormat.format(sql, getCurrentCompanyId(), folderCode);
            int total = mailDataService.total(getCurrentCompanyId(), sql);
            model.setTotal(total);
        }
    }
    
    @SecureMethod(SecureMethod.Method.VIEW)
    public void load(){}

    private void _LoadDynamicFolder() {
        dynamicFolders.clear();
        List<MailFolder> mailFolders = mailFolderService.search(getCurrentCompanyId(), (short) 0);
        for (MailFolder mailFolder : mailFolders) {
            String folderCode = mailFolder.getMailFolderId().toString();
            MailFolderModel mfm = new MailFolderModel(mailFolder, "fa fa-folder-o");

            mfm.setSelected(selFolderCode.equals(folderCode));
            dynamicFolders.add(mfm);
        }
    }

    private void _LoadAllFolders() {
        this.allFolders.clear();
        this.allFolders.addAll(fixedFolders);
        this.allFolders.addAll(dynamicFolders);
    }

    @PostConstruct
    public void init() {
        folderModel = new MailFolderModel();
        _Load();
    }

    @Override
    protected void doSearch(SearchFilter filter) {
        throw new UnsupportedOperationException("Not supported yet.");

    }

    @SecureMethod(SecureMethod.Method.UPDATE)
    public void onChangeMailFolder(ActionEvent event) {
        if (StringUtils.isEmpty(getParameter("mailFolderId"))) {
            folderModel = new MailFolderModel();
        } else {
            Integer mailFolderId = Integer.parseInt(getParameter("mailFolderId"));
            MailFolder mf = mailFolderService.find(mailFolderId);
            this.folderModel.setMailFolder(mf);
        }
    }

    @Override
    @SecureMethod(SecureMethod.Method.DELETE)
    public void delete(ActionEvent event) {
        try {
            Integer mailFolderId = Integer.parseInt(getParameter("mailFolderId"));
            MailFolder mf = mailFolderService.find(mailFolderId);
            mailFolderService.delete(mf);
            _LoadFixedFolder();
            _LoadDynamicFolder();
            JsfUtil.addSuccessMessage(msgBundle.getString("msg.action.delete.success", mf.getMailFolderName()));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    @SecureMethod(SecureMethod.Method.UPDATE)
    public void update(ActionEvent event) {
        try {
            MailFolder mf = folderModel.getMailFolder();
        
            MailFolder tmp = mailFolderService.search(getCurrentCompanyId(), mf.getMailFolderName(), (short) 0);
            if (tmp == null) {
                mf.setUpdatedId(UserModel.getLogined().getUserId());
                mf.setUpdatedTime(DateUtil.now());
                mailFolderService.edit(mf);
                JsfUtil.updateForClientId("mainContent");
                _LoadDynamicFolder();
                JsfUtil.addSuccessMessage(msgBundle.getString("msg.action.update.success", mf.getMailFolderName()));
            } else {
                if(mf.getMailFolderId().intValue() != tmp.getMailFolderId().intValue()) {
                    JsfUtil.addErrorMessage("フォルダ名が重複しています");
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    @SecureMethod(SecureMethod.Method.CREATE)
    public void create(ActionEvent event) {
        try {
            MailFolder mf = folderModel.getMailFolder();

            MailFolder tmp = mailFolderService.search(getCurrentCompanyId(), mf.getMailFolderName(), (short) 0);
            if (tmp == null) {
                mf.setCreatorId(UserModel.getLogined().getUserId());
                mf.setCreatedTime(DateUtil.now());
                mf.setCompany(getCurrentCompany());
                mailFolderService.create(mf);
                JsfUtil.updateForClientId("mainContent");
                _LoadDynamicFolder();
                JsfUtil.addSuccessMessage(msgBundle.getString("msg.action.create.success", mf.getMailFolderName()));
            } else {
                JsfUtil.addErrorMessage("フォルダ名が重複しています");
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
    
    public void changeFolder(String folder) {
        setSelFolderCode(folder);
        init();
    }
    
    public String getCurrentFolderName() {
        if(allFolders == null || allFolders.isEmpty()) return StringUtils.EMPTY;
        for(MailFolderModel mfm : allFolders) {
            if(mfm.getFolderCode().equals(selFolderCode)) return mfm.getMailFolder().getMailFolderName();
        }
        return StringUtils.EMPTY;
    }
}
