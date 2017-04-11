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
import java.awt.geom.Point2D;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import mobac.program.interfaces.MapSpace;
import mobac.utilities.Utilities;



@XmlRootElement
public class EastNorthCoordinate {

	@XmlAttribute
	public double lat;
	@XmlAttribute
	public double lon;

	public EastNorthCoordinate() {
		lat = Double.NaN;
		lon = Double.NaN;
	}

	public EastNorthCoordinate(MapSpace mapSpace, int zoom, int pixelCoordinateX,
			int pixelCoordinateY) {
		this.lat = mapSpace.cYToLat(pixelCoordinateY, zoom);
		this.lon = mapSpace.cXToLon(pixelCoordinateX, zoom);
	}

	public EastNorthCoordinate(double lat, double lon) {
		this.lat = lat;
		this.lon = lon;
	}

	public EastNorthCoordinate(Point2D.Double c) {
		this.lat = c.y;
		this.lon = c.x;
	}

	public Point toTileCoordinate(MapSpace mapSpace, int zoom) {
		int x = mapSpace.cLonToX(lon, zoom);
		int y = mapSpace.cLatToY(lat, zoom);
		return new Point(x, y);
	}

	@Override
	public String toString() {
		return Utilities.prettyPrintLatLon(lat, true) + " "
				+ Utilities.prettyPrintLatLon(lon, false);
	}

}
