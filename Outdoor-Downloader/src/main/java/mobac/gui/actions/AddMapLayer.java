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
package mobac.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import mobac.gui.MainGUI;
import mobac.gui.mapview.controller.AbstractPolygonSelectionMapController;
import mobac.gui.mapview.controller.JMapController;
import mobac.gui.mapview.controller.RectangleSelectionMapController;

public class AddMapLayer implements ActionListener {

	public static final AddMapLayer INSTANCE = new AddMapLayer();

	public void actionPerformed(ActionEvent event) {
		JMapController msc = MainGUI.getMainGUI().previewMap.getMapSelectionController();
		if (msc instanceof RectangleSelectionMapController)
			new AddRectangleMapAutocut().actionPerformed(event);
		else if (msc instanceof AbstractPolygonSelectionMapController)
			new AddPolygonMapLayer().actionPerformed(event);
		else
			throw new RuntimeException("Unknown mapSelectionController type");
	}
}
