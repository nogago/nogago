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

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;

import mobac.gui.MainGUI;
import mobac.program.DirectoryManager;
import mobac.utilities.GUIExceptionHandler;

public class ShowReadme implements ActionListener {

	public void actionPerformed(ActionEvent event) {
		File readme = new File(DirectoryManager.programDir, "README.HTM");
		if (!readme.isFile()) {
			JOptionPane.showMessageDialog(MainGUI.getMainGUI(), "Unable to find README.HTM", "Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		try {
			Desktop.getDesktop().browse(readme.toURI());
		} catch (IOException e) {
			GUIExceptionHandler.processException(e);
		}
	}

}
