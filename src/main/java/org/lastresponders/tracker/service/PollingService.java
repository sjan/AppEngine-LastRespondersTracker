package org.lastresponders.tracker.service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.lastresponders.tracker.adapter.DelormeAdapter;
import org.lastresponders.tracker.data.TripPosition;
import org.lastresponders.tracker.exception.BadDataException;
import org.lastresponders.tracker.exception.NoDataException;
import org.lastresponders.tracker.store.DataCache;
import org.lastresponders.tracker.store.GoogleDataStore;

import com.google.common.collect.ImmutableList;

public class PollingService {
	private static final Logger log = Logger.getLogger(PollingService.class
			.getName());

	private static final String DELORME_FEED = "StephenJan";

	private Date refreshDate;
	private Date journeyStartDate;

	@Inject
	DataCache dataCache;

	@Inject
	GoogleDataStore googleDataStore;

	@Inject
	DelormeAdapter delormeAdapter;

	public PollingService() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(2016, Calendar.AUGUST, 1);
		journeyStartDate = calendar.getTime();
	}

	public void pollGpsPoints(String journeyId) {
		log.info("pollGpsPoints");

		try {
			refreshDate = googleDataStore.fetchLastPoint(journeyId)
					.getDateTime();
		} catch (NoDataException e) {
			refreshDate = journeyStartDate;
		}

		List<TripPosition> list = delormeAdapter.callDelorme(DELORME_FEED,
				refreshDate);
		googleDataStore.addTripPositions(journeyId, list);
	}

	public void pollResampleRoute(String journeyId) throws NoDataException {
		log.info("pollResampleRoute");
		resampleRoute(journeyId);
	}

	// TODO: this can be optimized. we don't need to resample all days
	public void resampleRoute(String journeyId) throws NoDataException {
		log.info("resampleRoute");
		TripPosition firstPoint = googleDataStore.fetchFirstPoint(journeyId);
		TripPosition lastPoint = googleDataStore.fetchLastPoint(journeyId);

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(firstPoint.getDateTime());

		Calendar calendar2 = Calendar.getInstance();
		calendar2.setTime(lastPoint.getDateTime());

		for (Date day : buildDayList(calendar, calendar2)) {
			googleDataStore
					.addResampledTripPositions(journeyId,
							resample(googleDataStore.fetchPointsForDay(day,
									journeyId)));
		}
	}

	public void resampleRouteOptimized(String journeyId) throws NoDataException {
		log.info("resampleRouteOptimized");
		TripPosition firstPoint = googleDataStore
				.fetchResampledLastPoint(journeyId);
		TripPosition lastPoint = googleDataStore.fetchLastPoint(journeyId);

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(firstPoint.getDateTime());

		Calendar calendar2 = Calendar.getInstance();
		calendar2.setTime(lastPoint.getDateTime());

		for (Date day : buildDayList(calendar, calendar2)) {
			List<TripPosition> resampledList = resample(googleDataStore
					.fetchPointsForDay(day, journeyId));
			googleDataStore.addResampledTripPositions(journeyId, resampledList);
		}
	}

	private List<Date> buildDayList(Calendar startTime, Calendar endTime) {
		ImmutableList.Builder<Date> dateListBuilder = new ImmutableList.Builder<Date>();

		Calendar firstDate = Calendar.getInstance();
		Calendar endDate = Calendar.getInstance();

		firstDate.clear();
		firstDate.set(startTime.get(Calendar.YEAR),
				startTime.get(Calendar.MONTH),
				startTime.get(Calendar.DAY_OF_MONTH));

		endDate.clear();
		endDate.set(endTime.get(Calendar.YEAR), endTime.get(Calendar.MONTH),
				endTime.get(Calendar.DAY_OF_MONTH));

		while (firstDate.before(endDate)) {
			dateListBuilder.add(firstDate.getTime());
			firstDate.add(Calendar.DATE, 1);
		}

		return dateListBuilder.build();
	}

	private List<TripPosition> resample(List<TripPosition> resampledList) {
		SimpleDateFormat formatter = new SimpleDateFormat("yy-MM-dd-");

		ImmutableList.Builder<TripPosition> listBuilder = new ImmutableList.Builder<TripPosition>();

		int size = resampledList.size();
		int increment = (size < 4) ? 1 : (size / 4);
		
		int index = 0;
		int dataStoreKey = 0;

		while (index < size) {
			String messageId = formatter.format(resampledList.get(index)
					.getDateTime());

			TripPosition tripPosition = new TripPosition(resampledList.get(
					index).getLatitude(), resampledList.get(index)
					.getLongitude(), resampledList.get(index).getDateTime(),
					messageId + Integer.toString(dataStoreKey++));
			listBuilder.add(tripPosition);
			index = index + increment;
		}
		
		return listBuilder.build();
	}

	public void refreshPlannedRoute(String journeyId) throws BadDataException {
		dataCache.refreshPlannedRouteData(journeyId);
	}
}
