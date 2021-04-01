package models;

import java.io.Serializable;

public class Request implements Serializable
{
	public String username,from;
	public Request(String username,String from)
	{
		this.username=username;
		this.from=from;
	}
}