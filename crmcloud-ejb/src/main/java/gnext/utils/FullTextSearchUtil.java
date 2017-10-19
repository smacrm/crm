/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.utils;

/**
 *
 * @author daind
 */
public class FullTextSearchUtil {
    public static String againstNatural(String[] columns, String text) {
        String sql = " MATCH (%s) AGAINST ('%s' IN NATURAL LANGUAGE MODE) ";
        return String.format(sql, String.join(",", columns), text);
    }
    
    public static void main(String[] args) {
        System.out.println(againstNatural(new String[]{"title", "body"}, "database"));
    }
}
