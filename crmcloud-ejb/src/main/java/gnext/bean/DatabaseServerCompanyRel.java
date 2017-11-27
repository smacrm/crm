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
 * @author daind
 */
@Entity
@Table(name = "crm_database_server_company_rel")
@XmlRootElement
public class DatabaseServerCompanyRel implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @EmbeddedId
    protected DatabaseServerCompanyRelPK databaseServerCompanyRelPK;

    public DatabaseServerCompanyRel() { }
    public DatabaseServerCompanyRel(DatabaseServerCompanyRelPK databaseServerCompanyRelPK) { this.databaseServerCompanyRelPK = databaseServerCompanyRelPK; }

    public DatabaseServerCompanyRel(int databaseServerId, int companyId) {
        this.databaseServerCompanyRelPK = new DatabaseServerCompanyRelPK(databaseServerId, companyId);
    }

    public DatabaseServerCompanyRelPK getDatabaseServerCompanyRelPK() {
        return databaseServerCompanyRelPK;
    }

    public void setDatabaseServerCompanyRelPK(DatabaseServerCompanyRelPK databaseServerCompanyRelPK) {
        this.databaseServerCompanyRelPK = databaseServerCompanyRelPK;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (databaseServerCompanyRelPK != null ? databaseServerCompanyRelPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof DatabaseServerCompanyRel)) {
            return false;
        }
        DatabaseServerCompanyRel other = (DatabaseServerCompanyRel) object;
        if ((this.databaseServerCompanyRelPK == null && other.databaseServerCompanyRelPK != null) || (this.databaseServerCompanyRelPK != null && !this.databaseServerCompanyRelPK.equals(other.databaseServerCompanyRelPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gnext.bean.DatabaseServerCompanyRel[ databaseServerCompanyRelPK=" + databaseServerCompanyRelPK + " ]";
    }
    
}
