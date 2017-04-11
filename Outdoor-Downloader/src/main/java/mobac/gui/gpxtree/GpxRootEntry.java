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

import mobac.data.gpx.gpx11.Gpx;
import mobac.data.gpx.gpx11.MetadataType;
import mobac.gui.mapview.layer.GpxLayer;

public class GpxRootEntry extends GpxEntry {

	public GpxRootEntry(GpxLayer layer) {
		this.setLayer(layer);
		this.setWaypointParent(true);
	}

	public String toString() {
		String name = getMetaDataName();
		if (name != null && !name.equals("")) {
			return name;
		} else {
			if (getLayer().getFile() == null) {
				return "unnamed (new gpx)";
			} else {
				return "unnamed (file " + getLayer().getFile().getName() + ")";
			}
		}
	}

	public String getMetaDataName() {
		try {
			return getLayer().getGpx().getMetadata().getName();
		} catch (NullPointerException e) {
			return null;
		}
	}

	public void setMetaDataName(String name) {
		Gpx gpx = getLayer().getGpx();
		if (gpx.getMetadata() == null)
			gpx.setMetadata(new MetadataType());
		gpx.getMetadata().setName(name);

		// Notify the model about the changed node text
		getLayer().getPanel().getTreeModel().nodeChanged(getNode());
	}
}
