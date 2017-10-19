/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.bean.role;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

/**
 *
 * @author hungpham
 */
@Embeddable
public class RolePageMethodRelPK implements Serializable {
    @Basic(optional = false)
    @NotNull
    @Column(name = "role_id")
    private int roleId;
    @Basic(optional = false)
    @NotNull
    @Column(name = "page_id")
    private int pageId;
    @Basic(optional = false)
    @NotNull
    @Column(name = "method_id")
    private int methodId;

    public RolePageMethodRelPK() {
    }

    public RolePageMethodRelPK(int roleId, int pageId, int methodId) {
        this.roleId = roleId;
        this.pageId = pageId;
        this.methodId = methodId;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public int getPageId() {
        return pageId;
    }

    public void setPageId(int pageId) {
        this.pageId = pageId;
    }

    public int getMethodId() {
        return methodId;
    }

    public void setMethodId(int methodId) {
        this.methodId = methodId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) roleId;
        hash += (int) pageId;
        hash += (int) methodId;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof RolePageMethodRelPK)) {
            return false;
        }
        RolePageMethodRelPK other = (RolePageMethodRelPK) object;
        if (this.roleId != other.roleId) {
            return false;
        }
        if (this.pageId != other.pageId) {
            return false;
        }
        if (this.methodId != other.methodId) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gnext.bean.RolePageMethodRelPK[ roleId=" + roleId + ", pageId=" + pageId + ", methodId=" + methodId + " ]";
    }
    
}
