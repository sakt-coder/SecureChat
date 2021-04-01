package server;

import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;

import MyTor.TorSocket;
import models.Message;
import models.MessageContent;
import models.SignupClass;

public class MessageManager
{
	Server server;
	MessageManager(Server server)
	{
		this.server=server;
	}

	public void insertUser(SignupClass sc)throws Exception
	{
		String query="INSERT INTO UserTable VALUES ('"+sc.username+"','"+sc.password+"','"+sc.publicKey+"')";
		Statement st=server.connection.createStatement();
		st.executeUpdate(query);

		String table=sc.username+"Table";
		query="CREATE TABLE "+table+" ( Sender varchar(11) , Valid int(255) , Message text(2000) , Time timestamp)";
		st.executeUpdate(query);
	}

	public void insertMessage(String receiver, Message ms, MessageContent mc)throws Exception
	{
		String query="INSERT INTO "+receiver+"Table VALUES ('"+ms.getFrom()+"', "+0+", '"+mc.getContent()+"', '"+ms.getSentTime()+"')";
		Statement st=server.connection.createStatement();
		st.executeUpdate(query);
	}

	//returns all the messages sent to this user when he was offline
	public void remove(String username,TorSocket socket)throws Exception
	{
		String table=username+"Table";
		String query="SELECT * FROM "+table;
		Statement st=server.connection.createStatement();
		ResultSet result=st.executeQuery(query);
		while(result.next())
		{
			String sender=result.getString("Sender");
			int valid=result.getInt("Valid");
			Timestamp time=result.getTimestamp("Time");
			String content=result.getString("Message");
			Message ms=new Message(sender,username,time);
			MessageContent mc=new MessageContent(content);
			socket.writeObject(ms);
			socket.writeObject(mc);
			socket.flush();
		}
	}
	
}