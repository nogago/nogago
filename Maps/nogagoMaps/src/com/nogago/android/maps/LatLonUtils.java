package com.nogago.android.maps;

import android.graphics.RectF;
import android.location.Location;

public class LatLonUtils {

	public static Location middleLocation(Location start, Location end,
			float meters) {
		double lat1 = toRad(start.getLatitude());
		double lon1 = toRad(start.getLongitude());
		double R = 6371; // radius of earth in km
		double d = meters / 1000; // in km
		float brng = (float) (toRad(start.bearingTo(end)));
		double lat2 = Math.asin(Math.sin(lat1) * Math.cos(d / R)
				+ Math.cos(lat1) * Math.sin(d / R) * Math.cos(brng));
		double lon2 = lon1
				+ Math.atan2(Math.sin(brng) * Math.sin(d / R) * Math.cos(lat1),
						Math.cos(d / R) - Math.sin(lat1) * Math.sin(lat2));
		Location nl = new Location(start);
		nl.setLatitude(toDegree(lat2));
		nl.setLongitude(toDegree(lon2));
		nl.setBearing(brng);
		return nl;
	}

	private static double toDegree(double radians) {
		return radians * 180 / Math.PI;
	}

	private static double toRad(double degree) {
		return degree * Math.PI / 180;
	}
	
	public static RectF getSearchBox(RectF f){
		RectF newF = new RectF(f.left, f.top, f.right, f.bottom);
		if(f.top-f.bottom > Constants.POI_SEARCH_THRESHOLD){
			float yCenter = (f.top + f.bottom)/2;
			newF.top = (yCenter + (Constants.POI_SEARCH_THRESHOLD/2));
			newF.bottom = newF.top - Constants.POI_SEARCH_THRESHOLD + Constants.POI_SEARCH_DEVIATION;
		}
		
		if(f.right-f.left > Constants.POI_SEARCH_THRESHOLD){
			float xCenter = (f.right + f.left)/2;
			newF.right = (xCenter + (Constants.POI_SEARCH_THRESHOLD/2));
			newF.left = newF.right - Constants.POI_SEARCH_THRESHOLD + Constants.POI_SEARCH_DEVIATION;
		}
		return newF;
	}

}
