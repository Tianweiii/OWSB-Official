package models.Utils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

public class Helper {

    // exactly 8 bytes
    private static final String secretKey = "12345678";
    private static final SecretKey key = new SecretKeySpec(secretKey.getBytes(), "DES");

    public static String DES_Encrypt(String word) {

        try{
            Cipher desCipher;
            desCipher = Cipher.getInstance("DES");

            byte[] text = word.getBytes(StandardCharsets.UTF_8);

            desCipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] textEncrypted = desCipher.doFinal(text);
            return Base64.getEncoder().encodeToString(textEncrypted);

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return "";
    }

    public static String DES_Decrypt(String word) {

        try {
            byte[] decoded = Base64.getDecoder().decode(word);

            Cipher desCipher;
            desCipher = Cipher.getInstance("DES");

            desCipher.init(Cipher.DECRYPT_MODE, key);
            byte[] textDecrypted = desCipher.doFinal(decoded);

            return new String(textDecrypted);

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return "";
    }

    // note: one way, cannot be decrypted
    public static String SHA_Hashing(String base) {
        try {

            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            final byte[] hash = digest.digest(base.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(hash);

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return "";
    }

    public static String MD5_Hashing(String base) {
        try {

            final MessageDigest digest = MessageDigest.getInstance("MD5");
            final byte[] hash = digest.digest(base.getBytes(StandardCharsets.UTF_8));

            BigInteger bigInt = new BigInteger(1, hash);
            return bigInt.toString();

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return "";
    }
}
