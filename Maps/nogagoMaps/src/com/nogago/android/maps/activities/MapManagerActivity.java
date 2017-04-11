package com.nogago.android.maps.activities;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import net.osmand.data.Amenity;

import org.apache.commons.io.FileUtils;

import com.nogago.android.maps.Constants;
import com.nogago.android.maps.LogUtil;
import com.nogago.android.maps.NogagoUtils;
import com.nogago.android.maps.R;
import com.nogago.android.maps.UIUtils;
import com.nogago.android.maps.download.MapFile;
import com.nogago.android.maps.download.MapFileListAdapter;
import com.nogago.android.maps.download.task.AsyncTaskManager;
import com.nogago.android.maps.download.task.OnTaskCompleteListener;
import com.nogago.android.maps.download.task.TrackableTask;
import com.nogago.android.maps.download.task.TrackableTaskException;
import com.nogago.android.maps.download.task.download.DeleteMapTask;
import com.nogago.android.maps.download.task.download.DownloadTaskException;
import com.nogago.android.maps.download.task.download.MultiAreaDownloadTask;
import com.nogago.android.maps.download.task.findcity.CityCoordinate;
import com.nogago.android.maps.download.task.findcity.FindCityListAdapter;
import com.nogago.android.maps.download.task.findcity.FindCityTask;
import com.nogago.android.maps.download.task.readareas.Area;
import com.nogago.android.maps.download.task.readareas.ReadAreasTask;
import com.nogago.android.maps.download.task.readareas.ReadAreasTaskException;
import com.nogago.android.maps.download.task.update.UpdateMapException;
import com.nogago.android.maps.download.task.update.UpdateMapTask;
import com.nogago.android.maps.plus.AmenityIndexRepositoryOdb;
import com.nogago.android.maps.plus.OsmandApplication;
import com.nogago.android.maps.plus.ResourceManager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class MapManagerActivity extends ListActivity implements
		OnTaskCompleteListener, OnItemClickListener, OnClickListener {

	private SharedPreferences preferences;

	private AsyncTaskManager mAsyncTaskManager;

	/**
	 * the last search entries
	 */
	// private SearchHistory searchHistory;

	/**
	 * the list adapters
	 */
	private FindCityListAdapter findCityListAdapter;
	private MapFileListAdapter mapFileListAdapter;

	/**
	 * the query view
	 */
	private EditText queryView;
	private Button findButton;
	//private Button prefButton;
	private Dialog cityPickerDialog;
	
	private static final int CONTEXT_MENUE_VIEW_MAP_ID = 1;
	private static final int CONTEXT_MENUE_UPDATE_MAP_ID =2;
	private static final int CONTEXT_MENUE_DELETE_MAP_ID = 3;
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		com.nogago.android.maps.utils.AnalyticsUtils.sendPageViews(this, "map_manager/create" );
		/*
		FileUtils.deleteQuietly(new File(Environment.getExternalStorageDirectory().
				toString()
				+ Constants.TEMP_PATH));
		createDirectory(Environment.getExternalStorageDirectory()
				.toString() + Constants.STORAGE_PATH);
		createDirectory(Environment.getExternalStorageDirectory()
				.toString() + Constants.TEMP_PATH);
		createDirectory(Environment.getExternalStorageDirectory()
				.toString() + Constants.POI_PATH);
		*/
		FileUtils.deleteQuietly(new File(OsmandApplication.getSettings().extendOsmandPath(Constants.TEMP_PATH).toString()));
		createDirectory(OsmandApplication.getSettings().extendOsmandPath(ResourceManager.APP_DIR).toString());
		createDirectory(OsmandApplication.getSettings().extendOsmandPath(Constants.TEMP_PATH).toString());
		createDirectory(OsmandApplication.getSettings().extendOsmandPath(ResourceManager.POI_PATH).toString());
		
		this.preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

		// Create manager and set this activity as context and listener
		mAsyncTaskManager = new AsyncTaskManager(this, this, getResources().getString(R.string.task_download, "Test"), true, false);
		mAsyncTaskManager.handleRetainedTask(getLastNonConfigurationInstance());

		// Setup UI and bind variables
		setContentView(R.layout.map_manager);
		setMapFileList();

		this.queryView = (EditText) findViewById(R.id.query_entry);

		this.findButton = (Button) findViewById(R.id.findButton);
		findButton.setOnClickListener(this);
		//this.prefButton = (Button) findViewById(R.id.prefButton);
		//prefButton.setOnClickListener(this);
		queryView.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if(keyCode==KeyEvent.KEYCODE_ENTER) {
					doQuery();
				} 
				return false;
			}
        });
		getListView().setOnItemClickListener(this);
		registerForContextMenu(getListView());
		Bundle bundle = getIntent().getExtras();

		// Execute Read Areas Task
		this.runOnUiThread(new Runnable(){
			@Override
			public void run() {
				ReadAreasTask task = new ReadAreasTask(getString(R.string.task_read_areas), MapManagerActivity.this);
				task.loadAreaFile();
				//mAsyncTaskManager.setupTask(task);
				
			}
		});
		
		
		if(bundle != null){
			double lon = bundle.getDouble(Constants.STRING_LONGITUDE);
			double lat = bundle.getDouble(Constants.STRING_LATITUDE);
			
			if(lon>=-180&&lon<=180 && lat>=-90&&lat<=90){
				FindCityTask task = new FindCityTask(getString(R.string.task_findcity, ""), lon, lat);
				mAsyncTaskManager.setupTask(task);
			}
		}
	}
	private void setMapFileList() {
		// readMapsFromDisk();
		mapFileListAdapter = new MapFileListAdapter(this);
		setListAdapter(mapFileListAdapter);
	}

	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		menu.setHeaderTitle(R.string.home_contextmenu_title);
		menu.add(Menu.NONE, CONTEXT_MENUE_VIEW_MAP_ID, CONTEXT_MENUE_VIEW_MAP_ID, R.string.home_contextmenu_view_map);
		menu.add(Menu.NONE, CONTEXT_MENUE_UPDATE_MAP_ID, CONTEXT_MENUE_UPDATE_MAP_ID, R.string.home_contextmenu_update_map);
		menu.add(Menu.NONE, CONTEXT_MENUE_DELETE_MAP_ID, CONTEXT_MENUE_DELETE_MAP_ID, R.string.home_contextmenu_delete_map);
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	public boolean onContextItemSelected(MenuItem item) {
		//Context menu index
		int menuIndex = item.getItemId();
		
		//List item index
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		final MapFile mapFile = mapFileListAdapter.getItem(info.position);
		
		if(menuIndex == CONTEXT_MENUE_VIEW_MAP_ID){
			Area area = ReadAreasTask.getAreaById(mapFile.getMapId());
			if(area != null){
				double lon = Double.parseDouble(this.preferences.getString(area.getMapId() + Constants.STRING_LONGITUDE_POSTFIX, ((Area.toDegrees(area.getMaxLong()) + Area.toDegrees(area.getMinLong()))/2)+""));
				double lat = Double.parseDouble(this.preferences.getString(area.getMapId() + Constants.STRING_LATITUDE_POSTFIX, ((Area.toDegrees(area.getMaxLat()) + Area.toDegrees(area.getMinLat()))/2)+""));
				String uri = "geo:"+ lat + "," + lon ; // + "?z=15";

				Intent viewIntent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri));
				viewIntent.setClass(MapManagerActivity.this, com.nogago.android.maps.activities.search.GeoIntentActivity.class);
				startActivity(viewIntent);
			}
		}else if(menuIndex == CONTEXT_MENUE_UPDATE_MAP_ID){
			if (isOnline()) {
				UpdateMapTask task = new UpdateMapTask(getString(R.string.task_update_map, mapFileListAdapter.getItemDisplayName(mapFile)), getUsername(), getPassword(), mapFile);
				mAsyncTaskManager.setupTask(task);
			} else {
				Toast.makeText(this, R.string.error_network, Toast.LENGTH_LONG).show();
			}

		}else if(menuIndex == CONTEXT_MENUE_DELETE_MAP_ID){
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
		    builder.setTitle(R.string.delete_map_dialog_title);
		    builder.setIcon(android.R.drawable.ic_dialog_info);
			builder.setPositiveButton(R.string.button_ok,
				new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialog, int which){
						DeleteMapTask task = new DeleteMapTask(getString(R.string.task_delete_map, mapFile.getName()), mapFile);
						mAsyncTaskManager.setupTask(task);
						removeCityCoordinateForMap(mapFile.getMapId());
					}
				}
			); 
			builder.setNegativeButton(R.string.button_cancel, null); 
			builder.show();
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget
	 * .AdapterView, android.view.View, int, long)
	 */
	public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
		// hide the keyboard
		hideKeyBoard();
		
		view.showContextMenu();
	}
	
	public void hideKeyBoard(){
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(this.queryView.getWindowToken(), 0);
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		// Delegate task retain to manager
		return mAsyncTaskManager.retainTask();
	}

	@Override
	public void onTaskComplete(final AsyncTask task) {

		if(task == null){
			
		} else if (task.isCancelled()) {
	    	  if (task instanceof TrackableTask) {
	    		  ((TrackableTask)task).cleanup();
	    	  }		
			// Report about cancel
	    	 Toast.makeText(this, R.string.task_cancelled, Toast.LENGTH_LONG).show();
		} else {
			Object result = new Object();
			try {
				result = (Object) task.get();
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (result instanceof TrackableTaskException) {
				if(task instanceof TrackableTask) ((TrackableTask)task).cleanup();
				// TODO Handle Exceptions (instanceof TrackableTaskException)
				TrackableTaskException e = (TrackableTaskException) result;
				if (e.getId() == DownloadTaskException.NOT_ENOUGH_SPACE) {
					Toast.makeText(this, R.string.error_no_space, Toast.LENGTH_LONG).show();
				} else if (e.getId() == UpdateMapException.NOT_ENOUGH_SPACE) {
					Toast.makeText(this, R.string.error_update_map_no_space, Toast.LENGTH_LONG).show();
				} else if (e.getId() == DownloadTaskException.FAILED) {
					Toast.makeText(this, R.string.error_download_failed, Toast.LENGTH_LONG).show();
				} else if (e.getId() == DownloadTaskException.CREDENTIALS_WRONG) {
					android.content.DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Intent settings = new Intent(MapManagerActivity.this, SettingsActivity.class);
							startActivity(settings);
						}
					};
					UIUtils.showAlertDialog(this, getString(R.string.wrong_credential), getString(R.string.task_invalid_credentials), listener);
				} else {
					Toast.makeText(this, "Task had an exception " + e.toString(), Toast.LENGTH_LONG).show();
				}
			} else if (result instanceof List<?>) {
				// FindCityTask returns List of <POISearchItem>
				if(((List) result).size()<1){
					Toast.makeText(this, R.string.no_city_found, Toast.LENGTH_SHORT).show();
				} else{
					// * DISPLAY RESULTS IN DIALOG */
					this.findCityListAdapter = new FindCityListAdapter(this, (List<CityCoordinate>) result);
					cityPickerDialog = new Dialog(this);
					AlertDialog.Builder builder = new AlertDialog.Builder(this);
					ListView lv = new ListView(this);
					lv.setAdapter(findCityListAdapter);
					lv.setOnItemClickListener(new DialogItemClicker(this));
					builder.setView(lv);
					cityPickerDialog = builder.create();
					cityPickerDialog.show();
				}
			} else if (result instanceof Boolean) {
				// returned false only if task was cancelled
				// UIUtils.showAlertDialog(HomeActivity.this, getString(R.string.task_title_success), getString(R.string.task_message_success), null);
				if(task instanceof MultiAreaDownloadTask){
					NogagoUtils.reloadIndexes(MapManagerActivity.this);
				}
			}
		}
		//Refresh ListView
		this.getListView().invalidateViews();
		this.getListView().postInvalidate();
	}

	/** Click on the Find/Download Button */
	@Override
	public void onClick(View arg0) {
		doQuery();
	}
	
	private void doQuery(){
		String s = queryView.getText().toString().trim();
		if(s.length()<1) return;
		hideKeyBoard();
		if (getUsername() != null && getUsername().compareTo("username") != 0) {
			if (isOnline()) {
				FindCityTask task = new FindCityTask(getString(R.string.task_findcity, s), s);
				mAsyncTaskManager.setupTask(task);
			} else {
				Toast.makeText(this, R.string.error_network, Toast.LENGTH_LONG).show();
			}
		} else {
			android.content.DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent settings = new Intent(MapManagerActivity.this, SettingsActivity.class);
					startActivity(settings);
				}
			};
			UIUtils.showAlertDialog(this, getString(R.string.wrong_credential), getString(R.string.error_username), listener);
		}
	}

	public boolean isOnline() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm == null) return false;
		NetworkInfo ni = cm.getActiveNetworkInfo();
		if (ni == null) return false;
		return ni.isConnectedOrConnecting();
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
		public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
			if(cityPickerDialog.isShowing()) {
				cityPickerDialog.dismiss();
			}
			// hide the keyboard
			MapManagerActivity.this.hideKeyBoard();
			final CityCoordinate poiSearchItem = (CityCoordinate) adapterView.getItemAtPosition(position);
			// neuer AlertDialog: Höhenlinien auch laden? Wenn nein, weiter wie gehabt, wenn ja, auch die zugehörige Höhenlinien-Datei laden
			
			AlertDialog.Builder builder = new AlertDialog.Builder(MapManagerActivity.this);
		    builder.setMessage(R.string.load_contours_also_title);
		    builder.setCancelable(false);
		    builder.setIcon(android.R.drawable.ic_dialog_info);
			builder.setPositiveButton(R.string.button_yes,
				new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialog, int which){
						
						//beide Dateien laden
						boolean loadCountours = true;
						String progMess = getString(R.string.task_download_contour);

						double minLat = poiSearchItem.getLat()
								- ReadAreasTask.getMinLatDelta();
						double minLon = poiSearchItem.getLon()
								- ReadAreasTask.getMinLonDelta();
						double maxLat = poiSearchItem.getLat()
								+ ReadAreasTask.getMinLatDelta();
						double maxLon = poiSearchItem.getLon()
								+ ReadAreasTask.getMinLonDelta();

						List<Area> allareas;
						HashSet<Area> result = new HashSet<Area>();
						try {
							allareas = ReadAreasTask.getAreas();
							// First pass (minLat,minLon)
							for (Area area : allareas) {
								if (area.contains(minLat, minLon)) {
									result.add(area);
									break;
								}
							}
							// (minLat,maxLon)
							for (Area area : allareas) {
								if (area.contains(minLat, maxLon)) {
									result.add(area);
									break;
								}
							}
							// (maxLat,minLon)
							for (Area area : allareas) {
								if (area.contains(maxLat, minLon)) {
									result.add(area);
									break;
								}
							}
							// (maxLat,maxLon)
							for (Area area : allareas) {
								if (area.contains(maxLat, maxLon)) {
									result.add(area);
									break;
								}
							}
							// DEBUG
							System.out.println("Areas: " + result.size());
						} catch (ReadAreasTaskException e) {
							// Not properly initialized
							result = null;
						}
						//android.util.Log.d(TAG, name + " " + message); 
						if (result == null || result.size() == 0) {
							Toast.makeText(ctx,
									"Did not find an area for Lat: "
											+ poiSearchItem.getLat() + " Lon: "
											+ poiSearchItem.getLon(), Toast.LENGTH_SHORT).show();
						} else {
							List<Area> existingAreas = new ArrayList<Area>();
							List<MapFile> exisitingMapFiles = new ArrayList<MapFile>();
							for(Area area: result){
								for(MapFile mapFile: mapFileListAdapter.getMapFiles()){
									if(area.getMapId() == mapFile.getMapId()){
										existingAreas.add(area);
										exisitingMapFiles.add(mapFile);
										break;
									}
								}
							}
							result.removeAll(existingAreas);
							Area[] areas = result.toArray(new Area[result.size()]);
							
							if(areas.length == 0){
								UIUtils.showAlertDialog(MapManagerActivity.this, getString(R.string.task_title_download_map_exists_area), getString(R.string.task_message_download_map_exists_area, getMapsDisplayName(exisitingMapFiles)), null);
							}else {
								if(existingAreas.size()>0){
									Toast.makeText(MapManagerActivity.this, getString(R.string.task_message_download_parts_exist_area, getMapsDisplayName(exisitingMapFiles)), Toast.LENGTH_LONG).show();
								}
								saveCityCoordinateForMap(areas, poiSearchItem);
								Area baseArea = mapFileListAdapter.getCityCenterArea(Area.formatMapFileName(poiSearchItem.name));
								MultiAreaDownloadTask task = new MultiAreaDownloadTask(getString(R.string.task_download_contour, poiSearchItem.name),
										getUsername(), getPassword(), poiSearchItem.name, areas, baseArea, loadCountours);
								mAsyncTaskManager.setupTask(task);
							}
						}
						if (cityPickerDialog != null) cityPickerDialog.hide();
					}
				}
			); 
			builder.setNegativeButton(R.string.button_no, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
		            	//nur city laden
					boolean loadCountours = false;
					String progMess = getString(R.string.task_download);

					double minLat = poiSearchItem.getLat()
							- ReadAreasTask.getMinLatDelta();
					double minLon = poiSearchItem.getLon()
							- ReadAreasTask.getMinLonDelta();
					double maxLat = poiSearchItem.getLat()
							+ ReadAreasTask.getMinLatDelta();
					double maxLon = poiSearchItem.getLon()
							+ ReadAreasTask.getMinLonDelta();

					List<Area> allareas;
					HashSet<Area> result = new HashSet<Area>();
					try {
						allareas = ReadAreasTask.getAreas();
						// First pass (minLat,minLon)
						for (Area area : allareas) {
							if (area.contains(minLat, minLon)) {
								result.add(area);
								break;
							}
						}
						// (minLat,maxLon)
						for (Area area : allareas) {
							if (area.contains(minLat, maxLon)) {
								result.add(area);
								break;
							}
						}
						// (maxLat,minLon)
						for (Area area : allareas) {
							if (area.contains(maxLat, minLon)) {
								result.add(area);
								break;
							}
						}
						// (maxLat,maxLon)
						for (Area area : allareas) {
							if (area.contains(maxLat, maxLon)) {
								result.add(area);
								break;
							}
						}
						// DEBUG
						System.out.println("Areas: " + result.size());
					} catch (ReadAreasTaskException e) {
						// Not properly initialized
						result = null;
					}
					//android.util.Log.d(TAG, name + " " + message); 
					if (result == null || result.size() == 0) {
						Toast.makeText(ctx,
								"Did not find an area for Lat: "
										+ poiSearchItem.getLat() + " Lon: "
										+ poiSearchItem.getLon(), Toast.LENGTH_SHORT).show();
					} else {
						List<Area> existingAreas = new ArrayList<Area>();
						List<MapFile> exisitingMapFiles = new ArrayList<MapFile>();
						for(Area area: result){
							for(MapFile mapFile: mapFileListAdapter.getMapFiles()){
								if(area.getMapId() == mapFile.getMapId()){
									existingAreas.add(area);
									exisitingMapFiles.add(mapFile);
									break;
								}
							}
						}
						result.removeAll(existingAreas);
						Area[] areas = result.toArray(new Area[result.size()]);
						
						if(areas.length == 0){
							UIUtils.showAlertDialog(MapManagerActivity.this, getString(R.string.task_title_download_map_exists_area), getString(R.string.task_message_download_map_exists_area, getMapsDisplayName(exisitingMapFiles)), null);
						}else {
							if(existingAreas.size()>0){
								Toast.makeText(MapManagerActivity.this, getString(R.string.task_message_download_parts_exist_area, getMapsDisplayName(exisitingMapFiles)), Toast.LENGTH_LONG).show();
							}
							saveCityCoordinateForMap(areas, poiSearchItem);
							Area baseArea = mapFileListAdapter.getCityCenterArea(Area.formatMapFileName(poiSearchItem.name));
							MultiAreaDownloadTask task = new MultiAreaDownloadTask(getString(R.string.task_download, poiSearchItem.name),
									getUsername(), getPassword(), poiSearchItem.name, areas, baseArea, loadCountours);
							mAsyncTaskManager.setupTask(task);
						}
					}
					if (cityPickerDialog != null) cityPickerDialog.hide();
		          }
			}); 
			builder.show();
		}
	}
	
	private String getMapsDisplayName(List<MapFile> maps){
		StringBuffer sb = new StringBuffer();
		sb.append("(");
		for(int i=0; i<maps.size();i++){
			sb.append(mapFileListAdapter.getItemDisplayName(maps.get(i)));
			if(i<maps.size()-1) sb.append(",");
		}
		sb.append(")");
		return sb.toString();
	}

	/**
	 * Returns the username
	 * 
	 * @return the username
	 */
	public String getUsername() {
		return OsmandApplication.getSettings().USER_NAME.get();
	}

	/**
	 * Returns the password
	 * 
	 * @return the password
	 */
	public String getPassword() {
		return OsmandApplication.getSettings().USER_PASSWORD.get();
	}
	
	public void saveCityCoordinateForMap(Area[] areas, CityCoordinate cityCoordinate){
		Editor editor = this.preferences.edit();
		for(Area area: areas){ 
			editor.putString(area.getMapId() + Constants.STRING_LONGITUDE_POSTFIX, new Double(cityCoordinate.getLon()).toString());
			editor.putString(area.getMapId() + Constants.STRING_LATITUDE_POSTFIX, new Double(cityCoordinate.getLat()).toString());
		}
		editor.commit();
	}
	
	public void removeCityCoordinateForMap(int mapId){
		Editor editor = this.preferences.edit();
		editor.remove(mapId + Constants.STRING_LONGITUDE_POSTFIX);
		editor.remove(mapId + Constants.STRING_LATITUDE_POSTFIX);
		editor.commit();
	}
	
	private synchronized void createDirectory(String path) {
		try {
			File file = new File(path);
			if (!file.exists()) {
				if (!file.mkdir()) {
					throw new IOException("unable to create directory : " + path);
				}
			}
		}catch(IOException e){
			e.printStackTrace();
		}
	}
}