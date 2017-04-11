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
package com.google.android.apps.mytracks.io.file;

import com.google.android.apps.mytracks.content.DescriptionGeneratorImpl;
import com.google.android.apps.mytracks.io.file.TrackWriterFactory.TrackFileFormat;
import com.google.android.apps.mytracks.util.Constants;
import com.google.android.apps.mytracks.util.StringUtils;
import com.nogago.android.apps.tracks.content.MyTracksLocation;
import com.nogago.android.apps.tracks.content.Track;
import com.nogago.android.apps.tracks.content.Waypoint;
import com.nogago.bb10.tracks.R;

import android.content.Context;
import android.location.Location;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Write track as GPX to a file.
 * 
 * @author Sandor Dornbush
 */
public class GpxTrackWriter implements TrackFormatWriter {

  private static final NumberFormat ELEVATION_FORMAT = NumberFormat.getInstance(Locale.US);
  private static final NumberFormat COORDINATE_FORMAT = NumberFormat.getInstance(Locale.US);
  static {
    // GPX readers expect to see fractional numbers with US-style punctuation.
    // That is, they want periods for decimal points, rather than commas.
    ELEVATION_FORMAT.setMaximumFractionDigits(1);
    ELEVATION_FORMAT.setGroupingUsed(false);

    COORDINATE_FORMAT.setMaximumFractionDigits(6);
    COORDINATE_FORMAT.setMaximumIntegerDigits(3);
    COORDINATE_FORMAT.setGroupingUsed(false);
  }

  private final Context context;
  private Track track;
  private PrintWriter printWriter;
  private boolean extensiveMode; // Write a lot

  public GpxTrackWriter(Context context, boolean mode) {
    this.context = context;
    this.extensiveMode = mode;
  }

  @Override
  public String getExtension() {
    return TrackFileFormat.GPX.getExtension();
  }

  @Override
  public void prepare(Track aTrack, OutputStream outputStream) {
    this.track = aTrack;
    this.printWriter = new PrintWriter(outputStream);
  }

  @Override
  public void close() {
    if (printWriter != null) {
      printWriter.close();
      printWriter = null;
    }
  }

  @Override
  public void writeHeader() {
    if (printWriter != null) {
      printWriter.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
      printWriter.println("<gpx version=\"1.1\"");
      if (extensiveMode) {
        printWriter.println("creator=\""
            + context.getString(R.string.n_send_google_by_my_tracks, "", "") + "\"");
      }
      printWriter.println("xmlns=\"http://www.topografix.com/GPX/1/1\"");
      printWriter
          .println("xmlns:topografix=\"http://www.topografix.com/GPX/Private/TopoGrafix/0/1\"");
      printWriter.println("xmlns:n=\"http://www.nogago.com/tracks\"");
      printWriter.println("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
      printWriter.println("xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1"
          + " http://www.topografix.com/GPX/1/1/gpx.xsd"
          + " http://www.topografix.com/GPX/Private/TopoGrafix/0/1"
          + " http://www.topografix.com/GPX/Private/TopoGrafix/0/1/topografix.xsd\">");
      if (extensiveMode) {
        printWriter.println("<metadata>");
        printWriter.println("<name>" + StringUtils.formatCData(track.getName()) + "</name>");
        printWriter.println("<desc>" + StringUtils.formatCData(track.getDescription()) + "</desc>");
        printWriter.println("<extensions>");
        // Extensions go here
        printWriter.println(new DescriptionGeneratorImpl(context)
            .generateXMLTripStatisticsDescription(track.getTripStatistics()));
        printWriter.println("</extensions>");
        printWriter.println("</metadata>");
      }
    }
  }

  @Override
  public void writeFooter() {
    if (printWriter != null) {
      printWriter.println("</gpx>");
    }
  }

  @Override
  public void writeBeginTrack(Location firstLocation) {
    if (printWriter != null) {
      printWriter.println("<trk>");
      if (extensiveMode) {
        printWriter.println("<name>" + StringUtils.formatCData(track.getName()) + "</name>");
        printWriter.println("<desc>" + StringUtils.formatCData(track.getDescription()) + "</desc>");
        printWriter.println("<type>" + StringUtils.formatCData(track.getCategory()) + "</type>");
        printWriter.println("<extensions><topografix:color>c0c0c0</topografix:color></extensions>");
      }
    }
  }

  @Override
  public void writeEndTrack(Location lastLocation) {
    if (printWriter != null) {
      printWriter.println("</trk>");
    }
  }

  @Override
  public void writeOpenSegment() {
    printWriter.println("<trkseg>");
  }

  @Override
  public void writeCloseSegment() {
    printWriter.println("</trkseg>");
  }

  @Override
  public void writeLocation(Location location) {
    if (printWriter != null) {
      printWriter.println("<trkpt " + formatLocation(location) + ">");
      if (extensiveMode) {
        printWriter.println("<ele>" + ELEVATION_FORMAT.format(location.getAltitude()) + "</ele>");
      }
      printWriter.println("<time>" + StringUtils.formatDateTimeIso8601(location.getTime())
          + "</time>");

      if (extensiveMode && !Constants.IS_BLACKBERRY && (location instanceof MyTracksLocation)) {
        printWriter.println("<extensions><n:gs>"
            + ((MyTracksLocation) location).getGsmSignalStrength() + "</n:gs></extensions>");
      }
      printWriter.println("</trkpt>");
    }
  }

  @Override
  public void writeBeginWaypoints() {
    // Do nothing
  }

  @Override
  public void writeEndWaypoints() {
    // Do nothing
  }

  @Override
  public void writeWaypoint(Waypoint waypoint) {
    if (printWriter != null) {
      Location location = waypoint.getLocation();
      if (location != null) {
        printWriter.println("<wpt " + formatLocation(location) + ">");
        printWriter.println("<ele>" + ELEVATION_FORMAT.format(location.getAltitude()) + "</ele>");
        printWriter.println("<time>" + StringUtils.formatDateTimeIso8601(location.getTime())
            + "</time>");
        printWriter.println("<name>" + StringUtils.formatCData(waypoint.getName()) + "</name>");
        printWriter.println("<desc>" + StringUtils.formatCData(waypoint.getDescription())
            + "</desc>");
        printWriter.println("</wpt>");
      }
    }
  }

  /**
   * Formats a location with latitude and longitude coordinates.
   * 
   * @param location the location
   */
  private String formatLocation(Location location) {
    return "lat=\"" + COORDINATE_FORMAT.format(location.getLatitude()) + "\" lon=\""
        + COORDINATE_FORMAT.format(location.getLongitude()) + "\"";
  }
}
