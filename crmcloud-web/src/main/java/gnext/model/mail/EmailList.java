/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.model.mail;

import gnext.bean.mail.MailData;
import gnext.model.BaseModel;
import gnext.util.EmailUtil;
import gnext.util.StringUtil;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author daind
 */
public class EmailList extends BaseModel {
    private static final long serialVersionUID = 4167346112348889650L;
    @Getter @Setter private Integer mailId;
    @Getter @Setter private String from;
    @Getter @Setter private String fromName;
    @Getter @Setter private String subject;
    @Getter @Setter private String issueCode;
    @Getter @Setter private String body;

    public EmailList(MailData mailData) {
        this.mailId = mailData.getMailDataId();
        this.from = mailData.getMailDataFrom();
        this.fromName = StringUtils.EMPTY;
        this.subject = mailData.getMailDataSubject();
        if(mailData.getIssue() != null) this.issueCode = mailData.getIssue().getIssueViewCode();
        this.body = mailData.getMailDataBody();
    }

    public String getDisplayFromName() {
        return EmailUtil.getFromName(from);
    }

    public String getDisplaySubject() {
        return StringUtil.substring(subject, 10);
    }

    public String getDisplayBody() {
        return StringUtil.substring(StringUtil.html2text(body), 40);
    }
}
