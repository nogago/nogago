/*
 * Copyright 2011 Google Inc.
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

import com.google.android.apps.mytracks.io.file.KmlTrackWriter;
import com.google.android.apps.mytracks.io.file.SaveAsyncTask;
import com.google.android.apps.mytracks.io.file.TrackWriterFactory.TrackFileFormat;
import com.google.android.apps.mytracks.util.DialogUtils;
import com.google.android.apps.mytracks.util.IntentUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import java.io.File;

/**
 * An activity for saving tracks to the SD card. If saving a specific track,
 * option to save it to a temp directory and play the track afterward.
 * 
 * @author Rodrigo Damazio
 */
public class SaveActivity extends Activity {

  public static final String EXTRA_TRACK_FILE_FORMAT = "track_file_format";
  public static final String EXTRA_TRACK_ID = "track_id";
  public static final String EXTRA_PLAY_TRACK = "play_track";
  public static final String EXTRA_SHOW_TRACK = "show_track";
  public static final String EXTRA_FOLLOW_TRACK = "follow_track";

  public static final String GOOGLE_EARTH_KML_MIME_TYPE = "application/vnd.google-earth.kml+xml";
  public static final String GOOGLE_EARTH_PACKAGE = "com.google.earth";
  public static final String GOOGLE_EARTH_MARKET_URL = "market://details?id="
      + GOOGLE_EARTH_PACKAGE;
  private static final String
      GOOGLE_EARTH_TOUR_FEATURE_ID = "com.google.earth.EXTRA.tour_feature_id";
  private static final String GOOGLE_EARTH_CLASS = "com.google.earth.EarthActivity";

  private static final int DIALOG_PROGRESS_ID = 0;
  private static final int DIALOG_RESULT_ID = 1;

  private TrackFileFormat trackFileFormat;
  private long trackId;
  private boolean playTrack;
  private boolean showTrack;
  private boolean followTrack;

  private SaveAsyncTask saveAsyncTask;
  private ProgressDialog progressDialog;

  // result from the AsyncTask
  private boolean success;
  
  // message id from the AsyncTask
  private int messageId;
  
  // saved file path from the AsyncTask
  private String savedPath;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Intent intent = getIntent();
    trackFileFormat = intent.getParcelableExtra(EXTRA_TRACK_FILE_FORMAT);
    trackId = intent.getLongExtra(EXTRA_TRACK_ID, -1L);
    playTrack = intent.getBooleanExtra(EXTRA_PLAY_TRACK, false);
    showTrack = intent.getBooleanExtra(EXTRA_SHOW_TRACK, false);
    followTrack = intent.getBooleanExtra(EXTRA_FOLLOW_TRACK, false);

    Object retained = getLastNonConfigurationInstance();
    if (retained instanceof SaveAsyncTask) {
      saveAsyncTask = (SaveAsyncTask) retained;
      saveAsyncTask.setActivity(this);
    } else {
      if (showTrack == true) {
        playTrack = showTrack;
        boolean onlyOne = true;
        saveAsyncTask = new SaveAsyncTask(this, trackFileFormat, trackId, playTrack, onlyOne);
        saveAsyncTask.execute();
        playTrack = false;
      } if (followTrack == true) {
        playTrack = followTrack;
        boolean onlyOne = true;
        saveAsyncTask = new SaveAsyncTask(this, trackFileFormat, trackId, playTrack, onlyOne);
        saveAsyncTask.execute();
        playTrack = false;
      } else {
        boolean onlyOne = false;
        saveAsyncTask = new SaveAsyncTask(this, trackFileFormat, trackId, playTrack, onlyOne);
        saveAsyncTask.execute();
      }
    }
  }

  @Override
  public Object onRetainNonConfigurationInstance() {
    saveAsyncTask.setActivity(null);
    return saveAsyncTask;
  }

  @Override
  protected Dialog onCreateDialog(int id) {
    switch (id) {
      case DIALOG_PROGRESS_ID:
        progressDialog = DialogUtils.createHorizontalProgressDialog(
            this, R.string.sd_card_save_progress_message, new DialogInterface.OnCancelListener() {
              @Override
              public void onCancel(DialogInterface dialog) {
                saveAsyncTask.cancel(true);
                finish();
              }
            });
        return progressDialog;
      case DIALOG_RESULT_ID:
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
            .setCancelable(true)
            .setIcon(success 
                ? android.R.drawable.ic_dialog_info : android.R.drawable.ic_dialog_alert)
            .setMessage(messageId)
            .setOnCancelListener(new DialogInterface.OnCancelListener() {
              @Override
              public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
                onPostResultDialog();
              }
            })
            .setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int arg1) {
                dialog.dismiss();
                onPostResultDialog();
              }
            })
            .setTitle(success ? R.string.generic_success_title : R.string.generic_error_title);

        if (success && trackId != -1L && !playTrack) {
          builder.setNegativeButton(
              R.string.share_track_share_file, new DialogInterface.OnClickListener() {
                  @Override
                public void onClick(DialogInterface dialog, int which) {
                  Intent intent = IntentUtils.newShareFileIntent(
                      SaveActivity.this, trackId, savedPath, trackFileFormat);
                  startActivity(
                      Intent.createChooser(intent, getString(R.string.share_track_picker_title)));
                  finish();
                }
              });
        }
        return builder.create();
      default:
        return null;
    }
  }

  /**
   * Invokes when the associated AsyncTask completes.
   *
   * @param isSuccess true if the AsyncTask is successful
   * @param aMessageId the id of the AsyncTask message
   * @param aSavedPath the path of the saved file
   */
  public void onAsyncTaskCompleted(boolean isSuccess, int aMessageId, String aSavedPath) {
    this.success = isSuccess;
    this.messageId = aMessageId;
    this.savedPath = aSavedPath;
    removeDialog(DIALOG_PROGRESS_ID);
    showDialog(DIALOG_RESULT_ID);
  }

  /**
   * Shows the progress dialog.
   */
  public void showProgressDialog() {
    showDialog(DIALOG_PROGRESS_ID);
  }

  /**
   * Sets the progress dialog value.
   *
   * @param number the number of points saved
   * @param max the maximum number of points
   */
  public void setProgressDialogValue(int number, int max) {
    if (progressDialog != null) {
      progressDialog.setIndeterminate(false);
      progressDialog.setMax(max);
      progressDialog.setProgress(Math.min(number, max));
    }
  }

  /**
   * To be invoked after showing the result dialog.
   */
  private void onPostResultDialog() {
    String nogagoPackage = "com.nogago.android.maps";
    String mapActivity = ".activities.MapActivity";
    // Alert if nogago Maps is not installed
    AlertDialog.Builder notInstalled = new AlertDialog.Builder(this);
    notInstalled.setMessage(R.string.maps_not_installed).setCancelable(false)
        .setPositiveButton(R.string.button_yes, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int id) {
            Uri uri = Uri.parse(Constants.MAPS_DOWNLOAD_URL);
            Intent showUri = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(showUri);
          }
        }).setNegativeButton(R.string.button_no, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int id) {
            dialog.cancel();
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.nogago.android.tracks", "com.nogago.android.tracks.TrackListActivity"));
            startActivity(intent);
          }
        });
    final AlertDialog alertnotInstalled = notInstalled.create();
    
    try {
      if (success && playTrack) {
        Intent intent = new Intent()
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK)
            .putExtra(GOOGLE_EARTH_TOUR_FEATURE_ID, KmlTrackWriter.TOUR_FEATURE_ID)
            .setClassName(GOOGLE_EARTH_PACKAGE, GOOGLE_EARTH_CLASS)
            .setDataAndType(Uri.fromFile(new File(savedPath)), GOOGLE_EARTH_KML_MIME_TYPE);
        startActivity(intent);
      } else if (success && showTrack) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setComponent(new ComponentName(nogagoPackage, nogagoPackage + mapActivity));
        intent.putExtra("from Tracks", true);
        startActivity(intent);
      } else if (success && followTrack) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setComponent(new ComponentName(nogagoPackage, nogagoPackage + mapActivity));
        intent.putExtra("follow", true);
        startActivity(intent);
      }
      finish();
    } catch (ActivityNotFoundException e) {
      alertnotInstalled.show();
    }
  }
}