package server;

import java.sql.ResultSet;
import java.sql.Statement;

import MyTor.TorSocket;
import models.Message;
import models.Request;
import models.SignupClass;
import models.SystemMessage;
import models.User;

public class ClientHandler implements Runnable {
	Server server;
	TorSocket socket;
	String username;
	ClientHandler(Server server,TorSocket socket) {
		this.server=server;
		this.socket=socket;
	}
	public void run() {
		try {
			readLogin();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	private void readLogin()throws Exception {
		//Reading Login Details of Client
		Object obj=socket.readObject();

		//If existing user login, then we receive a User object
		if(obj instanceof User) {
			User user=(User)obj;
			if(authenticate(user)) {
				username=user.username;
				sendSystemMessage("Login Success");
				server.msh.remove(user.username,socket);//Display old messages
				startService();
			} else {
				sendSystemMessage("Invalid Login");
			}
			this.socket.close();
		}
		//If new user login, then we receive a SignupClass Object
		else {
			SignupClass temp=(SignupClass)obj;
			server.msh.insertUser(temp);
			sendSystemMessage("Login Success");
			System.out.println("User added");
			username=temp.username;
			startService();
		}
	}

	private void startService()throws Exception {
		server.activeUserMap.put(username,socket);//add user to activeUserMap
		while(true) {
			Object obj=socket.readObject();
			if(obj instanceof Message) {
				Message ms=(Message)obj;

				String receiver = ms.to;
				TorSocket tsTo=find(receiver);
				if(tsTo!=null) { //If user is online
					tsTo.writeObject(ms);
					tsTo.flush();
				} else { //If user is offline
					server.msh.insertMessage(receiver, ms);
				}
			} else if(obj instanceof SystemMessage) {//If we receive a system message to logout
				break;
			} else if(obj instanceof Request) {//Request for PublicKey
				Request req=(Request)obj;
				String query="SELECT PublicKey FROM UserTable WHERE UserName='"+req.username+"'";
				String publicKey=null;

				Statement st=server.connection.createStatement();
				ResultSet rs=st.executeQuery(query);
				if(rs.next())
					publicKey=rs.getString("PublicKey");

				TorSocket tsTo=find(req.from);
				tsTo.writeObject(publicKey);
				tsTo.flush();
			}
		}
		logout();
	}

	private TorSocket find(String receiver) {
		return server.activeUserMap.getOrDefault(receiver,null);
	}

	private void logout()throws Exception {
		server.activeUserMap.remove(username);
		sendSystemMessage("Logged Out");
	}
	private void sendSystemMessage(String note)throws Exception {
		SystemMessage sm=new SystemMessage(note);
		socket.writeObject(sm);
		socket.flush();
	}
	private boolean authenticate(User user)throws Exception {
		String query="SELECT Password FROM UserTable WHERE UserName='"+user.username+"'";
		Statement st=server.connection.createStatement();
		ResultSet rs=st.executeQuery(query);
		if(rs.next()) {
			return user.password.equals(rs.getString("Password"));
		}
		return false;
	}
}