/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.bean.role;

import gnext.bean.BaseEntity;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author hungpham
 */
@Entity
@Table(name = "crm_page")
@XmlRootElement
public class Page extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "page_id")
    @Setter @Getter
    private Integer pageId;
    
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 45)
    @Column(name = "page_name")
    @Setter @Getter
    private String pageName;
    
    @Size(max = 255)
    @Column(name = "page_source")
    @Setter @Getter
    private String pageSource;
    
    @Column(name = "page_deleted")
    @Setter @Getter
    private Short pageDeleted;
    
    @Column(name = "creator_id")
    @Setter @Getter
    private Integer creatorId;
    
    @Column(name = "created_time")
    @Temporal(TemporalType.TIMESTAMP)
    @Setter @Getter
    private Date createdTime;
    
    @Column(name = "updated_id")
    @Setter @Getter
    private Integer updatedId;
    
    @Column(name = "updated_time")
    @Temporal(TemporalType.TIMESTAMP)
    @Setter @Getter
    private Date updatedTime;
    
    @ManyToMany()
    @JoinTable(name = "crm_page_method_rel", 
        joinColumns = {
            @JoinColumn(name = "page_id", nullable = false)
        },
        inverseJoinColumns = {
            @JoinColumn(name = "method_id", nullable = false)
        }
    )
    @Setter @Getter
    @OrderBy("methodOrder ASC")
    private List<Method> methodList = new ArrayList<>();
    
    @ManyToMany(mappedBy = "pages")
    @Setter @Getter
    private List<SystemModule> moduleList = new ArrayList<>();
    
    public Page() { }
    public Page(Integer pageId) { this.pageId = pageId; }
    
    public Page(Integer pageId, String pageName) {
        this.pageId = pageId;
        this.pageName = pageName;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (pageId != null ? pageId.hashCode() : 0);
        return hash;
    }
    
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Page)) {
            return false;
        }
        Page other = (Page) object;
        if ((this.pageId == null && other.pageId != null) || (this.pageId != null && !this.pageId.equals(other.pageId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.mycompany.bean.a.Page[ pageId=" + pageId + " ]";
    }
}