/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.bean;

import gnext.interceptors.items.MetaDataContent;
import java.io.Serializable;
import java.util.List;

/**
 *
 * @author daind
 */
public interface QuickSearchEntity extends Serializable {
    public List<MetaDataContent> getMetadata();
}
