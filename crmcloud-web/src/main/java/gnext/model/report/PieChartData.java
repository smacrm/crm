package gnext.model.report;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import java.util.HashMap;
import java.util.Map;
import lombok.Setter;
import org.apache.commons.lang3.math.NumberUtils;

/**
 *
 * @author hungpham
 * @since Nov 29, 2016
 */
public class PieChartData extends HashMap<String, Integer>{ //ItemId -> current
    @Setter private Map<Integer, String> labelMapping = new HashMap<>();
    
    public String getLabels(){
        final JsonArray arr = new JsonArray();
        this.forEach((key, value) -> {
            arr.add(labelMapping.get(NumberUtils.toInt(key)));
        });
        return new Gson().toJson(arr);
    }
    
    public String getValues(){
        if(this.isEmpty()) return null;
        final JsonArray arr = new JsonArray();
        this.forEach((key, value) -> {
            arr.add(value);
        });
        return new Gson().toJson(arr);
    }
}
