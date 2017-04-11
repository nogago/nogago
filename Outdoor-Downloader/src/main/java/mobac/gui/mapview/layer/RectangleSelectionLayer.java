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
package mobac.gui.mapview.layer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;

import mobac.gui.mapview.JMapViewer;
import mobac.gui.mapview.PreviewMap;
import mobac.gui.mapview.controller.RectangleSelectionMapController;
import mobac.gui.mapview.interfaces.MapLayer;
import mobac.program.interfaces.MapSpace;

/**
 * Displays a polygon on the map - only for testing purposes
 */
public class RectangleSelectionLayer implements MapLayer {

	private final RectangleSelectionMapController mapController;

	public RectangleSelectionLayer(RectangleSelectionMapController rectangleSelectionMapController) {
		this.mapController = rectangleSelectionMapController;
	}

	public void paint(JMapViewer map, Graphics2D g, int zoom, int minX, int minY, int maxX, int maxY) {
		MapSpace mapSpace = map.getMapSource().getMapSpace();
		g.setColor(Color.BLUE);
		Point p1 = mapController.getiStartSelectionPoint();
		Point p2 = mapController.getiEndSelectionPoint();
		if (p1 == null || p2 == null)
			return;
		p1 = mapSpace.changeZoom(p1, PreviewMap.MAX_ZOOM, zoom);
		p2 = mapSpace.changeZoom(p2, PreviewMap.MAX_ZOOM, zoom);
		
		int x = Math.min(p1.x, p2.x);
		int y = Math.min(p1.y, p2.y);
		int w = Math.abs(p1.x - p2.x);
		int h = Math.abs(p1.y - p2.y);
		
		AffineTransform at = g.getTransform();
		try {
			g.translate(-minX, -minY);
			g.drawRect(x, y, w, h);
		} finally {
			g.setTransform(at);
		}
	}
}
