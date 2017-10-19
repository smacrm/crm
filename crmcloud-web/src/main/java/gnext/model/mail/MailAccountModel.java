/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.model.mail;

import gnext.bean.MailAccount;
import gnext.bean.mail.MailServer;
import gnext.model.BaseModel;
import gnext.model.authority.UserModel;
import gnext.util.StatusUtil;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author daind
 */
public class MailAccountModel extends BaseModel<MailAccount> {

    @Getter @Setter private MailAccount mailAccount;
    private boolean accountSendFlag;
    private boolean accountReceiveFlag;
    private boolean accountSupport;
    private boolean accountRequest;
    private boolean accountDeleteReceivedDays;

    public MailAccountModel() {
        this(new MailAccount(new MailServer(), UserModel.getLogined().getCompany()));
    }

    public MailAccountModel(MailAccount mailAccount) {
        this.mailAccount = mailAccount;
    }

    public boolean isAccountSendFlag() {
        return StatusUtil.getBoolean(getMailAccount().getAccountSendFlag());
    }

    public void setAccountSendFlag(boolean accountSendFlag) {
        this.accountSendFlag = accountSendFlag;
        mailAccount.setAccountSendFlag(StatusUtil.getShort(accountSendFlag));
    }

    public boolean isAccountReceiveFlag() {
        return StatusUtil.getBoolean(mailAccount.getAccountReceiveFlag());
    }

    public void setAccountReceiveFlag(boolean accountReceiveFlag) {
        this.accountReceiveFlag = accountReceiveFlag;
        mailAccount.setAccountReceiveFlag(StatusUtil.getShort(accountReceiveFlag));
    }

    public boolean isAccountSupport() {
        return StatusUtil.getBoolean(mailAccount.getAccountSupport());
    }

    public void setAccountSupport(boolean accountSupport) {
        this.accountSupport = accountSupport;
        mailAccount.setAccountSupport(StatusUtil.getShort(accountSupport));
    }

    public boolean isAccountRequest() {
        return StatusUtil.getBoolean(mailAccount.getAccountRequest());
    }

    public void setAccountRequest(boolean accountRequest) {
        this.accountRequest = accountRequest;
        mailAccount.setAccountRequest(StatusUtil.getShort(accountRequest));
    }

    public boolean isAccountDeleteReceivedDays() {
        return accountDeleteReceivedDays;
    }

    public void setAccountDeleteReceivedDays(boolean accountDeleteReceivedDays) {
        this.accountDeleteReceivedDays = accountDeleteReceivedDays;
    }
    
}
