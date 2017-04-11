package com.nogago.android.maps.routing;

import java.util.List;

import net.osmand.osm.LatLon;

import com.nogago.android.maps.activities.ApplicationMode;
import com.nogago.android.maps.routing.RouteProvider.RouteCalculationResult;
import com.nogago.android.maps.routing.RouteProvider.RouteService;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.location.Location;
import android.net.Uri;

public class RouteContentProvider extends ContentProvider {
	
	public static final Uri CONTENT_URI = Uri.parse("content://com.nogago.android.maps.routing.provider");
	
	public static final String LATITUDE = "Latitude";
	public static final String LONGITUDE = "Longitude";
	
	private RouteProvider provider;


	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		return "vnd.android.cursor.dir/vnd.nogago.android.maps.route";
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean onCreate() {
		this.provider = new RouteProvider();
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		MatrixCursor c = new MatrixCursor(new String[]{ LATITUDE, LONGITUDE });
		List<Location> locations = null;
		RouteCalculationResult result;
		if(selectionArgs != null){
			if(selectionArgs.length != 6) return c;
			double startLat, startLon, endLat, endLon;
			boolean fast;
			ApplicationMode mode;
			try{
				startLat = Double.parseDouble(selectionArgs[0]);
				startLon = Double.parseDouble(selectionArgs[1]);
				endLat = Double.parseDouble(selectionArgs[2]);
				endLon = Double.parseDouble(selectionArgs[3]);
				mode = ApplicationMode.fromString(selectionArgs[4]);
				fast = Boolean.parseBoolean(selectionArgs[5]);
			}catch(Exception e){
				e.printStackTrace();
				//Wrong parameters
				return c;
			}
			Location start = new Location("nogago");
			start.setLatitude(startLat);
			start.setLongitude(startLon);
			LatLon end = new LatLon(endLat, endLon);
			result = provider.calculateRouteImpl(start, end, mode, RouteService.OSMAND, getContext(), null, fast);
			locations = (result != null) ? result.getLocations() : null;
		}else{
			locations = RouteProvider.routeList;
		}
		if(locations != null && locations.size()>0){
			for(Location location: locations){
				c.addRow(new Object[]{ location.getLatitude(), location.getLongitude() });
			}
		}
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

}
