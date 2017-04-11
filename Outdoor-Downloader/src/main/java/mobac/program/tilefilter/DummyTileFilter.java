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
package mobac.program.tilefilter;

import mobac.program.interfaces.MapSource;
import mobac.program.interfaces.TileFilter;

/**
 * Dummy tile filter - always returns <code>true</code> for any tile.
 */
public class DummyTileFilter implements TileFilter {

	public boolean testTile(int x, int y, int zoom, MapSource mapSource) {
		return true;
	}

}
