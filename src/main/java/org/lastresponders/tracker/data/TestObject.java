package org.lastresponders.tracker.data;

import java.io.Serializable;
import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public  class TestObject implements Serializable {
	/**
	 * JJust a dummy object
	 */
	private static final long serialVersionUID = 1L;

	private Date dateTime;
	
	private String string;

	public TestObject() {
		
	}
	
	public TestObject(String string2, Date time) {
		this.string = string2;
		this.dateTime = time;
	}

	public Date getDateTime() {
		return dateTime;
	}

	public void setDateTime(Date timeStamp) {
		this.dateTime = timeStamp;
	}

	public String getString() {
		return string;
	}

	public void setString(String string) {
		this.string = string;
	}
}
