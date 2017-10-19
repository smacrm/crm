/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.security.twofa;

import gnext.bean.Member;
import gnext.util.ResourceUtil;
import java.net.URLEncoder;
import org.apache.commons.lang3.StringUtils;
import org.jboss.aerogear.security.otp.Totp;
import org.jboss.aerogear.security.otp.api.Base32;

/**
 *
 * @author daind
 */
public class TimeBasedOneTimePasswordUtil {
    public static final String QR_PREFIX = "https://chart.googleapis.com/chart?chs=200x200&chld=M%%7C0&cht=qr&chl=";
    
    /**
     * Kiem tra xac thuc nguoi dung thuc hien Two Factor Authentication.
     * @param member nguoi dung thuc hien login.
     * @param code ma nguoi dung nhap.
     * @return 
     */
    public static boolean validation(Member member, String code) {
        if(!member.isUsing2FA()) return true;
        Totp totp = new Totp(member.getSecret());
        if (!StringUtils.isNumeric(code) || !totp.verify(code)) return false;
        return true;
    }
    
    /**
     * Ham tra ve QR-IMAGE.
     * Ma cu: ResourceUtil.QR_PREFIX + URLEncoder.encode(String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s", ResourceUtil.APP_NAME, member.getMemberLoginId(), member.getSecret(), ResourceUtil.APP_NAME), "UTF-8");
     * @param member
     * @return
     * @throws Exception 
     */
    public static String qrImageUrl(Member member) throws Exception {
        // tao secret-key cho nguoi dung.
        if(member.isUsing2FA() && StringUtils.isEmpty(member.getSecret()))
            member.setSecret(Base32.random());
        
        // tra ve QR Image url.
        StringBuilder sb = new StringBuilder(QR_PREFIX);
        addOtpAuthPart(member.getMemberLoginId(), member.getSecret(), sb);
        return sb.toString();
    }
    
    private static void addOtpAuthPart(String memberLoginId, String memberSecret, StringBuilder sb) throws Exception {
        sb.append(URLEncoder.encode(String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s", ResourceUtil.APP_NAME, memberLoginId, memberSecret, ResourceUtil.APP_NAME), "UTF-8"));
    }
}
