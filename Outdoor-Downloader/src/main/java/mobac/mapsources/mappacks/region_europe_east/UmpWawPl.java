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
package mobac.mapsources.mappacks.region_europe_east;

import mobac.mapsources.AbstractHttpMapSource;
import mobac.program.model.TileImageType;

/**
 * Darmowa Mapa Polski dla GPS Garmin - UMP-pcPL (added by "maniek-ols")
 * <p>
 * <a href="http://ump.waw.pl">ump.waw.pl</a>
 * </p>
 */
public class UmpWawPl extends AbstractHttpMapSource {

	private static int SERVER_NUM = 0;
	private static final int MAX_SERVER_NUM = 4;

	public UmpWawPl() {
		super("UMP-pcPL", 0, 18, TileImageType.PNG, TileUpdate.LastModified);
	}

	public String getTileUrl(int zoom, int tilex, int tiley) {
		String s = "http://" + SERVER_NUM + ".tiles.ump.waw.pl/ump_tiles/" + zoom + "/" + tilex + "/" + tiley + ".png";
		SERVER_NUM = (SERVER_NUM + 1) % MAX_SERVER_NUM;
		return s;
	}

	@Override
	public String toString() {
		return getName() + " (Poland only)";
	}

}
