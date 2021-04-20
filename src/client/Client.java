package client;

import MyTor.TorObjectStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Client {
	public TorObjectStream tos;
	public ClientWindowController controller;
	public String username;
	public Connection connection;

	public Client(ClientWindowController controller,String username) {
		try{
			tos=new TorObjectStream("localhost",5000);
		} catch(Exception e){
			System.out.println("Could not connect to Server");
		}

		try{
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch(ClassNotFoundException e){
			System.out.println("JDBC Driver for SQL not found");
		}
		String url="jdbc:mysql://127.0.0.1:3306/Chat_App";
		try{
			connection=DriverManager.getConnection(url,"root",Constants.SQL_PASSWORD);
		} catch(SQLException e){
			System.out.println("Connection to database could not be made");
		}

		ClientReceiver clientReceiver=new ClientReceiver(this);
		clientReceiver.setController(controller);
		this.username=username;
		this.controller = controller;
    Thread t=new Thread(clientReceiver);
    t.start();
	}
}