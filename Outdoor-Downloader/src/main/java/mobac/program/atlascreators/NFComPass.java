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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import mobac.exceptions.MapCreationException;
import mobac.mapsources.mapspace.MercatorPower2MapSpace;
import mobac.program.annotations.AtlasCreatorName;
import mobac.program.annotations.SupportedParameters;
import mobac.program.atlascreators.impl.MapTileBuilder;
import mobac.program.atlascreators.impl.MapTileWriter;
import mobac.program.atlascreators.tileprovider.CacheTileProvider;
import mobac.program.atlascreators.tileprovider.TileProvider;
import mobac.program.interfaces.LayerInterface;
import mobac.program.interfaces.MapInterface;
import mobac.program.interfaces.MapSource;
import mobac.program.interfaces.MapSpace;
import mobac.program.interfaces.MapSpace.ProjectionCategory;
import mobac.program.model.TileImageFormat;
import mobac.program.model.TileImageParameters;
import mobac.program.model.TileImageParameters.Name;
import mobac.program.tiledatawriter.TileImagePngDataWriter;
import mobac.utilities.Utilities;

@AtlasCreatorName(value = "nfComPass")
@SupportedParameters(names = { Name.height, Name.width })
public class NFComPass extends AtlasCreator {

	private File layerDir;
	private File mapDir;

	@Override
	public void initializeMap(MapInterface map, TileProvider mapTileProvider) {
		super.initializeMap(map, mapTileProvider);
		LayerInterface layer = map.getLayer();
		layerDir = new File(atlasDir, layer.getName());
		mapDir = new File(layerDir, map.getName());
		if (parameters == null) {
			parameters = new TileImageParameters(64, 64, TileImageFormat.PNG);
		}
	}

	@Override
	public boolean testMapSource(MapSource mapSource) {
		MapSpace mapSpace = mapSource.getMapSpace();
		return (mapSpace instanceof MercatorPower2MapSpace && ProjectionCategory.SPHERE.equals(mapSpace
				.getProjectionCategory()));
	}

	@Override
	public void initLayerCreation(LayerInterface layer) throws IOException {
		super.initLayerCreation(layer);
		if (layer.getMapCount() == 0)
			return;
		int lastZoom = layer.getMap(0).getZoom();
		Writer w = new BufferedWriter(new FileWriter(new File(atlasDir, "nfComPass.dat"), true));
		w.append("[" + layer.getName() + "]\r\n");
		w.append("SIZEXY = extern\r\n");
		w.append("MAPPATH =\r\n");
		w.append("VMAX = 160\r\n");
		w.append("WIDTH = 5\r\n");
		w.append("LASTZOOM = " + lastZoom + "\r\n\r\n");
		w.flush();
		w.close();
	}

	@Override
	public void createMap() throws MapCreationException, InterruptedException {
		try {
			Utilities.mkDirs(mapDir);
		} catch (IOException e) {
			throw new MapCreationException(map, e);
		}
		createKalFile(map);
		CacheTileProvider ctp = new CacheTileProvider(mapDlTileProvider);
		try {
			mapDlTileProvider = ctp;

			MapTileBuilder mapTileBuilder = new MapTileBuilder(this, new TileImagePngDataWriter(),
					new NFCompassTileWriter(), true);
			atlasProgress.initMapCreation(mapTileBuilder.getCustomTileCount());
			mapTileBuilder.createTiles();
		} finally {
			ctp.cleanup();
		}

	}

	protected void createKalFile(MapInterface map) throws MapCreationException {
		Writer w = null;
		MapSpace mapSpace = map.getMapSource().getMapSpace();
		double longitudeMin = mapSpace.cXToLon(xMin * tileSize, zoom);
		double longitudeMax = mapSpace.cXToLon((xMax + 1) * tileSize - 1, zoom);
		double latitudeMin = mapSpace.cYToLat((yMax + 1) * tileSize - 1, zoom);
		double latitudeMax = mapSpace.cYToLat(yMin * tileSize, zoom);

		int width = (xMax - xMin + 1) * tileSize;
		int height = (yMax - yMin + 1) * tileSize;

		try {
			w = new BufferedWriter(new FileWriter(new File(mapDir, map.getName() + ".kal"), true));
			w.append("[" + map.getName() + "]\r\n");
			w.append(String.format("TILEXY = %dx%d\r\n", parameters.getWidth(), parameters.getHeight()));
			w.append("X0LON = " + longitudeMin + "\r\n");
			w.append("Y0LAT = " + latitudeMax + "\r\n");
			w.append("X1LON = " + longitudeMax + "\r\n");
			w.append("Y1LAT = " + latitudeMin + "\r\n");
			w.append(String.format("SIZEXY = %dx%d\r\n", width, height));
			w.flush();
		} catch (IOException e) {
			throw new MapCreationException(map, e);
		} finally {
			Utilities.closeWriter(w);
		}

	}

	public class NFCompassTileWriter implements MapTileWriter {
		int tileHeight = 256;
		int tileWidth = 256;

		int ff_x;
		int ff_y;

		public NFCompassTileWriter() {
			super();
			if (parameters != null) {
				tileHeight = parameters.getHeight();
				tileWidth = parameters.getWidth();
			}
			int highest_bit_x = Utilities.getHighestBitSet(tileWidth) + 2;
			int highest_bit_y = Utilities.getHighestBitSet(tileHeight) + 2;

			ff_x = Integer.MAX_VALUE << (highest_bit_x);
			ff_y = Integer.MAX_VALUE << (highest_bit_y);
		}

		public void writeTile(int tilex, int tiley, String tileType, byte[] tileData) throws IOException {
			int x = tilex * tileWidth;
			int y = tiley * tileHeight;
			String folderName = String.format("%dx%d", (x & ff_x), (y & ff_y));
			String tileFileName = String.format("%d_%d.png", x, y);

			File folder = new File(mapDir, folderName);
			Utilities.mkDir(folder);
			File f = new File(folder, tileFileName);
			FileOutputStream out = new FileOutputStream(f);
			try {
				out.write(tileData);
			} finally {
				Utilities.closeStream(out);
			}
		}

		public void finalizeMap() throws IOException {

		}

	}
}
