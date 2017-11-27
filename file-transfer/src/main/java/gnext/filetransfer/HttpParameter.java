/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.filetransfer;

import gnext.filetransfer.authenticate.BasicAuthenticate;
import java.util.List;
import lombok.Getter;

/**
 *
 * @author daind
 */
public class HttpParameter extends Parameter {
    
    /** nếu sử dung basic authenticate thì cần cung cấp username và password. */
    @Getter private BasicAuthenticate basicauthenticate;
    /** địa chỉ url lấy tập tin. */
    @Getter private String url;
    /** danh sách các transfer sử dụng tiếp theo. */
    @Getter private List<BaseFileTransfer> callback;
    
    public static HttpParameter getInstance() {
        return getInstance(TransferType.HTTP);
    }
    
    public static HttpParameter getInstance(TransferType type) {
        HttpParameter parameter = new HttpParameter();
        parameter.type(type);
        return parameter;
    }
    
    public HttpParameter url(String url) {
        this.url = url;
        return this;
    }
    
    public HttpParameter callback(List<BaseFileTransfer> callback) {
        this.callback = callback;
        return this;
    }
    
    public HttpParameter basicauthenticate(String username, String password) {
        this.basicauthenticate = new BasicAuthenticate(username, password);
        return this;
    }
    
    public boolean isBasicauthenticate() {
        return this.basicauthenticate != null;
    }
}
