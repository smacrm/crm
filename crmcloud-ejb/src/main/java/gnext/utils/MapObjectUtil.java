/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.utils;

import gnext.bean.Group;
import gnext.bean.Member;
import gnext.bean.mail.MailData;
import gnext.bean.attachment.Attachment;
import gnext.bean.attachment.Server;

/**
 *
 * @author daind
 */
public class MapObjectUtil {
    /***
     * Hàm xử lí chuyển đổi {@link Server} to {@link gnext.dbutils.model.Server}.
     * @param s
     * @return 
     */
    public static gnext.dbutils.model.Server convert(Server s) {
        gnext.dbutils.model.Server pojo = new gnext.dbutils.model.Server();
        pojo.setCompany_id(s.getCompany().getCompanyId());
        pojo.setCreated_time(s.getCreatedTime());
        pojo.setCreator_id(s.getCreatorId());
        pojo.setServer_deleted(s.getServerDeleted());
        pojo.setServer_flag(s.getServerFlag());
        pojo.setServer_folder(s.getServerFolder());
        pojo.setServer_gnext(s.getServerGnext());
        pojo.setServer_host(s.getServerHost());
        pojo.setServer_id(s.getServerId());
        pojo.setServer_name(s.getServerName());
        pojo.setServer_password(s.getServerPassword());
        pojo.setServer_port(s.getServerPort());
        pojo.setServer_protocol(s.getServerProtocol());
        pojo.setServer_ssl(s.getServerSsl());
        pojo.setServer_type(s.getServerType());
        pojo.setServer_username(s.getServerUsername());
        pojo.setUpdated_id(s.getUpdatedId());
        pojo.setUpdated_time(s.getUpdatedTime());
        return pojo;
    }
    
    /***
     * Hàm xử lí chuyển đổi {@link Attachment} to {@link gnext.dbutils.model.Attachment}.
     * @param p_attachment
     * @return 
     */
    public static Attachment convert(gnext.dbutils.model.Attachment p_attachment) {
        Attachment entity = new Attachment();
        entity.setAttachmentName(p_attachment.getAttachment_name());
        entity.setAttachmentHashName(p_attachment.getAttachment_hash_name());
        entity.setAttachmentExtension(p_attachment.getAttachment_extension());
        entity.setAttachmentMimeType(p_attachment.getAttachment_mime_type());
        entity.setAttachmentPath(p_attachment.getAttachment_path());
        entity.setAttachmentFileSize(p_attachment.getAttachment_file_size());
        entity.setAttachmentDeleted(p_attachment.getAttachment_deleted());
        entity.setCreatedTime(p_attachment.getCreated_time());
        return entity;
    }
    
    /***
     * Hàm xử lí chuyển đổi {@link MailData} to {@link gnext.dbutils.model.MailData}.
     * @param p_md
     * @return 
     */
    public static MailData convert(gnext.dbutils.model.MailData p_md) {
        MailData md = new MailData();
        md.setMailDataUniqueId(p_md.getMail_data_unique_id());
        md.setMailDataHeader(p_md.getMail_data_header());
        md.setMailDataSubject(p_md.getMail_data_subject());
        md.setMailDataFrom(p_md.getMail_data_from());
        md.setMailDataTo(p_md.getMail_data_to());
        md.setMailDataCc(p_md.getMail_data_cc());
        md.setMailDataBcc(p_md.getMail_data_bcc());
        md.setMailDataDatetime(p_md.getMail_data_datetime());
        md.setMailDataSize(p_md.getMail_data_size());
        md.setMailDataReplyReturnPath(p_md.getMail_data_reply_return_path());
        md.setMailDataBody(p_md.getMail_data_body());
        md.setMailDataFolderCode(p_md.getMail_data_folder_code());
        md.setMailDataFolderName(p_md.getMail_data_folder_name());
        md.setMailDataDeleteFlag(p_md.getMail_data_delete_flag().shortValue());
        md.setCreatedTime(p_md.getCreated_time());
        return md;
    }
    
    public static Group convert(final Group source) {
        Group target = new Group();
        target.setGroupMemo(source.getGroupMemo());
        target.setGroupOrder(source.getGroupOrder());
        target.setGroupName(source.getGroupName());
        target.setCompanyId(source.getCompanyId());
        target.setGroupDeleted(source.getGroupDeleted());
        target.setGroupTreeId(source.getGroupTreeId());
        target.setParent(source.getParent());
        
        target.setSource(source.getSource());
        target.setTarget(source.getTarget());
        
        target.setCreatedTime(source.getCreatedTime());
        target.setUpdatedTime(source.getUpdatedTime());
        
        return target;
    }
    
    public static Member convert(final Member source, boolean updateGroup) {
        Member target = new Member();
        target.setMemberAddress(source.getMemberAddress());
        target.setMemberAddressKana(source.getMemberAddressKana());
        target.setMemberCity(source.getMemberCity());
        target.setMemberCode(source.getMemberCode());
        target.setMemberDeleted(source.getMemberDeleted());
        target.setMemberFirewall(source.getMemberFirewall());
        target.setMemberGlobalFlag(source.getMemberGlobalFlag());
        target.setMemberGlobalLocale(source.getMemberGlobalLocale());
        target.setMemberImage(source.getMemberImage());
        target.setMemberKanaFirst(source.getMemberKanaFirst());
        target.setMemberKanaLast(source.getMemberKanaLast());
        target.setMemberLayout(source.getMemberLayout());
        target.setMemberLoginId(source.getMemberLoginId());
        target.setMemberManagerFlag(source.getMemberManagerFlag());
        target.setMemberMemo(source.getMemberMemo());
        target.setMemberNameFirst(source.getMemberNameFirst());
        target.setMemberNameLast(source.getMemberNameLast());
        target.setMemberPassword(source.getMemberPassword());
        target.setMemberPost(source.getMemberPost());
        
        target.setCreatedTime(source.getCreatedTime());
        target.setUpdatedTime(source.getUpdatedTime());
        
        if(updateGroup) target.setGroupId(source.getGroupId());
        
        return target;
    }
}
