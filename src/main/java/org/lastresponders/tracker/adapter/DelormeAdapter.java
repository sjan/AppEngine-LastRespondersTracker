package org.lastresponders.tracker.adapter;

import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.lastresponders.tracker.data.TripPosition;
import org.springframework.util.Assert;

import com.google.common.base.Optional;

import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import de.micromata.opengis.kml.v_2_2_0.Data;
import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.ExtendedData;
import de.micromata.opengis.kml.v_2_2_0.Feature;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Point;
import de.micromata.opengis.kml.v_2_2_0.TimeStamp;

public class DelormeAdapter {
	private static final Logger log = Logger.getLogger(DelormeAdapter.class.getName());
	public static String DELORME_URL = "https://share.delorme.com";
	public static String DELORME_PATH = "feed/share/";

	private DateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

	public DelormeAdapter() {

	}
	
	public static final String DELORME_START_DATE_PARAM = "d1";
	public static final String DELORME_END_DATE_PARAM = "d2";

	public List<TripPosition> callDelorme(String feed, Optional<Date> startDate, Optional<Date> endDate) {
		List<TripPosition> ret = new ArrayList<TripPosition>();

		try {
			Client client = ClientBuilder.newClient();
			WebTarget target = client.target(DELORME_URL).path(DELORME_PATH + feed);

			if (startDate.isPresent()) {
				target = target.queryParam(DELORME_START_DATE_PARAM, dateformat.format(startDate.get()));
			}

			if (endDate.isPresent()) {
				target = target.queryParam(DELORME_END_DATE_PARAM, dateformat.format(endDate.get()));
			}

			Response response = target.request(MediaType.TEXT_PLAIN_TYPE).get();

			List<Feature> featureList = ((Document) Kml.unmarshal(response.readEntity(InputStream.class)).getFeature())
					.getFeature();
			for (Feature feature : featureList) {
				Folder folder = (Folder) feature;
				for (Feature featureObject : folder.getFeature()) {
					Placemark p = (Placemark) featureObject;
					if (p.getName().equals("")) {
						TimeStamp timestamp = (TimeStamp) p.getTimePrimitive();
						Date date = dateformat.parse(timestamp.getWhen());
						Point point = (Point) p.getGeometry();
						List<Coordinate> coordinates = point.getCoordinates();
						Coordinate coordinate = coordinates.get(0);

						ExtendedData extendedData = p.getExtendedData();
						List<Data> extendedDataList = extendedData.getData();
						String messageId = null;
						for (Data data : extendedDataList) {
							if (data.getName().equals("Id")) {
								messageId = data.getValue();
							}
						}
						ret.add(new TripPosition(coordinate.getLatitude(), coordinate.getLongitude(), date, messageId));
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	public List<TripPosition> callDelorme(String feed, Date startDate) {
		Assert.notNull(startDate);
		Assert.notNull(feed);
		return callDelorme(feed, Optional.of(startDate), Optional.<Date>absent());
	}
}
