
import gnext.bean.DatabaseServer;
import gnext.controller.company.CompanyController;
import gnext.model.authority.UserModel;
import gnext.security.annotation.SecureMethod;
import gnext.security.annotation.SecurePage;
import gnext.service.DatabaseServerService;
import gnext.util.JsfUtil;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Date;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.event.AjaxBehaviorEvent;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.math.NumberUtils;
import org.primefaces.component.selectonemenu.SelectOneMenu;
import org.primefaces.context.RequestContext;

/**
 *
 * @author hungpham
 * @since Sep 14, 2017
 */
@ManagedBean(name = "dbServerController", eager = true)
@SessionScoped()
@SecurePage(module = SecurePage.Module.SYSTEM, require = false)
public class DBServerController implements Serializable{

    private static final long serialVersionUID = -1996149560343606649L;
    
    @EJB private DatabaseServerService databaseServerService;
    
    @ManagedProperty(value = "#{companyController}") @Getter @Setter 
    private CompanyController companyController;
    
    @Getter @Setter
    private DatabaseServer bean = new DatabaseServer();
    @Getter @Setter
    private boolean selected;

    @SecureMethod(value=SecureMethod.Method.NONE, require = false)
    public void showEditForm(DatabaseServer db){
        if(db == null || db.getDatabaseServerId() == null){
            this.setSelected(false);
            this.setBean(new DatabaseServer());
        } else {
            this.setSelected(true);
            this.setBean(db);
        }
    }    
    
    private boolean isValid(){
        String name = bean.getDatabaseServerName().trim();
        for(DatabaseServer db : companyController.getDatabaseServers()){
            if(db.getDatabaseServerName().trim().equals(name)){
                JsfUtil.addErrorMessage("Server name is uniqe!");
                return false; // Name is unique
            }
        }
            
        try {
            // check connect is valid
            String driver = "com.mysql.jdbc.Driver";
            String connection = String.format("jdbc:mysql://%s:%s/information_schema",bean.getDatabaseServerHost(), bean.getDatabaseServerPort() );
            Class.forName(driver);
            Connection con = DriverManager.getConnection(connection, bean.getDatabaseServerUsername(), bean.getDatabaseServerPassword());
            if (con != null && !con.isClosed()) {
                con.close();
            }
            
        } catch (ClassNotFoundException | SQLException ex) {
            JsfUtil.addErrorMessage("Cannot connect to Database server!");
            return false;
        }
        return true;
    }
    
    @SecureMethod(value=SecureMethod.Method.NONE, require = false)
    public void create(){
        if(!this.selected && isValid()){
            addDatabaseServer();
//            bean.setDatabaseServerId(0);
//            bean.setCreatedTime(new Date());
//            bean.setUpdatedTime(new Date());
//            bean.setCreatorId(UserModel.getLogined().getUserId());
//            bean.setUpdatedId(UserModel.getLogined().getUserId());

//            databaseServerService.create(bean);
            if(bean.getDatabaseServerId() == null || bean.getDatabaseServerId() == 0){
                JsfUtil.addErrorMessage("Error");
            }else{
                bean = new DatabaseServer();
                companyController.loadDatabaseServers();
                JsfUtil.addSuccessMessage("Success");
                JsfUtil.executeClientScript("PF('dbServerManager').hide()");
            }
        }
        if(this.selected && this.bean.getDatabaseServerId() != null) {
            this.companyController.getCompanyModel().getCompany().setDatabaseServerId(this.bean.getDatabaseServerId());
            JsfUtil.executeClientScript("PF('dbServerManager').hide()");
        }
    }

    @SecureMethod(value=SecureMethod.Method.NONE, require = false)
    public void edit(){
        if(!this.selected && isValid()){
            if(this.bean.getDatabaseServerId() != null) {
                bean.setUpdatedTime(new Date());
                bean.setUpdatedId(UserModel.getLogined().getUserId());
                databaseServerService.edit(bean);                
            } else {
                addDatabaseServer();
            }
            if(bean.getDatabaseServerId() == null || bean.getDatabaseServerId() == 0){
                JsfUtil.addErrorMessage("Error");
            }else{
                bean = new DatabaseServer();
                companyController.loadDatabaseServers();
                JsfUtil.addSuccessMessage("Success");
                JsfUtil.executeClientScript("PF('dbServerManager').hide()");
            }
        }
        if(this.selected && this.bean.getDatabaseServerId() != null) {
            this.companyController.getCompanyModel().getCompany().setDatabaseServerId(this.bean.getDatabaseServerId());
            JsfUtil.executeClientScript("PF('dbServerManager').hide()");
        }
    }

    private void addDatabaseServer() {
        bean.setDatabaseServerId(0);
        bean.setCreatedTime(new Date());
        bean.setUpdatedTime(new Date());
        bean.setCreatorId(UserModel.getLogined().getUserId());
        bean.setUpdatedId(UserModel.getLogined().getUserId());
        databaseServerService.create(bean);
    }

    public void onSelectChanged(final AjaxBehaviorEvent event) {
        SelectOneMenu change = (SelectOneMenu) event.getSource();
        String val = String.valueOf(change.getValue());
        boolean isOpenDailog = "0".equals(val);
        if(!isOpenDailog) return;
        this.setSelected(false);
        this.setBean(new DatabaseServer());
        RequestContext.getCurrentInstance().addCallbackParam("isOpenDailog", isOpenDailog);
    }

    public void onChangedUpdateDBId(final AjaxBehaviorEvent event) {
        SelectOneMenu change = (SelectOneMenu) event.getSource();
        String val = String.valueOf(change.getValue());
        if(NumberUtils.isDigits(val)) return;
        this.companyController.getCompanyModel().getCompany().setDatabaseServerId(Integer.valueOf(val));
    }

    public void resetDatabaseServer(final AjaxBehaviorEvent event) {
        SelectOneMenu change = (SelectOneMenu) event.getSource();
        String val = String.valueOf(change.getValue());
        if(!NumberUtils.isDigits(val)) return;
        if(Integer.valueOf(val) <= 0) {
            this.setSelected(false);
            this.setBean(new DatabaseServer());
        } else {
            this.setBean(this.databaseServerService.findById(Integer.valueOf(val)));
            this.companyController.getCompanyModel().getCompany().setDatabaseServerId(this.getBean().getDatabaseServerId());
        }
    }
}
