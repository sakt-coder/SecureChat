package models;

import java.io.Serializable;

public class SystemMessage implements Serializable {

	public String note;

	public SystemMessage(String note) {
		this.note=note;
	}
}