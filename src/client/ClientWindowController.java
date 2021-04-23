package client;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import models.Message;
import models.Request;
import models.SignupClass;
import models.SystemMessage;
import models.User;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Base64;

import static client.Constants.AES_KEY_SIZE;
import static client.Constants.RSA_KEY_SIZE;

public class ClientWindowController {
    @FXML TextField username;
    @FXML PasswordField password;
    @FXML Label MessageLabel;
    @FXML TextArea SendMessageText;
    @FXML TextField SendTo;
    @FXML Label LoginStatus;
    @FXML Label MessageSent;
    Client client;
    int count;
    boolean isLogged;
    PublicKey publicKey;

    private String hash(String s)throws Exception {
        MessageDigest digest=MessageDigest.getInstance("SHA-256");
        byte[] encodedhash=digest.digest(s.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString=new StringBuilder();
        for (byte b : encodedhash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1)
                hexString.append(hex);
        }
        return hexString.toString();
    }

    public void SignIn()throws Exception {
        if (isLogged) {
            LoginStatus.setText("Logout First");
        } else {
            User user=new User(username.getText(),hash(password.getText()));
            client=new Client(this,user.username);
            client.tos.writeObject(user);
            client.tos.flush();
        }
    }
    public void SignUp()throws Exception {
        if (isLogged) {
            LoginStatus.setText("Logout First");
        } else {
            client=new Client(this,username.getText());

            //generate RSA Keys
            KeyPair keyPair = genRSAKeyPair();
            String publicKey=Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
            String privateKey=Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());

            //create PrivateKey Table
            String table=client.username+"PrivateKey";
            String query="CREATE TABLE "+table+" ( PrivateKey varchar(2048) )";
            PreparedStatement preStat=client.connection.prepareStatement(query);
            preStat.executeUpdate();
            //store private key in Private Key Table
            query="INSERT INTO "+table+" VALUES (?)";
            preStat=client.connection.prepareStatement(query);
            preStat.setString(1,privateKey);
            preStat.executeUpdate();

            //create Message Table
            table=client.username+"MessageTable";
            query="CREATE TABLE "+table+" ( Sender varchar(11) , Message text(2000) , Time timestamp )";
            preStat=client.connection.prepareStatement(query);
            preStat.executeUpdate();

            SignupClass temp=new SignupClass(username.getText(),hash(password.getText()),publicKey);
            client.tos.writeObject(temp);
            client.tos.flush();
        }
    }
    public void logout()throws Exception {
        if(!isLogged)
            return;
        SystemMessage sm=new SystemMessage("Logout");
        client.tos.writeObject(sm);
        client.tos.close();
        MessageLabel.setText("Messages will be displayed here");
    }
    public void SendMessage()throws Exception {
        //request publicKey
        Request req=new Request(SendTo.getText(),client.username);
        client.tos.writeObject(req);
        client.tos.flush();
        
        Thread.sleep(4000);

        //generate AESKey
        SecretKey AESKey = genAESKey();
        String AESKeyString = Base64.getEncoder().encodeToString(AESKey.getEncoded());
        
        //encrypt Message
        String content=SendMessageText.getText();
        content = AESEncrypt(content, AESKey);

        //encrypt Key
        String EncryptedAESKey = RSAEncrypt(AESKeyString, publicKey);

        Message ms=new Message(client.username,SendTo.getText(),new Timestamp(System.currentTimeMillis()), content, EncryptedAESKey);

        client.tos.writeObject(ms);
        client.tos.flush();

        MessageSent.setText("Sent Message"+(count>0?"("+count+++")":""));
    }
    private SecretKey genAESKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(AES_KEY_SIZE);
        return keyGenerator.generateKey();
    }
    private KeyPair genRSAKeyPair() throws NoSuchAlgorithmException {
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
    private static String AESEncrypt(String plainText, SecretKey AESKey)throws Exception {
        Cipher cipher=Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, AESKey);
        byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(cipherText);
    }
}