package org.lastresponders.tracker.store;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.lastresponders.tracker.data.TripPosition;
import org.lastresponders.tracker.exception.BadDataException;
import org.lastresponders.tracker.exception.NoDataException;
import org.mortbay.log.Log;

import com.google.api.services.sheets.v4.model.ValueRange;

public class DataCache {
	public static enum CacheKey {
		PROGRESS_ROUTE(Duration.standardHours(1)), 
		PROGRESS_POINT(Duration.standardMinutes(10)), 
		PLAN_ROUTE(Duration.standardHours(12));

		private final Duration expirationDuration;

		CacheKey(Duration d) {
			this.expirationDuration = d;
		}

		Duration getDuration() {
			return expirationDuration;
		}

	};

	@Inject
	GoogleDataStore googleDataStore;

	@Inject
	GoogleSheetData googleSheetData;

	private Map<CacheKey, DateTime> cacheExpireMap = new HashMap<CacheKey, DateTime>();
	private List<TripPosition> resampledRoute = null;
	private ValueRange valueRange = null;
	private TripPosition lastPoint = null;

	// progress
	public TripPosition getLastProgressPosition(String journeyId) throws NoDataException {
		if (lastPoint == null || DateTime.now().isAfter(cacheExpireMap.get(CacheKey.PROGRESS_POINT))) {
			Log.info("refeshing PROGRESS_POINT cache");
			cacheExpireMap.put(CacheKey.PROGRESS_POINT, DateTime.now().plus(CacheKey.PROGRESS_POINT.getDuration()));
			lastPoint = googleDataStore.fetchLastPoint(journeyId);
		}
		return lastPoint;
	}

	public List<TripPosition> progressResampledRoute(String journeyId) throws NoDataException {
		if (resampledRoute == null || DateTime.now().isAfter(cacheExpireMap.get(CacheKey.PROGRESS_ROUTE))) {
			Log.info("refeshing PROGRESS_ROUTE cache");
			cacheExpireMap.put(CacheKey.PROGRESS_ROUTE, DateTime.now().plus(CacheKey.PROGRESS_ROUTE.getDuration()));
			resampledRoute = googleDataStore.fetchResampledRoute(journeyId);
		}
		return resampledRoute;
	}
	
	public List<TripPosition> progressResampledRoute(String journeyId, Date defaultDate) {
		if (resampledRoute == null || DateTime.now().isAfter(cacheExpireMap.get(CacheKey.PROGRESS_ROUTE))) {
			Log.info("refeshing PROGRESS_ROUTE cache");
			cacheExpireMap.put(CacheKey.PROGRESS_ROUTE, DateTime.now().plus(CacheKey.PROGRESS_ROUTE.getDuration()));
			resampledRoute = googleDataStore.fetchResampledRoute(journeyId, defaultDate);
		}
		return resampledRoute;
	}
	
	// planned
		public ValueRange getPlannedRouteData(String journeyId) throws BadDataException {
			if (valueRange == null || DateTime.now().isAfter(cacheExpireMap.get(CacheKey.PLAN_ROUTE))) {
				Log.info("refeshing PLAN_ROUTE cache");
				cacheExpireMap.put(CacheKey.PLAN_ROUTE, DateTime.now().plus(CacheKey.PLAN_ROUTE.getDuration()));
				valueRange = googleSheetData.getPlannedSheet();
			}

			return valueRange;
		}
		
		public void refreshPlannedRouteData(String journeyId) throws BadDataException {
			Log.info("refeshing PLAN_ROUTE cache");
			cacheExpireMap.put(CacheKey.PLAN_ROUTE, DateTime.now().plus(CacheKey.PLAN_ROUTE.getDuration()));
			valueRange = googleSheetData.getPlannedSheet();
		
		}
		

}
