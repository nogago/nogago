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
import java.util.ArrayList;

import mobac.gui.mapview.JMapViewer;
import mobac.gui.mapview.PreviewMap;
import mobac.gui.mapview.layer.PolygonSelectionLayer;

/**
 */
public abstract class AbstractPolygonSelectionMapController extends JMapController {

	protected boolean finished = false;
	protected ArrayList<Point> polygonPoints = new ArrayList<Point>();
	protected PolygonSelectionLayer mapLayer = null;

	public AbstractPolygonSelectionMapController(PreviewMap map) {
		super(map, false);
		mapLayer = new PolygonSelectionLayer(this);
	}

	public void reset() {
		polygonPoints = new ArrayList<Point>();
		finished = false;
	}

	public void finishPolygon() {
		finished = true;
	}

	@Override
	public void enable() {
		map.mapLayers.add(mapLayer);
		super.enable();
	}

	@Override
	public void disable() {
		map.mapLayers.remove(mapLayer);
		super.disable();
	}

	/**
	 * @return List of absolute tile coordinate points regarding {@link JMapViewer#MAX_ZOOM}
	 */
	public ArrayList<Point> getPolygonPoints() {
		return polygonPoints;
	}

}
