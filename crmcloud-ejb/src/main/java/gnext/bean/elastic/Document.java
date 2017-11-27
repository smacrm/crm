/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.bean.elastic;

import java.io.Serializable;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author hungpham
 */
public class Document implements Serializable{
    @Getter @Setter
    public String id;
    
    @Getter @Setter
    public Map<String, Object> data;

    public Document(String id, Map<String, Object> data) {
        this.id = id;
        this.data = data;
    }
    
    
}
