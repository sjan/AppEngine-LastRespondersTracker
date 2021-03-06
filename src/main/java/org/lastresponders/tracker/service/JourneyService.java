package org.lastresponders.tracker.service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.lastresponders.tracker.adapter.DelormeAdapter;
import org.lastresponders.tracker.data.TripPosition;
import org.lastresponders.tracker.data.TripStatus;
import org.lastresponders.tracker.exception.BadDataException;
import org.lastresponders.tracker.exception.NoDataException;
import org.lastresponders.tracker.store.DataCache;
import org.lastresponders.tracker.store.GoogleSheetData;

import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.common.collect.ImmutableList;

public class JourneyService {
	private static final Logger log = Logger.getLogger(JourneyService.class.getName());

	public final static double KM_TO_MILE_CONVERSION = 1.609;
	public final static int RADIUS_EARTH = 6371; // Radius of the earth
	
	@Inject
	DelormeAdapter delormeAdapter;

	@Inject
	DataCache dataCache;

	private final Date defaultDate;

	public JourneyService() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(2016, Calendar.AUGUST, 23);
		defaultDate = calendar.getTime();
	}

	public List<TripPosition> plannedRoute(String journeyId) throws NoDataException, BadDataException {
		return GoogleSheetData.extractPlannedRoute(dataCache.getPlannedRouteData(journeyId));
	}

	public TripStatus plannedStatus(String journeyId) throws BadDataException, NoDataException {
		ValueRange range = dataCache.getPlannedRouteData(journeyId);
		return GoogleSheetData.extractPlannedStatus(range, Calendar.getInstance().getTime());
	}

	public List<TripPosition> progressRoute(String journeyId) throws NoDataException {
		ImmutableList.Builder<TripPosition> returnList = ImmutableList.builder();
		returnList.addAll(dataCache.progressResampledRoute(journeyId, defaultDate));
		//addToday's points
		returnList.add(progressPosition(journeyId));
		return returnList.build();
	}

	public TripPosition progressPosition(String journeyId) throws NoDataException {
		return dataCache.getLastProgressPosition(journeyId);
	}

	public TripStatus progressStatus(String journeyId) throws NoDataException {
		double totalDistance = 0;
		List<TripPosition> positionList = dataCache.progressResampledRoute(journeyId, defaultDate);

		TripPosition lastPosition = null;
		for (TripPosition position : positionList) {
			totalDistance += distance(lastPosition, position);
			lastPosition = position;
		}

		TripPosition lastPoint = dataCache.getLastProgressPosition(journeyId);
		totalDistance += distance(lastPosition, dataCache.getLastProgressPosition(journeyId));
		return new TripStatus(totalDistance, lastPoint.getDateTime());
	}

	private static Double distance(TripPosition position1, TripPosition position2) {
		if (position1 == null || position2 == null)
			return new Double(0);
		return distance(position1.getLatitude(), position2.getLatitude(), position1.getLongitude(),
				position2.getLongitude());
	}
	
	private static double distance(double lat1, double lat2, double lon1, double lon2) {
		Double latDistance = Math.toRadians(lat2 - lat1);
		Double lonDistance = Math.toRadians(lon2 - lon1);
		Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) + Math.cos(Math.toRadians(lat1))
				* Math.cos(Math.toRadians(lat2)) * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
		Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

		return RADIUS_EARTH * c / KM_TO_MILE_CONVERSION; // convert to kmeters
	}

}
