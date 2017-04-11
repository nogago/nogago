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
package mobac.gui.components;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import mobac.utilities.Utilities;
import mobac.utilities.geo.CoordinateDms2Format;

public class JCoordinateField extends JTextField {

	private static final long serialVersionUID = 1L;

	private static final Color ERROR_COLOR = new Color(255, 100, 100);

	private static final String INVALID_TEXT = "<html>Invalid coordinate!<br>"
			+ "Please enter a number between %s and %s</html>";

	private JCoordinateListener coordinateListener;
	private boolean inputIsValid = true;

	private NumberFormat numberFormat;

	private final double min;
	private final double max;

	public JCoordinateField(double min, double max) {
		super(10);
		this.min = min;
		this.max = max;
		numberFormat = new CoordinateDms2Format(new DecimalFormatSymbols());
		coordinateListener = new JCoordinateListener();
		coordinateListener.checkCoordinate(null);
	}

	@Override
	public Point getToolTipLocation(MouseEvent event) {
		if (getToolTipText().length() > 0)
			return super.getToolTipLocation(event);
		else
			// We don't want a tool tip but Java does not allow to disable it?
			// -> show it at a point where no user will ever see it
			return new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);
	}

	public void setCoordinate(double value) {
		try {
			// We know that the number is valid, therefore we can skip the check
			// -> saves CPU power while selecting via preview map
			boolean newValid = true;
			coordinateListener.setEnabled(false);
			if (Double.isNaN(value)) {
				super.setText("");
				newValid = false;
			} else {
				super.setText(numberFormat.format(value));
			}
			if (newValid != inputIsValid)
				coordinateListener.changeValidMode(true);
		} finally {
			coordinateListener.setEnabled(true);
		}
	}

	public double getCoordinate() throws ParseException {
		ParsePosition pos = new ParsePosition(0);
		String text = JCoordinateField.this.getText();
		Number num = numberFormat.parse(text, pos);
		if (num == null || pos.getErrorIndex() >= 0 || Double.isNaN(num.doubleValue()))
			throw new ParseException(text, pos.getErrorIndex());
		return num.doubleValue();
	}

	public double getCoordinateOrNaN() {
		ParsePosition pos = new ParsePosition(0);
		String text = JCoordinateField.this.getText();
		Number num = numberFormat.parse(text, pos);
		if (num == null || pos.getErrorIndex() >= 0)
			return Double.NaN;
		return num.doubleValue();
	}

	public boolean isInputValid() {
		return inputIsValid;
	}

	public NumberFormat getNumberFormat() {
		return numberFormat;
	}

	public void setNumberFormat(NumberFormat numberFormat) {
		double coord = getCoordinateOrNaN();
		this.numberFormat = numberFormat;
		setCoordinate(coord);
	}

	protected class JCoordinateListener implements DocumentListener {

		private Color defaultColor;

		private boolean enabled;

		private JCoordinateListener() {
			enabled = true;
			defaultColor = JCoordinateField.this.getBackground();
			JCoordinateField.this.getDocument().addDocumentListener(this);
		}

		private void checkCoordinate(DocumentEvent de) {
			if (!enabled)
				return;
			boolean valid = false;
			try {
				ParsePosition pos = new ParsePosition(0);
				String text = JCoordinateField.this.getText();
				Number num = numberFormat.parse(text, pos);
				if (num == null) {
					valid = false;
					return;
				}
				double d = num.doubleValue();
				valid = (!Double.isNaN(d)) && (d >= min) && (d <= max);
			} catch (Exception e) {
				valid = false;
			}
			if (valid != inputIsValid)
				changeValidMode(valid);
		}

		private void changeValidMode(boolean valid) {
			Color newC = valid ? defaultColor : ERROR_COLOR;
			JCoordinateField.this.setBackground(newC);
			String toolTip = valid ? "" : String.format(INVALID_TEXT, numberFormat.format(min),
					numberFormat.format(max));
			JCoordinateField.this.setToolTipText(toolTip);
			if (toolTip.length() > 0)
				Utilities.showTooltipNow(JCoordinateField.this);
			inputIsValid = valid;
		}

		public void changedUpdate(DocumentEvent e) {
			checkCoordinate(e);
		}

		public void insertUpdate(DocumentEvent e) {
			checkCoordinate(e);
		}

		public void removeUpdate(DocumentEvent e) {
			checkCoordinate(e);
		}

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}
	}
}
