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

import mobac.program.interfaces.MapSource;

import org.apache.log4j.Logger;

/**
 * Base implementation of an {@link TileProvider} that changes somehow the images, e.g. combines two layers to one or
 * paints something onto a tile image.
 */
public abstract class FilterTileProvider implements TileProvider {

	protected final Logger log;

	protected final TileProvider tileProvider;

	public FilterTileProvider(TileProvider tileProvider) {
		log = Logger.getLogger(this.getClass());
		this.tileProvider = tileProvider;
	}

	public BufferedImage getTileImage(int x, int y) throws IOException {
		return tileProvider.getTileImage(x, y);
	}

	public byte[] getTileData(int x, int y) throws IOException {
		return tileProvider.getTileData(x, y);
	}

	public MapSource getMapSource() {
		return tileProvider.getMapSource();
	}

}
