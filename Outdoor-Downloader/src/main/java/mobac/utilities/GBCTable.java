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
package mobac.utilities;

import java.awt.Insets;

public class GBCTable {

	public static final int DEFAULT_ROW_SPACING = 4, DEFAULT_COL_SPACING = 8;

	private final int colSpacing, rowSpacing;
	private final GBC gbc = GBC.std();

	private int x, y, yBegin;

	public GBCTable() {
		this(DEFAULT_ROW_SPACING, DEFAULT_COL_SPACING);
	}

	public GBCTable(int spacing) {
		this(spacing, spacing);
	}

	public GBCTable(int rowSpacing, int colSpacing) {
		this.rowSpacing = rowSpacing;
		this.colSpacing = colSpacing;
		begin();
	}

	public GBC begin() {
		return begin(1, 1);
	}

	public GBC begin(int x, int y) {
		this.x = x;
		this.y = yBegin = y;
		reset();
		return gbc;
	}

	public GBC incX() {
		return nextCol(1);
	}

	public GBC nextCol(int x) {
		this.x += x;
		y = yBegin;
		reset();
		return gbc;
	}

	public GBC incY() {
		return nextRow(1);
	}

	public GBC nextRow(int y) {
		this.y += y;
		reset();
		return gbc;
	}

	/**
	 * Ensures the {@link gbc} object was not modified. Reseting the values is cheaper than creating a new object.
	 */
	private void reset() {
		gbc.gridwidth = gbc.gridheight = 1;
		gbc.weightx = gbc.weighty = 0d;
		gbc.fill = GBC.HORIZONTAL;
		gbc.anchor = GBC.WEST;
		if (gbc.insets == null) {
			gbc.insets = new Insets(0, 0, 0, 0);
		} else {
			gbc.insets.top = gbc.insets.bottom = gbc.insets.left = gbc.insets.right = 0;
		}
		if ((gbc.gridx = x) > 1) {
			gbc.insets.left = colSpacing;
		}
		if ((gbc.gridy = y) > 1) {
			gbc.insets.top = rowSpacing;
		}
	}

}
