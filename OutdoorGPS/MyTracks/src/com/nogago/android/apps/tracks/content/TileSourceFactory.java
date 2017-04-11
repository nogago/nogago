package com.nogago.android.apps.tracks.content;

import java.util.ArrayList;

import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;

public class TileSourceFactory {

  // private static final Logger logger =
  // LoggerFactory.getLogger(TileSourceFactory.class);

  /**
   * Get the tile source with the specified name.
   * 
   * @param aName the tile source name
   * @return the tile source
   * @throws IllegalArgumentException if tile source not found
   */
  public static ITileSource getTileSource(final String aName) throws IllegalArgumentException {
    for (final ITileSource tileSource : mTileSources) {
      if (tileSource.name().equals(aName)) {
        return tileSource;
      }
    }
    throw new IllegalArgumentException("No such tile source: " + aName);
  }

  public static boolean containsTileSource(final String aName) {
    for (final ITileSource tileSource : mTileSources) {
      if (tileSource.name().equals(aName)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Get the tile source at the specified position.
   * 
   * @param aOrdinal
   * @return the tile source
   * @throws IllegalArgumentException if tile source not found
   */
  public static ITileSource getTileSource(final int aOrdinal) throws IllegalArgumentException {
    for (final ITileSource tileSource : mTileSources) {
      if (tileSource.ordinal() == aOrdinal) {
        return tileSource;
      }
    }
    throw new IllegalArgumentException("No tile source at position: " + aOrdinal);
  }

  public static ArrayList<ITileSource> getTileSources() {
    return mTileSources;
  }

  public static void addTileSource(final ITileSource mTileSource) {
    mTileSources.add(mTileSource);
  }

  /*
   * Driving Cycling Public Transport // TODO Hiking OSMPublicTransport
   */

  public static final OnlineTileSourceBase SATELLITE = new AnnotatedXYTileSource(
      "SATELLITE",
      ResourceProxy.string.mapquest_aerial,
      0,
      11,
      256,
      ".png",
      "Tiles Courtesy of MapQuest Portions Courtesy NASA/JPL-Caltech and U.S. Depart. of Agriculture, Farm Service Agency",
      new String[] { "http://otile1.mqcdn.com/tiles/1.0.0/sat/",
          "http://otile2.mqcdn.com/tiles/1.0.0/sat/", "http://otile3.mqcdn.com/tiles/1.0.0/sat/",
          "http://otile4.mqcdn.com/tiles/1.0.0/sat/" });

  public static final OnlineTileSourceBase DRIVING = new AnnotatedXYTileSource("DRIVING",
      ResourceProxy.string.mapquest_osm, 0, 18, 256, ".png",
      "Tiles Courtesy of MapQuest Data © OpenStreetMap contributors under OdBL License",
      new String[] { "http://otile1.mqcdn.com/tiles/1.0.0/map/",
          "http://otile2.mqcdn.com/tiles/1.0.0/map/", "http://otile3.mqcdn.com/tiles/1.0.0/map/",
          "http://otile4.mqcdn.com/tiles/1.0.0/map/" });

  public static final OnlineTileSourceBase CYCLING = new AnnotatedXYTileSource(
      "CYCLING",
      ResourceProxy.string.cyclemap,
      0,
      18,
      256,
      ".png",
      "Tiles Courtesy of Thunderforest.com under CC-BY-SA, Data © OpenStreetMap contributors under OdBL",
      new String[] { "http://a.tile.opencyclemap.org/cycle/",
          "http://b.tile.opencyclemap.org/cycle/", "http://c.tile.opencyclemap.org/cycle/" });

  public static final OnlineTileSourceBase PUBLIC_TRANSPORT = new AnnotatedXYTileSource(
      "PUBLIC_TRANSPORT",
      ResourceProxy.string.public_transport,
      0,
      18,
      256,
      ".png",
      "Tiles Courtesy of Thunderforest.com under CC-BY-SA, Data © OpenStreetMap contributors under OdBL",
      new String[] { "a.tile.thunderforest.com/transport/", "b.tile.thunderforest.com/transport/",
          "c.tile.thunderforest.com/transport/" }); // "Maps © Thunderforest, Data © OpenStreetMap contributors";

  public static final OnlineTileSourceBase HIKING = new AnnotatedXYTileSource(
      "HIKING",
      ResourceProxy.string.mapnik,
      0,
      18,
      256,
      ".png",
      "Tiles Courtesy of Thunderforest.com under CC-BY-SA, Data © OpenStreetMap contributors under OdBL",
      new String[] { "http://a.tile.thunderforest.com/outdoors/",
          "http://b.tile.thunderforest.com/outdoors/", "http://c.tile.thunderforest.com/outdoors/" }); // "Maps © Thunderforest, Data © OpenStreetMap contributors";

  public static final OnlineTileSourceBase BLACKNWHITE = new AnnotatedXYTileSource("BLACKNWHITE",
      ResourceProxy.string.mapnik, 0, 20, 256, ".png",
      "Tiles Courtesy of Stamen under CC-BY 3.0, Data © OpenStreetMap contributors under OdBL",
      new String[] { "http://a.tile.stamen.com/toner/", "http://b.tile.stamen.com/toner/",
          "http://c.tile.stamen.com/toner/", "http://d.tile.stamen.com/toner/" }); // Map
                                                                                   // tiles
                                                                                   // by
                                                                                   // Stamen
                                                                                   // Design,
                                                                                   // under
                                                                                   // CC
                                                                                   // BY
                                                                                   // 3.0.
                                                                                   // Data
                                                                                   // by
                                                                                   // OpenStreetMap,
                                                                                   // under
                                                                                   // CC
                                                                                   // BY
                                                                                   // SA
  public static final OnlineTileSourceBase CLOUDS = new AnnotatedXYTileSource("CLOUDS",
      ResourceProxy.string.mapnik, 0, 10, 256, ".png", "Data by OpenWeatherMap", new String[] {
          "http://a.tile.openweathermap.org/map/clouds/",
          "http://b.tile.openweathermap.org/map/clouds/" }); // Data by
                                                             // OpenWeatherMap
  public static final OnlineTileSourceBase PRECIPATION = new AnnotatedXYTileSource("PRECIPATION",
      ResourceProxy.string.mapnik, 0, 15, 256, ".png", "Data by OpenWeatherMap", new String[] {
          "http://a.tile.openweathermap.org/map/precipitation/",
          "http://b.tile.openweathermap.org/map/precipitation/" }); // Data by
                                                                    // OpenWeatherMap
  public static final OnlineTileSourceBase RAIN = new AnnotatedXYTileSource("RAIN",
      ResourceProxy.string.mapnik, 0, 15, 256, ".png", "Data by OpenWeatherMap", new String[] {
          "http://a.tile.openweathermap.org/map/rain/",
          "http://b.tile.openweathermap.org/map/rain/" }); // Data by
                                                           // OpenWeatherMappublic
                                                           // static final

  public static final OnlineTileSourceBase WIND = new AnnotatedXYTileSource("WIND",
      ResourceProxy.string.mapnik, 0, 15, 256, ".png", "Data by OpenWeatherMap", new String[] {
          "http://a.tile.openweathermap.org/map/wind/",
          "http://b.tile.openweathermap.org/map/wind/" }); // Data by
                                                           // OpenWeatherMap
  public static final OnlineTileSourceBase PRESSURE = new AnnotatedXYTileSource("PRESSURE",
      ResourceProxy.string.mapnik, 0, 15, 256, ".png", "Data by OpenWeatherMap", new String[] {
          "http://a.tile.openweathermap.org/map/pressure/",
          "http://b.tile.openweathermap.org/map/pressure/" }); // Data by
                                                               // OpenWeatherMap

  public static final OnlineTileSourceBase TEMPERATURE = new AnnotatedXYTileSource("TEMPERATURE",
      ResourceProxy.string.mapnik, 0, 15, 256, ".png", "Data by OpenWeatherMap", new String[] {
          "http://a.tile.openweathermap.org/map/temp/",
          "http://b.tile.openweathermap.org/map/temp/" }); // Data by
                                                           // OpenWeatherMap

  public static final OnlineTileSourceBase DEFAULT_TILE_SOURCE = DRIVING;

  private static ArrayList<ITileSource> mTileSources;
  static {
    mTileSources = new ArrayList<ITileSource>();
    mTileSources.add(SATELLITE);
    mTileSources.add(DRIVING);
    mTileSources.add(PUBLIC_TRANSPORT);
    mTileSources.add(CYCLING);
    mTileSources.add(HIKING);
    mTileSources.add(BLACKNWHITE);
    mTileSources.add(CLOUDS);
    mTileSources.add(PRECIPATION);
    mTileSources.add(RAIN);
    mTileSources.add(PRESSURE);
    mTileSources.add(WIND);
    mTileSources.add(TEMPERATURE);

  }
}