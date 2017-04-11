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
package mobac.mapsources.mapspace;

import java.awt.Point;

import mobac.gui.mapview.PreviewMap;
import mobac.program.interfaces.MapSpace;


/**
 * Mercator projection with a world width and height of 256 * 2<sup>zoom</sup>
 * pixel. This is the common projecton used by Openstreetmap and Google. It
 * provides methods to translate coordinates from 'map space' into latitude and
 * longitude (on the WGS84 ellipsoid) and vice versa. Map space is measured in
 * pixels. The origin of the map space is the top left corner. The map space
 * origin (0,0) has latitude ~85 and longitude -180
 * 
 * <p>
 * This is the only implementation that is currently supported by Mobile Atlas
 * Creator.
 * </p>
 * <p>
 * DO NOT TRY TO IMPLEMENT YOUR OWN. IT WILL NOT WORK!
 * </p>
 * 
 * @see MapSpace
 */
public class MercatorPower2MapSpace implements MapSpace {

	public static final double MAX_LAT = 85.05112877980659;
	public static final double MIN_LAT = -85.05112877980659;

	protected final int tileSize;

	/**
	 * Pre-computed values for the world size (height respectively width) in the
	 * different zoom levels.
	 */
	protected final int[] worldSize;
	public static final MapSpace INSTANCE_256 = new MercatorPower2MapSpace(256);

	protected MercatorPower2MapSpace(int tileSize) {
		this.tileSize = tileSize;
		worldSize = new int[PreviewMap.MAX_ZOOM + 1];
		for (int zoom = 0; zoom < worldSize.length; zoom++)
			worldSize[zoom] = 256 * (1 << zoom);
	}

	protected double radius(int zoom) {
		return getMaxPixels(zoom) / (2.0 * Math.PI);
	}

	public ProjectionCategory getProjectionCategory() {
		return ProjectionCategory.SPHERE;
	}

	/**
	 * Returns the absolute number of pixels in y or x, defined as:
	 * 2<sup>zoom</sup> * TILE_WIDTH where TILE_WIDTH is the width respectively
	 * height of a tile in pixels
	 * 
	 * @param zoom
	 *            [0..22]
	 * @return
	 */
	public int getMaxPixels(int zoom) {
		return worldSize[zoom];
	}

	protected int falseNorthing(int aZoomlevel) {
		return (-1 * getMaxPixels(aZoomlevel) / 2);
	}

	/**
	 * Transforms latitude to pixelspace
	 * 
	 * @param lat
	 *            [-90...90] qparam zoom [0..22]
	 * @return [0..2^zoom*TILE_SIZE[
	 * @author Jan Peter Stotz
	 */
	public int cLatToY(double lat, int zoom) {
		lat = Math.max(MIN_LAT, Math.min(MAX_LAT, lat));
		double sinLat = Math.sin(Math.toRadians(lat));
		double log = Math.log((1.0 + sinLat) / (1.0 - sinLat));
		int mp = getMaxPixels(zoom);
		int y = (int) (mp * (0.5 - (log / (4.0 * Math.PI))));
		y = Math.min(y, mp - 1);
		return y;
	}

	/**
	 * Transform longitude to pixelspace
	 * 
	 * @param lon
	 *            [-180..180]
	 * @param zoom
	 *            [0..22]
	 * @return [0..2^zoom*TILE_SIZE[
	 * @author Jan Peter Stotz
	 */
	public int cLonToX(double lon, int zoom) {
		int mp = getMaxPixels(zoom);
		int x = (int) ((mp * (lon + 180l)) / 360l);
		x = Math.min(x, mp - 1);
		return x;
	}

	/**
	 * Transforms pixel coordinate X to longitude
	 * 
	 * @param x
	 *            [0..2^zoom*TILE_WIDTH[
	 * @param zoom
	 *            [0..22]
	 * @return ]-180..180[
	 * @author Jan Peter Stotz
	 */
	public double cXToLon(int x, int zoom) {
		return ((360d * x) / getMaxPixels(zoom)) - 180.0;
	}

	/**
	 * Transforms pixel coordinate Y to latitude
	 * 
	 * @param y
	 *            [0..2^zoom*TILE_WIDTH[
	 * @param zoom
	 *            [0..22]
	 * @return [MIN_LAT..MAX_LAT] is about [-85..85]
	 */
	public double cYToLat(int y, int zoom) {
		y += falseNorthing(zoom);
		double latitude = (Math.PI / 2) - (2 * Math.atan(Math.exp(-1.0 * y / radius(zoom))));
		return -1 * Math.toDegrees(latitude);
	}

	public int getTileSize() {
		return tileSize;
	}

	public int moveOnLatitude(int startX, int y, int zoom, double angularDist) {
		y += falseNorthing(zoom);
		double lat = -1 * ((Math.PI / 2) - (2 * Math.atan(Math.exp(-1.0 * y / radius(zoom)))));

		double lon = cXToLon(startX, zoom);
		double sinLat = Math.sin(lat);

		lon += Math.toDegrees(Math.atan2(Math.sin(angularDist) * Math.cos(lat), Math
				.cos(angularDist)
				- sinLat * sinLat));
		int newX = cLonToX(lon, zoom);
		int w = newX - startX;
		return w;
	}

	public double horizontalDistance(int zoom, int y, int xDist) {
		y = Math.max(y, 0);
		y = Math.min(y, getMaxPixels(zoom));
		double lat = cYToLat(y, zoom);
		double lon1 = -180.0;
		double lon2 = cXToLon(xDist, zoom);

		double dLon = Math.toRadians(lon2 - lon1);

		double cos_lat = Math.cos(Math.toRadians(lat));
		double sin_dLon_2 = Math.sin(dLon) / 2;

		double a = cos_lat * cos_lat * sin_dLon_2 * sin_dLon_2;
		return 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
	}

	public int xChangeZoom(int x, int oldZoom, int newZoom) {
		int zoomDiff = oldZoom - newZoom;
		return (zoomDiff > 0) ? x >> zoomDiff : x << -zoomDiff;
	}

	public int yChangeZoom(int y, int oldZoom, int newZoom) {
		int zoomDiff = oldZoom - newZoom;
		return (zoomDiff > 0) ? y >> zoomDiff : y << -zoomDiff;
	}

	public Point changeZoom(Point pixelCoordinate, int oldZoom, int newZoom) {
		int x = xChangeZoom(pixelCoordinate.x, oldZoom, newZoom);
		int y = yChangeZoom(pixelCoordinate.y, oldZoom, newZoom);
		return new Point(x, y);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + tileSize;
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
		MercatorPower2MapSpace other = (MercatorPower2MapSpace) obj;
		if (tileSize != other.tileSize)
			return false;
		return true;
	}
	
}
