package org.lastresponders.tracker.service;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.lastresponders.tracker.adapter.DelormeAdapter;
import org.lastresponders.tracker.data.TripPosition;
import org.lastresponders.tracker.data.TripStatus;
import org.lastresponders.tracker.store.DataCache;
import org.lastresponders.tracker.store.GoogleDataStore;

import com.google.api.services.sheets.v4.model.ValueRange;

public class JourneyService {
	private static final Logger log = Logger.getLogger(JourneyService.class.getName());

	private static final String SPREADSHEET_ID = "1tiVCjheex7q5c-N5ZHWQ9nP9ZbGRCoNRTWAWds09GzA";
	private static final String SPREADSHEET_SHEETID = "Route without Kyrgyzstan";
	private static final String SPREADSHEET_RANGE = "A2:T76";

	private static SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

	private List<TripPosition> cachedValue = null;
	private boolean cacheValid = true;

	@Inject
	DelormeAdapter delormeAdapter;

	@Inject
	DataCache dataCache;

	

	public List<TripPosition> plannedRoute() {
		List<TripPosition> ret = new ArrayList<TripPosition>();

		// if(cachedValue != null && cacheValid ) {
		// return cachedValue;
		// }
		//
		try {
			String range = SPREADSHEET_SHEETID + "!" + SPREADSHEET_RANGE;
			ValueRange response = SheetsUtil.getSheet().spreadsheets().values().get(SPREADSHEET_ID, range).execute();

			List<List<Object>> values = response.getValues();

			if (values == null || values.size() == 0) {
				System.out.println("No data found.");
			} else {
				Iterator<List<Object>> iterator = values.iterator();
				iterator.next(); // skip first two lines
				iterator.next();
				while (iterator.hasNext()) {
					List<Object> row = iterator.next();
					if (row.size() >= 18) {
						System.out.printf("%s, %s, [%s, %s]\n", row.get(1), row.get(5), row.get(18), row.get(19));
						Date formattedDate = formatter.parse((String) row.get(1));
						ret.add(new TripPosition(Double.parseDouble((String) row.get(18)),
								Double.parseDouble((String) row.get(19)), formattedDate));
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// cachedValue = ret;
		return ret;
	}

	public List<TripPosition> progressRoute(String journeyId) {
		return dataCache.progressRoute(journeyId);		
	}

	public TripPosition progressPosition(String journeyId) {
		return dataCache.getLastPosition(journeyId);		
	}

	public TripStatus plannedStatus(String journeyId) {
		return dataCache.plannedStatus(journeyId);
	}

	public TripStatus progressStatus(String journeyId) {
		return dataCache.progressStatus(journeyId);
	}
	
}
