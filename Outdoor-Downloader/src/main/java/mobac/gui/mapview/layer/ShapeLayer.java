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
import java.awt.Shape;
import java.awt.geom.AffineTransform;

import mobac.gui.mapview.JMapViewer;
import mobac.gui.mapview.interfaces.MapLayer;

/**
 * Displays a polygon on the map - only for testing purposes
 */
public class ShapeLayer implements MapLayer {

	private Color color = new Color(0f, 1f, 0f, 0.5f);

	private int calculationZoom;
	private Shape shape;;

	public ShapeLayer(Shape shape, int zoom) {
		this.shape = shape;
	}

	public void paint(JMapViewer map, Graphics2D g, int zoom, int minX, int minY, int maxX, int maxY) {
		AffineTransform af = g.getTransform();
		g.translate(-minX, -minY);
		double scale;
		if (zoom < calculationZoom)
			scale = 1d / (1 << (calculationZoom - zoom));
		else
			scale = 1 << (zoom - calculationZoom);
		g.scale(scale, scale);
		g.setColor(color);
		g.fill(shape);
		g.setTransform(af);
	}

}
