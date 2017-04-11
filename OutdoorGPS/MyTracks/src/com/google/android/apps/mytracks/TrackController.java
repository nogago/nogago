/*
 * Copyright 2012 Google Inc.
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

package com.google.android.apps.mytracks;

import com.google.android.apps.mytracks.services.ITrackRecordingService;
import com.google.android.apps.mytracks.services.TrackRecordingServiceConnection;
import com.nogago.bb10.tracks.R;

import android.app.Activity;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

/**
 * Track controller for record, pause, resume, and stop.
 * 
 * @author Jimmy Shih
 */
public class TrackController {

  private static final String TAG = TrackController.class.getSimpleName();
  private static final int ONE_SECOND = 1000;

  private final Activity activity;
  private final TrackRecordingServiceConnection trackRecordingServiceConnection;
  private final Handler handler;
  private View containerView;
  // private final TextView statusTextView;
  // private final TextView totalTimeTextView;
  private ImageButton recordImageButton;
  // private ImageButton markerImageButton;
  private ImageButton stopImageButton;
  private boolean alwaysShow;

  private boolean isRecording;
  private boolean isPaused;
  private long totalTime = 0;
  // the timestamp for the toal time
  private long totalTimeTimestamp = 0;

  // A runnable to update the total time.
  private final Runnable updateTotalTimeRunnable = new Runnable() {
    public void run() {
      if (isRecording && !isPaused) {
        // totalTimeTextView.setText(StringUtils.formatElapsedTimeWithHour(System.currentTimeMillis()
        // - totalTimeTimestamp + totalTime));
        handler.postDelayed(this, ONE_SECOND);
      }
    }
  };

  public TrackController(Activity activity,
      TrackRecordingServiceConnection trackRecordingServiceConnection, boolean alwaysShow,
      OnClickListener recordListener, OnClickListener stopListener) { // ,
                                                                      // OnClickListener
                                                                      // markerListener)
                                                                      // {
    this.activity = activity;
    this.trackRecordingServiceConnection = trackRecordingServiceConnection;
    this.alwaysShow = alwaysShow;
    handler = new Handler();
    // statusTextView = (TextView)
    // activity.findViewById(R.id.track_controller_status);
    // totalTimeTextView = (TextView)
    // activity.findViewById(R.id.track_controller_total_time);
    View view = null;
    try {
      view = (View) activity.findViewById(R.id.track_controller_container);
    } catch (NullPointerException ne1) {
      view = null;
    }
    if (view != null)
      containerView = view;

    try {
      view = (View) activity.findViewById(R.id.listBtnBarRecord);
    } catch (NullPointerException ne1) {
      view = null;
    }
    if (view != null) {
      recordImageButton = (ImageButton) view;
      recordImageButton.setOnClickListener(recordListener);
    } else
      recordImageButton = null;
    try {
      view = (View) activity.findViewById(R.id.listBtnBarStop);
    } catch (NullPointerException ne1) {
      view = null;
    }
    if (view != null) {
      stopImageButton = (ImageButton) view;
      stopImageButton.setOnClickListener(stopListener);
    } else
      stopImageButton = null;
    // Buttons not visible

    /*
     * if(markerListener != null) { markerImageButton = (ImageButton)
     * activity.findViewById(R.id.listBtnBarMarker);
     * markerImageButton.setOnClickListener(markerListener); } else {
     * markerImageButton=null; }
     */
  }

  public void update(boolean recording, boolean paused) {
    isRecording = recording;
    isPaused = paused;
    // containerView.setVisibility(alwaysShow || isRecording ? View.VISIBLE :
    // View.GONE);

    if (!alwaysShow && !isRecording) {
      stop();
      return;
    }
    if(recordImageButton != null) {
    recordImageButton.setImageResource(isRecording && !isPaused ? R.drawable.ic_pause
        : R.drawable.ic_record);
    recordImageButton.setContentDescription(activity
        .getString(isRecording && !isPaused ? R.string.icon_pause_recording
            : R.string.icon_record_track));
    }
    /*
     * LayoutParams lp = recordImageButton.getLayoutParams(); lp.width=144;
     * lp.height=81; recordImageButton.setLayoutParams(lp);
     * recordImageButton.setScaleType(ScaleType.CENTER);
     */
    if(stopImageButton != null) {
    stopImageButton.setImageResource(isRecording ? R.drawable.ic_stop_1 : R.drawable.ic_stop_0);
    stopImageButton.setEnabled(isRecording);
    }
    /*
     * lp = stopImageButton.getLayoutParams(); lp.width=144; lp.height=81;
     * stopImageButton.setLayoutParams(lp);
     * stopImageButton.setScaleType(ScaleType.CENTER);
     */

    /*
     * statusTextView.setVisibility(isRecording ? View.VISIBLE :
     * View.INVISIBLE); if (isRecording) {
     * statusTextView.setTextColor(activity.getResources().getColor( isPaused ?
     * android.R.color.white : R.color.red)); statusTextView.setText(isPaused ?
     * R.string.generic_paused : R.string.generic_recording); }
     */
    stop();
    /*
     * totalTime = isRecording ? getTotalTime() : 0L;
     * totalTimeTextView.setText(StringUtils
     * .formatElapsedTimeWithHour(totalTime)); if (isRecording && !isPaused) {
     * totalTimeTimestamp = System.currentTimeMillis();
     * handler.postDelayed(updateTotalTimeRunnable, ONE_SECOND); }
     */
    /*
     * if(markerImageButton != null) {
     * markerImageButton.setImageResource(isRecording ? R.drawable.ic_marker :
     * R.drawable.ic_upload); markerImageButton.setContentDescription(activity
     * .getString(isRecording ? R.string.icon_marker : R.string.icon_upload)); }
     */
  }

  /**
   * Stops the timer.
   */
  public void stop() {
    handler.removeCallbacks(updateTotalTimeRunnable);
  }

  /**
   * Gets the total time for the current recording track.
   */
  private long getTotalTime() {
    ITrackRecordingService trackRecordingService = trackRecordingServiceConnection
        .getServiceIfBound();
    try {
      return trackRecordingService != null ? trackRecordingService.getTotalTime() : 0L;
    } catch (RemoteException e) {
      Log.e(TAG, "exception", e);
      return 0L;
    }
  }
}
