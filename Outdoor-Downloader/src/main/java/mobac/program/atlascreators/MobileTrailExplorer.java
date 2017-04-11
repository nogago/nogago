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
import mobac.program.atlascreators.tileprovider.ConvertedRawTileProvider;
import mobac.program.atlascreators.tileprovider.TileProvider;
import mobac.program.interfaces.MapInterface;
import mobac.program.interfaces.MapSource;
import mobac.program.model.TileImageFormat;
import mobac.program.model.TileImageType;
import mobac.utilities.Utilities;

/**
 * Creates maps using the Mobile Trail Explorer (MTE) / JTileDownloader atlas format (converted to PNG, size 256x256
 * pixel).
 * 
 * Please note that this atlas format ignores the defined atlas structure. It uses a separate directory for each used
 * map source and inside one directory for each zoom level.
 */
@AtlasCreatorName(value = "Mobile Trail Explorer", type = "MTE")
public class MobileTrailExplorer extends AtlasCreator {

	private File mapDir = null;
	private File mapZoomDir = null;

	protected String appendFileExt = "";

	@Override
	public boolean testMapSource(MapSource mapSource) {
		return MercatorPower2MapSpace.INSTANCE_256.equals(mapSource.getMapSpace());
	}

	@Override
	public void initializeMap(MapInterface map, TileProvider mapTileProvider) {
		super.initializeMap(map, mapTileProvider);
		String mapName = map.getMapSource().getName().replaceAll(" ", "_");
		mapDir = new File(atlasDir, mapName);
		mapZoomDir = new File(mapDir, Integer.toString(map.getZoom()));
	}

	public void createMap() throws MapCreationException, InterruptedException {
		try {
			Utilities.mkDir(mapDir);
			Utilities.mkDir(mapZoomDir);
		} catch (IOException e1) {
			throw new MapCreationException(map, e1);
		}
		if (mapSource.getTileImageType() != TileImageType.PNG)
			// If the tile image format is not png we have to convert it
			mapDlTileProvider = new ConvertedRawTileProvider(mapDlTileProvider, TileImageFormat.PNG);
		createTiles();
	}

	protected void createTiles() throws InterruptedException, MapCreationException {
		atlasProgress.initMapCreation((xMax - xMin + 1) * (yMax - yMin + 1));
		ImageIO.setUseCache(false);

		for (int x = xMin; x <= xMax; x++) {
			File xDir = new File(mapZoomDir, Integer.toString(x));
			try {
				Utilities.mkDir(xDir);
			} catch (IOException e1) {
				throw new MapCreationException(map, e1);
			}
			for (int y = yMin; y <= yMax; y++) {
				checkUserAbort();
				atlasProgress.incMapCreationProgress();
				try {
					String tileFileName = x + "/" + y + ".png";
					byte[] sourceTileData = mapDlTileProvider.getTileData(x, y);
					if (sourceTileData != null) {
						File f = new File(mapZoomDir, tileFileName);
						FileOutputStream out = new FileOutputStream(f);
						try {
							out.write(sourceTileData);
						} finally {
							Utilities.closeStream(out);
						}
					}
				} catch (IOException e) {
					throw new MapCreationException("Error writing tile image: " + e.getMessage(), map, e);
				}
			}
		}
	}

}
