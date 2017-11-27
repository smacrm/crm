/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.mailapi.util.items;

import gnext.dbutils.model.MailData;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author daind
 * {@link #crmcloud-web/gnext.model.search.SearchField}
 */
public class SearchField extends SearchBase {
    // Danh sách ánh xạ các cột cần tìm kiếm.
    private static final Map<String, String> FIELDS = new HashMap<>();
    static {
        FIELDS.put("filter_by_title",           "getMail_data_subject");
        FIELDS.put("filter_by_mail_sender",     "getMail_data_from_address");
        FIELDS.put("filter_by_mail_sender_name","getMail_data_from_name");
        FIELDS.put("filter_by_mail_content",    "getMail_data_body");
        FIELDS.put("filter_by_receive_address", "getMail_data_to");
        FIELDS.put("filter_by_cc_address",      "getMail_data_cc");
        FIELDS.put("filter_by_mail_size",       "getMail_data_size");
    }
    
    @Setter @Getter private String name; 
    @Setter @Getter private String condition; // AND, OR
    @Setter @Getter private String value;
    
    @Override
    public void doSearch(final MailData md) {
        _parse(getData(md));
        setOperator(condition);
    }
    
    private void _parse(final String data) {
        String field_value = String.valueOf(value);
        if(field_value == null) field_value = "";
        switch (operator) {
            case "%":
                this.found = data.toLowerCase().trim().contains(field_value.toLowerCase().trim());
                break;
            case "%-":
                this.found = data.toLowerCase().trim().endsWith(field_value.toLowerCase().trim());
                break;
            case "-%":
                this.found = data.toLowerCase().trim().startsWith(field_value.toLowerCase().trim());
                break;
            case "!%":
                this.found = !data.toLowerCase().trim().contains(field_value.toLowerCase().trim());
                break;    
            case "bl":
                this.found = field_value.trim().isEmpty();
                break;
            case "nbl":
                this.found = !field_value.trim().isEmpty();
                break;    
            case "=":
                this.found = data.trim().equals(field_value.trim());
                break;
            default:
                this.found = false;
        }
    }
    
    private String getData(final MailData md) {
        String data = null; Class noparams[] = {};
        for (Map.Entry<String, String> entry : FIELDS.entrySet()) {
            if(entry.getKey().equals(name)) {
                try {
                    Method method = md.getClass().getDeclaredMethod(entry.getValue(), noparams);
                    data = String.valueOf(method.invoke(md));
                } catch (Exception e) {
                    data = null;
                }
                break;
            }
        }
        
        return data == null || data.isEmpty() ? "" : data;
    }
}
