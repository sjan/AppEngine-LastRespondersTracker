package org.lastresponders.tracker.data;

import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TripStatus{
	private Double latitude;
	private Double longitude;
	private Double distance;
	private Date dateTime;
	
	private static final long serialVersionUID = 1L;

	public Date getDateTime() {
		return dateTime;
	}

	public void setDateTime(Date dateTime) {
		this.dateTime = dateTime;
	}

	public Double getDistance() {
		return distance;
	}

	public void setDistance(Double distance) {
		this.distance = distance;
	}

	public TripStatus() {

	}

	public TripStatus(Double distance, Date date) {
		this.dateTime = date;
		this.distance = distance;
		
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
}
