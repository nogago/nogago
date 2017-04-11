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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Locale;

import javax.imageio.ImageIO;

import mobac.exceptions.AtlasTestException;
import mobac.exceptions.MapCreationException;
import mobac.mapsources.mapspace.MercatorPower2MapSpace;
import mobac.program.annotations.AtlasCreatorName;
import mobac.program.annotations.SupportedParameters;
import mobac.program.atlascreators.impl.MapTileBuilder;
import mobac.program.atlascreators.impl.MapTileWriter;
import mobac.program.atlascreators.tileprovider.CacheTileProvider;
import mobac.program.atlascreators.tileprovider.TileProvider;
import mobac.program.interfaces.AtlasInterface;
import mobac.program.interfaces.LayerInterface;
import mobac.program.interfaces.MapInterface;
import mobac.program.interfaces.MapSource;
import mobac.program.interfaces.MapSpace;
import mobac.program.interfaces.MapSpace.ProjectionCategory;
import mobac.program.model.TileImageParameters.Name;
import mobac.utilities.Utilities;
import mobac.utilities.geo.GeoUtils;

@AtlasCreatorName(value = "TrekBuddy untared atlas", type = "UntaredAtlas")
@SupportedParameters(names = { Name.format, Name.height, Name.width })
public class TrekBuddy extends AtlasCreator {

	protected static final String FILENAME_PATTERN = "t_%d_%d.%s";

	protected File layerDir = null;
	protected File mapDir = null;
	protected MapTileWriter mapTileWriter;

	@Override
	public boolean testMapSource(MapSource mapSource) {
		MapSpace mapSpace = mapSource.getMapSpace();
		return (mapSpace instanceof MercatorPower2MapSpace && ProjectionCategory.SPHERE.equals(mapSpace
				.getProjectionCategory()));
		// TODO supports Mercator ellipsoid?
	}

	public void startAtlasCreation(AtlasInterface atlas, File customAtlasDir) throws IOException, InterruptedException,
			AtlasTestException {
		super.startAtlasCreation(atlas, customAtlasDir);
	}

	public void finishAtlasCreation() {
		createAtlasTbaFile("cr");
	}

	@Override
	public void initializeMap(MapInterface map, TileProvider mapTileProvider) {
		super.initializeMap(map, mapTileProvider);
		LayerInterface layer = map.getLayer();
		layerDir = new File(atlasDir, layer.getName());
		mapDir = new File(layerDir, map.getName());
	}

	protected void writeMapFile() throws IOException {
		File mapFile = new File(mapDir, map.getName() + ".map");
		FileOutputStream mapFileStream = null;
		try {
			mapFileStream = new FileOutputStream(mapFile);
			writeMapFile(mapFileStream);
		} finally {
			Utilities.closeStream(mapFileStream);
		}
	}

	protected void writeMapFile(OutputStream stream) throws IOException {
		writeMapFile("t_." + mapSource.getTileImageType().getFileExt(), stream);
	}

	protected void writeMapFile(String imageFileName, OutputStream stream) throws IOException {
		log.trace("Writing map file");
		OutputStreamWriter mapWriter = new OutputStreamWriter(stream, TEXT_FILE_CHARSET);

		MapSpace mapSpace = mapSource.getMapSpace();

		double longitudeMin = mapSpace.cXToLon(xMin * tileSize, zoom);
		double longitudeMax = mapSpace.cXToLon((xMax + 1) * tileSize - 1, zoom);
		double latitudeMin = mapSpace.cYToLat((yMax + 1) * tileSize - 1, zoom);
		double latitudeMax = mapSpace.cYToLat(yMin * tileSize, zoom);

		int width = (xMax - xMin + 1) * tileSize;
		int height = (yMax - yMin + 1) * tileSize;

		mapWriter.write(prepareMapString(imageFileName, longitudeMin, longitudeMax, latitudeMin, latitudeMax, width,
				height));
		mapWriter.flush();
	}

	public void createMap() throws MapCreationException, InterruptedException {
		try {
			Utilities.mkDirs(mapDir);

			// write the .map file containing the calibration points
			writeMapFile();

			// This means there should not be any resizing of the tiles.
			mapTileWriter = createMapTileWriter();

			// Select the tile creator instance based on whether tile image
			// parameters has been set or not
			if (parameters != null)
				createCustomTiles();
			else
				createTiles();

			mapTileWriter.finalizeMap();
		} catch (MapCreationException e) {
			throw e;
		} catch (InterruptedException e) {
			throw e;
		} catch (Exception e) {
			throw new MapCreationException(map, e);
		}
	}

	protected MapTileWriter createMapTileWriter() throws IOException {
		return new FileTileWriter();
	}

	/**
	 * New experimental custom tile size algorithm implementation.
	 * 
	 * It creates each custom sized tile separately. Therefore each original tile (256x256) will be loaded and painted
	 * multiple times. Therefore this implementation needs much more CPU power as each original tile is loaded at least
	 * once and each generated tile has to be saved.
	 * 
	 * @throws MapCreationException
	 */
	protected void createCustomTiles() throws InterruptedException, MapCreationException {
		log.debug("Starting map creation using custom parameters: " + parameters);

		CacheTileProvider ctp = new CacheTileProvider(mapDlTileProvider);
		try {
			mapDlTileProvider = ctp;

			MapTileBuilder mapTileBuilder = new MapTileBuilder(this, mapTileWriter, true);
			atlasProgress.initMapCreation(mapTileBuilder.getCustomTileCount());
			mapTileBuilder.createTiles();
		} finally {
			ctp.cleanup();
		}
	}

	protected void createTiles() throws InterruptedException, MapCreationException {
		int tilex = 0;
		int tiley = 0;

		atlasProgress.initMapCreation((xMax - xMin + 1) * (yMax - yMin + 1));

		ImageIO.setUseCache(false);
		byte[] emptyTileData = Utilities.createEmptyTileData(mapSource);
		String tileType = mapSource.getTileImageType().getFileExt();
		for (int x = xMin; x <= xMax; x++) {
			tiley = 0;
			for (int y = yMin; y <= yMax; y++) {
				checkUserAbort();
				atlasProgress.incMapCreationProgress();
				try {
					byte[] sourceTileData = mapDlTileProvider.getTileData(x, y);
					if (sourceTileData != null) {
						mapTileWriter.writeTile(tilex, tiley, tileType, sourceTileData);
					} else {
						log.trace(String.format("Tile x=%d y=%d not found in tile archive - creating default", tilex,
								tiley));
						mapTileWriter.writeTile(tilex, tiley, tileType, emptyTileData);
					}
				} catch (IOException e) {
					throw new MapCreationException("Error writing tile image: " + e.getMessage(), map, e);
				}
				tiley++;
			}
			tilex++;
		}
	}

	private class FileTileWriter implements MapTileWriter {

		File setFolder;
		Writer setFileWriter;

		int tileHeight = 256;
		int tileWidth = 256;

		public FileTileWriter() throws IOException {
			super();
			setFolder = new File(mapDir, "set");
			Utilities.mkDir(setFolder);
			log.debug("Writing tiles to set folder: " + setFolder);
			File setFile = new File(mapDir, map.getName() + ".set");
			if (parameters != null) {
				tileHeight = parameters.getHeight();
				tileWidth = parameters.getWidth();
			}
			try {
				setFileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(setFile),
						TEXT_FILE_CHARSET));
			} catch (IOException e) {
				log.error("", e);
			}
		}

		public void writeTile(int tilex, int tiley, String imageFormat, byte[] tileData) throws IOException {
			String tileFileName = String.format(FILENAME_PATTERN, (tilex * tileWidth), (tiley * tileHeight),
					imageFormat);

			File f = new File(setFolder, tileFileName);
			FileOutputStream out = new FileOutputStream(f);
			setFileWriter.write(tileFileName + "\r\n");
			try {
				out.write(tileData);
			} finally {
				Utilities.closeStream(out);
			}
		}

		public void finalizeMap() {
			try {
				setFileWriter.flush();
			} catch (IOException e) {
				log.error("", e);
			}
			Utilities.closeWriter(setFileWriter);
		}
	}

	protected String prepareMapString(String fileName, double longitudeMin, double longitudeMax, double latitudeMin,
			double latitudeMax, int width, int height) {

		StringBuffer sbMap = new StringBuffer();

		sbMap.append("OziExplorer Map Data File Version 2.2\r\n");
		sbMap.append(fileName + "\r\n");
		sbMap.append(fileName + "\r\n");
		sbMap.append("1 ,Map Code,\r\n");
		sbMap.append("WGS 84,WGS 84,   0.0000,   0.0000,WGS 84\r\n");
		sbMap.append("Reserved 1\r\n");
		sbMap.append("Reserved 2\r\n");
		sbMap.append("Magnetic Variation,,,E\r\n");
		sbMap.append("Map Projection,Mercator,PolyCal,No," + "AutoCalOnly,No,BSBUseWPX,No\r\n");

		String latMax = GeoUtils.getDegMinFormat(latitudeMax, true);
		String latMin = GeoUtils.getDegMinFormat(latitudeMin, true);
		String lonMax = GeoUtils.getDegMinFormat(longitudeMax, false);
		String lonMin = GeoUtils.getDegMinFormat(longitudeMin, false);

		String pointLine = "Point%02d,xy, %4s, %4s,in, deg, %1s, %1s, grid, , , ,N\r\n";

		sbMap.append(String.format(pointLine, 1, 0, 0, latMax, lonMin));
		sbMap.append(String.format(pointLine, 2, width - 1, 0, latMax, lonMax));
		sbMap.append(String.format(pointLine, 3, width - 1, height - 1, latMin, lonMax));
		sbMap.append(String.format(pointLine, 4, 0, height - 1, latMin, lonMin));

		for (int i = 5; i <= 30; i++) {
			String s = String.format(pointLine, i, "", "", "", "");
			sbMap.append(s);
		}
		sbMap.append("Projection Setup,,,,,,,,,,\r\n");
		sbMap.append("Map Feature = MF ; Map Comment = MC     These follow if they exist\r\n");
		sbMap.append("Track File = TF      These follow if they exist\r\n");
		sbMap.append("Moving Map Parameters = MM?    These follow if they exist\r\n");

		sbMap.append("MM0,Yes\r\n");
		sbMap.append("MMPNUM,4\r\n");

		String mmpxLine = "MMPXY, %d, %5d, %5d\r\n";

		sbMap.append(String.format(mmpxLine, 1, 0, 0));
		sbMap.append(String.format(mmpxLine, 2, width - 1, 0));
		sbMap.append(String.format(mmpxLine, 3, width - 1, height - 1));
		sbMap.append(String.format(mmpxLine, 4, 0, height - 1));

		String mpllLine = "MMPLL, %d, %2.6f, %2.6f\r\n";

		sbMap.append(String.format(Locale.ENGLISH, mpllLine, 1, longitudeMin, latitudeMax));
		sbMap.append(String.format(Locale.ENGLISH, mpllLine, 2, longitudeMax, latitudeMax));
		sbMap.append(String.format(Locale.ENGLISH, mpllLine, 3, longitudeMax, latitudeMin));
		sbMap.append(String.format(Locale.ENGLISH, mpllLine, 4, longitudeMin, latitudeMin));

		sbMap.append("MOP,Map Open Position,0,0\r\n");

		// The simple variant for calculating mm1b
		// http://www.trekbuddy.net/forum/viewtopic.php?t=3755&postdays=0&postorder=asc&start=286
		double mm1b = (longitudeMax - longitudeMin) * 111319;
		mm1b *= Math.cos(Math.toRadians((latitudeMax + latitudeMin) / 2.0)) / width;

		sbMap.append(String.format(Locale.ENGLISH, "MM1B, %2.6f\r\n", mm1b));

		sbMap.append("IWH,Map Image Width/Height, " + width + ", " + height + "\r\n");

		return sbMap.toString();
	}

	public void createAtlasTbaFile(String name) {
		File crtba = new File(atlasDir.getAbsolutePath(), name + ".tba");
		try {
			FileWriter fw = new FileWriter(crtba);
			fw.write("Atlas 1.0\r\n");
			fw.close();
		} catch (IOException e) {
			log.error("", e);
		}
	}

}
