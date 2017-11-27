package gnext.service.project;

import gnext.bean.Member;
import gnext.bean.issue.Issue;
import gnext.bean.project.ProjectCustColumnWidth;
import gnext.service.*;
import gnext.bean.project.ProjectCustSearch;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.ejb.Local;
import javax.faces.model.SelectItem;

/**
 *
 * @author hungpd
 */
@Local
public interface ProjectService extends EntityService<ProjectCustSearch> {
    public List<ProjectCustSearch> findSearchAvaiableList(int companyId);
    public List<ProjectCustSearch> findAllAvaiable(int companyId, Member member);
    public List<ProjectCustSearch> search(int companyId, String query);
    
    public List<Map<String, String>> findIssueById(Integer issueId, Integer companyId, Integer memberId, List<String> columns);
    public List<Map<String, String>> findIssueByIdAndLocale(Integer issueId, Integer companyId, Integer memberId, List<String> columns, String locale);
    public List<Map<String, Object>> advanceSearch(short comFlag, int companyId, int memberId, boolean quickSearch, String query, List<String> condFields, String keyword, String keywordCondition, List<SelectItem> columns, Date from, Date to, String lang);
    public List<Map<String, Object>> advanceSearch(short comFlag, int issueId, int companyId, int memberId, boolean quickSearch, String query, List<String> condFields, String keyword, String keywordCondition, List<SelectItem> columns, Date from, Date to, String lang);

    public int reIndexAll();
    public boolean indexDocument(int issueId);

    public String persitIssueToElastic(Issue issue, List<String> columns, List<Locale> locales);
    
    public void saveColumnWidth(String column, int width, int companyId, int updateId);
    public List<ProjectCustColumnWidth> getColumnWidthList(int companyId);
}
