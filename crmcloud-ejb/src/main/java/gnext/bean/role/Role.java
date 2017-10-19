/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.bean.role;

import gnext.bean.Company;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author hungpham
 */
@Entity
// @Cacheable(true)
@Table(name = "crm_role")
@XmlRootElement
public class Role implements Serializable {
    private static final long serialVersionUID = -8786075965386881319L;
    
    public static final short ROLE_HIDDEN = 1;
    public static final short ROLE_UN_HIDDEN = 0;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "role_id")
    @Setter @Getter
    private Integer roleId;
    
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 45)
    @Column(name = "role_name")
    @Setter @Getter
    private String roleName;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "role_flag")
    @Setter @Getter
    private short roleFlag;
    
    @Column(name = "role_deleted")
    @Setter @Getter
    private Short roleDeleted;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "company_id")
    @Setter @Getter
    private int companyId;
    
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
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "role", fetch = FetchType.EAGER)
    @Setter @Getter
    @XmlTransient
    private List<RolePageMethodRel> rolePageMethodList = new ArrayList<>();
    
    @JoinColumn(name = "company_id", referencedColumnName = "company_id", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    @Setter @Getter
    private Company company;
    
    public Role() { } 
    public Role(Integer roleId) {
        this.roleId = roleId;
    }
    public Role(Integer roleId, String roleName, short roleFlag, int companyId) {
        this.roleId = roleId;
        this.roleName = roleName;
        this.roleFlag = roleFlag;
        this.companyId = companyId;
    }
    
    public boolean isDeleted(){
        return roleDeleted == 1;
    }
}
