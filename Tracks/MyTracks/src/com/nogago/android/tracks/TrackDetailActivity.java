/*
 * Copyright 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.nogago.android.tracks;

import com.google.android.apps.mytracks.AbstractMyTracksActivity;
import com.google.android.apps.mytracks.MyTracksApplication;
import com.google.android.apps.mytracks.TabManager;
import com.google.android.apps.mytracks.content.MyTracksProviderUtils;
import com.google.android.apps.mytracks.content.Track;
import com.google.android.apps.mytracks.content.TrackDataHub;
import com.google.android.apps.mytracks.content.Waypoint;
import com.google.android.apps.mytracks.content.WaypointCreationRequest;
import com.google.android.apps.mytracks.fragments.ChartFragment;
import com.google.android.apps.mytracks.fragments.ChooseActivityDialogFragment;
import com.google.android.apps.mytracks.fragments.DeleteOneTrackDialogFragment;
import com.google.android.apps.mytracks.fragments.DeleteOneTrackDialogFragment.DeleteOneTrackCaller;
import com.google.android.apps.mytracks.fragments.FrequencyDialogFragment;
import com.google.android.apps.mytracks.fragments.InstallEarthDialogFragment;
import com.google.android.apps.mytracks.fragments.StatsFragment;
import com.google.android.apps.mytracks.io.file.TrackWriterFactory.TrackFileFormat;
import com.google.android.apps.mytracks.services.TrackRecordingServiceConnection;
import com.google.android.apps.mytracks.util.AnalyticsUtils;
import com.google.android.apps.mytracks.util.ApiAdapterFactory;
import com.google.android.apps.mytracks.util.IntentUtils;
import com.google.android.apps.mytracks.util.PreferencesUtils;
import com.google.android.apps.mytracks.util.TrackRecordingServiceConnectionUtils;
import com.nogago.android.task.AsyncTaskManager;
import com.nogago.android.task.OnTaskCompleteListener;
import com.nogago.android.task.TrackableTask;
import com.nogago.android.tracks.io.GPXUploadTask;
import com.nogago.android.tracks.io.UploadTaskException;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.Toast;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * An activity to show the track detail.
 * 
 * @author Leif Hendrik Wilden
 * @author Rodrigo Damazio
 */
public class TrackDetailActivity extends AbstractMyTracksActivity implements DeleteOneTrackCaller,
    OnTaskCompleteListener {

  public static final String EXTRA_TRACK_ID = "track_id";
  public static final String EXTRA_MARKER_ID = "marker_id";

  private static final String TAG = TrackDetailActivity.class.getSimpleName();
  private static final String CURRENT_TAG_KEY = "tab";

  private TrackDataHub trackDataHub;
  private TrackRecordingServiceConnection trackRecordingServiceConnection;
  private TabHost tabHost;
  private TabManager tabManager;
  public long trackId;
  public static long TRACK_ID = -1L;
  private long markerId;
  public static boolean RECORDING = false;

  private MenuItem stopRecordingMenuItem;
  private MenuItem insertMarkerMenuItem;
  private MenuItem playMenuItem;
  private MenuItem shareMenuItem;
  private MenuItem voiceFrequencyMenuItem;
  private MenuItem splitFrequencyMenuItem;
  private MenuItem sendGoogleMenuItem;
  private MenuItem saveMenuItem;

 //private View mapViewContainer;

  /*
   * Note that sharedPreferenceChangeListener cannot be an anonymous inner
   * class. Anonymous inner class will get garbage collected.
   */
  private final OnSharedPreferenceChangeListener sharedPreferenceChangeListener = new OnSharedPreferenceChangeListener() {
    @Override
    public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
      // Note that key can be null
      if (PreferencesUtils.getKey(TrackDetailActivity.this, R.string.recording_track_id_key)
          .equals(key)) {
        updateMenu();
      }
    }
  };

  /**
   * We are not displaying driving directions. Just an arbitrary track that is
   * not associated to any licensed mapping data. Therefore it should be okay to
   * return false here and still comply with the terms of service.
  
  @Override
  protected boolean isRouteDisplayed() {
    return false;
  }
   */

  /**
   * We are displaying a location. This needs to return true in order to comply
   * with the terms of service.
   
  @Override
  protected boolean isLocationDisplayed() {
    return true;
  }
  */
  public long returnTrackId() {
    if (trackId > 0) {
      TRACK_ID = trackId;
      return TRACK_ID;
    } 
    return -1L;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    handleIntent(getIntent());
    returnTrackId();
    Intent intent2 = getIntent();
    Intent intent;
    boolean clicked = intent2.getBooleanExtra("clicked", false);
    boolean fromTrackList = intent2.getBooleanExtra("fromTrackList", false);
    ApiAdapterFactory.getApiAdapter().hideTitle(this);
    setContentView(R.layout.track_detail);

    getSharedPreferences(Constants.SETTINGS_NAME, Context.MODE_PRIVATE)
        .registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
    trackRecordingServiceConnection = new TrackRecordingServiceConnection(this, null);
    trackDataHub = ((MyTracksApplication) getApplication()).getTrackDataHub();
    trackDataHub.loadTrack(trackId);

   // mapViewContainer = getLayoutInflater().inflate(R.layout.map, null);
   // ApiAdapterFactory.getApiAdapter().disableHardwareAccelerated(mapViewContainer);

    tabHost = (TabHost) findViewById(android.R.id.tabhost);
    tabHost.setup();
    tabManager = new TabManager(this, tabHost, R.id.realtabcontent);

    TabSpec chartTabSpec = tabHost.newTabSpec(ChartFragment.CHART_FRAGMENT_TAG).setIndicator(
        getString(R.string.track_detail_chart_tab),
        getResources().getDrawable(R.drawable.tab_chart));
    tabManager.addTab(chartTabSpec, ChartFragment.class, null);
    TabSpec statsTabSpec = tabHost.newTabSpec(StatsFragment.STATS_FRAGMENT_TAG).setIndicator(
        getString(R.string.track_detail_stats_tab),
        getResources().getDrawable(R.drawable.tab_stats));
    tabManager.addTab(statsTabSpec, StatsFragment.class, null);
    TabSpec mapTabSpec = tabHost.newTabSpec("mapFragment").setIndicator(
        getString(R.string.track_detail_map_tab), getResources().getDrawable(R.drawable.tab_map));
    tabManager.addTab(mapTabSpec, StatsFragment.class, null);

    tabHost.setCurrentTabByTag(StatsFragment.STATS_FRAGMENT_TAG);    showMarker();
    if(clicked == true) {
      AnalyticsUtils.sendPageViews(this, "/action/play");
      intent = IntentUtils.newIntent(this, SaveActivity.class)
          .putExtra(SaveActivity.EXTRA_TRACK_ID, TRACK_ID)
          .putExtra(SaveActivity.EXTRA_TRACK_FILE_FORMAT, (Parcelable) TrackFileFormat.GPX)
          .putExtra(SaveActivity.EXTRA_SHOW_TRACK, true)
          .putExtra("EXTRA_RECORDING", RECORDING);
      startActivity(intent);
    }
    if(fromTrackList == true) {
      AnalyticsUtils.sendPageViews(this, "/action/play");
      intent = IntentUtils.newIntent(this, SaveActivity.class)
          .putExtra(SaveActivity.EXTRA_TRACK_ID, TRACK_ID)
          .putExtra(SaveActivity.EXTRA_TRACK_FILE_FORMAT, (Parcelable) TrackFileFormat.GPX)
          .putExtra(SaveActivity.EXTRA_FOLLOW_TRACK, true)
          .putExtra("EXTRA_RECORDING", RECORDING);
      startActivity(intent);
    }
  }

  @Override
  public void onNewIntent(Intent intent) {
    setIntent(intent);
    handleIntent(intent);
    trackDataHub.loadTrack(trackId);
    showMarker();
  }

  @Override
  protected void onStart() {
    super.onStart();
    trackDataHub.start();
  }

  @Override
  protected void onResume() {
    super.onResume();
    TrackRecordingServiceConnectionUtils.resume(this, trackRecordingServiceConnection);
    setTitle(trackId == PreferencesUtils.getLong(this, R.string.recording_track_id_key));
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putString(CURRENT_TAG_KEY, tabHost.getCurrentTabTag());
  }

  @Override
  protected void onStop() {
    super.onStop();
    trackDataHub.stop();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    trackRecordingServiceConnection.unbind();
  }

  /*
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.track_detail, menu);
    String fileTypes[] = getResources().getStringArray(R.array.file_types);
    menu.findItem(R.id.track_detail_save_gpx).setTitle(
        getString(R.string.menu_save_format, fileTypes[0]));
    menu.findItem(R.id.track_detail_save_kml).setTitle(
        getString(R.string.menu_save_format, fileTypes[1]));
    menu.findItem(R.id.track_detail_save_csv).setTitle(
        getString(R.string.menu_save_format, fileTypes[2]));
    menu.findItem(R.id.track_detail_save_tcx).setTitle(
        getString(R.string.menu_save_format, fileTypes[3]));

    stopRecordingMenuItem = menu.findItem(R.id.track_detail_stop_recording);
    insertMarkerMenuItem = menu.findItem(R.id.track_detail_insert_marker);
    playMenuItem = menu.findItem(R.id.track_detail_play);
    shareMenuItem = menu.findItem(R.id.track_detail_share);
    voiceFrequencyMenuItem = menu.findItem(R.id.track_detail_voice_frequency);
    splitFrequencyMenuItem = menu.findItem(R.id.track_detail_split_frequency);
    sendGoogleMenuItem = menu.findItem(R.id.track_detail_send_google);
    saveMenuItem = menu.findItem(R.id.track_detail_save);

    updateMenu();
    return true;
  }
  */

  @Override
  protected void onHomeSelected() {
    Intent intent = IntentUtils.newIntent(this, TrackListActivity.class);
    startActivity(intent);
    finish();
  }

  
  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    String sensorTypeValueNone = getString(R.string.sensor_type_value_none);
    boolean showSensorState = !sensorTypeValueNone.equals(PreferencesUtils.getString(this,
        R.string.sensor_type_key, sensorTypeValueNone));
    menu.findItem(R.id.track_detail_sensor_state).setVisible(showSensorState);
    return super.onPrepareOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    Intent intent;
    switch (item.getItemId()) {
      case R.id.track_detail_stop_recording:
        updateMenuItems(false);
        setTitle(false);
        TrackRecordingServiceConnectionUtils.stop(this, trackRecordingServiceConnection, true);
        return true;
      case R.id.track_detail_insert_marker:
        AnalyticsUtils.sendPageViews(this, "/action/insert_marker");
        intent = IntentUtils.newIntent(this, MarkerEditActivity.class).putExtra(
            MarkerEditActivity.EXTRA_TRACK_ID, trackId);
        startActivity(intent);
        return true;
      case R.id.track_detail_play:
        if (isEarthInstalled()) {
          AnalyticsUtils.sendPageViews(this, "/action/play");
          intent = IntentUtils.newIntent(this, SaveActivity.class)
              .putExtra(SaveActivity.EXTRA_TRACK_ID, trackId)
              .putExtra(SaveActivity.EXTRA_TRACK_FILE_FORMAT, (Parcelable) TrackFileFormat.KML)
              .putExtra(SaveActivity.EXTRA_PLAY_TRACK, true);
          startActivity(intent);
        } else {
          new InstallEarthDialogFragment().show(getSupportFragmentManager(),
              InstallEarthDialogFragment.INSTALL_EARTH_DIALOG_TAG);
        }
        return true;
      case R.id.track_detail_share:
        AnalyticsUtils.sendPageViews(this, "/action/share");
        ChooseActivityDialogFragment.newInstance(trackId, null).show(getSupportFragmentManager(),
            ChooseActivityDialogFragment.CHOOSE_ACTIVITY_DIALOG_TAG);
        return true;
      case R.id.track_detail_markers:
        intent = IntentUtils.newIntent(this, MarkerListActivity.class).putExtra(
            MarkerListActivity.EXTRA_TRACK_ID, trackId);
        startActivity(intent);
        return true;
      case R.id.track_detail_voice_frequency:
        FrequencyDialogFragment.newInstance(R.string.announcement_frequency_key,
            PreferencesUtils.ANNOUNCEMENT_FREQUENCY_DEFAULT,
            R.string.settings_voice_frequency_title).show(getSupportFragmentManager(),
            FrequencyDialogFragment.FREQUENCY_DIALOG_TAG);
        return true;
      case R.id.track_detail_split_frequency:
        FrequencyDialogFragment.newInstance(R.string.split_frequency_key,
            PreferencesUtils.SPLIT_FREQUENCY_DEFAULT, R.string.settings_split_frequency_title)
            .show(getSupportFragmentManager(), FrequencyDialogFragment.FREQUENCY_DIALOG_TAG);
        return true;
      case R.id.track_detail_send_google:
        /*
         * AnalyticsUtils.sendPageViews(this, "/action/send_google");
         * ChooseUploadServiceDialogFragment.newInstance(new
         * SendRequest(trackId)).show( getSupportFragmentManager(),
         * ChooseUploadServiceDialogFragment.CHOOSE_UPLOAD_SERVICE_DIALOG_TAG);
         */
        sendTrackToNogago();
        return true;
      case R.id.track_detail_save_gpx:
        startSaveActivity(TrackFileFormat.GPX);
        return true;
      case R.id.track_detail_save_kml:
        startSaveActivity(TrackFileFormat.KML);
        return true;
      case R.id.track_detail_save_csv:
        startSaveActivity(TrackFileFormat.CSV);
        return true;
      case R.id.track_detail_save_tcx:
        startSaveActivity(TrackFileFormat.TCX);
        return true;
      case R.id.track_detail_edit:
        intent = IntentUtils.newIntent(this, TrackEditActivity.class).putExtra(
            TrackEditActivity.EXTRA_TRACK_ID, trackId);
        startActivity(intent);
        return true;
      case R.id.track_detail_delete:
        DeleteOneTrackDialogFragment.newInstance(trackId).show(getSupportFragmentManager(),
            DeleteOneTrackDialogFragment.DELETE_ONE_TRACK_DIALOG_TAG);
        return true;
      case R.id.track_detail_sensor_state:
        intent = IntentUtils.newIntent(this, SensorStateActivity.class);
        startActivity(intent);
        return true;
      case R.id.track_detail_settings:
        intent = IntentUtils.newIntent(this, SettingsActivity.class);
        startActivity(intent);
        return true;
      case R.id.track_detail_help:
        intent = IntentUtils.newIntent(this, HelpActivity.class);
        startActivity(intent);
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  private void sendTrackToNogago() {
    if (isOnline()) {
      if (getUserName() != null && getUserName().compareTo("username") != 0) {
        // Create manager and set this activity as context and listener
        AsyncTaskManager mAsyncTaskManager = new AsyncTaskManager(this, this, "TEST", true);
        mAsyncTaskManager.handleRetainedTask(getLastNonConfigurationInstance());
        GPXUploadTask task = new GPXUploadTask(this, "Title", getUserName(), getPassword(), trackId);
        mAsyncTaskManager.setupTask(task);
      } else {
        android.content.DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            Intent settings = new Intent(TrackDetailActivity.this, SettingsActivity.class);
            startActivity(settings);
          }
        };
        showAlertDialog(this, getString(R.string.wrong_credential),
            getString(R.string.error_username), listener);
      }

    } else {
      Toast.makeText(TrackDetailActivity.this, R.string.error_network, Toast.LENGTH_LONG).show();
    }
  }

  private void showAlertDialog(Context context, String title, String message,
      OnClickListener listener) {
    Builder builder = new AlertDialog.Builder(context);
    builder.setTitle(title);
    builder.setIcon(android.R.drawable.ic_dialog_info);
    builder.setMessage(message);
    builder.setNeutralButton(R.string.ok_button, listener);
    builder.show();
  }

  private boolean isOnline() {
    ConnectivityManager cm = (ConnectivityManager) getSystemService(TrackDetailActivity.this.CONNECTIVITY_SERVICE);
    if (cm == null)
      return false;
    NetworkInfo ni = cm.getActiveNetworkInfo();
    if (ni == null)
      return false;
    return ni.isConnectedOrConnecting();
  }

  private String getUserName() {
    return PreferencesUtils.getString(TrackDetailActivity.this, R.string.user_name, "");
  }

  private String getPassword() {
    return PreferencesUtils.getString(TrackDetailActivity.this, R.string.user_password, "");
  }

  @Override
  public boolean onTrackballEvent(MotionEvent event) {
    if (event.getAction() == MotionEvent.ACTION_DOWN) {
      if (TrackRecordingServiceConnectionUtils.isRecording(this, trackRecordingServiceConnection)) {
        TrackRecordingServiceConnectionUtils.addMarker(this, trackRecordingServiceConnection,
            WaypointCreationRequest.DEFAULT_WAYPOINT);
        return true;
      }
    }
    return super.onTrackballEvent(event);
  }

  /**
   * @return the mapViewContainer
 
  public View getMapViewContainer() {
    return mapViewContainer;
  }
    */

  /**
   * Handles the data in the intent.
   */
  private void handleIntent(Intent intent) {
    trackId = intent.getLongExtra(EXTRA_TRACK_ID, -1L);
    markerId = intent.getLongExtra(EXTRA_MARKER_ID, -1L);
    if (markerId != -1L) {
      Waypoint waypoint = MyTracksProviderUtils.Factory.get(this).getWaypoint(markerId);
      if (waypoint == null) {
        exit();
        return;
      }
      trackId = waypoint.getTrackId();
    }
    if (trackId == -1L) {
      exit();
      return;
    }
  }

  /**
   * Exists and returns to {@link TrackListActivity}.
   */
  private void exit() {
    Intent newIntent = IntentUtils.newIntent(this, TrackListActivity.class);
    startActivity(newIntent);
    finish();
  }

  /**
   * Shows marker.
   */
  private void showMarker() {
    /*
    if (markerId != -1L) {
      MapFragment mapFragmet = (MapFragment) getSupportFragmentManager().findFragmentByTag(
          MapFragment.MAP_FRAGMENT_TAG);
      if (mapFragmet != null) {
        tabHost.setCurrentTabByTag(MapFragment.MAP_FRAGMENT_TAG);
        mapFragmet.showMarker(trackId, markerId);
      } else {
        Log.e(TAG, "MapFragment is null");
      }
    }
      */
  }

  /**
   * Sets the title.
   * 
   * @param isRecording true if recording
   */
  private void setTitle(boolean isRecording) {
    String title;
    if (isRecording) {
      title = getString(R.string.track_detail_title_recording);
      RECORDING = true;
    } else {
      Track track = MyTracksProviderUtils.Factory.get(this).getTrack(trackId);
      title = track != null ? track.getName() : getString(R.string.my_tracks_app_name);
    }
    setTitle(title);
  }

  /**
   * Updates the menu.
   */
  private void updateMenu() {
    updateMenuItems(trackId == PreferencesUtils.getLong(this, R.string.recording_track_id_key));
  }

  /**
   * Updates the menu items.
   * 
   * @param isRecording true if recording
   */
  private void updateMenuItems(boolean isRecording) {
    if (stopRecordingMenuItem != null) {
      stopRecordingMenuItem.setVisible(isRecording);
    }
    if (insertMarkerMenuItem != null) {
      insertMarkerMenuItem.setVisible(isRecording);
    }
    if (playMenuItem != null) {
      playMenuItem.setVisible(!isRecording);
    }
    if (shareMenuItem != null) {
      shareMenuItem.setVisible(!isRecording);
    }
    if (voiceFrequencyMenuItem != null) {
      voiceFrequencyMenuItem.setVisible(isRecording);
    }
    if (splitFrequencyMenuItem != null) {
      splitFrequencyMenuItem.setVisible(isRecording);
    }
    if (sendGoogleMenuItem != null) {
      sendGoogleMenuItem.setVisible(!isRecording);
    }
    if (saveMenuItem != null) {
      saveMenuItem.setVisible(!isRecording);
    }
  }

  /**
   * Starts the {@link SaveActivity} to save a track.
   * 
   * @param trackFileFormat the track file format
   */
  public void startSaveActivity(TrackFileFormat trackFileFormat) {
    
    AnalyticsUtils.sendPageViews(this, "/action/save");
    Intent intent = IntentUtils.newIntent(this, SaveActivity.class)
        .putExtra(SaveActivity.EXTRA_TRACK_ID, trackId)
        .putExtra(SaveActivity.EXTRA_TRACK_FILE_FORMAT, (Parcelable) trackFileFormat);
    startActivity(intent);
  }
  /**
   * Returns true if Google Earth app is installed.
   */
  private boolean isEarthInstalled() {
    List<ResolveInfo> infos = getPackageManager().queryIntentActivities(
        new Intent().setType(SaveActivity.GOOGLE_EARTH_KML_MIME_TYPE),
        PackageManager.MATCH_DEFAULT_ONLY);
    for (ResolveInfo info : infos) {
      if (info.activityInfo != null && info.activityInfo.packageName != null
          && info.activityInfo.packageName.equals(SaveActivity.GOOGLE_EARTH_PACKAGE)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public TrackRecordingServiceConnection getTrackRecordingServiceConnection() {
    return trackRecordingServiceConnection;
  }

  @Override
  public void onTaskComplete(AsyncTask task) {
    // nogago upload completed
    if (task == null) {

    } else if (task.isCancelled()) {
      if (task instanceof TrackableTask)
        ((TrackableTask) task).cleanup();
      // Report about cancel
      Toast.makeText(this, R.string.task_cancelled, Toast.LENGTH_LONG).show();
    } else {
      Object result = new Object();
      try {
        result = task.get();
      } catch (InterruptedException e) {
        e.printStackTrace();
      } catch (ExecutionException e) {
        e.printStackTrace();
      }
      if (result instanceof UploadTaskException) {
        Toast.makeText(TrackDetailActivity.this, ((UploadTaskException) result).getMessage(),
            Toast.LENGTH_LONG).show();
      } else {
        Toast.makeText(TrackDetailActivity.this, R.string.successfully_uploaded_track,
            Toast.LENGTH_LONG).show();
      }
    }
  }
}