package com.nogago.android.maps.activities.search;


import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import net.osmand.Algoritms;
import net.osmand.data.City;
import net.osmand.data.MapObject;
import net.osmand.data.Street;
import net.osmand.osm.LatLon;
import net.osmand.osm.MapUtils;

import org.apache.commons.logging.Log;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Xml;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View.OnKeyListener;

import com.nogago.android.maps.Constants;
import com.nogago.android.maps.LogUtil;
import com.nogago.android.maps.OsmAndFormatter;
import com.nogago.android.maps.R;
import com.nogago.android.maps.Version;
import com.nogago.android.maps.access.AccessibleToast;
import com.nogago.android.maps.activities.MapActivity;
import com.nogago.android.maps.activities.search.SearchActivity.SearchActivityChild;
import com.nogago.android.maps.plus.OsmandApplication;
import com.nogago.android.maps.plus.OsmandSettings;
import com.nogago.android.maps.plus.RegionAddressRepository;

public class SearchAddressActivity extends Activity implements SearchActivityChild {

	public static final String SELECT_ADDRESS_POINT_INTENT_KEY = "SELECT_ADDRESS_POINT_INTENT_KEY";
	public static final int SELECT_ADDRESS_POINT_RESULT_OK = 1;	
	public static final String SELECT_ADDRESS_POINT_LAT = "SELECT_ADDRESS_POINT_LAT";
	public static final String SELECT_ADDRESS_POINT_LON = "SELECT_ADDRESS_POINT_LON";
	
	private EditText cityText;
	private EditText streetText;
	private EditText houseNoText;
	
	private Button searchButton;
	
	private LatLon location;
	private ProgressDialog progressDlg;
	private static PlacesAdapter lastResult = null;
	
	private final static Log log = LogUtil.getLog(SearchAddressActivity.class);
	
	private Dialog placePickerDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.search_address);
		
		cityText = (EditText) findViewById(R.id.SearchCityText);
		streetText = (EditText) findViewById(R.id.SearchStreetText);
		houseNoText = (EditText) findViewById(R.id.SearchHouseNoText);
		
		searchButton = (Button) findViewById(R.id.SearchAddress);
		attachListeners();
		location = OsmandApplication.getSettings().getLastKnownMapLocation();
	}
	
	private void attachListeners() {
		searchButton.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
				inputMethodManager.hideSoftInputFromWindow(cityText.getWindowToken(), 0); // Remove keyboard
				searchPlaces();
			}
		});

		findViewById(R.id.ResetHouseNo).setOnClickListener(
			new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					houseNoText.setText("");
				}
		});
		findViewById(R.id.ResetStreet).setOnClickListener(
			new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					streetText.setText("");
					houseNoText.setText("");
				}
		});
		findViewById(R.id.ResetCity).setOnClickListener(
			new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					cityText.setText("");
					streetText.setText("");
					houseNoText.setText("");
				}
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Intent intent = getIntent();
		if(intent != null){
			double lat = intent.getDoubleExtra(SearchActivity.SEARCH_LAT, 0);
			double lon = intent.getDoubleExtra(SearchActivity.SEARCH_LON, 0);
			if(lat != 0 || lon != 0){
				location = new LatLon(lat, lon);
			}
		}
		if (location == null && getParent() instanceof SearchActivity) {
			location = ((SearchActivity) getParent()).getSearchPoint();
		}
		if (location == null) {
			location = OsmandApplication.getSettings().getLastKnownMapLocation();
		}
	}
	
	@Override
	public void locationUpdate(LatLon l) {
		location = l;
		if(lastResult != null){
			lastResult.notifyDataSetInvalidated();
		}
	}

	private void searchPlaces() {
		final String city = cityText.getText().toString().trim();
		final String street = streetText.getText().toString().trim();
		final String houseNo = houseNoText.getText().toString().trim();
		
		if(Algoritms.isEmpty(city) && Algoritms.isEmpty(street)){
			return;
		}
		
		progressDlg = ProgressDialog.show(this, getString(R.string.searching), getString(R.string.searching_address), true, true);
		new Thread(new Runnable(){
			@Override
			public void run() {
				try {
					List<Place> places = null;
					places = search(city, street);
					if(places.isEmpty()){
						String q = buildNominatimQueryString(city, street, houseNo);
						places = searchByNominatim(q);
					}
					
					if(places.isEmpty()){
						showResult(R.string.search_nothing_found, null);
					} else {
						showResult(0, places);
					}
				} catch(Exception e){
					showResult(R.string.no_internet, null);
				} finally {
					if(progressDlg != null){
						progressDlg.dismiss();
						progressDlg = null;
					}
				}
			}
			
		}, "SearchingAddress").start(); //$NON-NLS-1$
	}
	
	private String buildNominatimQueryString(String city, String street, String houseNo){
		StringBuffer sb = new StringBuffer();
		if(!Algoritms.isEmpty(houseNo)) sb.append(houseNo+",");
		if(!Algoritms.isEmpty(street)) sb.append(street+",");
		if(!Algoritms.isEmpty(city)) sb.append(city);
		String query = sb.toString();
		if(query.endsWith(",")) query = query.substring(0, query.length()-1);
		return query;
	}
	
	private List<Place> search(String city, String street) throws UnsupportedEncodingException{
		
		List<Place> places = new ArrayList<Place>();
		Collection<RegionAddressRepository> addressMaps = ((OsmandApplication)getApplication()).getResourceManager().getAddressRepositories();
		for(RegionAddressRepository addressMap: addressMaps){
			addressMap.preloadCities(null);
			List<MapObject> results = addressMap.getPlaceByName(city, street, null, null);
			for(MapObject o: results){
				Place p = new Place();
				p.lat = o.getLocation().getLatitude();
				p.lon = o.getLocation().getLongitude();
				String name = "";
				if(o instanceof Street){
					name = o.getName() + " ("+((Street)o).getCity().getName()+")";
				}else {
					name = o.getName();
				}
				p.displayName = name;
				places.add(p);
			}
			addressMap.clearCache();
			addressMap = null;
		}
		return places;
	}
	
	private List<Place> searchByNominatim(String q) throws IOException, XmlPullParserException{
		List<Place> places = new ArrayList<Place>();
		StringBuilder b = new StringBuilder();
		b.append(Constants.NOMINATIM_SEARCH_URL); //$NON-NLS-1$
		b.append("?format=xml&addressdetails=0&accept-language=").append(Locale.getDefault().getLanguage()); //$NON-NLS-1$
		b.append("&q=").append(URLEncoder.encode(q, "UTF-8")); //$NON-NLS-1$
		
		log.info("Searching address at : " + b.toString()); //$NON-NLS-1$
		URL url = new URL(b.toString());
		URLConnection conn = url.openConnection();
		conn.setDoInput(true);
		conn.setRequestProperty("User-Agent", Version.getFullVersion(SearchAddressActivity.this)); //$NON-NLS-1$
		conn.connect();
		InputStream is = conn.getInputStream();
		XmlPullParser parser = Xml.newPullParser();
		parser.setInput(is, "UTF-8"); //$NON-NLS-1$
		int ev;
		while ((ev = parser.next()) != XmlPullParser.END_DOCUMENT) {
			if(ev == XmlPullParser.START_TAG){
				if(parser.getName().equals("place")){ //$NON-NLS-1$
					String lat = parser.getAttributeValue("", "lat"); //$NON-NLS-1$ //$NON-NLS-2$
					String lon = parser.getAttributeValue("", "lon");  //$NON-NLS-1$//$NON-NLS-2$
					String displayName = parser.getAttributeValue("", "display_name"); //$NON-NLS-1$ //$NON-NLS-2$
					if(lat != null && lon != null && displayName != null){
						Place p = new Place();
						p.lat = Double.parseDouble(lat);
						p.lon = Double.parseDouble(lon);
						p.displayName = displayName;
						places.add(p);
					}
				}
			}

		}
		is.close();
		return places;
	}
	
	private void showResult(final int warning, final List<Place> places) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if(places == null){
					AccessibleToast.makeText(SearchAddressActivity.this, getString(warning), Toast.LENGTH_LONG).show();
				} else {
					lastResult = new PlacesAdapter(places);
					if (placePickerDialog != null) {
						placePickerDialog.dismiss();
						placePickerDialog = null;
					}
					placePickerDialog = new Dialog(SearchAddressActivity.this);
					AlertDialog.Builder builder = new AlertDialog.Builder(SearchAddressActivity.this);
					ListView lv = new ListView(SearchAddressActivity.this);
					lv.setAdapter(lastResult);
					lv.setOnItemClickListener(new DialogItemClicker(SearchAddressActivity.this));
					builder.setView(lv);
					placePickerDialog = builder.create();
					placePickerDialog.show();
				}
			}
		});
	}
	
	private class DialogItemClicker implements OnItemClickListener {
		Context ctx;
		DialogItemClicker(Context ctx) {
			this.ctx = ctx;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * android.widget.AdapterView.OnItemClickListener#onItemClick(android
		 * .widget .AdapterView, android.view.View, int, long)
		 */
		@Override
		public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
			//if(placePickerDialog.isShowing()) placePickerDialog.dismiss();
			Place item = (Place)adapterView.getItemAtPosition(position);
			OsmandSettings settings = OsmandApplication.getSettings();
			settings.setMapLocationToShow(item.lat, item.lon,
					Math.max(15, settings.getLastKnownMapZoom()), getString(R.string.address)+ " : " + item.displayName); //$NON-NLS-1$
			MapActivity.launchMapActivityMoveToTop(SearchAddressActivity.this);
			//if (placePickerDialog != null) placePickerDialog.hide();
		}
	}
	

	@Override
	protected void onStop() {
		if(progressDlg != null){
			progressDlg.dismiss();
			progressDlg = null;
		}
		super.onStop();
	}
	
	@Override
	protected void onPause() {
		if(progressDlg != null){
			progressDlg.dismiss();
			progressDlg = null;
		}
		super.onPause();
	}
	
	private static class Place {
		public double lat;
		public double lon;
		public String displayName;
	}
	
	class PlacesAdapter extends ArrayAdapter<Place> {

		public PlacesAdapter(List<Place> places) {
			super(SearchAddressActivity.this, R.layout.search_address_online_list_item, places);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View row = convertView;
			if (row == null) {
				LayoutInflater inflater = getLayoutInflater();
				row = inflater.inflate(R.layout.search_address_online_list_item, parent, false);
			}
			Place model = getItem(position);
			TextView label = (TextView) row.findViewById(R.id.label);
			TextView distanceLabel = (TextView) row.findViewById(R.id.distance_label);
			if(location != null){
				int dist = (int) (MapUtils.getDistance(location, model.lat, model.lon));
				distanceLabel.setText(OsmAndFormatter.getFormattedDistance(dist, SearchAddressActivity.this));
			} else {
				distanceLabel.setText(""); //$NON-NLS-1$
			}
			label.setText(model.displayName);
			return row;
		}
		
	}
}
