/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.dbutils.util;

import javax.mail.internet.InternetAddress;

/**
 *
 * @author daind
 */
public class StringUtil {

    private static final int NOT_FOUND = -1;
    public static final char EXTENSION_SEPARATOR = '.';
    private static final char UNIX_SEPARATOR = '/';
    private static final char WINDOWS_SEPARATOR = '\\';

    /**
     * Hàm xử lí xóa kí tự cuối cùng l trong String.
     * @param s
     * @param l
     * @return 
     */
    public static String killLastCharacter(StringBuilder s, String l) {
        if (s.indexOf(l) > 0) s.deleteCharAt(s.lastIndexOf(l));
        return s.toString();
    }

    /**
     * Hàm kiểm tra null hoặc empty của 1 String.
     * @param cs
     * @return 
     */
    public static boolean isEmpty(final CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    /**
     * Hàm xử lí lấy Extension từ tên file.
     * @param filename
     * @return 
     */
    public static String getExtension(final String filename) {
        if (filename == null) return null;
        final int index = indexOfExtension(filename);
        if (index == NOT_FOUND) {
            return "";
        } else {
            return filename.substring(index + 1);
        }
    }

    private static int indexOfExtension(final String filename) {
        if (filename == null) return NOT_FOUND;
        final int extensionPos = filename.lastIndexOf(EXTENSION_SEPARATOR);
        final int lastSeparator = indexOfLastSeparator(filename);
        return lastSeparator > extensionPos ? NOT_FOUND : extensionPos;
    }

    private static int indexOfLastSeparator(final String filename) {
        if (filename == null) return NOT_FOUND;
        final int lastUnixPos = filename.lastIndexOf(UNIX_SEPARATOR);
        final int lastWindowsPos = filename.lastIndexOf(WINDOWS_SEPARATOR);
        return Math.max(lastUnixPos, lastWindowsPos);
    }
    
    /**
     * Hàm xử lí trả về mảng InternetAddress từ mảng String định dạng mail.
     * @param emails
     * @return
     * @throws Exception 
     */
    public static InternetAddress[] getAddress(String[] emails) throws Exception {
        InternetAddress[] addressTo = new InternetAddress[emails.length];
        for (int i = 0; i < emails.length; i++)
            addressTo[i] = new InternetAddress(emails[i]);
        return addressTo;
    }
}
