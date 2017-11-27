/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.bean.project;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author hungpham
 */
public class DynamicColumn implements Serializable{
    @Setter @Getter
    private String id;
    
    @Setter @Getter
    private String name;
    
    @Setter @Getter
    private int type;

    @Setter @Getter
    private Boolean required = false;
    
    @Setter @Getter
    private Integer tabId; //Su dung cho truong hop column la customize

    public DynamicColumn(String id, String name) {
        if(StringUtils.isEmpty(id))
            throw new RuntimeException("The ID need to not empty.");
        
        this.id = id;
        this.name = name;
    }

    public DynamicColumn(String id, String name, int type) {
        if(StringUtils.isEmpty(id))
            throw new RuntimeException("The ID need to not empty.");
        
        this.id = id;
        this.name = name;
        this.type = type;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj != null){
            DynamicColumn other = (DynamicColumn)obj;
            return this.id.equals(other.getId());
        }
        return false;
    }
}
