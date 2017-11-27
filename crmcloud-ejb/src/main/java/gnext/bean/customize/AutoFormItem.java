/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.bean.customize;

import gnext.bean.BaseEntity;
import gnext.bean.Company;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
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
@Table(name = "crm_item")
@XmlRootElement
public class AutoFormItem  extends BaseEntity {

    @Column(name = "item_order")
    private Integer itemOrder;
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "item_id")
    @Setter @Getter
    private Integer itemId;
    @Basic(optional = false)
    @NotNull
    @Column(name = "item_type")
    @Setter @Getter
    private int itemType;
    @Basic(optional = false)
    @Column(name = "item_class")
    @Setter @Getter
    private String itemClass;
    @Basic(optional = false)
    @NotNull
    @Column(name = "item_level")
    @Setter @Getter
    private int itemLevel;
    @Basic(optional = false)
    @NotNull
    @Column(name = "item_required")
    @Setter @Getter
    private short itemRequired;
    @Basic(optional = false)
    @NotNull
    @Column(name = "item_multiple")
    @Setter @Getter
    private short itemMultiple;
    @Column(name = "item_parent_id")
    @Setter @Getter
    private Integer itemParentId;
    @Column(name = "item_reference_type")
    @Setter @Getter
    private Boolean itemReferenceType;
    @Column(name = "item_edit_add_flag")
    @Setter @Getter
    private Boolean itemEditAddFlag;
    @Column(name = "item_search_flag")
    @Setter @Getter
    private Boolean itemSearchFlag;
    @Column(name = "item_show_flag")
    @Setter @Getter
    private Boolean itemShowFlag;
    
    @Column(name = "item_deleted")
    @Setter @Getter
    private Boolean itemDeleted;
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

    @JoinColumn(name = "company_id", referencedColumnName = "company_id",
            nullable = false, insertable = true, updatable = false)
    @ManyToOne(optional = false)
    @Getter @Setter
    private Company company;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "item")
    @XmlTransient
    @Setter @Getter
    private List<AutoFormMultipleDataValue> autoFormMultipleDataValueList;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "item")
    @XmlTransient
    @Setter @Getter
    private List<AutoFormPageTabDivItemRel> autoFormPageTabDivItemRelList;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "autoFormItem")
    @Setter @Getter
    private List<AutoFormItemGlobal> itemGlobalList = new ArrayList<>();
    

    public AutoFormItem() {
    }

    public AutoFormItem(Integer itemId) {
        this.itemId = itemId;
    }

    public AutoFormItem(Integer itemId, int itemType, int itemLevel, short itemRequired, short itemMultiple) {
        this.itemId = itemId;
        this.itemType = itemType;
        this.itemLevel = itemLevel;
        this.itemRequired = itemRequired;
        this.itemMultiple = itemMultiple;
    }

    public Map<String, Object> getItemName() {
        Map<String, Object> itemName = new HashMap<>();
        if( getItemGlobalList().size() > 0 ){
            getItemGlobalList().forEach((item) -> {
                itemName.put(item.getAutoFormItemGlobalPK().getItemLang(), item.getItemName());
            });
        }
        return itemName;
    }

//    public String getItemName(String locale) {
//        if(StringUtils.isBlank(locale)) return InterfaceUtil.FIELD_NAME_PREFIX + this.itemId;
//        if( getAutoFormItemGlobalList().size() > 0 ){
//            for(AutoFormItemGlobal item:getAutoFormItemGlobalList()) {
//                if(!locale.equals(item.autoFormItemGlobalPK.getItemLang())) continue;
//                return item.getItemName();
//            }
//        }
//        return InterfaceUtil.FIELD_NAME_PREFIX + this.itemId;
//    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (itemId != null ? itemId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof AutoFormItem)) {
            return false;
        }
        AutoFormItem other = (AutoFormItem) object;
        if ((this.itemId == null && other.itemId != null) || (this.itemId != null && !this.itemId.equals(other.itemId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gnext.bean.customize.AutoFormItem[ itemId=" + itemId + " ]";
    }

    public Integer getItemOrder() {
        return itemOrder;
    }

    public void setItemOrder(Integer itemOrder) {
        this.itemOrder = itemOrder;
    }
}
