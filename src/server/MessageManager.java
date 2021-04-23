package server;

import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;

import MyTor.TorSocket;
import models.Message;
import models.SignupClass;

public class MessageManager {

	Server server;
	MessageManager(Server server) {
		this.server=server;
	}

	public void insertUser(SignupClass sc)throws Exception {
		String query="INSERT INTO UserTable VALUES ('"+sc.username+"','"+sc.password+"','"+sc.publicKey+"')";
		Statement st=server.connection.createStatement();
		st.executeUpdate(query);
	}

	public void insertMessage(String receiver, Message ms)throws Exception {
		String query="INSERT INTO ServerMessageTable VALUES ('"+ms.from+"', '"+ms.to+"', '"+ms.sentTime+"', '"+ms.content+"', '"+ms.AESKey+"')";
		Statement st=server.connection.createStatement();
		st.executeUpdate(query);
	}

	//returns all the messages sent to this user when he was offline
	public void remove(String username,TorSocket socket)throws Exception {
		String query="SELECT * FROM ServerMessageTable WHERE Receiver = '"+username+"'";
		Statement st=server.connection.createStatement();
		ResultSet result=st.executeQuery(query);
		while(result.next()) {
			String from = result.getString("Sender");
			String to=result.getString("Receiver");
			Timestamp sentTime=result.getTimestamp("SentTime");
			String content=result.getString("Content");
			String AESKey = result.getString("AESKey");
			Message ms=new Message(from, to, sentTime, content, AESKey);
			socket.writeObject(ms);
			socket.flush();
		}
		// delete all removed messages
		query = "DELETE FROM ServerMessageTable WHERE Receiver = '"+username+"'";
		st = server.connection.createStatement();
		st.executeUpdate(query);
	}
	
}