/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.model.system;

import gnext.bean.attachment.Server;
import gnext.model.BaseModel;
import gnext.util.StatusUtil;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author tungdt
 */
public class ServerModel extends BaseModel<Server>{
    @Getter @Setter private Server server;

    public ServerModel(Server server) {
        this.server = server;
    }

    public void setServerSsl(boolean serverSsl) {
        getServer().setServerSsl(StatusUtil.getShort(serverSsl));
    }

    public boolean isServerSsl() {
       return StatusUtil.getBoolean(server.getServerSsl());
    }
}
