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
package mobac.gui.dialogs;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.xml.bind.JAXBException;

import mobac.gui.MainGUI;
import mobac.program.DirectoryManager;
import mobac.program.Logging;
import mobac.program.model.Bookmark;
import mobac.program.model.Settings;
import mobac.utilities.GBC;

public class ManageBookmarks extends JDialog implements ListSelectionListener, ActionListener {

	private JButton deleteButton;

	private JButton applyButton;

	private JList bookmarks;

	private DefaultListModel bookmarksModel;

	public ManageBookmarks(Window owner) throws HeadlessException {
		super(owner, "Manage Bookmarks");
		setIconImages(MainGUI.MOBAC_ICONS);
		setLayout(new GridBagLayout());
		applyButton = new JButton("Close");
		applyButton.addActionListener(this);
		applyButton.setDefaultCapable(true);

		deleteButton = new JButton("Delete Bookmark");
		deleteButton.addActionListener(this);

		bookmarksModel = new DefaultListModel();
		for (Bookmark b : Settings.getInstance().placeBookmarks)
			bookmarksModel.addElement(b);
		bookmarks = new JList(bookmarksModel);
		bookmarks.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		bookmarks.addListSelectionListener(this);
		bookmarks.setVisibleRowCount(10);
		bookmarks.setPreferredSize(new Dimension(250, 300));

		add(bookmarks, GBC.eol().insets(10, 10, 10, 10).fill());
		add(deleteButton, GBC.eol().anchor(GBC.CENTER).insets(0, 0, 0, 10));
		add(applyButton, GBC.eol().anchor(GBC.CENTER).insets(0, 0, 0, 10));
		pack();

		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((dim.width - getWidth()) / 2, (dim.height - getHeight()) / 2);

		valueChanged(null);
	}

	public void actionPerformed(ActionEvent e) {
		if (deleteButton.equals(e.getSource()))
			deleteSelectedEntries();
		else if (applyButton.equals(e.getSource()))
			apply();
	}

	protected void deleteSelectedEntries() {
		int[] selected = bookmarks.getSelectedIndices();
		for (int i = selected.length - 1; i >= 0; i--)
			bookmarksModel.remove(selected[i]);
	}

	protected void apply() {
		ArrayList<Bookmark> bookmarksList = new ArrayList<Bookmark>(bookmarksModel.getSize());
		for (int i = 0; i < bookmarksModel.getSize(); i++)
			bookmarksList.add((Bookmark) bookmarksModel.get(i));
		Settings.getInstance().placeBookmarks = bookmarksList;
		setVisible(false);
		dispose();
	}

	public void valueChanged(ListSelectionEvent e) {
		deleteButton.setEnabled(bookmarks.getSelectedIndices().length > 0);
	}

	public static void main(String[] args) {
		DirectoryManager.initialize();
		Logging.configureLogging();
		try {
			Settings.load();
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		new ManageBookmarks(null).setVisible(true);
	}

}
