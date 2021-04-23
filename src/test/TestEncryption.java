package test;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Arrays;
import java.util.Base64;

import static client.Constants.AES_KEY_SIZE;
import static client.Constants.RSA_KEY_SIZE;

public class TestEncryption {
    public static void main(String args[])throws Exception {
        SecretKey AESKey = genAESKey();
        String msg = "hello";
        String enc = AESEncrypt(msg, AESKey);
        String dec = AESDecrypt(enc, AESKey);
        System.out.println(dec);
    }
    private static SecretKey genAESKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(AES_KEY_SIZE);
        return keyGenerator.generateKey();
    }
    private static KeyPair genRSAKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen=KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(RSA_KEY_SIZE);
        return keyGen.genKeyPair();
    }
    private static String RSAEncrypt(String plainText, PublicKey publicKey)throws Exception {
        Cipher cipher=Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE,publicKey);
        byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(cipherText);
    }
    private static String RSADecrypt(String cipherText, PrivateKey privateKey)throws Exception {
        Cipher cipher=Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE,privateKey);
        byte[] plainText = cipher.doFinal(Base64.getDecoder().decode(cipherText));
        return new String(plainText);
    }
    private static String AESEncrypt(String plainText, SecretKey AESKey)throws Exception {
        Cipher cipher=Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, AESKey);
        byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(cipherText);
    }
    private static String AESDecrypt(String cipherText, SecretKey AESKey)throws Exception {
        Cipher cipher=Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, AESKey);
        byte[] plainText = cipher.doFinal(Base64.getDecoder().decode(cipherText));
        return new String(plainText);
    }
}
