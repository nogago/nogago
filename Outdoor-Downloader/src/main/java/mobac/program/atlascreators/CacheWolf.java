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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import mobac.exceptions.MapCreationException;
import mobac.program.annotations.AtlasCreatorName;
import mobac.program.atlascreators.impl.MapTileBuilder;
import mobac.program.atlascreators.impl.MapTileWriter;
import mobac.program.atlascreators.tileprovider.CacheTileProvider;
import mobac.program.atlascreators.tileprovider.TileProvider;
import mobac.program.interfaces.MapInterface;
import mobac.program.interfaces.MapSpace;
import mobac.utilities.Utilities;

@AtlasCreatorName("CacheWolf (WFL)")
public class CacheWolf extends Ozi {

	@Override
	public void initializeMap(MapInterface map, TileProvider mapTileProvider) {
		super.initializeMap(map, mapTileProvider);
	}

	@Override
	public void createMap() throws MapCreationException, InterruptedException {
		try {
			Utilities.mkDirs(layerDir);
		} catch (IOException e1) {
			throw new MapCreationException(map, e1);
		}
		if (parameters == null) {
			// One image per map
			super.createTiles();
			writeWflFile();
		} else
			// Use automatic tiling as specified in the parameters
			createTiles();
	}

	@Override
	protected void createTiles() throws InterruptedException, MapCreationException {
		MapTileWriter mapTileWriter;

		CacheTileProvider ctp = new CacheTileProvider(mapDlTileProvider);
		try {
			mapDlTileProvider = ctp;
			mapTileWriter = new CWFileTileWriter();
			MapTileBuilder mapTileBuilder = new MapTileBuilder(this, mapTileWriter, true);
			atlasProgress.initMapCreation(mapTileBuilder.getCustomTileCount());
			mapTileBuilder.createTiles();
		} catch (IOException e) {
			throw new MapCreationException(map, e);
		} finally {
			ctp.cleanup();
		}
	}

	private void writeWflFile() throws MapCreationException {
		int width = (xMax - xMin + 1) * tileSize;
		int height = (yMax - yMin + 1) * tileSize;
		try {
			writeWflFile(mapName, 0, 0, width, height);
		} catch (IOException e) {
			throw new MapCreationException("Error writing wfl file: " + e.getMessage(), map, e);
		}
	}

	private void writeWflFile(String filename, int tilex, int tiley, int width, int height) throws IOException {
		FileOutputStream fout = null;
		try {
			fout = new FileOutputStream(new File(layerDir, filename + ".wfl"));
			OutputStreamWriter mapWriter = new OutputStreamWriter(fout, TEXT_FILE_CHARSET);

			MapSpace mapSpace = mapSource.getMapSpace();

			int xStart = xMin * tileSize;
			int yStart = yMin * tileSize;

			if (parameters != null) {
				xStart += tilex * parameters.getWidth();
				yStart += tiley * parameters.getHeight();
			}

			double topLeftLon = mapSpace.cXToLon(xStart, zoom);
			double topLeftLat = mapSpace.cYToLat(yStart, zoom);

			double bottomRightLon = mapSpace.cXToLon(xStart + width, zoom);
			double bottomRightLat = mapSpace.cYToLat(yStart + height, zoom);

			double[] affine = { 0, 0, 0, 0 };

			// Mobile Atlas Creator does only output maps with north at top
			// (no rotation). Therefore we should be able to simplify the affine
			// calculation process:
			affine[1] = (bottomRightLon - topLeftLon) / width;
			affine[2] = (bottomRightLat - topLeftLat) / height;

			for (double d : affine)
				mapWriter.write(Double.toString(d) + "\n");

			mapWriter.write(Double.toString(topLeftLat) + "\n");
			mapWriter.write(Double.toString(topLeftLon) + "\n");
			mapWriter.write(Double.toString(bottomRightLat) + "\n");
			mapWriter.write(Double.toString(bottomRightLon) + "\n");

			mapWriter.flush();
			mapWriter.close();
		} finally {
			Utilities.closeStream(fout);
		}
	}

	public class CWFileTileWriter implements MapTileWriter {

		public CWFileTileWriter() throws IOException {
			super();
			log.debug("Writing tiles to set folder: " + layerDir);
		}

		public void writeTile(int tilex, int tiley, String imageFormat, byte[] tileData) throws IOException {
			String tileFileName = String.format("%s_%dx%d", mapName, tilex, tiley);
			File f = new File(layerDir, tileFileName + '.' + imageFormat);
			FileOutputStream out = new FileOutputStream(f);
			try {
				out.write(tileData);
			} finally {
				Utilities.closeStream(out);
			}
			writeWflFile(tileFileName, tilex, tiley, parameters.getWidth(), parameters.getHeight());
		}

		public void finalizeMap() {
		}
	}

}
