/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.bean;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 *
 * @author hungpham
 */
@Embeddable
public class UnionCompanyRelPK implements Serializable {
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "company_union_key", nullable = false, length = 100)
    private String companyUnionKey;
    @Basic(optional = false)
    @NotNull
    @Column(name = "company_id", nullable = false)
    private int companyId;

    public UnionCompanyRelPK() {
    }

    public UnionCompanyRelPK(String companyUnionKey, int companyId) {
        this.companyUnionKey = companyUnionKey;
        this.companyId = companyId;
    }

    public String getCompanyUnionKey() {
        return companyUnionKey;
    }

    public void setCompanyUnionKey(String companyUnionKey) {
        this.companyUnionKey = companyUnionKey;
    }

    public int getCompanyId() {
        return companyId;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (companyUnionKey != null ? companyUnionKey.hashCode() : 0);
        hash += (int) companyId;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof UnionCompanyRelPK)) {
            return false;
        }
        UnionCompanyRelPK other = (UnionCompanyRelPK) object;
        if ((this.companyUnionKey == null && other.companyUnionKey != null) || (this.companyUnionKey != null && !this.companyUnionKey.equals(other.companyUnionKey))) {
            return false;
        }
        if (this.companyId != other.companyId) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gnext.bean.CrmUnionCompanyRelPK[ companyUnionKey=" + companyUnionKey + ", companyId=" + companyId + " ]";
    }
    
}
