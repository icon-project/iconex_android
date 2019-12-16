package foundation.icon.iconex.util;

import org.spongycastle.util.encoders.Base64;

import java.security.MessageDigest;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by js on 2018. 4. 22..
 */

public class CryptoUtil {

    public static String encryptText(String cryptText, String password) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA256");
        byte[] keyAndIv = md.digest(password.getBytes());

        byte[] key = new byte[16];
        byte[] iv = new byte[16];
        System.arraycopy(keyAndIv, 0, key, 0, 16);
        System.arraycopy(keyAndIv, 16, iv, 0, 16);

        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivSpec);
        byte[] encData = cipher.doFinal(cryptText.getBytes());
        return Base64.toBase64String(encData);
    }

    public static String decryptText(String encText, String password) throws Exception {
        byte[] encData = Base64.decode(encText);

        MessageDigest md = MessageDigest.getInstance("SHA256");
        byte[] keyAndIv = md.digest(password.getBytes());

        byte[] key = new byte[16];
        byte[] iv = new byte[16];
        System.arraycopy(keyAndIv, 0, key, 0, 16);
        System.arraycopy(keyAndIv, 16, iv, 0, 16);

        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivSpec);
        byte[] decData = cipher.doFinal(encData);
        return new String(decData);
    }
}
