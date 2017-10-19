/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.interceptors;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author daind
 */
@Entity
@Table(name = "crm_quick_search")
@XmlRootElement
public class QuickSearch implements Serializable {

    private static final long serialVersionUID = 5640558991266354372L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "quick_search_id")
    private Integer quickSearchId;
    
    @Column(name = "quick_search_module")
    @Getter @Setter private String quickSearchModule;
    
    @Lob
    @Column(name = "quick_search_content")
    @Getter @Setter private String quickSearchContent;
    
    @Lob
    @Column(name = "quick_search_binary")
    @Getter @Setter private String quickSearchBinary;
    
    @Lob
    @Column(name = "quick_search_cols")
    @Getter @Setter private String quickSearchCols;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "quick_search_target_id")
    @Getter @Setter private Integer quickSearchTargetId;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "quick_search_deleted")
    @Getter @Setter private Short quickSearchDeleted;
    
    public QuickSearch() {
    }

    public QuickSearch(Integer quickSearchId) {
        this.quickSearchId = quickSearchId;
    }

    public QuickSearch(Integer quickSearchId, int quickSearchTargetId, short quickSearchDeleted) {
        this.quickSearchId = quickSearchId;
        this.quickSearchTargetId = quickSearchTargetId;
        this.quickSearchDeleted = quickSearchDeleted;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (quickSearchId != null ? quickSearchId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof QuickSearch)) {
            return false;
        }
        QuickSearch other = (QuickSearch) object;
        if ((this.quickSearchId == null && other.quickSearchId != null) || (this.quickSearchId != null && !this.quickSearchId.equals(other.quickSearchId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gnext.interceptors.QuickSearch[ quickSearchId=" + quickSearchId + " ]";
    }
    
}
