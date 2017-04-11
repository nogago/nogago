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

import mobac.data.gpx.GPXUtils;
import mobac.data.gpx.gpx11.Gpx;
import mobac.gui.MainGUI;
import mobac.gui.gpxtree.GpxRootEntry;
import mobac.gui.mapview.layer.GpxLayer;
import mobac.gui.panels.JGpxPanel;
//import mobac.gui.panels.JGpxPanel.ListModelEntry;


public class GpxNew implements ActionListener {

	JGpxPanel panel;

	public GpxNew(JGpxPanel panel) {
		super();
		this.panel = panel;
	}

	public void actionPerformed(ActionEvent event) {
		if (!GPXUtils.checkJAXBVersion())
			return;
		newGpx();
		MainGUI.getMainGUI().previewMap.repaint();
	}

	public GpxRootEntry newGpx() {
		Gpx gpx = Gpx.createGpx();
		GpxLayer gpxLayer = new GpxLayer(gpx);
		return panel.addGpxLayer(gpxLayer);
	}
}
