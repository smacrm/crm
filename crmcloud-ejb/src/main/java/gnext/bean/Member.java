/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.bean;

import gnext.interceptors.annotation.QuickSearchField;
import gnext.interceptors.items.MetaDataContent;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author hungpham
 */
@Entity
@Table(name = "crm_member")
@XmlRootElement
public class Member implements CloneSelfDataToDbChild, QuickSearchEntity {
    public static final int SUPER_ADMIN_MEMBER_ID = 1;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "member_id", nullable = false)
    @Setter @Getter
    private Integer memberId;
    
    @Size(max = 30)
    @Column(name = "member_login_id", length = 30)
    @Setter @Getter
    @QuickSearchField(name = "member_login_id", view = true, title = "Login ID")
    private String memberLoginId;
    
    @Size(max = 30)
    @Column(name = "member_code", length = 30)
    @Setter @Getter
    @QuickSearchField(name = "member_code", view = true, title = "label.member.code")
    private String memberCode;
    
    @Size(max = 60)
    @Column(name = "member_password", length = 60)
    @Setter @Getter
    private String memberPassword;
    
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 70)
    @Column(name = "member_name_first", nullable = false, length = 70)
    @QuickSearchField(name = "member_name_first")
    @Setter @Getter
    private String memberNameFirst;
    
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 70)
    @Column(name = "member_name_last", nullable = false, length = 70)
    @QuickSearchField(name = "member_name_last")
    @Setter @Getter
    private String memberNameLast;
    
    @Size(max = 70)
    @Column(name = "member_kana_first", length = 70)
    @QuickSearchField(name = "member_kana_first")
    @Setter @Getter
    private String memberKanaFirst;
    
    @Size(max = 70)
    @Column(name = "member_kana_last", length = 70)
    @QuickSearchField(name = "member_kana_last")
    @Setter @Getter
    private String memberKanaLast;
    
    @Size(max = 20)
    @Column(name = "member_post", length = 20)
    @Setter @Getter
    private String memberPost;
    
    @Column(name = "member_city")
    @Setter @Getter
    private Integer memberCity;
    
    @Size(max = 150)
    @Column(name = "member_address", length = 150)
    @Setter @Getter
    private String memberAddress;
    
    @Size(max = 200)
    @Column(name = "member_address_kana", length = 200)
    @Setter @Getter
    private String memberAddressKana;
    
    @Size(max = 150)
    @Column(name = "member_image", length = 150)
    @Setter @Getter
    private String memberImage;
    
    @Size(max = 20)
    @Column(name = "member_layout", length = 20)
    @Setter @Getter
    private String memberLayout;
    
    @Column(name = "member_firewall")
    @Setter @Getter
    private Short memberFirewall;
    
    @Column(name = "member_global_flag")
    @Setter @Getter
    private Short memberGlobalFlag;
    
    @Column(name = "member_manager_flag")
    @Setter @Getter
    private Short memberManagerFlag;
    
    @Column(name = "member_global_locale")
    @Setter @Getter
    private Short memberGlobalLocale;
    
    @Column(name = "member_deleted")
    @Setter @Getter
    private Short memberDeleted;
    
    @JoinColumn(name = "creator_id", referencedColumnName = "member_id",
            nullable = true, insertable = true, updatable = true)
    @ManyToOne(optional = false)
    @Setter @Getter private Member creator;
    
    @Size(max = 500)
    @Column(name = "member_memo")
    @Setter @Getter
    private String memberMemo;
    
    @Column(name = "member_using_twofa")
    @Setter @Getter
    private Short isUsing2FA = (short)0;
    
    @Size(max = 500)
    @Column(name = "member_secret")
    @Setter @Getter
    private String secret;
    
    @Column(name = "created_time")
    @Temporal(TemporalType.TIMESTAMP)
    @Setter @Getter
    private Date createdTime;
    
    @JoinColumn(name = "updated_id", referencedColumnName = "member_id",
            nullable = true, insertable = true, updatable = true)
    @ManyToOne(optional = false)
    @Setter @Getter private Member updator;
    
    @Column(name = "updated_time")
    @Temporal(TemporalType.TIMESTAMP)
    @Setter @Getter
    private Date updatedTime;
    
    @Column(name = "member_last_login_time")
    @Temporal(TemporalType.TIMESTAMP)
    @Setter @Getter
    private Date lastLoginTime;
    
    @Column(name = "member_last_logout_time")
    @Temporal(TemporalType.TIMESTAMP)
    @Setter @Getter
    private Date lastLogoutTime;
    
    @Column(name = "member_reset_pwd_datetime")
    @Temporal(TemporalType.TIMESTAMP)
    @Setter @Getter
    private Date memberResetPwdDatetime;
    
    @JoinColumn(name = "group_id", referencedColumnName = "group_id", nullable = true, insertable = false, updatable = false)
    @ManyToOne(optional = false)
    @Getter private Group group;
    @Column(name = "group_id", nullable = false, insertable = true, updatable = true)
    @Setter @Getter private Integer groupId;
    public void setGroup(Group group) {
        this.group = group;
        if(group != null) 
            this.groupId = group.getGroupId();
        else 
            this.groupId = null;
    }
    
    @Setter @Getter @Transient private String phoneFaxMailHomepage; /** for quick search module */
    @Setter @Getter @Transient private List<String> listGroupName; /** for import module */
    
    @Column(name = "manual_id")
    @Setter @Getter private Integer manualId;
    
    public Member() {}
    
    public String getMemberNameFull(){
        return StringUtils.defaultString(this.getMemberNameFirst(), "") + StringUtils.defaultString(this.getMemberNameLast(), "");
    }
    
    public boolean isUsing2FA() {
        if(isUsing2FA==null)return false;
        return isUsing2FA == 1;
    }

    public void setUsing2FA(boolean using2FA) {
        this.isUsing2FA = using2FA ? (short)1 : (short)0;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (memberId != null ? memberId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Member)) {
            return false;
        }
        Member other = (Member) object;
        if ((this.memberId == null && other.memberId != null) || (this.memberId != null && !this.memberId.equals(other.memberId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return String.valueOf(memberId);
    }

    @Override
    public List<MetaDataContent> getMetadata() {
        List<MetaDataContent> mdcs = new ArrayList<>();
        
        if(phoneFaxMailHomepage != null && !phoneFaxMailHomepage.isEmpty())
            mdcs.add(new MetaDataContent("phone_fax_mail_homepage", phoneFaxMailHomepage));
        
        return mdcs;
    }

}
