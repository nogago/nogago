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
package mobac.mapsources;

import mobac.program.interfaces.MapSource;
import mobac.program.interfaces.MapSpace;

/**
 * Utility methods used by several map sources.
 */
public class MapSourceTools {

	protected static final char[] NUM_CHAR = { '0', '1', '2', '3' };

	/**
	 * See: http://msdn.microsoft.com/en-us/library/bb259689.aspx
	 * 
	 * @param zoom
	 * @param tilex
	 * @param tiley
	 * @return quadtree encoded tile number
	 * 
	 */
	public static String encodeQuadTree(int zoom, int tilex, int tiley) {
		char[] tileNum = new char[zoom];
		for (int i = zoom - 1; i >= 0; i--) {
			// Binary encoding using ones for tilex and twos for tiley. if a bit
			// is set in tilex and tiley we get a three.
			int num = (tilex % 2) | ((tiley % 2) << 1);
			tileNum[i] = NUM_CHAR[num];
			tilex >>= 1;
			tiley >>= 1;
		}
		return new String(tileNum);
	}

	/**
	 * Calculates latitude and longitude of the upper left corner of the specified tile of <code>mapsource</code>
	 * regarding the zoom level specified by <code>zoom</code>.
	 * 
	 * @param mapSource
	 * @param zoom
	 * @param tilex
	 *            horizontal tile number
	 * @param tiley
	 *            vertical tile number
	 * @return <code>double[] {lon_min , lat_min , lon_max , lat_max}</code>
	 */
	public static double[] calculateLatLon(MapSource mapSource, int zoom, int tilex, int tiley) {
		MapSpace mapSpace = mapSource.getMapSpace();
		int tileSize = mapSpace.getTileSize();
		double[] result = new double[4];
		tilex *= tileSize;
		tiley *= tileSize;
		result[0] = mapSpace.cXToLon(tilex, zoom); // lon_min
		result[1] = mapSpace.cYToLat(tiley + tileSize, zoom); // lat_max
		result[2] = mapSpace.cXToLon(tilex + tileSize, zoom); // lon_min
		result[3] = mapSpace.cYToLat(tiley, zoom); // lat_max
		return result;
	}

	public static String formatMapUrl(String mapUrl, int zoom, int tilex, int tiley) {
		String tmp = mapUrl;
		tmp = tmp.replace("{$x}", Integer.toString(tilex));
		tmp = tmp.replace("{$y}", Integer.toString(tiley));
		tmp = tmp.replace("{$z}", Integer.toString(zoom));
		tmp = tmp.replace("{$q}", MapSourceTools.encodeQuadTree(zoom, tilex, tiley));
		return tmp;
	}

	public static String formatMapUrl(String mapUrl, int serverNum, int zoom, int tilex, int tiley) {
		String tmp = mapUrl;
		tmp = tmp.replace("{$servernum}", Integer.toString(serverNum));
		return formatMapUrl(tmp, zoom, tilex, tiley);
	}

	public static String formatMapUrl(String mapUrl, String serverPart, int zoom, int tilex, int tiley) {
		String tmp = mapUrl;
		tmp = tmp.replace("{$serverpart}", serverPart);
		return formatMapUrl(tmp, zoom, tilex, tiley);
	}
}
