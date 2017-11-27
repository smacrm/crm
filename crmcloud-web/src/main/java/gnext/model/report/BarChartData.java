package gnext.model.report;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import java.util.AbstractMap;

/**
 *
 * @author hungpham
 * @since Nov 29, 2016
 */
public class BarChartData extends AbstractChartData<Integer, AbstractMap.SimpleEntry<Integer, Integer>>{ //Month -> {current, last}
    
    public String getLabels(){
        final JsonArray arr = new JsonArray();
        this.getMonthList().forEach((m) -> {
            arr.add(m + "月");
        });
        return new Gson().toJson(arr);
    }
    
    public String getCurrentValues(){
        final JsonArray arr = new JsonArray();
        
        this.getMonthList().forEach((m) -> {
            arr.add(this.containsKey(m) ? this.get(m).getKey() : 0);
        });
        
        return new Gson().toJson(arr);
    }
    
    public String getLastValues(){
        final JsonArray arr = new JsonArray();
        this.getMonthList().forEach((m) -> {
            arr.add(this.containsKey(m) ? this.get(m).getValue() : 0);
        });
        return new Gson().toJson(arr);
    }
}
