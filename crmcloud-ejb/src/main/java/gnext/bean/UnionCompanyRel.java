/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.bean;

import java.io.Serializable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author hungpham
 */
@Entity
@Table(name = "crm_union_company_rel")
@XmlRootElement
public class UnionCompanyRel implements Serializable {
    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected UnionCompanyRelPK crmUnionCompanyRelPK;

    public UnionCompanyRel() { }

    public UnionCompanyRel(UnionCompanyRelPK crmUnionCompanyRelPK) {
        this.crmUnionCompanyRelPK = crmUnionCompanyRelPK;
    }

    public UnionCompanyRel(String companyUnionKey, int companyId) {
        this.crmUnionCompanyRelPK = new UnionCompanyRelPK(companyUnionKey, companyId);
    }

    public UnionCompanyRelPK getUnionCompanyRelPK() {
        return crmUnionCompanyRelPK;
    }

    public void setUnionCompanyRelPK(UnionCompanyRelPK crmUnionCompanyRelPK) {
        this.crmUnionCompanyRelPK = crmUnionCompanyRelPK;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (crmUnionCompanyRelPK != null ? crmUnionCompanyRelPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof UnionCompanyRel)) {
            return false;
        }
        UnionCompanyRel other = (UnionCompanyRel) object;
        if ((this.crmUnionCompanyRelPK == null && other.crmUnionCompanyRelPK != null) || (this.crmUnionCompanyRelPK != null && !this.crmUnionCompanyRelPK.equals(other.crmUnionCompanyRelPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gnext.bean.UnionCompanyRel[ crmUnionCompanyRelPK=" + crmUnionCompanyRelPK + " ]";
    }
    
}
