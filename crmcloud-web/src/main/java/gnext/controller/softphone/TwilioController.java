package gnext.controller.softphone;

import com.google.gson.Gson;
import com.twilio.sdk.client.TwilioCapability;
import gnext.bean.softphone.Twilio;
import gnext.bean.softphone.TwilioConfig;
import gnext.controller.AbstractController;
import gnext.controller.common.LayoutController;
import gnext.controller.issue.IssueController;
import gnext.bean.issue.CustTargetInfo;
import gnext.model.authority.UserModel;
import gnext.model.search.SearchFilter;
import gnext.security.annotation.SecureMethod;
import gnext.security.annotation.SecurePage;
import gnext.service.softphone.TwilioConfigService;
import gnext.service.softphone.TwilioService;
import gnext.util.JsfUtil;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hungpham
 * @since Dec 22, 2016
 */
@ManagedBean
@SecurePage(module = SecurePage.Module.SYSTEM)
@SessionScoped()
public class TwilioController extends AbstractController implements Serializable {

    private static final long serialVersionUID = 1L;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @EJB private TwilioConfigService twilioConfigService;
    @EJB private TwilioService twilioService;
    @Getter private TwilioConfig twilioConfig;
    
    @ManagedProperty(value = "#{layout}")
    @Getter @Setter private LayoutController layout;
    
    @Setter @Getter private String callSid;
    @Setter @Getter private String custPhoneNumber;
    @Setter @Getter private Integer issueId;
    
    @Setter @Getter private List<Twilio> list;
    
    private String exportFileName;
    @Setter @Getter private Integer itemViewIndex = 0;
    @Setter @Getter private Twilio twilio;

    @PostConstruct
    public void init() {
        List<TwilioConfig> availableList = twilioConfigService.getByCompanyId(UserModel.getLogined().getCompanyId());
        Integer userId = UserModel.getLogined().getUserId();
        for(TwilioConfig c : availableList){
            String allowMemberStr = c.getAllowMemberList();
            if(!StringUtils.isEmpty(allowMemberStr)) {
                List<Double> allowMemberList = new Gson().fromJson(allowMemberStr, List.class);
                if(isContains(allowMemberList, userId)) {
                    this.twilioConfig = c;
                    break;
                }
            }
        }
        
        try {
            exportFileName = URLEncoder.encode("Twilioデータ履歴", "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            logger.error(ex.getMessage(), ex);
        }
    }
    
    private boolean isContains(List<Double> memberListId, Integer memberId){
        for(Double item : memberListId){
            if(item.intValue() == memberId.intValue()){
                return true;
            }
        }
        return false;
    }

    public String getToken() {
        return getTokenForAgent(UserModel.getLogined().getUsername());
    }

    public String getTokenForAgent(String user) {
        if(this.twilioConfig != null){
            TwilioCapability capability = new TwilioCapability(twilioConfig.getAccountSid(), twilioConfig.getAccountAuthToken());
            capability.allowClientOutgoing(twilioConfig.getApplicationSid());
            capability.allowClientIncoming(user);
            try {
                return capability.generateToken();
            }catch(Exception e) {}
        }
        return null;
    }

    @SecureMethod(value = SecureMethod.Method.NONE, require = false)
    public void doCreateIssue() {
        IssueController issueController = JsfUtil.getManagedBean("issueController", IssueController.class);
        issueController.create(null);
        
        CustTargetInfo target = new CustTargetInfo();
        target.setCustFlagType(this.custPhoneNumber.length() > 10 ? (short) 3 : (short) 2);
        target.setCustTargetData(this.custPhoneNumber);
        target.setCustomer(issueController.getIssue().getCustomerList().get(0));
        target.setCompany(UserModel.getLogined().getCompany());
        
        issueController.getIssue().getCustomerList().get(0).getCustTargetInfoList().add(target);
        issueController.getIssue().setTwilioCallSid(callSid);
    }
    
    private void showForm(){
        layout.setCenter("/modules/system/twilio/form.xhtml");
    }
    
    @SecureMethod(SecureMethod.Method.SEARCH)
    public void reload(){
        load();
    }
    
    @SecureMethod(value = SecureMethod.Method.INDEX, require = false)
    public void load(){
        layout.setCenter("/modules/system/twilio/list.xhtml");
        
        this.list = twilioService.findByCompanyId(UserModel.getLogined().getCompanyId());
        if(this.list == null) this.list = new ArrayList<>();
    }
    
    @SecureMethod(SecureMethod.Method.DOWNLOAD)
    public String getExportFileName() {
        return exportFileName;
    }
    
    @SecureMethod(SecureMethod.Method.VIEW)
    public void view(Twilio c, Integer itemViewIndex){
        this.itemViewIndex = itemViewIndex;
        this.twilio = c;
        showForm();
    }
    
    /**
     * Show form by item index (next/prev)
     * @param itemViewIndex 
     */
    @SecureMethod(SecureMethod.Method.VIEW)
    public void viewByIndex(Integer itemViewIndex){
        if(list.size() > itemViewIndex){
            twilio = list.get(itemViewIndex);
            this.view(twilio, itemViewIndex);
        }
    }
    
    @Override
    protected void doSearch(SearchFilter filter) {
        String query = filter != null ? filter.getQuery() : "";
        
        list = twilioService.search(UserModel.getLogined().getCompanyId(), query);
    }
}
