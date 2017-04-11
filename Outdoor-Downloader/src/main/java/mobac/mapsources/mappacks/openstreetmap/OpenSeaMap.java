package mobac.mapsources.mappacks.openstreetmap;

import java.awt.Color;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import mobac.exceptions.TileException;
import mobac.exceptions.UnrecoverableDownloadException;
import mobac.mapsources.AbstractHttpMapSource;
import mobac.mapsources.AbstractMultiLayerMapSource;
import mobac.program.Logging;
import mobac.program.interfaces.MapSource;
import mobac.program.interfaces.MapSourceTextAttribution;
import mobac.program.model.TileImageType;
import mobac.program.tilestore.TileStore;
import mobac.utilities.Utilities;

/**
 * http://openseamap.org/
 * @see OpenSeaMapLayer
 */
public class OpenSeaMap extends AbstractMultiLayerMapSource implements MapSourceTextAttribution {

	public static final String LAYER_OPENSEA = "http://tiles.openseamap.org/seamark/";

	public OpenSeaMap() {
		super("OpenSeaMap", TileImageType.PNG);
		mapSources = new MapSource[] { new Mapnik(), new OpenSeaMapLayer() };
		initializeValues();
	}

	public String getAttributionText() {
		return "© OpenStreetMap contributors, CC-BY-SA";
	}

	public String getAttributionLinkURL() {
		return "http://openstreetmap.org";
	}

	/**
	 * Not working correctly:
	 * 
	 * 1. The map is a "sparse map" (only tiles are present that have content - the other are missing) <br>
	 * 2. The map layer's background is not transparent!
	 */
	public static class OpenSeaMapLayer extends AbstractHttpMapSource {

		public OpenSeaMapLayer() {
			super("OpenSeaMapLayer", 11, 17, TileImageType.PNG, TileUpdate.LastModified);
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			return LAYER_OPENSEA + zoom + "/" + tilex + "/" + tiley + ".png";
		}

		@Override
		public byte[] getTileData(int zoom, int x, int y, LoadMethod loadMethod) throws IOException,
				InterruptedException, TileException {
			byte[] data = super.getTileData(zoom, x, y, loadMethod);
			if (data != null && data.length == 0) {
				log.info("loaded non-existing tile");
				return null;
			}
			return data;
		}

		@Override
		public BufferedImage getTileImage(int zoom, int x, int y, LoadMethod loadMethod) throws IOException,
				UnrecoverableDownloadException, InterruptedException {
			try {
				byte[] data = getTileData(zoom, x, y, loadMethod);
				if (data == null) {
					return null;
				}
				com.sixlegs.png.PngImage png = new com.sixlegs.png.PngImage();
				BufferedImage image = png.read(new ByteArrayInputStream(data), true);
				return image;
			} catch (FileNotFoundException e) {
				TileStore ts = TileStore.getInstance();
				ts.putTile(ts.createNewEmptyEntry(x, y, zoom), this);
			} catch (Exception e) {
				Logging.LOG.error("Unknown error in OpenSeaMap", e);
			}
			return null;
		}

		@Override
		public Color getBackgroundColor() {
			return Utilities.COLOR_TRANSPARENT;
		}

	}

	public static Image makeColorTransparent(Image im, final Color color) {
		ImageFilter filter = new RGBImageFilter() {
			// the color we are looking for... Alpha bits are set to opaque
			public int markerRGB = color.getRGB() | 0xFF000000;

			public final int filterRGB(int x, int y, int rgb) {
				if ((rgb | 0xFF000000) == markerRGB) {
					// Mark the alpha bits as zero - transparent
					return 0x00FFFFFF & rgb;
				} else {
					// nothing to do
					return rgb;
				}
			}
		};

		ImageProducer ip = new FilteredImageSource(im.getSource(), filter);
		return Toolkit.getDefaultToolkit().createImage(ip);
	}
}