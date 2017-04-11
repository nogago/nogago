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
import java.math.BigDecimal;

import javax.swing.JOptionPane;

import mobac.data.gpx.gpx11.Gpx;
import mobac.data.gpx.gpx11.WptType;
import mobac.gui.actions.GpxEditor;
import mobac.gui.gpxtree.GpxEntry;
import mobac.gui.gpxtree.GpxRootEntry;
import mobac.gui.gpxtree.RteEntry;
import mobac.gui.gpxtree.TrksegEntry;
import mobac.gui.mapview.PreviewMap;
import mobac.gui.panels.JGpxPanel;
import mobac.program.interfaces.MapSpace;

/**
 * Allows to create new GPX way-points by clicking on the preview map
 */
public class GpxMapController extends JMapController implements MouseListener {

	private JGpxPanel panel;
	private GpxEntry entry;

	public GpxMapController(PreviewMap map, JGpxPanel panel, boolean enabled) {
		super(map, enabled);
		this.panel = panel;
	}

	public void mouseClicked(MouseEvent e) {
		// Add new GPX point to currently selected GPX file
		disable();
		if (e.getButton() == MouseEvent.BUTTON1) {
			entry = panel.getSelectedEntry();
			Gpx gpx = entry.getLayer().getGpx();
			Point p = e.getPoint();
			Point tl = ((PreviewMap) map).getTopLeftCoordinate();
			p.x += tl.x;
			p.y += tl.y;
			MapSpace mapSpace = map.getMapSource().getMapSpace();
			int maxPixel = mapSpace.getMaxPixels(map.getZoom());
			if (p.x < 0 || p.x > maxPixel || p.y < 0 || p.y > maxPixel)
				return; // outside of world region
			double lon = mapSpace.cXToLon(p.x, map.getZoom());
			double lat = mapSpace.cYToLat(p.y, map.getZoom());
			String name = JOptionPane.showInputDialog(null, "Plase input a name for the new point:");
			if (name == null)
				return;
			Gpx gpx11 = (Gpx) gpx;
			WptType wpt = new WptType();
			wpt.setName(name);
			wpt.setLat(new BigDecimal(lat));
			wpt.setLon(new BigDecimal(lon));
			GpxEditor editor = GpxEditor.getInstance();
			if (entry.getClass() == GpxRootEntry.class) {
				gpx11.getWpt().add(wpt);
			} else if (entry instanceof RteEntry) {
				editor.findRteAndAdd((RteEntry) entry, wpt);
			} else if (entry instanceof TrksegEntry) {
				editor.findTrksegAndAdd((TrksegEntry) entry, wpt);
			}
			panel.addWpt(wpt, entry);
		}
		map.repaint();
	}

	public void repaint() {
		map.repaint();
	}

	public void mouseEntered(MouseEvent e) {

	}

	public void mouseExited(MouseEvent e) {

	}

	public void mousePressed(MouseEvent e) {

	}

	public void mouseReleased(MouseEvent e) {

	}

	@Override
	public void disable() {
		super.disable();
		((PreviewMap) map).getMapSelectionController().enable();
	}
}
