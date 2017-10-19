/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.interceptors.items;

import java.io.Serializable;
import java.util.LinkedList;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author daind
 */
public class MetaDataColumn implements Serializable {
    @Getter @Setter private LinkedList<String> cols;
    @Getter @Setter private LinkedList<String> vals;
    
    public MetaDataColumn() {}
    
    public MetaDataColumn(LinkedList<String> cols, LinkedList<String> vals) {
        this.cols = cols;
        this.vals = vals;
    }
}
