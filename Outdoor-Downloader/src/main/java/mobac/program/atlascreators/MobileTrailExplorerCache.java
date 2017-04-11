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
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;

import mobac.exceptions.AtlasTestException;
import mobac.exceptions.MapCreationException;
import mobac.mapsources.mapspace.MercatorPower2MapSpace;
import mobac.program.annotations.AtlasCreatorName;
import mobac.program.atlascreators.tileprovider.ConvertedRawTileProvider;
import mobac.program.atlascreators.tileprovider.TileProvider;
import mobac.program.interfaces.AtlasInterface;
import mobac.program.interfaces.MapInterface;
import mobac.program.interfaces.MapSource;
import mobac.program.model.TileImageFormat;
import mobac.program.model.TileImageType;
import mobac.utilities.Utilities;

/**
 * Creates maps using the Mobile Trail Explorer (MTE) cache format.
 * 
 * Please note that this atlas format ignores the defined atlas structure.
 * 
 */
@AtlasCreatorName(value = "Mobile Trail Explorer Cache", type = "MTECache")
public class MobileTrailExplorerCache extends AtlasCreator {

	protected DataOutputStream cacheOutStream = null;
	protected long lastTileOffset = 0;
	protected Set<String> availableTileList = new HashSet<String>();

	@Override
	public boolean testMapSource(MapSource mapSource) {
		return MercatorPower2MapSpace.INSTANCE_256.equals(mapSource.getMapSpace());
	}

	@Override
	public void startAtlasCreation(AtlasInterface atlas, File customAtlasDir) throws IOException, InterruptedException,
			AtlasTestException {
		super.startAtlasCreation(atlas, customAtlasDir);
		File cacheFile = new File(atlasDir, "MTEFileCache");
		OutputStream out = new BufferedOutputStream(new FileOutputStream(cacheFile), 8216);
		cacheOutStream = new DataOutputStream(out);
	}

	@Override
	public void abortAtlasCreation() throws IOException {
		Utilities.closeStream(cacheOutStream);
		super.abortAtlasCreation();
	}

	@Override
	public void finishAtlasCreation() throws IOException, InterruptedException {
		super.finishAtlasCreation();
		cacheOutStream.close();
	}

	@Override
	public void initializeMap(MapInterface map, TileProvider mapTileProvider) {
		super.initializeMap(map, mapTileProvider);
	}

	public void createMap() throws MapCreationException, InterruptedException {
		if (mapSource.getTileImageType() != TileImageType.PNG)
			// If the tile image format is not png we have to convert it
			mapDlTileProvider = new ConvertedRawTileProvider(mapDlTileProvider, TileImageFormat.PNG);
		createTiles();
	}

	protected void createTiles() throws InterruptedException, MapCreationException {
		atlasProgress.initMapCreation((xMax - xMin + 1) * (yMax - yMin + 1));
		ImageIO.setUseCache(false);
		String mapName = map.getMapSource().getName().replaceAll(" ", "_");

		for (int x = xMin; x <= xMax; x++) {
			for (int y = yMin; y <= yMax; y++) {
				checkUserAbort();
				atlasProgress.incMapCreationProgress();
				try {
					byte[] sourceTileData = mapDlTileProvider.getTileData(x, y);
					if (sourceTileData != null)
						writeTile(mapName, sourceTileData, x, y, zoom);
				} catch (IOException e) {
					throw new MapCreationException("Error writing tile image: " + e.getMessage(), map, e);
				}
			}
		}
	}

	protected boolean writeTile(String cache, byte[] tileData, int x, int y, int zoom) throws IOException {
		String url = "not used";
		String cacheKey = cache + "-" + zoom + "-" + x + "-" + y;

		if (availableTileList.contains(cacheKey)) {
			log.warn("Map tile already in cache: " + cacheKey + " -> ignoring");
			return false;
		}

		cacheOutStream.writeInt(x);
		cacheOutStream.writeInt(y);
		cacheOutStream.writeInt(zoom);

		byte[] urlBytes = url.getBytes();
		cacheOutStream.writeShort(urlBytes.length);
		cacheOutStream.write(urlBytes);

		byte[] keyBytes = cacheKey.getBytes();
		cacheOutStream.writeShort(keyBytes.length);
		cacheOutStream.write(keyBytes);
		cacheOutStream.writeLong(lastTileOffset);

		lastTileOffset += 12 + // x, y and z
				2 + urlBytes.length + // strings and their lengths
				2 + keyBytes.length + 8 + // tile offset (long)
				4 + // image byte array length (int)
				tileData.length;

		cacheOutStream.writeInt(tileData.length);
		cacheOutStream.write(tileData);
		return true;
	}

}
