package gnext.bean.report;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author hungpham
 * @since Nov 30, 2016
 */
public class NestProduct implements Serializable{
    @Setter @Getter private String id;

    public NestProduct() {
    }

    public NestProduct(String id) {
        this.id = id;
    }
    
}
