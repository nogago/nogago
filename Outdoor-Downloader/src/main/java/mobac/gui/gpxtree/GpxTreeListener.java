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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import mobac.gui.actions.GpxElementListener;

/**
 * Listener for the gpx editor tree.
 * 
 * @author lhoeppner
 * 
 */
public class GpxTreeListener implements MouseListener {
	private JPopupMenu popup;

	public void actionPerformed(ActionEvent e) {
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		if (e.isPopupTrigger()) {
			showPopup(e);
		}
	}

	public void mouseReleased(MouseEvent e) {
		if (e.isPopupTrigger()) {
			showPopup(e);
		}
	}

	/**
	 * Popup for all elements in the gpx tree. TODO separate for waypoints, files, tracks and routes
	 * 
	 * @param e
	 */
	private void showPopup(MouseEvent e) {
		JTree tree = (JTree) e.getSource();
		TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
		tree.setSelectionPath(selPath);
		if (selPath == null)
			return;

		DefaultMutableTreeNode node = (DefaultMutableTreeNode) selPath.getLastPathComponent();

		GpxEntry gpxEntry = null;
		try {
			gpxEntry = (GpxEntry) node.getUserObject();
			gpxEntry.setNode(node);
		} catch (ClassCastException exc) {
		}

		popup = new JPopupMenu();
		JMenuItem delete = new JMenuItem("delete element");
		delete.setName(GpxElementListener.MENU_NAME_DELETE);
		GpxElementListener listener = new GpxElementListener(gpxEntry);
		delete.addMouseListener(listener);
		popup.add(delete);
		JMenuItem rename = new JMenuItem("rename element");
		rename.setName(GpxElementListener.MENU_NAME_RENAME);
		rename.addMouseListener(listener);
		popup.add(rename);

		popup.show((Component) e.getSource(), e.getX(), e.getY());
	}
}
