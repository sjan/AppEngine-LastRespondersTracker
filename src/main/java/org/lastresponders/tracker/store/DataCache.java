package org.lastresponders.tracker.store;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.lastresponders.tracker.data.TripPosition;
import org.lastresponders.tracker.data.TripStatus;

public class DataCache {
	private TripStatus plannedTripStatus;
	//private Duration plannedTripStatusDate;
	
	private TripStatus tripStatus2;
		
	@Inject
	GoogleDataStore googleDataStore;
	
	@Inject
	GoogleSheetData googleSheetData;
	
	public TripPosition getLastPosition(String journeyId) {
		return googleDataStore.fetchLastPoint(journeyId);
	}

	public List<TripPosition> progressRoute(String journeyId) {
		List <TripPosition> list = googleDataStore.fetchResampledRoute(journeyId);
		list.add(googleDataStore.fetchLastPoint(journeyId));
		return list;
	}

	public TripStatus plannedStatus(String journeyId) {
		if(refeshPlannedStatus()) { //need new plannedPoint
			Calendar c = Calendar.getInstance();
			plannedTripStatus = googleSheetData.extractPlannedStatus(googleSheetData.getPlannedRouteData() , c.getTime());
		} 
		return plannedTripStatus;
	}

	public TripStatus progressStatus(String journeyId) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private boolean refeshPlannedStatus () {
		return true;
	}
}
