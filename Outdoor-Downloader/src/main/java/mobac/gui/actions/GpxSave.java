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
import mobac.gui.gpxtree.GpxEntry;
import mobac.gui.panels.JGpxPanel;
import mobac.program.model.Settings;
import mobac.utilities.file.GpxFileFilter;


public class GpxSave implements ActionListener {

	private JGpxPanel panel;
	private boolean saveAs;

	public GpxSave(JGpxPanel panel) {
		this(panel, false);
	}

	/**
	 * 
	 * @param panel
	 * @param saveAs
	 *            if true a file chooser dialog is displayed where the user can
	 *            change the filename
	 */
	public GpxSave(JGpxPanel panel, boolean saveAs) {
		super();
		this.panel = panel;
		this.saveAs = saveAs;
	}

	public void actionPerformed(ActionEvent event) {

		GpxEntry entry = panel.getSelectedEntry();
		if (entry == null) {
			JOptionPane.showMessageDialog(null, "No Gpx file selected", "Error saving Gpx file",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		if (!GPXUtils.checkJAXBVersion())
			return;

		Gpx gpx = entry.getLayer().getGpx();

		try {
			File f = entry.getLayer().getFile();
			if (saveAs || f == null)
				f = selectFile(f);
			if (f == null)
				return;
			if (!f.getName().toLowerCase().endsWith(".gpx"))
				f = new File(f.getAbsolutePath() + ".gpx");
			entry.getLayer().setFile(f);
			GPXUtils.saveGpxFile(gpx, f);
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
		MainGUI.getMainGUI().previewMap.repaint();
	}

	private File selectFile(File f) {
		JFileChooser fc = new JFileChooser();
		try {
			File dir = new File(Settings.getInstance().gpxFileChooserDir);
			if (f == null)
				fc.setCurrentDirectory(dir); // restore the saved directory
			else
				fc.setSelectedFile(f);
		} catch (Exception e) {
		}
		fc.addChoosableFileFilter(new GpxFileFilter(true));
		int returnVal = fc.showSaveDialog(MainGUI.getMainGUI());
		if (returnVal != JFileChooser.APPROVE_OPTION)
			return null;
		Settings.getInstance().gpxFileChooserDir = fc.getCurrentDirectory().getAbsolutePath();
		return fc.getSelectedFile();
	}
}
