package gnext.bean.project;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author hungpham
 * @since Feb 7, 2017
 */
@Entity
@Table(name = "crm_project_cust_column_width")
@NamedQueries({})
public class ProjectCustColumnWidth implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @Id
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 45)
    @Column(name = "column_id")
    @Getter @Setter
    private String columnId;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "column_width")
    @Getter @Setter
    private int columnWidth;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "company_id")
    @Getter @Setter
    private int companyId;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "updated_id")
    @Getter @Setter
    private int updatedId;
    
    @Column(name = "updated_time")
    @Temporal(TemporalType.TIMESTAMP)
    @Getter @Setter
    private Date updatedTime;

    public ProjectCustColumnWidth() {
    }

    public ProjectCustColumnWidth(String columnId) {
        this.columnId = columnId;
    }

    public ProjectCustColumnWidth(String columnId, int columnWidth, int companyId, int updatedId) {
        this.columnId = columnId;
        this.columnWidth = columnWidth;
        this.companyId = companyId;
        this.updatedId = updatedId;
    }


    @Override
    public int hashCode() {
        int hash = 0;
        hash += (columnId != null ? columnId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ProjectCustColumnWidth)) {
            return false;
        }
        ProjectCustColumnWidth other = (ProjectCustColumnWidth) object;
        if ((this.columnId == null && other.columnId != null) || (this.columnId != null && !this.columnId.equals(other.columnId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gnext.bean.project.ProjectCustColumnWidth[ columnId=" + columnId + " ]";
    }

}
