/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.model.system.command.mail;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

/**
 *
 * @author daind
 */
public class MailCommandItem implements Serializable {
    private static final long serialVersionUID = -1837955310898863700L;
    @Getter @Setter private String action;
    @Getter @Setter private String cfg;
    @Getter @Setter private String folder = "inbox"; // mặc định sẽ đọc ở trong inbox folder của Mail-Server.
    @Getter @Setter private String flag;
    @Getter @Setter private String prior;
    @Getter @Setter private String serverid; // hiểu như là fpt server-id trong bảng crm_server.
    @Getter @Setter private String path;
            
    /**
     * Xây dựng cli command.
     * @return 
     */
    public String buildReadMailCommand() {
        StringBuilder c = new StringBuilder("java -jar {0} action:receive cfg:{1}");
        c.append(" folder:").append(folder);// folder cần lấy mail về từ server.
        c.append(" flag:").append(flag);
        if(!StringUtils.isEmpty(prior)) c.append(" prior:").append(prior); // filters mail theo ngày(số ngày lùi so với hiện tại).
        c.append(" serverid:").append(serverid); // server fpt cho việc lưu trữ attachment.
        c.append(" companyid:").append("{2}"); // công ty cần lấy mail về.
        c.append(" debug:false"); // đặt chế độ DEBUG mail.
        return c.toString();
    }
}
