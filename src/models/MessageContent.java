package models;

import java.io.Serializable;

public class MessageContent implements Serializable
{
	private String content;

	public MessageContent(String content)
	{
		this.content=content;
	}
	public String getContent()
	{
		return content;
	}
	public String toString()
	{
		return content;
	}
}