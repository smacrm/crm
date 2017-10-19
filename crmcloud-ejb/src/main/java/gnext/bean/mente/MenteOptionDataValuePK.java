/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.bean.mente;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 *
 * @author gnextadmin
 */
@Embeddable
public class MenteOptionDataValuePK implements Serializable {

    @Basic(optional = false)
    @NotNull
    @Column(name = "item_id")
    private Integer itemId;
    
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 3)
    @Column(name = "item_language")
    private String itemLanguage;
    
    public MenteOptionDataValuePK() {
    }

    public MenteOptionDataValuePK(int itemId, String itemLanguage) {
        this.itemId = itemId;
        this.itemLanguage = itemLanguage;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public String getItemLanguage() {
        return itemLanguage;
    }

    public void setItemLanguage(String itemLanguage) {
        this.itemLanguage = itemLanguage;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) (itemId != null ? itemId : 0);
        hash += (itemLanguage != null ? itemLanguage.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof MenteOptionDataValuePK)) {
            return false;
        }
        MenteOptionDataValuePK other = (MenteOptionDataValuePK) object;
        return this.getItemId() == other.getItemId();
    }

    @Override
    public String toString() {
        return "gnext.bean.MenteOptionDataValuePK[ itemId=" + itemId + ", itemLanguage=" + itemLanguage + " ]";
    }
    
}
