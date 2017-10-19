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
public class Tab {
    @Setter @Getter
    private int id;
    
    @Setter @Getter
    private String name;
    
    @Setter @Getter
    private int order = 1;
    
    @Setter @Getter
    private List<Div> divs;
    
    @Setter
    private String data;
    
    public void addDiv(Div f){
        if(divs == null) divs = new ArrayList<>();
        divs.add(f);
    }

    public String getData() {
        if(divs != null){
            return new Gson().toJson(divs.toArray());
        }else{
            return "{}";
        }
    }
    
}
