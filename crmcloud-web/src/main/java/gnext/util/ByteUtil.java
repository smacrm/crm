/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.util;

import java.math.BigDecimal;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author daind
 */
public final class ByteUtil {
    private static final int CHAR_ONE_BYTE_MASK     = 0xFFFFFF80;
    private static final int CHAR_TWO_BYTES_MASK    = 0xFFFFF800;
    private static final int CHAR_THREE_BYTES_MASK  = 0xFFFF0000;
    private static final int CHAR_FOUR_BYTES_MASK   = 0xFFE00000;
    private static final int CHAR_FIVE_BYTES_MASK   = 0xFC000000;
    private static final int CHAR_SIX_BYTES_MASK    = 0x80000000;
    
    /**
     * Returns the unicode block of a character. The test is optimized to work faster than
     * <CODE>Character.UnicodeBlock.of</CODE> for Japanese characters, but will work slower
     * for other scripts.
     * {@link http://jgloss.sourceforge.net/jgloss-core/jacoco/jgloss.util/StringTools.java.html}
     * @param c
     * @return 
     */
    public static Character.UnicodeBlock unicodeBlockOf(char c) {
        if (c>=0x4e00 && c<0xa000) {
            return Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS;
        } else if (c>=0x30a0 && c<0x3100) {
            return Character.UnicodeBlock.KATAKANA;
        } else if (c>=0x3040) {
            return Character.UnicodeBlock.HIRAGANA;
        } else if (c>=0x3000) {
            return Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION;
        } else if (c < 0x80) {
            return Character.UnicodeBlock.BASIC_LATIN;
        } else {
            return Character.UnicodeBlock.of( c);
        }
    }
    
    public static boolean isKatakana( char c) { return (c>=0x30a0 && c<0x3100); }
    public static boolean isHiragana( char c) { return (c>=0x3040 && c<0x30a0); }
    public static boolean isKana( char c) { return (c>=0x3040 && c<0x3100); }
    public static boolean isCJKUnifiedIdeographs( char c) { return (c>=0x4e00 && c<0xa000); }
    public static boolean isCJKSymbolsAndPunctuation( char c) { return (c>=0x3000 && c<0x3040); }
    public static boolean isKanji( char c) { return (c>=0x4e00 && c<0xa000) ||  c == '\u3005';  }
    public static boolean isKatakana(String s, Integer numberOfByte) {
        for ( int i=0; i<s.length(); i++) {
            char c = s.charAt( i);
            int sizeofbyte = countNbBytesPerChar(c);
            boolean isKatakana = isKatakana(c);
            boolean isByte = numberOfByte == null || sizeofbyte <= numberOfByte;
            if(!isKatakana && !isByte) return false;
        }
        return true;
    }
    
    /**
     * Return the number of bytes that hold an Unicode char.
     *
     * @param car The character to be decoded
     * @return The number of bytes to hold the char. TODO : Should stop after
     * the third byte, as a char is only 2 bytes long.
     */
    public static final int countNbBytesPerChar(char car) {
        if ((car & CHAR_ONE_BYTE_MASK) == 0) return 1;
        if ((car & CHAR_TWO_BYTES_MASK) == 0) return 2;
        if ((car & CHAR_THREE_BYTES_MASK) == 0) return 3;
        if ((car & CHAR_FOUR_BYTES_MASK) == 0) return 4;
        if ((car & CHAR_FIVE_BYTES_MASK) == 0) return 5;
        if ((car & CHAR_SIX_BYTES_MASK) == 0)  return 6;
        return -1;
    }
    
    public interface UNIT {
        String BYTE = "B";
        String K_BYTE = "KB";
        String G_BYTE = "GB";
    }
    
    /**
     * Hàm trả về số bytes cùng đơn vị trong {@link UNIT}.
     * @param size
     * @return 
     */
    public static String getSizeToString(long size) {
        if(size <= 0) return StringUtils.EMPTY;
        if (1024 > size) {
          return size + UNIT.BYTE;
        } else if (1024 * 1024 > size) {
          double dsize = size;
          dsize = dsize / 1024;
          BigDecimal bi = new BigDecimal(String.valueOf(dsize));
          double value = bi.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
          return value + UNIT.K_BYTE;
        } else {
          double dsize = size;
          dsize = dsize / 1024 / 1024;
          BigDecimal bi = new BigDecimal(String.valueOf(dsize));
          double value = bi.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
          return value + UNIT.G_BYTE;
        }
    }
}
