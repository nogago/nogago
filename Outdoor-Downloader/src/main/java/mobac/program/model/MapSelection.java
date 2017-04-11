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
package mobac.program.model;

import java.awt.Point;

import mobac.gui.mapview.JMapViewer;
import mobac.mapsources.AbstractMultiLayerMapSource;
import mobac.program.interfaces.MapInterface;
import mobac.program.interfaces.MapSource;
import mobac.program.interfaces.MapSpace;

public class MapSelection {

	public static final int LAT_MAX = 85;
	public static final int LAT_MIN = -85;
	public static final int LON_MAX = 179;
	public static final int LON_MIN = -179;

	private final MapSource mapSource;
	private final MapSpace mapSpace;
	private final int mapSourceTileSize;
	private final int zoom;
	private int minTileCoordinate_x;
	private int minTileCoordinate_y;
	private int maxTileCoordinate_x;
	private int maxTileCoordinate_y;

	public MapSelection(MapSource mapSource, EastNorthCoordinate max, EastNorthCoordinate min) {
		super();
		this.mapSource = mapSource;
		this.mapSpace = mapSource.getMapSpace();
		mapSourceTileSize = this.mapSpace.getTileSize();
		zoom = JMapViewer.MAX_ZOOM;
		int x1 = mapSpace.cLonToX(min.lon, zoom);
		int x2 = mapSpace.cLonToX(max.lon, zoom);
		int y1 = mapSpace.cLatToY(min.lat, zoom);
		int y2 = mapSpace.cLatToY(max.lat, zoom);
		setCoordinates(x1, x2, y1, y2);
	}

	public MapSelection(MapInterface map) {
		this(map.getMapSource(), map.getMaxTileCoordinate(), map.getMinTileCoordinate(), map.getZoom());
	}

	/**
	 * @param mapSource
	 * @param p1
	 *            tile coordinate
	 * @param p2
	 *            tile coordinate
	 * @param zoom
	 */
	public MapSelection(MapSource mapSource, Point p1, Point p2, int zoom) {
		super();
		this.mapSource = mapSource;
		this.mapSpace = mapSource.getMapSpace();
		mapSourceTileSize = mapSpace.getTileSize();
		this.zoom = zoom;
		setCoordinates(p1.x, p2.x, p1.y, p2.y);
	}

	public MapSelection(MapSource mapSource, MercatorPixelCoordinate c1, MercatorPixelCoordinate c2) {
		if (c1.getZoom() != c2.getZoom())
			throw new RuntimeException("Different zoom levels - unsuported!");
		this.mapSource = mapSource;
		this.mapSpace = mapSource.getMapSpace();
		mapSourceTileSize = mapSpace.getTileSize();
		this.zoom = c1.getZoom();
		setCoordinates(c1.getX(), c2.getX(), c1.getY(), c2.getY());
	}

	protected void setCoordinates(int x1, int x2, int y1, int y2) {
		maxTileCoordinate_x = Math.max(x1, x2);
		minTileCoordinate_x = Math.min(x1, x2);
		maxTileCoordinate_y = Math.max(y1, y2);
		minTileCoordinate_y = Math.min(y1, y2);
	}

	/**
	 * Is an area selected or only one point?
	 * 
	 * @return
	 */
	public boolean isAreaSelected() {
		boolean result = maxTileCoordinate_x != minTileCoordinate_x && maxTileCoordinate_y != minTileCoordinate_y;
		return result;
	}

	/**
	 * Warning: maximum lat/lon is the top right corner of the map selection!
	 * 
	 * @return maximum lat/lon
	 */
	public EastNorthCoordinate getMax() {
		return new EastNorthCoordinate(mapSpace, zoom, maxTileCoordinate_x, minTileCoordinate_y);
	}

	/**
	 * Warning: minimum lat/lon is the bottom left corner of the map selection!
	 * 
	 * @return minimum lat/lon
	 */
	public EastNorthCoordinate getMin() {
		return new EastNorthCoordinate(mapSpace, zoom, minTileCoordinate_x, maxTileCoordinate_y);
	}

	/**
	 * Returns the top left tile x- and y-tile-number (minimum) of the selected area marked by the {@link MapSelection}.
	 * 
	 * @param aZoomLevel
	 * @return tile number [0..2<sup>zoom</sup>]
	 */
	public Point getTopLeftTileNumber(int aZoomlevel) {
		Point tlc = getTopLeftPixelCoordinate(aZoomlevel);
		tlc.x /= mapSourceTileSize;
		tlc.y /= mapSourceTileSize;
		return tlc;
	}

	public MercatorPixelCoordinate getTopLeftPixelCoordinate() {
		return new MercatorPixelCoordinate(mapSpace, minTileCoordinate_x, minTileCoordinate_y, zoom);
	}

	/**
	 * Returns the top left tile x- and y-tile-coordinate (minimum) of the selected area marked by the
	 * {@link MapSelection}.
	 * 
	 * @param aZoomlevel
	 * @return tile coordinate [0..(256 * 2<sup>zoom</sup>)]
	 */
	public Point getTopLeftPixelCoordinate(int aZoomlevel) {
		int zoomDiff = this.zoom - aZoomlevel;
		int x = minTileCoordinate_x;
		int y = minTileCoordinate_y;
		if (zoomDiff < 0) {
			zoomDiff = -zoomDiff;
			x <<= zoomDiff;
			y <<= zoomDiff;
		} else {
			x >>= zoomDiff;
			y >>= zoomDiff;
		}
		return new Point(x, y);
	}

	/**
	 * Returns the bottom right tile x- and y-tile-number (minimum) of the selected area marked by the
	 * {@link MapSelection}.
	 * 
	 * @param aZoomlevel
	 * @return tile number [0..2<sup>zoom</sup>]
	 */
	public Point getBottomRightTileNumber(int aZoomlevel) {
		Point brc = getBottomRightPixelCoordinate(aZoomlevel);
		brc.x = brc.x / mapSourceTileSize;
		brc.y = brc.y / mapSourceTileSize;
		return brc;
	}

	/**
	 * Returns the bottom right tile x- and y-tile-coordinate (minimum) of the selected area regarding the zoom level
	 * specified at creation time of this {@link MapSelection} instance.
	 * 
	 * @return tile coordinate [0..(256 * 2<sup>zoom</sup>)]
	 */
	public MercatorPixelCoordinate getBottomRightPixelCoordinate() {
		return new MercatorPixelCoordinate(mapSpace, maxTileCoordinate_x, maxTileCoordinate_y, zoom);
	}

	/**
	 * Returns the bottom right tile x- and y-tile-coordinate (minimum) of the selected area marked by the
	 * {@link MapSelection}.
	 * 
	 * @param aZoomlevel
	 * @return tile coordinate [0..(256 * 2<sup>zoom</sup>)]
	 */
	public Point getBottomRightPixelCoordinate(int aZoomlevel) {
		int zoomDiff = this.zoom - aZoomlevel;
		int x = maxTileCoordinate_x;
		int y = maxTileCoordinate_y;
		if (zoomDiff < 0) {
			zoomDiff = -zoomDiff;
			x <<= zoomDiff;
			y <<= zoomDiff;
		} else {
			x >>= zoomDiff;
			y >>= zoomDiff;
		}
		return new Point(x, y);
	}

	/**
	 * Return the amount of tiles for the current selection in the specified zoom level.
	 * 
	 * @param zoom
	 *            is the zoom level to calculate the amount of tiles for
	 * @return the amount of tiles in the current selection in the supplied zoom level
	 */
	public long calculateNrOfTiles(int zoom) {
		Point max = getBottomRightTileNumber(zoom);
		Point min = getTopLeftTileNumber(zoom);
		long width = max.x - min.x + 1;
		long height = max.y - min.y + 1;
		long tileCount = width * height;
		if (mapSource instanceof AbstractMultiLayerMapSource) {
			int mapLayerCount = ((AbstractMultiLayerMapSource) mapSource).getLayerMapSources().length;
			tileCount *= mapLayerCount;
		}
		return tileCount;
	}

	public long[] calculateNrOfTilesEx(int zoom) {
		Point max = getBottomRightTileNumber(zoom);
		Point min = getTopLeftTileNumber(zoom);
		long width = max.x - min.x + 1;
		long height = max.y - min.y + 1;
		long tileCount = width * height;
		if (mapSource instanceof AbstractMultiLayerMapSource) {
			int mapLayerCount = ((AbstractMultiLayerMapSource) mapSource).getLayerMapSources().length;
			tileCount *= mapLayerCount;
		}
		return new long[] { tileCount, width, height };
	}

	@Override
	public String toString() {
		EastNorthCoordinate max = getMax();
		EastNorthCoordinate min = getMin();
		return String.format("lat/lon: max(%6f/%6f) min(%6f/%6f)", new Object[] { max.lat, max.lon, min.lat, min.lon });
	}

}
