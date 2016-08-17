package org.lastresponders.tracker.service;

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
	private String journeyId;

	@Inject
	GoogleDataStore googleDataStore;

	@Inject
	DelormeAdapter delormeAdapter;

	public PollingService() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(2016, Calendar.AUGUST, 9);
		refreshDate = calendar.getTime();
		journeyId = "projectSpark";
	}

	public void poll() {
		log.info("poll");
		List<TripPosition> list = delormeAdapter.callDelorme("StephenJan", refreshDate);

		googleDataStore.addTripPositions(journeyId, list);

		List<TripPosition> positionList = googleDataStore.fetchPointsFromDatastore(journeyId);

	}
}
