package gnext.rest;

import java.util.Set;
import javax.ws.rs.core.Application;
/**
 *
 * @author hungpham
 * @since Nov 1, 2016
 */
@javax.ws.rs.ApplicationPath("rest")
public class ApplicationConfig extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new java.util.HashSet<>();
        try {
            // Class jsonProvider = Class.forName("org.glassfish.jersey.jackson.JacksonFeature");
             Class jsonProvider = Class.forName("org.glassfish.jersey.moxy.json.MoxyJsonFeature");
            // Class jsonProvider = Class.forName("org.glassfish.jersey.jettison.JettisonFeature");
            //resources.add(jsonProvider);
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(getClass().getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        addRestResourceClasses(resources);
        return resources;
    }

    /**
     * Do not modify addRestResourceClasses() method.
     * It is automatically populated with
     * all resources defined in the project.
     * If required, comment out calling this method in getClasses().
     */
    private void addRestResourceClasses(Set<Class<?>> resources) {
        resources.add(gnext.rest.common.IssueRestService.class);
        resources.add(gnext.rest.customize.DynamicContent.class);
        resources.add(gnext.rest.iteply.Iteply.class);
        resources.add(gnext.rest.softphone.Customer.class);
        resources.add(gnext.rest.twilio.TwilioService.class);
    }

}
