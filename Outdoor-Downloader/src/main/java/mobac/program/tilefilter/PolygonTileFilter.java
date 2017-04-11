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

import java.awt.Polygon;

import mobac.program.interfaces.MapSource;
import mobac.program.interfaces.TileFilter;
import mobac.program.model.MapPolygon;

public class PolygonTileFilter implements TileFilter {

	private final Polygon polygon;
	private final int tileSize;
	private final int polygonZoom;

	public PolygonTileFilter(MapPolygon map) {
		this(map.getPolygon(), map.getZoom(), map.getMapSource());
	}

	public PolygonTileFilter(Polygon polygon, int polygonZoom, MapSource mapSource) {
		super();
		this.polygon = polygon;
		this.polygonZoom = polygonZoom;
		this.tileSize = mapSource.getMapSpace().getTileSize();
	}

	public boolean testTile(int x, int y, int zoom, MapSource mapSource) {
		if (polygonZoom != zoom)
			throw new RuntimeException("Wrong zoom level!");
		int tileCoordinateX = x * tileSize;
		int tileCoordinateY = y * tileSize;
		return polygon.intersects(tileCoordinateX, tileCoordinateY, tileSize, tileSize);
	}

}
