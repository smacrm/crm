/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.mailapi.test;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author daind
 */
public class RegexTest {

    public static void main(String[] args) throws Exception {
        String f2p = "/home/daind/workspace/vnext/document/mail_test.txt";
        String content = FileUtils.readFileToString(new File(f2p), "utf-8");
        
//        System.out.println("gnext.RegexTest.main()" +  content);
//        String regex = "(here)(.*)\\r*\\n*";
//        String regex = "(here)(.*)";
//        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
//        Matcher matcher = pattern.matcher(content);
//        while (matcher.find()) {
//            System.out.print("Start index: " + matcher.start());
//            System.out.print(" End index: " + matcher.end() + " ");
//            System.out.println(matcher.group(2));
//        }
        
//        String val_001 = parseMailContent2Issue("【ABCD】", content, Arrays.asList("【ABCD1】","【ABCD2】"), true);
//        System.out.println("【ABCD】 = " + val_001);
//        
//        String val_002 = parseMailContent2Issue("【ABCD1】", content, Arrays.asList("【ABCD】","【ABCD2】"), false);
//        System.out.println("【ABCD1】 = " + val_002);
//        
//        String val_003 = parseMailContent2Issue("【ABCD2】", content, Arrays.asList(), true);
//        val_003 = StringUtils.strip(val_003);
//        System.out.println("【ABCD2】 = " + val_003);

        parsetFullName("　ユーザ ー名 【ABCD2】");
        
//        System.out.println("gnext.RegexTest.main()" + smoothingPath("/home/ftpuser/upload//mail/1705//"));
        
//        System.out.println("gnext.RegexTest.main()" + getFileName("__CRMCLOUD_REPLACE_ウィキペディアへようこそ.docx"));
    }
    
//    private static void parseEOL(String key, String content) {
//        String regex = "(" + key + ")(.*)\\r*\\n*";
//        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
//        Matcher matcher = pattern.matcher(content);
//        while (matcher.find()) {
//            System.out.println("gnext.RegexTest.parseEOL()" + matcher.group(2));
//        }
//    }
    
    private static String[] parsetFullName(String origin) {
        String[] news = StringUtils.strip(origin).split("\\s+");
        for(String n : news) System.out.println("gnext.RegexTest.parsetFullName()" + n);
        return news;
    }
    
    private static String smoothingPath(String origin) {
        if(origin == null || origin.isEmpty()) return "";
        return origin.replaceAll("/+", "/");
    }
    
    /**
     * Hàm phân cắt mail, sau khi phân cắt sẽ được chuyển tới màn hình issue để tạo mới issue.
     * @param key phân cắt theo key.
     * @param content nội dung mail cần phân cắt.
     * @param otherKeys danh sách key giới hạn phân cắt mail.
     * @param multiline nếu là true giá trị phân cắt được sẽ là nhiều dòng.
     * @return 
     */
    public static String parseMailContent2Issue(String key, String content, List<String> otherKeys, boolean multiline) {
        String content_utf8 = string2utf8(content);
        String key_utf8 = string2utf8(key);
        
        StringBuilder sz = new StringBuilder();
        boolean existsKey = false;
        try (Scanner scanner = new Scanner(content_utf8)) {
            String line = null;
            while (scanner.hasNextLine()) {
                line = scanner.nextLine();
                
                // nếu dòng không chứa key thì bỏ qua và tiếp tục dòng tiếp theo.
                if(existsKey || line.contains(key_utf8)) existsKey = true;
                if(!existsKey) continue;
                
                // nếu sử dụng multiline cần kiểm tra hết các keyword tiếp theo.
                if(multiline) {
                    for(String otherKey : otherKeys) {
                        String otherKey_utf8 = string2utf8(otherKey);
                        if(line.contains(otherKey_utf8)) return sz.toString();
                    }
                }
                
                // lấy text đúng của dòng, nếu dòng chứa key thì bỏ key đó đi.
                String val = line;
                if(line.contains(key_utf8))
                    val = line.substring(line.indexOf(key_utf8) + key_utf8.length(), line.length());
                if(val != null && !val.isEmpty()) sz.append(val).append("\r\n");
                
                // nếu không sử dụng multiline thì cần trả về ngay line này tới người dùng.
                if(!multiline) return val;
            }
        }
        return sz.toString();
    }
    
    public static String string2utf8(String origin) {
        if(StringUtils.isEmpty(origin)) return StringUtils.EMPTY;
        return new String(origin.getBytes(), Charset.forName("UTF-8"));
    }
//    private static String parseIssue(String key, String content, List<String> otherKeys, boolean multiline) {
//        StringBuilder sz = new StringBuilder();
//        String content_utf8 = new String(content.getBytes(), Charset.forName("UTF-8"));
//        String key_utf8 = new String(key.getBytes(), Charset.forName("UTF-8"));
//        
//        boolean existsKey = false;
//        try (Scanner scanner = new Scanner(content_utf8)) {
//            boolean readed = false;
//            String line = null;
//            String line_utf8 = null;
//            while (scanner.hasNextLine()) {
//                if(!readed) line = scanner.nextLine();
//                if(line == null || line.isEmpty()) continue;
//                line_utf8 = new String(line.getBytes(), Charset.forName("UTF-8"));
//                
//                // nếu dòng không chứa key thì bỏ qua và tiếp tục dòng tiếp theo.
//                if(existsKey || line_utf8.contains(key_utf8)) existsKey = true;
//                if(!existsKey) continue;
//                
//                // lấy text đúng của dòng, nếu dòng chứa key thì bỏ key đó đi.
//                String val = null;
//                if(line_utf8.contains(key_utf8)) val = line_utf8.substring(line_utf8.indexOf(key_utf8) + key_utf8.length(), line_utf8.length());
//                else val = line_utf8;
//                if(val != null && !val.isEmpty()) sz.append(val).append("\r\n");
//                
//                // nếu không sử dụng multiline thì cần trả về ngay line này tới người dùng.
//                if(!multiline) return val;
//                
//                // nếu sử dụng multiline cần kiểm tra hết các keyword tiếp theo.
//                line = scanner.nextLine();
//                line_utf8 = new String(line.getBytes(), Charset.forName("UTF-8"));
//                readed = true;
//                for(String otherKey : otherKeys) {
//                    String otherKey_utf8 = new String(otherKey.getBytes(), Charset.forName("UTF-8"));
//                    if(line_utf8.contains(otherKey_utf8)) return sz.toString();
//                }
//            }
//        }
//        return sz.toString();
//    }
    
//    private static String parseIssue(String key, String content, List<String> otherKeys, boolean multiline) {
//        StringBuilder sz = new StringBuilder();
//        String content_utf8 = new String(content.getBytes(), Charset.forName("UTF-8"));
//        String key_utf8 = new String(key.getBytes(), Charset.forName("UTF-8"));
//        
//        boolean existsKey = false;
//        try (Scanner scanner = new Scanner(content_utf8)) {
//            while (scanner.hasNextLine()) {
//                String line = scanner.nextLine();
//                if(existsKey || line.contains(key_utf8)) existsKey = true;
//                if(!existsKey) continue;
//                
//                if(multiline) { // nếu sử dụng multiline cần kiểm tra hết các keyword tiếp theo.
//                    for(String otherKey : otherKeys) {
//                        String otherKey_utf8 = new String(otherKey.getBytes(), Charset.forName("UTF-8"));
//                        if(line.contains(otherKey_utf8)) return sz.toString();
//                    }
//                }
//                
//                
//                String val = null;
//                if(line.contains(key_utf8)) val = line.substring(line.indexOf(key_utf8) + key_utf8.length(), line.length());
//                else val = line;
//                if(val != null && !val.isEmpty()) sz.append(val).append("\r\n");
//                
//                // nếu không sử dụng multiline thì cần trả về ngay line này tới người dùng.
//                if(!multiline) return val;
//            }
//        }
//        return sz.toString();
//    }
    
    private static String getFileName(String origin) {
        try {
            String regex = "(__CRMCLOUD_REPLACE_\\d*__)(.+)";
            Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(origin);
            if(!matcher.find()) return origin;
    //        String prefix = matcher.group(1);
            String suffix = matcher.group(2);
            return suffix;
        } catch (Exception e) {
        }
        return origin;
    }
}
