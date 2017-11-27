/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.model;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author daind
 * @param <E> The type of the entity.
 */
public abstract class BaseModel<E> implements Serializable {

    @Getter @Setter private int rowNum;
    @Getter @Setter private boolean selected;

    public boolean isEven() {
        return getRowNum() % 2 == 0;
    }
}
