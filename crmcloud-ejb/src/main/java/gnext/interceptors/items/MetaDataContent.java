/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.interceptors.items;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author daind
 */
public class MetaDataContent implements Serializable {
    private static final long serialVersionUID = 4734108912005479240L;
    
    @Getter @Setter private String name;
    @Getter @Setter private Object value;
    
    public MetaDataContent() {}
    
    public MetaDataContent(String name, Object value) {
        this.name = name;
        this.value = value;
    }
}

