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
package mobac.utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mobac.gui.mapview.JMapViewer;
import mobac.mapsources.mapspace.MercatorPower2MapSpace;
import mobac.program.interfaces.MapSpace;



public class MapDataFileParser {

	private static final String K = "[\\s ]*,[\\s ]*";

	private static final String HEAD_REGEX = "^OziExplorer Map Data File Version (\\d\\.\\d)";
	private static final String POINT_REGEX = "^Point(\\d\\d)@xy@(\\d+)@(\\d+)@in@deg@"
			+ "(\\d+)@(\\d+(?:\\.\\d+)?)@(N|S)@(\\d+)@(\\d+(?:\\.\\d+)?)@(E|W)@grid@@@" + ".*";
	private static final String MMPLL_REGEX = "^MMPLL,(\\d)@(\\d+\\.\\d+)@(\\d+\\.\\d+)";

	public MapDataFileParser(File mapFile) throws IOException, MapFileFormatException {
		this(new FileInputStream(mapFile));
	}

	public MapDataFileParser(InputStream in) throws IOException, MapFileFormatException {
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String line = br.readLine();
		Matcher m = Pattern.compile(HEAD_REGEX).matcher(line);
		if (!m.matches())
			throw new MapFileFormatException("OziExplorer Map Data File Version not found");
		String fileVersion = m.group(1);
		line = br.readLine();
		// System.out.println(POINT_REGEX.replace("@", K));
		Pattern pointP = Pattern.compile(POINT_REGEX.replaceAll("\\@", K));
		Pattern mmpllP = Pattern.compile(MMPLL_REGEX.replaceAll("\\@", K));
		ArrayList<MapPoint> mapPoints = new ArrayList<MapPoint>();
		while (line != null) {
			String cLine = line;
			line = br.readLine();
			m = pointP.matcher(cLine);
			if (m.matches()) {
				MapPoint p = new MapPoint();
				// p.num = Integer.parseInt(m.group(1));
				p.x = Integer.parseInt(m.group(2));
				p.y = Integer.parseInt(m.group(3));
				double lat = Integer.parseInt(m.group(4)) + (Double.parseDouble(m.group(5)) / 60.0);
				if ("S".equalsIgnoreCase(m.group(6)))
					lat = -lat;
				p.lat = lat;
				double lon = Integer.parseInt(m.group(7)) + (Double.parseDouble(m.group(8)) / 60.0);
				if ("W".equalsIgnoreCase(m.group(9)))
					lon = -lon;
				p.lon = lon;
				mapPoints.add(p);
				// System.out.println(m.group(0));
				continue;
			}
			m = mmpllP.matcher(cLine);
			if (m.matches()) {
				MapPoint p = new MapPoint();
				p.lon = Double.parseDouble(m.group(2));
				p.lat = Double.parseDouble(m.group(3));
				mapPoints.add(p);
			}

		}
		System.out.println("File version: " + fileVersion);
		// System.out.println("Callibration / map border points:");
		double lat_max = Double.MIN_VALUE;
		double lon_max = Double.MIN_VALUE;
		double lat_min = Double.MAX_VALUE;
		double lon_min = Double.MAX_VALUE;
		for (MapPoint p : mapPoints) {
			// System.out.println("\t" + p);
			lat_max = Math.max(lat_max, p.lat);
			lon_max = Math.max(lon_max, p.lon);
			lat_min = Math.min(lat_min, p.lat);
			lon_min = Math.min(lon_min, p.lon);
		}
		System.out.println(String.format("Max point (lat/lon): %4f %4f", lat_max, lon_max));
		System.out.println(String.format("Min point (lat/lon): %4f %4f", lat_min, lon_min));

		MapSpace ms = MercatorPower2MapSpace.INSTANCE_256;
		int x1 = ms.cLonToX(lon_max, JMapViewer.MAX_ZOOM);
		int x2 = ms.cLonToX(lon_min, JMapViewer.MAX_ZOOM);
		int diff = Math.abs(x1 - x2);
		for (int i = 1; i < 10; i++) {
			System.out.println((JMapViewer.MAX_ZOOM - i) + " : " + diff);
			diff /= 2;
		}
		br.close();
	}

	public static class MapPoint {
		// int num;
		int x;
		int y;
		double lat;
		double lon;

		public String toString() {
			return String.format("%6f %6f", lat, lon);
		}
	}

	public static class MapFileFormatException extends Exception {

		private static final long serialVersionUID = 1L;

		public MapFileFormatException(String message, Throwable cause) {
			super(message, cause);
		}

		public MapFileFormatException(String message) {
			super(message);
		}

	}

	public static void main(String[] args) {
		try {
			File f = new File("atlases/Test_2009-10-01_164148/Test1/Test1 14/Test1 14.map");
			MapDataFileParser p = new MapDataFileParser(f);
			System.out.println(p);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
