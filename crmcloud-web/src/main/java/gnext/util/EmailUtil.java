package gnext.util;

import gnext.mailapi.MailClient;
import gnext.bean.MailAccount;
import gnext.bean.Member;
import gnext.bean.mail.MailFolder;
import gnext.dbutils.model.MailData;
import gnext.bean.issue.Escalation;
import gnext.bean.issue.Issue;
import gnext.mailapi.mail.SendEmail;
import gnext.model.MailMode;
import static gnext.util.StatusUtil.UN_DELETED;
import gnext.utils.InterfaceUtil.ARRAY_STRING_ICON;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author daind
 */
public final class EmailUtil {

    public static final String INBOX = "INBOX";
    public static final String SENT = "SENT";
    public static final String DRAFT = "DRAFT";
    public static final String JUNK = "JUNK";
    public static final String TRASH = "TRASH";

    private EmailUtil() { }

    public static String getExplodeType(String folder) {
        if (StringUtils.isEmpty(folder)) return MailFolder.DATA_MAIL_FOLDER_INBOX;
        if (StringUtils.isNumeric(folder)) return folder;
        switch (folder.toUpperCase()) {
            case INBOX: return MailFolder.DATA_MAIL_FOLDER_INBOX;
            case SENT: return MailFolder.DATA_MAIL_FOLDER_SENT;
            case DRAFT: return MailFolder.DATA_MAIL_FOLDER_DRAFT;
            case JUNK: return MailFolder.DATA_MAIL_FOLDER_JUNK;
            case TRASH: return MailFolder.DATA_MAIL_FOLDER_TRASH;
        }
        return MailFolder.DATA_MAIL_FOLDER_INBOX;
    }

    public static String getFromName(String from) {
        if (StringUtils.isEmpty(from)) return StringUtils.EMPTY;
        String[] x = from.split("@")[0].split("<");
        return x[0];
    }

    public static MailAccount getAccount(List<MailAccount> list, String address) {
        if(list == null || list.size() <= 0 || StringUtils.isBlank(address)) return null;
        for(MailAccount ac:list) {
            if(ac == null || !address.equals(ac.getAccountMailAddress())) continue;
            return ac;
        }
        return null;
    }

    @SuppressWarnings("IndexOfReplaceableByContains")
    public static String getValuesReplaceRexg(String val, String rexg) {
        if(StringUtils.isBlank(val)) return null;
        if(val.startsWith(ARRAY_STRING_ICON.COMMAR)) val = val.substring(1, val.length());
        if(!StringUtils.isBlank(rexg) && val.indexOf(rexg) > -1) val = val.replaceAll(rexg, StringUtils.EMPTY);
        return val;
    }

    public static Escalation getEscalation(Escalation esc, MailMode mailModel, Issue issue, Member user, Short escType) {
        if(mailModel == null || issue == null || user == null || escType == null) return null;
        if(esc == null) {
            esc = new Escalation();
        }
        esc.setEscalationIssueId(issue);
        esc.setEscalationFromEmail(StringUtils.defaultIfEmpty(mailModel.getSendMail().getFrom(), StringUtils.EMPTY));
        esc.setEscalationSendType(escType);
        esc.setEscalationSendDate(mailModel.getDate());
        esc.setEscalationTo(StringUtils.join(mailModel.getSendMail().getRecipient(), ARRAY_STRING_ICON.COMMAR));
        esc.setEscalationCc(StringUtils.join(mailModel.getSendMail().getCc(), ARRAY_STRING_ICON.COMMAR));
        esc.setEscalationBcc(StringUtils.join(mailModel.getSendMail().getBcc(), ARRAY_STRING_ICON.COMMAR));
        esc.setEscalationTitle(mailModel.getSendMail().getSubject());
        if(StringUtils.isNotBlank(mailModel.getHeader())) {
            esc.setEscalationHeader(mailModel.getHeader());
        }
        esc.setEscalationBody(mailModel.getSendMail().getMessage());
        if(StringUtils.isNotBlank(mailModel.getFooter())) {
            esc.setEscalationFooter(mailModel.getFooter());
        }
        esc.setEscalationMemberId(user);
        esc.setEscalationIsDeleted(UN_DELETED);
        esc.setCompanyId(user.getGroup().getCompany().getCompanyId());
        return esc;
    }

    public static MailData send(SendEmail sendMail, MailAccount ac, Map<String, InputStream> map, Integer companyId, Integer serverId, Integer mailAccountId)
            throws Exception {
        sendMail.setAttachments(map);
        sendMail.setType(gnext.mailapi.util.InterfaceUtil.Type.SMTP);
        sendMail.setUserName(ac.getAccountUserName());
        sendMail.setPassword(ac.getAccountPassword());
        sendMail.setHost(ac.getMailServer().getServerSmtp());
        sendMail.setPort(ac.getMailServer().getServerSmtpPort());
        sendMail.setSsl(Boolean.valueOf(ac.getMailServer().getServerSsl()));
        sendMail.setTls(true);
        sendMail.setAuth(Boolean.valueOf(ac.getMailServer().getServerAuth()));
        /** メールを送信 */
        Map<String, String> mapArgs = new HashMap<>();
        mapArgs.put("action", "send");
        
        // các tham số cần thiết để lưu vào cơ sở dữ liệu.
        mapArgs.put("savedb", "true"); // trạng thái lưu mail và attachment.
        mapArgs.put("cfg", gnext.utils.StringUtil.DEFAULT_DB_PROPERTIES); // chứa connection tới MasterDb.
        mapArgs.put("companyid", companyId.toString()); // company id.(company slave)
        mapArgs.put("serverid", serverId.toString()); // ftp server.(File được lưu trên ftp server của slave công ty)
        mapArgs.put("accountid", mailAccountId.toString()); // mail account id.
        
        return MailClient._SendMail(sendMail, mapArgs);
    }

    /**
     * ファイルをアップロードやダウンロード際にリストに追加または削除
     * @param mailMode
     * @param file：UploadedFile
     * @param idx：削除Index
     * @return 削除したファイルID
     */
    public static Integer uploadDowload(MailMode mailMode, UploadedFileExt file, String idx) {
        if(file != null && StringUtils.isNotBlank(file.getFile().getFileName())) {
            file.setSizeView(ByteUtil.getSizeToString(file.getFile().getSize()));
            mailMode.getAttachs().add(file);
        } else {
            for(int i=0; i<mailMode.getAttachs().size(); i++) {
                if(i != Integer.valueOf(idx)) continue;
                Integer deletedFileId = mailMode.getAttachs().get(i).getOldId();
                mailMode.getAttachs().remove(i);
//                if(mailMode.getSizes().get(i) != null) {
//                    mailMode.getSizes().remove(i);
//                }
                return deletedFileId;
            }
        }
        return 0;
    }
}
