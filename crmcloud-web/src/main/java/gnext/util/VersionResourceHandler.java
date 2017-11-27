package gnext.util;

import java.util.Date;
import javax.faces.application.Resource;
import javax.faces.application.ResourceHandler;
import javax.faces.application.ResourceHandlerWrapper;
import javax.faces.application.ResourceWrapper;

/**
 *
 * @author hungpham
 * @since Jun 22, 2017
 */
public class VersionResourceHandler extends ResourceHandlerWrapper {

    private ResourceHandler wrapped;

    private long timestamp;
    
    public VersionResourceHandler(ResourceHandler wrapped) {
        this.wrapped = wrapped;
        timestamp = (new Date()).getTime();
    }

    @Override
    public Resource createResource(String resourceName) {
        return createResource(resourceName, null, null);
    }

    @Override
    public Resource createResource(String resourceName, String libraryName) {
        return createResource(resourceName, libraryName, null);
    }

    @Override
    public Resource createResource(String resourceName, String libraryName, String contentType) {
        final Resource resource = super.createResource(resourceName, libraryName, contentType);
        
        if (resource == null) {
            return null;
        }

        return new ResourceWrapper() {

            @Override
            public String getRequestPath() {
                String requestPath = super.getRequestPath();
//                if(requestPath.contains("theme.css.xhtml")){
//                    requestPath = requestPath.substring(0, requestPath.indexOf("?ln=")+1);
//                    requestPath = requestPath + "ln=primefaces-afterdark";
//                }
                return requestPath + (requestPath.contains("?") ?  "&" : "?" ) + "ts=" + timestamp;
            }

            @Override // Necessary because this is missing in ResourceWrapper (will be fixed in JSF 2.2).
            public String getResourceName() {
                return resource.getResourceName();
            }

            @Override // Necessary because this is missing in ResourceWrapper (will be fixed in JSF 2.2).
            public String getLibraryName() {
                return resource.getLibraryName();
            }

            @Override // Necessary because this is missing in ResourceWrapper (will be fixed in JSF 2.2).
            public String getContentType() {
                return resource.getContentType();
            }

            @Override
            public Resource getWrapped() {
                return resource;
            }
        };
    }

    @Override
    public ResourceHandler getWrapped() {
        return wrapped;
    }

}
