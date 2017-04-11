package com.nogago.android.maps.plus;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

import com.nogago.android.maps.Constants;
import com.nogago.android.maps.LogUtil;
import com.nogago.android.maps.R;

import net.osmand.ResultMatcher;
import net.osmand.data.Amenity;
import net.osmand.data.AmenityType;
import net.osmand.osm.MapUtils;
import net.sf.junidecode.Junidecode;

public class SearchByNameFilter extends PoiFilter {

	public static final String FILTER_ID = PoiFilter.BY_NAME_FILTER_ID; //$NON-NLS-1$
	private static final Log log = LogUtil.getLog(SearchByNameFilter.class);
	private static final int LIMIT = 300;
	
	List<Amenity> searchedAmenities = new ArrayList<Amenity>();
	
	private String query = ""; //$NON-NLS-1$
	
	public SearchByNameFilter(OsmandApplication application) {
		super(application.getString(R.string.poi_filter_by_name), FILTER_ID, new LinkedHashMap<AmenityType, LinkedHashSet<String>>(), application);
		this.distanceToSearchValues = new double[] {1, 2, 5, 10, 20, 30, 100, 250 };
		this.filterId = FILTER_ID;
	}
	
	@Override
	public List<Amenity> searchAgain(double lat, double lon) {
		MapUtils.sortListOfMapObject(searchedAmenities, lat, lon);
		return searchedAmenities;
	}
	
	public String getQuery() {
		return query;
	}
	
	public void setQuery(String query) {
		this.query = query;
	}
	
	@Override
	protected List<Amenity> searchAmenities(double lat, double lon, ResultMatcher<Amenity> matcher) {
		double baseDistY = MapUtils.getDistance(lat, lon, lat - 1, lon);
		double baseDistX = MapUtils.getDistance(lat, lon, lat, lon - 1);
		double distance = distanceToSearchValues[distanceInd] * 1000;
		
		double topLatitude = lat + (distance/ baseDistY );
		double bottomLatitude = lat - (distance/ baseDistY );
		double leftLongitude = lon - (distance / baseDistX);
		double rightLongitude = lon + (distance/ baseDistX);
		
		List<Amenity> amenities = searchAmenities(lat, lon, topLatitude, bottomLatitude, leftLongitude, rightLongitude, matcher);
		return amenities;
	}
	
	@Override
	protected List<Amenity> searchAmenities(double lat, double lon, double topLatitude,
			double bottomLatitude, double leftLongitude, double rightLongitude, ResultMatcher<Amenity> matcher) {
		double distance = distanceToSearchValues[distanceInd] * 1000;
		searchedAmenities.clear();
		
		searchedAmenities = application.getResourceManager().searchAmenitiesByName(query, 
				topLatitude, leftLongitude, bottomLatitude, rightLongitude, lat, lon, distance, matcher);
		
		if(searchedAmenities.isEmpty()){
		
			String viewbox = "viewboxlbrt="+((float) leftLongitude)+","+((float) bottomLatitude)+","+((float) rightLongitude)+","+((float) topLatitude);
			try {
				String urlq = Constants.NOMINATIM_SEARCH_URL+"?q="+URLEncoder.encode(query)+ "&format=xml&addressdetails=1&limit="+LIMIT+"&bounded=1&"+viewbox;
				log.info(urlq);
				URL url = new URL(urlq); //$NON-NLS-1$
				InputStream stream = url.openStream();
				XmlPullParser parser = Xml.newPullParser();
				parser.setInput(stream, "UTF-8"); //$NON-NLS-1$
				int eventType;
				int namedDepth= 0;
				Amenity a = null;
				while ((eventType = parser.next()) != XmlPullParser.END_DOCUMENT) {
					if (eventType == XmlPullParser.START_TAG) {
						if (parser.getName().equals("searchresults")) { //$NON-NLS-1$
							String err = parser.getAttributeValue("", "error"); //$NON-NLS-1$ //$NON-NLS-2$
							if (err != null && err.length() > 0) {
								stream.close();
								return searchedAmenities;
							}
						}
						if (parser.getName().equals("place")) { //$NON-NLS-1$
							namedDepth++;
							if (namedDepth == 1) {
								try {
									a = new Amenity();
									a.setLocation(Double.parseDouble(parser.getAttributeValue("", "lat")), //$NON-NLS-1$//$NON-NLS-2$
											Double.parseDouble(parser.getAttributeValue("", "lon"))); //$NON-NLS-1$//$NON-NLS-2$
									a.setId(Long.parseLong(parser.getAttributeValue("", "place_id"))); //$NON-NLS-1$ //$NON-NLS-2$
									String name = parser.getAttributeValue("", "display_name");  //$NON-NLS-1$//$NON-NLS-2$
									a.setName(name);
									a.setEnName(Junidecode.unidecode(name));
									a.setType(AmenityType.AMENITY);
									a.setSubType(parser.getAttributeValue("", "type"));  //$NON-NLS-1$//$NON-NLS-2$
									if (matcher == null || matcher.publish(a)) {
										if(MapUtils.getDistance(a.getLocation(), lat, lon)<distance) searchedAmenities.add(a);
									}
								} catch (NullPointerException e) {
									log.info("Invalid attributes", e); //$NON-NLS-1$
								} catch (NumberFormatException e) {
									log.info("Invalid attributes", e); //$NON-NLS-1$
								}
							}
						} else if (a != null && parser.getName().equals(a.getSubType())) {
							if (parser.next() == XmlPullParser.TEXT) {
								String name = parser.getText();
								if (name != null) {
									a.setName(name);
									a.setEnName(Junidecode.unidecode(name));
								}
							}
						}
					} else if (eventType == XmlPullParser.END_TAG) {
						if (parser.getName().equals("place")) { //$NON-NLS-1$
							namedDepth--;
							if(namedDepth == 0){
								a = null;
							}
						}
					}
				}
				stream.close();
			} catch (IOException e) {
				log.error("Error loading name finder poi", e); //$NON-NLS-1$
				//lastError = getApplication().getString(R.string.no_internet); //$NON-NLS-1$
			} catch (XmlPullParserException e) {
				log.error("Error parsing name finder poi", e); //$NON-NLS-1$
				//lastError = getApplication().getString(R.string.input_output_error); //$NON-NLS-1$
			}
		}
		
		MapUtils.sortListOfMapObject(searchedAmenities, lat, lon);
		return searchedAmenities;
	}
	
	
	public List<Amenity> getSearchedAmenities() {
		return searchedAmenities;
	}

	

}
