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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import mobac.gui.MainGUI;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class DebugSetLogLevel implements ActionListener {

	public void actionPerformed(ActionEvent event) {
		Logger log = Logger.getRootLogger();
		JMenuItem menuItem = (JMenuItem) event.getSource();
		log.setLevel(Level.toLevel(menuItem.getName()));
		JMenu menu = MainGUI.getMainGUI().logLevelMenu;
		Component[] c = menu.getMenuComponents();
		for (int i = 0; i < c.length; i++) {
			((JMenuItem) c[i]).setSelected(c[i] == menuItem);
		}
	}
}
