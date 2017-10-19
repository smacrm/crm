/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.bean.role;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
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
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author hungpham
 */
@Entity
@Table(name = "crm_system_module")
@XmlRootElement
public class SystemModule implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "module_id", nullable = false)
    @Setter @Getter
    private Integer moduleId;
    
    @Basic(optional = false)
    @NotNull
    //@Size(min = 1, max = 45)
    @Column(name = "module_name", nullable = false, length = 45)
    @Setter @Getter
    private String moduleName;
    
    @Column(name = "module_deleted")
    @Setter @Getter
    private Short moduleDeleted;
    
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
    
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "crm_module_page_rel",
        joinColumns = {
            @JoinColumn(name = "module_id", nullable = false)
        },
        inverseJoinColumns = {
            @JoinColumn(name = "page_id", nullable = false)
        }
    )
    @Setter @Getter
    @OrderBy("pageId ASC")
    private List<Page> pages = new ArrayList<>();
    
//    @ManyToMany(mappedBy = "systemModuleList")
//    @Setter @Getter
//    private Collection<Company> companyCollection;

    public SystemModule() {
    }

    public SystemModule(Integer moduleId) {
        this.moduleId = moduleId;
    }

    public SystemModule(Integer moduleId, String modueName) {
        this.moduleId = moduleId;
        this.moduleName = modueName;
    }

    public void removePage(Page pageBean) {
        for (int i = 0; i < pages.size(); i++) {
            Page p = pages.get(i);
            if(Objects.equals(p.getPageId(), pageBean.getPageId())){
                pages.remove(i);
                return;
            }
        }
    }
}
