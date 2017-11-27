/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.bean;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author hungpham
 */
@Embeddable
public class MemberAuthPK implements Serializable{
    @Column
    @Setter @Getter
    private int company_id;
    
    @Column
    @Setter @Getter
    private int member_id;
    
     @Column
    @Setter @Getter
    private String module;
    
    @Column
    @Setter @Getter
    private String page;
    
    @Column
    @Setter @Getter
    private String method;
    
    public MemberAuthPK() {
    }

    public MemberAuthPK(int company_id, int member_id) {
        this.company_id = company_id;
        this.member_id = member_id;
    }
    
}
