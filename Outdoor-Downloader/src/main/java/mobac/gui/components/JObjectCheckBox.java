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

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JCheckBox;

/**
 * An extended {@link JCheckBox} implementation that allows to link one user
 * object to the checkbox.
 * 
 * @param <E>
 *            type of the user object linked/stored with the checkbox
 */
public class JObjectCheckBox<E> extends JCheckBox {

	private static final long serialVersionUID = 1L;
	
	private E object;

	public JObjectCheckBox(Icon icon) {
		super(icon);
	}

	public JObjectCheckBox(String text) {
		super(text);
	}

	public JObjectCheckBox(Action a) {
		super(a);
	}

	public JObjectCheckBox(Icon icon, boolean selected) {
		super(icon, selected);
	}

	public JObjectCheckBox(String text, boolean selected) {
		super(text, selected);
	}

	public JObjectCheckBox(String text, Icon icon) {
		super(text, icon);
	}

	public JObjectCheckBox(String text, Icon icon, boolean selected) {
		super(text, icon, selected);
	}

	public E getObject() {
		return object;
	}

	public void setObject(E object) {
		this.object = object;
	}

}
