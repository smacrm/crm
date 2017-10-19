package gnext.model.report;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author hungpham
 * @since Nov 29, 2016
 */
public class AbstractChartData<K, V> extends HashMap<K, V>{
    @Setter @Getter private int startMonth;
    
    public List<Integer> getMonthList(){
        List<Integer> months = new ArrayList<>();
        Calendar c = Calendar.getInstance();
        c.set(Calendar.MONTH, startMonth);
        for(int i = 0; i< 12; i++){
            months.add(c.get(Calendar.MONTH)+1);
            c.add(Calendar.MONTH, 1);
        }
        return months;
    }
    
}
