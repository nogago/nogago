package com.nogago.android.apps.tracks.content;
import org.osmdroid.ResourceProxy.string;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
public class AnnotatedXYTileSource extends XYTileSource {


private final String annotation;

  public AnnotatedXYTileSource(final String aName, final string aResourceId, final int aZoomMinLevel,
      final int aZoomMaxLevel, final int aTileSizePixels, final String aImageFilenameEnding,final String annotation,
      final String... aBaseUrl) {
      super(aName, aResourceId, aZoomMinLevel, aZoomMaxLevel, aTileSizePixels,
      aImageFilenameEnding, aBaseUrl);
      this.annotation = annotation;
      }

  public String getAnnotation() {
    return annotation;
  }
  
  
}
