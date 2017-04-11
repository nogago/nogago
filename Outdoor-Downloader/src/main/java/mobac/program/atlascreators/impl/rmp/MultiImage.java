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
package mobac.program.atlascreators.impl.rmp;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import mobac.exceptions.MapCreationException;
import mobac.program.atlascreators.tileprovider.TileProvider;
import mobac.program.interfaces.MapInterface;
import mobac.program.interfaces.MapSource;
import mobac.program.interfaces.MapSpace;
import mobac.utilities.collections.SoftHashMap;

import org.apache.log4j.Logger;

/**
 * CalibratedImage that gets its data from a set of other CalibratedImage2
 * 
 */
public class MultiImage {

	private static final Logger log = Logger.getLogger(MultiImage.class);

	private final MapInterface map;
	private final MapSource mapSource;
	private final int zoom;
	private final TileProvider tileProvider;
	private SoftHashMap<TileKey, MobacTile> cache;

	public MultiImage(MapSource mapSource, TileProvider tileProvider, MapInterface map) {
		this.mapSource = mapSource;
		this.tileProvider = tileProvider;
		this.zoom = map.getZoom();
		this.map = map;
		cache = new SoftHashMap<TileKey, MobacTile>(400);
	}

	public BufferedImage getSubImage(BoundingRect area, int width, int height) throws MapCreationException {
		if (log.isTraceEnabled())
			log.trace(String.format("getSubImage %d %d %s", width, height, area));

		MapSpace mapSpace = mapSource.getMapSpace();
		int tilesize = mapSpace.getTileSize();

		int xMax = mapSource.getMapSpace().cLonToX(area.getEast(), zoom) / tilesize;
		int xMin = mapSource.getMapSpace().cLonToX(area.getWest(), zoom) / tilesize;
		int yMax = mapSource.getMapSpace().cLatToY(-area.getSouth(), zoom) / tilesize;
		int yMin = mapSource.getMapSpace().cLatToY(-area.getNorth(), zoom) / tilesize;

		BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		Graphics2D graph = result.createGraphics();
		try {
			graph.setColor(Color.WHITE);
			graph.fillRect(0, 0, width, height);

			for (int x = xMin; x <= xMax; x++) {
				for (int y = yMin; y <= yMax; y++) {
					TileKey key = new TileKey(x, y);
					MobacTile image = cache.get(key);
					if (image == null) {
						image = new MobacTile(tileProvider, mapSpace, x, y, zoom);
						cache.put(key, image);
					}
					image.drawSubImage(area, result);
				}
			}
		} catch (Throwable t) {
			throw new MapCreationException(map, t);
		} finally {
			graph.dispose();
		}
		return result;
	}

	protected static class TileKey {
		int x;
		int y;

		public TileKey(int x, int y) {
			this.x = x;
			this.y = y;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + x;
			result = prime * result + y;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			TileKey other = (TileKey) obj;
			if (x != other.x)
				return false;
			if (y != other.y)
				return false;
			return true;
		}

	}
}
