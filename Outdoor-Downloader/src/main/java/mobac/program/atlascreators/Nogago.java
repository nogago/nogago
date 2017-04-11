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

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;

import javax.imageio.ImageIO;

import mobac.exceptions.AtlasTestException;
import mobac.exceptions.MapCreationException;
import mobac.mapsources.mapspace.MercatorPower2MapSpace;
import mobac.program.annotations.AtlasCreatorName;
import mobac.program.annotations.SupportedParameters;
import mobac.program.atlascreators.tileprovider.ConvertedRawTileProvider;
import mobac.program.atlascreators.tileprovider.TileProvider;
import mobac.program.interfaces.AtlasInterface;
import mobac.program.interfaces.LayerInterface;
import mobac.program.interfaces.MapInterface;
import mobac.program.interfaces.MapSource;
import mobac.program.interfaces.MapSpace;
import mobac.program.interfaces.MapSpace.ProjectionCategory;
import mobac.program.interfaces.TileImageDataWriter;
import mobac.program.model.TileImageParameters.Name;
import mobac.utilities.Utilities;
import mobac.utilities.stream.ArrayOutputStream;

/**
 * Creates maps using the Nogago Version of the MGM pack file format (.mgm).
 * 
 * Each zoom level in a different directory, 64 tiles per mgm file.
 * 
 * <h3>Format</h3>
 * <ul>
 * <li>2 bytes: number of tiles in this file for each tile (even for tiles that are not used, in which case the data is
 * left null)</li>
 * <li>1 byte: tile x within this file; add tileX * tilesPerFileX to get global tile number</li>
 * <li>1 byte: tile y within this file; add tileY * tilesPerFileY to get global tile number</li>
 * <li>4 bytes: offset of the end of the tile data within this file (to get the offset for the start of the tile data,
 * subtract the value for the previous tile, or 2 + 6 * tilesPerFile tile data 1 tile data 2 ...</li>
 * </ul>
 * 
 * @author paour
 */
@AtlasCreatorName(value = "Nogago", type = "NOGAGO")
@SupportedParameters(names = { Name.format })
public class Nogago extends AtlasCreator {

	private static final int TILES_PER_FILE_X = 8;
	private static final int TILES_PER_FILE_Y = 8;
	private static final int TILES_PER_FILE = TILES_PER_FILE_X * TILES_PER_FILE_Y;

	private double xResizeRatio = 1.0;
	private double yResizeRatio = 1.0;

	private double east;
	private double west;
	private double north;
	private double south;

	@Override
	public void startAtlasCreation(AtlasInterface atlas, File customAtlasDir) throws AtlasTestException, IOException,
			InterruptedException {
		super.startAtlasCreation(atlas, customAtlasDir);

		File cache_conf = new File(atlasDir, "cache.conf");
		PrintWriter pw = new PrintWriter(new FileWriter(cache_conf));
		try {
			pw.println("version=3");
			pw.println("tiles_per_file=" + TILES_PER_FILE);
			pw.println("hash_size=1");
			// TODO Write the center coordinates into the extra space
			east = atlas.getMaxLon();
			west = atlas.getMinLon();
			north = atlas.getMaxLat();
			south = atlas.getMinLat();
			double hCenter = west + (east - west) / 2.0d;
			double vCenter = south + (north - south) / 2.0d;
			pw.println("center=" + vCenter + "," + hCenter + "," + 10 + ",OpenStreetMap");
			pw.println("");
			// Determine the available zoom levels
			int minZoom = 100;
			int maxZoom = -100;
			for (int i = 0; i < atlas.getLayerCount(); i++) {
				LayerInterface l = atlas.getLayer(i);
				for (int j = 0; j < l.getMapCount(); j++) {
					MapInterface m = l.getMap(j);
					int z = m.getZoom();
					minZoom = (minZoom > z) ? z : minZoom;
					maxZoom = (maxZoom < z) ? z : maxZoom;
				}	
			}
			pw.println(minZoom + "-" + maxZoom + ":" + north + "," + west + ":" + south + "," + east);
		} finally {
			pw.close();
		}
	}

	@Override
	public void initializeMap(final MapInterface map, final TileProvider mapTileProvider) {
		super.initializeMap(map, mapTileProvider);

		xResizeRatio = 1.0;
		yResizeRatio = 1.0;

		if (parameters != null) {
			int mapTileSize = map.getMapSource().getMapSpace().getTileSize();
			if ((parameters.getWidth() != mapTileSize) || (parameters.getHeight() != mapTileSize)) {
				// handle image re-sampling + image re-sizing
				xResizeRatio = (double) parameters.getWidth() / (double) mapTileSize;
				yResizeRatio = (double) parameters.getHeight() / (double) mapTileSize;
			} else {
				// handle only image re-sampling
				mapDlTileProvider = new ConvertedRawTileProvider(mapDlTileProvider, parameters.getFormat());
			}
		}
	}

	@Override
	public void createMap() throws MapCreationException, InterruptedException {
		MGMTileWriter mgmTileWriter = null;
		try {
			if ((xResizeRatio != 1.0) || (yResizeRatio != 1.0))
				mgmTileWriter = new MGMResizeTileWriter();
			else
				mgmTileWriter = new MGMTileWriter();

			// TO DO FIX ME BACK
			String name = "OpenStreetMap"; // map.getLayer().getName();

			// safe naming: replace all non-word characters: [^a-zA-Z_0-9]
			// name = name.replaceAll("[^a-zA-Z_0-9]", "_");

			// crate directory if necessary
			File folder = new File(atlasDir, name + "_" + map.getZoom());
			Utilities.mkDirs(folder);

			atlasProgress.initMapCreation((xMax - xMin + 1) * (yMax - yMin + 1));

			ImageIO.setUseCache(false);

			int pxMin = xMin / TILES_PER_FILE_X;
			int pxMax = xMax / TILES_PER_FILE_X;
			int pyMin = yMin / TILES_PER_FILE_Y;
			int pyMax = yMax / TILES_PER_FILE_Y;

			for (int px = pxMin; px <= pxMax; px++) {
				for (int py = pyMin; py <= pyMax; py++) {
					int count = 0;
					int pos = 2 + TILES_PER_FILE * 6;
					File pack = new File(folder, px + "_" + py + ".mgm");
					RandomAccessFile raf = null;

					try {
						for (int i = 0; i < TILES_PER_FILE_X; i++) {
							int x = px * TILES_PER_FILE_X + i;
							if (x < xMin || x > xMax) {
								continue;
							}

							for (int j = 0; j < TILES_PER_FILE_Y; j++) {
								int y = py * TILES_PER_FILE_Y + j;
								if (y < yMin || y > yMax) {
									continue;
								}

								if (raf == null)
									// Only create a file when needed
									raf = new RandomAccessFile(pack, "rw");

								checkUserAbort();
								atlasProgress.incMapCreationProgress();
								int res = mgmTileWriter.writeTile(x, y, i, j, raf, pos, count);
								if (res >= 0) {
									pos = res;
									count++;
								}
							}
						}

						if (raf != null) {
							// POSITION 0: number of tiles
							raf.seek(0);
							raf.writeChar(count);
						}
					} finally {
						Utilities.closeFile(raf);
					}
					if (count == 0) {
						// the file doesn't contain any tiles
						if (pack.exists())
							Utilities.deleteFile(pack);
					}
				}
			}

		} catch (Exception e) {
			throw new MapCreationException(map, e);
		} finally {
			if (mgmTileWriter != null)
				mgmTileWriter.dispose();
		}
	}

	@Override
	public boolean testMapSource(final MapSource mapSource) {
		MapSpace mapSpace = mapSource.getMapSpace();
		return (mapSpace instanceof MercatorPower2MapSpace)
				&& (ProjectionCategory.SPHERE.equals(mapSource.getMapSpace().getProjectionCategory()) || ProjectionCategory.ELLIPSOID
						.equals(mapSource.getMapSpace().getProjectionCategory()));
	}

	/**
	 * Simply writes the tile to the file without resizing
	 */
	private class MGMTileWriter {

		protected byte[] getSourceTileData(int x, int y) throws IOException {
			return mapDlTileProvider.getTileData(x, y);
		}

		public int writeTile(int x, int y, int i, int j, RandomAccessFile raf, int startPos, int count)
				throws MapCreationException {
			try {
				byte[] sourceTileData = getSourceTileData(x, y);
				if (sourceTileData == null)
					return -1;
				raf.seek(startPos);
				raf.write(sourceTileData);

				// write the tile index
				raf.seek(2 + count * 6);
				raf.writeByte(i);
				raf.writeByte(j);
				int pos = startPos + sourceTileData.length;
				raf.writeInt(pos);
				return pos;
			} catch (IOException e) {
				throw new MapCreationException("Error writing tile image: " + e.getMessage(), map, e);
			}
		}

		public void dispose() {
			// Nothing to do
		}
	}

	/**
	 * Resizes the tile and saves it to the file
	 */
	private class MGMResizeTileWriter extends MGMTileWriter {

		private final BufferedImage tileImage;
		private final Graphics2D graphics;
		private final TileImageDataWriter writer;
		private final ArrayOutputStream buffer;

		public MGMResizeTileWriter() {
			// resize image
			tileImage = new BufferedImage(parameters.getWidth(), parameters.getHeight(), BufferedImage.TYPE_3BYTE_BGR);

			// associated graphics with affine transform
			graphics = tileImage.createGraphics();
			graphics.setTransform(AffineTransform.getScaleInstance(xResizeRatio, yResizeRatio));
			graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

			// image compression writer
			writer = parameters.getFormat().getDataWriter();

			// buffer to store compressed image
			buffer = new ArrayOutputStream(3 * parameters.getWidth() * parameters.getHeight());
		}

		@Override
		protected byte[] getSourceTileData(int x, int y) throws IOException {
			// need to resize the tile
			final BufferedImage tile = mapDlTileProvider.getTileImage(x, y);
			graphics.drawImage(tile, 0, 0, null);
			buffer.reset();
			writer.initialize();
			writer.processImage(tileImage, buffer);

			byte[] processedTileData = buffer.toByteArray();
			buffer.reset();
			return processedTileData;
		}

		@Override
		public void dispose() {
			buffer.reset();
			graphics.dispose();
		}

	}
}
