package gl.glue.brahma.util;

import org.apache.commons.codec.binary.Base64;
import play.Play;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class Signing {

    public static String sign(String message) {
        long date = System.currentTimeMillis();
        return sign(message, date);
    }

    public static String sign(String message, long date) {
        String key = Play.application().configuration().getString("application.secret");
        String innerMessage = Long.toString(date) + message;
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            String hash = Base64.encodeBase64String(sha256_HMAC.doFinal(innerMessage.getBytes()));
            return hash + Long.toString(date);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    public static long getTimestamp(String signature) {
        return Long.parseLong(signature.substring(44));
    }

    public static boolean check(String message, String signature) {
        long date = getTimestamp(signature);
        String newSignature = sign(message, date);
        return newSignature.equals(signature);
    }

}
