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

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import mobac.gui.MainGUI;
import mobac.mapsources.MapSourcesManager;
import mobac.program.interfaces.MapSource;
import mobac.program.model.MapSourceLoaderInfo;

public class DebugShowMapSourceNames implements ActionListener {

	public void actionPerformed(ActionEvent e) {
		ArrayList<MapSource> mapSources = new ArrayList<MapSource>(MapSourcesManager.getInstance()
				.getAllAvailableMapSources());

		Collections.sort(mapSources, new Comparator<MapSource>() {

			public int compare(MapSource o1, MapSource o2) {
				return o1.getName().compareTo(o2.getName());
			}

		});
		JFrame dialog = new JFrame("Map source names");
		dialog.setLocationRelativeTo(MainGUI.getMainGUI());
		dialog.setLocation(100, 40);
		Dimension dScreen = Toolkit.getDefaultToolkit().getScreenSize();
		dScreen.height -= 200;
		dScreen.width = Math.min(dScreen.width - 100, 700);
		dialog.setSize(dScreen);
		dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		JTable mapSourcesTable = new JTable(new MapSourcesTableModel(mapSources));
		JScrollPane scroller = new JScrollPane(mapSourcesTable);

		mapSourcesTable.getColumnModel().getColumn(2).setMaxWidth(100);
		dialog.add(scroller);
		dialog.setVisible(true);
	}

	static class MapSourcesTableModel extends AbstractTableModel {

		List<MapSource> mapSources;

		public MapSourcesTableModel(List<MapSource> mapSources) {
			super();
			this.mapSources = mapSources;
		}

		public int getRowCount() {
			return mapSources.size();
		}

		public int getColumnCount() {
			return 4;
		}

		@Override
		public String getColumnName(int column) {
			switch (column) {
			case 0:
				return "Name";
			case 1:
				return "Display text";
			case 2:
				return "Revision";
			case 3:
				return "Type";
			default:
				return null;
			}
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			MapSource ms = mapSources.get(rowIndex);
			MapSourceLoaderInfo li;
			switch (columnIndex) {
			case 0:
				return ms.getName();
			case 1:
				return ms.toString();
			case 2:
				li = ms.getLoaderInfo();
				if (li == null)
					return null;
				return li.getRevision();
			case 3:
				li = ms.getLoaderInfo();
				if (li == null)
					return null;
				String s = "";
				File f = li.getSourceFile();
				if (f != null)
					s += f.getName() + " / ";
				return s + li.getLoaderType();
			default:
				return null;
			}
		}
	}

}
