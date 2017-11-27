package gnext.model.authority;

import gnext.bean.Company;
import gnext.bean.Group;
import gnext.bean.Member;
import gnext.security.SecurityService;
import gnext.utils.InterfaceUtil.COMPANY_TYPE;
import gnext.util.UserAgentUtil;
import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

/**
 *
 * @author hungpham
 */
@ManagedBean(name = "userModel", eager = true)
@SessionScoped()
public final class UserModel implements UserDetails, Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(UserModel.class);

    @Getter private Member member;
    @Getter @Setter private short companyBusinessFlag;
    @Setter @Getter private Integer groupId;
    @Setter @Getter private Integer companyId;
    @Setter @Getter private Company company;
    @Setter @Getter private String companyName;
    @Getter @Setter private String companyLogo;
    @Getter @Setter private String companyCopyRight;
    @Setter @Getter private Integer userId = 0;
    @Setter @Getter private String timeZone;
    @Setter @Getter private String username;
    @Setter @Getter private String password;
    @Setter @Getter private String email;
    @Setter @Getter private String firstName;
    @Setter @Getter private String lastName;
    @Setter @Getter private String memberImage;
    @Setter @Getter private String language;
    @Setter @Getter private Locale locale;
    @Setter @Getter private String dateMode; //ja | en | vi
    @Setter @Getter private String theme;
    @Setter @Getter private List<RoleModel> authorities; /* Spring Security related fields*/
    @Setter @Getter private boolean accountNonExpired = true;
    @Setter @Getter private boolean accountNonLocked = true;
    @Setter @Getter private boolean credentialsNonExpired = true;
    @Setter @Getter private boolean enabled = true;
    @Setter @Getter private boolean using2FA = false; //hungd 20161215 2FA
    @Setter @Getter private String secret;
    @Setter @Getter private boolean userAgentSupportSpeech = false;
    @Getter @Setter private byte[] streamBytes; /** sử dụng lưu logo company lấy từ ftp server  */
    @Getter @Setter private byte[] streamBytesForMemberImage;
    @Setter @Getter private boolean loginedOrtherComapny = false;
    
    public boolean hasRemoteLogo() {
        return streamBytes != null && streamBytes.length > 0;
    }
    
    public StreamedContent getStreamedContent() {
        if(streamBytes != null && streamBytes.length > 0) return new DefaultStreamedContent(new ByteArrayInputStream(streamBytes));
        return new DefaultStreamedContent();
    }
    
    public boolean hasRemoteMemberLogo() {
        return streamBytesForMemberImage != null && streamBytesForMemberImage.length > 0;
    }
    
    public StreamedContent getStreamedMemberImageContent() {
        if(streamBytesForMemberImage != null && streamBytesForMemberImage.length > 0) return new DefaultStreamedContent(new ByteArrayInputStream(streamBytesForMemberImage));
        return new DefaultStreamedContent();
    }

    public boolean getCompanyCustomerMode() {
        return (this.companyBusinessFlag == COMPANY_TYPE.CUSTOMER);
    }
    
    public boolean isCompanyStoreMode() {
        return (this.companyBusinessFlag == COMPANY_TYPE.STORE);
    }
    
    public UserModel() { this(null); }
    public UserModel(Member bean) {
        if (bean == null) return;
        this.member = bean;
        
        this.setUserId(bean.getMemberId());
        this.setFirstName(bean.getMemberNameFirst());
        this.setLastName(bean.getMemberNameLast());
        this.setUsername(bean.getMemberLoginId());
        this.setPassword(bean.getMemberPassword());
        this.setEnabled(bean.getMemberDeleted() == 1);
        this.setAccountNonExpired(true);
        this.setCredentialsNonExpired(true);
        this.setAccountNonLocked(bean.getMemberDeleted() == 0);
        this.setMemberImage(bean.getMemberImage());
        this.setTheme(bean.getMemberLayout());
        this.setUsing2FA(bean.getIsUsing2FA() == 1);
        this.setSecret(bean.getSecret());

        Group group = bean.getGroup();
        this.setGroupId(group.getGroupId());
        this.setCompanyId(group.getCompany().getCompanyId());
        this.setCompanyLogo(group.getCompany().getCompanyLogo());
        this.setCompanyName(group.getCompany().getCompanyName());
        this.setCompany(group.getCompany());
        this.setCompanyBusinessFlag(group.getCompany().getCompanyBusinessFlag());
        if (this.getTheme() == null) this.setTheme(group.getCompany().getCompanyLayout());
        
        this.loadFromPrimeFaceConext();
    }

    public boolean isSuperAdmin() {
        return this.userId != null && SecurityService.checkMemberIdIsSuperAdmin(this.userId);
    }

    public void loadFromPrimeFaceConext() {
        FacesContext context = FacesContext.getCurrentInstance();
        
        /** SpeechAPIを対応する、Browerを判断 */
        ExternalContext externalContext = context.getExternalContext();
        this.setUserAgentSupportSpeech(UserAgentUtil.getUserAgentSpeechSuport(externalContext.getRequestHeaderMap().get("User-Agent")));
        
        /** ログイン画面から入力した「言語」コードを取得 */
        Locale locale = context.getViewRoot().getLocale();
        this.setLanguage(locale.getLanguage());
    }
    
    public final static UserModel getLogined() {
        try {
            return (UserModel) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        } catch (Exception e) {
            logger.error("You are authorization faile.", e);
        }
        return new UserModel();
    }
}
