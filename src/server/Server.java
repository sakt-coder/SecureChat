package server;

import MyTor.TorServer;
import MyTor.TorSocket;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;

public class Server {

	public HashMap<String,TorSocket> activeUserMap;
	public MessageManager msh;
	public Connection connection;

	Server()throws Exception {
		activeUserMap=new HashMap<>();
		msh=new MessageManager(this);
		
		Class.forName("com.mysql.cj.jdbc.Driver");
		String url="jdbc:mysql://127.0.0.1:3306/Chat_App";
		connection=DriverManager.getConnection(url,"root", Constants.SQL_PASSWORD);
	}
	public static void main(String[] args)throws Exception {
		Server server=new Server();
		TorServer ss=new TorServer(5000);
		while(true) {
			TorSocket ts=ss.accept();
			ClientHandler auth=new ClientHandler(server,ts);
			Thread t=new Thread(auth);
			t.start();
		}
	}
}