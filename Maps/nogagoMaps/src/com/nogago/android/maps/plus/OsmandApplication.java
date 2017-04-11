package com.nogago.android.maps.plus;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.io.IOUtils;

import net.osmand.Algoritms;
import net.osmand.IProgress;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Handler;
import android.text.format.DateFormat;
import android.widget.Toast;

import com.bidforfix.andorid.BidForFixHelper;
import com.nogago.android.maps.Constants;
import com.nogago.android.maps.FavouritePoint;
import com.nogago.android.maps.GPXUtilities;
import com.nogago.android.maps.LogUtil;
import com.nogago.android.maps.R;
import com.nogago.android.maps.GPXUtilities.GPXFile;
import com.nogago.android.maps.GPXUtilities.WptPt;
import com.nogago.android.maps.access.AccessibleToast;
import com.nogago.android.maps.activities.DayNightHelper;
import com.nogago.android.maps.activities.SavingTrackHelper;
import com.nogago.android.maps.activities.SettingsActivity;
import com.nogago.android.maps.plus.FavouritesDbHelper;
import com.nogago.android.maps.plus.NavigationService;
import com.nogago.android.maps.plus.OsmandSettings;
import com.nogago.android.maps.plus.PoiFiltersHelper;
import com.nogago.android.maps.plus.ProgressDialogImplementation;
import com.nogago.android.maps.render.RendererRegistry;
import com.nogago.android.maps.routing.RoutingHelper;
import com.nogago.android.maps.voice.CommandPlayer;
import com.nogago.android.maps.voice.CommandPlayerException;
import com.nogago.android.maps.voice.CommandPlayerFactory;

public class OsmandApplication extends Application {
	public static final String EXCEPTION_PATH = ResourceManager.APP_DIR + "exception.log"; //$NON-NLS-1$
	private static final org.apache.commons.logging.Log LOG = LogUtil.getLog(OsmandApplication.class);

	ResourceManager manager = null;
	PoiFiltersHelper poiFilters = null;
	RoutingHelper routingHelper = null;
	FavouritesDbHelper favorites = null;
	CommandPlayer player = null;
	
	/**
	 * Static reference to instance of settings class.
	 * Transferred from OsmandSettings class to allow redefine actual instance behind it
	 */
	static OsmandSettings osmandSettings = null;
	
	DayNightHelper daynightHelper;
	NavigationService navigationService;
	RendererRegistry rendererRegistry;
	BidForFixHelper bidforfix;
	
	// start variables
	private ProgressDialogImplementation startDialog;
	private List<String> startingWarnings;
	private Handler uiHandler;
	private GPXFile gpxFileToDisplay;
	private GPXFile recordingTrackToDisplay;

	private boolean applicationInitializing = false;
	private Locale prefferedLocale = null;

	@Override
	public void onCreate() {
		super.onCreate();
		com.nogago.android.maps.utils.AnalyticsUtils.sendPageViews(getApplicationContext(), "/appstart");
	    com.nogago.android.maps.utils.EulaUtils.increaseAppStart(this);
		long timeToStart = System.currentTimeMillis();
		osmandSettings = createOsmandSettingsInstance();
		routingHelper = new RoutingHelper(osmandSettings, this, player);
		manager = new ResourceManager(this);
		daynightHelper = new DayNightHelper(this);
		bidforfix = new BidForFixHelper("nogago.com", getString(R.string.default_buttons_support), getString(R.string.default_buttons_cancel));
		uiHandler = new Handler();
		rendererRegistry = new RendererRegistry();
		checkPrefferedLocale();
		startApplication();
		if (LOG.isDebugEnabled()) {
			LOG.debug("Time to start application " + (System.currentTimeMillis() - timeToStart) + " ms. Should be less < 800 ms");
		}
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		if (routingHelper != null) {
			routingHelper.getVoiceRouter().onApplicationTerminate(getApplicationContext());
		}
		if (bidforfix != null) {
    		bidforfix.onDestroy();
    	}
	}

	public RendererRegistry getRendererRegistry() {
		return rendererRegistry;
	}
	
	/**
	 * Creates instance of OsmandSettings
	 * @return Reference to instance of OsmandSettings
	 */
	protected OsmandSettings createOsmandSettingsInstance() {
		return new OsmandSettings(this);
	}

	/**
	 * Application settings
	 * @return Reference to instance of OsmandSettings
	 */
	public static OsmandSettings getSettings() {
		if(osmandSettings == null) {
			LOG.error("Trying to access settings before they were created");
		}
		return osmandSettings;
	}
	

	public PoiFiltersHelper getPoiFilters() {
		if (poiFilters == null) {
			poiFilters = new PoiFiltersHelper(this);
		}
		return poiFilters;
	}
	
	public void setGpxFileToDisplay(GPXFile gpxFileToDisplay, boolean showCurrentGpxFile) {
		this.gpxFileToDisplay = gpxFileToDisplay;
		displayGPXFile(gpxFileToDisplay, showCurrentGpxFile);
	}
	
	public void setRecordingTrackToDisplay(GPXFile gpxFileToDisplay, boolean showCurrentGpxFile) {
		this.recordingTrackToDisplay = gpxFileToDisplay;
		displayGPXFile(gpxFileToDisplay, showCurrentGpxFile);
	}

	public void displayGPXFile(GPXFile gpxFileToDisplay, boolean showCurrentGpxFile) {
		osmandSettings.SHOW_CURRENT_GPX_TRACK.set(showCurrentGpxFile);
		if (gpxFileToDisplay == null) {
			getFavorites().setFavoritePointsFromGPXFile(null);
		} else {
			List<FavouritePoint> pts = new ArrayList<FavouritePoint>();
			for (WptPt p : gpxFileToDisplay.points) {
				FavouritePoint pt = new FavouritePoint();
				pt.setLatitude(p.lat);
				pt.setLongitude(p.lon);
				if (p.name == null) {
					p.name = "";
				}
				pt.setName(p.name);
				pts.add(pt);
			}
			getFavorites().setFavoritePointsFromGPXFile(pts);
		}
	}

	public GPXFile getGpxFileToDisplay() {
		return gpxFileToDisplay;
	}
	
	public GPXFile getRecordingTrackToDisplay() {
		return recordingTrackToDisplay;
	}

	public FavouritesDbHelper getFavorites() {
		if (favorites == null) {
			favorites = new FavouritesDbHelper(this);
		}
		return favorites;
	}

	public ResourceManager getResourceManager() {
		return manager;
	}

	public DayNightHelper getDaynightHelper() {
		return daynightHelper;
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		manager.onLowMemory();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (prefferedLocale != null) {
			newConfig.locale = prefferedLocale;
			Locale.setDefault(prefferedLocale);
			getBaseContext().getResources().updateConfiguration(newConfig, getBaseContext().getResources().getDisplayMetrics());
		}
	}

	public void checkPrefferedLocale() {
		Configuration config = getBaseContext().getResources().getConfiguration();
		String lang = osmandSettings.PREFERRED_LOCALE.get();
		if (!"".equals(lang) && !config.locale.getLanguage().equals(lang)) {
			prefferedLocale = new Locale(lang);
			Locale.setDefault(prefferedLocale);
			config.locale = prefferedLocale;
			getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
		}

	}

	public static final int PROGRESS_DIALOG = 5;

	/**
	 * @param activity
	 *            that supports onCreateDialog({@link #PROGRESS_DIALOG}) and
	 *            returns @param progressdialog
	 * @param progressDialog
	 *            - it should be exactly the same as onCreateDialog
	 * @return
	 */
	public void checkApplicationIsBeingInitialized(Activity activity, ProgressDialog progressDialog) {
		// start application if it was previously closed
		startApplication();
		synchronized (OsmandApplication.this) {
			if (startDialog != null) {
				progressDialog.setTitle(getString(R.string.loading_data));
				progressDialog.setMessage(getString(R.string.reading_indexes));
				activity.showDialog(PROGRESS_DIALOG);
				startDialog.setDialog(progressDialog);
			} else if (startingWarnings != null) {
				showWarnings(startingWarnings, activity);
			}
		}
	}

	public boolean isApplicationInitializing() {
		return startDialog != null;
	}

	public RoutingHelper getRoutingHelper() {
		return routingHelper;
	}

	public CommandPlayer getPlayer() {
		return player;
	}

	public void showDialogInitializingCommandPlayer(final Activity uiContext) {
		showDialogInitializingCommandPlayer(uiContext, true);
	}

	public void showDialogInitializingCommandPlayer(final Activity uiContext, boolean warningNoneProvider) {
		showDialogInitializingCommandPlayer(uiContext, warningNoneProvider, null);
	}

	public void showDialogInitializingCommandPlayer(final Activity uiContext, boolean warningNoneProvider, Runnable run) {
		String voiceProvider = osmandSettings.VOICE_PROVIDER.get();
		if (voiceProvider == null || OsmandSettings.VOICE_PROVIDER_NOT_USE.equals(voiceProvider)) {
			if (warningNoneProvider && voiceProvider == null) {
				Builder builder = new AlertDialog.Builder(uiContext);
				builder.setCancelable(true);
				builder.setNegativeButton(R.string.default_buttons_cancel, null);
				builder.setPositiveButton(R.string.default_buttons_ok,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								Intent intent = new Intent(uiContext,
										SettingsActivity.class);
								intent.putExtra(
										SettingsActivity.INTENT_KEY_SETTINGS_SCREEN,
										SettingsActivity.SCREEN_NAVIGATION_SETTINGS);
								uiContext.startActivity(intent);
							}
						});
				builder.setTitle(R.string.voice_is_not_available_title);
				builder.setMessage(R.string.voice_is_not_available_msg);
				builder.show();
			}

		} else {
			if (player == null || !Algoritms.objectEquals(voiceProvider, player.getCurrentVoice())) {
				initVoiceDataInDifferentThread(uiContext, voiceProvider, run);
			}
		}

	}

	private void initVoiceDataInDifferentThread(final Activity uiContext, final String voiceProvider, final Runnable run) {
		final ProgressDialog dlg = ProgressDialog.show(uiContext,
				getString(R.string.loading_data),
				getString(R.string.voice_data_initializing));
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					if (player != null) {
						player.clear();
					}
					player = CommandPlayerFactory.createCommandPlayer(voiceProvider, OsmandApplication.this, uiContext);
					routingHelper.getVoiceRouter().setPlayer(player);
					dlg.dismiss();
					if (run != null && uiContext != null) {
						uiContext.runOnUiThread(run);
					}
				} catch (CommandPlayerException e) {
					dlg.dismiss();
					showWarning(uiContext, e.getError());
				}
			}
		}).start();
	}

	public NavigationService getNavigationService() {
		return navigationService;
	}

	public void setNavigationService(NavigationService navigationService) {
		this.navigationService = navigationService;
	}
	
	public BidForFixHelper getBidForFix() {
		return bidforfix;
	}
	

	public synchronized void closeApplication() {
		if (applicationInitializing) {
			manager.close();
		}
		applicationInitializing = false;
	}

	public synchronized void startApplication() {
		if (applicationInitializing) {
			return;
		}
		applicationInitializing = true;
		startDialog = new ProgressDialogImplementation(this, null, false);

		startDialog.setRunnable("Initializing app", new Runnable() { //$NON-NLS-1$
					@Override
					public void run() {
						startApplicationBackground();
					}
				});
		startDialog.run();

		Thread.setDefaultUncaughtExceptionHandler(new DefaultExceptionHandler());

	}

	public String exportFavorites(File f) {
		GPXFile gpx = new GPXFile();
		for (FavouritePoint p : getFavorites().getFavouritePoints()) {
			if (p.isStored()) {
				WptPt pt = new WptPt();
				pt.lat = p.getLatitude();
				pt.lon = p.getLongitude();
				pt.name = p.getName() + "_" + p.getCategory();
				gpx.points.add(pt);
			}
		}
		return GPXUtilities.writeGpxFile(f, gpx, this);
	}
	
	private List<String> checkBaseMapExisted(IProgress progress) {
		List<String> warnings = new ArrayList<String>();
		boolean baseMapExists = OsmandApplication.getSettings().extendOsmandPath(ResourceManager.MAPS_PATH + Constants.BASEMAP_NAME).exists();
		if(!baseMapExists){
			progress.startTask(getApplicationContext().getString(R.string.copying_basemap), -1); 
			try {
				String basemapPath = OsmandApplication.getSettings().extendOsmandPath(ResourceManager.MAPS_PATH + Constants.BASEMAP_NAME).toString();
				OutputStream output = new FileOutputStream(basemapPath);
				for(int i=1;i<=4;i++){
					InputStream is = getAssets().open(Constants.BASEMAP_NAME_DIR + "/" + Constants.BASEMAP_NAME_PREFIX+i+Constants.BASEMAP_NAME_EXT);
					IOUtils.copy(is, output);
			        is.close();
			        is = null;
			    }
				output.close();
				output = null;
			} catch (IOException e) {
				e.printStackTrace();
				warnings.add(getString(R.string.basemap_can_not_be_copied));
			}
		}
		return warnings;
	}


	private List<String> checkVoiceDataExists(IProgress progress) {
		List<String> warnings = new ArrayList<String>();
		String[] subdirs = { "Deutsch", "English-Metric", "English-Miles", "Nederlands" };
		for(int i=(subdirs.length-1); i >= 0 ;i--){
			boolean voiceDataExists = OsmandApplication.getSettings().extendOsmandPath(ResourceManager.VOICE_PATH + subdirs[i]).exists();
			if(!voiceDataExists){
				progress.startTask(getApplicationContext().getString(R.string.copying_voicedata), -1); 
				try {
					String basemapPath =  OsmandApplication.getSettings().extendOsmandPath(ResourceManager.VOICE_PATH + subdirs[i]).toString();
					File dir = new File(basemapPath);
					dir.mkdirs();
					File file = new File(dir, "/ttsconfig.p");
					OutputStream output = new FileOutputStream(file);

					InputStream is = getAssets().open("voice" + "/" + subdirs[i] + "/ttsconfig.p");
					IOUtils.copy(is, output);
			        is.close();
			        is = null;
			        output.close();
			        output = null;
				} catch (IOException e) {
					e.printStackTrace();
					warnings.add(getString(R.string.voicedata_can_not_be_copied));
				}
			}
		}
		return warnings;
	}
	
	
	private void startApplicationBackground() {
		List<String> warnings = new LinkedList<String>();
		try {
			/*			if (osmandSettings.NATIVE_RENDERING.get()) {
				startDialog.startTask(getString(R.string.init_native_library), -1);
				RenderingRulesStorage storage = rendererRegistry.getCurrentSelectedRenderer();
/*				boolean initialized = NativeOsmandLibrary.getLibrary(storage) != null;
				if (!initialized) {
					LOG.info("Native library could not loaded!");
					osmandSettings.NATIVE_RENDERING.set(false);
				}
			}
				*/
			warnings.addAll(checkBaseMapExisted(startDialog));
			warnings.addAll(checkVoiceDataExists(startDialog));
			warnings.addAll(manager.reloadIndexes(startDialog));
			player = null;
			SavingTrackHelper helper = new SavingTrackHelper(OsmandApplication.this);
			if (helper.hasDataToSave()) {
				startDialog.startTask(getString(R.string.saving_gpx_tracks), -1);
				warnings.addAll(helper.saveDataToGpx());
			}
			helper.close();

			// restore backuped favorites to normal file
			final File appDir = OsmandApplication.getSettings().extendOsmandPath(ResourceManager.APP_DIR);
			File save = new File(appDir, FavouritesDbHelper.FILE_TO_SAVE);
			File bak = new File(appDir, FavouritesDbHelper.FILE_TO_BACKUP);
			if (bak.exists() && (!save.exists() || bak.lastModified() > save.lastModified())) {
				if (save.exists()) {
					save.delete();
				}
				bak.renameTo(save);
			}
		} finally {
			synchronized (OsmandApplication.this) {
				final ProgressDialog toDismiss;
				if (startDialog != null) {
					toDismiss = startDialog.getDialog();
				} else {
					toDismiss = null;
				}
				startDialog = null;

				if (toDismiss != null) {
					uiHandler.post(new Runnable() {
						@Override
						public void run() {
							if (toDismiss != null) {
								//TODO handling this dialog is bad, we need a better standard way
								toDismiss.dismiss();
//								toDismiss.getOwnerActivity().dismissDialog(PROGRESS_DIALOG);
							}
						}
					});
					showWarnings(warnings, toDismiss.getContext());
				} else {
					startingWarnings = warnings;
				}
			}
		}
	}

	protected void showWarnings(List<String> warnings, final Context uiContext) {
		if (warnings != null && !warnings.isEmpty()) {
			final StringBuilder b = new StringBuilder();
			boolean f = true;
			for (String w : warnings) {
				if (f) {
					f = false;
				} else {
					b.append('\n');
				}
				b.append(w);
			}
			showWarning(uiContext, b.toString());
		}
	}

	private void showWarning(final Context uiContext, final String b) {
		uiHandler.post(new Runnable() {
			@Override
			public void run() {
				AccessibleToast.makeText(uiContext, b.toString(), Toast.LENGTH_LONG).show();
			}
		});
	}

	private class DefaultExceptionHandler implements UncaughtExceptionHandler {

		private UncaughtExceptionHandler defaultHandler;

		public DefaultExceptionHandler() {
			defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		}

		@Override
		public void uncaughtException(final Thread thread, final Throwable ex) {
			File file = osmandSettings.extendOsmandPath(EXCEPTION_PATH);
			try {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				PrintStream printStream = new PrintStream(out);
				ex.printStackTrace(printStream);
				StringBuilder msg = new StringBuilder();
				msg.append(
						"Exception occured in thread " + thread.toString() + " : "). //$NON-NLS-1$ //$NON-NLS-2$
						append(DateFormat.format("MMMM dd, yyyy h:mm:ss", System.currentTimeMillis())).append("\n"). //$NON-NLS-1$//$NON-NLS-2$
						append(new String(out.toByteArray()));

				if (file.getParentFile().canWrite()) {
					BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
					writer.write(msg.toString());
					writer.close();
				}
				defaultHandler.uncaughtException(thread, ex);
			} catch (Exception e) {
				// swallow all exceptions
				android.util.Log.e(LogUtil.TAG, "Exception while handle other exception", e); //$NON-NLS-1$
			}

		}
	}
}
