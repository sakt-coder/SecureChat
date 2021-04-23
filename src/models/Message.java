package models;

import java.io.Serializable;
import java.sql.Timestamp;

public class Message implements Serializable {

	public String from;
	public String to;
	public Timestamp sentTime;
	public String content;
	public String AESKey;

	public Message(String from,String to,Timestamp sentTime, String content, String AESKey) {
		this.from=from;
		this.to=to;
		this.sentTime=sentTime;
		this.content=content;
		this.AESKey=AESKey;
	}
}