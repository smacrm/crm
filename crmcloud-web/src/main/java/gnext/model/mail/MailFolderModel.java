/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.model.mail;

import gnext.bean.mail.MailFolder;
import gnext.model.BaseModel;
import gnext.resource.bundle.MailBundle;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author daind
 */
public class MailFolderModel extends BaseModel<MailFolder> {
    private static final long serialVersionUID = 707889213588436688L;
    @Getter @Setter private MailFolder mailFolder;
    @Getter @Setter private String folderCode;
    @Getter @Setter private String icon;
    @Getter @Setter private int total;

    public MailFolderModel() {
        this.mailFolder = new MailFolder();
    }

    public MailFolderModel(String folderCode, String mailFolderName, String icon) {
        this.folderCode = folderCode;
        this.mailFolder = new MailFolder(Integer.parseInt(folderCode), mailFolderName);
        this.icon = icon;
    }

    public MailFolderModel(MailFolder mailFolder, String icon) {
        this.folderCode = mailFolder.getMailFolderId().toString();
        this.mailFolder = mailFolder;
        this.icon = icon;
    }

    public String getDisplayFolderNameWithIcon() {
        return this.mailFolder.getMailFolderName();
    }

    public static List<MailFolderModel> getFixedFolders() {
        MailBundle msg = new MailBundle();
        List<MailFolderModel> fixedFolders = new ArrayList<>();
        fixedFolders.add(new MailFolderModel(MailFolder.DATA_MAIL_FOLDER_INBOX, msg.getString("label.mail.folder.inbox"), "fa fa-inbox"));
        fixedFolders.add(new MailFolderModel(MailFolder.DATA_MAIL_FOLDER_DRAFT, msg.getString("label.mail.folder.draft"), "fa fa-file-text-o"));
        fixedFolders.add(new MailFolderModel(MailFolder.DATA_MAIL_FOLDER_TRASH, msg.getString("label.mail.folder.trash"), "fa fa-trash-o"));
        fixedFolders.add(new MailFolderModel(MailFolder.DATA_MAIL_FOLDER_JUNK, msg.getString("label.mail.folder.junk"), "fa fa-filter"));
        fixedFolders.add(new MailFolderModel(MailFolder.DATA_MAIL_FOLDER_SENT, msg.getString("label.mail.folder.sent"), "fa fa-envelope-o"));

        return fixedFolders;
    }
}
