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
package mobac.gui.atlastree;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.tree.TreePath;

public class MouseController extends MouseAdapter {

	JAtlasTree atlasTree;

	public MouseController(JAtlasTree atlasTree) {
		super();
		this.atlasTree = atlasTree;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getButton() != MouseEvent.BUTTON1 || e.getClickCount() != 2)
			return;
		TreePath selPath = atlasTree.getSelectionPath();
		if (selPath == null)
			return; // clicked on empty area
		atlasTree.selectElementOnMap(selPath.getLastPathComponent());
	}

	public void mousePressed(MouseEvent e) {
		if (e.isPopupTrigger()) {
			atlasTree.showNodePopupMenu(e);
		}
	}

	public void mouseReleased(MouseEvent e) {
		if (e.isPopupTrigger()) {
			atlasTree.showNodePopupMenu(e);
		}
	}

}
