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

import javax.swing.JOptionPane;

import mobac.gui.MainGUI;
import mobac.gui.mapview.PreviewMap;
import mobac.program.model.Bookmark;
import mobac.program.model.Settings;

public class BookmarkAdd implements ActionListener {

	private final PreviewMap previewMap;

	public BookmarkAdd(PreviewMap previewMap) {
		this.previewMap = previewMap;
	}

	public void actionPerformed(ActionEvent arg0) {
		Bookmark bm = previewMap.getPositionBookmark();
		String name = JOptionPane.showInputDialog("please select a name for the new bookmark", bm.toString());
		if (name == null)
			return;
		bm.setName(name);
		Settings.getInstance().placeBookmarks.add(bm);
		MainGUI.getMainGUI().updateBookmarksMenu();
	}

}
