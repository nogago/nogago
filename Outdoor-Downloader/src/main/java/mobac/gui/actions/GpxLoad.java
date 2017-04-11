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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.xml.bind.JAXBException;

import mobac.data.gpx.GPXUtils;
import mobac.data.gpx.gpx11.Gpx;
import mobac.gui.MainGUI;
import mobac.gui.mapview.layer.GpxLayer;
import mobac.gui.panels.JGpxPanel;
import mobac.program.model.Settings;
import mobac.utilities.file.GpxFileFilter;

public class GpxLoad implements ActionListener {

	JGpxPanel panel;

	public GpxLoad(JGpxPanel panel) {
		super();
		this.panel = panel;
	}

	public void actionPerformed(ActionEvent event) {
		if (!GPXUtils.checkJAXBVersion())
			return;
		JFileChooser fc = new JFileChooser();
		try {
			File dir = new File(Settings.getInstance().gpxFileChooserDir);
			fc.setCurrentDirectory(dir); // restore the saved directory
		} catch (Exception e) {
		}
		fc.addChoosableFileFilter(new GpxFileFilter(false));
		int returnVal = fc.showOpenDialog(MainGUI.getMainGUI());
		if (returnVal != JFileChooser.APPROVE_OPTION)
			return;
		Settings.getInstance().gpxFileChooserDir = fc.getCurrentDirectory().getAbsolutePath();

		File f = fc.getSelectedFile();
		if (panel.isFileOpen(f.getAbsolutePath())) {
			int answer = JOptionPane.showConfirmDialog(null, "The file was already opened.\n"
					+ "Do you want to open a new instance of this file?", "Warning", JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE);
			if (answer != JOptionPane.YES_OPTION)
				return;
		}
		doLoad(f);
	}

	/**
	 * @param f
	 */
	private void doLoad(File f) {
		try {
			Gpx gpx = GPXUtils.loadGpxFile(f);
			GpxLayer gpxLayer = new GpxLayer(gpx);
			gpxLayer.setFile(f);
			panel.addGpxLayer(gpxLayer);
		} catch (JAXBException e) {
			JOptionPane.showMessageDialog(MainGUI.getMainGUI(), "<html>Unable to load the GPX file <br><i>"
					+ f.getAbsolutePath()
					+ "</i><br><br><b>Please make sure the file is a valid GPX v1.1 file.</b><br>"
					+ "<br>Internal error message:<br>" + e.getMessage() + "</html>", "GPX loading failed",
					JOptionPane.ERROR_MESSAGE);

		}
		MainGUI.getMainGUI().previewMap.repaint();
	}
}
