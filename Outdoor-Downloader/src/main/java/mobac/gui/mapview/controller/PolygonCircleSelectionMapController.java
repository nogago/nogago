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
package mobac.gui.mapview.controller;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import mobac.gui.mapview.PreviewMap;

/**
 * Implements the GUI logic for the preview map panel that manages the map selection and actions triggered by key
 * strokes.
 * 
 */
public class PolygonCircleSelectionMapController extends AbstractPolygonSelectionMapController implements
		MouseMotionListener, MouseListener {

	private static final int POLYGON_POINTS = 16;
	private static final double ANGLE_PART = Math.PI * 2.0 / POLYGON_POINTS;

	private Point center;

	public PolygonCircleSelectionMapController(PreviewMap map) {
		super(map);
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			center = convertToAbsolutePoint(e.getPoint());
			polygonPoints.ensureCapacity(POLYGON_POINTS);
		}
	}

	public void mouseDragged(MouseEvent e) {
		if ((e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) == MouseEvent.BUTTON1_DOWN_MASK) {
			if (center != null) {
				Point circlePoint = convertToAbsolutePoint(e.getPoint());
				double radius = circlePoint.distance(center);
				polygonPoints.clear();
				for (int i = 0; i < POLYGON_POINTS; i++) {
					double angle = ANGLE_PART * i;
					int y = (int) Math.round(Math.sin(angle) * radius);
					int x = (int) Math.round(Math.cos(angle) * radius);
					polygonPoints.add(new Point(center.x + x, center.y + y));
				}
				map.grabFocus();
				map.repaint();
			}
		}
	}

	public void mouseMoved(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

}
