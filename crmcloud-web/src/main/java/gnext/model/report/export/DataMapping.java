package gnext.model.report.export;

import gnext.bean.report.ReportItem;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author hungpham
 * @since Nov 30, 2016
 */
public class DataMapping extends HashMap<String, Map<Integer, Month>> { // itemId -> {month -> [current, last]}

    @Setter @Getter private int startMonth;
    final private List<Integer> months = new ArrayList<>();
    
    public List<Integer> getMonthList() {
        if(months.isEmpty()){
            Calendar c = Calendar.getInstance();
            c.set(Calendar.MONTH, startMonth);
            for (int i = 0; i < 12; i++) {
                months.add(c.get(Calendar.MONTH) + 1);
                c.add(Calendar.MONTH, 1);
            }
        }
        return months;
    }
    
    public void push(final ReportItem item){
        String id = item.getKey().getId();
        int month = item.getKey().getMonth();
        
        if( !this.containsKey(id)){
            final Map<Integer, Month> rowItem = new HashMap<>();
            getMonthList().forEach((m) -> {
                rowItem.put(m, null);
            });
            this.put(id, rowItem);
        }
        this.get(id).put(month, new Month(item.getCurrent(), item.getLast()));
    }
    
    
    @Override
    public void clear(){
        super.clear();
        months.clear();
    }
}
