/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.util;

import gnext.bean.issue.Issue;
import gnext.util.DateUtil.SYMBOL;
import gnext.utils.AddToElasticSearch;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.persistence.Column;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author daind
 */
public final class ClassUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClassUtil.class);
    private ClassUtil() {  }

    /**
     * Quýet tất cả các fields có trong map để đẩy vào object.
     * @param <T>
     * @param t
     * @param mapFields
     * @param lang
     * @return 
     */
    public static <T> Map<String, String> createObject(T t, Map<String, String> mapFields, String lang) {
        if(mapFields == null || mapFields.isEmpty()) return null;
        Map<String, String> remaining = (HashMap)((HashMap) mapFields).clone();
        Class<?> clazz = t.getClass();
        Field[] fields = clazz.getDeclaredFields();
        for(Field f : fields) {
            if(f == null) continue;
            String nf = null;
            if(f.getAnnotation(Column.class) != null){
                nf = f.getAnnotation(Column.class).name();
            } else if(f.getAnnotation(AddToElasticSearch.class) != null){
                nf = f.getAnnotation(AddToElasticSearch.class).name();
            }
            if(nf == null || nf.isEmpty()) continue;
            
            //Xu ly truong hop dac biet cho Issue Object
            // created_time ~ issue_created_time
            // updated_time ~ issue_updated_time
            if(t instanceof Issue){
                if ( "created_time".equals(nf) ){
                    nf = "issue_created_time";
                }else if ( "updated_time".equals(nf) ){
                    nf = "issue_updated_time";
                }
            }
            
            Iterator<Map.Entry<String,String>> iter = remaining.entrySet().iterator();
            if(!iter.hasNext()) break;
            while (iter.hasNext()) {
                Map.Entry<String,String> mapField = iter.next();
                String fieldCode = mapField.getKey();
                if(!fieldCode.equals(nf)) continue;
                ClassUtil.amazingSetField(t, f, mapField.getValue(), lang);
                iter.remove();
            }
        }
        return remaining;
    }
    
    /***
     * Tùy thuộc vào loại {@link Field} sẽ đặt giá trị tương ứng từ value.
     * @param obj
     * @param f
     * @param value 
     */
    public static void amazingSetField(Object obj, Field f, String value, String lang) {
        try {
            f.setAccessible(true);
            if (String.class.isAssignableFrom(f.getType())) { f.set(obj, value); return; }
            if (Integer.class.isAssignableFrom(f.getType())) { f.set(obj, NumberUtils.toInt(value)); return; }
            if (Short.class.isAssignableFrom(f.getType())) { f.set(obj, NumberUtils.toShort(value)); return; }
            if (Float.class.isAssignableFrom(f.getType())) { f.set(obj, NumberUtils.toFloat(value)); return; }
            if (Boolean.class.isAssignableFrom(f.getType())) { try{f.set(obj, Boolean.valueOf(value));}catch(Exception e){} return; }
            if(Date.class.isAssignableFrom(f.getType()) && !StringUtils.isEmpty(value)) {
                String dateFormat = DateUtil.getDateTime(lang, SYMBOL.SLASH, true);
                f.set(obj, new SimpleDateFormat(dateFormat).parse(value)); return;
            }
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }
    }
    
    /**
     * Scans all classes accessible from the context class loader which belong
     * to the given package and subpackages.
     *
     * @param packageName The base package
     * @return The classes
     * @throws ClassNotFoundException
     * @throws IOException
     */
    public static List<Class<?>> getClasses(String packageName)
            throws ClassNotFoundException, IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assert classLoader != null;
        String path = packageName.replace('.', '/');
        Enumeration resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<>();
        while (resources.hasMoreElements()) {
            URL resource = (URL) resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }
        List<Class<?>> classes = new ArrayList<>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName));
        }
        return classes;
    }

    /**
     * Recursive method used to find all classes in a given directory and
     * subdirs.
     *
     * @param directory The base directory
     * @param packageName The package name for classes found inside the base
     * directory
     * @return The classes
     * @throws ClassNotFoundException
     */
    private static List findClasses(File directory, String packageName)
            throws ClassNotFoundException {
        List classes = new ArrayList();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file,
                        packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                classes.add(Class.forName(packageName + '.'
                        + file.getName().substring(0, file.getName().length() - 6)));
            }
        }
        return classes;
    }
    
    public static <T> String findColumnByFieldName(Class<T> clazz, String fieldName) throws NoSuchFieldException{
        if(StringUtils.isEmpty(fieldName)) return "";
        
        Field field = clazz.getDeclaredField(fieldName);
        Column column = field.getAnnotation(Column.class);
        
        return column != null ? column.name() : "";
    }
}
