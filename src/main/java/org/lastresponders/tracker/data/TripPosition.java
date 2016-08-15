package org.lastresponders.tracker.data;

import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TripPosition {
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

	private static final long serialVersionUID = 1L;

	private Date dateTime;
	private Double latitude;
	private Double longitude;

	public TripPosition() {
		
	}	

	public TripPosition(Double lat, Double lon, Date date) {
		this.dateTime = date;
		this.latitude = lat;
		this.longitude = lon;
	}	

}
