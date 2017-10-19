package gnext.controller.issue;

import gnext.bean.Member;
import gnext.controller.issue.bean.IssueGlobalEditingStatus;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

/**
 *
 * @author hungpham
 * @since Jul 6, 2017
 */
@ApplicationScoped
@ManagedBean(name = "issueGlobalStatus")
public class IssueGlobalStatus implements Serializable{
    
    private static final long serialVersionUID = 5244131932504220647L;
    
    /**
     * Danh sach editing issue tuong ung voi moi cong ty
     * List<CompanyId, Map<IssueId, TimeStamp>
     */
    final private Map<Integer, Map<Integer, IssueGlobalEditingStatus>> editingIssueMapper = new HashMap<>();
    
    @PostConstruct
    public void init(){
        
    }
    
    /**
     * Lay ve danh sach editing issue tuong ung voi companyId
     * @param companyId
     * @return 
     */
    private Map<Integer, IssueGlobalEditingStatus> getMapping(Integer companyId){
        Map<Integer, IssueGlobalEditingStatus> mapper;
        if(editingIssueMapper.containsKey(companyId)){
            mapper = editingIssueMapper.get(companyId);
        }else{
            editingIssueMapper.put(companyId, (mapper = new HashMap<>()));
        }
        return mapper;
    }
    
    /**
     * Them issue vao danh sach editing
     * @param companyId
     * @param issueId 
     * @param member 
     */
    public void putEditing(Integer companyId, Integer issueId, final Member member){
        Map<Integer, IssueGlobalEditingStatus> mapper = getMapping(companyId);
        mapper.put(issueId, new IssueGlobalEditingStatus(member.getMemberId(), member.getMemberNameFull()));
    }
    
    /**
     * Loai bo issue ra khoi danh sach editing
     * @param companyId
     * @param issueId 
     */
    public void popEditing(Integer companyId, Integer issueId){
        Map<Integer, IssueGlobalEditingStatus> mapper = getMapping(companyId);
        mapper.remove(issueId);
    }
    
    /**
     * Kiem trang trang thai editing cua issue
     * @param companyId
     * @param issueId
     * @return 
     */
    public boolean isEditing(Integer companyId, Integer issueId){
        Map<Integer, IssueGlobalEditingStatus> mapper = getMapping(companyId);
        return mapper.containsKey(issueId);
    }
    
    /**
     * Kiem trang trang thai editing cua issue co phai dang edit boi memberId khong
     * @param companyId
     * @param issueId
     * @param memberId
     * @return 
     */
    public boolean isEditingByMember(Integer companyId, Integer issueId, Integer memberId){
        Map<Integer, IssueGlobalEditingStatus> mapper = getMapping(companyId);
        return mapper.containsKey(issueId) && mapper.get(issueId).getMemberId().equals(memberId);
    }
    
    /**
     * Tra ve thong tin cua nguoi dang edit issue
     * @param companyId
     * @param issueId
     * @return 
     */
    public IssueGlobalEditingStatus getEditingProfile(Integer companyId, Integer issueId){
        Map<Integer, IssueGlobalEditingStatus> mapper = getMapping(companyId);
        return mapper.containsKey(issueId) ? mapper.get(issueId) : null;
    }

    /**
     * Loai bo toan bo user cua 1 member dang duoc hold
     * @param companyId
     * @param memberId 
     */
    public void popAll(Integer companyId, Integer memberId) {
        Map<Integer, IssueGlobalEditingStatus> mapper = getMapping(companyId);
        
        for (Iterator<Map.Entry<Integer, IssueGlobalEditingStatus>> it = mapper.entrySet().iterator(); it.hasNext();) {
            Map.Entry<Integer, IssueGlobalEditingStatus> entry = it.next();
            try{
                if (entry.getValue().getMemberId().equals(memberId)) {
                    it.remove();
                }
            }catch(Exception e){
                //Ignore exception
            }
        }
    }
}
