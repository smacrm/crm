/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.model;

import gnext.mailapi.mail.SendEmail;
import gnext.util.UploadedFileExt;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.faces.model.SelectItem;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author HUONG
 */
public class MailMode implements Serializable{

    private static final long serialVersionUID = -2443068824807904266L;
    
    /** 送信元 */
    @Getter @Setter
    private SendEmail sendMail = new SendEmail();

    /** 宛先リスト */
    @Getter @Setter
    private String to;

    @Getter @Setter
    private String header;

    @Getter @Setter
    private String footer;

    @Getter @Setter
    private String replyBody;

    /** 送信日 */
    @Getter @Setter
    private Date date;

    /** 添付ファイルのリスト */
    @Getter @Setter
    private List<UploadedFileExt> attachs = new ArrayList<>();

    @Getter @Setter
    private List<SelectItem> tos = new ArrayList<>();
    @Getter @Setter
    private List<SelectItem> ccs = new ArrayList<>();
    @Getter @Setter
    private List<SelectItem> bccs = new ArrayList<>();
    /** ファイルMBSizeのリスト */
//    @Getter @Setter
//    public List<String> sizes = new ArrayList<>();

    /** Dailog閉じる */
//    @Getter @Setter
//    private boolean close;
}

