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

import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import mobac.data.gpx.gpx11.Gpx;
import mobac.data.gpx.gpx11.RteType;
import mobac.data.gpx.gpx11.TrkType;
import mobac.data.gpx.gpx11.TrksegType;
import mobac.data.gpx.gpx11.WptType;
import mobac.gui.gpxtree.GpxEntry;
import mobac.gui.gpxtree.RteEntry;
import mobac.gui.gpxtree.TrkEntry;
import mobac.gui.gpxtree.TrksegEntry;

/**
 * Encapsulates all functionality regarding edits of loaded gpx files.
 * 
 * @author lhoeppner
 * 
 */
public class GpxEditor {
	private static GpxEditor editor = null;

	public static GpxEditor getInstance() {
		if (editor == null) {
			editor = new GpxEditor();
		}
		return editor;
	}

	/**
	 * Adds a wpt to the selected route.
	 * 
	 * @param entry
	 * @param wpt
	 */
	public void findRteAndAdd(RteEntry entry, WptType wpt) {
		List<RteType> rtes = entry.getLayer().getGpx().getRte();
		RteType rteParent = (entry).getRte();
		for (RteType rte : rtes) {
			if (rte.equals(rteParent)) {
				rte.getRtept().add(wpt);
			}
		}
	}

	/**
	 * Adds a wpt to the selected track segment.
	 * 
	 * @param entry
	 * @param wpt
	 */
	public void findTrksegAndAdd(TrksegEntry entry, WptType wpt) {
		// get the track the selected track segment belongs to
		DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) entry.getNode().getParent();
		TrkEntry trkParent = (TrkEntry) parentNode.getUserObject();

		// get the selected track segment
		TrksegType trksegParent = entry.getTrkSeg();
		List<TrksegType> trksegs = trkParent.getTrk().getTrkseg();

		for (TrksegType trkseg : trksegs) {
			if (trkseg.equals(trksegParent)) {
				trkseg.getTrkpt().add(wpt);
			}
		}
	}

	/**
	 * Removes a waypoint from the Gpx assigned to the layer.
	 * 
	 * @param wpt
	 *            - the node to be removed
	 * @return - true if wpt found and deleted, false otherwise
	 */
	public boolean findWptAndDelete(WptType wpt, GpxEntry gpxEntry) {
		Gpx gpx = gpxEntry.getLayer().getGpx();
		// wpts
		List<WptType> wpts = gpx.getWpt();
		for (WptType currentWpt : wpts) {
			if (currentWpt.equals(wpt)) {
				wpts.remove(currentWpt);
				return true;
			}
		}
		// trks
		List<TrkType> trks = gpx.getTrk();
		for (TrkType currentTrk : trks) {
			List<TrksegType> trksegs = currentTrk.getTrkseg();
			for (TrksegType currentTrkseg : trksegs) {
				wpts = currentTrkseg.getTrkpt();
				for (WptType currentWpt : wpts) {
					if (currentWpt.equals(wpt)) {
						wpts.remove(currentWpt);
						return true;
					}
				}
			}
		}
		// rtes
		List<RteType> rtes = gpx.getRte();
		for (RteType currentRte : rtes) {
			wpts = currentRte.getRtept();
			for (WptType currentWpt : wpts) {
				if (currentWpt.equals(wpt)) {
					wpts.remove(currentWpt);
					return true;
				}
			}
		}
		return false; // if the node wasn't found
	}
}
