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
package mobac.program.atlascreators;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.text.NumberFormat;

import javax.imageio.ImageIO;

import mobac.exceptions.AtlasTestException;
import mobac.exceptions.MapCreationException;
import mobac.mapsources.mapspace.MercatorPower2MapSpace;
import mobac.program.annotations.AtlasCreatorName;
import mobac.program.interfaces.LayerInterface;
import mobac.program.interfaces.MapInterface;
import mobac.program.interfaces.MapSource;
import mobac.program.interfaces.MapSpace;
import mobac.program.interfaces.MapSpace.ProjectionCategory;
import mobac.program.interfaces.TileImageDataWriter;
import mobac.program.tiledatawriter.TileImageJpegDataWriter;
import mobac.utilities.Charsets;
import mobac.utilities.Utilities;

/**
 * 
 * https://sourceforge.net/tracker/?func=detail&aid=3489104&group_id=238075&atid=1105497
 * http://create.tomtom.com/manuals/create-your-own-content/index.html?map_overlays.htm
 * 
 * @author r_x
 */
@AtlasCreatorName(value = "TomTom Raster (image + SAT)")
public class TomTomRaster extends AtlasCreator {

	protected File layerDir;

	@Override
	public boolean testMapSource(MapSource mapSource) {
		MapSpace mapSpace = mapSource.getMapSpace();
		return (mapSpace instanceof MercatorPower2MapSpace && ProjectionCategory.SPHERE.equals(mapSpace
				.getProjectionCategory()));
	}

	@Override
	protected void testAtlas() throws AtlasTestException {
		Runtime r = Runtime.getRuntime();
		long heapMaxSize = r.maxMemory();
		int maxMapSize = (int) (Math.sqrt(heapMaxSize / 3d) * 0.8); // reduce maximum by 20%
		maxMapSize = (maxMapSize / 100) * 100; // round by 100;
		for (LayerInterface layer : atlas) {
			for (MapInterface map : layer) {
				int w = map.getMaxTileCoordinate().x - map.getMinTileCoordinate().x;
				int h = map.getMaxTileCoordinate().y - map.getMinTileCoordinate().y;
				if (w > maxMapSize || h > maxMapSize)
					throw new AtlasTestException("Map size too large for memory (is: " + Math.max(w, h) + " max:  "
							+ maxMapSize + ")", map);
			}
		}

	}

	@Override
	public void initLayerCreation(LayerInterface layer) throws IOException {
		super.initLayerCreation(layer);
		layerDir = new File(atlasDir, layer.getName());
		Utilities.mkDirs(layerDir);
	}

	@Override
	public void createMap() throws MapCreationException, InterruptedException {
		try {
			createImage();
		} catch (InterruptedException e) {
			throw e;
		} catch (MapCreationException e) {
			throw e;
		} catch (Exception e) {
			throw new MapCreationException(map, e);
		}
	}

	protected void createImage() throws InterruptedException, MapCreationException {

		atlasProgress.initMapCreation((xMax - xMin + 1) * (yMax - yMin + 1));
		ImageIO.setUseCache(false);

		int mapWidth = (xMax - xMin + 1) * tileSize;
		int mapHeight = (yMax - yMin + 1) * tileSize;

		int maxImageSize = getMaxImageSize();
		int imageWidth = Math.min(maxImageSize, mapWidth);
		int imageHeight = Math.min(maxImageSize, mapHeight);

		int len = Math.max(mapWidth, mapHeight);
		double scaleFactor = 1.0;
		boolean scaleImage = (len > maxImageSize);
		if (scaleImage) {
			scaleFactor = (double) getMaxImageSize() / (double) len;
			if (mapWidth != mapHeight) {
				// Map is not rectangle -> adapt height or width
				if (mapWidth > mapHeight)
					imageHeight = (int) (scaleFactor * mapHeight);
				else
					imageWidth = (int) (scaleFactor * mapWidth);
			}
		}
		if (imageHeight < 0 || imageWidth < 0)
			throw new MapCreationException("Invalid map size: (width/height: " + imageWidth + "/" + imageHeight + ")",
					map);
		long imageSize = 3l * ((long) imageWidth) * ((long) imageHeight);
		if (imageSize > Integer.MAX_VALUE)
			throw new MapCreationException("Map image too large: (width/height: " + imageWidth + "/" + imageHeight
					+ ") - reduce the map size and try again", map);
		BufferedImage tileImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_3BYTE_BGR);
		Graphics2D graphics = tileImage.createGraphics();
		try {
			if (scaleImage) {
				graphics.setTransform(AffineTransform.getScaleInstance(scaleFactor, scaleFactor));
				graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			}
			int lineY = 0;
			for (int y = yMin; y <= yMax; y++) {
				int lineX = 0;
				for (int x = xMin; x <= xMax; x++) {
					checkUserAbort();
					atlasProgress.incMapCreationProgress();
					try {
						byte[] sourceTileData = mapDlTileProvider.getTileData(x, y);
						if (sourceTileData != null) {
							BufferedImage tile = ImageIO.read(new ByteArrayInputStream(sourceTileData));
							graphics.drawImage(tile, lineX, lineY, Color.WHITE, null);
						}
					} catch (IOException e) {
						log.error("", e);
					}
					lineX += tileSize;
				}
				lineY += tileSize;
			}
		} finally {
			graphics.dispose();
		}
		writeTileImage(tileImage);
	}

	protected void writeTileImage(BufferedImage tileImage) throws MapCreationException {
		TileImageDataWriter writer;
		if (parameters != null) {
			writer = parameters.getFormat().getDataWriter();
		} else
			writer = new TileImageJpegDataWriter(0.9);
		writer.initialize();
		try {
			int initialBufferSize = tileImage.getWidth() * tileImage.getHeight() / 4;
			ByteArrayOutputStream buf = new ByteArrayOutputStream(initialBufferSize);
			writer.processImage(tileImage, buf);
			String imageFileName = map.getName() + "." + writer.getType();
			File imageFile = new File(layerDir, imageFileName);
			FileOutputStream fout = new FileOutputStream(imageFile);
			try {
				fout.write(buf.toByteArray());
				fout.flush();
			} finally {
				fout.close();
			}
			writeSatFile(imageFileName, tileImage.getWidth(), tileImage.getHeight());
		} catch (Exception e) {
			throw new MapCreationException(map, e);
		}
	}

	/**
	 * SAT file content
	 * 
	 * <pre>
	 * Line 1 - filename of the image file.
	 * Line 2 - GPS coordinate of the top left corner of the image (longitude).
	 * Line 3 - GPS coordinate of the top left corner of the image (latitude).
	 * Line 4 - GPS coordinate of the bottom right corner of the image (longitude).
	 * Line 5 - GPS coordinate of the bottom right corner of the image (latitude).
	 * Line 6 - Minimum zoom level for image to be visible (min = 0).
	 * Line 7 - Maximum zoom level for image to be visible (max = 65,535).
	 * Line 8 - Width of image file in pixels.
	 * Line 9 - Height of image file in pixels.
	 * </pre>
	 */
	protected void writeSatFile(String imageFileName, int width, int height) throws IOException {
		int startX = xMin * tileSize;
		int startY = yMin * tileSize;

		MapSpace mapSpace = mapSource.getMapSpace();
		NumberFormat df = Utilities.FORMAT_6_DEC_ENG;

		String longitudeMin = df.format(mapSpace.cXToLon(startX, zoom));
		String longitudeMax = df.format(mapSpace.cXToLon(startX + width, zoom));
		String latitudeMin = df.format(mapSpace.cYToLat(startY + height, zoom));
		String latitudeMax = df.format(mapSpace.cYToLat(startY, zoom));

		StringWriter sw = new StringWriter();
		sw.write(imageFileName + "\r\n");
		sw.write(longitudeMin + "\r\n");
		sw.write(latitudeMax + "\r\n");
		sw.write(longitudeMax + "\r\n");
		sw.write(latitudeMin + "\r\n");
		sw.write("0\r\n");
		sw.write("65535\r\n");
		sw.write(Integer.toString(width) + "\r\n");
		sw.write(Integer.toString(height));

		int i = imageFileName.lastIndexOf('.');
		String satFileName = imageFileName.substring(0, i) + ".sat";
		FileOutputStream fout = new FileOutputStream(new File(layerDir,satFileName));
		OutputStreamWriter writer = new OutputStreamWriter(fout, Charsets.ISO_8859_1);
		writer.append(sw.toString());
		writer.flush();
		writer.close();
	}

	protected int getMaxImageSize() {
		return 2048;
	}
}
