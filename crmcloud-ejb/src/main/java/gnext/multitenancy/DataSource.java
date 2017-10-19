/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.multitenancy;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author daind
 */
public class DataSource {
    @Getter @Setter private String driver;
    @Getter @Setter private String usr;
    @Getter @Setter private String pwd;
    @Getter @Setter private String host;
    @Getter @Setter private String port;

    public DataSource() {}
    
    public DataSource(String driver, String usr, String pwd, String host, String port) {
        this.driver = driver;
        this.usr = usr;
        this.pwd = pwd;
        this.host = host;
        this.port = port;
    }
}
