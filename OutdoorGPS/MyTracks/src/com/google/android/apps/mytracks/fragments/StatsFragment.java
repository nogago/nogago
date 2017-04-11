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

package com.google.android.apps.mytracks.fragments;

import com.google.android.apps.mytracks.TrackDetailActivity;
import com.google.android.apps.mytracks.content.TrackDataHub;
import com.google.android.apps.mytracks.content.TrackDataListener;
import com.google.android.apps.mytracks.content.TrackDataType;
import com.google.android.apps.mytracks.signalstrength.SignalStrengthListener;
import com.google.android.apps.mytracks.signalstrength.SignalStrengthListener.SignalStrengthCallback;
import com.google.android.apps.mytracks.signalstrength.SignalStrengthListenerFactory;
import com.google.android.apps.mytracks.stats.TripStatistics;
import com.google.android.apps.mytracks.util.Constants;
import com.google.android.apps.mytracks.util.StatsUtils;
import com.nogago.android.apps.tracks.content.Track;
import com.nogago.android.apps.tracks.content.Waypoint;
import com.nogago.bb10.tracks.R;

import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.EnumSet;

/**
 * A fragment to display track statistics to the user.
 * 
 * @author Sandor Dornbush
 * @author Rodrigo Damazio
 */
public class StatsFragment extends Fragment implements TrackDataListener, SignalStrengthCallback {

  public static final String STATS_FRAGMENT_TAG = "statsFragment";

  private static final int ONE_SECOND = 1000;

  private SignalStrengthListenerFactory signalListenerFactory;
  private SignalStrengthListener signalListener;
  private TrackDataHub trackDataHub;
  private Handler handler;

  private Location lastLocation = null;
  private TripStatistics lastTripStatistics = null;

  // A runnable to update the total time field.
  private final Runnable updateTotalTime = new Runnable() {
    public void run() {
      if (isResumed() && isSelectedTrackRecording()) {
        if (!isSelectedTrackPaused() && lastTripStatistics != null) {
          StatsUtils.setTotalTimeValue(getActivity(), System.currentTimeMillis()
              - lastTripStatistics.getStopTime() + lastTripStatistics.getTotalTime());
        }
        handler.postDelayed(this, ONE_SECOND);
      }
    }
  };

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    signalListenerFactory = new SignalStrengthListenerFactory();
    signalListener = signalListenerFactory.create(getActivity(), this);
    signalListener.register();
    return inflater.inflate(R.layout.stats, container, false);
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    handler = new Handler();
    updateUi(getActivity());
  }

  @Override
  public void onResume() {
    super.onResume();
    if (signalListenerFactory == null)
      signalListenerFactory = new SignalStrengthListenerFactory();
    if (signalListener == null)
      signalListener = signalListenerFactory.create(getActivity(), this);
    signalListener.register();
    resumeTrackDataHub();
  }

  @Override
  public void onPause() {
    super.onPause();
    if (signalListenerFactory != null && signalListener != null)
      signalListener.unregister();
    pauseTrackDataHub();
    handler.removeCallbacks(updateTotalTime);
  }

  @Override
  public void onLocationStateChanged(LocationState state) {
    if (isResumed() && state != LocationState.GOOD_FIX) {
      getActivity().runOnUiThread(new Runnable() {
        @Override
        public void run() {
          if (isResumed()) {
            lastLocation = null;
            StatsUtils.setLocationValues(getActivity(), lastLocation, gsmSignal, true);
          }
        }
      });
    }
  }

  @Override
  public void onLocationChanged(final Location location) {
    if (isResumed()) {
      getActivity().runOnUiThread(new Runnable() {
        @Override
        public void run() {
          if (isResumed()) {
            if (isSelectedTrackRecording() && !isSelectedTrackPaused()) {
              lastLocation = location;
              StatsUtils.setLocationValues(getActivity(), location, gsmSignal, true);
            } else {
              if (lastLocation != null) {
                lastLocation = null;
                StatsUtils.setLocationValues(getActivity(), lastLocation, gsmSignal, true);
              }
            }
          }
        }
      });
    }
  }

  @Override
  public void onHeadingChanged(double heading) {
    // We don't care.
  }

  @Override
  public void onSelectedTrackChanged(Track track) {
    if (isResumed()) {
      handler.removeCallbacks(updateTotalTime);
      if (isSelectedTrackRecording()) {
        handler.post(updateTotalTime);
      }
    }
  }

  @Override
  public void onTrackUpdated(final Track track) {
    if (isResumed()) {
      getActivity().runOnUiThread(new Runnable() {
        @Override
        public void run() {
          if (isResumed()) {
            lastTripStatistics = track != null ? track.getTripStatistics() : null;
            updateUi(getActivity());
          }
        }
      });
    }
  }

  @Override
  public void clearTrackPoints() {
    // We don't care.
  }

  @Override
  public void onSampledInTrackPoint(Location location) {
    // We don't care.
  }

  @Override
  public void onSampledOutTrackPoint(Location location) {
    // We don't care.
  }

  @Override
  public void onSegmentSplit(Location location) {
    // We don't care.
  }

  @Override
  public void onNewTrackPointsDone() {
    // We don't care.
  }

  @Override
  public void clearWaypoints() {
    // We don't care.
  }

  @Override
  public void onNewWaypoint(Waypoint wpt) {
    // We don't care.
  }

  @Override
  public void onNewWaypointsDone() {
    // We don't care.
  }

  @Override
  public boolean onMetricUnitsChanged(final boolean metric) {
    if (isResumed()) {
      getActivity().runOnUiThread(new Runnable() {
        @Override
        public void run() {
          if (isResumed()) {
            updateUi(getActivity());
          }
        }
      });
    }
    return true;
  }

  @Override
  public boolean onReportSpeedChanged(final boolean speed) {
    if (isResumed()) {
      getActivity().runOnUiThread(new Runnable() {
        @Override
        public void run() {
          if (isResumed()) {
            updateUi(getActivity());
          }
        }
      });
    }
    return true;
  }

  @Override
  public boolean onMinRecordingDistanceChanged(int minRecordingDistance) {
    // We don't care.
    return false;
  }

  /**
   * Resumes the trackDataHub. Needs to be synchronized because trackDataHub can
   * be accessed by multiple threads.
   */
  private synchronized void resumeTrackDataHub() {
    trackDataHub = ((TrackDetailActivity) getActivity()).getTrackDataHub();
    trackDataHub.registerTrackDataListener(this, EnumSet.of(TrackDataType.SELECTED_TRACK,
        TrackDataType.TRACKS_TABLE, TrackDataType.LOCATION, TrackDataType.PREFERENCE));
  }

  /**
   * Pauses the trackDataHub. Needs to be synchronized because trackDataHub can
   * be accessed by multiple threads.
   */
  private synchronized void pauseTrackDataHub() {
    trackDataHub.unregisterTrackDataListener(this);
    trackDataHub = null;
  }

  /**
   * Returns true if the selected track is recording. Needs to be synchronized
   * because trackDataHub can be accessed by multiple threads.
   */
  private synchronized boolean isSelectedTrackRecording() {
    return trackDataHub != null && trackDataHub.isSelectedTrackRecording();
  }

  /**
   * Returns true if the selected track is paused. Needs to be synchronized
   * because trackDataHub can be accessed by multiple threads.
   */
  private synchronized boolean isSelectedTrackPaused() {
    return trackDataHub != null && trackDataHub.isSelectedTrackPaused();
  }

  /**
   * Updates the UI.
   */
  private void updateUi(FragmentActivity activity) {
    StatsUtils.setTripStatisticsValues(activity, lastTripStatistics);
    StatsUtils.setLocationValues(activity, lastLocation, gsmSignal, true);
  }

  SignalStrength signalStrength = null;
  int gsmBitErrorRate = -1;
  int gsmSignal = Constants.UNKNOWN_SIGNAL; // in dBM

  /**
   * <rssi>: 
0 -113 dBm or less 
1 -111 dBm 
2...30 -109... -53 dBm 
31 -51 dBm or greater 
99 not known or not detectable 
<ber> (in percent): 
0...7 as RXQUAL values in the table in TS 45.008 [20] subclause 8.2.4 
99 not known or not detectable
   */
  @Override
  public void onSignalStrengthSampled(SignalStrength signal) {
    if (signal.isGsm()) {
      signalStrength = signal;
      gsmBitErrorRate = signal.getGsmBitErrorRate();
      if (signal.getGsmSignalStrength() < 2 || signal.getGsmSignalStrength() > 30) {
        switch (signal.getGsmSignalStrength()) {
          case 0:
            gsmSignal = -113;
          case 1:
            gsmSignal = -111;
          case 31:
            gsmSignal = -51; // or better
          case 99:
            gsmSignal = Constants.UNKNOWN_SIGNAL;
        }
      } else {
        gsmSignal = -109 + (signal.getGsmSignalStrength() - 2) * 2;
      }
    }
  }

  @Override
  public void onDestroy() {
    // TODO Auto-generated method stub
    super.onDestroy();
    if (signalListenerFactory != null && signalListener != null)
      signalListener.unregister();

  }

  @Override
  public void onDetach() {
    // TODO Auto-generated method stub
    super.onDetach();
    if (signalListenerFactory != null && signalListener != null)
      signalListener.unregister();
  }

  @Override
  public void onStop() {
    // TODO Auto-generated method stub
    super.onStop();
    if (signalListenerFactory != null && signalListener != null)
      signalListener.unregister();
  }

  @Override
  public void onServiceStateChanged(ServiceState serviceState) {
    if (serviceState.getState() == serviceState.STATE_OUT_OF_SERVICE
        || serviceState.getState() == serviceState.STATE_POWER_OFF)
      gsmSignal = Constants.UNKNOWN_SIGNAL;

  }
}
