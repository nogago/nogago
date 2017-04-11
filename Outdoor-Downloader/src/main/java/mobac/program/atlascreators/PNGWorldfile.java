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
import java.util.Locale;

import mobac.exceptions.MapCreationException;
import mobac.program.annotations.AtlasCreatorName;
import mobac.program.interfaces.MapSpace;
import mobac.utilities.Utilities;

/**
 * http://sourceforge.net/tracker/?func=detail&atid=1105497&aid=3147526&group_id=238075
 */
@AtlasCreatorName("PNG + Worldfile (PNG & PGW)")
public class PNGWorldfile extends Glopus {

	@Override
	public void createMap() throws MapCreationException, InterruptedException {
		try {
			Utilities.mkDir(layerDir);
		} catch (IOException e) {
			throw new MapCreationException(map, e);
		}
		createTiles();
		writeWorldFile();
		writeProjectionFile();
	}

	/**
	 * http://en.wikipedia.org/wiki/World_file
	 * 
	 * <pre>
	 * Format of Worldfile: 
	 * 			   0.000085830078125  (size of pixel in x direction)                              =(east-west)/image width
	 * 			   0.000000000000     (rotation term for row)
	 * 			   0.000000000000     (rotation term for column)
	 * 			   -0.00006612890625  (size of pixel in y direction)                              =-(north-south)/image height
	 * 			   -106.54541         (x coordinate of centre of upper left pixel in map units)   =west
	 * 			   39.622615          (y coordinate of centre of upper left pixel in map units)   =north
	 * </pre>
	 */
	private void writeWorldFile() throws MapCreationException {
		FileOutputStream fout = null;
		try {
			fout = new FileOutputStream(new File(layerDir, mapName + ".pgw"));
			OutputStreamWriter mapWriter = new OutputStreamWriter(fout, TEXT_FILE_CHARSET);

			MapSpace mapSpace = mapSource.getMapSpace();

			int width = (xMax - xMin + 1) * tileSize;
			int height = (yMax - yMin + 1) * tileSize;

			double lonMin = mapSpace.cXToLon(xMin * tileSize, zoom);
			double lonMax = mapSpace.cXToLon((xMax + 1) * tileSize, zoom);
			double latMin = mapSpace.cYToLat((yMax + 1) * tileSize, zoom);
			double latMax = mapSpace.cYToLat(yMin * tileSize, zoom);

			double originShift = 2 * Math.PI * 6378137 / 2.0;

			double xMin1 = lonMin * originShift / 180.0;
			double yMin1 = mercY(latMin);
			double xMax1 = lonMax * originShift / 180.0;
			double yMax1 = mercY(latMax);

			mapWriter.write(String.format(Locale.ENGLISH, "%.15f\n", (xMax1 - xMin1) / width));
			mapWriter.write("0.0\n");
			mapWriter.write("0.0\n");
			mapWriter.write(String.format(Locale.ENGLISH, "%.15f\n", -(yMax1 - yMin1) / height));
			mapWriter.write(String.format(Locale.ENGLISH, "%.7f\n", xMin1));
			mapWriter.write(String.format(Locale.ENGLISH, "%.7f\n", yMax1));

			mapWriter.flush();
			mapWriter.close();
		} catch (IOException e) {
			throw new MapCreationException(map, e);
		} finally {
			Utilities.closeStream(fout);
		}
	}

	private void writeProjectionFile() throws MapCreationException {
		FileOutputStream fout = null;
		try {
			fout = new FileOutputStream(new File(layerDir, mapName + ".png.aux.xml"));
			OutputStreamWriter writer = new OutputStreamWriter(fout, TEXT_FILE_CHARSET);

			writer.write("<PAMDataset><SRS>" + "PROJCS[&quot;World_Mercator&quot;,GEOGCS[&quot;GCS_WGS_1984&quot;,"
					+ "DATUM[&quot;WGS_1984&quot;,SPHEROID[&quot;WGS_1984&quot;,6378137.0,298.257223563]],"
					+ "PRIMEM[&quot;Greenwich&quot;,0.0],UNIT[&quot;Degree&quot;,0.0174532925199433]],"
					+ "PROJECTION[&quot;Mercator_1SP&quot;],PARAMETER[&quot;False_Easting&quot;,0.0],"
					+ "PARAMETER[&quot;False_Northing&quot;,0.0]," + "PARAMETER[&quot;Central_Meridian&quot;,0.0],"
					+ "PARAMETER[&quot;latitude_of_origin&quot;,0.0],"
					+ "UNIT[&quot;Meter&quot;,1.0]]</SRS></PAMDataset>");

			writer.flush();
			writer.close();
		} catch (IOException e) {
			throw new MapCreationException(map, e);
		} finally {
			Utilities.closeStream(fout);
		}
	}

	final private static double R_MAJOR = 6378137.0;
	final private static double R_MINOR = 6356752.3142;

	private double mercY(double lat) {
		if (lat > 89.5) {
			lat = 89.5;
		}
		if (lat < -89.5) {
			lat = -89.5;
		}
		double temp = R_MINOR / R_MAJOR;
		double es = 1.0 - (temp * temp);
		double eccent = Math.sqrt(es);
		double phi = Math.toRadians(lat);
		double sinphi = Math.sin(phi);
		double con = eccent * sinphi;
		double com = 0.5 * eccent;
		con = Math.pow(((1.0 - con) / (1.0 + con)), com);
		double ts = Math.tan(0.5 * ((Math.PI * 0.5) - phi)) / con;
		double y = 0 - R_MAJOR * Math.log(ts);
		return y;
	}
}