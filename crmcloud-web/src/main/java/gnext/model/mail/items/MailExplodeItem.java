/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.model.mail.items;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author daind
 */
public class MailExplodeItem implements Serializable {
    private static final long serialVersionUID = -6149881694371427391L;
    @Getter @Setter private transient Integer id;
    @Getter @Setter private String firstChar;
    @Getter @Setter private String secondChar;
    @Getter @Setter private String fieldExplode;
    @Getter @Setter private boolean trimSpace;
    public MailExplodeItem() {  }
    public MailExplodeItem(Integer id) { this.id = id; }
    
    /**
     * Hàm trả về danh sách KEY trong explode trừ KEY hiện tại.
     * @param items
     * @param key
     * @return 
     */
    public static List<String> listOfKeys(MailExplodeItem[] items, String key) {
        List<String> keys = new ArrayList<>();
        for(MailExplodeItem item : items) {
            String key_temp = item.getFirstChar(); if(StringUtils.isEmpty(key_temp)) continue;
            if(key_temp.equalsIgnoreCase(key)) continue;
            keys.add(key_temp);
        }
        return keys;
    }
}
