/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.model.mail;

import gnext.bean.mail.MailFilter;
import gnext.model.BaseModel;
import gnext.util.StatusUtil;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author daind
 */
public class MailFilterModel extends BaseModel<MailFilter> {

    @Getter @Setter private MailFilter mailFilter;
    private boolean mailFilterMoveFolderFlag;
    private boolean mailFilterUseSettingExplodeFlag;

    public MailFilterModel(MailFilter filter) {
        this.mailFilter = filter;
    }

    public boolean isMailFilterMoveFolderFlag() {
        return StatusUtil.getBoolean(mailFilter.getMailFilterMoveFolderFlag());
    }

    public void setMailFilterMoveFolderFlag(boolean mailFilterMoveFolderFlag) {
        this.mailFilterMoveFolderFlag = mailFilterMoveFolderFlag;
        mailFilter.setMailFilterMoveFolderFlag(StatusUtil.getShort(mailFilterMoveFolderFlag));
    }

    public boolean isMailFilterUseSettingExplodeFlag() {
        return StatusUtil.getBoolean(mailFilter.getMailFilterUseSettingExplodeFlag());
    }

    public void setMailFilterUseSettingExplodeFlag(boolean mailFilterUseSettingExplodeFlag) {
        this.mailFilterUseSettingExplodeFlag = mailFilterUseSettingExplodeFlag;
        mailFilter.setMailFilterUseSettingExplodeFlag(StatusUtil.getShort(mailFilterUseSettingExplodeFlag));
    }

}
