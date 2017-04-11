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
package mobac.program.tilestore;

public class TileStoreInfo {

	int tileCount;
	long storeSize;

	public TileStoreInfo(long storeSize, int tileCount) {
		super();
		this.storeSize = storeSize;
		this.tileCount = tileCount;
	}

	/**
	 * @return Number of tiles stored in the tile store
	 */
	public int getTileCount() {
		return tileCount;
	}

	/**
	 * @return store size in bytes
	 */
	public long getStoreSize() {
		return storeSize;
	}

}
