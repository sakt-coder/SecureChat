package client;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import javafx.application.Platform;
import models.Message;
import models.SystemMessage;

public class ClientReceiver implements Runnable {

	Client client;
	ClientWindowController controller;
	StringBuilder msb;

	public ClientReceiver(Client client) {
		this.client=client;
		msb=new StringBuilder();
	}
	public void setController(ClientWindowController controller) {
		this.controller=controller;
	}
	public void run() {
		try {
			listen();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	private void listen()throws Exception {
		while(true) {
			Object obj=client.tos.readObject();
			if(obj instanceof Message) {

				//get privatekey
				String table=client.username+"PrivateKey";
				String query="SELECT * FROM "+table;
				Statement st=client.connection.createStatement();
				ResultSet result=st.executeQuery(query);

				String privateKey=null;
				while(result.next())
					privateKey=result.getString("privateKey");

				KeyFactory factory=KeyFactory.getInstance("RSA");
        		PrivateKey prk=(PrivateKey)factory.generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKey)));

				//decrypt message
				Message ms=(Message)obj;
				String AESKeyString = RSADecrypt(ms.AESKey , prk);

				byte[] decodedKey = Base64.getDecoder().decode(AESKeyString);
				SecretKey AESKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");

				String content = AESDecrypt(ms.content, AESKey);

				//Append message to database
				query="INSERT INTO "+client.username+"MessageTable VALUES ('"+ms.from+"', '"+content+"', '"+ms.sentTime+"')";
				st=client.connection.createStatement();
				st.executeUpdate(query);

				//Append message to screen
				String s=ms.from+": "+content+" "+ms.sentTime+"\n";
				msb.append(s);
				Platform.runLater(new Runnable(){
					public void run(){
						controller.MessageLabel.setText(msb.length()==0?"You have no messages":msb.toString());
					}
				});
			} else if(obj instanceof SystemMessage) {
				SystemMessage sm=(SystemMessage)obj;

				Platform.runLater(new Runnable(){
					public void run(){
						controller.LoginStatus.setText(sm.note);
						if(sm.note.equals("Login Success")) {
							controller.isLogged = true;
							try {
								displayMessages();
							} catch(Exception e) { }
						}
						else if(sm.note.equals("Logged Out"))
							controller.isLogged=false;
					}
				});
				if(sm.note.equals("Logged Out")){
					client.tos.close();
					break;
				}
			} else if(obj instanceof String) {
		        KeyFactory factory=KeyFactory.getInstance("RSA");
				PublicKey puk=(PublicKey)factory.generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode((String)obj)));
		        controller.publicKey=puk;
			}
		}
	}
	void displayMessages()throws Exception {
		String table=client.username+"MessageTable";
		String query="SELECT * FROM "+table;
		Statement st=client.connection.createStatement();
		ResultSet result=st.executeQuery(query);

		while(result.next()) {
			String from = result.getString("Sender");
			String content = result.getString("Message");
			Timestamp sentTime = result.getTimestamp("Time");
			String s=from+": "+content+" "+sentTime+"\n";
			msb.append(s);
		}
		Platform.runLater(new Runnable(){
			public void run(){
				controller.MessageLabel.setText(msb.length()==0?"You have no messages":msb.toString());
			}
		});
	}
	private static String RSADecrypt(String cipherText, PrivateKey privateKey)throws Exception {
		Cipher cipher=Cipher.getInstance("RSA");
		cipher.init(Cipher.DECRYPT_MODE,privateKey);
		byte[] plainText = cipher.doFinal(Base64.getDecoder().decode(cipherText));
		return new String(plainText);
	}
	private static String AESDecrypt(String cipherText, SecretKey AESKey)throws Exception {
		Cipher cipher=Cipher.getInstance("AES");
		cipher.init(Cipher.DECRYPT_MODE, AESKey);
		byte[] plainText = cipher.doFinal(Base64.getDecoder().decode(cipherText));
		return new String(plainText);
	}
}