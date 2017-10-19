package gnext.model.report;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Setter;

/**
 *
 * @author hungpham
 * @since Nov 29, 2016
 */
public class LineChartData extends AbstractChartData<String, Map<Integer, Integer>>{ //Item ID -> {Month -> current}
    
    @Setter private Map<Integer, String> labelMapping = new HashMap<>();
    
    public String getLabel(Integer id){
        return labelMapping.get(id);
    }
    
    public String getMonths(){
        final JsonArray arr = new JsonArray();
        this.getMonthList().forEach((month) -> {
            arr.add(month+"月");
        });
        return new Gson().toJson(arr);
    }
    
    public List<String> getProducts(){
        List<String> productList = new ArrayList<>();
        this.forEach((id, pd) -> {
            productList.add(id);
        });
        return productList;
    }
    
    public String getValues(String id){
        final JsonArray arr = new JsonArray();
        Map<Integer, Integer> productData = this.get(id);
        this.getMonthList().forEach((month) -> {
            arr.add(productData.containsKey(month) ? productData.get(month) : 0);
        });
        return new Gson().toJson(arr);
    }
}
