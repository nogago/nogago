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
package com.nogago.android.apps.tracks.io;

import com.google.android.apps.mytracks.io.file.TrackWriter;
import com.google.android.apps.mytracks.io.file.TrackWriterFactory;
import com.google.android.apps.mytracks.io.file.TrackWriterFactory.TrackFileFormat;
import com.google.android.apps.mytracks.io.sendtogoogle.AbstractSendAsyncTask;
import com.google.android.apps.mytracks.util.Constants;
import com.google.android.apps.mytracks.util.PreferencesUtils;
import com.google.common.annotations.VisibleForTesting;
import com.nogago.android.apps.tracks.content.MyTracksProviderUtils;
import com.nogago.android.apps.tracks.content.Track;
import com.nogago.bb10.tracks.R;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;

/**
 * AsyncTask to send a track to Google Maps.
 * <p>
 * IMPORTANT: While this code is Apache-licensed, please notice that usage of
 * the Google Maps servers through this API is only allowed for the My Tracks
 * application. Other applications looking to upload maps data should look into
 * using the Google Fusion Tables API.
 * 
 * @author Jimmy Shih
 */
public class SendNogagoAsyncTask extends AbstractSendAsyncTask {

  private static final int PROGRESS_FETCH_MAP_ID = 5;
  @VisibleForTesting
  static final int PROGRESS_UPLOAD_DATA_MIN = 10;
  @VisibleForTesting
  static final int PROGRESS_UPLOAD_DATA_MAX = 90;
  private static final int PROGRESS_SERIALIZED = 30;
  private static final int PROGRESS_COMPLETE = 100;

  private static final String TAG = SendNogagoAsyncTask.class.getSimpleName();

  private final long trackId;
  private final String chooseMapId;
  private final MyTracksProviderUtils myTracksProviderUtils;
  private final Context context;

  private String mapId; // Hier kommt das hin...
  
  int currentSegment;

  public SendNogagoAsyncTask(
      SendNogagoActivity activity, long trackId, String chooseMapId) {
    this(activity, trackId, chooseMapId, MyTracksProviderUtils.Factory.get(
        activity.getApplicationContext()));
  }

  /**
   * This constructor is created for test.
   */
  @VisibleForTesting
  public SendNogagoAsyncTask(SendNogagoActivity activity, long trackId, 
      String chooseMapId, MyTracksProviderUtils myTracksProviderUtils) {
    super(activity);
    this.trackId = trackId;
    this.chooseMapId = chooseMapId;
    this.myTracksProviderUtils = myTracksProviderUtils;
    context = activity.getApplicationContext();
  }

  @Override
  protected void closeConnection() {
  }

  @Override
  protected void saveResult() {
    Track track = myTracksProviderUtils.getTrack(trackId);
    if (track == null) {
      Log.d(TAG, "No track for " + trackId);
      return;
    }
    track.setMapId(mapId);
    myTracksProviderUtils.updateTrack(track);
  }

  @Override
  protected boolean performTask() {

    // Get the track
    Track track = myTracksProviderUtils.getTrack(trackId);
    if (track == null) {
      Log.d(TAG, "No track for " + trackId);
      return false;
    }
    // Fetch the mapId, create a new map if necessary
    publishProgress(PROGRESS_FETCH_MAP_ID);
    
    // GPX Upload
    URL u = null;
    TrackWriter trackWriter = TrackWriterFactory.newWriter(context, myTracksProviderUtils, trackId,
        TrackFileFormat.GPX);
    String s =  trackWriter.writeTrackAsString();

    publishProgress(10);
    String trackName;
    try {
      trackName = URLEncoder.encode(myTracksProviderUtils.getTrack(trackId).getName(), "utf-8");
    } catch (UnsupportedEncodingException e) {
      Date date = new Date();
      SimpleDateFormat sdf = new SimpleDateFormat("-yyyy-MM-dd-HH-mm");
      trackName = "Track" + sdf.format(date);
    }

    try {
      // create the POST data
      StringBuffer data = new StringBuffer();
      data.append("username=").append(getUserName());
      data.append('&').append("password=").append(getPassword());
      data.append('&').append("trackName=").append(trackName);
      data.append('&').append("public=true"); //share_track=true war vereinbart
      data.append('&').append("content=");
      byte[] dataHeader = data.toString().getBytes("UTF-8");
      byte[] dataGPX = s.getBytes("UTF-8");
      byte[] postBytes = new byte[dataHeader.length + dataGPX.length];
      System.arraycopy(dataHeader, 0, postBytes, 0, dataHeader.length);
      System.arraycopy(dataGPX, 0, postBytes, dataHeader.length, dataGPX.length);

      publishProgress(PROGRESS_SERIALIZED );
      // prepare and execute the POST
      HttpClient client = new DefaultHttpClient();
      HttpPost post = new HttpPost(Constants.TRACKS_UPLOAD_URL);
      post.setHeader("Content-Type", "application/x-www-form-urlencoded");
      post.setEntity(new ByteArrayEntity(postBytes));
      HttpResponse response = client.execute(post, new BasicHttpContext());

      // read the status

      StatusLine statusLine = response.getStatusLine();
      int status = statusLine.getStatusCode();
      Log.i(TAG, statusLine.toString());
      // if status is not null
      ;
      if (status != 200) {
        switch (status) {
          case 400:
            Log.e(TAG, "Error 400: GPX wrong");
            break;
          case 401:
            Log.e(TAG, "Error 400: GPX wrong");
            break;
        }
        mapId=null;
        publishProgress(PROGRESS_COMPLETE);
        return false;
      } else {
        publishProgress(PROGRESS_UPLOAD_DATA_MAX);
        mapId=getUrl(response);
        publishProgress(PROGRESS_COMPLETE);
      }

    } catch (IOException e3) {
      Log.e(TAG, e3.getMessage(), e3);
      return false;
    } 
    publishProgress(PROGRESS_COMPLETE);
    return true;
  }
  

  private String getUserName() {
    return PreferencesUtils.getString(context, R.string.user_name, "");
  }

  private String getPassword() {
    return PreferencesUtils.getString(context, R.string.user_password, "");
  }


  /**
   * Read the response for the URL
   * @param response
   * @return
   */
  String getUrl(HttpResponse response) {
    BasicResponseHandler brh = new BasicResponseHandler();
    String s = null;
    try {
      s = brh.handleResponse(response);
    } catch (HttpResponseException e) {
      Log.e(TAG, "response to string broke", e);
    } catch (IOException e) {
      Log.e(TAG, "response to string broke", e);
    }
    return s;
  }

  /**
   * Updates the progress based on the number of locations uploaded.
   * 
   * @param uploaded the number of uploaded locations
   * @param total the number of total locations
   */
  @VisibleForTesting
  void updateProgress(int uploaded, int total) {
    publishProgress(getPercentage(uploaded, total));
  }

  /**
   * Count the percentage of the number of locations uploaded.
   * 
   * @param uploaded the number of uploaded locations
   * @param total the number of total locations
   */
  @VisibleForTesting
  static int getPercentage(int uploaded, int total) {
    double totalPercentage = (double) uploaded / total;
    double scaledPercentage = totalPercentage
        * (PROGRESS_UPLOAD_DATA_MAX - PROGRESS_UPLOAD_DATA_MIN) + PROGRESS_UPLOAD_DATA_MIN;
    return (int) scaledPercentage;
  }

  /**
   * Gets the mapID.
   * 
   * @return mapId
   */
  @VisibleForTesting
  String getMapId() {
    return mapId;
  }

  @Override
  protected void invalidateToken() {
    // TODO Auto-generated method stub
    
  }

}