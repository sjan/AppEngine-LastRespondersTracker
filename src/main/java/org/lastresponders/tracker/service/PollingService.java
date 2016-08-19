package org.lastresponders.tracker.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.lastresponders.tracker.adapter.DelormeAdapter;
import org.lastresponders.tracker.data.TripPosition;
import org.lastresponders.tracker.exception.NoDataException;
import org.lastresponders.tracker.store.GoogleDataStore;

import com.google.appengine.repackaged.com.google.common.collect.ImmutableList;

public class PollingService {
	private static final Logger log = Logger.getLogger(PollingService.class.getName());

	private Date refreshDate;
	private Date defaultDate;

	
	@Inject
	GoogleDataStore googleDataStore;

	@Inject
	DelormeAdapter delormeAdapter;

	public PollingService() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(2016, Calendar.AUGUST, 23);
		defaultDate = calendar.getTime();
	}

	public void pollGpsPoints(String journeyId) {
		log.info("pollGpsPoints");
		
		try {
			refreshDate = googleDataStore.fetchLastPoint(journeyId).getDateTime();			
		} catch(NoDataException e) {
			refreshDate = defaultDate;
		}
		
		List<TripPosition> list = delormeAdapter.callDelorme("StephenJan", refreshDate);
		googleDataStore.addTripPositions(journeyId, list);
	}
	
	public void pollResampleRoute(String journeyId) throws NoDataException {
		log.info("pollResampleRoute");
		resampleRoute(journeyId);
		
	}
	
	public void test(String journeyId) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(2016, Calendar.AUGUST, 10);
		
		
		List<TripPosition> positionList = googleDataStore.fetchPointsForDay(calendar.getTime(), journeyId);
		for(TripPosition position : positionList) {
			log.info(position.getMessageId() + " " + position.getDateTime());
		}
	}

	//TODO: this can be optimzed. we don't need to resample all days
	public void resampleRoute(String journeyId) throws NoDataException {
		log.info("resampleRoute");
		TripPosition firstPoint = googleDataStore.fetchFirstPoint(journeyId);
		TripPosition lastPoint = googleDataStore.fetchLastPoint(journeyId);

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(firstPoint.getDateTime());
		
		Calendar calendar2 = Calendar.getInstance();
		calendar2.setTime(lastPoint.getDateTime());
		
		for(Date day : buildDayList(calendar , calendar2)) {
			List<TripPosition> resampledList = resample(googleDataStore.fetchPointsForDay(day , journeyId));
			googleDataStore.addResampledTripPositions(journeyId, resampledList);			
		}
	}
	
	public void resampleRouteOptimized(String journeyId) throws NoDataException {
		log.info("resampleRouteOptimized");
		TripPosition firstPoint = googleDataStore.fetchResampledLastPoint(journeyId);
		TripPosition lastPoint = googleDataStore.fetchLastPoint(journeyId);

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(firstPoint.getDateTime());
		
		Calendar calendar2 = Calendar.getInstance();
		calendar2.setTime(lastPoint.getDateTime());
		
		for(Date day : buildDayList(calendar , calendar2)) {
			List<TripPosition> resampledList = resample(googleDataStore.fetchPointsForDay(day , journeyId));
			googleDataStore.addResampledTripPositions(journeyId, resampledList);			
		}
	}

	private List<Date> buildDayList(Calendar calendar1, Calendar calendar2) {
		ImmutableList.Builder<Date> dateListBuilder = new ImmutableList.Builder<Date>();
		
		Calendar firstDate = Calendar.getInstance();
		Calendar endDate = Calendar.getInstance();
		
		firstDate.clear();
		firstDate.set(
				calendar1.get(Calendar.YEAR),
				calendar1.get(Calendar.MONTH),
				calendar1.get(Calendar.DAY_OF_MONTH));
		
		endDate.clear();
		endDate.set(
				calendar2.get(Calendar.YEAR),
				calendar2.get(Calendar.MONTH),
				calendar2.get(Calendar.DAY_OF_MONTH));
		
		while(firstDate.before(endDate)) {
			dateListBuilder.add(firstDate.getTime());
			firstDate.add(Calendar.DATE, 1);			
		}
				
		return dateListBuilder.build();
		
	}

	private List<TripPosition> resample(List<TripPosition> resampledList) {
		SimpleDateFormat formatter = new SimpleDateFormat("yy-MM-dd-");
		
		ImmutableList.Builder<TripPosition> listBuilder = new ImmutableList.Builder<TripPosition>();
		
		int size = resampledList.size();
		int increment = size/4;
		
		if(increment  < 1) {
			increment = 1;
		}
		
		int index=0;
		int i=0;
		
		while(index < size) {
			String messageId = formatter.format(resampledList.get(index).getDateTime());
			
			TripPosition tripPosition = new TripPosition(
					resampledList.get(index).getLatitude(), 
					resampledList.get(index).getLongitude(), 
					resampledList.get(index).getDateTime(), 
					messageId + Integer.toString(i++));
			listBuilder.add(tripPosition);
			index = index + increment;			
		}
		
		return listBuilder.build();
	}
}
