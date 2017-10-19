/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.mailapi;

import gnext.mailapi.mail.Email;
import gnext.mailapi.util.ParameterUtil;
import java.util.Map;

/**
 *
 * @author daind
 * @param <E>
 */
public class EmailBuilder<E extends Email> {
    private final E e;
    public EmailBuilder(E e) { this.e = e; }
    public EmailBuilder(E e, String type) { this(e); e.setType(type); }
    
    public EmailBuilder<E> parameters(Map<String, String> parameters) {
        type(ParameterUtil.getType(parameters));
        
        host(ParameterUtil.getHost(parameters));
        port(ParameterUtil.getPort(parameters));
        username(ParameterUtil.getUser(parameters));
        password(ParameterUtil.getPass(parameters));
        
        ssl(ParameterUtil.getSsl(parameters));
        sslPort(ParameterUtil.getSslPort(parameters));
        sslSmtpPort(ParameterUtil.getSslSmtpPort(parameters));
        
        auth(ParameterUtil.getAuth(parameters));
        tls(ParameterUtil.getTls(parameters));
        
        folder(ParameterUtil.getFolder(parameters));
        flag(ParameterUtil.getFlag(parameters));
        
        debug(ParameterUtil.getDebug(parameters));
        timeout(ParameterUtil.getTimeout(parameters));
        
        return this;
    }
    
    public EmailBuilder<E> type(String type) { e.setType(type); return this; }
    public EmailBuilder<E> timeout(Integer timeout) { e.setTimeout(timeout); return this; }
    
    public EmailBuilder<E> host(String host) { e.setHost(host); return this; }
    public EmailBuilder<E> port(Integer port) { e.setPort(port); return this; }
    public EmailBuilder<E> username(String username) { e.setUserName(username); return this; }
    public EmailBuilder<E> password(String password) { e.setPassword(password); return this; }
    
    public EmailBuilder<E> ssl(Boolean ssl) { e.setSsl(ssl); return this; }
    public EmailBuilder<E> sslPort(Integer sslPort) { e.setSslPort(sslPort); return this; }
    public EmailBuilder<E> sslSmtpPort(Integer sslSmtpPort) { e.setSslPort(sslSmtpPort); return this; }
    
    public EmailBuilder<E> auth(Boolean auth) { e.setAuth(auth); return this; }
    public EmailBuilder<E> tls(Boolean tls) { e.setTls(tls); return this; }
    
    public EmailBuilder<E> folder(String folder) { e.setFolder(folder); return this; }
    public EmailBuilder<E> flag(String flag) { e.setFlag(flag); return this; }
    
    public EmailBuilder<E> debug(Boolean debug) { e.setDebug(debug); return this; }
    
    public E builder() { return e; }
}
