package test;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;
import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;

public class TestEncryption {
    public static void main(String args[])throws Exception {
        Serializable obj = "Hello";
        SecretKey AESKey[] = new SecretKey[4];
        for(int i=0;i<4;i++)
            AESKey[i] = genAESKey();
        for(int i=0;i<4;i++)
            obj = AESEncrypt(obj, AESKey[i]);
        for(int i=3;i>=0;i--)
            obj = (Serializable) AESDecrypt((SealedObject) obj, AESKey[i]);
        System.out.println((String)obj);
    }
    private static SecretKey genAESKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(128);
        SecretKey key = keyGenerator.generateKey();
        return key;
    }
    static SealedObject AESEncrypt(Serializable obj, SecretKey AESKey)throws Exception {
        Cipher cipher=Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, AESKey);
        return new SealedObject(obj,cipher);
    }
    static Object AESDecrypt(SealedObject obj, SecretKey AESKey)throws Exception {
        Cipher cipher=Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, AESKey);
        return(obj.getObject(cipher));
    }
    static Object RSADecrypt(SealedObject obj, PrivateKey privateKey)throws Exception {
        Cipher cipher=Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return obj.getObject(cipher);
    }
}
