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
package com.nogago.android.apps.tracks.content;

import com.nogago.android.apps.tracks.content.Sensor.SensorDataSet;

import android.location.Location;

/**
 * This class extends the standard Android location with extra information.
 *
 * @author Sandor Dornbush
 */
public class MyTracksLocation extends Location {

  private SensorDataSet sensorDataSet = null;

  /**
   * The id of this location from the provider.
   */
  private int id = -1;

  public MyTracksLocation(Location location, Sensor.SensorDataSet sd, int gsmSignal) {
    super(location);
    this.sensorDataSet = sd;
    this.gsmSignalStrength = gsmSignal;
  }

  public MyTracksLocation(String provider) {
    super(provider);
  }

  public SensorDataSet getSensorDataSet() {
    return sensorDataSet;
  }

  public void setSensorDataSet(SensorDataSet sensorDataSet) {
    this.sensorDataSet = sensorDataSet;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }
  
  public void reset() {
    super.reset();
    sensorDataSet = null;
    id = -1;
    gsmSignalStrength = -114; // UNKNOWN_SIGNAL
  }
  
  public int gsmSignalStrength; // in dbM

  public int getGsmSignalStrength() {
    return gsmSignalStrength;
  }

  public void setGsmSignalStrength(int gsmSignalStrength) {
    this.gsmSignalStrength = gsmSignalStrength;
  }
}
