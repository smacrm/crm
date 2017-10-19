/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.service.issue;

import gnext.bean.issue.Customer;
import gnext.bean.issue.Issue;
import gnext.bean.issue.IssueLamp;
import gnext.service.EntityService;
import java.util.List;
import java.util.Locale;
import javax.ejb.Local;
import javax.faces.model.SelectItem;

/**
 * 
 * @author gnextadmin
 */
@Local
public interface IssueService extends EntityService<Issue> {
    public Issue findByIssueId(final int issueId, String locale);
    public List<Issue> findByIssueIdList(final List<Integer> issueIdList);
    public Issue findByIssueViewCode(final int companyId, String issueViewCode);

    public List<SelectItem> getList(final String field, final int companyId, final String language);

    public List<SelectItem> getListByLevel(final int parentId, final int companyId, final String language, final int level, final boolean product);

    public List<SelectItem> getProductByName(final String name, final int companyId, final String language, final int maxLevel, final Integer flag,String inKey);

    public Integer getCustomizePageId(final String page, final int companyId);

    public String getMaxLevelField(final String field, final int companyId, final boolean product);

    public List<Issue> getIssueHistoryCustomers(final int issueId, int companyId, final String telMobileMails, final String custCode);
    public List<Customer> getSpecialCustomers(final String telMobileMails, int companyId, final String custCode);

    public List<IssueLamp> getIssueLamps(final int companyId);

    public Issue createIssue(final Issue issue, final List<String> availableColumns, final List<Locale> availableLocales) throws Exception;
    public Issue editIssue(final Issue issue, final List<String> availableColumns, final List<Locale> availableLocales, String locale) throws Exception;

    public List<Issue> findByIssueViewCodeLike(int companyId, String code);

    public List<Object> findByCountIssueSame(int companyId, List<Integer> months, Integer issueId);

    public List<Issue> findByCompanyId(int companyId);
}
