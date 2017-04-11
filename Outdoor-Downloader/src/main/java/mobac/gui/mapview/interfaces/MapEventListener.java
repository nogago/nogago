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
package mobac.gui.mapview.interfaces;

import mobac.gui.mapview.controller.JMapController;
import mobac.program.interfaces.MapSource;
import mobac.program.model.MercatorPixelCoordinate;

public interface MapEventListener {

	/** the selection changed */
	public void selectionChanged(MercatorPixelCoordinate max, MercatorPixelCoordinate min);

	/** the zoom changed */
	public void zoomChanged(int newZoomLevel);

	/** the grid zoom changed */
	public void gridZoomChanged(int newGridZoomLevel);

	/** select the next map source from the map list */
	public void selectNextMapSource();

	/** select the previous map source from the map list */
	public void selectPreviousMapSource();

	public void mapSourceChanged(MapSource newMapSource);

	public void mapSelectionControllerChanged(JMapController newMapController);
}
