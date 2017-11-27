/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.dbutils.processor;

import gnext.dbutils.util.FieldUtils;
import gnext.dbutils.util.TableUtils;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.beanutils.BeanUtils;

/**
 *
 * @author daind
 */
public class AnnotationBeanProcessor {
    public AnnotationBeanProcessor() {  }
    
    public <T> T toBean(ResultSet rs, Class<T> type) throws Exception {
        List<Field> fields = new ArrayList<>(); FieldUtils.recusiveFields(fields, type);
        return this.createBean(rs, type, fields);
    }

    public <T> List<T> toBeanList(ResultSet rs, Class<T> type) throws Exception {
        List<Field> fields = new ArrayList<>(); FieldUtils.recusiveFields(fields, type);
        List<T> results = new ArrayList<>();
        if (!rs.next()) return results;
        do {
            results.add(createBean(rs, type, fields));
        } while (rs.next());
        return results;
    }

    private <T> T createBean(ResultSet rs, Class<T> type, List<Field> fields) throws Exception {
        T bean = this.newInstance(type);
        LinkedList<String> columnNames = TableUtils.getColNames(rs);
        for (String columnName : columnNames) {
            for (Field field : fields) {
                if(!field.isAnnotationPresent(Column.class)) continue;
                Column column = field.getAnnotation(Column.class);
                String annotationName = column.name();
                if(!FieldUtils.checkFieldNameWithColumn(annotationName, columnName)) continue;
                Object columnValue = rs.getObject(columnName);
                if(columnValue == null) continue;
                BeanUtils.setProperty(bean, field.getName(), columnValue);
            }
        }
        return bean;
    }

    protected <T> T newInstance(Class<T> c) throws Exception {
        try {
            return c.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new Exception( "Cannot create " + c.getName() + ": " + e.getMessage());
        }
    }
}
