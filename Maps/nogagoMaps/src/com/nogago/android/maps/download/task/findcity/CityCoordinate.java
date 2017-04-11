package com.nogago.android.maps.download.task.findcity;


/**
 * Object to store search results from POI Search
 * @author Raphael Volz
 *
 */
public class CityCoordinate  {
	public double lat;
	public double lon;
	public String name;
	public String countryCode;
	
	
	public CityCoordinate() {}

	public CityCoordinate(double lat, double lon, String name, String countryCode) {
		super();
		this.lat = lat;
		this.lon = lon;

		if (null != countryCode && countryCode.length() > 0) {
			this.name = name.trim() + ", " + countryCode.toUpperCase();
		} else {
			this.name = name.trim();
		}
		this.countryCode = countryCode;
	}

	public String toString() {
		return name;
	}

	public double getLat() {
		return lat;
	}

	public double getLon() {
		return lon;
	}

	public boolean equals(Object obj) {
		CityCoordinate poiSearchItem = (CityCoordinate)obj;
		return this.name.compareTo(poiSearchItem.name)==0;
	}

	public int hashCode() {
		return this.name.hashCode();
	}
	
}
