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

import java.awt.Color;
import java.util.Vector;

import org.apache.log4j.Logger;

public class JMapSizeCombo extends JIntCombo {

	private static final long serialVersionUID = 1L;

	public static final int MIN = 10;

	public static final int MAX = Integer.MAX_VALUE;

	static Vector<Integer> MAP_SIZE_VALUES;

	static Integer DEFAULT;

	static Logger log = Logger.getLogger(JMapSizeCombo.class);

	static {
		// Sizes from 1024 to 32768
		MAP_SIZE_VALUES = new Vector<Integer>(11);
		MAP_SIZE_VALUES.addElement(new Integer(128000));
		MAP_SIZE_VALUES.addElement(new Integer(1048575));
		MAP_SIZE_VALUES.addElement(new Integer(65536));
		MAP_SIZE_VALUES.addElement(DEFAULT = new Integer(32767));
		MAP_SIZE_VALUES.addElement(new Integer(30000));
		MAP_SIZE_VALUES.addElement(new Integer(25000));
		MAP_SIZE_VALUES.addElement(new Integer(20000));
		MAP_SIZE_VALUES.addElement(new Integer(15000));
		MAP_SIZE_VALUES.addElement(new Integer(10000));
		MAP_SIZE_VALUES.addElement(new Integer(2048));
		MAP_SIZE_VALUES.addElement(new Integer(1024));
	}

	public JMapSizeCombo() {
		super(MAP_SIZE_VALUES, DEFAULT);
		setEditable(true);
		setEditor(new Editor());
		setMaximumRowCount(MAP_SIZE_VALUES.size());
		setSelectedItem(DEFAULT);
	}

	@Override
	protected void createEditorComponent() {
		editorComponent = new JIntField(MIN, MAX, 4, "");
		editorComponent.setErrorColor(Color.ORANGE);
	}

}
