/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.dbutils.util;

import gnext.dbutils.processor.Column;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author daind
 */
public final class FieldUtils {
    private FieldUtils() {}
    
    /**
     * loại bỏ cột nào là primary-key trong danh sách các cột lấy được theo bảng.
     * @param columnNames
     * @param clazz 
     */
    public static void removeAutoGene(LinkedList<String> columnNames, final Class<?> clazz) {
        List<Field> fields = new ArrayList<>(); FieldUtils.recusiveFields(fields, clazz);
        LinkedList<String> removing = new LinkedList<>();
        for(Field field : fields) {
            if(!field.isAnnotationPresent(Column.class)) continue;
            Column column = field.getAnnotation(Column.class);
            if(!column.generated()) continue;
            for(String columnName : columnNames) {
                if(column.name().contains(columnName)) removing.add(columnName);
            }
        }
        columnNames.removeAll(removing);
    }
    
    /**
     * Lấy danh sách các fields trong class kể cả lớp cha.
     * @param fields
     * @param clazz
     * @return 
     */
    public static List<Field> recusiveFields(List<Field> fields, Class<?> clazz) {
        fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
        if (clazz.getSuperclass() != null) fields = recusiveFields(fields, clazz.getSuperclass());
        return fields;
    }
    
    public static boolean checkFieldNameWithColumn(String annotationName, String columnName) {
        String[] fNames = annotationName.split(",");
        for (String fn : fNames) {
            if (fn.trim().equalsIgnoreCase(columnName.trim())){
                return true;
            }
        }
        return false;
    }
}
