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

package com.google.android.apps.mytracks.io.sendtogoogle;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Send request states for sending a track to Google Maps, Google Fusion Tables,
 * and Google Docs.
 *
 * @author Jimmy Shih
 */
public class SendRequest implements Parcelable {

  public static final String SEND_REQUEST_KEY = "sendRequest";

  private long trackId = -1L;
  private String sharingAppPackageName = null;
  private String sharingAppClassName = null;
  private boolean sendNogago = false;
  private String mapId = null;
  private boolean nogagoSuccess = false;

  /**
   * Creates a new send request.
   *
   * @param trackId the track id
   */
  public SendRequest(long trackId) {
    this.trackId = trackId;
  }

  /**
   * Get the track id.
   */
  public long getTrackId() {
    return trackId;
  }

  /**
   * Gets the sharing app package name.
   */
  public String getSharingAppPackageName() {
    return sharingAppPackageName;
  }

  /**
   * Sets the sharing app package name.
   * 
   * @param sharingAppPackageName the sharing app package name
   */
  public void setSharingAppPackageName(String sharingAppPackageName) {
    this.sharingAppPackageName = sharingAppPackageName;
  }

  /**
   * Gets the sharing app class name.
   */
  public String getSharingAppClassName() {
    return sharingAppClassName;
  }

  /**
   * Sets the sharing app class name.
   * 
   * @param sharingAppClassName the sharing app class name
   */
  public void setSharingAppClassName(String sharingAppClassName) {
    this.sharingAppClassName = sharingAppClassName;
  }

  /**
   * True if the user has selected the send to Google Maps option.
   */
  public boolean isSendNogago() {
    return sendNogago;
  }

  /**
   * Sets the send to Google Maps option.
   *
   * @param sendMaps true if the user has selected the send to Google Maps
   *          option
   */
  public void setSendNogago(boolean sendMaps) {
    this.sendNogago = sendMaps;
  }

  /**
   * Gets the selected map id if the user has selected to send a track to an
   * existing Google Maps.
   */
  public String getMapId() {
    return mapId;
  }

  /**
   * Sets the map id.
   *
   * @param mapId the map id
   */
  public void setMapId(String mapId) {
    this.mapId = mapId;
  }

  /**
   * True if sending to Google Maps is success.
   */
  public boolean isNogagoSuccess() {
    return nogagoSuccess;
  }

  /**
   * Sets the Google Maps result.
   *
   * @param mapsSuccess true if sending to Google Maps is success
   */
  public void setNogagoSuccess(boolean mapsSuccess) {
    this.nogagoSuccess = mapsSuccess;
  }


  private SendRequest(Parcel in) {
    trackId = in.readLong();
    sharingAppPackageName = in.readString();
    sharingAppClassName = in.readString();
    sendNogago = in.readByte() == 1;
    mapId = in.readString();
    nogagoSuccess = in.readByte() == 1;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel out, int flags) {
    out.writeLong(trackId);
    out.writeString(sharingAppPackageName);
    out.writeString(sharingAppClassName);
    out.writeByte((byte) (sendNogago ? 1 : 0));
    out.writeString(mapId);
    out.writeByte((byte) (nogagoSuccess ? 1 : 0));
  }

  public static final Parcelable.Creator<SendRequest> CREATOR = new Parcelable.Creator<
      SendRequest>() {
    public SendRequest createFromParcel(Parcel in) {
      return new SendRequest(in);
    }

    public SendRequest[] newArray(int size) {
      return new SendRequest[size];
    }
  };
}
