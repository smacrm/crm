/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.bean;

import java.io.Serializable;

/**
 * Giao diện chỉ mang tính đánh dấu.
 * Những Entity nào implement lớp này có ý nghĩa các dữ liệu trong DB Master sẽ được clone tới DB Child.
 * @author daind
 */
public interface CloneAllDataToDbChild extends Serializable {
    
}
