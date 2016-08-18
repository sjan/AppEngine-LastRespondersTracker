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
import org.lastresponders.tracker.store.GoogleDataStore;

public class PollingService {
	private static final Logger log = Logger.getLogger(PollingService.class.getName());

	private Date refreshDate;
	private Date defaultDate;

	private static SimpleDateFormat formatter = new SimpleDateFormat("yy-MM-dd");
	
	@Inject
	GoogleDataStore googleDataStore;

	@Inject
	DelormeAdapter delormeAdapter;

	public PollingService() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(2016, Calendar.AUGUST, 9);
		defaultDate = calendar.getTime();
	}

	public void poll(String journeyId) {
		log.info("poll");

		TripPosition lastPoint = googleDataStore.fetchLastPoint(journeyId);
		
		if(lastPoint == null ) {
			refreshDate = defaultDate;
		} else {
			refreshDate = lastPoint.getDateTime();
		}
		
		List<TripPosition> list = delormeAdapter.callDelorme("StephenJan", refreshDate);
		googleDataStore.addTripPositions(journeyId, list);
		
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
	
	public void resampleRoute(String journeyId) {
		log.info("resampleRoute");
		TripPosition firstPoint = googleDataStore.fetchFirstPoint(journeyId);
		TripPosition lastPoint = googleDataStore.fetchLastPoint(journeyId);
		
//		TripPosition firstResampledPoint = googleDataStore.fetchResampledFirstPoint(journeyId);
//		TripPosition lastResampledPoint = googleDataStore.fetchResampledLastPoint(journeyId);

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(firstPoint.getDateTime());
		
		Calendar calendar2 = Calendar.getInstance();
		calendar2.setTime(lastPoint.getDateTime());
		
		while(calendar.before(calendar2)) {
			List<TripPosition> resampledList = resample(googleDataStore.fetchPointsForDay(calendar.getTime(), journeyId));
			googleDataStore.addResampledTripPositions(journeyId, resampledList);
			calendar.add(Calendar.DATE, 1);
		}	
		
		//List<TripPosition> positionList = googleDataStore.fetchAllPoints(journeyId);

	}

	private List<TripPosition> resample(List<TripPosition> resampledList) {
		
		List <TripPosition> ret = new ArrayList<TripPosition>();
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
					messageId + "-"  +Integer.toString(i++));
			ret.add(tripPosition);
			index = index + increment;			
		}
		
		return ret;
	}
}
