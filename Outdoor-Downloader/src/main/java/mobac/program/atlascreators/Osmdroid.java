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
import java.text.SimpleDateFormat;
import java.util.Date;

import mobac.exceptions.AtlasTestException;
import mobac.exceptions.MapCreationException;
import mobac.program.annotations.AtlasCreatorName;
import mobac.program.atlascreators.impl.MapTileWriter;
import mobac.program.atlascreators.tileprovider.TileProvider;
import mobac.program.interfaces.AtlasInterface;
import mobac.program.interfaces.MapInterface;
import mobac.program.model.Settings;
import mobac.utilities.Utilities;
import mobac.utilities.stream.ZipStoreOutputStream;

/**
 * 
 * @see http://sourceforge.net/tracker/?func=detail&atid=1105497&aid=3078443&group_id=238075
 */
@AtlasCreatorName("Osmdroid ZIP")
public class Osmdroid extends OSMTracker {

	protected ZipStoreOutputStream zipStream = null;
	protected String currentMapStoreName = null;

	public void createMap() throws MapCreationException, InterruptedException {
		createTiles();
	}

	@Override
	public void startAtlasCreation(AtlasInterface atlas, File customAtlasDir) throws AtlasTestException, IOException,
			InterruptedException {
		if (customAtlasDir == null)
			customAtlasDir = Settings.getInstance().getAtlasOutputDirectory();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
		String atlasDirName = atlas.getName() + "_" + sdf.format(new Date());
		super.startAtlasCreation(atlas, customAtlasDir);
		zipStream = new ZipStoreOutputStream(new File(atlasDir, atlasDirName + ".zip"));
		mapTileWriter = new OSMDroidTileWriter();
	}

	@Override
	public void abortAtlasCreation() throws IOException {
		Utilities.closeStream(zipStream);
		super.abortAtlasCreation();
	}

	@Override
	public void finishAtlasCreation() throws IOException, InterruptedException {
		Utilities.closeStream(zipStream);
		super.finishAtlasCreation();
	}

	@Override
	public void initializeMap(MapInterface map, TileProvider mapTileProvider) {
		super.initializeMap(map, mapTileProvider);
		currentMapStoreName = map.getMapSource().getName();
		if (currentMapStoreName.equals("TilesAtHome"))
			currentMapStoreName = "Osmarender";
		else if (currentMapStoreName.equals("OSM Cycle Map"))
			currentMapStoreName = "CycleMap";
	}

	private class OSMDroidTileWriter implements MapTileWriter {

		public void finalizeMap() throws IOException {
		}

		public void writeTile(int tilex, int tiley, String tileType, byte[] tileData) throws IOException {
			String tileName = currentMapStoreName + "/"
					+ String.format(tileFileNamePattern, zoom, tilex, tiley, tileType);
			zipStream.writeStoredEntry(tileName, tileData);
		}
	}
}
