/*******************************************************************************
 * Copyright (c) MOBAC developers
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
/*
 * add to mobac.program.model.AtlasOutputFormat.java
 *
 * import mobac.program.atlascreators.TwoNavRmap;
 * TwoNavRMAP("TwoNav (RMAP)", TwoNavRmap.class), //
 *
 */
package mobac.program.atlascreators;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import javax.imageio.ImageIO;

import mobac.exceptions.AtlasTestException;
import mobac.exceptions.MapCreationException;
import mobac.mapsources.mapspace.MercatorPower2MapSpace;
import mobac.program.annotations.AtlasCreatorName;
import mobac.program.annotations.SupportedParameters;
import mobac.program.atlascreators.tileprovider.ConvertedRawTileProvider;
import mobac.program.interfaces.LayerInterface;
import mobac.program.interfaces.MapInterface;
import mobac.program.interfaces.MapSource;
import mobac.program.interfaces.MapSpace;
import mobac.program.interfaces.TileImageDataWriter;
import mobac.program.interfaces.MapSpace.ProjectionCategory;
import mobac.program.model.TileImageFormat;
import mobac.program.model.TileImageType;
import mobac.program.model.TileImageParameters.Name;
import mobac.program.tiledatawriter.TileImageJpegDataWriter;
import mobac.utilities.Utilities;

import org.apache.log4j.Level;

/**
 * 
 * Creates one RMAP file per layer.
 * 
 * @author Luka Logar
 * @author r_x
 * 
 */
@AtlasCreatorName(value = "TwoNav (RMAP)")
@SupportedParameters(names = { Name.format })
public class TwoNavRMAP extends AtlasCreator {

	private RmapFile rmapFile = null;

	private class ZoomLevel {

		private int index = 0;
		private long offset = 0;
		private int width = 0;
		private int height = 0;
		private int xTiles = 0;
		private int yTiles = 0;
		private long jpegOffsets[][] = null;
		private int zoom = 0;
		private boolean dl = false;

		private void writeHeader() throws IOException {
			if (offset == 0) {
				offset = rmapFile.getFilePointer();
			} else {
				rmapFile.seek(offset);
			}
			log.trace(String.format("Writing ZoomLevel %d (%dx%d pixels, %dx%d tiles) header at offset %d", index,
					width, height, xTiles, yTiles, offset));
			rmapFile.writeIntI(width);
			rmapFile.writeIntI(-height);
			rmapFile.writeIntI(xTiles);
			rmapFile.writeIntI(yTiles);
			if (jpegOffsets == null) {
				jpegOffsets = new long[xTiles][yTiles];
			}
			for (int y = 0; y < yTiles; y++) {
				for (int x = 0; x < xTiles; x++) {
					rmapFile.writeLongI(jpegOffsets[x][y]);
				}
			}
		}

		private BufferedImage loadJpegAtOffset(long offset) throws IOException {
			if (offset == 0) {
				throw new IOException("offset == 0");
			}
			rmapFile.seek(offset);
			int TagId = rmapFile.readIntI();
			if (TagId != 7) {
				throw new IOException("TagId != 7");
			}
			int TagLen = rmapFile.readIntI();
			byte[] jpegImageBuf = new byte[TagLen];
			rmapFile.readFully(jpegImageBuf);
			ByteArrayInputStream input = new ByteArrayInputStream(jpegImageBuf);
			return ImageIO.read(input);
		}

		private byte[] getTileData(ZoomLevel source, int x, int y) throws IOException {
			log.trace(String.format("Shrinking jpegs (%d,%d,%d - %d,%d,%d)", source.index, x, y, source.index,
					(x + 1 < source.xTiles) ? x + 1 : x, (y + 1 < source.yTiles) ? y + 1 : y));
			BufferedImage bi11 = loadJpegAtOffset(source.jpegOffsets[x][y]);
			BufferedImage bi21 = (x + 1 < source.xTiles) ? loadJpegAtOffset(source.jpegOffsets[x + 1][y]) : null;
			BufferedImage bi12 = (y + 1 < source.yTiles) ? loadJpegAtOffset(source.jpegOffsets[x][y + 1]) : null;
			BufferedImage bi22 = (x + 1 < source.xTiles) && (y + 1 < source.yTiles) ? loadJpegAtOffset(source.jpegOffsets[x + 1][y + 1])
					: null;
			int biWidth = bi11.getWidth() + (bi21 != null ? bi21.getWidth() : 0);
			int biHeight = bi11.getHeight() + (bi12 != null ? bi12.getHeight() : 0);
			BufferedImage bi = new BufferedImage(biWidth, biHeight, BufferedImage.TYPE_3BYTE_BGR);
			Graphics2D g = bi.createGraphics();
			g.drawImage(bi11, 0, 0, null);
			if (bi21 != null) {
				g.drawImage(bi21, bi11.getWidth(), 0, null);
			}
			if (bi12 != null) {
				g.drawImage(bi12, 0, bi11.getHeight(), null);
			}
			if (bi22 != null) {
				g.drawImage(bi22, bi11.getWidth(), bi11.getHeight(), null);
			}
			AffineTransformOp op = new AffineTransformOp(new AffineTransform(0.5, 0, 0, 0.5, 0, 0),
					AffineTransformOp.TYPE_BILINEAR);
			BufferedImage biOut = new BufferedImage(biWidth / 2, biHeight / 2, BufferedImage.TYPE_3BYTE_BGR);
			op.filter(bi, biOut);
			ByteArrayOutputStream buffer = new ByteArrayOutputStream(biOut.getWidth() * biOut.getHeight() * 4);
			TileImageDataWriter writer = new TileImageJpegDataWriter(0.9);
			writer.initialize();
			writer.processImage(biOut, buffer);
			return buffer.toByteArray();
		}

		private void shrinkFrom(ZoomLevel source) {
			try {
				writeHeader();
				atlasProgress.initMapCreation(xTiles * yTiles);
				for (int x = 0; x < xTiles; x++) {
					for (int y = 0; y < yTiles; y++) {
						checkUserAbort();
						atlasProgress.incMapCreationProgress();
						jpegOffsets[x][y] = rmapFile.getFilePointer();
						byte[] tileData = getTileData(source, 2 * x, 2 * y);
						rmapFile.seek(jpegOffsets[x][y]);
						log.trace(String.format("Writing shrunk jpeg (%d,%d,%d) at offset %d", index, x, y,
								jpegOffsets[x][y]));
						rmapFile.writeIntI(7);
						rmapFile.writeIntI(tileData.length);
						rmapFile.write(tileData);
					}
				}
			} catch (Exception e) {
				log.error("Failed generating ZoomLevel " + index + ": " + e.getMessage());
			}
		}
	}

	private class RmapFile extends RandomAccessFile {

		private String name = "";
		private int width = 0;
		private int height = 0;
		private int tileWidth = 0;
		private int tileHeight = 0;
		private double longitudeMin = 0;
		private double longitudeMax = 0;
		private double latitudeMin = 0;
		private double latitudeMax = 0;
		private long mapDataOffset = 0;
		private ZoomLevel zoomLevels[] = null;

		private RmapFile(File file) throws FileNotFoundException {
			super(file, "rw");
			this.name = file.getName();
		}

		private int readIntI() throws IOException {
			int ch1 = this.read();
			int ch2 = this.read();
			int ch3 = this.read();
			int ch4 = this.read();
			if ((ch1 | ch2 | ch3 | ch4) < 0) {
				throw new IOException();
			}
			return ((ch4 << 24) + (ch3 << 16) + (ch2 << 8) + (ch1 << 0));
		}

		private void writeIntI(int i) throws IOException {
			write((i >>> 0) & 0xFF);
			write((i >>> 8) & 0xFF);
			write((i >>> 16) & 0xFF);
			write((i >>> 24) & 0xFF);
		}

		private void writeLongI(long l) throws IOException {
			write((int) (l >>> 0) & 0xFF);
			write((int) (l >>> 8) & 0xFF);
			write((int) (l >>> 16) & 0xFF);
			write((int) (l >>> 24) & 0xFF);
			write((int) (l >>> 32) & 0xFF);
			write((int) (l >>> 40) & 0xFF);
			write((int) (l >>> 48) & 0xFF);
			write((int) (l >>> 56) & 0xFF);
		}

		private void writeHeader() throws IOException {
			log.trace("Writing rmap header");
			if (zoomLevels == null) {
				throw new IOException("zoomLevels == null");
			}
			seek(0);
			write("CompeGPSRasterImage".getBytes());
			writeIntI(10);
			writeIntI(7);
			writeIntI(0);
			writeIntI(width);
			writeIntI(-height);
			writeIntI(24);
			writeIntI(1);
			writeIntI(tileWidth);
			writeIntI(tileHeight);
			writeLongI(mapDataOffset);
			writeIntI(0);
			writeIntI(zoomLevels.length);
			for (int n = 0; n < zoomLevels.length; n++) {
				writeLongI(zoomLevels[n].offset);
			}
		}

		private void writeMapInfo() throws IOException {
			if (mapDataOffset == 0) {
				mapDataOffset = getFilePointer();
			} else {
				seek(mapDataOffset);
			}
			log.trace("Writing MAP data at offset %d" + mapDataOffset);
			StringBuffer sbMap = new StringBuffer();
			sbMap.append("CompeGPS MAP File\r\n");
			sbMap.append("<Header>\r\n");
			sbMap.append("Version=2\r\n");
			sbMap.append("VerCompeGPS=MOBAC\r\n");
			sbMap.append("Projection=2,Mercator,\r\n");
			sbMap.append("Coordinates=1\r\n");
			sbMap.append("Datum=WGS 84\r\n");
			sbMap.append("</Header>\r\n");
			sbMap.append("<Map>\r\n");
			sbMap.append("Bitmap=" + name + "\r\n");
			sbMap.append("BitsPerPixel=0\r\n");
			sbMap.append(String.format("BitmapWidth=%d\r\n", width));
			sbMap.append(String.format("BitmapHeight=%d\r\n", height));
			sbMap.append("Type=10\r\n");
			sbMap.append("</Map>\r\n");
			sbMap.append("<Calibration>\r\n");
			String pointLine = "P%d=%d,%d,A,%s,%s\r\n";
			DecimalFormat df = (DecimalFormat) NumberFormat.getNumberInstance(Locale.US);
			df.applyPattern("#0.00000000");
			sbMap.append(String.format(pointLine, 0, 0, 0, df.format(longitudeMin), df.format(latitudeMax)));
			sbMap.append(String.format(pointLine, 1, width - 1, 0, df.format(longitudeMax), df.format(latitudeMax)));
			sbMap.append(String.format(pointLine, 2, width - 1, height - 1, df.format(longitudeMax), df
					.format(latitudeMin)));
			sbMap.append(String.format(pointLine, 3, 0, height - 1, df.format(longitudeMin), df.format(latitudeMin)));
			sbMap.append("</Calibration>\r\n");
			sbMap.append("<MainPolygonBitmap>\r\n");
			String polyLine = "M%d=%d,%d\r\n";
			sbMap.append(String.format(polyLine, 0, 0, 0));
			sbMap.append(String.format(polyLine, 1, width, 0));
			sbMap.append(String.format(polyLine, 2, width, height));
			sbMap.append(String.format(polyLine, 3, 0, height));
			sbMap.append("</MainPolygonBitmap>\r\n");
			writeIntI(1);
			writeIntI(sbMap.length());
			write(sbMap.toString().getBytes());
		}
	}

	// ************************************************************

	public TwoNavRMAP() {
		super();
		log.setLevel(Level.TRACE);
	}

	@Override
	protected void testAtlas() throws AtlasTestException {
		for (LayerInterface layer : atlas) {
			MapInterface map0 = layer.getMap(0);
			MapSpace mapSpace0 = map0.getMapSource().getMapSpace();
			double longitudeMin = mapSpace0.cXToLon(map0.getMinTileCoordinate().x, map0.getZoom());
			double longitudeMax = mapSpace0.cXToLon(map0.getMaxTileCoordinate().x + 1, map0.getZoom());
			double latitudeMin = mapSpace0.cYToLat(map0.getMaxTileCoordinate().y + 1, map0.getZoom());
			double latitudeMax = mapSpace0.cYToLat(map0.getMinTileCoordinate().y, map0.getZoom());
			for (int n = 1; n < layer.getMapCount(); n++) {
				MapInterface mapN = layer.getMap(n);
				MapSpace mapSpaceN = mapN.getMapSource().getMapSpace();

				double longitudeMinN = mapSpaceN.cXToLon(mapN.getMinTileCoordinate().x, mapN.getZoom());
				double longitudeMaxN = mapSpaceN.cXToLon(mapN.getMaxTileCoordinate().x + 1, mapN.getZoom());
				double latitudeMinN = mapSpaceN.cYToLat(mapN.getMaxTileCoordinate().y + 1, mapN.getZoom());
				double latitudeMaxN = mapSpaceN.cYToLat(mapN.getMinTileCoordinate().y, mapN.getZoom());
				if ((longitudeMin != longitudeMinN) || (longitudeMax != longitudeMaxN) || (latitudeMin != latitudeMinN)
						|| (latitudeMax != latitudeMaxN)) {
					throw new AtlasTestException("All maps in one layer have to cover the same area!\n"
							+ "Use grid zoom on the lowest zoom level to get an acceptable result.");
				}
				for (int m = 0; m < layer.getMapCount(); m++) {
					if ((mapN.getZoom() == layer.getMap(m).getZoom()) && (m != n)) {
						throw new AtlasTestException("Several maps with the same zoom level within the same layer "
								+ "are not supported!");
					}
				}
			}
		}
	}

	public boolean testMapSource(MapSource mapSource) {
		MapSpace mapSpace = mapSource.getMapSpace();
		return (mapSpace instanceof MercatorPower2MapSpace && ProjectionCategory.SPHERE.equals(mapSpace
				.getProjectionCategory()));
	}

	@Override
	public void initLayerCreation(LayerInterface layer) throws IOException {
		if (rmapFile != null)
			throw new RuntimeException("Layer mismatch - last layer has not been finished correctly!");

		super.initLayerCreation(layer);
		// Logging.configureConsoleLogging(org.apache.log4j.Level.ALL, new SimpleLayout());

		rmapFile = new RmapFile(new File(atlasDir, layer.getName() + ".rmap"));

		int DefaultMap = 0;

		rmapFile.width = 0;
		rmapFile.height = 0;
		for (int n = 0; n < layer.getMapCount(); n++) {
			int width = layer.getMap(n).getMaxTileCoordinate().x - layer.getMap(n).getMinTileCoordinate().x + 1;
			int height = layer.getMap(n).getMaxTileCoordinate().y - layer.getMap(n).getMinTileCoordinate().y + 1;
			if ((width > rmapFile.width) || (height > rmapFile.height)) {
				rmapFile.width = width;
				rmapFile.height = height;
				DefaultMap = n;
			}
		}

		log.trace("rmap width  = " + rmapFile.width);
		log.trace("rmap height = " + rmapFile.height);

		rmapFile.tileWidth = layer.getMap(DefaultMap).getTileSize().width;
		rmapFile.tileHeight = layer.getMap(DefaultMap).getTileSize().height;

		log.trace("rmap tileWidth  = " + rmapFile.tileWidth);
		log.trace("rmap tileHeight = " + rmapFile.tileHeight);

		MapSpace mapSpace = layer.getMap(DefaultMap).getMapSource().getMapSpace();
		rmapFile.longitudeMin = mapSpace.cXToLon(layer.getMap(DefaultMap).getMinTileCoordinate().x, layer.getMap(
				DefaultMap).getZoom());
		rmapFile.longitudeMax = mapSpace.cXToLon(layer.getMap(DefaultMap).getMaxTileCoordinate().x, layer.getMap(
				DefaultMap).getZoom());
		rmapFile.latitudeMin = mapSpace.cYToLat(layer.getMap(DefaultMap).getMaxTileCoordinate().y, layer.getMap(
				DefaultMap).getZoom());
		rmapFile.latitudeMax = mapSpace.cYToLat(layer.getMap(DefaultMap).getMinTileCoordinate().y, layer.getMap(
				DefaultMap).getZoom());

		log.trace("rmap longitudeMin = " + rmapFile.longitudeMin);
		log.trace("rmap longitudeMax = " + rmapFile.longitudeMax);
		log.trace("rmap latitudeMin = " + rmapFile.latitudeMin);
		log.trace("rmap latitudeMax = " + rmapFile.latitudeMax);

		double width = rmapFile.width;
		double height = rmapFile.height;
		int count = 1;
		while ((width >= 256.0) || (height >= 256.0)) {
			width = Math.ceil(width / 2.0);
			height = Math.ceil(height / 2.0);
			count++;
		}

		log.trace("rmap zoomLevels count = " + count);

		rmapFile.zoomLevels = new ZoomLevel[count];
		width = rmapFile.width;
		height = rmapFile.height;
		for (int n = 0; n < rmapFile.zoomLevels.length; n++) {
			rmapFile.zoomLevels[n] = new ZoomLevel();
			rmapFile.zoomLevels[n].index = n;
			rmapFile.zoomLevels[n].width = (int) Math.round(width);
			rmapFile.zoomLevels[n].height = (int) Math.round(height);
			rmapFile.zoomLevels[n].xTiles = (int) Math.ceil((double) rmapFile.zoomLevels[n].width
					/ (double) rmapFile.tileWidth);
			rmapFile.zoomLevels[n].yTiles = (int) Math.ceil((double) rmapFile.zoomLevels[n].height
					/ (double) rmapFile.tileHeight);
			rmapFile.zoomLevels[n].jpegOffsets = new long[rmapFile.zoomLevels[n].xTiles][rmapFile.zoomLevels[n].yTiles];
			rmapFile.zoomLevels[n].zoom = layer.getMap(DefaultMap).getZoom() - n;
			rmapFile.zoomLevels[n].dl = false;
			for (int m = 0; m < layer.getMapCount(); m++) {
				if ((rmapFile.zoomLevels[n].zoom == layer.getMap(m).getZoom())) {
					rmapFile.zoomLevels[n].dl = true;
				}
			}
			width = Math.ceil(width / 2.0);
			height = Math.ceil(height / 2.0);
		}

		for (int n = 0; n < rmapFile.zoomLevels.length; n++) {
			log.trace(String.format("zoomLevels[%d] zoom=%d %dx%d pixels, %dx%d tiles %s",
					rmapFile.zoomLevels[n].index, rmapFile.zoomLevels[n].zoom, rmapFile.zoomLevels[n].width,
					rmapFile.zoomLevels[n].height, rmapFile.zoomLevels[n].xTiles, rmapFile.zoomLevels[n].yTiles,
					rmapFile.zoomLevels[n].dl == false ? "calc" : "dl"));
		}

		rmapFile.writeHeader();

	}

	public void createMap() throws MapCreationException, InterruptedException {
		try {

			int index;
			for (index = 0; index < rmapFile.zoomLevels.length; index++) {
				if (rmapFile.zoomLevels[index].zoom == map.getZoom()) {
					break;
				}
			}
			if (index == rmapFile.zoomLevels.length) {
				throw new MapCreationException("Map not found in the zoomLevels list", map);
			}
			try {
				rmapFile.zoomLevels[index].writeHeader();
			} catch (IOException e) {
				throw new MapCreationException("rmapFile.zoomLevels[Index].writeHeader() failed: " + e.getMessage(),
						map, e);
			}

			int tilex = 0;
			int tiley = 0;

			atlasProgress.initMapCreation((xMax - xMin + 1) * (yMax - yMin + 1));

			if ((map.getMapSource().getTileImageType() != TileImageType.JPG) || (map.getParameters() != null)) {
				// Tiles have to be converted to jpeg format
				TileImageFormat imageFormat = TileImageFormat.JPEG90;
				if (map.getParameters() != null)
					imageFormat = map.getParameters().getFormat();
				mapDlTileProvider = new ConvertedRawTileProvider(mapDlTileProvider, imageFormat);
			}

			ImageIO.setUseCache(false);
			byte[] emptyTileData = Utilities.createEmptyTileData(mapSource);
			for (int x = xMin; x <= xMax; x++) {
				tiley = 0;
				for (int y = yMin; y <= yMax; y++) {
					checkUserAbort();
					atlasProgress.incMapCreationProgress();
					try {
						// Remember offset to tile
						rmapFile.zoomLevels[index].jpegOffsets[tilex][tiley] = rmapFile.getFilePointer();
						byte[] sourceTileData = mapDlTileProvider.getTileData(x, y);
						if (sourceTileData != null) {
							rmapFile.writeIntI(7);
							rmapFile.writeIntI(sourceTileData.length);
							rmapFile.write(sourceTileData);
						} else {
							log.trace(String.format("Tile x=%d y=%d not found in tile archive - creating default",
									tilex, tiley));
							rmapFile.writeIntI(7);
							rmapFile.writeIntI(emptyTileData.length);
							rmapFile.write(emptyTileData);
						}
					} catch (IOException e) {
						throw new MapCreationException("Error writing tile image: " + e.getMessage(), map, e);
					}
					tiley++;
				}
				tilex++;
			}

		} catch (MapCreationException e) {
			throw e;
		} catch (Exception e) {
			throw new MapCreationException(map, e);
		}
	}

	@Override
	public void abortAtlasCreation() throws IOException {
		try {
			rmapFile.setLength(0);
		} finally {
			Utilities.closeFile(rmapFile);
		}
		super.abortAtlasCreation();
	}

	@Override
	public void finishLayerCreation() throws IOException {
		try {
			for (int n = 0; n < rmapFile.zoomLevels.length; n++) {
				if (rmapFile.zoomLevels[n].offset == 0) {
					if (n == 0) {
						throw new IOException("Missing top level map");
					}
					rmapFile.zoomLevels[n].shrinkFrom(rmapFile.zoomLevels[n - 1]);
				}
			}
			rmapFile.writeMapInfo();
			rmapFile.writeHeader();
			for (int n = 0; n < rmapFile.zoomLevels.length; n++) {
				rmapFile.zoomLevels[n].writeHeader();
			}
			rmapFile.close();
		} catch (IOException e) {
			log.error("Failed writing rmap file \"" + rmapFile.name + "\": " + e.getMessage(), e);
			abortAtlasCreation();
			throw e;
		}
		rmapFile = null;
		super.finishLayerCreation();
	}

}
