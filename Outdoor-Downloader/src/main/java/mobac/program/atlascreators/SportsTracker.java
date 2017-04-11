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
import mobac.program.interfaces.LayerInterface;
import mobac.program.interfaces.MapInterface;
import mobac.program.interfaces.MapSource;
import mobac.program.model.TileImageParameters;
import mobac.program.model.TileImageParameters.Name;
import mobac.utilities.Utilities;

/**
 * [Nokia] Sports Tracker
 * 
 * 
 */
@AtlasCreatorName(value = "Sports Tracker", type = "NST")
@SupportedParameters(names = { Name.format })
public class SportsTracker extends AtlasCreator {

	protected File mapDir = null;

	protected MapTileWriter mapTileWriter = null;

	protected String tileType = null;

	@Override
	public boolean testMapSource(MapSource mapSource) {
		return MercatorPower2MapSpace.INSTANCE_256.equals(mapSource.getMapSpace());
	}

	@Override
	public void initLayerCreation(LayerInterface layer) throws IOException {
		super.initLayerCreation(layer);
		mapDir = new File(atlasDir, layer.getName());
	}

	@Override
	public void initializeMap(MapInterface map, TileProvider mapTileProvider) {
		super.initializeMap(map, mapTileProvider);
		tileType = map.getMapSource().getTileImageType().getFileExt();
		TileImageParameters param = map.getParameters();
		if (param != null) {
			mapDlTileProvider = new ConvertedRawTileProvider(mapDlTileProvider, param.getFormat());
			tileType = param.getFormat().getFileExt();
		}

	}

	public void createMap() throws MapCreationException, InterruptedException {
		// This means there should not be any resizing of the tiles.
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
						writeTile(x, y, sourceTileData);
				} catch (IOException e) {
					throw new MapCreationException("Error writing tile image: " + e.getMessage(), map, e);
				}
			}
		}
	}

	public void writeTile(int tilex, int tiley, byte[] tileData) throws IOException {
		String tileName = getTileName(zoom, tilex, tiley);
		int count = tileName.length();
		int dirCount = count / 3;
		if ((count % 3 == 0) & (dirCount > 0))
			dirCount--;
		File tileDir = mapDir;
		for (int i = 0; i < dirCount; i++) {
			int start = i * 3;
			String dirName = tileName.substring(start, start + 3);
			tileDir = new File(tileDir, dirName);
		}
		// File extension needs to be jpg (requested by telemaxx)
		// see https://sourceforge.net/tracker/?func=detail&atid=1105497&aid=3066161&group_id=238075
		String fileName = tileName + ".jpg";
		File file = new File(tileDir, fileName);
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

	protected static final char[] NUM_CHAR = { 'q', 'r', 't', 's' };

	public static String getTileName(int zoom, int tilex, int tiley) {
		char[] tileNum = new char[zoom + 1];
		tileNum[0] = 't';
		for (int i = zoom; i > 0; i--) {
			int num = (tilex % 2) | ((tiley % 2) << 1);
			tileNum[i] = NUM_CHAR[num];
			tilex >>= 1;
			tiley >>= 1;
		}
		return new String(tileNum);
	}

}
