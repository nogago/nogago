/*
 * Copyright 2010 Google Inc.
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

package com.google.android.apps.mytracks.signalstrength;

import static com.google.android.apps.mytracks.signalstrength.SignalStrengthConstants.TAG;

import com.nogago.bb10.tracks.R;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.telephony.CellLocation;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.List;

/**
 * A class to monitor the network signal strength. TODO: i18n
 * 
 * @author Sandor Dornbush
 */
public class SignalStrengthListenerEclair extends PhoneStateListener implements
    SignalStrengthListener {

  private SignalStrength signalStrength = null;

  public SignalStrengthListenerEclair(Context ctx, SignalStrengthCallback callback) {
    this.context = ctx;
    this.callback = callback;
    manager = null;
  }

  protected int getListenEvents() {
    return PhoneStateListener.LISTEN_SIGNAL_STRENGTHS;
  }

  @Override
  public void onServiceStateChanged(ServiceState serviceState) {
    Log.d(TAG, "TODO Service Change State");
    // Todo;
  }

  @Override
  public void onSignalStrengthsChanged(SignalStrength signalStrength) {
    Log.d(TAG, "Signal Strength Modern: " + signalStrength);
    this.signalStrength = signalStrength;
    callback.onSignalStrengthSampled(signalStrength);
  }

  /**
   * Gets a human readable description for the network type.
   * 
   * @param type The integer constant for the network type
   * @return A human readable description of the network type
   */
  protected String getTypeAsString(int type) {
    switch (type) {
      case TelephonyManager.NETWORK_TYPE_1xRTT:
        return "1xRTT";
      case TelephonyManager.NETWORK_TYPE_CDMA:
        return "CDMA";
      case TelephonyManager.NETWORK_TYPE_EDGE:
        return "EDGE";
      case TelephonyManager.NETWORK_TYPE_EVDO_0:
        return "EVDO 0";
      case TelephonyManager.NETWORK_TYPE_EVDO_A:
        return "EVDO A";
      case TelephonyManager.NETWORK_TYPE_GPRS:
        return "GPRS";
      case TelephonyManager.NETWORK_TYPE_HSDPA:
        return "HSDPA";
      case TelephonyManager.NETWORK_TYPE_HSPA:
        return "HSPA";
      case TelephonyManager.NETWORK_TYPE_HSUPA:
        return "HSUPA";
      case TelephonyManager.NETWORK_TYPE_UMTS:
        return "UMTS";
      case TelephonyManager.NETWORK_TYPE_UNKNOWN:
      default:
        return "UNKNOWN";
    }
  }

  /**
   * Gets the url for the waypoint icon for the current network type.
   * 
   * @param type The network type
   * @return A url to a image to use as the waypoint icon
   */
  protected String getIcon(int type) {
    switch (type) {
      case TelephonyManager.NETWORK_TYPE_1xRTT:
      case TelephonyManager.NETWORK_TYPE_CDMA:
      case TelephonyManager.NETWORK_TYPE_GPRS:
      case TelephonyManager.NETWORK_TYPE_EDGE:
        return "http://maps.google.com/mapfiles/ms/micons/green.png";
      case TelephonyManager.NETWORK_TYPE_EVDO_0:
      case TelephonyManager.NETWORK_TYPE_EVDO_A:
      case TelephonyManager.NETWORK_TYPE_HSDPA:
      case TelephonyManager.NETWORK_TYPE_HSPA:
      case TelephonyManager.NETWORK_TYPE_HSUPA:
      case TelephonyManager.NETWORK_TYPE_UMTS:
        return "http://maps.google.com/mapfiles/ms/micons/blue.png";
      case TelephonyManager.NETWORK_TYPE_UNKNOWN:
      default:
        return "http://maps.google.com/mapfiles/ms/micons/red.png";
    }
  }

  public String getStrengthAsString() {
    if (signalStrength == null) {
      return "Strength: " + getContext().getString(R.string.unknown) + "\n";
    }
    StringBuffer sb = new StringBuffer();
    if (signalStrength.isGsm()) {
      appendSignal(signalStrength.getGsmSignalStrength(), R.string.gsm_strength, sb);
      maybeAppendSignal(signalStrength.getGsmBitErrorRate(), R.string.error_rate, sb);
    } else {
      appendSignal(signalStrength.getCdmaDbm(), R.string.cdma_strength, sb);
      appendSignal(signalStrength.getCdmaEcio() / 10.0, R.string.ecio, sb);
      appendSignal(signalStrength.getEvdoDbm(), R.string.evdo_strength, sb);
      appendSignal(signalStrength.getEvdoEcio() / 10.0, R.string.ecio, sb);
      appendSignal(signalStrength.getEvdoSnr(), R.string.signal_to_noise_ratio, sb);
    }
    return sb.toString();
  }

  private void maybeAppendSignal(int signal, int signalFormat, StringBuffer sb) {
    if (signal > 0) {
      sb.append(getContext().getString(signalFormat, signal));
    }
  }

  private void appendSignal(int signal, int signalFormat, StringBuffer sb) {
    sb.append(getContext().getString(signalFormat, signal));
    sb.append("\n");
  }

  private void appendSignal(double signal, int signalFormat, StringBuffer sb) {
    sb.append(getContext().getString(signalFormat, signal));
    sb.append("\n");
  }

  private static final Uri APN_URI = Uri.parse("content://telephony/carriers");

  private final Context context;
  private final SignalStrengthCallback callback;

  private TelephonyManager manager;

  public void register() {
    if (manager == null) {
      manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
      if (manager == null) {
        Log.e(TAG, "Cannot get telephony manager.");
      } else {
        manager.listen(this, getListenEvents());
      }
    }
  }
  /**
   * Builds a description for the current signal strength.
   * 
   * @return A human readable description of the network state
   */
  private String getDescription() {
    StringBuilder sb = new StringBuilder();
    sb.append(getStrengthAsString());

    sb.append("Network Type: ");
    sb.append(getTypeAsString(manager.getNetworkType()));
    sb.append('\n');

    sb.append("Operator: ");
    sb.append(manager.getNetworkOperatorName());
    sb.append(" / ");
    sb.append(manager.getNetworkOperator());
    sb.append('\n');

    sb.append("Roaming: ");
    sb.append(manager.isNetworkRoaming());
    sb.append('\n');

    appendCurrentApns(sb);

    List<NeighboringCellInfo> infos = manager.getNeighboringCellInfo();
    Log.i(TAG, "Found " + infos.size() + " cells.");
    if (infos.size() > 0) {
      sb.append("Neighbors: ");
      for (NeighboringCellInfo info : infos) {
        sb.append(info.toString());
        sb.append(' ');
      }
      sb.append('\n');
    }

    CellLocation cell = manager.getCellLocation();
    if (cell != null) {
      sb.append("Cell: ");
      sb.append(cell.toString());
      sb.append('\n');
    }

    return sb.toString();
  }

  private void appendCurrentApns(StringBuilder output) {
    ContentResolver contentResolver = context.getContentResolver();

    Cursor cursor = contentResolver.query(APN_URI, new String[] { "name", "apn" }, "current=1",
        null, null);

    if (cursor == null) {
      return;
    }

    try {
      String name = null;
      String apn = null;

      while (cursor.moveToNext()) {
        int nameIdx = cursor.getColumnIndex("name");
        int apnIdx = cursor.getColumnIndex("apn");
        if (apnIdx < 0 || nameIdx < 0) {
          continue;
        }

        name = cursor.getString(nameIdx);
        apn = cursor.getString(apnIdx);
        output.append("APN: ");
        if (name != null) {
          output.append(name);
        }
        if (apn != null) {
          output.append(" (");
          output.append(apn);
          output.append(")\n");
        }
      }
    } finally {
      cursor.close();
    }
  }

  @Override
  public void unregister() {
    if (manager != null) {
      manager.listen(this, PhoneStateListener.LISTEN_NONE);
      manager = null;
    }
  }

  protected Context getContext() {
    return context;
  }

  public SignalStrengthCallback getSignalStrengthCallback() {
    return callback;
  }
}