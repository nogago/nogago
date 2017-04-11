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
package mobac.program.jaxb;

import java.awt.Point;
import java.awt.Polygon;
import java.util.Vector;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Required {@link XmlAdapter} implementation for serializing a {@link Polygon}
 */
public class PolygonAdapter extends XmlAdapter<PolygonType, Polygon> {

	@Override
	public PolygonType marshal(Polygon polygon) throws Exception {
		Vector<Point> points = new Vector<Point>(polygon.npoints);
		for (int i = 0; i < polygon.npoints; i++) {
			Point p = new Point(polygon.xpoints[i], polygon.ypoints[i]);
			points.add(p);
		}
		return new PolygonType(points);
	}

	@Override
	public Polygon unmarshal(PolygonType value) throws Exception {
		int npoints = value.points.size();
		int[] xpoints = new int[npoints];
		int[] ypoints = new int[npoints];
		for (int i = 0; i < npoints; i++) {
			Point p = value.points.get(i);
			xpoints[i] = p.x;
			ypoints[i] = p.y;
		}

		return new Polygon(xpoints, ypoints, npoints);
	}

}
