/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.bean.role;

import java.io.Serializable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author hungpham
 */
@Entity
@Table(name = "crm_role_page_method_rel")
@XmlRootElement
public class RolePageMethodRel implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @EmbeddedId
    @Setter @Getter
    protected RolePageMethodRelPK rolePageMethodRelPK;
    
    @JoinColumn(name = "method_id", referencedColumnName = "method_id", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    @Setter @Getter
    private Method method;
    
    @JoinColumn(name = "page_id", referencedColumnName = "page_id", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    @Setter @Getter
    private Page page;
    
    @JoinColumn(name = "role_id", referencedColumnName = "role_id", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    @Setter @Getter
    private Role role;

    public RolePageMethodRel() {
    }

    public RolePageMethodRel(RolePageMethodRelPK rolePageMethodRelPK) {
        this.rolePageMethodRelPK = rolePageMethodRelPK;
    }

    public RolePageMethodRel(int roleId, int pageId, int methodId) {
        this.rolePageMethodRelPK = new RolePageMethodRelPK(roleId, pageId, methodId);
    }
}
