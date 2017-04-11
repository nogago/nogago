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
import java.io.OutputStreamWriter;

import mobac.exceptions.MapCreationException;
import mobac.program.annotations.AtlasCreatorName;
import mobac.program.atlascreators.tileprovider.TileProvider;
import mobac.program.interfaces.MapInterface;
import mobac.program.interfaces.MapSpace;
import mobac.utilities.Utilities;

@AtlasCreatorName("Glopus (PNG & KAL)")
public class Glopus extends Ozi {

	@Override
	public void initializeMap(MapInterface map, TileProvider mapTileProvider) {
		super.initializeMap(map, mapTileProvider);
		String layerName = map.getLayer().getName().replaceAll(" ", "_");
		mapName = map.getName().replaceAll(" ", "_");
		layerDir = new File(atlasDir, layerName);
	}

	@Override
	public void createMap() throws MapCreationException, InterruptedException {
		try {
			Utilities.mkDir(layerDir);
		} catch (IOException e) {
			throw new MapCreationException(map, e);
		}
		createTiles();
		writeKalFile();
	}

	private void writeKalFile() throws MapCreationException {
		FileOutputStream fout = null;
		try {
			fout = new FileOutputStream(new File(layerDir, mapName + ".kal"));
			OutputStreamWriter mapWriter = new OutputStreamWriter(fout, TEXT_FILE_CHARSET);

			MapSpace mapSpace = mapSource.getMapSpace();

			String longitudeMin = Double.toString(mapSpace.cXToLon(xMin * tileSize, zoom));
			String longitudeMax = Double.toString(mapSpace.cXToLon((xMax + 1) * tileSize, zoom));
			String latitudeMin = Double.toString(mapSpace.cYToLat((yMax + 1) * tileSize, zoom));
			String latitudeMax = Double.toString(mapSpace.cYToLat(yMin * tileSize, zoom));

			int width = (xMax - xMin + 1) * tileSize;
			int height = (yMax - yMin + 1) * tileSize;

			mapWriter.write("[Calibration Point 1]\n");
			mapWriter.write(String.format("Longitude = %s\n", longitudeMin));
			mapWriter.write(String.format("Latitude =  %s\n", latitudeMax));
			mapWriter.write("Pixel = POINT(0,0)\n");

			mapWriter.write("[Calibration Point 2]\n");
			mapWriter.write(String.format("Longitude = %s\n", longitudeMax));
			mapWriter.write(String.format("Latitude =  %s\n", latitudeMin));
			mapWriter.write(String.format("Pixel = POINT(%d,%d)\n", width, height));

			mapWriter.write("[Calibration Point 3]\n");
			mapWriter.write(String.format("Longitude = %s\n", longitudeMax));
			mapWriter.write(String.format("Latitude =  %s\n", latitudeMax));
			mapWriter.write(String.format("Pixel = POINT(%d,%d)\n", width, 0));

			mapWriter.write("[Calibration Point 4]\n");
			mapWriter.write(String.format("Longitude = %s\n", longitudeMin));
			mapWriter.write(String.format("Latitude =  %s\n", latitudeMin));
			mapWriter.write(String.format("Pixel = POINT(%d,%d)\n", 0, height));

			mapWriter.write("[Map]\n");
			mapWriter.write(String.format("Bitmap = %s.png\n", mapName));
			mapWriter.write(String.format("Size = SIZE(%d,%d)\n", width, height));

			mapWriter.flush();
			mapWriter.close();
		} catch (IOException e) {
			throw new MapCreationException(map, e);
		} finally {
			Utilities.closeStream(fout);
		}
	}
}
