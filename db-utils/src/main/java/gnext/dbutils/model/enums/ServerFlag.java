/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.dbutils.model.enums;

/**
 * Trong 1 công ty có thể có nhiều server khác nhau,
 * do vậy việc xác định chính xác server nào dựa vào enum này.
 * Ứng với mỗi flag sẽ có 1 thư mục mẫu, cái này khi ứng dụng chạy
 * thực tế sẽ lấy theo truờng folder_server trong
 * DB (cái này người dùng sẽ sử dụng màn hình để nhập đường dẫn).
 * @author daind
 */
public enum ServerFlag {
    COMMON(1, "/upload/", "Lưu logo công ty, ảnh members."),
    SOUND( 2, "/upload/sound", "Lưu file âm thanh"),
    MAIL(  3, "/upload/mail", "Lưu attachment của mail"),
    ISSUE( 5, "/upload/issue", "Lưu ảnh liên quan tới Issue"),
    JOB_LOG( 6, "/upload/job", "Lưu file log của bảng crm_batch theo mẫu: /[companyid]/[batchid]/[datetime]/[batchid].[date].System.currentTimeMillis().log");
    
    private final int id;
    private final String folder;
    private final String desc;
    
    private ServerFlag(int id, String folder, String desc) {
        this.id = id;
        this.folder = folder;
        this.desc = desc;
    }

    public int getId() { return id; }
    
    public String getFolder(){return folder;}
    
    @Override public String toString() {
        return String.valueOf(this.id);
    }
}

