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

//License: GPL. Copyright 2008 by Jan Peter Stotz

import java.awt.Point;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;

import mobac.gui.mapview.PreviewMap;

/**
 * Abstract base class for all mouse controller implementations. For implementing your own controller create a class
 * that derives from this one and implements one or more of the following interfaces:
 * <ul>
 * <li>{@link MouseListener}</li>
 * <li>{@link MouseMotionListener}</li>
 * <li>{@link MouseWheelListener}</li>
 * </ul>
 */
public abstract class JMapController {

	protected final PreviewMap map;
	protected boolean enabled = false;

	public JMapController(PreviewMap map) {
		this.map = map;
	}

	public JMapController(PreviewMap map, boolean enabled) {
		this(map);
		if (enabled)
			enable();
	}

	public void enable() {
		if (enabled)
			return;
		if (this instanceof MouseListener)
			map.addMouseListener((MouseListener) this);
		if (this instanceof MouseWheelListener)
			map.addMouseWheelListener((MouseWheelListener) this);
		if (this instanceof MouseMotionListener)
			map.addMouseMotionListener((MouseMotionListener) this);
		this.enabled = true;
	}

	public void disable() {
		if (!enabled)
			return;
		if (this instanceof MouseListener)
			map.removeMouseListener((MouseListener) this);
		if (this instanceof MouseWheelListener)
			map.removeMouseWheelListener((MouseWheelListener) this);
		if (this instanceof MouseMotionListener)
			map.removeMouseMotionListener((MouseMotionListener) this);
		this.enabled = false;
	}

	protected Point convertToAbsolutePoint(Point p) {
		Point mapPoint = map.getTopLeftCoordinate();
		mapPoint.x += p.getX();
		mapPoint.y += p.getY();
		mapPoint = map.getMapSource().getMapSpace().changeZoom(mapPoint, map.getZoom(), PreviewMap.MAX_ZOOM);
		return mapPoint;
	}
}
