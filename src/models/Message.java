package models;

import java.io.Serializable;
import java.sql.Timestamp;

public class Message implements Serializable
{
	private String from;
	private String to;
	private Timestamp sentTime;

	public Message(String from,String to,Timestamp sentTime)
	{
		this.from=from;
		this.to=to;
		this.sentTime=sentTime;
	}
	public String getFrom()
	{
		return from;
	}
	public String getTo()
	{
		return to;
	}
	public Timestamp getSentTime()
	{
		return sentTime;
	}
	public String toString()
	{
		String s=from+": "+" "+sentTime+"\n";
		return s;
	}
}