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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Locale;

import javax.imageio.ImageIO;

import mobac.exceptions.MapCreationException;
import mobac.mapsources.mapspace.MercatorPower2MapSpace;
import mobac.program.annotations.AtlasCreatorName;
import mobac.program.annotations.SupportedParameters;
import mobac.program.atlascreators.tileprovider.TileProvider;
import mobac.program.interfaces.MapInterface;
import mobac.program.interfaces.MapSource;
import mobac.program.interfaces.MapSpace;
import mobac.program.interfaces.MapSpace.ProjectionCategory;
import mobac.utilities.Utilities;
import mobac.utilities.geo.GeoUtils;
import mobac.utilities.imageio.PngXxlWriter;

@AtlasCreatorName(value = "OziExplorer (PNG & MAP)", type = "OziPng")
@SupportedParameters(names = {})
public class Ozi extends AtlasCreator {

	protected File layerDir = null;
	protected String mapName = null;

	@Override
	public boolean testMapSource(MapSource mapSource) {
		MapSpace mapSpace = mapSource.getMapSpace();
		return (mapSpace instanceof MercatorPower2MapSpace && ProjectionCategory.SPHERE.equals(mapSpace
				.getProjectionCategory()));
		// TODO supports Mercator ellipsoid?
	}

	@Override
	public void initializeMap(MapInterface map, TileProvider mapTileProvider) {
		super.initializeMap(map, mapTileProvider);
		layerDir = new File(atlasDir, map.getLayer().getName());
		mapName = map.getName();
	}

	public void createMap() throws MapCreationException, InterruptedException {
		try {
			Utilities.mkDir(layerDir);
		} catch (IOException e) {
			throw new MapCreationException(map, e);
		}
		createTiles();
		writeMapFile();
	}

	protected void writeMapFile() {
		FileOutputStream fout = null;
		try {
			fout = new FileOutputStream(new File(layerDir, mapName + ".map"));
			writeMapFile(map.getName() + ".png", fout);
		} catch (Exception e) {
			log.error("", e);
		} finally {
			Utilities.closeStream(fout);
		}
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

		// Calculate the 100 pixel scale here for MM1B
		// Supplied by MrPete, based on suggestion from Des Newman, author of OziExplorer
		// Set latitude to midpoint (maxLat+minLat/2)
		double midLat = Math.toRadians((latitudeMax + latitudeMin) / 2.0);

		// Calculate 50 pixel Longitude for interpolation: Lon50 = 50/(ImagePixelWidth) * abs(maxLon - minLon)
		double rlonMax = Math.toRadians(longitudeMax);
		double rlonMin = Math.toRadians(longitudeMin);
		double Lon50 = (50.0 / width) * Math.abs(rlonMax - rlonMin);

		// Calculate midpoint Lon: midLon = (maxLon+minLon)/2
		double midLon = (rlonMax + rlonMin) / 2.0;

		// Set lonW and lonE to midpoint +/1 50 pixels: lonW = midLon - Lon50; lonE = midLon+Lon50
		double lonW = midLon - Lon50;
		double lonE = midLon + Lon50;

		// Now do the calculation:
		// d=2*asin(sqrt((sin((lat1-lat2)/2))^2 + cos(lat1)*cos(lat2)*(sin((lon1-lon2)/2))^2))
		// d=2*asin(sqrt(0 + cos(lat1)*cos(lat2)*(sin((lon1-lon2)/2))^2))
		double mDistA = Math.cos(midLat);
		double mDistB = Math.sin((lonW - lonE) / 2.0);
		double mDist = 2.0 * Math.asin(Math.sqrt(mDistA * mDistA * mDistB * mDistB));

		// For the final scaling, convert to distance in meters (multiply by earth radius),
		// then simply divide by 100 (100 pixels between the two reference points)
		// We're using the polar radius, as this gives results very close to OziExplorer
		double mm1b = 6399592 * mDist / 100.0;

		mapWriter.write(prepareMapString(imageFileName, longitudeMin, longitudeMax, latitudeMin, latitudeMax, width,
				height, mm1b));
		mapWriter.flush();
	}

	protected String prepareMapString(String fileName, double longitudeMin, double longitudeMax, double latitudeMin,
			double latitudeMax, int width, int height, double mm1b) {

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

		sbMap.append(String.format(Locale.ENGLISH, "MM1B, %2.6f\r\n", mm1b));

		sbMap.append("IWH,Map Image Width/Height, " + width + ", " + height + "\r\n");

		return sbMap.toString();
	}

	/**
	 * Writes the large picture (tile) line by line. Each line has the full width of the map and the height of one tile
	 * (256 pixels).
	 */
	protected void createTiles() throws InterruptedException, MapCreationException {
		atlasProgress.initMapCreation((xMax - xMin + 1) * (yMax - yMin + 1));
		ImageIO.setUseCache(false);

		int width = (xMax - xMin + 1) * tileSize;
		int height = (yMax - yMin + 1) * tileSize;
		int tileLineHeight = tileSize;

		FileOutputStream fileOs = null;
		Color backgroundColor = mapSource.getBackgroundColor();
		try {
			fileOs = new FileOutputStream(new File(layerDir, mapName + ".png"));
			PngXxlWriter pngWriter = new PngXxlWriter(width, height, fileOs);

			for (int y = yMin; y <= yMax; y++) {
				BufferedImage lineImage = new BufferedImage(width, tileLineHeight, BufferedImage.TYPE_INT_RGB);
				Graphics2D graphics = lineImage.createGraphics();
				try {
					graphics.setColor(backgroundColor);
					graphics.fillRect(0, 0, width, tileLineHeight);
					int lineX = 0;
					for (int x = xMin; x <= xMax; x++) {
						checkUserAbort();
						atlasProgress.incMapCreationProgress();
						BufferedImage tile = mapDlTileProvider.getTileImage(x, y);
						if (tile != null)
							graphics.drawImage(tile, lineX, 0, backgroundColor, null);
						lineX += tileSize;
					}
				} finally {
					graphics.dispose();
				}
				pngWriter.writeTileLine(lineImage);
			}
			pngWriter.finish();
		} catch (IOException e) {
			throw new MapCreationException(map, e);
		} finally {
			Utilities.closeStream(fileOs);
		}
	}
}
