/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.model.customize;

import com.google.gson.Gson;
import gnext.bean.customize.AutoFormItem;
import gnext.bean.customize.AutoFormItemGlobal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author hungpham
 */
public class Field {
    
    public interface Type{
        final public static int TEXT = 1;
        final public static int TEXTAREA = 2;
        final public static int CHECKBOX = 3;
        final public static int RADIO = 4;
        final public static int BUTTON = 5;
        final public static int DATE = 6;
        final public static int SELECT = 7;
        final public static int RADIO_GROUP = 8;
        final public static int CHECKBOX_GROUP = 9;
    }
    
    @Getter @Setter
    private String id;
    
    @Getter @Setter
    private String type;
    
    @Getter @Setter
    private Map<String, Object> label = new HashMap<>();
    
    @Getter @Setter
    private String className;
    
    @Getter @Setter
    private String name;
    
    @Getter @Setter
    private String value;
    
    @Getter @Setter
    private boolean multiple = false;
    
    @Getter @Setter
    private boolean required = false;
    
    @Getter @Setter
    private List<FieldOption> values = new ArrayList<>();
    
    public AutoFormItem getItem(){
        AutoFormItem item = new AutoFormItem();
        item.setItemType(this.getItemType(this.type));
        item.setItemRequired((short)(isRequired()? 1 : 0));
        return item;
    }
    
    public List<AutoFormItemGlobal> getItemGlobal(AutoFormItem item){
        List<AutoFormItemGlobal> itemGlobalList = new ArrayList<>();
        label.forEach((lang, value) -> {
            AutoFormItemGlobal itemGlobal = new AutoFormItemGlobal(item.getItemId(), lang);
            itemGlobal.setAutoFormItem(item);
            itemGlobal.setItemName(value.toString());
            //itemGlobal.setCompany(new Company(item.getCompanyId()));
            if(values.size() > 0){
                itemGlobal.setItemDataDefault(new Gson().toJson(values));
            }
            itemGlobalList.add(itemGlobal);
        });
        
        return itemGlobalList;
    }
    
    public void setValueFromBean(String lang, List<AutoFormItemGlobal> itemGlobalList){
        if( itemGlobalList != null ){
            itemGlobalList.forEach((item) -> {
                if(item.getAutoFormItemGlobalPK().getItemLang().equals(lang) && item.getItemDataDefault() != null){
                    values = new Gson().fromJson(item.getItemDataDefault(), List.class);
                }
            });
        }
    }
    
    public static int getItemType(String type){
        if(null != type) switch (type) {
            case "checkbox":
                return Type.CHECKBOX;
            case "text":
                return Type.TEXT;
            case "date":
                return Type.DATE;
            case "button":
                return Type.BUTTON;
            case "radio":
                return Type.RADIO;
            case "textarea":
                return Type.TEXTAREA;
            case "select":
                return Type.SELECT;
            case "radio-group":
                return Type.RADIO_GROUP;
            case "checkbox-group":
                return Type.CHECKBOX_GROUP;
            default:
                break;
        }
        
        return 0;
    }
    
    public static String getItemTypeAsString(int type){
        switch(type){
            case Type.CHECKBOX: return "checkbox";
            case Type.TEXT: return "text";
            case Type.DATE: return "date";
            case Type.BUTTON: return "button";
            case Type.RADIO: return "radio";
            case Type.TEXTAREA: return "textarea";
            case Type.SELECT: return "select";
            case Type.RADIO_GROUP: return "radio-group";
            case Type.CHECKBOX_GROUP: return "checkbox-group";
        }
        return "";
    }
    
    public String getOptions() {
        Gson gson = new Gson();
        
        return "";
    }
}
