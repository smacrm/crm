package gnext.model.customize;

import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author hungpham
 */
public class Div {
    @Setter @Getter
    private Integer id;
    
    @Setter @Getter
    private Short col;
    
    @Setter @Getter
    private String name;
    
    @Setter @Getter
    private List<Field> fields;
    
    @Setter
    private String data;
    
    public void addField(Field f){
        if(fields == null) fields = new ArrayList<>();
        fields.add(f);
    }

    public String getData() {
        if(fields != null){
            return new Gson().toJson(fields.toArray());
        }else{
            return "{}";
        }
    }
    
}
