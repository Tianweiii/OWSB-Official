package models.Utils;

import controllers.NotificationController;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

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

    public static String toAttrString(String input) {
        return input.toLowerCase().replace(" ", "_");
    }

    public static String toTableString(String input) {

        String[] split = input.replace("_", " ").split(" ");
        for (int i = 0; i < split.length; i++) {
            split[i] = split[i].substring(0, 1).toUpperCase() + split[i].substring(1).toLowerCase();
        }

        return split.length == 1 ? split[0] : String.join(" ", split);
    }

    public static void adjustPanePosition(NotificationController.popUpPos pos, BorderPane root, Pane pane) {
        pane.boundsInLocalProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue.getWidth() > 0 && newValue.getHeight() > 0) {
                if (pos == NotificationController.popUpPos.CENTER) {
                    double centerX = (root.getWidth() - newValue.getWidth()) / 2;
                    double centerY = (root.getHeight() - newValue.getHeight()) / 2;
                    pane.setLayoutX(centerX);
                    pane.setLayoutY(centerY);

                } else if (pos == NotificationController.popUpPos.TOP) {
                    double centerX = (root.getWidth() - newValue.getWidth()) / 2;
                    double topY = newValue.getHeight() - 20;
                    pane.setLayoutX(centerX);
                    pane.setLayoutY(topY);

                } else if (pos == NotificationController.popUpPos.BOTTOM_RIGHT) {
                    double rightX = root.getWidth() - newValue.getWidth() - 20;
                    double bottomY = root.getHeight() - newValue.getHeight() - 20;
                    pane.setLayoutX(rightX);
                    pane.setLayoutY(bottomY);

                }
            }
        });
    }

    public static String extractNumber(String text) {
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (Character.isDigit(c)) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static String getCapitalLetters(String str) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (Character.isUpperCase(c)) {
                result.append(c);
            }
        }
        return result.toString();
    }
}
