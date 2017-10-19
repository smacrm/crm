package gnext.bean;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

/**
 *
 * @author daind
 */
@Embeddable
public class DatabaseServerCompanyRelPK implements Serializable {

    @Basic(optional = false)
    @NotNull
    @Column(name = "database_server_id", nullable = false)
    private int databaseServerId;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "company_id", nullable = false)
    private int companyId;

    public DatabaseServerCompanyRelPK() {
    }

    public DatabaseServerCompanyRelPK(int databaseServerId, int companyId) {
        this.databaseServerId = databaseServerId;
        this.companyId = companyId;
    }

    public int getDatabaseServerId() {
        return databaseServerId;
    }

    public void setDatabaseServerId(int databaseServerId) {
        this.databaseServerId = databaseServerId;
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
        hash += (int) databaseServerId;
        hash += (int) companyId;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof DatabaseServerCompanyRelPK)) {
            return false;
        }
        DatabaseServerCompanyRelPK other = (DatabaseServerCompanyRelPK) object;
        if (this.databaseServerId != other.databaseServerId) {
            return false;
        }
        if (this.companyId != other.companyId) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gnext.bean.DatabaseServerCompanyRelPK[ databaseServerId=" + databaseServerId + ", companyId=" + companyId + " ]";
    }
    
}
