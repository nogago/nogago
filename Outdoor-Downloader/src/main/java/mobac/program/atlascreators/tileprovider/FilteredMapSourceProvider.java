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
package mobac.program.atlascreators.tileprovider;

import java.awt.image.BufferedImage;
import java.io.IOException;

import mobac.program.interfaces.MapInterface;
import mobac.program.interfaces.MapSource;
import mobac.program.interfaces.MapSource.LoadMethod;
import mobac.program.interfaces.TileFilter;

/**
 * Based on a given {@link TileFilter} implementation an {@link FilteredMapSourceProvider} instance ignores certain
 * requests via {@link #getTileData(int, int)} and {@link #getTileImage(int, int)} and returns <code>null</code>
 * instead.
 * 
 * This functionality is required especially for polygonal maps where certain tiles which are located outside of the
 * polygon should be ignored.
 */
public class FilteredMapSourceProvider extends MapSourceProvider {

	protected final TileFilter tileFilter;

	public FilteredMapSourceProvider(MapInterface map, LoadMethod loadMethod) {
		this(map.getMapSource(), map.getZoom(), loadMethod, map.getTileFilter());
	}

	public FilteredMapSourceProvider(MapSource mapSource, int zoom, LoadMethod loadMethod, TileFilter tileFilter) {
		super(mapSource, zoom, loadMethod);
		this.tileFilter = tileFilter;
	}

	@Override
	public byte[] getTileData(int x, int y) throws IOException {
		if (!tileFilter.testTile(x, y, zoom, mapSource))
			return null;
		return super.getTileData(x, y);
	}

	@Override
	public BufferedImage getTileImage(int x, int y) throws IOException {
		if (!tileFilter.testTile(x, y, zoom, mapSource))
			return null;
		return super.getTileImage(x, y);
	}

}
