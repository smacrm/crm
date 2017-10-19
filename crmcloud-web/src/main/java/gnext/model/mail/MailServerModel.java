/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.model.mail;

import gnext.bean.mail.MailServer;
import gnext.model.BaseModel;
import gnext.util.StatusUtil;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author daind
 */
public class MailServerModel extends BaseModel<MailServer> {

    @Getter @Setter private MailServer mailServer;

    public MailServerModel(MailServer ms) {
        this.mailServer = ms;
    }

    public boolean isServerAuth() {
        return StatusUtil.getBoolean(getMailServer().getServerAuth());
    }

    public void setServerAuth(boolean serverAuth) {
        getMailServer().setServerAuth(StatusUtil.getString(serverAuth));
    }

    public boolean isServerSsl() {
        return StatusUtil.getBoolean(getMailServer().getServerSsl());
    }

    public void setServerSsl(boolean serverSsl) {
        getMailServer().setServerSsl(StatusUtil.getString(serverSsl));
    }
}
