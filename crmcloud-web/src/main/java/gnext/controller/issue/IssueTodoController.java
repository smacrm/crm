package gnext.controller.issue;

import gnext.controller.common.LayoutController;
import gnext.controller.customize.RenderController;
import gnext.bean.issue.IssueTodo;
import gnext.model.authority.UserModel;
import gnext.security.annotation.SecureMethod;
import gnext.security.annotation.SecurePage;
import gnext.service.issue.IssueTodoService;
import gnext.util.DateUtil;
import gnext.util.HTTPResReqUtil;
import gnext.util.InterfaceUtil;
import gnext.util.JsfUtil;
import gnext.util.ResourceUtil;
import gnext.util.StatusUtil;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hungpham
 * @since Jan 16, 2017
 */
@ManagedBean(name = "issueTodoController")
@SessionScoped()
@SecurePage(module = SecurePage.Module.ISSUE)
public class IssueTodoController implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(IssueTodoController.class);
    
    @ManagedProperty(value = "#{layout}")
    @Getter @Setter
    private LayoutController layout;

    @ManagedProperty(value = "#{renderController}")
    @Getter @Setter
    private RenderController renderController;

    @ManagedProperty(value = "#{issueController}")
    @Getter @Setter
    private IssueController issueController;

    @EJB private IssueTodoService issueTodoService;
    
    @Getter @Setter
    private IssueTodo issueTodo = new IssueTodo();;

    @Getter
    private List<IssueTodo> list = new ArrayList<>();
    
    @PostConstruct
    public void init(){
        reload();
    }
    
    @SecureMethod(value=SecureMethod.Method.SEARCH)
    public void reload() {
        list = issueTodoService.getTodoListByUser(UserModel.getLogined().getMember());
    }
 
    @SecureMethod(value=SecureMethod.Method.CREATE)
    public void create() {
        try {
            String id = HTTPResReqUtil.getRequestParameter("todoId");
            this.issueTodo = new IssueTodo();
            this.issueTodo.setTodoIssueId(this.issueController.getIssue());
        } catch(NumberFormatException ex) {
            logger.error("IssueTodoController.create()", ex.getMessage());
        }
    }
    

    @SecureMethod(value=SecureMethod.Method.CREATE)
    public void insert() {
        try {
            if(!chkFormRequest()) return;
            if(this.issueTodo.getTodoIssueId() == null) {
                this.issueTodo.setTodoIssueId(this.issueController.getIssue());
            }
            this.issueTodo.setTodoDeleted(StatusUtil.UN_DELETED);
            this.issueTodo.setTodoStatus(InterfaceUtil.STATUS.INCOMPLETE);
            this.issueTodo.setCreator(UserModel.getLogined().getMember());
            this.issueTodo.setCreatedTime(DateUtil.now());
            this.issueTodo.setUpdatedId(UserModel.getLogined().getUserId());
            this.issueTodo.setUpdatedTime(DateUtil.now());
            this.issueTodo = this.issueTodoService.create(this.issueTodo);

            this.issueController.getIssue().getIssueTodoList().add(this.issueTodo);
            JsfUtil.getResource().alertMsgInfo("label.tab_todo_list", ResourceUtil.BUNDLE_ISSUE_NAME, "label.issue.add", ResourceUtil.BUNDLE_ISSUE_NAME);
            init();
        } catch(Exception e) {
            logger.error(e.getMessage(), e);
            return;
        }
        HTTPResReqUtil.setCloseDialog();
    }

    @SecureMethod(value=SecureMethod.Method.UPDATE)
    public void edit(IssueTodo issueTodo) {
        this.issueTodo = issueTodo;
    }

    @SecureMethod(value=SecureMethod.Method.UPDATE)
    public void update() {
        try {
            if(!chkFormRequest()) return;
            if(this.issueTodo == null || this.issueTodo.getTodoId()== null) {
                return;
            }
            this.issueTodo.setUpdatedId(UserModel.getLogined().getUserId());
            this.issueTodo.setUpdatedTime(DateUtil.now());
            this.issueTodo = this.issueTodoService.edit(this.issueTodo);

            this.mergeToIssue();
            JsfUtil.getResource().alertMsgInfo("label.tab_todo_list", ResourceUtil.BUNDLE_ISSUE_NAME, "label.issue.update", ResourceUtil.BUNDLE_ISSUE_NAME);
            init();
        } catch(Exception e) {
            logger.error(e.getMessage(), e);
            return;
        }
        HTTPResReqUtil.setCloseDialog();
    }
    
    /**
     * Merge current data bean to issue object on issueController.issue
     */
    private void mergeToIssue(){
        for(int i = 0; i< this.issueController.getIssue().getIssueTodoList().size(); i++){
            if(this.issueController.getIssue().getIssueTodoList().get(i).equals(this.issueTodo)){
                this.issueController.getIssue().getIssueTodoList().set(i, this.issueTodo);
                break;
            }
        }
    }

    @SecureMethod(value=SecureMethod.Method.DELETE)
    @SuppressWarnings({"BoxedValueEquality", "NumberEquality"})
    public void delete() {
        try {
            String ids = HTTPResReqUtil.getRequestParameter("todoId");
            if(!NumberUtils.isDigits(ids) || this.issueController.getIssue().getIssueTodoList() == null) return;
            for(IssueTodo todo:this.issueController.getIssue().getIssueTodoList()) {
                if(todo.getTodoId() != Integer.valueOf(ids)) continue;
                this.issueTodoService.remove(todo);
                
                // update on left bar
                for (Iterator<IssueTodo> iterator = this.list.iterator(); iterator.hasNext();) {
                    IssueTodo o = iterator.next();
                    if (o.getTodoId().equals(todo.getTodoId())) {
                        iterator.remove();
                    }
                }
                
                this.issueController.getIssue().getIssueTodoList().remove(todo);
                break;
            }
        } catch(Exception e) {
            logger.error(e.getMessage(), e);
            return ;
        }
        JsfUtil.getResource().alertMsgInfo("label.tab_todo_list", ResourceUtil.BUNDLE_ISSUE_NAME, "label.issue.delete", ResourceUtil.BUNDLE_ISSUE_NAME);
    }
    
    @SecureMethod(value=SecureMethod.Method.UPDATE)
    public void updateStatus(Integer id, Short status){
        this.issueTodoService.updateStatusByTodoId(id, status);
        
        // update issue status on current issue
        if(this.issueController.getIssue() != null){
            this.issueController.getIssue().getIssueTodoList().forEach((item) -> {
                if(item.getTodoId().equals(id)){
                    item.setTodoStatus(status);
                    return;
                }
            });
        }
        
        // update issue status on left bar
        reload();
//        this.list.forEach((item) -> {
//            if(item.getTodoId().equals(id)){
//                item.setTodoStatus(status);
//                return;
//            }
//        });
    }


    /** TODOステータスを変更
     * @param todo */
    @SecureMethod(value=SecureMethod.Method.UPDATE)
    public void updateStatus(IssueTodo todo) {
        try {
            this.issueTodoService.edit(todo);
            // update issue status on left bar
            reload();
//            this.list.forEach((item) -> {
//                if(item.getTodoId().equals(todo.getTodoId())){
//                    item.setTodoStatus(todo.getTodoStatus());
//                    return;
//                }
//            });
            /** TODOリストを取得 */
            this.mergeToIssue();
        } catch(Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
    
    private boolean chkFormRequest() {
        if(this.issueTodo.getTodoIntendedDatetime() == null) {
            JsfUtil.getResource().alertMsg("label.plans.datetime", ResourceUtil.BUNDLE_MSG, "label.not_input", ResourceUtil.BUNDLE_ISSUE_NAME);
            return false;
        }
        if(StringUtils.isBlank(this.issueTodo.getTodoContent())) {
            JsfUtil.getResource().alertMsg("label.todo_comment", ResourceUtil.BUNDLE_ISSUE_NAME, "label.not_input", ResourceUtil.BUNDLE_ISSUE_NAME);
            return false;
        }
        if(this.issueTodo.getTodoContent() == null
                || this.issueTodo.getTodoContent().length() < 1 
                || this.issueTodo.getTodoContent().length() > 20000) {
            JsfUtil.getResource().alertMsgMaxLength("label.todo_comment", ResourceUtil.BUNDLE_ISSUE_NAME, "label.max.length", ResourceUtil.BUNDLE_ISSUE_NAME, 20000);
            return false;
        }
        return true;
    }
}
