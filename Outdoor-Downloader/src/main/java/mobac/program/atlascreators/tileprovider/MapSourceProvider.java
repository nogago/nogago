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

import mobac.exceptions.TileException;
import mobac.program.interfaces.MapSource;
import mobac.program.interfaces.MapSource.LoadMethod;

/**
 * A {@link TileProvider} implementation that retrieves all tiles directly from the {@link MapSource}.
 */
public class MapSourceProvider implements TileProvider {

	protected final MapSource mapSource;
	protected final int zoom;
	protected final LoadMethod loadMethod;

	/**
	 * 
	 * @param mapSource
	 * @param zoom
	 * @param loadMethod
	 *            defines if the tile should be taken from tile cache or from it's original source (web server,
	 *            generated...).
	 */
	public MapSourceProvider(MapSource mapSource, int zoom, LoadMethod loadMethod) {
		super();
		this.mapSource = mapSource;
		this.zoom = zoom;
		this.loadMethod = loadMethod;
	}

	public byte[] getTileData(int x, int y) throws IOException {
		try {
			return mapSource.getTileData(zoom, x, y, loadMethod);
		} catch (TileException e) {
			throw new IOException(e);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public BufferedImage getTileImage(int x, int y) throws IOException {
		try {
			return mapSource.getTileImage(zoom, x, y, loadMethod);
		} catch (TileException e) {
			throw new IOException(e);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean preferTileImageUsage() {
		return false;
	}

	public MapSource getMapSource() {
		return mapSource;
	}

	public int getZoom() {
		return zoom;
	}

}
