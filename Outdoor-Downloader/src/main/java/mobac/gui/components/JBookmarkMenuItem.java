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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;

import mobac.gui.MainGUI;
import mobac.program.model.Bookmark;

public class JBookmarkMenuItem extends JMenuItem implements ActionListener {

	private final Bookmark bookmark;

	public JBookmarkMenuItem(Bookmark bookmark) {
		super(bookmark.toString());
		this.bookmark = bookmark;
		addActionListener(this);
	}

	public void actionPerformed(ActionEvent paramActionEvent) {
		MainGUI.getMainGUI().previewMap.gotoPositionBookmark(bookmark);

	}

}
