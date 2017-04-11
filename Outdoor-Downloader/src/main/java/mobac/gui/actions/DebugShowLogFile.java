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
import mobac.program.Logging;
import mobac.utilities.GUIExceptionHandler;

import org.apache.log4j.Logger;

public class DebugShowLogFile implements ActionListener {

	public void actionPerformed(ActionEvent event) {
		Logger log = Logger.getLogger(DebugShowLogFile.class);
		String logFile = Logging.getLogFile();
		if (logFile == null) {
			log.error("No file logger configured");
			JOptionPane.showMessageDialog(MainGUI.getMainGUI(), "No file logger configured", "Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		File f = new File(logFile);
		if (!f.isFile()) {
			log.error("Log file does not exists: " + f.getAbsolutePath());
			JOptionPane.showMessageDialog(MainGUI.getMainGUI(), "<html>Log file does not exists:<br><br>"
					+ f.getAbsolutePath() + "</html>", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		try {
			Desktop.getDesktop().open(f);
		} catch (IOException e) {
			GUIExceptionHandler.processException(e);
		}
	}

}
