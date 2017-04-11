package com.nogago.android.apps.tracks.content;

import android.location.Location;

/** Captures a location annotated with gsmSignalStrength */
public class LocationGsmSignal extends Location {
  
  public LocationGsmSignal(java.lang.String provider, int strength) {
    super(provider);
    gsmSignalStrength = strength;
  }
  
  public LocationGsmSignal(android.location.Location l, int strength) {
    super(l);
    gsmSignalStrength = strength;
    
  }
  public int gsmSignalStrength; // in dbM

  public int getGsmSignalStrength() {
    return gsmSignalStrength;
  }

  public void setGsmSignalStrength(int gsmSignalStrength) {
    this.gsmSignalStrength = gsmSignalStrength;
  }
}
