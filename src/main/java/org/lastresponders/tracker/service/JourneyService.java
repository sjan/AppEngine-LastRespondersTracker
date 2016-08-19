package org.lastresponders.tracker.service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.lastresponders.tracker.adapter.DelormeAdapter;
import org.lastresponders.tracker.data.TripPosition;
import org.lastresponders.tracker.data.TripStatus;
import org.lastresponders.tracker.store.DataCache;
import org.lastresponders.tracker.store.GoogleSheetData;

import com.google.api.services.sheets.v4.model.ValueRange;

public class JourneyService {
	private static final Logger log = Logger.getLogger(JourneyService.class.getName());

	@Inject
	DelormeAdapter delormeAdapter;

	@Inject
	DataCache dataCache;

	public List<TripPosition> plannedRoute() {
		return dataCache.plannedRoute();
	}

	public TripStatus plannedStatus(String journeyId) {
		return dataCache.plannedStatus(journeyId);
	}

	public List<TripPosition> progressRoute(String journeyId) {
		return dataCache.progressRoute(journeyId);		
	}

	public TripPosition progressPosition(String journeyId) {
		return dataCache.getLastPosition(journeyId);		
	}

	public TripStatus  progressStatus(String journeyId) {
		 ValueRange range = dataCache.getPlannedRouteData(journeyId);
		 return GoogleSheetData.extractPlannedStatus(range, Calendar.getInstance().getTime());		 
	}
	
}
