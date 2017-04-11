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
package mobac.gui.panels;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import mobac.gui.components.FilledLayeredPane;
import mobac.gui.components.JCollapsiblePanel;
import mobac.gui.components.JCoordinateField;
import mobac.gui.components.JDropDownButton;
import mobac.program.interfaces.MapSource;
import mobac.program.model.CoordinateStringFormat;
import mobac.program.model.EastNorthCoordinate;
import mobac.program.model.MapSelection;
import mobac.program.model.MercatorPixelCoordinate;
import mobac.utilities.GBC;



/**
 * Encapsulates all interface components and code for the panel that shows the
 * coordinates of the current selection and allows the user to enter own
 * coordinates.
 */
public class JCoordinatesPanel extends JCollapsiblePanel {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "Coordinates";

	private JCoordinateField latMinTextField;
	private JCoordinateField latMaxTextField;
	private JCoordinateField lonMinTextField;
	private JCoordinateField lonMaxTextField;
	private JButton applySelectionButton;

	private CoordinateStringFormat csf = CoordinateStringFormat.DEG_ENG;

	public JCoordinatesPanel() {
		super("Selection coordinates (min/max)", new GridBagLayout());
		setName(NAME);
		// coordinates panel
		latMaxTextField = new JCoordinateField(MapSelection.LAT_MIN, MapSelection.LAT_MAX);
		latMaxTextField.setActionCommand("latMaxTextField");
		lonMinTextField = new JCoordinateField(MapSelection.LON_MIN, MapSelection.LON_MAX);
		lonMinTextField.setActionCommand("longMinTextField");
		lonMaxTextField = new JCoordinateField(MapSelection.LON_MIN, MapSelection.LON_MAX);
		lonMaxTextField.setActionCommand("longMaxTextField");
		latMinTextField = new JCoordinateField(MapSelection.LAT_MIN, MapSelection.LAT_MAX);
		latMinTextField.setActionCommand("latMinTextField");

		applySelectionButton = new JButton("Select entered coordinates");

		JLabel latMaxLabel = new JLabel("N ", JLabel.CENTER);
		JLabel lonMinLabel = new JLabel("W ", JLabel.CENTER);
		JLabel lonMaxLabel = new JLabel("E ", JLabel.CENTER);
		JLabel latMinLabel = new JLabel("S ", JLabel.CENTER);

		JPanel northPanel = new JPanel(new BorderLayout());
		JLayeredPane layeredPane = new FilledLayeredPane();

		JPanel northInnerPanel = new JPanel();
		northInnerPanel.add(latMaxLabel);
		northInnerPanel.add(latMaxTextField);

		JPanel formatButtonPanel = new JPanel(null);
		formatButtonPanel.setOpaque(false);
		JDropDownButton formatButton = new JDropDownButton("Fmt");
		formatButton.setMargin(new Insets(0, 5, 0, 0));
		formatButton.setBounds(2, 2, 50, 20);
		formatButtonPanel.add(formatButton);
		for (CoordinateStringFormat csf : CoordinateStringFormat.values())
			formatButton.addDropDownItem(new JNumberFormatMenuItem(csf));

		layeredPane.add(northInnerPanel, Integer.valueOf(0));
		layeredPane.setMinimumSize(northInnerPanel.getMinimumSize());
		layeredPane.setPreferredSize(northInnerPanel.getPreferredSize());
		layeredPane.add(formatButtonPanel, Integer.valueOf(2));
		northPanel.add(layeredPane, BorderLayout.CENTER);

		// northPanel.add(northInnerPanel, BorderLayout.CENTER);
		contentContainer.add(northPanel, GBC.eol().fillH().insets(0, 5, 0, 0));

		JPanel eastWestPanel = new JPanel(new GridBagLayout());
		eastWestPanel.add(lonMinLabel, GBC.std());
		eastWestPanel.add(lonMinTextField, GBC.std());
		eastWestPanel.add(lonMaxLabel, GBC.std().insets(10, 0, 0, 0));
		eastWestPanel.add(lonMaxTextField, GBC.std());
		contentContainer.add(eastWestPanel, GBC.eol().fill());

		JPanel southPanel = new JPanel();
		southPanel.add(latMinLabel);
		southPanel.add(latMinTextField);
		contentContainer.add(southPanel, GBC.eol().anchor(GBC.CENTER));
		contentContainer.add(applySelectionButton, GBC.eol().anchor(GBC.CENTER).insets(0, 5, 0, 0));
	}

	public void setNumberFormat(CoordinateStringFormat csf) {
		this.csf = csf;
		NumberFormat numberFormat = csf.getNumberFormat();
		latMaxTextField.setNumberFormat(numberFormat);
		latMinTextField.setNumberFormat(numberFormat);
		lonMaxTextField.setNumberFormat(numberFormat);
		lonMinTextField.setNumberFormat(numberFormat);
	}

	public CoordinateStringFormat getNumberFormat() {
		return csf;
	}

	protected void setNumberFormat(NumberFormat numberFormat) {
		latMaxTextField.setNumberFormat(numberFormat);
		latMinTextField.setNumberFormat(numberFormat);
		lonMaxTextField.setNumberFormat(numberFormat);
		lonMinTextField.setNumberFormat(numberFormat);
	}

	public void setCoordinates(EastNorthCoordinate max, EastNorthCoordinate min) {
		latMaxTextField.setCoordinate(max.lat);
		lonMaxTextField.setCoordinate(max.lon);
		latMinTextField.setCoordinate(min.lat);
		lonMinTextField.setCoordinate(min.lon);
	}

	public void setCoordinates(MapSelection ms) {
		MercatorPixelCoordinate max = ms.getBottomRightPixelCoordinate();
		MercatorPixelCoordinate min = ms.getTopLeftPixelCoordinate();
		setSelection(max, min);
	}

	public void setSelection(MercatorPixelCoordinate max, MercatorPixelCoordinate min) {
		EastNorthCoordinate c1 = min.getEastNorthCoordinate();
		EastNorthCoordinate c2 = max.getEastNorthCoordinate();
		latMaxTextField.setCoordinate(c1.lat);
		lonMaxTextField.setCoordinate(c2.lon);
		latMinTextField.setCoordinate(c2.lat);
		lonMinTextField.setCoordinate(c1.lon);
	}

	/**
	 * Checks if the values for min/max langitude and min/max latitude are
	 * interchanged (smaller value in the max field and larger value in the min
	 * field) and swaps them if necessary.
	 */
	public void correctMinMax() {
		try {
			double lat1 = latMaxTextField.getCoordinate();
			double lat2 = latMinTextField.getCoordinate();
			if (lat1 < lat2) {
				String tmp = latMaxTextField.getText();
				latMaxTextField.setText(latMinTextField.getText());
				latMinTextField.setText(tmp);
			}
		} catch (ParseException e) {
			// one of the lat fields contains an invalid coordinate
		}
		try {
			double lon1 = lonMaxTextField.getCoordinate();
			double lon2 = lonMinTextField.getCoordinate();
			if (lon1 < lon2) {
				String tmp = lonMaxTextField.getText();
				lonMaxTextField.setText(lonMinTextField.getText());
				lonMinTextField.setText(tmp);
			}
		} catch (ParseException e) {
			// one of the lon fields contains an invalid coordinate
		}
	}

	public MapSelection getMapSelection(MapSource mapSource) {
		EastNorthCoordinate max = getMaxCoordinate();
		EastNorthCoordinate min = getMinCoordinate();
		return new MapSelection(mapSource, max, min);
	}

	public EastNorthCoordinate getMaxCoordinate() {
		return new EastNorthCoordinate(latMaxTextField.getCoordinateOrNaN(), lonMaxTextField
				.getCoordinateOrNaN());
	}

	public EastNorthCoordinate getMinCoordinate() {
		return new EastNorthCoordinate(latMinTextField.getCoordinateOrNaN(), lonMinTextField
				.getCoordinateOrNaN());
	}

	public void addButtonActionListener(ActionListener l) {
		applySelectionButton.addActionListener(l);
	}

	protected class JNumberFormatMenuItem extends JMenuItem implements ActionListener {

		private final CoordinateStringFormat csf;

		public JNumberFormatMenuItem(CoordinateStringFormat csf) {
			super(csf.getDisplayName());
			this.csf = csf;
			addActionListener(this);
		}

		public void actionPerformed(ActionEvent e) {
			JCoordinatesPanel.this.setNumberFormat(csf);
		}

	}
}
