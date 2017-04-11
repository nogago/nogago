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
package mobac.gui.gpxtree;

import mobac.data.gpx.gpx11.TrkType;
import mobac.gui.mapview.layer.GpxLayer;

public class TrkEntry extends GpxEntry {
	private TrkType trk;
	
	public TrkEntry(TrkType trk, GpxLayer layer) {
		this.trk = trk;
		this.setLayer(layer);
		this.setWaypointParent(false);
	}	
	
	public String toString() {
		String name = "";
		try {
			name = trk.getName();
		} catch (NullPointerException e) {
			// no name set
		}
		if (name != null && !name.equals("")) {
			return name;
		} else {
			return "unnamed track";
		}
	}

	public TrkType getTrk() {
		return trk;
	}		
}
