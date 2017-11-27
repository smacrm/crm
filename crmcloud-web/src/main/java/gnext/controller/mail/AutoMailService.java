package gnext.controller.mail;

import com.google.gson.Gson;
import gnext.bean.MailAccount;
import gnext.bean.automail.SimpleAutoMail;
import gnext.bean.issue.EscalationSample;
import gnext.controller.issue.bean.PersitBean;
import gnext.mailapi.MailClient;
import gnext.mailapi.mail.SendEmail;
import gnext.service.config.ConfigService;
import gnext.service.issue.IssueEscalationSampleService;
import gnext.service.issue.IssueService;
import gnext.service.label.LabelService;
import gnext.service.mail.MailAccountService;
import gnext.util.InterfaceUtil;
import gnext.util.ResourceUtil;
import gnext.utils.StringUtil;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.faces.model.SelectItem;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hungpham
 * @since Sep 5, 2017
 */
public class AutoMailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AutoMailService.class);

    public static void run(SimpleAutoMail item, 
            ConfigService configService, 
            MailAccountService mailAccountService, 
            gnext.service.automail.AutoMailService autoMailServiceImpl,
            IssueService issueService, 
            IssueEscalationSampleService issueEscalationSampleService,
            LabelService labelService) {
        try {
            int companyId = item.getCompanyId();
            int optionId = item.getOptionId();
            int issueId = item.getIssueId();
            String language = "ja";

            List<String> mailToList = item.getToList();
            List<String> mailCcList = item.getCcList();

            List<Integer> allowViewUserList = new ArrayList<>();
            allowViewUserList.addAll(item.getToIntList());
            allowViewUserList.addAll(item.getCcIntList());

            if (mailToList.isEmpty() && mailToList.isEmpty()) {
                LOGGER.error("Cannot send Auto Mail for issue " + issueId);
                return;
            }

            StringBuilder subject = new StringBuilder();
            subject.append(String.format("【%s】", item.getIssueViewCode()));
            StringBuilder content = new StringBuilder();

            SendEmail sendMail = new SendEmail();
            switch (optionId) {
                case 2: // 対応依頼
                    List<SelectItem> requestTypes = issueService.getList(
                            gnext.utils.InterfaceUtil.COLS.MAIL_REQUEST,
                            companyId,
                            language);
                    EscalationSample escs = null;
                    if (!requestTypes.isEmpty()) {
                        escs = issueEscalationSampleService.getEscalationSampleByTypeIdAndTargetId(
                                gnext.utils.InterfaceUtil.ISSUE_TYPE.REQUEST,
                                (Integer) requestTypes.get(0).getValue(),
                                companyId,
                                language);
                    }
                    if (escs != null) {
                        subject.append(escs.getSampleSubject());
                        content.append(escs.getSampleBody());
                    } else {
                        subject.append(message(labelService, companyId, ResourceUtil.BUNDLE_ISSUE_NAME, new Locale(language), "label.escalation_2"));
                    }
                    break;
                case 5: // 承認依頼
                    subject.append(message(labelService, companyId, ResourceUtil.BUNDLE_ISSUE_NAME, new Locale(language), "label.escalation_6"));
                    content.append(message(labelService, companyId, ResourceUtil.BUNDLE_ISSUE_NAME, new Locale(language), "label.issue.request"));
                    break;
                case 6: // 完了
                    subject.append(message(labelService, companyId, ResourceUtil.BUNDLE_MSG, new Locale(language), "label.complete"));
                    content.append(message(labelService, companyId, ResourceUtil.BUNDLE_ISSUE_NAME, new Locale(language), "label.issue.complete"));
                    break;
            }

            PersitBean bean = new PersitBean();
            bean.setIssueId(issueId);
            bean.setCompanyId(companyId);
            bean.getMemberList().addAll(allowViewUserList);
            bean.setExpiredDate(DateUtils.addDays(new Date(), 7));
            String key = bean.getSyncKey();

            String link = new StringBuilder(configService.get("WEBSITE_URL")).append("/issue/").append(key).toString();

            content.append(StringUtils.repeat(InterfaceUtil.HTML.BR, 2)).append(InterfaceUtil.HTML.CIRCLE);
            content.append(message(labelService, companyId, ResourceUtil.BUNDLE_MSG, new Locale(language), "label.request_mail_footer"));
            content.append(StringUtils.repeat(InterfaceUtil.HTML.BR, 2));
            content.append(String.format("<a href=\"%s\" style=\"border-collapse: collapse; border-radius: 2px; text-align: center; display: inline-block; border: solid 1px #344c80; background: #4c649b; padding: 7px 16px 11px 16px; text-decoration: none; color: #fff; font-weight: bold\">%s</a>", link,
                    message(labelService, companyId, ResourceUtil.BUNDLE_MSG, new Locale(language), "label.system.link.text")));

            if (!mailToList.isEmpty()) {
                sendMail.setRecipient(mailToList.toArray(new String[]{}));
            }
            if (!mailCcList.isEmpty()) {
                sendMail.setCc(mailCcList.toArray(new String[]{}));
            }
            sendMail.setSubject(subject.toString());
            sendMail.setMessage(content.toString());

            List<MailAccount> acs = mailAccountService.getSendAccountList(companyId);
            if (acs.isEmpty()) {
                return;
            }

            MailAccount ac = acs.get(0); // lay du db mail account dau tien duoc phep gui mail
            sendMail.setFrom(ac.getAccountMailAddress());
            sendMail.setType(gnext.mailapi.util.InterfaceUtil.Type.SMTP);
            sendMail.setUserName(ac.getAccountUserName());
            sendMail.setPassword(ac.getAccountPassword());
            sendMail.setHost(ac.getMailServer().getServerSmtp());
            sendMail.setPort(ac.getMailServer().getServerSmtpPort());
            sendMail.setSsl(Boolean.valueOf(ac.getMailServer().getServerSsl()));
            sendMail.setTls(true);
            sendMail.setAuth(Boolean.valueOf(ac.getMailServer().getServerAuth()));
            /**
             * メールを送信
             */
            Map<String, String> mapArgs = new HashMap<>();
            mapArgs.put("action", "send");
            mapArgs.put("serverid", String.valueOf(ac.getMailServer().getServerId()));
            mapArgs.put("cfg", StringUtil.DEFAULT_DB_PROPERTIES);
            MailClient._SendMail(sendMail, mapArgs);

            // save log mail
            autoMailServiceImpl.pushHistory(item.getAutoId(), issueId, companyId, new Gson().toJson(item));

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
    
    public static String message(LabelService labelService, Integer companyId, String bundleName, Locale locale, String key, Object... params){
        
        String dbMessage = labelService.findByKey(bundleName, key, locale.getLanguage(), companyId); 
        
        if(StringUtils.isEmpty(dbMessage)){
            ResourceBundle rb = ResourceBundle.getBundle(bundleName, locale);
            try{
                return MessageFormat.format(rb.getString(key), params);
            }catch(MissingResourceException mre){}
            return key;
        }
        return dbMessage;
    }
}
