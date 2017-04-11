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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import mobac.gui.MainGUI;
import mobac.program.ProgramInfo;
import mobac.utilities.GBC;
import mobac.utilities.Utilities;

public class AboutDialog extends JDialog implements MouseListener {

	public AboutDialog() throws HeadlessException {
		super(MainGUI.getMainGUI(), "About");
		setIconImages(MainGUI.MOBAC_ICONS);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setResizable(false);

		JPanel panel = new JPanel(null);
		panel.setBackground(Color.WHITE);
		GBC std = GBC.std();
		GBC eol = GBC.eol();
		std.insets(3, 3, 3, 3);
		eol.insets(3, 3, 3, 3);
		ImageIcon splash = Utilities.loadResourceImageIcon("Splash.jpg");
		Dimension size = new Dimension(splash.getIconWidth(), splash.getIconHeight());
		panel.setPreferredSize(size);
		panel.setMinimumSize(size);
		panel.setMaximumSize(size);
		panel.setSize(size);

		JLabel splashLabel = new JLabel(splash);
		JPanel infoPanel = new JPanel(new GridBagLayout());
		infoPanel.setBackground(Color.WHITE);
		infoPanel.setOpaque(false);
		// infoPanel.add(new JLabel("<html><h2>" + ProgramInfo.PROG_NAME + "</h2></html>"), eol);
		infoPanel.add(new JLabel("Version:"), std);
		infoPanel.add(new JLabel(ProgramInfo.getVersion()), eol);
		infoPanel.add(new JLabel("Program revision:"), std);
		infoPanel.add(new JLabel(ProgramInfo.getRevisionStr()), eol);
		// infoPanel.add(new JLabel("Map sources revision:"), std);
		// infoPanel.add(new JLabel(Integer.toString(MapSourcesUpdater.getCurrentMapSourcesRev())), eol);

		panel.add(infoPanel);
		panel.add(splashLabel);

		infoPanel.setBounds(270, 155, 200, 200);
		splashLabel.setBounds(0, 0, splash.getIconWidth(), splash.getIconHeight());

		add(panel);
		pack();
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((dim.width - getWidth()) / 2, (dim.height - getHeight()) / 2);

		addMouseListener(this);
	}

	public void mouseClicked(MouseEvent e) {
		setVisible(false);
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			ProgramInfo.initialize(); // Load revision info
			JDialog dlg = new AboutDialog();
			dlg.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
