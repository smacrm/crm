/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.mailapi.mail;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author daind
 */
public class SendEmail extends Email {
    private static final long serialVersionUID = 4723777810214693805L;

    /** タイトル */
    @Getter @Setter private String subject;
    /** 内容 */
    @Getter @Setter private String message;
    /** 送信先 */
    @Getter @Setter private String from;
    /** TOリスト */
    @Getter @Setter private String[] recipient;
    /** CCリスト */
    @Getter @Setter private String[] cc;
    /** BCCリスト */
    @Getter @Setter private String[] bcc;
    /**  */
    @Getter @Setter private String name;
    /**  */
    @Getter @Setter private String priority;
    
    /** Danh sách đường dẫn tới file attachment. */
    @Getter @Setter private String[] attchFiles;
    /** Danh sách InputStream cùng tên. */
    @Getter @Setter Map<String, InputStream> attachments;
    /** Danh sách File tạm khi gửi có đính kèm InputStream. */
    @Getter @Setter List<File> tempFiles = new ArrayList<>();
}
