package com.nogago.android.maps;

import android.content.ComponentName;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Constants {

	  /**
	   * URL to upload tracks to nogago
	   */
	  public final static String TRACKS_UPLOAD_URL = "https://www.nogago.com:443/tracks/uploadTrack";
	  public final static String TRACKS_PACKAGE = "com.nogago.bb10.tracks";
	  public final static String BB_TRACKS_PACKAGE = "com.nogago.bb10.tracks";

	  /**
	   * URL to dowload nogagoMaps from AppWorld
	   */
	  public final static String BB_MAPS_DOWNLOAD_URL = "appworld://content/128568/"; // appworld://vendor/2395
	  /**
	  * URL to dowload nogagoMaps from PlayStore
	  */
	 public final static String PLAY_MAPS_DOWNLOAD_URL = "market://details?id=com.nogago.bb10.maps";
	  
	 /**
	  * URL to dowload and review nogago Tracks from AppWorld
	  */
	 public final static String BB_TRACKS_DOWNLOAD_URL = "appworld://content/28277456/"; // appworld://vendor/2395
	 /**
	 * URL to dowload and review nogago Tracks from PlayStore
	 */
	// public final static String PLAY_TRACKS_DOWNLOAD_URL = "market://details?id=pname:com.nogago.android.tracks";

	public final static String PLAY_TRACKS_DOWNLOAD_URL = "market://details?id=com.nogago.bb10.tracks";
	 
	 /**
	   * URL to download nogago Apps in BB
	   */
	  public final static String BB_VENDOR_URL = "appworld://vendor/2395"; 
	  /**
	   * URL to download nogago Apps in Play
	   */
	  public final static String PLAY_VENDOR_URL = "market://search?q=pub:nogago+GmbH"; 
	  /**
	   * URL to download nogago Apps in Web
	   */
	  public final static String WWW_VENDOR_URL = "http://www.nogago.com/";

	  public final static String SUPPORT_MAIL = "support@nogago.com";
	  
	  public final static boolean IS_BLACKBERRY = java.lang.System.getProperty("os.name").equals("qnx");

	  public final static ComponentName MAPS_COMPONENT = new ComponentName("com.nogago.android.maps",
	      "com.nogago.android.maps.activities.MapActivity");
	  
	  public final static String MAPS_PACKAGE = "com.nogago.android.maps"; 
	  
	  public final static String MAPS_ACTIVITY = ".activities.MapActivity";
	  
	public static final String NOMINATIM_SEARCH_URL = "http://www.nogago.com/nominatim/search";
	public static final String ROUTING_SERVICE_URL = "http://route.nogago.com/gosmore.php";
	public static final String POI_SEARCH_URL = "http://www.nogago.com/poi/search";
	public static final float POI_SEARCH_DEVIATION = 0.0001f;
	public static final float POI_SEARCH_THRESHOLD = 0.1f;
	public static final double POI_PERIMETER_LIMIT = 10000.0;
	
	public static final String NOGAGO_MAP_MANAGER_COMPONENT = "com.nogago.android.download";
	public static final String NOGAGO_MAP_DOWNLOADER_ACTIVITY = "com.nogago.android.download.ui.SplashActivity";
	// public static final String TRACKS_DOWNLOAD_URL = "market://search?q=com.nogago.bb10.tracks";
	
	public static final String STRING_LONGITUDE = "longitude";
	public static final String STRING_LATITUDE = "latitude";
	
	public static final String POITYPES_ASSET = "poi-type.xml";
	
	public static final String BASEMAP_NAME_DIR = "basemap";
	public static final String BASEMAP_NAME_PREFIX = "World_Basemap";
	public static final String BASEMAP_NAME_EXT = ".obf";
	public static final String BASEMAP_NAME = BASEMAP_NAME_PREFIX + BASEMAP_NAME_EXT;
	
	public static final String APP_PACKAGE_NAME = "com.nogago.android.maps";
	
	public static final int MAX_LOADED_WAYPOINTS_POINTS = 10000;
	
	public static final String MYTRACKS_SERVICE_PACKAGE = "com.nogago.android.tracks";
	public static final String MYTRACKS_SERVICE_CLASS = "com.nogago.android.tracks.services.TrackRecordingService";
	public static final long BAD_MYTRACKS_TRACK_ID = -1L;
	
	//nogago Map Downloader
	public final static String STORAGE_PATH = "/nogago/";
	public final static String TEMP_PATH = STORAGE_PATH + "temp/";
	public final static String POI_PATH = STORAGE_PATH + "POI/";
	public final static String NOGAGO_STORAGE_URL = "https://download.nogago.com/latest/";
	public final static String NOGAGO_CONTOURS_STORAGE_URL = "https://download.nogago.com/contours/";
	public final static String NOGAGO_MAP_URL = NOGAGO_STORAGE_URL + "maps/";
	public final static String NOGAGO_POI_URL = NOGAGO_STORAGE_URL + "pois/";
	public final static String MAP_FILE_EXTENSION = ".obf";
	public final static String POLY_FILE_EXTENSION = ".poly";
	public final static String POI_FILE_EXTENSION = ".poi.odb";
	public final static String STRING_LONGITUDE_POSTFIX = "longitude";
	public final static String STRING_LATITUDE_POSTFIX = "latitude";
	public final static int HTTP_CODE_401 = 401;
	public final static String URL_ENCODING = "UTF-8";
	public final static int BAD_OBF_FILE_THRESHOLD = 10000;
	
	public final static String NOGAGO_REGISTER_URL = "https://www.nogago.com/userRegister/index";
	public static final String BUGS_MAIL = "bugs@nogago.com";
	public static boolean isOnline(Context context) {
	    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    if (cm == null) return false;
	    NetworkInfo ni = cm.getActiveNetworkInfo();
	    if (ni == null) return false;
	    return ni.isConnectedOrConnecting();
	}
	
}
