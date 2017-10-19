/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.interceptors.annotation.enums;

/**
 *
 * @author daind
 */
public enum Module {
    COMPANY(true, "COMPANY"),
    MEMBER(true,"MEMBER"),
    ISSUE(true, "ISSUE"),
    MAIL(true, "MAIL"),
    MESSAGE(true, "MESSAGE"),
    AUTHENTICATION(true, "AUTHENTICATION"),
    CUSTOMIZE(true, "CUSTOMIZE"),
    SPEECHAPI(true, "SPEECHAPI"),
    SYSTEM(true, "SYSTEM"),
    REPORT(true, "REPORT"),
    CUSTOMER(true, "CUSTOMER"),
    NONE(true, "NONE");

    private final boolean use;
    private final String name;

    private Module(boolean use, String name) {
        this.use = use;
        this.name = name;
    }
    
    public boolean isUse() {return this.use;}
    public String getName() {return this.name;}
    
    @Override
    public String toString() {
        return this.name;
    }
}
