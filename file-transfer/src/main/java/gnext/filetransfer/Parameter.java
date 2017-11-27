/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.filetransfer;

import gnext.dbutils.model.enums.AttachmentTargetType;
import lombok.Getter;

/**
 *
 * @author daind
 */
public class Parameter extends OutParameter {
    @Getter private TransferType type;
    
    /** các thông tin cấu hình cần có. */
    @Getter private String host;
    @Getter private int port = 21; // $$default port$$
    @Getter private String username;
    @Getter private String password;
    
    /** Sử dụng giao thức FTPS. */
    @Getter private boolean security;
    @Getter private String protocol; // $$SSL/TLS$$
    
    /** Tên file và thư mục tải lên. */
    @Getter private String uploadfilename;                      // tên file đẩy lên server.
    @Getter private String uploadpath;                          // đường dẫn thư mục.
    @Getter private String appendFolder;                        // thư mục mở rộng.
    @Getter private boolean createfolder;
    
    /** Thông tin cấu hình database. */
    @Getter private boolean manualconfig = false;                // mặc định sẽ lấy thông tin từ slavedb, khi là false yêu cầu cần cung cấp conpmay_id để lấy slave.
    @Getter private Integer companyid = null;                    // ID của công ty slave.
    @Getter private Integer serverid;                            // serverid cho phép lookup thông tin kết nối tới server.
    @Getter private boolean storeDb = true;                      // có lưu vào db không, nếu có sẽ lưu vào bảng crm_attachment.
    @Getter private String conf;                                 // đường dẫn tới file cấu hình kết nối database.
    @Getter private AttachmentTargetType attachmentTargetType;   // cần cung cấp nếu lưu vào database(storeDb = true)
    @Getter private Integer attachmentTargetId;                  // id của entity.
    
    public static Parameter getInstance(TransferType type) {
        return new Parameter().type(type);
    }
    
    public Parameter type(TransferType type) {
        this.type = type; return this;
    }
    
    public Parameter host(String host) {
        this.host = host; return this;
    }
    
    public Parameter username(String username) {
        this.username = username; return this;
    }
    
    public Parameter password(String password) {
        this.password = password; return this;
    }
    
    public Parameter security(boolean security) {
        this.security = security; return this;
    }
    
    public Parameter usesecurity() {
        this.security = true; return this;
    }
    
    public Parameter createfolderifnotexists() {
        this.createfolder = true; return this;
    }
    
    public Parameter port(int port) {
        this.port = port; return this;
    }
    
    public Parameter protocol(String protocol) {
        this.protocol = protocol; return this;
    }
    
    public Parameter protocol(ProtocolType protocolType) {
        this.protocol = protocolType.getType(); return this;
    }
    
    public Parameter appendFolder(String appendFolder) {
        this.appendFolder = appendFolder; return this;
    }
    
    public Parameter uploadfilename(String uploadfilename) {
        this.uploadfilename = uploadfilename; return this;
    }
    
    public Parameter uploadpath(String uploadpath) {
        this.uploadpath = uploadpath; return this;
    }
    
    public Parameter serverid(Integer serverid) {
        this.serverid = serverid; return this;
    }
    
    public Parameter companyid(Integer companyid) {
        this.companyid = companyid; return this;
    }
    
    public Parameter manualconfig(boolean manualconfig) {
        this.manualconfig = manualconfig; return this;
    }
    
    public Parameter attachmentTargetType(AttachmentTargetType attachmentTargetType) {
        this.attachmentTargetType = attachmentTargetType; return this;
    }
    
    public Parameter attachmentTargetId(Integer attachmentTargetId) {
        this.attachmentTargetId = attachmentTargetId; return this;
    }
    
    public Parameter storeDb(boolean storeDb) {
        this.storeDb = storeDb; return this;
    }
    
    public Parameter conf(String conf) {
        this.conf = conf; return this;
    }
}
