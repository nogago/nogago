package com.nogago.android.maps.download.task.findcity;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.nogago.android.maps.Constants;
import com.nogago.android.maps.download.task.TrackableTask;

import android.util.Xml;

/**
 * Talks to nogago.com in order to find the lat and lon of the searched for city
 * 
 * @author Raphael Volz
 * 
 */
public final class FindCityTask extends TrackableTask {

	private static final String FIND_CITY_BY_NAME_URL = "http://www.nogago.com/geonames/search?tool=downloader"; // C&featureCode=PPLC&featureCode=PPLX";
	private static final String FIND_CITY_BY_COORDINATE_URL = "http://www.nogago.com/geonames/findNearbyPlaceName?tool=downloader"; 
	private String query;
	private double lon, lat;

	/* UI Thread */
	public FindCityTask(String progressMessage, String query) {
		super(progressMessage);
		this.query = query;
	}
	
	public FindCityTask(String progressMessage, double lon, double lat) {
		super(progressMessage);
		this.lon = lon;
		this.lat = lat;
	}

	/* Separate Thread */
	@Override
	protected Object doInBackground(Object... arg0) {
		// Initialize
		publishProgress(0);


		InputStream inputStream = null;
		try {
			String requestURL = null;
			if(query != null) requestURL = getRequestURL(this.query);
			else requestURL = getRequestURL(this.lon, this.lat);
			inputStream = new URL(requestURL).openStream();
			List<CityCoordinate> poiSearchItems = parse(inputStream);
			return poiSearchItems;
		} catch (Exception e) {
			e.printStackTrace();
			return new FindCityTaskException(this,
					FindCityTaskException.UNABLE_TO_CONNECT);
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
				}
			}
		}
	}

	/**
	 * Parses input XML search response from server
	 * 
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	protected List<CityCoordinate> parse(InputStream inputStream)
			throws IOException, XmlPullParserException {
		XmlPullParser parser = Xml.newPullParser();
		// KXmlParser parser = new KXmlParser();
		int token;
		double lat = Double.NaN;
		double lon = Double.NaN;
		String poiName = "";
		String poiCountryCode = "";
		boolean isEntrySequence = false;
		int i = 0;
		parser.setInput(inputStream, null);

		ArrayList<CityCoordinate> poiSearchItems = new ArrayList<CityCoordinate>();

		do {
			token = parser.next();
			if (token == XmlPullParser.START_TAG) {
				String name = parser.getName();
				if (name.equals("geoname")) {
					isEntrySequence = true;
				} else if (isEntrySequence) {
					if (name.equals("name")) {
						poiName = parser.nextText().trim();
					
						poiName = poiName.replace("/", "");
						poiName = poiName.replace("-", "_");
					} else if (name.equals("lat")) {
						publishProgress(i++);
						lat = Double.parseDouble(parser.nextText().trim());
					} else if (name.equals("lng")) {
						lon = Double.parseDouble(parser.nextText().trim());
					} else if (name.equals("countryCode")) {
						poiCountryCode = parser.nextText().trim();
					}
				}
			} else if (token == XmlPullParser.END_TAG) {
				String name = parser.getName();
				if (name.equals("geoname")) {
					CityCoordinate poi;
					if (Double.isNaN(lat) || Double.isNaN(lon)) {
						; // do nothing - ignore malformed waypoints
					} else {
						poi = new CityCoordinate(lat, lon, poiName,
								poiCountryCode);
						if (!poiSearchItems.contains(poi)) {
							poiSearchItems.add(poi);
						}
					}

					// reset to original condition
					lat = Double.NaN;
					lon = Double.NaN;
					poiName = "";
					poiCountryCode = "";
					isEntrySequence = false;
				}
			}
		} while (token != XmlPullParser.END_DOCUMENT);

		return poiSearchItems;
	}

	/**
	 * Returns search web request URL
	 * 
	 * @param searcingCity
	 *            City name prefix
	 * @return Search URL
	 * @throws UnsupportedEncodingException 
	 */
	private String getRequestURL(String searcingCity) throws UnsupportedEncodingException {
		StringBuffer url = new StringBuffer(FIND_CITY_BY_NAME_URL);
		url.append("&q=").append(URLEncoder.encode(searcingCity, Constants.URL_ENCODING));
		url.append("&lang=").append(
				Locale.getDefault().getISO3Language().substring(0, 1));
		url.append("&maxRows=").append(20);
		return url.toString();
	}
	
	private String getRequestURL(double lon, double lat) throws UnsupportedEncodingException {
		StringBuffer url = new StringBuffer(FIND_CITY_BY_COORDINATE_URL);
		url.append("&lng=").append(lon);
		url.append("&lat=").append(lat);
		url.append("&lang=").append(
				Locale.getDefault().getISO3Language().substring(0, 1));
		url.append("&maxRows=").append(20);
		return url.toString();
	}

}