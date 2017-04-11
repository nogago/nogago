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
package mobac.gui.components;

import java.awt.Component;

import javax.swing.JLayeredPane;

public class FilledLayeredPane extends JLayeredPane {

	private static final long serialVersionUID = 1L;

	/**
	 * Layout each of the components in this JLayeredPane so that they all fill
	 * the entire extents of the layered pane -- from (0,0) to (getWidth(),
	 * getHeight())
	 */
	@Override
	public void doLayout() {
		// Synchronizing on getTreeLock, because I see other layouts doing
		// that.
		// see BorderLayout::layoutContainer(Container)
		synchronized (getTreeLock()) {
			int w = getWidth();
			int h = getHeight();
			for (Component c : getComponents()) {
				c.setBounds(0, 0, w, h);
			}
		}
	}
}
