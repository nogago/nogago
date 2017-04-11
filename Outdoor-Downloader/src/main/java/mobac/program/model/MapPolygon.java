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
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import mobac.program.Logging;
import mobac.program.interfaces.LayerInterface;
import mobac.program.interfaces.MapInterface;
import mobac.program.interfaces.MapSource;
import mobac.program.interfaces.MapSpace;
import mobac.program.interfaces.TileFilter;
import mobac.program.tilefilter.PolygonTileFilter;
import mobac.utilities.MyMath;

@XmlRootElement
public class MapPolygon extends Map {

	@XmlElement
	protected Polygon polygon;
	protected long calculatedTileCount = -1;

	protected MapPolygon() {
	}

	public static MapPolygon createFromTrack(Layer layer, String name, MapSource mapSource, int zoom,
			EastNorthCoordinate[] trackPoints, int pixelDistance, TileImageParameters parameters) {

		MapSpace mapSpace = mapSource.getMapSpace();
		Area area = new Area();
		for (int i = 1; i < trackPoints.length; i++) {
			EastNorthCoordinate point1 = trackPoints[i - 1];
			EastNorthCoordinate point2 = trackPoints[i];

			int y1 = mapSpace.cLatToY(point1.lat, zoom);
			int y2 = mapSpace.cLatToY(point2.lat, zoom);
			int x1 = mapSpace.cLonToX(point1.lon, zoom);
			int x2 = mapSpace.cLonToX(point2.lon, zoom);

			Line2D.Double ln = new Line2D.Double(x1, y1, x2, y2);
			double indent = pixelDistance; // distance from central line
			double length = ln.getP1().distance(ln.getP2());

			double dx_li = (ln.getX2() - ln.getX1()) / length * indent;
			double dy_li = (ln.getY2() - ln.getY1()) / length * indent;

			// moved p1 point
			double p1X = ln.getX1() - dx_li;
			double p1Y = ln.getY1() - dy_li;

			// line moved to the left
			double lX1 = ln.getX1() - dy_li;
			double lY1 = ln.getY1() + dx_li;
			double lX2 = ln.getX2() - dy_li;
			double lY2 = ln.getY2() + dx_li;

			// moved p2 point
			double p2X = ln.getX2() + dx_li;
			double p2Y = ln.getY2() + dy_li;

			// line moved to the right
			double rX1_ = ln.getX1() + dy_li;
			double rY1 = ln.getY1() - dx_li;
			double rX2 = ln.getX2() + dy_li;
			double rY2 = ln.getY2() - dx_li;

			Path2D p = new Path2D.Double();
			p.moveTo(lX1, lY1);
			p.lineTo(lX2, lY2);
			p.lineTo(p2X, p2Y);
			p.lineTo(rX2, rY2);
			p.lineTo(rX1_, rY1);
			p.lineTo(p1X, p1Y);
			p.lineTo(lX1, lY1);

			area.add(new Area(p));
		}
		PathIterator pi = area.getPathIterator(null);
		ArrayList<Integer> xPoints = new ArrayList<Integer>(100);
		ArrayList<Integer> yPoints = new ArrayList<Integer>(100);
		double coords[] = new double[6];
		while (!pi.isDone()) {
			int type = pi.currentSegment(coords);
			switch (type) {
			case PathIterator.SEG_MOVETO:
			case PathIterator.SEG_LINETO:
			case PathIterator.SEG_CLOSE:
				xPoints.add((int) coords[0]);
				yPoints.add((int) coords[1]);
				break;
			default:
				Logging.LOG.warn("Area to polygon conversion: unexpected segment type found: " + type + " "
						+ Arrays.toString(coords));
			}
			pi.next();
		}
		int[] xp = new int[xPoints.size()];
		int[] yp = new int[yPoints.size()];
		for (int i = 0; i < xp.length; i++) {
			xp[i] = xPoints.get(i);
			yp[i] = yPoints.get(i);
		}
		Polygon polygon = new Polygon(xp, yp, xp.length);
		return new MapPolygon(layer, name, mapSource, zoom, polygon, parameters);
	}

	public static MapPolygon createFromMapPolygon(Layer layer, String name, int newZoom, MapPolygon map) {
		Polygon oldPolygon = map.getPolygon();
		int oldZoom = map.getZoom();
		MapSpace mapSpace = map.getMapSource().getMapSpace();
		int xPoints[] = new int[oldPolygon.npoints];
		int yPoints[] = new int[oldPolygon.npoints];
		Point p = new Point();
		for (int i = 0; i < xPoints.length; i++) {
			p.x = oldPolygon.xpoints[i];
			p.y = oldPolygon.ypoints[i];
			Point nP = mapSpace.changeZoom(p, oldZoom, newZoom);
			xPoints[i] = nP.x;
			yPoints[i] = nP.y;
		}
		Polygon newPolygon = new Polygon(xPoints, yPoints, xPoints.length);
		return new MapPolygon(layer, name, map.getMapSource(), newZoom, newPolygon, map.getParameters());
	}

	public MapPolygon(Layer layer, String name, MapSource mapSource, int zoom, Polygon polygon,
			TileImageParameters parameters) {
		super(layer, name, mapSource, zoom, null, null, parameters);
		this.polygon = polygon;
		Rectangle bounds = polygon.getBounds();
		int mapSourceTileSize = mapSource.getMapSpace().getTileSize();
		// Make sure the minimum tile coordinate starts/ends on the edge of a tile from the map source
		int minx = MyMath.roundDownToNearest(bounds.x, mapSourceTileSize);
		int miny = MyMath.roundDownToNearest(bounds.y, mapSourceTileSize);
		int maxx = MyMath.roundUpToNearest(bounds.x + bounds.width, mapSourceTileSize) - 1;
		int maxy = MyMath.roundUpToNearest(bounds.y + bounds.height, mapSourceTileSize) - 1;
		minTileCoordinate = new Point(minx, miny);
		maxTileCoordinate = new Point(maxx, maxy);
		internalCalculateTilesToDownload();
	}

	@Override
	public long calculateTilesToDownload() {
		if (calculatedTileCount < 0)
			internalCalculateTilesToDownload();
		return calculatedTileCount;
	}

	protected void internalCalculateTilesToDownload() {
		int tileSize = mapSource.getMapSpace().getTileSize();
		double tileSizeD = tileSize;
		int xMin = minTileCoordinate.x;
		int xMax = maxTileCoordinate.x;
		int yMin = minTileCoordinate.y;
		int yMax = maxTileCoordinate.y;

		int count = 0;
		for (int x = xMin; x <= xMax; x += tileSize) {
			for (int y = yMin; y <= yMax; y += tileSize) {
				if (polygon.intersects(x, y, tileSizeD, tileSizeD))
					count++;
			}
		}
		calculatedTileCount = count;
	}

	@Override
	public String getToolTip() {
		MapSpace mapSpace = mapSource.getMapSpace();
		EastNorthCoordinate tl = new EastNorthCoordinate(mapSpace, zoom, minTileCoordinate.x, minTileCoordinate.y);
		EastNorthCoordinate br = new EastNorthCoordinate(mapSpace, zoom, maxTileCoordinate.x, maxTileCoordinate.y);

		StringWriter sw = new StringWriter(1024);
		sw.write("<html>");
		sw.write("<b>Polygonal Map</b><br>");
		sw.write("Map source: " + mapSource.getName() + "<br>");
		sw.write("Zoom level: " + zoom + "<br>");
		sw.write("Polygon points: " + polygon.npoints + "<br>");
		sw.write("Area start: " + tl + " (" + minTileCoordinate.x + " / " + minTileCoordinate.y + ")<br>");
		sw.write("Area end: " + br + " (" + maxTileCoordinate.x + " / " + maxTileCoordinate.y + ")<br>");
		sw.write("Map size: " + (maxTileCoordinate.x - minTileCoordinate.x + 1) + "x"
				+ (maxTileCoordinate.y - minTileCoordinate.y + 1) + " pixel<br>");
		if (parameters != null) {
			sw.write("Tile size: " + parameters.getWidth() + "x" + parameters.getHeight() + "<br>");
			sw.write("Tile format: " + parameters.getFormat() + "<br>");
		} else
			sw.write("Tile size: 256x256 (no processing)<br>");
		sw.write("Maximum tiles to download: " + calculateTilesToDownload() + "<br>");
		sw.write("</html>");
		return sw.toString();
	}

	public Polygon getPolygon() {
		return polygon;
	}

	public TileFilter getTileFilter() {
		return new PolygonTileFilter(this);
	}

	@Override
	public MapInterface deepClone(LayerInterface newLayer) {
		MapPolygon map = (MapPolygon) super.deepClone(newLayer);
		map.polygon = new Polygon(polygon.xpoints, polygon.ypoints, polygon.npoints);
		return map;
	}

}
