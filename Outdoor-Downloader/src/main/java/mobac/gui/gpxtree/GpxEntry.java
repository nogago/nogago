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

import javax.swing.tree.DefaultMutableTreeNode;

import mobac.gui.mapview.layer.GpxLayer;

/**
 * Generalized entry in the gpx tree. All actual entries derive from this class. The class encapsulates everything
 * gui-related as well as the actual gpx data for the editor. Subclasses: {@link GpxRootEntry}, {@link TrkEntry},
 * {@link RteEntry}, {@link WptEntry}
 * 
 * @author lhoeppner
 * 
 */
public class GpxEntry {
	private DefaultMutableTreeNode node;
	private GpxLayer layer;
	/** determines whether an entry can be a parent for waypoints */
	private boolean isWaypointParent = false;

	public void setLayer(GpxLayer layer) {
		this.layer = layer;
	}

	public GpxLayer getLayer() {
		return layer;
	}

	/**
	 * Remembers the associated tree node.
	 * 
	 * @param node
	 */
	public void setNode(DefaultMutableTreeNode node) {
		this.node = node;
	}

	public DefaultMutableTreeNode getNode() {
		return node;
	}

	public void setWaypointParent(boolean isWaypointParent) {
		this.isWaypointParent = isWaypointParent;
	}

	public boolean isWaypointParent() {
		return isWaypointParent;
	}
}
