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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import mobac.gui.mapview.PreviewMap;
import mobac.utilities.OSUtilities;

/**
 * Default map controller which implements map moving by pressing the right mouse button and zooming by double click or
 * by mouse wheel.
 * 
 * @author Jan Peter Stotz
 * 
 */
public class DefaultMapController extends JMapController implements MouseListener, MouseMotionListener,
		MouseWheelListener {

	private static final int MOUSE_BUTTONS_MASK = MouseEvent.BUTTON3_DOWN_MASK | MouseEvent.BUTTON1_DOWN_MASK
			| MouseEvent.BUTTON2_DOWN_MASK;

	private static final int MAC_MOUSE_BUTTON3_MASK = MouseEvent.CTRL_DOWN_MASK | MouseEvent.BUTTON1_DOWN_MASK;

	public DefaultMapController(PreviewMap map) {
		super(map, true);
	}

	private Point lastDragPoint;

	private boolean isMoving = false;

	private boolean movementEnabled = true;

	private int movementMouseButton = MouseEvent.BUTTON3;
	private int movementMouseButtonMask = MouseEvent.BUTTON3_DOWN_MASK;

	private boolean wheelZoomEnabled = true;
	private boolean doubleClickZoomEnabled = true;

	public void mouseDragged(MouseEvent e) {
		if (!movementEnabled || !isMoving)
			return;
		// Is only the selected mouse button pressed?
		if ((e.getModifiersEx() & MOUSE_BUTTONS_MASK) == movementMouseButtonMask) {
			Point p = e.getPoint();
			if (lastDragPoint != null) {
				int diffx = lastDragPoint.x - p.x;
				int diffy = lastDragPoint.y - p.y;
				map.moveMap(diffx, diffy);
			}
			lastDragPoint = p;
		}
	}

	public void mouseClicked(MouseEvent e) {
		if (doubleClickZoomEnabled && e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1)
			map.zoomIn(e.getPoint());
	}

	public void mousePressed(MouseEvent e) {
		if (e.getButton() == movementMouseButton || OSUtilities.isPlatformOsx()
				&& e.getModifiersEx() == MAC_MOUSE_BUTTON3_MASK) {
			lastDragPoint = null;
			isMoving = true;
		}
	}

	public void mouseReleased(MouseEvent e) {
		if (e.getButton() == movementMouseButton || OSUtilities.isPlatformOsx() && e.getButton() == MouseEvent.BUTTON1) {
			lastDragPoint = null;
			isMoving = false;
		}
	}

	public void mouseWheelMoved(MouseWheelEvent e) {
		if (wheelZoomEnabled)
			map.setZoom(map.getZoom() - e.getWheelRotation(), e.getPoint());
	}

	public boolean isMovementEnabled() {
		return movementEnabled;
	}

	/**
	 * Enables or disables that the map pane can be moved using the mouse.
	 * 
	 * @param movementEnabled
	 */
	public void setMovementEnabled(boolean movementEnabled) {
		this.movementEnabled = movementEnabled;
	}

	public int getMovementMouseButton() {
		return movementMouseButton;
	}

	/**
	 * Sets the mouse button that is used for moving the map. Possible values are:
	 * <ul>
	 * <li>{@link MouseEvent#BUTTON1} (left mouse button)</li>
	 * <li>{@link MouseEvent#BUTTON2} (middle mouse button)</li>
	 * <li>{@link MouseEvent#BUTTON3} (right mouse button)</li>
	 * </ul>
	 * 
	 * @param movementMouseButton
	 */
	public void setMovementMouseButton(int movementMouseButton) {
		this.movementMouseButton = movementMouseButton;
		switch (movementMouseButton) {
		case MouseEvent.BUTTON1:
			movementMouseButtonMask = MouseEvent.BUTTON1_DOWN_MASK;
			break;
		case MouseEvent.BUTTON2:
			movementMouseButtonMask = MouseEvent.BUTTON2_DOWN_MASK;
			break;
		case MouseEvent.BUTTON3:
			movementMouseButtonMask = MouseEvent.BUTTON3_DOWN_MASK;
			break;
		default:
			throw new RuntimeException("Unsupported button");
		}
	}

	public boolean isWheelZoomEnabled() {
		return wheelZoomEnabled;
	}

	public void setWheelZoomEnabled(boolean wheelZoomEnabled) {
		this.wheelZoomEnabled = wheelZoomEnabled;
	}

	public boolean isDoubleClickZoomEnabled() {
		return doubleClickZoomEnabled;
	}

	public void setDoubleClickZoomEnabled(boolean doubleClickZoomEnabled) {
		this.doubleClickZoomEnabled = doubleClickZoomEnabled;
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mouseMoved(MouseEvent e) {

		// Mac OSX simulates with ctrl + mouse 1 the second mouse button hence
		// no dragging events get fired.
		if (!OSUtilities.isPlatformOsx() || !movementEnabled || !isMoving)
			return;
		// Is only the selected mouse button pressed?
		if (e.getModifiersEx() == MouseEvent.CTRL_DOWN_MASK) {
			Point p = e.getPoint();
			if (lastDragPoint != null) {
				int diffx = lastDragPoint.x - p.x;
				int diffy = lastDragPoint.y - p.y;
				map.moveMap(diffx, diffy);
			}
			lastDragPoint = p;
		}

	}

}
