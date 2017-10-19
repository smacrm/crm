/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.util;

import gnext.controller.issue.bean.PersitBean;
import gnext.model.authority.UserModel;
import gnext.util.InterfaceUtil.HTML;
import gnext.utils.InterfaceUtil.COLS;
import gnext.utils.InterfaceUtil.ISSUE_TYPE;
import static gnext.utils.StringUtil.PREFIX_LOGIN_ID;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.regex.Pattern;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author gnextadmin
 */
public class StringUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(StringUtil.class);
    
    public static String FIELD_NAME_PREFIX = "field-";
    public static String FIELD_ID_PATTERN = "^" + StringUtil.FIELD_NAME_PREFIX + "([0-9]+)$";
    

    public static String arrayToString(String[] array, String symbol) {
        String val = StringUtils.EMPTY;
        if (array == null || array.length <= 0) return val;
        for (int i = 0; i < array.length; i++) {
            if(StringUtils.isEmpty(array[i])) continue;
            if (i == 0) {
                val += array[i];
            } else {
                val += StringUtils.isEmpty(symbol) ? "_" + array[i] : symbol + array[i];//addStr;
            }
        }
        return val;
    }

    public static String listToString(List<SelectItem> items, boolean id) {
        if(items.isEmpty()) return null;
        StringBuilder b = new StringBuilder();
        for(SelectItem item:items) {
            if(item == null) continue;
            if(id) {
                b.append(item.getValue()).append(",");
            } else {
                b.append(item.getLabel()).append(",");                
            }
        }
        if(b == null || b.length() <= 0) return null;
        String val = b.toString();
        if(val.endsWith(",")) return val.substring(0, val.length() - 1);
        return val;
    }

    public static List<SelectItem> getNewList(List<SelectItem> nItems, List<SelectItem> oItems) {
        if(nItems.isEmpty()) return oItems;
        for(SelectItem newItem:nItems) {
            if(newItem == null
                    || !NumberUtils.isDigits(String.valueOf(newItem.getValue()))
                    || StringUtil.isExistsInList((Integer) newItem.getValue(), oItems)) continue;
            oItems.add(newItem);
        }
        return oItems;
    }

    public static int getRandomNumberInRange(int min, int max) {
        if (min >= max) throw new IllegalArgumentException("max must be greater than min");
        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }

    public static String substring(String from, int num) {
        if (from.length() <= num) return from;
        return from.substring(0, num) + "...";
    }

    /**
     * Hàm xóa kí tự l ở cuối 1 string.
     * @param s
     * @param l
     * @return 
     */
    public static String killLastCharacter(StringBuilder s, String l) {
        if (s.indexOf(l) > 0) s.deleteCharAt(s.lastIndexOf(l));
        return s.toString();
    }

    /**
     * Hàm chuyển đổi string dạng HTML sang dạng TEXT.
     * phục vụ cho việc hiển thị dữ liệu ngẵn dạng mô tả.
     * @param html
     * @return 
     */
    public static String html2text(String html) {
        if(StringUtils.isEmpty(html)) return StringUtils.EMPTY;
        try {
            return Jsoup.parse(html).text();
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }
        return html;
    }
    
    public static final String generatePrefixLoginId(final String memberLoginId, final Integer companyId){
        StringBuilder memberLoginIdBuilder = new StringBuilder();
        String memberLoginIdDisplay = memberLoginId;
        String memberLoginIdPrefix = String.format(PREFIX_LOGIN_ID, companyId);
        memberLoginIdBuilder.append(memberLoginIdPrefix)
                .append(memberLoginIdDisplay);
        return memberLoginIdBuilder.toString();
    }

    /**
     * Hàm thay thế các kí tự xuống sang mã HTML.
     * @param html
     * @param replace
     * @return 
     */
    public static String nl2br(String html,String replace) {
        if(StringUtils.isEmpty(html)) return StringUtils.EMPTY;
        return html.replaceAll("(\r\n|\n)+", replace);
    }

    public static boolean isExistsInArray(String page, String[] pages){
        if(page == null || page.isEmpty()) return false;
        for(String next: pages) {
            if(!next.equals(page)) continue;
            return true;
        }
        return false;
    }

    public static String getField(String field, List<SelectItem> fields){
        if(field == null || field.isEmpty() || fields == null || fields.size() <= 0) return StringUtils.EMPTY;
        for(SelectItem item: fields) {
            if(item == null || !field.equals(String.valueOf(item.getValue()))) continue;
            return item.getLabel();
        }
        return field;
    }

    public static boolean isExistsInList(Integer id, List<SelectItem> fields){
        if(id == null || fields == null || fields.size() <= 0) return false;
        for(SelectItem item: fields) {
            if(item == null
                    || !NumberUtils.isDigits(String.valueOf(item.getValue()))
                    || !Objects.equals(id, Integer.valueOf(item.getValue().toString()))) continue;
            return true;
        }
        return false;
    }
    
    /**
     * Hàm xử lí chuyển origin sang Charset.UTF-8.
     * @param origin
     * @return 
     */
    public static String string2utf8(String origin) {
        if(StringUtils.isEmpty(origin)) return StringUtils.EMPTY;
        return new String(origin.getBytes(), Charset.forName("UTF-8"));
    }
    
    public static void main(String[] args){
        String t = "issue_id_issue_id";
        String p = WordUtils.capitalizeFully(t, '_').replaceAll("_", "");
        p = Character.toLowerCase(p.charAt(0)) + p.substring(1);
        System.err.println(p);        
    }

    public static Short getSampleTypeId(String key) {
        Short typeId = 0;
        if(StringUtils.isBlank(key)) return typeId;
        if(COLS.SUPPORT_METHOD.equals(key)) {
            return ISSUE_TYPE.SUPPORT;
        }
        if(COLS.SIGNATURE.equals(key)) {
            return ISSUE_TYPE.SIGNATURE;
        }
        if(COLS.MAIL_REQUEST.equals(key)) {
            return ISSUE_TYPE.REQUEST;
        }
        return typeId;
    }

    public static String getRequestMailUrl(String footer, Integer issueId, Integer comId, Integer userId, Date expiredDate, String websiteUrl) {
        if(issueId == null || comId == null || userId == null) return "";
        PersitBean bean = new PersitBean();
        bean.setIssueId(issueId);
        bean.setCompanyId(comId);
        bean.getMemberList().add(userId);
        bean.setExpiredDate(expiredDate);
        String key = bean.getSyncKey();
        
        String link = new StringBuilder(websiteUrl).append("/issue/").append(key).toString();
        
        return (StringUtils.isBlank(footer)?"":footer) +
                StringUtils.repeat(HTML.BR, 2) + HTML.CIRCLE + 
                JsfUtil.getResource().message(
                    UserModel.getLogined().getCompanyId()
                    ,ResourceUtil.BUNDLE_MSG
                    ,"label.request_mail_footer", (Object) null) +
                StringUtils.repeat(HTML.BR, 2) +
                String.format("<a href=\"%s\" style=\"border-collapse: collapse; border-radius: 2px; text-align: center; display: inline-block; border: solid 1px #344c80; background: #4c649b; padding: 7px 16px 11px 16px; text-decoration: none; color: #fff; font-weight: bold\">%s</a>", link,
                            JsfUtil.getResource().message(UserModel.getLogined().getCompanyId(), ResourceUtil.BUNDLE_MSG, "label.system.link.text"));
    }
    
    /**
     * Encrypt string
     * 
     * @author hungpham
     * @param strip
     * @return
     * @throws UnsupportedEncodingException 
     */
    public static String easeyEncrypt(String strip) throws UnsupportedEncodingException {
        byte[] encryptArray = Base64.encodeBase64(strip.getBytes());
        String encstr = new String(encryptArray, "UTF-8");
        return encstr;
    }

    /**
     * Decrypt string
     * 
     * @author hungpham
     * @param secret
     * @return
     * @throws UnsupportedEncodingException 
     */
    public static String easeyDecrypt(String secret) throws UnsupportedEncodingException {
        byte[] dectryptArray = secret.getBytes();
        byte[] decarray = Base64.decodeBase64(dectryptArray);
        String decstr = new String(decarray, "UTF-8");
        return decstr;
    }
    
    /**
     * Hàm kiểm tra tính hợp lệ của mật khẩu.
     * @param password
     * @return 
     */
    public static boolean validatePassword(String password) {
        Pattern upperCasePatten = Pattern.compile("[A-Z ]");
        Pattern lowerCasePatten = Pattern.compile("[a-z ]");
        Boolean flag = true;
        if (password.length() < 6) {
            JsfUtil.addErrorMessage(JsfUtil.getResource().message(ResourceUtil.BUNDLE_VALIDATOR_NAME, "validator.password.lesscharacters"));
            flag =false;
        }
     
        if(!upperCasePatten.matcher(password).find()
                ||!lowerCasePatten.matcher(password).find()){
            JsfUtil.addErrorMessage(JsfUtil.getResource().message(ResourceUtil.BUNDLE_VALIDATOR_NAME, "validator.password.weak"));
            flag =false;
        }
        return flag;
     }

    public static String getMysqlDateAddSup(boolean add, int dates) {
        if(dates <= 0) return StringUtils.EMPTY;
        if(add) return "+" + dates;
        return "-" + dates;
    }

    public static String getStringNullToEmpty(String val) {
        if(val == null) return StringUtils.EMPTY;
        if("null".equalsIgnoreCase(val)) return StringUtils.EMPTY;
        return val;
    }
    
    public static boolean isNullOrEmpty(String val) {
        if(StringUtils.isEmpty(val)) return true;
        if("null".equalsIgnoreCase(val)) return true;
        return false;
    }
}
