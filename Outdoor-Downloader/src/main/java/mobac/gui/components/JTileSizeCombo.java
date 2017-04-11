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

import java.util.Vector;

import org.apache.log4j.Logger;

public class JTileSizeCombo extends JIntCombo {

	private static final long serialVersionUID = 1L;

	public static final int MIN = 50;

	public static final int MAX = 8192;

	static Vector<Integer> TILE_SIZE_VALUES;

	static Integer DEFAULT;

	static Logger log = Logger.getLogger(JTileSizeCombo.class);

	static {
		DEFAULT = new Integer(256);
		TILE_SIZE_VALUES = new Vector<Integer>();
		TILE_SIZE_VALUES.addElement(new Integer(64));
		TILE_SIZE_VALUES.addElement(new Integer(128));
		TILE_SIZE_VALUES.addElement(DEFAULT);
		TILE_SIZE_VALUES.addElement(new Integer(512));
		TILE_SIZE_VALUES.addElement(new Integer(768));
		TILE_SIZE_VALUES.addElement(new Integer(1024));
		TILE_SIZE_VALUES.addElement(new Integer(1536));
		for (int i = 2048; i <= MAX; i += 1024) {
			TILE_SIZE_VALUES.addElement(new Integer(i));
		}
	}

	public JTileSizeCombo() {
		super(TILE_SIZE_VALUES, DEFAULT);
		setEditable(true);
		setEditor(new Editor());
		setMaximumRowCount(TILE_SIZE_VALUES.size());
		setSelectedItem(DEFAULT);
	}

	@Override
	protected void createEditorComponent() {
		editorComponent = new JIntField(MIN, MAX, 4, "<html>Invalid tile size!<br>"
				+ "Please enter a number between %d and %d</html>");
	}

}
