package org.lastresponders.tracker.data;

import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TripPosition {
	private Date dateTime;
	private Double latitude;
	private Double longitude;
	private String messageId;

	private static final long serialVersionUID = 1L;

	public Date getDateTime() {
		return dateTime;
	}

	public void setDateTime(Date dateTime) {
		this.dateTime = dateTime;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public TripPosition() {

	}

	public TripPosition(Double lat, Double lon, Date date) {
		this.dateTime = date;
		this.latitude = lat;
		this.longitude = lon;
		this.messageId = null;
	}

	public TripPosition(double latitude2, double longitude2, Date date, String messageId) {
		this.dateTime = date;
		this.latitude = latitude2;
		this.longitude = longitude2;
		this.messageId = messageId;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}
}
