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

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

public class MessageDialogs {

	public static void showErrorMessage(Component parentComponent, String message, String title) {
		JLabel label = new JLabel("<html>" + message + "<html>");
		int maxWidth = 400;
		Dimension size = label.getPreferredSize();
		if (size.width > maxWidth) {
			// Estimate the number of lines
			int lineCount = (int) Math.ceil(((double) size.width) / maxWidth);
			lineCount += 1; // Add one extra line as reserve
			size.width = maxWidth; // Limit the maximum width
			// Increase the size so that the
			size.height *= lineCount;
			label.setPreferredSize(size);
		}
		JOptionPane.showMessageDialog(null, label, "Test", JOptionPane.ERROR_MESSAGE);
	}

}
