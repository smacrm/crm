/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.dbutils.model.enums;

/**
 * Bảng crm_attachment sẽ lưu trữ thông tin của File được đẩy lên server,
 * việc lấy đúng file sẽ dựa vào enum này.
 * Ứng với mỗi AttachmentTargetType sẽ chưa ID thực sự của FILE theo company đó.
 * 
 * @author daind
 */
public enum AttachmentTargetType {
    COMPANY(1, "company", "Type for Company"),
    MEMBER(2, "member", "Type for Member"),
    SOUND(3, "sound", "Type for Sound"),
    MAIL(4, "mail", "Type for Mail"),
    ISSUE(5, "issue", "Type for Issue");
    
    private final int id;
    private final String name;
    private final String description;
    
    private AttachmentTargetType(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    
    @Override public String toString() {
        return String.valueOf(this.id);
    }
}