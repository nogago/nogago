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
package mobac.program.atlascreators.impl;

import java.io.IOException;

public interface MapTileWriter {

	/**
	 * 
	 * @param tilex
	 *            x tile number regarding regarding the currently processed map
	 *            (0..mapWidth / tileWidth)]
	 * @param tiley
	 *            y tile number regarding regarding the currently processed map
	 *            (0..mapheight / tileHeight)]
	 * @param tileType
	 * @param tileData
	 * @throws IOException
	 */
	public void writeTile(int tilex, int tiley, String tileType, byte[] tileData)
			throws IOException;

	public void finalizeMap() throws IOException;

}
