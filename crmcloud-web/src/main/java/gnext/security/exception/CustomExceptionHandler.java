/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.security.exception;

import gnext.util.JsfUtil;
import java.io.IOException;
import java.io.PrintWriter;
import javax.faces.FacesException;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerWrapper;
import javax.faces.context.FacesContext;
import javax.faces.event.ExceptionQueuedEvent;
import java.util.Iterator;
import java.util.Map;
import javax.faces.application.ProjectStage;
import javax.servlet.http.HttpServletResponse;
import org.primefaces.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author daind
 */
public class CustomExceptionHandler extends ExceptionHandlerWrapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomExceptionHandler.class);

    private ExceptionHandler exceptionHandler;
    public CustomExceptionHandler(ExceptionHandler exceptionHandler) { this.exceptionHandler = exceptionHandler; }

    @Override
    public ExceptionHandler getWrapped() { return exceptionHandler; }

    @Override
    public void handle() throws FacesException {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context == null || context.getResponseComplete()) return;
        
        Iterable<ExceptionQueuedEvent> exceptionQueuedEvents = getUnhandledExceptionQueuedEvents();
        if (exceptionQueuedEvents != null && exceptionQueuedEvents.iterator() != null) {
            Iterator<ExceptionQueuedEvent> unhandledExceptionQueuedEvents = getUnhandledExceptionQueuedEvents().iterator();
            if (unhandledExceptionQueuedEvents.hasNext()) {
                try {
                    Throwable throwable = unhandledExceptionQueuedEvents.next().getContext().getException();
                    unhandledExceptionQueuedEvents.remove();
                    Throwable rootCause = getRootCause(throwable);
                    
                    // print exception in development stage
                    if (context.getApplication().getProjectStage() == ProjectStage.Development) rootCause.printStackTrace();

                    if (context.getPartialViewContext().isAjaxRequest()) {
                        handleAjaxException(context, rootCause);
                    } else {
                        handleRedirect(context, rootCause, false);
                    }
                } catch (Exception ex) {
                    LOGGER.error("Could not handle exception!", ex);
                }
            }
            while (unhandledExceptionQueuedEvents.hasNext()) {
                unhandledExceptionQueuedEvents.next();
                unhandledExceptionQueuedEvents.remove();
            }
        }
    }

    protected void handleAjaxException(FacesContext context, Throwable rootCause) throws Exception {
        Map<String, Object> requestMap = context.getExternalContext().getRequestMap();
        
        requestMap.put("error-message", rootCause.getMessage());
        requestMap.put("error-stack", rootCause.getStackTrace());
        
        //RequestContext.getCurrentInstance().update("errorDialog");
        //RequestContext.getCurrentInstance().execute("try{ PF('errorDialog').show(); }catch(e){ console.error('"+rootCause.getMessage()+"') }");
    }
    
    protected void handleRedirect(FacesContext context, Throwable rootCause, boolean responseResetted) throws IOException {
        // TODO: redirect to common page.
    }
    
    private void redirectWithinAjax(String redirectURL) throws IOException {
        HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?><partial-response><redirect url=\"").append(redirectURL).append("\"></redirect></partial-response>");
        response.setHeader("Cache-Control", "no-cache");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/xml");
        PrintWriter pw = response.getWriter();
        pw.println(sb.toString());
        pw.flush();
    }
}
