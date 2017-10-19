package gnext.controller.issue.test;

import gnext.security.annotation.SecurePage;
import java.io.Serializable;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author hungpham
 * @since Jul 5, 2017
 */
@ManagedBean(name = "testController", eager = true)
@SessionScoped()
@SecurePage(module = SecurePage.Module.ISSUE, require = false)
public class TestController implements Serializable{

    private static final long serialVersionUID = 7816451062774270661L;
    
    @Getter @Setter
    private String layout;
    
    @PostConstruct
    public void init(){
        layout = "/test/list.xhtml";
    }
    
    public void show(Integer itemId){
        // layout = "/test/show.xhtml";
        System.out.println("Show item " + itemId);
    }
    
    public String outcome(Integer issueId){
        return "/index.xhtml?id="+issueId+"&faces-redirect=true";
    }
    
    public void editForm(){
        
    }
    
    public void editAction(){
    
    }
    
    public void createForm(){
        
    }
    
    public void createAction(){
        
    }
}
