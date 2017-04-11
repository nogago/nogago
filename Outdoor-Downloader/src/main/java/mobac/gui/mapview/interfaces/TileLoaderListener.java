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
package mobac.gui.mapview.interfaces;

import mobac.gui.mapview.MemoryTileCache;
import mobac.gui.mapview.Tile;

//License: GPL. Copyright 2008 by Jan Peter Stotz

public interface TileLoaderListener {

	/**
	 * Will be called if a new {@link Tile} has been loaded successfully. 
	 * Loaded can mean downloaded or loaded from file cache. 
	 * 
	 * @param tile
	 */
	public void tileLoadingFinished(Tile tile, boolean success);

	public MemoryTileCache getTileImageCache();
}
