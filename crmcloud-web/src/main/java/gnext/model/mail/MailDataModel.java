/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.model.mail;

import gnext.bean.mail.MailData;
import gnext.bean.attachment.Attachment;
import gnext.bean.mail.MailExplode;
import gnext.bean.issue.Escalation;
import gnext.bean.issue.Issue;
import gnext.model.BaseModel;
import gnext.util.DateUtil;
import gnext.util.StringUtil;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author daind
 */
public class MailDataModel extends BaseModel<MailData> {
    private static final long serialVersionUID = -203475074135265998L;
    private static final int MAX_BODY_SHOW_IN_LIST_PAGE = 200;
    
    @Getter @Setter private MailData mailData;
    @Getter @Setter private List<MailExplode> mailExplodes;
    @Getter @Setter private List<Attachment> attachments;
    @Getter @Setter private List<Issue> issueRelated;
    
    public MailDataModel(MailData mailData) {
        this.mailData = mailData;
    }

    public String getDisplayBodyOnList() {
        return StringUtil.substring(StringUtil.html2text(mailData.getMailDataBody()), MAX_BODY_SHOW_IN_LIST_PAGE);
    }
    
    public String getDisplaySentDate() {
        return DateUtil.getDateToString(mailData.getMailDataDatetime(), DateUtil.PATTERN_JP_SLASH_HH_MM);
    }
    
    public boolean hasExplode() {
        if(mailExplodes == null || mailExplodes.isEmpty()) return false;
        return true;
    }
    
    public String getBody() {
        return mailData.getMailDataBody();
    }
    
    public boolean hasEscalation() {
        if(mailData == null) return false;
        if(mailData.getIssue() == null) return false;
        if(mailData.getIssue().getEscalationList() == null || mailData.getIssue().getEscalationList().isEmpty()) return false;
        for(Escalation escalation : mailData.getIssue().getEscalationList()) {
            if(escalation.getEscalationSendType() == gnext.utils.InterfaceUtil.ISSUE_TYPE.EMAIL) return true;
        }
        return false;
    }
    
    public boolean hasIssueRelated() {
        return this.issueRelated != null && !this.issueRelated.isEmpty();
    }
}
