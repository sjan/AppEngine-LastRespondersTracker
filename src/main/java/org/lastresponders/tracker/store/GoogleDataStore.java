package org.lastresponders.tracker.store;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.lastresponders.tracker.data.TripPosition;
import org.lastresponders.tracker.exception.NoDataException;
import org.springframework.util.Assert;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

public class GoogleDataStore {
	private static final Logger log = Logger.getLogger(GoogleDataStore.class.getName());
	private static final int FETCH_LIMIT = 2000;

	private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

	public void addTripPositions(String journeyId, List<TripPosition> newPoints) {
		log.info("addPoints: " + newPoints.size());

		for (TripPosition point : newPoints) {
			datastore.put(dataStoreEntity(point, journeyId));
		}
	}

	public void addResampledTripPositions(String journeyId, List<TripPosition> newPoints) {
		log.info("addResampledTripPositions: " + newPoints.size());

		for (TripPosition point : newPoints) {
			datastore.put(dataStoreResampledEntity(point, journeyId));
		}
	}

	private Entity dataStoreEntity(TripPosition position, String journeyId) {
		Entity gpsPoint = new Entity(journeyId + "-position", position.getMessageId());
		gpsPoint.setProperty("lat", position.getLatitude());
		gpsPoint.setProperty("long", position.getLongitude());
		gpsPoint.setProperty("dateTime", position.getDateTime());
		gpsPoint.setProperty("id", position.getMessageId());
		gpsPoint.setProperty("journeyId", journeyId);
		return gpsPoint;
	}

	private Entity dataStoreResampledEntity(TripPosition position, String journeyId) {
		Entity gpsPoint = new Entity(journeyId + "-position-resampled", position.getMessageId());
		gpsPoint.setProperty("lat", position.getLatitude());
		gpsPoint.setProperty("long", position.getLongitude());
		gpsPoint.setProperty("dateTime", position.getDateTime());
		gpsPoint.setProperty("id", position.getMessageId());
		gpsPoint.setProperty("journeyId", journeyId);
		return gpsPoint;
	}

	public List<TripPosition> fetchAllPoints(String journeyId) {
		ImmutableList.Builder<TripPosition> listBuilder = ImmutableList.builder();

		Query q = new Query(journeyId + "-position").addSort("dateTime", Query.SortDirection.ASCENDING);

		PreparedQuery query = datastore.prepare(q);
		List<Entity> entityList = query.asList(FetchOptions.Builder.withLimit(FETCH_LIMIT));

		for (Entity entity : entityList) {
			listBuilder.add(tripData(entity));
		}
		return listBuilder.build();
	}

	public List<TripPosition> fetchPointsForDateRange(Date startTime, Date endTime, String journeyId) {
		log.info("fetchPointsForDateRange" + startTime + " " + endTime);
		ImmutableList.Builder<TripPosition> listBuilder = ImmutableList.builder();

		Query q = new Query(journeyId + "-position").addSort("dateTime", Query.SortDirection.ASCENDING);

		Filter journeyFilter = new FilterPredicate("journeyId", FilterOperator.EQUAL, journeyId);
		Filter compositeFilter = null;
		Filter afterDate = null;
		Filter beforeDate = null;
		Filter dateRangeFilter = null;

		if (startTime != null) {
			afterDate = new FilterPredicate("dateTime", FilterOperator.GREATER_THAN_OR_EQUAL, startTime);
		}

		if (endTime != null) {
			beforeDate = new FilterPredicate("dateTime", FilterOperator.LESS_THAN_OR_EQUAL, endTime);
		}

		// nofilter
		if (afterDate == null && beforeDate == null) {
			// get all points
			compositeFilter = journeyFilter;
		} else if (afterDate != null && beforeDate != null) {
			dateRangeFilter = CompositeFilterOperator.and(beforeDate, afterDate);
			compositeFilter = CompositeFilterOperator.and(dateRangeFilter, journeyFilter);
		} else if (afterDate != null) {
			compositeFilter = CompositeFilterOperator.and(afterDate, journeyFilter);
		} else if (beforeDate != null) {
			compositeFilter = CompositeFilterOperator.and(beforeDate, journeyFilter);
		}

		q.setFilter(compositeFilter);

		PreparedQuery query = datastore.prepare(q);
		List<Entity> entityList = query.asList(FetchOptions.Builder.withLimit(FETCH_LIMIT));
		Iterator<Entity> iterator = entityList.iterator();
		while (iterator.hasNext()) {
			Entity e = iterator.next();
			listBuilder.add(tripData(e));
		}

		return listBuilder.build();

	}

	public List<TripPosition> fetchPointsForDay(Date day, String journeyId) {
		Calendar c = Calendar.getInstance();
		c.setTime(day);
		
		Calendar c2 = Calendar.getInstance();
		c2.clear();
		c2.set(Calendar.YEAR, c.get(Calendar.YEAR));
		c2.set(Calendar.DAY_OF_YEAR, c.get(Calendar.DAY_OF_YEAR));
		Date dayStart = c2.getTime();
		c2.add(Calendar.DATE, 1);
		Date dayEnd = c2.getTime(); 
		
		return fetchPointsForDateRange(dayStart, dayEnd, journeyId);
	}

	public TripPosition fetchLastPoint(String journeyId) throws NoDataException {
		Assert.notNull(journeyId, "journeyId was null");

		Query q = new Query(journeyId + "-position").addSort("dateTime", Query.SortDirection.DESCENDING);

		PreparedQuery query = datastore.prepare(q);
		Iterator<Entity> entityIterator = query.asIterator((FetchOptions.Builder.withLimit(1)));
		if (entityIterator.hasNext())
			return tripData(entityIterator.next());
		throw new NoDataException("no points for " + journeyId);
	}

	public TripPosition fetchFirstPoint(String journeyId) throws NoDataException {
		Assert.notNull(journeyId, "journeyId was null");

		Query q = new Query(journeyId + "-position").addSort("dateTime", Query.SortDirection.ASCENDING);
		PreparedQuery query = datastore.prepare(q);
		Iterator<Entity> entityIterator = query.asIterator((FetchOptions.Builder.withLimit(1)));
		if (entityIterator.hasNext())
			return tripData(entityIterator.next());
		throw new NoDataException("no points for " + journeyId);
	}

	public TripPosition fetchResampledFirstPoint(String journeyId) throws NoDataException {
		Assert.notNull(journeyId, "journeyId was null");

		Query q = new Query(journeyId + "-position-resampled").addSort("dateTime", Query.SortDirection.ASCENDING);
		PreparedQuery query = datastore.prepare(q);
		Iterator<Entity> entityIterator = query.asIterator((FetchOptions.Builder.withLimit(1)));
		if (entityIterator.hasNext())
			return tripData(entityIterator.next());
		throw new NoDataException("no points for " + journeyId);
	}

	public TripPosition fetchResampledLastPoint(String journeyId) throws NoDataException {
		Assert.notNull(journeyId, "journeyId was null");

		Query q = new Query(journeyId + "-position-resampled").addSort("dateTime", Query.SortDirection.DESCENDING);
		PreparedQuery query = datastore.prepare(q);
		Iterator<Entity> entityIterator = query.asIterator((FetchOptions.Builder.withLimit(1)));
		if (entityIterator.hasNext())
			return tripData(entityIterator.next());
		throw new NoDataException("no points for " + journeyId);
	}

	public List<TripPosition> fetchResampledRoute(String journeyId) {
		return fetchResampledRoute(journeyId, Optional.<Date>absent(), Optional.<Date>absent());
	}

	public List<TripPosition> fetchResampledRoute(String journeyId, Date startDate) {
		return fetchResampledRoute(journeyId, Optional.of(startDate), Optional.<Date>absent());
	}

	public List<TripPosition> fetchResampledRoute(String journeyId, Optional<Date> startTime, Optional<Date> endTime) {
		Assert.notNull(journeyId, "journeyId was null");

		ImmutableList.Builder<TripPosition> listBuilder = ImmutableList.builder();
		Query q = new Query(journeyId + "-position-resampled").addSort("dateTime", Query.SortDirection.ASCENDING);

		Filter journeyFilter = new FilterPredicate("journeyId", FilterOperator.EQUAL, journeyId);
		Filter compositeFilter = null;
		Filter afterDate = null;
		Filter beforeDate = null;
		Filter dateRangeFilter = null;

		if (startTime.isPresent()) {
			afterDate = new FilterPredicate("dateTime", FilterOperator.GREATER_THAN_OR_EQUAL, startTime.get());
		}

		if (endTime.isPresent()) {
			beforeDate = new FilterPredicate("dateTime", FilterOperator.LESS_THAN_OR_EQUAL, endTime.get());
		}

		if (startTime.isPresent() && endTime.isPresent()) {
			dateRangeFilter = CompositeFilterOperator.and(beforeDate, afterDate);
			compositeFilter = CompositeFilterOperator.and(dateRangeFilter, journeyFilter);
		} else if (startTime.isPresent()) {
			compositeFilter = CompositeFilterOperator.and(afterDate, journeyFilter);
		} else if (endTime.isPresent()) {
			compositeFilter = CompositeFilterOperator.and(beforeDate, journeyFilter);
		} else {
			compositeFilter = journeyFilter;
		}

		q.setFilter(compositeFilter);

		PreparedQuery query = datastore.prepare(q);
		List<Entity> entityList = query.asList(FetchOptions.Builder.withLimit(FETCH_LIMIT));
		for (Entity entity : entityList) {
			listBuilder.add(tripData(entity));
		}

		return listBuilder.build();
	}

	private TripPosition tripData(Entity entity) {
		TripPosition tripPosition = new TripPosition();
		tripPosition.setDateTime((Date) entity.getProperty("dateTime"));
		tripPosition.setLatitude((Double) entity.getProperty("lat"));
		tripPosition.setLongitude((Double) entity.getProperty("long"));
		tripPosition.setMessageId((String) entity.getProperty("id"));
		return tripPosition;
	}

}
