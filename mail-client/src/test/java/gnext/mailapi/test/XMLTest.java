/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.mailapi.test;

import gnext.mailapi.util.XMLUtil;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

/**
 *
 * @author tungdt
 */
public class XMLTest {

    public static void main(String[] args) throws Exception {
        Result result = JUnitCore.runClasses(XMLTest.class);

        result.getFailures().forEach((failure) -> {
            System.out.println(failure.toString());
        });
        System.out.println(result.wasSuccessful());
    }

    @Test
    public void testConvertXmlToBean() throws Exception {
        Customer customer = new Customer();
        customer.setName("TungDo");
        customer.setAge(23);
        customer.setId(1);
        assertEquals(customer.getName(), XMLUtil.xmlToBean(Customer.class, "src/main/resources/customer.xml").getName());
    }
    
    public void testConvertBeanToXml() throws Exception {
        Customer customer1 = new Customer();
        customer1.setName("Beo");
        customer1.setAge(24);
        customer1.setId(2);
        XMLUtil.beanToXml(null, "src/main/resources/customer1.xml");
    }
}
