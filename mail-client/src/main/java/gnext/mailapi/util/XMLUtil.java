/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.mailapi.util;

import gnext.dbutils.util.StringUtil;
import java.io.File;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 *
 * @author tungdt
 */
public class XMLUtil {
    private XMLUtil() {}
    
    /**
     * 
     * @param <B>
     * @param clazz
     * @param xmlfilePath
     * @return 
     * @throws java.lang.Exception 
     */
    public static <B> B xmlToBean(Class<B> clazz, String xmlfilePath) throws Exception {
        if(clazz == null) throw new NullPointerException("Clazz can not null");
        if(StringUtil.isEmpty(xmlfilePath)) throw new NullPointerException("Xml file path can not null or empty");
        File file = new File(xmlfilePath);
        JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        return (B) jaxbUnmarshaller.unmarshal(file);
    }
    
    /**
     * 
     * @param bean
     * @param xmlfilePath 
     * @throws java.lang.Exception 
     */
    public static void beanToXml(Object bean, String xmlfilePath) throws Exception {
        if(bean == null) throw new NullPointerException("Bean can not null");
        if(StringUtil.isEmpty(xmlfilePath)) throw new NullPointerException("Xml file path can not null or empty");
        
        File file = new File(xmlfilePath);
        JAXBContext jaxbContext = JAXBContext.newInstance(bean.getClass());
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, false);
        jaxbMarshaller.marshal(bean, file);
    }

}
