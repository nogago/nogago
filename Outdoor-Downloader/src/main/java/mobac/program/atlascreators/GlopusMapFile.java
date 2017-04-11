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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.LinkedList;

import mobac.exceptions.MapCreationException;
import mobac.program.annotations.AtlasCreatorName;
import mobac.program.atlascreators.impl.MapTileWriter;
import mobac.program.interfaces.LayerInterface;
import mobac.program.interfaces.MapSpace;
import mobac.utilities.GUIExceptionHandler;
import mobac.utilities.Utilities;
import mobac.utilities.stream.LittleEndianOutputStream;

/**
 * General structure of an GMF file (Little Endian)
 * 
 * <pre>
 * DWORD Version // 0xff000002
 * DWORD cnt // Number of tiles in the file
 * 
 * for each tile: 
 *   DWORD len;         // number of characters in tile name
 *   wchar_t name[len]  // map/tile name in UTF_16LE
 *   DWORD filepos      // offset where image data starts in this file
 *   DWORD width        // tile width in pixel
 *   DWORD height       // tile height in pixel
 *   DWORD cntCalPoints // calibration point count (usually 2 or 4)
 *   for each tile calibration point
 *     DWORD x      // calibration point x position in tile
 *     DWORD y      // calibration point y position in tile
 *     double dLong // longitude of calibration point
 *     double dLat  // latitude of calibration point
 * END OF FILE HEADER
 * Afterwards the tile image data follows as specified by each filepos 
 * offset.
 * </pre>
 * 
 */
@AtlasCreatorName(value = "Glopus Map File (GMF)", type = "Gmf")
public class GlopusMapFile extends TrekBuddy {

	@Override
	public void initLayerCreation(LayerInterface layer) throws IOException {
		super.initLayerCreation(layer);
		mapTileWriter = new GlopusTileWriter(layer);
	}

	@Override
	public void finishLayerCreation() throws IOException {
		mapTileWriter.finalizeMap();
		mapTileWriter = null;
		super.finishLayerCreation();
	}

	@Override
	public void createMap() throws MapCreationException, InterruptedException {
		try {
			((GlopusTileWriter) mapTileWriter).initMap();
			// Select the tile creator instance based on whether tile image
			// parameters has been set or not
			if (parameters != null)
				createCustomTiles();
			else
				createTiles();
		} catch (MapCreationException e) {
			throw e;
		} catch (InterruptedException e) {
			throw e;
		} catch (Exception e) {
			throw new MapCreationException(map, e);
		}
	}

	@Override
	public void createAtlasTbaFile(String name) {
	}

	@Override
	public void abortAtlasCreation() throws IOException {
		mapTileWriter = null;
		super.abortAtlasCreation();
	}

	private class GlopusTileWriter implements MapTileWriter {

		final LayerInterface layer;
		LinkedList<GlopusTile> tiles;
		int xCoordStart;
		int yCoordStart;
		int tileHeight = 256;
		int tileWidth = 256;
		int zoom;
		MapSpace mapSpace;
		String tileType;

		public GlopusTileWriter(LayerInterface layer) {
			super();
			this.layer = layer;
			tiles = new LinkedList<GlopusTile>();
		}

		public void initMap() {
			if (parameters != null) {
				tileHeight = parameters.getHeight();
				tileWidth = parameters.getWidth();
			}
			zoom = map.getZoom();
			mapSpace = mapSource.getMapSpace();
			xCoordStart = GlopusMapFile.this.xMin * mapSpace.getTileSize();
			yCoordStart = GlopusMapFile.this.yMin * mapSpace.getTileSize();
		}

		public void writeTile(int tilex, int tiley, String tileType, byte[] tileData) throws IOException {
			this.tileType = tileType;
			int xCooord = xCoordStart + tilex * tileWidth;
			int yCooord = yCoordStart + tiley * tileHeight;

			double calWLon = mapSpace.cXToLon(xCooord, zoom);
			double calNLat = mapSpace.cYToLat(yCooord, zoom);
			double calELon = mapSpace.cXToLon(xCooord + tileWidth, zoom);
			double calSLat = mapSpace.cYToLat(yCooord + tileHeight, zoom);
			GlopusTile gt = new GlopusTile(tileData, calNLat, calWLon, calSLat, calELon);
			tiles.add(gt);
		}

		public void finalizeMap() {

			File gmfFile = new File(atlasDir, layer.getName() + ".gmf");
			FileOutputStream fout = null;
			try {
				int count = tiles.size();
				int offset = 8 + count * ( //
						20 // nameLength, offset and calibration point count,
						// tile height & width
						+ (12 * 2) // name bytes
						+ (4 * 24) // four calibration points
						);
				fout = new FileOutputStream(gmfFile);
				LittleEndianOutputStream out = new LittleEndianOutputStream(new BufferedOutputStream(fout, 16384));
				out.writeInt((int) 0xff000002);
				out.writeInt(count);
				int mapNumber = 0;
				Charset charset = Charset.forName("UTF-16LE");
				for (GlopusTile gt : tiles) {
					String mapName = String.format("%08d.%s", mapNumber++, tileType);
					byte[] nameBytes = mapName.getBytes(charset);
					out.writeInt(mapName.length());// Name length
					out.write(nameBytes);
					out.writeInt(offset);
					out.writeInt(tileWidth);
					out.writeInt(tileHeight);
					out.writeInt(4); // number of calibration points
					out.writeInt(0);
					out.writeInt(0);
					out.writeDouble(gt.calWLon);
					out.writeDouble(gt.calNLat);
					out.writeInt(tileHeight);
					out.writeInt(tileWidth);
					out.writeDouble(gt.calELon);
					out.writeDouble(gt.calSLat);
					out.writeInt(tileHeight);
					out.writeInt(0);
					out.writeDouble(gt.calELon);
					out.writeDouble(gt.calNLat);
					out.writeInt(0);
					out.writeInt(tileWidth);
					out.writeDouble(gt.calWLon);
					out.writeDouble(gt.calSLat);
					if (log.isTraceEnabled())
						log.trace(String.format("Offset %f %f %f %f \"%s\": 0x%x", gt.calWLon, gt.calNLat, gt.calELon,
								gt.calELon, mapName, offset));
					offset += gt.data.length;
				}
				out.flush();
				out = null;
				for (GlopusTile gt : tiles) {
					fout.write(gt.data);
				}
				fout.flush();
			} catch (IOException e) {
				GUIExceptionHandler.showExceptionDialog(e);
			} finally {
				Utilities.closeStream(fout);
			}
		}

	}

	private static class GlopusTile {
		byte[] data;
		double calNLat;
		double calWLon;
		double calSLat;
		double calELon;

		public GlopusTile(byte[] data, double calNLat, double calWLon, double calSLat, double calELon) {
			super();
			this.data = data;
			this.calNLat = calNLat;
			this.calWLon = calWLon;
			this.calSLat = calSLat;
			this.calELon = calELon;
		}

	}
}
