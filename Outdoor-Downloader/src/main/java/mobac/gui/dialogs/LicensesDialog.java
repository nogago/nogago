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

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import mobac.gui.MainGUI;
import mobac.program.ProgramInfo;
import mobac.utilities.GBC;
import mobac.utilities.Utilities;

public class LicensesDialog extends JFrame implements ChangeListener, ActionListener {

	private LicenseInfo[] licenses = new LicenseInfo[] { new LicenseInfo("<h2>Mobile Atlas Creator</h2>", "gpl.txt"),
			new LicenseInfo("<h3>Library Apache Log4J</h3>", "apache-2.0.txt"),
			new LicenseInfo("<h3>Library Apache Commons Codec</h3>", "apache-2.0.txt"),
			new LicenseInfo("<h3>Library Apache Commons IO</h3>", "apache-2.0.txt"),
			new LicenseInfo("<h3>Library Berkely-DB JavaEdition</h3>", "license-dbd-je.txt"),
			new LicenseInfo("<h3>Library BeanShell</h3>", "lgpl-3.0.txt"),
			new LicenseInfo("<h3>Library JavaPNG</h3>", "gpl.txt"),
			new LicenseInfo("<h3>Library iTextPDF</h3>", "agpl.txt") };

	private final JTextArea textArea;
	private final JTabbedPane tab;
	private String currentLicense = null;

	public LicensesDialog() {
		super("Licenses");
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		setLayout(new GridBagLayout());
		setIconImages(MainGUI.MOBAC_ICONS);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		JButton ok = new JButton("OK");
		textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.setBackground(this.getBackground());
		JScrollPane textScroller = new JScrollPane(textArea);
		textScroller.setPreferredSize(new Dimension(700, (int) (dim.height * 0.8)));

		tab = new JTabbedPane(JTabbedPane.LEFT, JTabbedPane.WRAP_TAB_LAYOUT);
		Icon icon = new ImageIcon(new BufferedImage(1, 50, BufferedImage.TYPE_INT_ARGB));

		boolean first = true;
		for (LicenseInfo li : licenses) {
			tab.addTab("<html>" + li.name + "</html>", icon, (first) ? textScroller : null);
			first = false;
		}
		tab.addChangeListener(this);
		stateChanged(null);
		add(tab, GBC.eol().anchor(GBC.NORTH).fill());

		// add(textScroller, GBC.eol());
		add(ok, GBC.eol().anchor(GBC.CENTER).insets(5, 10, 10, 10));
		ok.addActionListener(this);
		pack();

		setLocation((dim.width - getWidth()) / 2, (dim.height - getHeight()) / 2);
	}

	public void stateChanged(ChangeEvent event) {
		String license;
		try {
			String nextLicense = licenses[tab.getSelectedIndex()].licenseResource;
			if (nextLicense.equals(currentLicense))
				return;
			license = Utilities.loadTextResource("text/" + nextLicense);
			currentLicense = nextLicense;
		} catch (IOException e) {
			license = "Failed to load license: " + e.getMessage();
		}
		textArea.setText(license);
		textArea.setCaretPosition(0);

	}

	public void actionPerformed(ActionEvent e) {
		dispose();
	}

	private static class LicenseInfo {
		public final String name;
		public final String licenseResource;

		public LicenseInfo(String name, String licenseResource) {
			super();
			this.name = name;
			this.licenseResource = licenseResource;
		}

	}

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			ProgramInfo.initialize(); // Load revision info
			JFrame dlg = new LicensesDialog();
			dlg.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
