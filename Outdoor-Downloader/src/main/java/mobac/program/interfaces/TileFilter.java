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
package mobac.program.interfaces;

/**
 * Basic interface that allows to filter tiles based on their position and zoom level in the map.
 */
public interface TileFilter {

	/**
	 * Tests if the tile specified by the parameters should be included or excluded
	 * 
	 * @param x
	 * @param y
	 * @param zoom
	 * @param mapSource
	 * @return <ul>
	 *         <li><code>true</code>: tile did pass the filter and should be included</li>
	 *         <li><code>false</code>:tile did not pass the filter and should be excluded</li>
	 *         </ul>
	 */
	public boolean testTile(int x, int y, int zoom, MapSource mapSource);
}
