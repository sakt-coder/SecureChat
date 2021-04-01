package client;

import java.sql.*;
import java.util.*;
import java.security.spec.*;
import java.security.*;
import javax.crypto.*;
import javafx.application.Platform;
import models.Message;
import models.MessageContent;
import models.SystemMessage;

//ClientReceiver is a thread that runs in the background listening for messages
public class ClientReceiver implements Runnable
{
	Client client;
	ClientWindowController controller;
	StringBuilder msb;
	public ClientReceiver(Client client)
	{
		this.client=client;
		msb=new StringBuilder();
	}
	public void setController(ClientWindowController controller)
	{
		this.controller=controller;
	}
	public void run()
	{
		try
		{
			listen();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	private void listen()throws Exception
	{
		while(true)
		{
			Object obj=client.tos.readObject();
			if(obj instanceof Message)
			{
				MessageContent mc=(MessageContent)client.tos.readObject();

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
				String content=mc.getContent();
				Cipher cipher=Cipher.getInstance("RSA");
				cipher.init(Cipher.DECRYPT_MODE,prk);
				content=new String(cipher.doFinal(Base64.getDecoder().decode(content)));
				
				//Append message to screen
				String s=ms.getFrom()+": "+content+" "+ms.getSentTime()+"\n";
				System.out.println(s);
				msb.append(s);
				Platform.runLater(new Runnable(){
					public void run(){
						controller.MessageLabel.setText(msb.length()==0?"You have no messages":msb.toString());
					}
				});
			}
			else if(obj instanceof SystemMessage)
			{
				SystemMessage sm=(SystemMessage)obj;

				Platform.runLater(new Runnable(){
					public void run(){
						controller.LoginStatus.setText(sm.note);
						if(sm.note.equals("Login Success"))
							controller.isLogged=true;
						else if(sm.note.equals("Logged Out"))
							controller.isLogged=false;
					}
				});
				if(sm.note.equals("Logged Out")){
					client.tos.close();
					break;
				}
			}
			else if(obj instanceof String)
			{
		        KeyFactory factory=KeyFactory.getInstance("RSA");
				PublicKey puk=(PublicKey)factory.generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode((String)obj)));
		        controller.publicKey=puk;
			}
		}
	}
}