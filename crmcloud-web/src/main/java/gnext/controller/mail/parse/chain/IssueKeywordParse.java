/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.controller.mail.parse.chain;

import gnext.bean.issue.Customer;
import gnext.controller.issue.IssueController;
import gnext.controller.mail.parse.MailParse;
import gnext.bean.issue.Issue;
import gnext.util.JsfUtil;
import gnext.util.SelectUtil;
import gnext.util.StringUtil;
import gnext.utils.InterfaceUtil.COLS;
import java.util.List;
import java.util.Map;
import javax.faces.model.SelectItem;

/**
 *
 * @author tungdt
 */
public class IssueKeywordParse implements MailParse {

    @Override
    public void parse(Issue issue, Map<String, String> mappings, Map<String, String> params) throws Exception {
        if(issue == null) return;
        if(mappings == null || mappings.isEmpty()) return;
        String keyword = mappings.get("issue_keyword_name");
        if(keyword == null || keyword.isEmpty() ) return;
        keyword = StringUtil.nl2br(keyword, "");
        IssueController issueController = JsfUtil.getManagedBean("issueController", IssueController.class);
        List<SelectItem> select = issueController.getSelect().get(COLS.KEYWORD);
        
        Integer keywordId = SelectUtil.getIdByLabel(select, keyword);
        
        // !!! TODO 
        // ↓ COMMENT FOR TEST
        // hungpd
        // if(keywordId != null) issue.setIssueKeywordId(keywordId.toString());
    }
}
