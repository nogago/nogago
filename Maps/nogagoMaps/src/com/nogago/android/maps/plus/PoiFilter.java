package com.nogago.android.maps.plus;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.graphics.Bitmap;
import android.util.Xml;

import com.nogago.android.maps.Constants;
import com.nogago.android.maps.LogUtil;
import com.nogago.android.maps.OsmAndFormatter;
import com.nogago.android.maps.R;

import net.osmand.ResultMatcher;
import net.osmand.data.Amenity;
import net.osmand.data.AmenityType;
import net.osmand.data.IndexConstants;
import net.osmand.osm.LatLon;
import net.osmand.osm.MapUtils;

public class PoiFilter implements Comparable<PoiFilter> {

	public final static String STD_PREFIX = "std_"; //$NON-NLS-1$
	public final static String USER_PREFIX = "user_"; //$NON-NLS-1$
	public final static String CUSTOM_FILTER_ID = USER_PREFIX + "custom_id"; //$NON-NLS-1$
	public final static String BY_NAME_FILTER_ID = USER_PREFIX + "by_name"; //$NON-NLS-1$

	private Map<AmenityType, LinkedHashSet<String>> acceptedTypes = new LinkedHashMap<AmenityType, LinkedHashSet<String>>();
	private String filterByName = null;
	private String subtype;
	private String displaySubtype;

	protected String filterId;
	protected String name;
	protected String nameFilter;
	private final boolean isStandardFilter;
	private Bitmap icon;
	private String id;

	protected final OsmandApplication application;

	protected int distanceInd = 1;
	// in kilometers
	protected double[] distanceToSearchValues = new double[] { 1, 2, 3, 5, 10,
			30, 100, 250 };
	private static final Log log = LogUtil.getLog(PoiFilter.class);

	// constructor for standard filters
	public PoiFilter(AmenityType type, OsmandApplication application) {
		this.application = application;
		isStandardFilter = true;
		filterId = STD_PREFIX + type;
		name = type == null ? application
				.getString(R.string.poi_filter_closest_poi) : OsmAndFormatter
				.toPublicString(type, application); //$NON-NLS-1$
		if (type == null) {
			initSearchAll();
		} else {
			acceptedTypes.put(type, null);
		}
	}

	// constructor for standard filters
	public PoiFilter(AmenityType type, String subtype, String id, Bitmap icon,
			OsmandApplication application) {
		this.application = application;
		this.subtype = subtype;
		this.icon = icon;
		this.id = id;
		isStandardFilter = true;
		filterId = STD_PREFIX + id;
		name = type == null ? application
				.getString(R.string.poi_filter_closest_poi) : OsmAndFormatter
				.toPublicString(type, application); //$NON-NLS-1$
		if (type == null) {
			// initSearchAll();
		} else {
			acceptedTypes.put(type, null);
		}
		subtype = subtype.replace("_", " ");
		if (subtype.length() <= 3)
			subtype = subtype.toUpperCase();
		else
			subtype = subtype.substring(0, 1).toUpperCase()
					+ subtype.substring(1);
		this.displaySubtype = subtype;
	}

	// constructor for standard filters
	public PoiFilter(String name, String filterId,
			Map<AmenityType, LinkedHashSet<String>> acceptedTypes,
			OsmandApplication app) {
		application = app;
		isStandardFilter = false;
		if (filterId == null) {
			filterId = USER_PREFIX + name.replace(' ', '_').toLowerCase();
		}
		this.filterId = filterId;
		this.name = name;
		if (acceptedTypes == null) {
			initSearchAll();
		} else {
			this.acceptedTypes.putAll(acceptedTypes);
		}
	}

	public void setNameFilter(String nameFilter) {
		if (nameFilter != null) {
			this.nameFilter = nameFilter.toLowerCase();
		} else {
			clearNameFilter();
		}
	}

	public String getNameFilter() {
		return nameFilter;
	}

	public void clearNameFilter() {
		nameFilter = null;
	}

	private void initSearchAll() {
		for (AmenityType t : AmenityType.values()) {
			acceptedTypes.put(t, null);
		}
		distanceToSearchValues = new double[] { 0.5, 1, 2, 3, 5, 10, 15, 30,
				100 };
	}

	public boolean isSearchFurtherAvailable() {
		return distanceInd < distanceToSearchValues.length - 1;
	}

	public List<Amenity> searchFurther(double latitude, double longitude,
			ResultMatcher<Amenity> matcher) {
		if (distanceInd < distanceToSearchValues.length - 1) {
			distanceInd++;
		}
		List<Amenity> amenityList = searchAmenities(latitude, longitude,
				matcher);
		MapUtils.sortListOfMapObject(amenityList, latitude, longitude);

		return amenityList;
	}

	public String getSearchArea() {
		double val = distanceToSearchValues[distanceInd];
		if (val >= 1) {
			return " < " + ((int) val) + " " + application.getString(R.string.km); //$NON-NLS-1$//$NON-NLS-2$
		} else {
			return " < 500 " + application.getString(R.string.m); //$NON-NLS-1$
		}
	}

	public void clearPreviousZoom() {
		distanceInd = 0;
	}

	public List<Amenity> initializeNewSearch(double lat, double lon,
			int firstTimeLimit, ResultMatcher<Amenity> matcher) {
		clearPreviousZoom();
		List<Amenity> amenityList = searchAmenities(lat, lon, matcher);
		MapUtils.sortListOfMapObject(amenityList, lat, lon);
		if (firstTimeLimit > 0) {
			while (amenityList.size() > firstTimeLimit) {
				amenityList.remove(amenityList.size() - 1);
			}
		}
		return amenityList;
	}

	protected List<Amenity> searchAmenities(double lat, double lon,
			ResultMatcher<Amenity> matcher) {
		double baseDistY = MapUtils.getDistance(lat, lon, lat - 1, lon);
		double baseDistX = MapUtils.getDistance(lat, lon, lat, lon - 1);
		double distance = distanceToSearchValues[distanceInd] * 1000;

		double topLatitude = lat + (distance / baseDistY);
		double bottomLatitude = lat - (distance / baseDistY);
		double leftLongitude = lon - (distance / baseDistX);
		double rightLongitude = lon + (distance / baseDistX);

		List<Amenity> amenities = searchAmenities(lat, lon, topLatitude,
				bottomLatitude, leftLongitude, rightLongitude, matcher);
		if (amenities.isEmpty()) {
			if (distance > Constants.POI_PERIMETER_LIMIT)
				distance = Constants.POI_PERIMETER_LIMIT;
			amenities = searchAmentitiesOnline(lat, lon, distance, matcher);
		}
		return amenities;
	}

	public ResultMatcher<Amenity> getResultMatcher(
			final ResultMatcher<Amenity> matcher) {
		if (nameFilter != null) {
			final boolean en = OsmandApplication.getSettings().USE_ENGLISH_NAMES
					.get();
			return new ResultMatcher<Amenity>() {
				@Override
				public boolean publish(Amenity object) {
					if (!OsmAndFormatter.getPoiStringWithoutType(object, en)
							.toLowerCase().contains(nameFilter)
							|| (matcher != null && !matcher.publish(object))) {
						return false;
					}
					return true;
				}

				@Override
				public boolean isCancelled() {
					return false || (matcher != null && matcher.isCancelled());
				}
			};
		}
		return matcher;
	}

	public List<Amenity> searchAmentitiesOnline(double lat, double lon,
			double distance, ResultMatcher<Amenity> matcher) {
		StringBuffer sb = new StringBuffer(Constants.POI_SEARCH_URL);
		sb.append("?lat=" + lat);
		sb.append("&lon=" + lon);
		sb.append("&perimeter=" + distance);
		return searchAmentitiesOnline(sb.toString(), matcher);
	}

	public List<Amenity> searchAmentitiesOnline(double n, double w, double s,
			double e, ResultMatcher<Amenity> matcher) {
		StringBuffer sb = new StringBuffer(Constants.POI_SEARCH_URL);
		sb.append("?n=" + n);
		sb.append("&s=" + s);
		sb.append("&w=" + w);
		sb.append("&e=" + e);
		return searchAmentitiesOnline(sb.toString(), matcher);
	}

	public List<Amenity> searchAmentitiesOnline(String urlString,
			ResultMatcher<Amenity> matcher) {
		List<Amenity> amenityList = new LinkedList<Amenity>();
		try {
			StringBuffer sb = new StringBuffer(urlString);
			if (id != null) {
				sb.append("&type=" + this.id);
			} else if (!acceptedTypes.isEmpty()) {
				sb.append("&type="
						+ this.acceptedTypes.keySet().toArray(
								new AmenityType[] {})[0].getDefaultTag());
			}
			URL url = new URL(sb.toString());

			InputStream stream = url.openStream();
			XmlPullParser parser = Xml.newPullParser();
			parser.setInput(stream, "UTF-8"); //$NON-NLS-1$
			int eventType;
			Amenity a = null;
			double aLat = 0, aLon = 0;

			while ((eventType = parser.next()) != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_TAG) {
					if (parser.getName().equals("entry")) { //$NON-NLS-1$
						a = new Amenity();
						a.setId(System.currentTimeMillis());
					} else if (parser.getName().equals("type")) { //$NON-NLS-1$
						String[] types = parser.nextText().split("-");
						a.setType(AmenityType.fromString(types[0]));
						a.setSubType(types[1]);
					} else if (parser.getName().equals("name")) { //$NON-NLS-1$
						a.setName(parser.nextText());
					} else if (parser.getName().equals("name_en")) { //$NON-NLS-1$
						a.setEnName(parser.nextText());
					} else if (parser.getName().equals("phone")) { //$NON-NLS-1$
						a.setPhone(parser.nextText());
					} else if (parser.getName().equals("opening_hours")) { //$NON-NLS-1$
						a.setOpeningHours(parser.nextText());
					} else if (parser.getName().equals("url")) { //$NON-NLS-1$
						a.setSite(parser.nextText());
					} else if (parser.getName().equals("lat")) { //$NON-NLS-1$
						aLat = Double.parseDouble(parser.nextText());
					} else if (parser.getName().equals("lon")) { //$NON-NLS-1$
						aLon = Double.parseDouble(parser.nextText());
					}
				}

				if (eventType == XmlPullParser.END_TAG) {
					if (parser.getName().equals("entry")) { //$NON-NLS-1$
						if (matcher == null || matcher.publish(a)) {
							a.setLocation(aLat, aLon);
							if (!amenityList.contains(a))
								amenityList.add(a);
							a = null;
						}
					}
				}
			}
			stream.close();
		} catch (IOException e) {
			log.error("Error loading name finder poi", e); //$NON-NLS-1$
		} catch (XmlPullParserException e) {
			log.error("Error parsing name finder poi", e); //$NON-NLS-1$
		}
		// MapUtils.sortListOfMapObject(amenityList, lat, lon);
		return amenityList;
	}

	protected List<Amenity> searchAmenities(double lat, double lon,
			double topLatitude, double bottomLatitude, double leftLongitude,
			double rightLongitude, final ResultMatcher<Amenity> matcher) {

		return application.getResourceManager().searchAmenities(this,
				topLatitude, leftLongitude, bottomLatitude, rightLongitude,
				lat, lon, matcher);
	}

	public List<Amenity> searchAgain(double lat, double lon) {
		List<Amenity> amenityList = searchAmenities(lat, lon, null);
		MapUtils.sortListOfMapObject(amenityList, lat, lon);
		return amenityList;
	}

	public String getName() {
		return name;
	}

	/**
	 * @param type
	 * @return null if all subtypes are accepted/ empty list if type is not
	 *         accepted at all
	 */
	public Set<String> getAcceptedSubtypes(AmenityType type) {
		if (!acceptedTypes.containsKey(type)) {
			return Collections.emptySet();
		}
		return acceptedTypes.get(type);
	}

	public boolean isTypeAccepted(AmenityType t) {
		return acceptedTypes.containsKey(t);
	}

	public boolean acceptTypeSubtype(AmenityType t, String subtype) {
		if (t == null || subtype == null || acceptedTypes == null
				|| this.subtype == null)
			return true;
		if (!acceptedTypes.containsKey(t) || !this.getSubtype().equals(subtype)) {
			return false;
		}
		return true;
	}

	public void clearFilter() {
		acceptedTypes = new LinkedHashMap<AmenityType, LinkedHashSet<String>>();
	}

	public boolean areAllTypesAccepted() {
		if (AmenityType.values().length == acceptedTypes.size()) {
			for (AmenityType a : acceptedTypes.keySet()) {
				if (acceptedTypes.get(a) != null) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	public void setTypeToAccept(AmenityType type, boolean accept) {
		if (accept) {
			acceptedTypes.put(type, new LinkedHashSet<String>());
		} else {
			acceptedTypes.remove(type);
		}
	}

	public void setMapToAccept(Map<AmenityType, List<String>> newMap) {
		Iterator<Entry<AmenityType, List<String>>> iterator = newMap.entrySet()
				.iterator();
		acceptedTypes.clear();
		while (iterator.hasNext()) {
			Entry<AmenityType, List<String>> e = iterator.next();
			if (e.getValue() == null) {
				acceptedTypes.put(e.getKey(), null);
			} else {
				acceptedTypes.put(e.getKey(),
						new LinkedHashSet<String>(e.getValue()));
			}
		}
	}

	public String buildSqlWhereFilter() {

		if (this instanceof NameFinderPoiFilter) {
			String nameQuery = ((NameFinderPoiFilter) this).getQuery();
			if (nameQuery != null && nameQuery.trim().length() > 0) {
				StringBuilder b = new StringBuilder();
				b.append("("); //$NON-NLS-1$
				b.append("name LIKE '%" + nameQuery + "%'");
				b.append(" OR ");
				b.append("name_en LIKE '%" + nameQuery + "%'");
				b.append(")");
				return b.toString();
			}
		}

		if (areAllTypesAccepted()) {
			return null;
		}
		assert IndexConstants.POI_TABLE != null : "use constants here to show table usage "; //$NON-NLS-1$
		if (acceptedTypes.size() == 0) {
			return "1 > 1"; //$NON-NLS-1$
		}
		StringBuilder b = new StringBuilder();
		b.append("("); //$NON-NLS-1$
		boolean first = true;
		for (AmenityType a : acceptedTypes.keySet()) {
			if (first) {
				first = false;
			} else {
				b.append(" OR "); //$NON-NLS-1$
			}
			b.append("(type = '").append(AmenityType.valueToString(a)).append("'"); //$NON-NLS-1$ //$NON-NLS-2$
			if (acceptedTypes.get(a) != null || subtype != null) {
				LinkedHashSet<String> list = acceptedTypes.get(a);
				b.append(" AND subtype IN ("); //$NON-NLS-1$
				if (subtype != null) {
					b.append("'");
					b.append(subtype);
					b.append("'");
				} else {
					boolean bfirst = true;
					for (String s : list) {
						if (bfirst) {
							bfirst = false;
						} else {
							b.append(", "); //$NON-NLS-1$
						}
						b.append("'").append(s).append("'"); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
				b.append(")"); //$NON-NLS-1$
			}
			b.append(")"); //$NON-NLS-1$
		}
		b.append(")"); //$NON-NLS-1$
		return b.toString();
	}

	public Map<AmenityType, LinkedHashSet<String>> getAcceptedTypes() {
		return new LinkedHashMap<AmenityType, LinkedHashSet<String>>(
				acceptedTypes);
	}

	public void selectSubTypesToAccept(AmenityType t,
			LinkedHashSet<String> accept) {
		acceptedTypes.put(t, accept);
	}

	public String getFilterId() {
		return filterId;
	}

	public String getFilterByName() {
		return filterByName;
	}

	public void setFilterByName(String filterByName) {
		this.filterByName = filterByName;
	}

	public boolean isStandardFilter() {
		return isStandardFilter;
	}

	public OsmandApplication getApplication() {
		return application;
	}

	public String getSubtype() {
		return this.subtype;
	}

	public Bitmap getIcon() {
		return this.icon;
	}

	public String getId() {
		return this.id;
	}

	public String getDisplaySubtype() {
		return this.displaySubtype;
	}

	@Override
	public int compareTo(PoiFilter another) {
		return this.subtype.compareTo(another.subtype);
	}
}
