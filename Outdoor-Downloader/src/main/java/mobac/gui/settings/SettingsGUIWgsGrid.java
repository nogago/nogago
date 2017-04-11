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
package mobac.gui.settings;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import mobac.gui.dialogs.FontChooser;
import mobac.program.model.Settings;
import mobac.program.model.SettingsWgsGrid;
import mobac.utilities.GBCTable;

public class SettingsGUIWgsGrid extends JPanel {

	private static final long serialVersionUID = -3067609813682787669L;

	private final FontChooser fontChooser = new FontChooser();

	private final JButton jButtonFont = new JButton(FontChooser.encodeFont(FontChooser.DEFAULT));

	private final JCheckBox jCheckBoxCompressLabels = new JCheckBox();

	private final JPanel jPanelColor = new JPanel();

	private final SpinnerNumberModel modelWidth = new SpinnerNumberModel(0.5d, 0.5d, 5.0d, 0.5d);

	private final JSpinner jSpinnerWidth = new JSpinner(modelWidth);

	private JLabel jLabelColor = new JLabel(), jLabelFont = new JLabel(), jLabelWidth = new JLabel();

	private String title;

	public SettingsGUIWgsGrid() {
		super(new GridBagLayout());
		i18n();

		jButtonFont.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fontChooser.show();
				if (fontChooser.wasCanceled()) {
					return;
				}
				String text = FontChooser.encodeFont(fontChooser.getFont());
				jButtonFont.setText(text);
			}
		});

		jPanelColor.setPreferredSize(new Dimension(64, 18));
		jPanelColor.setOpaque(true);
		jPanelColor.setBorder(BorderFactory.createEtchedBorder());
		jPanelColor.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				Color color = jPanelColor.getBackground();
				color = JColorChooser.showDialog(jPanelColor, title, color);
				if (color != null) {
					jPanelColor.setBackground(color);
				}
			}

			public void mouseEntered(MouseEvent e) {
				jPanelColor.setBorder(BorderFactory.createRaisedBevelBorder());
			}

			public void mouseExited(MouseEvent e) {
				jPanelColor.setBorder(BorderFactory.createEtchedBorder());
			}
		});

		GBCTable gbc = new GBCTable();
		add(jLabelColor, gbc.begin());
		add(jLabelWidth, gbc.incY());
		add(jPanelColor, gbc.incX());
		add(jSpinnerWidth, gbc.incY());
		add(jLabelFont, gbc.incX());
		add(jCheckBoxCompressLabels, gbc.incY().gridwidth(3));
		add(jButtonFont, gbc.incX());
		add(Box.createHorizontalGlue(), gbc.incX().fillH());
	}

	public void i18n() {
		jCheckBoxCompressLabels.setText("Compress labels");
		jCheckBoxCompressLabels
				.setToolTipText("If selected, the coordinate labels will not containt explicit information.");
		setBorder(SettingsGUI.createSectionBorder("WGS Grid"));
		title = "Choose grid color";
		jLabelWidth.setText("Width:");
		String width = "The width of the lines.";
		jLabelWidth.setToolTipText(width);
		jSpinnerWidth.setToolTipText(width);
		jLabelColor.setText("Color:");
		String color = "The color of the lines and coordinate labels.";
		jLabelColor.setToolTipText(color);
		jPanelColor.setToolTipText(color);
		jLabelFont.setText("Font:");
		String font = "Chooses the font to be used for the coordinate labels.";
		jLabelFont.setToolTipText(font);
		jButtonFont.setToolTipText(font);
	}

	public void applySettings(Settings s) {
		applySettings(s.wgsGrid);
	}

	public void applySettings(SettingsWgsGrid s) {
		s.compressLabels = jCheckBoxCompressLabels.isSelected();
		s.font = fontChooser.getFont();
		s.color = jPanelColor.getBackground();
		s.width = modelWidth.getNumber().floatValue();
	}

	public void loadSettings(Settings s) {
		loadSettings(s.wgsGrid);
	}

	public void loadSettings(SettingsWgsGrid s) {
		jCheckBoxCompressLabels.setSelected(s.compressLabels);
		fontChooser.setFont(s.font);
		jButtonFont.setText(FontChooser.encodeFont(s.font));
		jPanelColor.setBackground(s.color);
		modelWidth.setValue((double) s.width);
	}
}
