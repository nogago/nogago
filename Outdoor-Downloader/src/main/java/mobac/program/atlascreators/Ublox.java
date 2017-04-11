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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Locale;

import mobac.program.annotations.AtlasCreatorName;
import mobac.program.interfaces.MapSpace;
import mobac.utilities.Utilities;


@AtlasCreatorName("Ublox")
public class Ublox extends Ozi {

	@Override
	protected void writeMapFile() {
		FileOutputStream fout = null;
		try {
			fout = new FileOutputStream(new File(layerDir, mapName + ".mcf"));
			writeMapFile(map.getName(), fout);
		} catch (Exception e) {
			log.error("", e);
		} finally {
			Utilities.closeStream(fout);
		}
	}

	@Override
	protected void writeMapFile(String imageFileName, OutputStream stream) throws IOException {
		log.trace("Writing mcf file");
		OutputStreamWriter mcfWriter = new OutputStreamWriter(stream, TEXT_FILE_CHARSET);

		MapSpace mapSpace = mapSource.getMapSpace();

		double longitudeMin = mapSpace.cXToLon(xMin * tileSize, zoom);
		double longitudeMax = mapSpace.cXToLon((xMax + 1) * tileSize - 1, zoom);
		double latitudeMin = mapSpace.cYToLat((yMax + 1) * tileSize - 1, zoom);
		double latitudeMax = mapSpace.cYToLat(yMin * tileSize, zoom);

		int width = (xMax - xMin + 1) * tileSize;
		int height = (yMax - yMin + 1) * tileSize;

		String refFmt = "%d = %5d, %5d, %10.6f, %10.6f\r\n";

		mcfWriter.write("; I N F O\r\n");
		mcfWriter.write("; ------------------------------------------------------------\r\n");
		mcfWriter.write("; File: " + imageFileName + ".mcf\r\n");
		mcfWriter.write("; Source: " + map.getMapSource().getName() + "\r\n");
		mcfWriter.write("\r\n");
		mcfWriter.write("; R E F E R E N C E\r\n");
		mcfWriter.write("; ------------------------------------------------------------\r\n");
		mcfWriter.write("; 3 Points must be defined to calibrate a Map\r\n");
		mcfWriter.write("; Parameters:\r\n");
		mcfWriter.write("; # = index of the point (1 to 3)\r\n");
		mcfWriter.write("; x,y = image coordinates\r\n");
		mcfWriter.write("; lat,lon = world coordinates\r\n");
		mcfWriter.write("; Syntax:\r\n");
		mcfWriter.write("; # = <x>, <y>, <lon>, <lat>\r\n");
		mcfWriter.write("\r\n");

		mcfWriter.write("[REFERENCE]\r\n");
		mcfWriter.write(String.format(Locale.ENGLISH, refFmt, 1, 0, 0, longitudeMin, latitudeMax));
		mcfWriter.write(String.format(Locale.ENGLISH, refFmt, 2, width, height, longitudeMax,
				latitudeMin));
		mcfWriter.write(String.format(Locale.ENGLISH, refFmt, 3, width, 0, longitudeMax,
				latitudeMax));
		mcfWriter.flush();
	}
}
