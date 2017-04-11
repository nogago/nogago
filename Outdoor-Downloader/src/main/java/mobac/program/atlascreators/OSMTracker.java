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

import javax.imageio.ImageIO;

import mobac.exceptions.MapCreationException;
import mobac.mapsources.mapspace.MercatorPower2MapSpace;
import mobac.program.annotations.AtlasCreatorName;
import mobac.program.annotations.SupportedParameters;
import mobac.program.atlascreators.impl.MapTileWriter;
import mobac.program.atlascreators.tileprovider.ConvertedRawTileProvider;
import mobac.program.atlascreators.tileprovider.TileProvider;
import mobac.program.interfaces.MapInterface;
import mobac.program.interfaces.MapSource;
import mobac.program.model.TileImageParameters.Name;
import mobac.utilities.Utilities;

/**
 * Creates maps identical to the atlas format used by OSMTracker.
 * 
 * Please note that this atlas format ignores the defined atlas structure. It uses a separate directory for each used
 * map source and inside one directory for each zoom level.
 */
@AtlasCreatorName("OSMTracker tile storage")
@SupportedParameters(names = { Name.format })
public class OSMTracker extends AtlasCreator {

	protected String tileFileNamePattern = "%d/%d/%d.%s";

	protected File mapDir = null;

	protected String tileType = null;

	protected MapTileWriter mapTileWriter = null;

	@Override
	public boolean testMapSource(MapSource mapSource) {
		return MercatorPower2MapSpace.INSTANCE_256.equals(mapSource.getMapSpace());
	}

	@Override
	public void initializeMap(MapInterface map, TileProvider mapTileProvider) {
		super.initializeMap(map, mapTileProvider);
		mapDir = new File(atlasDir, map.getMapSource().getName());
		tileType = mapSource.getTileImageType().getFileExt();
		if (parameters != null) {
			mapDlTileProvider = new ConvertedRawTileProvider(mapDlTileProvider, parameters.getFormat());
			tileType = parameters.getFormat().getFileExt();
		}
	}

	public void createMap() throws MapCreationException, InterruptedException {
		// This means there should not be any resizing of the tiles.
		if (mapTileWriter == null)
			mapTileWriter = new OSMTileWriter();
		createTiles();
	}

	protected void createTiles() throws InterruptedException, MapCreationException {
		atlasProgress.initMapCreation((xMax - xMin + 1) * (yMax - yMin + 1));
		ImageIO.setUseCache(false);

		for (int x = xMin; x <= xMax; x++) {
			for (int y = yMin; y <= yMax; y++) {
				checkUserAbort();
				atlasProgress.incMapCreationProgress();
				try {
					byte[] sourceTileData = mapDlTileProvider.getTileData(x, y);
					if (sourceTileData != null)
						mapTileWriter.writeTile(x, y, tileType, sourceTileData);
				} catch (IOException e) {
					throw new MapCreationException("Error writing tile image: " + e.getMessage(), map, e);
				}
			}
		}
	}

	protected class OSMTileWriter implements MapTileWriter {

		public void writeTile(int tilex, int tiley, String tileType, byte[] tileData) throws IOException {
			File file = new File(mapDir, String.format(tileFileNamePattern, zoom, tilex, tiley, tileType));
			writeTile(file, tileData);
		}

		protected void writeTile(File file, byte[] tileData) throws IOException {
			Utilities.mkDirs(file.getParentFile());
			FileOutputStream out = new FileOutputStream(file);
			try {
				out.write(tileData);
			} finally {
				Utilities.closeStream(out);
			}
		}

		public void finalizeMap() throws IOException {
			// Nothing to do
		}

	}
}
