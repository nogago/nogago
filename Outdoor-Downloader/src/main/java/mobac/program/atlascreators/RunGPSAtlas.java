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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;

import mobac.exceptions.AtlasTestException;
import mobac.exceptions.MapCreationException;
import mobac.mapsources.mapspace.MercatorPower2MapSpace;
import mobac.program.annotations.AtlasCreatorName;
import mobac.program.atlascreators.impl.rungps.RunGPSAtlasFile;
import mobac.program.atlascreators.tileprovider.ConvertedRawTileProvider;
import mobac.program.atlascreators.tileprovider.TileProvider;
import mobac.program.interfaces.AtlasInterface;
import mobac.program.interfaces.MapInterface;
import mobac.program.interfaces.MapSource;
import mobac.program.model.TileImageFormat;
import mobac.program.model.TileImageType;

/**
 * Creates maps using the Run.GPS Trainer atlas format.
 * 
 * Please note that this atlas format ignores the defined atlas structure.
 * 
 * <p>
 * Run.GPS Atlas format has been designed to support huge collections of maps (2 GB and more) and for very fast access
 * (using a numeric index at the beginning of an atlas file). The file format can hold integer values, strings and
 * binary data. The file format is described on this page: http://www.rungps.net/wiki/RunGPSAtlasFormat (full sample
 * source code is available).
 * </p>
 */
@AtlasCreatorName(value = "Run.GPS Atlas", type = "RunGPS")
public class RunGPSAtlas extends AtlasCreator {

	protected RunGPSAtlasFile atlasIndex = null;
	protected Set<String> availableTileList = new HashSet<String>();
	protected int minZoom, maxZoom;

	@Override
	public boolean testMapSource(MapSource mapSource) {
		return MercatorPower2MapSpace.INSTANCE_256.equals(mapSource.getMapSpace());
	}

	@Override
	public void startAtlasCreation(AtlasInterface atlas, File customAtlasDir) throws IOException, InterruptedException,
			AtlasTestException {
		super.startAtlasCreation(atlas, customAtlasDir);
		String atlasName = this.atlas.getName().replace(' ', '_');
		atlasIndex = new RunGPSAtlasFile(atlasDir.getPath() + File.separatorChar + atlasName + RunGPSAtlasFile.SUFFIX,
				true);
		minZoom = Integer.MAX_VALUE;
		maxZoom = Integer.MIN_VALUE;
	}

	@Override
	public void abortAtlasCreation() throws IOException {
		atlasIndex.close();
		super.abortAtlasCreation();
	}

	@Override
	public void finishAtlasCreation() throws IOException, InterruptedException {
		super.finishAtlasCreation();

		// log.debug(atlasIndex.listAll());

		// archive data

		atlasIndex.setValue("/0/0/0", 3L); // file format version
		atlasIndex.setString("/0/0/1", "Run.GPS Atlas File"); // type
		atlasIndex.setString("/0/0/2", atlas.getName()); // atlas name
		atlasIndex.setString("/0/0/3", mapSource.getName()); // map source name
		atlasIndex.setString("/0/0/4", "Mobile Atlas Creator"); // created by

		// metadata

		atlasIndex.setValue("/0/1/1", minZoom);
		atlasIndex.setValue("/0/1/2", maxZoom);

		// create file

		atlasIndex.finishArchive();

		atlasIndex.close();
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
					if (sourceTileData != null) {
						writeTile(mapName, sourceTileData, x, y, zoom);
					}
				} catch (IOException e) {
					throw new MapCreationException("Error writing tile image: " + e.getMessage(), map, e);
				}
			}
		}
	}

	protected boolean writeTile(String cache, byte[] tileData, int x, int y, int zoom) throws IOException {
		if (zoom < minZoom)
			minZoom = zoom;
		if (zoom > maxZoom)
			maxZoom = zoom;

		String cacheKey = cache + "-" + zoom + "-" + x + "-" + y;

		if (availableTileList.contains(cacheKey)) {
			log.warn("Map tile already in cache: " + cacheKey + " -> ignoring");
			return false;
		}

		ArrayList<Integer> hierarchy = new ArrayList<Integer>();
		hierarchy.add(zoom);
		hierarchy.add(x);
		hierarchy.add(y);
		atlasIndex.addData(hierarchy, tileData);

		return true;
	}
}
