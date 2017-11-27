package gnext.model.report.export;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author hungpham
 * @since Nov 30, 2016
 */
public class Month {
    @Setter @Getter private Integer current = 0;
    @Setter @Getter private Integer last = 0;
    
    public Month() {
    }

    public Month(int current, int last) {
        this.current = current;
        this.last = last;
    }
    
    public String getPercent(){
        double dLast = this.last;
        double dCurrent = this.current;
        if(dLast == 0 && dCurrent == 0) return null;
        else if(dLast == 0) return "100";
        else if(dCurrent == 0) return null;
        return String.format( "%.2f", (dCurrent * 100 / dLast) * 100 / 100);
    }
    
    public Month clone(){
        return new Month(this.current, this.last);
    }
    
    public Month merge(Month m){
        if(m != null){
            this.current += m.getCurrent();
            this.last += m.getLast();
        }
        return this;
    }

    @Override
    public String toString() {
        return String.format("[C] %d [L] %d", this.current, this.last);
    }
}
