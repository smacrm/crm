package gnext.controller.authority;

import gnext.bean.role.Page;
import gnext.bean.role.SystemModule;
import gnext.controller.common.LayoutController;
import gnext.model.authority.AuthModel;
import gnext.model.authority.PageModel;
import gnext.model.authority.UserModel;
import gnext.security.annotation.SecureMethod;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import gnext.security.annotation.SecurePage;
import gnext.service.role.MethodService;
import gnext.service.role.SystemModuleService;
import gnext.util.JsfUtil;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.type.filter.AnnotationTypeFilter;

/**
 * Scan package to get all modules & page, method
 *
 * @author hungpham
 * @since 2016/09
 */
@ManagedBean(name = "pageScanController")
@SecurePage(module = SecurePage.Module.SYSTEM, require = false)
@ViewScoped() 
public class PageScanController implements Serializable{
    
    private static final long serialVersionUID = 1L;
    private final Logger logger = LoggerFactory.getLogger(PageScanController.class);

    @ManagedProperty(value = "#{layout}")
    @Getter @Setter private LayoutController layout;
    
    @ManagedProperty(value = "#{authorityController}")
    @Getter @Setter private AuthorityController authorityController;
    
    @Setter @Getter private TreeNode authList;
    
    @EJB private SystemModuleService systemModuleServiceImpl;
    @EJB private MethodService methodServiceImpl;
    
    private List<SystemModule> systemModuleList;
    
    final private Map<String, List<gnext.bean.role.Page>> pageList = new HashMap<>();
    final private Map<String, List<gnext.bean.role.Page>> pageListFull = new HashMap<>();
    final private Map<String, List<gnext.bean.role.Method>> methodList = new HashMap<>();
    final private Map<String, List<gnext.bean.role.Method>> methodListFull = new HashMap<>();
    
    final private String scanPackage = "gnext.controller"; //package to scan controller
    
    @PostConstruct
    public void init(){
        systemModuleList = systemModuleServiceImpl.findAll();
        pageList.clear();
        methodList.clear();
        pageListFull.clear();
        methodListFull.clear();
        
        systemModuleList.stream().forEach((m) -> {
            if(m.getModuleDeleted() == 0){
                List<gnext.bean.role.Page> pageListTmp = new ArrayList<>();
                m.getPages().stream().forEach((p) -> {
                    if(p.getPageDeleted() == 0){
                        List<gnext.bean.role.Method> methodListTmp = new ArrayList<>(); 
                        p.getMethodList().forEach((mt) -> {
                            methodListTmp.add(mt);
                        });
                        methodList.put(p.getPageName(), methodListTmp);
                        pageListTmp.add(p);
                    }
                    methodListFull.put(p.getPageName(), p.getMethodList());
                });
                pageList.put(m.getModuleName(), pageListTmp);
            }
            pageListFull.put(m.getModuleName(), m.getPages());
        });
        
        authList = createModuleList();
    }
    
    /**
     * create full tree nodes from scanned resource
     * 
     * @return TreeNode
     */
    private TreeNode createModuleList() {
        PageModel authTreeRootDocument = new PageModel("ROOT");
        TreeNode authTreeRoot = new DefaultTreeNode(authTreeRootDocument, null);
        authTreeRootDocument.setTreeNode(authTreeRoot);
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(SecurePage.class));
        Map<String, TreeNode> moduleTreeMap = new HashMap<>();
        
        for (BeanDefinition bd : scanner.findCandidateComponents(scanPackage)){
            //Console.log(bd.getBeanClassName());
            try{
                SecurePage page = Class.forName(bd.getBeanClassName()).getAnnotation(SecurePage.class);
                if(page.require() == true){
                    String moduleName = page.module().toString();
                    
                    String pageClassSimpleName = Class.forName(bd.getBeanClassName()).getSimpleName();
                    
                    String pageName = StringUtils.isEmpty(page.value()) ? pageClassSimpleName : page.value();
                    String pageSource = bd.getBeanClassName();
                    
                    TreeNode moduleTreeNode = null, pageTreeNode = null;
                    if(moduleTreeMap.containsKey(moduleName)){
                        moduleTreeNode = moduleTreeMap.get(moduleName);
                    }else{
                        PageModel moduleData = new PageModel(0, moduleName);
                        moduleTreeNode = new DefaultTreeNode(moduleData, authTreeRoot);
                        moduleData.setTreeNode(moduleTreeNode);
                        moduleTreeNode.setExpanded(true);
                        moduleTreeMap.put(moduleName, moduleTreeNode);
                    }

                    PageModel pageItemData = new PageModel(0, pageName);
                    pageItemData.setSource(pageSource);
                    
                    pageTreeNode = new DefaultTreeNode(pageItemData, moduleTreeNode);
                    pageItemData.setTreeNode(pageTreeNode);
                    pageTreeNode.setExpanded(true);
                    Method[] methods = Class.forName(bd.getBeanClassName()).getDeclaredMethods();
                    List<String> tmpUniqueMethodName = new ArrayList<>();
                    boolean isMethodCheckedAll = false;
                    for(Method m: methods){
                        if(m.isAnnotationPresent(SecureMethod.class)){
                            SecureMethod method = m.getDeclaredAnnotation(SecureMethod.class);
                            if(method.require() == true){
                                String methodName = method.value() == SecureMethod.Method.NONE ? m.getName() : method.value().toString();
                                //Console.log(">>"+m.getName() + " > " + methodName);
                                if( !tmpUniqueMethodName.contains(methodName) ){
                                    tmpUniqueMethodName.add(methodName);
                                    boolean methodCheckStatus = this.getMethodBean(moduleName, pageName, methodName)!=null;
                                    PageModel.ActionModel methodData = pageItemData.new ActionModel(0, methodName, methodCheckStatus);
                                    methodData.setTreeNode(new DefaultTreeNode(methodData, pageTreeNode));
                                    
                                    isMethodCheckedAll |= methodCheckStatus;
                                }
                            }
                        }
                    }
                    tmpUniqueMethodName.clear();
                    pageItemData.setSelected(isMethodCheckedAll);
                }
            }catch(ClassNotFoundException e){
                logger.error(e.getMessage(), e);
            }
        }
        
        //reload node's status
        for (TreeNode module : authTreeRoot.getChildren()) {
            PageModel moduleData = (PageModel) module.getData();
            boolean isPageCheckAll = false;
            for (TreeNode page : module.getChildren()) {
                PageModel pageData = (PageModel) page.getData();
                isPageCheckAll |= pageData.isSelected();
            }
            moduleData.setSelected(isPageCheckAll);
        }
        
        return authTreeRoot;
    }
    
    /**
     * Save choosen list with transaction mode to DB
     * 
     */
    @SecureMethod(value=SecureMethod.Method.CREATE)
    public void save(){
        boolean hasFailed = false;
        String failedMessage = null;
        Integer userId = UserModel.getLogined().getUserId();
        Map<String, gnext.bean.role.Method> listAddedMethod = new HashMap<>();
        for (TreeNode module : authList.getChildren()) {
            try{
                PageModel moduleData = (PageModel) module.getData();
                gnext.bean.role.SystemModule moduleBean = this.getModuleBeanFull(moduleData.getName());
                if(moduleData.isSelected()){
                    if( null == moduleBean ){
                        moduleBean = new SystemModule();
                        moduleBean.setCreatorId(userId);
                        moduleBean.setCreatedTime(new Date());
                        moduleBean.setModuleName(moduleData.getName());
                    }else{
                        moduleBean.setUpdatedId(userId);
                        moduleBean.setUpdatedTime(new Date());
                    }
                    moduleBean.setModuleDeleted((short)0);

                    for (TreeNode page : module.getChildren()) {
                        PageModel pageData = (PageModel) page.getData();
                        gnext.bean.role.Page pageBean = this.getPageBeanFull(moduleData.getName(), pageData.getName());
                        if( pageData.isSelected() ){
                            if( null == pageBean){ //Create new page if not exists before
                                pageBean = new Page();
                                pageBean.setPageName(pageData.getName());
                                pageBean.setPageSource(pageData.getSource());
                                pageBean.setCreatorId(userId);
                                pageBean.setCreatedTime(new Date());
                                moduleBean.getPages().add(pageBean);
                            }else{
                                pageBean.setUpdatedId(userId);
                                pageBean.setUpdatedTime(new Date());
                            }
                            pageBean.setPageDeleted((short)0);
                        }else{
                            if( null != pageBean ){
                                pageBean.setUpdatedId(userId);
                                pageBean.setUpdatedTime(new Date());
                                pageBean.setPageDeleted((short)1);
                            }
                            continue;
                        }

                        for (TreeNode method : page.getChildren()) {
                            PageModel.ActionModel m = (PageModel.ActionModel) method.getData();
                            if(m.isSelected() == false){
                                for (Iterator<gnext.bean.role.Method> it = pageBean.getMethodList().iterator(); it.hasNext(); ) {
                                    gnext.bean.role.Method t = it.next();
                                    if(t.getMethodName().equals(m.getName())){
                                        it.remove();
                                        break;
                                    }
                                }
                            }else{
                                boolean isMethodAdded = false;
                                for( gnext.bean.role.Method item : pageBean.getMethodList() ) isMethodAdded |= item.getMethodName().equals(m.getName());
                                if( !isMethodAdded ){
                                    gnext.bean.role.Method methodBean;
                                    if( !listAddedMethod.containsKey(m.getName()) ){
                                        methodBean = this.getMethodBean(m.getName());
                                        if( null == methodBean ) {
                                            methodBean = new gnext.bean.role.Method();
                                            methodBean.setMethodName(m.getName());
                                            methodBean.setMethodOrder(0);
                                            methodBean.setCreatedTime(new Date());
                                            methodBean.setCreatorId(userId);

                                            methodBean = methodServiceImpl.create(methodBean);

                                            listAddedMethod.put(m.getName(), methodBean);
                                        }else{
                                            methodBean.setUpdatedId(userId);
                                            methodBean.setUpdatedTime(new Date());
                                        }
                                    }else{
                                        methodBean = listAddedMethod.get(m.getName());
                                    }
                                    pageBean.getMethodList().add(methodBean);
                                }
                            }
                        }
                    }
                } else if ( null != moduleBean ){
                    moduleBean.setUpdatedId(userId);
                    moduleBean.setUpdatedTime(new Date());
                    moduleBean.setModuleDeleted((short)1);
                }
                systemModuleServiceImpl.save(moduleBean);
            } catch (Exception e) {
                hasFailed = true;
                failedMessage = e.getMessage();
                logger.error(e.getMessage(), e);
            }
        }
        reload();
        authorityController.reloadData();
        
        if(!hasFailed) JsfUtil.addSuccessMessage("完了しました。");
        else JsfUtil.addErrorMessage((failedMessage != null ? failedMessage+"<br/>" : "") + "エラーがあるので、完了ができません。");
    }
    
    /**
     * Get module object from full module list
     * 
     * @param name
     * @return 
     */
    private SystemModule getModuleBeanFull(String name){
        if(pageListFull.containsKey(name)){
            for(SystemModule m : systemModuleList){
                if(m.getModuleName().equals(name)){
                    return m;
                }
            }
        }
        return null;
    }
    
    /**
     * Get Page Object from list of all cached-page
     * 
     * @param module
     * @param page
     * @return 
     */
    private Page getPageBeanFull(String module, String page){
        if(pageListFull.containsKey(module)){
            for(Page p : pageListFull.get(module)){
                if(p.getPageName().equals(page)){
                    return p;
                }
            }
        }
        return null;
    }
    
    /**
     * Get method object from cached method list
     * 
     * @param module
     * @param page
     * @param method
     * @return 
     */
    private gnext.bean.role.Method getMethodBean(String module, String page, String method){
        if(pageList.containsKey(module) && methodList.containsKey(page)){
            for(gnext.bean.role.Method t : methodList.get(page)){
                if(t.getMethodName().equals(method)){
                    return t;
                }
            }
        }
        return null;
    }
    
    /**
     * Get method from DB for distinting added method
     * 
     * @param method
     * @return 
     */
    private gnext.bean.role.Method getMethodBean(String method){
        return methodServiceImpl.find(method);
    }
    
    /**
     * Toggle action after clicking to node's checkbox
     * 
     * @param selectedNode 
     */
    @SecureMethod(value=SecureMethod.Method.NONE, require=false)
    public void toggleAction(TreeNode selectedNode) {  
        if(selectedNode != null){
            boolean toggleSelected = ((AuthModel)selectedNode.getData()).isSelected();
            for(TreeNode node : selectedNode.getChildren()){
                ((AuthModel)node.getData()).setSelected(toggleSelected);
                for(TreeNode node2 : node.getChildren()){
                    ((AuthModel)node2.getData()).setSelected(toggleSelected);
                }
            }
            toggleParentStatus(selectedNode.getParent());
        }
    }
    
    /**
     * Toggle parent of tree node
     * 
     * @param parent 
     */
    private void toggleParentStatus(TreeNode parent){
        if( null != parent ){
            boolean isAllChecked = false;
            for(TreeNode node : parent.getChildren()){
                isAllChecked |= ((AuthModel)node.getData()).isSelected();
            }
            
            ((AuthModel)parent.getData()).setSelected(isAllChecked);

            toggleParentStatus(parent.getParent());
        }
    }
    
    /**
     * Reload data & screen
     */
    @SecureMethod(value=SecureMethod.Method.NONE, require=false)
    public void reload(){
        init();
        loadPageList();
    }
    
    /**
     * Load screen
     */
    @SecureMethod(value=SecureMethod.Method.INDEX, require = false)
    public void loadPageList(){
        this.layout.setCenter("/modules/authority/page_scan.xhtml");
    }
}
