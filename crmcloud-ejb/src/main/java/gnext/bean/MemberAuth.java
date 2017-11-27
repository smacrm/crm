/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.bean;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.persistence.annotations.ReadOnly;

/**
 *
 * @author hungpham
 */
@Entity
@ReadOnly
public class MemberAuth implements Serializable{
    
    private static final long serialVersionUID = 1L;
    @EmbeddedId
    @Setter @Getter
    protected MemberAuthPK memberAuthPK;
        
    @Column
    @Setter @Getter
    private int role_flag;
    
    @Override
    public String toString() {
        return String.format("Role COMP[%d] MEM[%s] FLAG[%d] PAGE[%s] METH[%s]", memberAuthPK.getCompany_id(), memberAuthPK.getMember_id(), role_flag, memberAuthPK.getPage(), memberAuthPK.getMethod());
    }
}
