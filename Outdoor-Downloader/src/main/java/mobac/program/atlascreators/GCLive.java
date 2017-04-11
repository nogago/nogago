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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;

import javax.imageio.ImageIO;

import mobac.exceptions.AtlasTestException;
import mobac.exceptions.MapCreationException;
import mobac.mapsources.mapspace.MercatorPower2MapSpace;
import mobac.program.annotations.AtlasCreatorName;
import mobac.program.annotations.SupportedParameters;
import mobac.program.atlascreators.impl.MapTileWriter;
import mobac.program.atlascreators.tileprovider.ConvertedRawTileProvider;
import mobac.program.atlascreators.tileprovider.TileProvider;
import mobac.program.interfaces.LayerInterface;
import mobac.program.interfaces.MapInterface;
import mobac.program.interfaces.MapSource;
import mobac.program.model.TileImageParameters.Name;
import mobac.utilities.Utilities;

/**
 * <a href=
 * "http://palmtopia.de/trac/GCLiveMapGen/browser/Reengineering%20of%20the%20Geocaching%20Live%20Tile%20Database.txt?format=txt"
 * >File format documentation</a>
 * 
 * <pre>
 * ===
 * === Reengineering of the Geocaching Live Tile Database ===
 * ===
 * 
 * index file:
 * ===========
 * 
 * 16 Bytes Header:
 * ----------------
 * 
 * 00 00 00 0A		Highest index used currently for the data files. Index is incremented starting with 0.
 * 00 00 4E 20		Max. number of tiles index. Current max. number is 20000.
 * 00 00 01 4D		Number of tile entries (16 bytes) indexed.
 * 00 01 0C FF		Size of the data file with the currently used highest index.
 * 
 * 16 Bytes per tile:
 * ------------------
 * 
 * 00 00			Inverse zoom level = 17 - Z. 
 * 01 0E 16			Number of the X tile.
 * 00 B1 0E			Number of the Y tile.
 * 00 00 50 92		Start offset of the PNG data in the data file.
 * 00 9D 6 			Size of the PNG data.
 *        0 01		Index of the data file which contains the PNG data.
 * 
 * The tiles are sorted starting by zoom level 17 (INVZ = 0).
 * 
 * data file:
 * ----------
 * 
 * Max. 32 PNGs concated directly together.
 * 
 * 
 * [0] - [x] directories:
 * ----------------------
 * 
 * Max. 32 data files per directory. The data files are named from 'data0' to 'dataN'.
 * </pre>
 */
@AtlasCreatorName("Geocaching Live offline map")
@SupportedParameters(names = { Name.format })
public class GCLive extends AtlasCreator {

	private static final int MAX_TILES = 65535;

	private MapTileWriter mapTileWriter = null;

	@Override
	public boolean testMapSource(MapSource mapSource) {
		return MercatorPower2MapSpace.INSTANCE_256.equals(mapSource.getMapSpace());
	}

	@Override
	protected void testAtlas() throws AtlasTestException {
		long tileCount = 0;
		for (LayerInterface layer : atlas) {
			for (MapInterface map : layer) {
				// We can not use map.calculateTilesToDownload() because be need the full tile count not the sparse
				int tileSize_t = 256; // Everything else is not allowed
				int xMin_t = map.getMinTileCoordinate().x / tileSize_t;
				int xMax_t = map.getMaxTileCoordinate().x / tileSize_t;
				int yMin_t = map.getMinTileCoordinate().y / tileSize_t;
				int yMax_t = map.getMaxTileCoordinate().y / tileSize_t;
				tileCount += (xMax_t - xMin_t + 1) * (yMax_t - yMin_t + 1);
			}
			// Check for max tile count <= 65535
			if (tileCount > MAX_TILES)
				throw new AtlasTestException("Tile count too high in layer " + layer.getName()
						+ "\n - please select smaller/fewer areas");
		}
	}

	@Override
	public void initLayerCreation(LayerInterface layer) throws IOException {
		super.initLayerCreation(layer);
		mapTileWriter = new GCLiveWriter(new File(atlasDir, layer.getName()));
	}

	@Override
	public void finishLayerCreation() throws IOException {
		mapTileWriter.finalizeMap();
		mapTileWriter = null;
		super.finishLayerCreation();
	}

	@Override
	public void abortAtlasCreation() throws IOException {
		mapTileWriter.finalizeMap();
		mapTileWriter = null;
		super.abortAtlasCreation();
	}

	@Override
	public void initializeMap(MapInterface map, TileProvider mapTileProvider) {
		super.initializeMap(map, mapTileProvider);
		if (parameters != null) {
			mapDlTileProvider = new ConvertedRawTileProvider(mapDlTileProvider, parameters.getFormat());
		}
	}

	@Override
	public void createMap() throws MapCreationException, InterruptedException {
		createTiles();
	}

	protected void createTiles() throws InterruptedException, MapCreationException {
		atlasProgress.initMapCreation((xMax - xMin + 1) * (yMax - yMin + 1));
		ImageIO.setUseCache(false);

		//byte[] emptyTileData = Utilities.createEmptyTileData(mapSource);

		for (int x = xMin; x <= xMax; x++) {
			for (int y = yMin; y <= yMax; y++) {
				checkUserAbort();
				atlasProgress.incMapCreationProgress();
				try {
					byte[] sourceTileData = mapDlTileProvider.getTileData(x, y);
					if (sourceTileData != null)
						mapTileWriter.writeTile(x, y, null, sourceTileData);
					// else
					// mapTileWriter.writeTile(x, y, null, emptyTileData);
				} catch (IOException e) {
					throw new MapCreationException("Error writing tile image: " + e.getMessage(), map, e);
				}
			}
		}
	}

	protected class GCLiveWriter implements MapTileWriter {

		private File mapDir;

		private int dataDirCounter = 0;
		private int dataFileCounter = 0;
		private int imageCounter = 0;

		private RandomAccessFile currentDataFile;

		private ArrayList<GCHeaderEntry> headerEntries;

		public GCLiveWriter(File mapDir) throws IOException {
			super();
			this.mapDir = mapDir;
			Utilities.mkDir(mapDir);
			headerEntries = new ArrayList<GCHeaderEntry>(MAX_TILES);
			prepareDataFile();
		}

		private void prepareDataFile() throws IOException {
			if (currentDataFile != null)
				Utilities.closeFile(currentDataFile);
			currentDataFile = null;
			File dataDir = new File(mapDir, Integer.toString(dataDirCounter));
			Utilities.mkDir(dataDir);
			File dataFile = new File(dataDir, "data" + Integer.toString(dataFileCounter));
			currentDataFile = new RandomAccessFile(dataFile, "rw");
			imageCounter = 0;
		}

		public void writeTile(int tilex, int tiley, String tileType, byte[] tileData) throws IOException {
			imageCounter++;
			if (imageCounter >= 32) {
				dataFileCounter++;
				if (dataFileCounter % 32 == 0) {
					dataDirCounter++;
					if (dataDirCounter >= 32)
						throw new RuntimeException("Maximum number of tiles exceeded");
				}
				prepareDataFile();
			}
			long offset = currentDataFile.getFilePointer();
			currentDataFile.write(tileData);
			int len = tileData.length;

			GCHeaderEntry header = new GCHeaderEntry(zoom, tilex, tiley, dataFileCounter, (int) offset, len);
			headerEntries.add(header);
		}

		public void finalizeMap() throws IOException {
			int dataPos = (int) currentDataFile.getFilePointer();
			Utilities.closeFile(currentDataFile);
			Collections.sort(headerEntries);

			RandomAccessFile indexFile;
			indexFile = new RandomAccessFile(new File(mapDir, "index"), "rw");

			// Write index header (first 16 bytes)
			indexFile.seek(0);
			int dataFileIndex = dataDirCounter * 32 + dataFileCounter;
			indexFile.writeInt(dataFileIndex); // Highest index used currently for the data files. Index is incremented
												// starting with 0.
			indexFile.writeInt(headerEntries.size()); // Max. number of tiles index. Current max. number is 20000.
			indexFile.writeInt(headerEntries.size()); // Number of tile entries (16 bytes) indexed.
			indexFile.writeInt(dataPos); // Size of the data file with the currently used highest index.

			for (GCHeaderEntry entry : headerEntries) {
				entry.writeHeader(indexFile);
				System.out.println(entry);
			}
			headerEntries = null;
			Utilities.closeFile(indexFile);
		}
	}

	public static class GCHeaderEntry implements Comparable<GCHeaderEntry> {
		public final int zoom;
		public final int tilex;
		public final int tiley;
		public final int dataFileIndex;
		public final int offset;
		public final int len;

		public GCHeaderEntry(int zoom, int tilex, int tiley, int dataFileIndex, int offset, int len) {
			super();
			this.zoom = zoom;
			this.tilex = tilex;
			this.tiley = tiley;
			this.dataFileIndex = dataFileIndex;
			this.offset = offset;
			this.len = len;
		}

		public void writeHeader(RandomAccessFile file) throws IOException {
			file.writeShort((short) (17 - zoom));
			file.write((tilex >> 16) & 0xFF);
			file.write((tilex >> 8) & 0xFF);
			file.write(tilex & 0xFF);
			file.write((tiley >> 16) & 0xFF);
			file.write((tiley >> 8) & 0xFF);
			file.write(tiley & 0xFF);
			file.writeInt(offset);

			int tmp = (len << 4);
			tmp = tmp | ((dataFileIndex >> 8) & 0x0F);

			file.write((tmp >> 16) & 0xFF);
			file.write((tmp >> 8) & 0xFF);
			file.write(tmp & 0xFF);
			file.write(dataFileIndex & 0xFF);
		}

		public int compareTo(GCHeaderEntry o) {
			if (zoom > o.zoom)
				return -1;
			if (zoom < o.zoom)
				return 1;
			if (tilex > o.tilex)
				return 1;
			if (tilex < o.tilex)
				return -1;
			if (tiley > o.tiley)
				return 1;
			if (tiley < o.tiley)
				return -1;
			return 0;
		}

		@Override
		public String toString() {
			return "GCHeaderEntry [zoom=" + zoom + ", tilex=" + tilex + ", tiley=" + tiley + ", dataFileIndex="
					+ dataFileIndex + ", offset=" + offset + ", len=" + len + "]";
		}

	}
}
