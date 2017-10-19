package gnext.controller.system;

import com.google.gson.Gson;
import gnext.bean.Group;
import gnext.bean.config.Config;
import gnext.bean.softphone.TwilioConfig;
import gnext.controller.AbstractController;
import gnext.controller.common.LayoutController;
import gnext.controller.system.bean.GroupMemberKeyValueBean;
import gnext.model.authority.UserModel;
import gnext.model.search.SearchFilter;
import gnext.security.annotation.SecureMethod;
import gnext.security.annotation.SecurePage;
import gnext.service.CompanyService;
import gnext.service.GroupService;
import gnext.service.softphone.TwilioConfigService;
import gnext.util.JsfUtil;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.model.CheckboxTreeNode;
import org.primefaces.model.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Data Monitoring controller
 *
 * @author hungpham
 * @since 2016/10
 */
@ManagedBean(name = "dataController")
@SessionScoped
@SecurePage(module = SecurePage.Module.SYSTEM)
public class DataController extends AbstractController implements Serializable {
    
    private static final long serialVersionUID = 1L;
    private final Logger logger = LoggerFactory.getLogger(ConfigController.class);

    @ManagedProperty(value = "#{layout}")
    @Getter @Setter private LayoutController layout;
    
    @EJB private TwilioConfigService twilioConfigService;
    @EJB private GroupService groupService;
    
    @Getter @Setter private TwilioConfig twilioConfig;
    @Setter @Getter private List<TwilioConfig> list = new ArrayList<>();
    
    @Getter @Setter private TreeNode groups;
    @Getter @Setter private TreeNode[] selectedGroups;
    
    @Setter @Getter private Integer itemViewIndex = 0;
    
    @PostConstruct
    public void init(){
        list = twilioConfigService.getByCompanyId(UserModel.getLogined().getCompanyId());
    }
    
    @SecureMethod(SecureMethod.Method.UPDATE)
    public void update(TwilioConfig twilioConfig, Integer itemViewIndex){
        this.itemViewIndex = itemViewIndex;
        this.twilioConfig = twilioConfig;
        String allowMemberList = twilioConfig.getAllowMemberList();
        List<Double> memberListId = new ArrayList<>(); //must Double type because of GSON return Double type list, not Integer type list
        try{
            memberListId.addAll(new Gson().fromJson(allowMemberList, List.class));
        }catch(Exception e){}
        
        List<Double> memberIgnoredList = twilioConfigService.getAllowMemberAdded(UserModel.getLogined().getCompanyId(), twilioConfig.getTwilioId());
        
        groups = new CheckboxTreeNode();
        groups.setExpanded(true);
        List<Group> groupList = groupService.findRootGroup(UserModel.getLogined().getCompanyId());
        groupList.forEach((g) -> {
            addGroupTree(groups, g, memberListId, memberIgnoredList);
        });
        showForm();
    }
    
    /**
     * Show create/update form by item index (next/prev)
     * @param itemViewIndex 
     */
    @SecureMethod(SecureMethod.Method.UPDATE)
    public void updateByIndex(Integer itemViewIndex){
        if(list.size() > itemViewIndex){
            twilioConfig = list.get(itemViewIndex);
            this.update(twilioConfig, itemViewIndex);
        }
    }
    
    private void addGroupTree(TreeNode parent, Group g, List<Double> memberListId, List<Double> memberIgnoredList){
        TreeNode item = new CheckboxTreeNode(new GroupMemberKeyValueBean(g.getGroupId(), g.getGroupName()), parent);
        item.setType("group");
        g.getMembers().forEach((m) -> {
            CheckboxTreeNode memberItem = new CheckboxTreeNode(new GroupMemberKeyValueBean(m.getMemberId(), m.getMemberNameLast() + " " + m.getMemberNameFirst()), item);
            memberItem.setType("member");
            memberItem.setSelected(this.isContains(memberListId, m.getMemberId()));
            
            if(this.isContains(memberIgnoredList, m.getMemberId())){
                memberItem.setSelectable(false);
            }
        });
        item.setExpanded(true);
        g.getChilds().forEach((gc) -> {
            addGroupTree(item, gc, memberListId, memberIgnoredList);
        });
    }
    
    private boolean isContains(List<Double> memberListId, Integer memberId){
        for(Double item : memberListId){
            if(item.intValue() == memberId.intValue()){
                return true;
            }
        }
        return false;
    }
    
    @SecureMethod(SecureMethod.Method.UPDATE)
    public void onUpdate(){
        try{
            String memberSelectedJson = "[]";
            List<Integer> memberSelectedList = new ArrayList<>();
            if(selectedGroups != null && selectedGroups.length > 0){
                for(TreeNode item : selectedGroups){
                    if("member".equals(item.getType())){
                        memberSelectedList.add(((GroupMemberKeyValueBean)item.getData()).getId());
                    }
                }
                try{
                    memberSelectedJson = new Gson().toJson(memberSelectedList);
                }catch(Exception e){}
            }
            
            //twilio config
            twilioConfig.setAllowMemberList(memberSelectedJson);
            if(twilioConfig.getTwilioId() == null){
                if(twilioConfigService.getByPhonenumber(twilioConfig.getPhoneNumber()).getTwilioId() != null){
                    JsfUtil.addErrorMessage(String.format("前に「%s」が登録しましたので、ご確認してください。", twilioConfig.getPhoneNumber()));
                    return;
                }
                twilioConfig.setCreatorId(UserModel.getLogined().getMember());
                twilioConfig.setCreatedTime(new Date());
                twilioConfig.setCompanyId(UserModel.getLogined().getCompanyId());
                twilioConfigService.create(this.twilioConfig);
                JsfUtil.addSuccessMessage("登録しました。");
            }else{
                twilioConfig.setUpdatedId(UserModel.getLogined().getMember());
                twilioConfig.setUpdatedTime(new Date());
                twilioConfigService.edit(this.twilioConfig);
                JsfUtil.addSuccessMessage("変更しました。");
            }
        }catch(Exception e){
            logger.error(e.getMessage(), e);
            JsfUtil.addErrorMessage("エラーがありますので、確認してください。");
        }
        reload();
    }
    
    @SecureMethod(SecureMethod.Method.UPDATE)
    public void create(){
        this.twilioConfig = new TwilioConfig();
        List<Double> memberListId = new ArrayList<>(); //must Double type because of GSON return Double type list, not Integer type list
        List<Double> memberIgnoredList = twilioConfigService.getAllowMemberAdded(UserModel.getLogined().getCompanyId(), null);
        
        groups = new CheckboxTreeNode();
        groups.setExpanded(true);
        List<Group> groupList = groupService.findRootGroup(UserModel.getLogined().getCompanyId());
        groupList.forEach((g) -> {
            addGroupTree(groups, g, memberListId, memberIgnoredList);
        });
        showForm();
    }
    
    @SecureMethod(SecureMethod.Method.DELETE)
    public void delete(TwilioConfig c){
        try{
            twilioConfigService.remove(c);
            reload();
            JsfUtil.addSuccessMessage("削除しました。");
        }catch(Exception e){
            JsfUtil.addSuccessMessage("エラーがありますので、確認してください。");
        }
        reload();
    }
   
    private void showForm(){
        layout.setCenter("/modules/system/data/form.xhtml");
    }
    
    @SecureMethod(SecureMethod.Method.SEARCH)
    public void reload(){
        init();
        load();
    }
    
    @SecureMethod(value = SecureMethod.Method.INDEX, require = false)
    public void load(){
        layout.setCenter("/modules/system/data/list.xhtml");
    }

    @Override
    protected void doSearch(SearchFilter filter) {
        String query = filter != null ? filter.getQuery() : "";
        List<TwilioConfig> tmp = twilioConfigService.find(0, 100000, null, null, query);
        
        list.clear();
        tmp.forEach((t) -> {
            list.add(t);
        });
    }
    
    @SecureMethod(value = SecureMethod.Method.PRINT)
    public void print(){};
}
