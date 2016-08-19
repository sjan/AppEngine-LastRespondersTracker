package org.lastresponders.tracker.store;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.joda.time.Duration;
import org.lastresponders.tracker.data.TripPosition;
import org.lastresponders.tracker.data.TripStatus;

import com.google.api.services.sheets.v4.model.ValueRange;

public class DataCache {
	public static enum CacheKey {
		PROGRESS_ROUTE(Duration.standardHours(1)),
		PLAN_ROUTE(Duration.standardDays(1)),
		PLAN_STATUS(Duration.standardHours(12));

		CacheKey(Duration d){
		}
		
	};
	
	public Map<CacheKey , Date > cacheMap;
	
	private TripStatus plannedTripStatus;
	//private Duration plannedTripStatusDate;
	
	private TripStatus tripStatus2;
		
	@Inject
	GoogleDataStore googleDataStore;
	
	@Inject
	GoogleSheetData googleSheetData;

	private ValueRange valueRange = null;

	//progress
	public TripPosition getLastPosition(String journeyId) {
		return googleDataStore.fetchLastPoint(journeyId);
	}	

	public List<TripPosition> progressRoute(String journeyId) {
		List <TripPosition> list = googleDataStore.fetchResampledRoute(journeyId);
		list.add(googleDataStore.fetchLastPoint(journeyId));
		return list;
	}

	public ValueRange getPlannedRouteData(String journeyId) {
		valueRange = googleSheetData.getPlannedRouteData();
		return valueRange;
	}
	
	public TripStatus plannedStatus(String journeyId) {
		Calendar c = Calendar.getInstance();
		plannedTripStatus = googleSheetData.extractPlannedStatus(googleSheetData.getPlannedRouteData() , c.getTime());
		return plannedTripStatus;
	}

	public List<TripPosition> plannedRoute() {
		// TODO Auto-generated method stub
		return null;
	}
}
