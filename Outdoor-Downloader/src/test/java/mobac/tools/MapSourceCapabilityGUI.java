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
package mobac.tools;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.HeadlessException;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

public class MapSourceCapabilityGUI extends JFrame {

	private final List<MapSourceCapabilityDetector> result;

	public MapSourceCapabilityGUI(List<MapSourceCapabilityDetector> result) throws HeadlessException {
		super("Map source capabilities");
		setLayout(new BorderLayout());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.result = result;
		JTable table = new JTable(new Model());
		table.setDefaultRenderer(Object.class, new Renderer());
		add(table.getTableHeader(), BorderLayout.NORTH);
		add(table, BorderLayout.CENTER);
		pack();
	}

	private class Model extends AbstractTableModel {

		public int getRowCount() {
			return result.size();
		}

		public Object getValueAt(int rowIndex, int columnIndex) {

			MapSourceCapabilityDetector mscd = result.get(rowIndex);

			switch (columnIndex) {
			case 0:
				return mscd.getZoom();
			case 1:
				return mscd.iseTagPresent();
			case 2:
				return mscd.isLastModifiedTimePresent();
			case 3:
				return mscd.isIfNoneMatchSupported();
			case 4:
				return mscd.isIfModifiedSinceSupported();
			case 5:
				return mscd.getContentType();
			}
			return null;
		}

		public int getColumnCount() {
			return 6;
		}

		public String getColumnName(int column) {
			switch (column) {
			case 0:
				return "Zoom";
			case 1:
				return "eTag";
			case 2:
				return "LastModified";
			case 3:
				return "IfNoneMatch";
			case 4:
				return "IfModifiedSince";
			case 5:
				return "Content type";
			}
			return null;
		}

	}

	private class Renderer extends DefaultTableCellRenderer {

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
				boolean hasFocus, int row, int column) {
			this.setHorizontalAlignment(JLabel.CENTER);
			Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			if ((value != null) && (value instanceof Boolean) && ((Boolean) value))
				c.setBackground(Color.GREEN);
			else
				c.setBackground(Color.WHITE);
			return c;
		}
	}
}
