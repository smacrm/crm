/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gnext.dbutils.util;

import java.util.Base64;

/**
 *
 * @author daind
 */
public class EncryptDecrypt {
    private final static String key = "bizcrm";
    private final static int CIPHER_LENGTH = key.length() + 2;

    public static String encrypt(String text) {
        byte[] salt = key.getBytes();
        return Base64.getEncoder().encodeToString(salt) + Base64.getEncoder().encodeToString(text.getBytes());
    }

    public static String decrypt(String encryptKey) {
        if (encryptKey.length() > CIPHER_LENGTH) {
            String cipher = encryptKey.substring(CIPHER_LENGTH);
            return new String(Base64.getDecoder().decode(cipher));
        }
        return null;
    }

    public static void main(String args[]) {
        String encrypt = encrypt("test");
        System.out.println("gnext.dbutils.util.EncryptDecrypt.main()" + encrypt);
        String decrypt = decrypt(encrypt);
        System.out.println("gnext.dbutils.util.EncryptDecrypt.main()" + decrypt);
    }
}
