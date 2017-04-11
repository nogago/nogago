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
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

import javax.imageio.ImageIO;

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
import mobac.program.model.TileImageParameters.Name;
import mobac.utilities.Utilities;

/**
 * AtlasCreator for MAPLORER ( http://maplorer.com )
 * 
 * @author Werner Keilholz
 */
@AtlasCreatorName("Maplorer atlas format")
@SupportedParameters(names = { Name.format })
public class Maplorer extends AtlasCreator {

	private static final String FILENAME_PATTERN = "map_%s%d.%s";

	protected File layerFolder = null;
	protected File mapFolder = null;
	protected MapTileWriter mapTileWriter;

	protected int tileXmax = 0;
	protected int tileYmax = 0;

	public boolean testMapSource(MapSource mapSource) {
		MapSpace mapSpace = mapSource.getMapSpace();
		return (mapSpace instanceof MercatorPower2MapSpace && ProjectionCategory.SPHERE.equals(mapSpace
				.getProjectionCategory()));
	}

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

	public void createMap() throws MapCreationException, InterruptedException {
		try {
			Utilities.mkDirs(mapFolder);

			mapTileWriter = new FileTileWriter();

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

	@Override
	public void initializeMap(MapInterface map, TileProvider mapTileProvider) {
		super.initializeMap(map, mapTileProvider);
		LayerInterface layer = map.getLayer();
		layerFolder = new File(atlasDir, layer.getName());
		mapFolder = new File(layerFolder, map.getName());
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

	public class FileTileWriter implements MapTileWriter {

		File setFolder;

		int tileHeight = 256;
		int tileWidth = 256;

		public FileTileWriter() throws IOException {
			super();
			setFolder = mapFolder; // don't need an extra sub folder for MAPLORER maps
			log.debug("Writing tiles to set folder: " + setFolder);

			if (parameters != null) {
				tileHeight = parameters.getHeight();
				tileWidth = parameters.getWidth();
			}
		}

		public void writeTile(int tilex, int tiley, String imageFormat, byte[] tileData) throws IOException {
			String tileFileName = String.format(FILENAME_PATTERN, IntToLetter(tilex + 1), tiley + 1, imageFormat);

			File f = new File(setFolder, tileFileName);
			FileOutputStream out = new FileOutputStream(f);
			try {
				out.write(tileData);
			} finally {
				Utilities.closeStream(out);
			}

			tileXmax = tilex;
			tileYmax = tiley;
		}

		private String IntToLetter(int i) throws IOException {
			if (i > 26)
				throw new IOException("Maximum tile column overflow - map too wide!");
			char c = (char) (i + 64);
			return Character.toString(c);
		}

		public void finalizeMap() throws IOException {

			MapSpace mapSpace = mapSource.getMapSpace();

			// compute corner coordinates for the entire map (all .JPG files)
			double longitudeMin = mapSpace.cXToLon(xMin * tileSize, zoom);
			double longitudeMax = mapSpace.cXToLon((xMax + 1) * tileSize - 1, zoom);
			double latitudeMin = mapSpace.cYToLat((yMax + 1) * tileSize - 1, zoom);
			double latitudeMax = mapSpace.cYToLat(yMin * tileSize, zoom);

			double widthInPixel = map.getMaxTileCoordinate().x - map.getMinTileCoordinate().x;
			double heihgtInPixel = map.getMaxTileCoordinate().y - map.getMinTileCoordinate().y;

			double pix2longitude = (longitudeMax - longitudeMin) / widthInPixel;
			double pix2latitude = (latitudeMax - latitudeMin) / heihgtInPixel;

			// compute corner coordinates for one tile (one JPG file)
			double lonBL = longitudeMin;
			double latBL = latitudeMax - tileHeight * pix2latitude;
			double lonTR = longitudeMin + tileWidth * pix2longitude;
			double latTR = latitudeMax;

			// write POS files
			for (int tilex = 0; tilex <= tileXmax; tilex++) {
				latBL = latitudeMax - tileHeight * pix2latitude;
				latTR = latitudeMax;

				for (int tiley = 0; tiley <= tileYmax; tiley++) {
					String posFileName = String.format(FILENAME_PATTERN, IntToLetter(tilex + 1), tiley + 1, "POS");
					File posFile = new File(setFolder, posFileName);

					lonBL = Math.max(longitudeMin, lonBL); // for tiles at the edges (might be smaller)
					latBL = Math.max(latitudeMin, latBL);
					lonTR = Math.min(longitudeMax, lonTR);
					latTR = Math.min(latitudeMax, latTR);

					FileWriter outFile = new FileWriter(posFile);
					try {
						PrintWriter out2 = new PrintWriter(outFile);

						String posLine = "LonBL = %2.6f";
						out2.println(String.format(Locale.ENGLISH, posLine, lonBL));

						posLine = "LatBL = %2.6f";
						out2.println(String.format(Locale.ENGLISH, posLine, latBL));

						posLine = "LonTR = %2.6f";
						out2.println(String.format(Locale.ENGLISH, posLine, lonTR));

						posLine = "LatTR = %2.6f";
						out2.println(String.format(Locale.ENGLISH, posLine, latTR));
						out2.close();
					} finally {
						Utilities.closeWriter(outFile);
					}

					latBL = latBL - tileHeight * pix2latitude;
					latTR = latTR - tileHeight * pix2latitude;

				} // for y

				lonBL = lonBL + tileWidth * pix2longitude;
				lonTR = lonTR + tileWidth * pix2longitude;

			} // for x

		}
	}
}
