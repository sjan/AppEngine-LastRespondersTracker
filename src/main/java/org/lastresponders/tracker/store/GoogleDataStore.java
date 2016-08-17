package org.lastresponders.tracker.store;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.lastresponders.tracker.data.TripPosition;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;

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

	private Entity dataStoreEntity(TripPosition position, String journeyId) {
		Entity gpsPoint = new Entity(journeyId + "-position", position.getMessageId());
		gpsPoint.setProperty("lat", position.getLatitude());
		gpsPoint.setProperty("long", position.getLongitude());
		gpsPoint.setProperty("dateTime", position.getDateTime());
		gpsPoint.setProperty("journeyId", journeyId);
		return gpsPoint;
	}

	public List<TripPosition> fetchPointsFromDatastore(String journeyId) {
		List<TripPosition> ret = new ArrayList<TripPosition>();

		Query q = new Query(journeyId + "-position").addSort("dateTime", Query.SortDirection.ASCENDING);

		PreparedQuery query = datastore.prepare(q);
		List<Entity> entityList = query.asList(FetchOptions.Builder.withLimit(FETCH_LIMIT));

		for (Entity entity : entityList) {
			ret.add(tripData(entity));
		}
		return ret;
	}

	public TripPosition tripData(Entity entity) {
		TripPosition tripPosition = new TripPosition();
		tripPosition.setDateTime((Date) entity.getProperty("dateTime"));
		tripPosition.setLatitude((Double) entity.getProperty("lat"));
		tripPosition.setLongitude((Double) entity.getProperty("long"));
		tripPosition.setMessageId((String) entity.getProperty("journeyId"));
		return tripPosition;
	}

}
