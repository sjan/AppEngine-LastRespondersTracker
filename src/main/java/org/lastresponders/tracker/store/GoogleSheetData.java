package org.lastresponders.tracker.store;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.lastresponders.tracker.data.TripPosition;
import org.lastresponders.tracker.data.TripStatus;
import org.lastresponders.tracker.exception.BadDataException;
import org.lastresponders.tracker.exception.NoDataException;
import org.lastresponders.tracker.service.JourneyService;
import org.lastresponders.tracker.service.SheetsUtil;

import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.common.collect.ImmutableList;

public class GoogleSheetData {
	private static final Logger log = Logger.getLogger(JourneyService.class.getName());

	private static final String SPREADSHEET_ID = "1tiVCjheex7q5c-N5ZHWQ9nP9ZbGRCoNRTWAWds09GzA";
	private static final String SPREADSHEET_PLAN_SHEETID = "PlannedRoute_Data";
	private static final String SPREADSHEET_PLAN_RANGE = "A2:T76";
	
	private static final String SPREADSHEET_PROGRESS_SHEETID = "progress sheet";
	private static final String SPREADSHEET_PROGRESS_RANGE = "A2:T76";
	
	private static final String DATE_FORMAT = "dd/MM/yyyy";

	private static final int PLAN_SHEET_COLUMNS = 11;
	private static final int PLAN_SHEET_COLUMN_DATE = 1;
	private static final int PLAN_SHEET_COLUMN_LAT = 6;
	private static final int PLAN_SHEET_COLUMN_LONG = 7;
	private static final int PLAN_SHEET_COLUMN_DISTANCE = 11;

	public ValueRange getPlannedSheet() throws BadDataException {
		log.info("getPlannedSheet");

		try {
			return SheetsUtil.getSheet().spreadsheets().values()
					.get(SPREADSHEET_ID, SPREADSHEET_PLAN_SHEETID + "!" + SPREADSHEET_PLAN_RANGE).execute();
		} catch (IOException e) {
			throw new BadDataException(e);
		}
	}

	public ValueRange getProgressSheet() throws BadDataException {
		log.info("getProgressSheet");

		try {
			return SheetsUtil.getSheet().spreadsheets().values()
					.get(SPREADSHEET_ID, SPREADSHEET_PROGRESS_SHEETID + "!" + SPREADSHEET_PROGRESS_RANGE).execute();
		} catch (IOException e) {
			throw new BadDataException(e);
		}
	}
	
	public static List<TripPosition> extractPlannedRoute(ValueRange valueRange)
			throws NoDataException, BadDataException {
		ImmutableList.Builder<TripPosition> listBuilder = ImmutableList.builder();

		try {
			List<List<Object>> values = valueRange.getValues();

			if (values == null || values.size() == 0) {
				throw new NoDataException("valueRange empty");
			} else {
				Iterator<List<Object>> iterator = values.iterator();
				while (iterator.hasNext()) {
					List<Object> row = iterator.next();
					if (row.size() >= PLAN_SHEET_COLUMNS) {
						Date formattedDate = new SimpleDateFormat(DATE_FORMAT).parse((String) row.get(PLAN_SHEET_COLUMN_DATE));
						listBuilder.add(new TripPosition(Double.parseDouble((String) row.get(PLAN_SHEET_COLUMN_LAT)),
								Double.parseDouble((String) row.get(PLAN_SHEET_COLUMN_LONG)), formattedDate));
					}
				}
			}
		} catch (ParseException e) {
			e.printStackTrace();
			throw new BadDataException(e);
		}
		return listBuilder.build();
	}

	public static TripStatus extractPlannedStatus(ValueRange valueRange, Date date)
			throws NoDataException, BadDataException {
		try {
			List<List<Object>> values = valueRange.getValues();
			if (values == null || values.size() == 0) {
				throw new NoDataException("valueRange empty");
			} else {
				Iterator<List<Object>> iterator = values.iterator();
				while (iterator.hasNext()) {
					List<Object> row = iterator.next();
					if (row.size() >= PLAN_SHEET_COLUMNS) {
						Date sheetDate = new SimpleDateFormat(DATE_FORMAT).parse((String) row.get(PLAN_SHEET_COLUMN_DATE));
						if (sameDay(date, sheetDate)) {
							Double distance = Double.parseDouble((String) row.get(PLAN_SHEET_COLUMN_DISTANCE));
							return new TripStatus(distance, date);
						}
					}
				}
			}
		} catch (ParseException e) {
			e.printStackTrace();
			throw new BadDataException(e);
		}
		return new TripStatus(Double.valueOf(0), date);
	}

	public static boolean sameDay(Date date1, Date date2) {
		Calendar calendarDate = Calendar.getInstance();
		calendarDate.setTime(date1);

		Calendar calendarDate2 = Calendar.getInstance();
		calendarDate2.setTime(date2);

		return (calendarDate.get(Calendar.YEAR) == calendarDate2.get(Calendar.YEAR)
				&& calendarDate.get(Calendar.DAY_OF_YEAR) == calendarDate2.get(Calendar.DAY_OF_YEAR));

	}
}
