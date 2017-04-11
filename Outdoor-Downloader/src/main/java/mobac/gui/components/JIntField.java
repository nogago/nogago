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

import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import mobac.program.model.NumericDocument;
import mobac.utilities.Utilities;


public class JIntField extends JTextField {

	private static final long serialVersionUID = 1L;

	protected Color errorColor = new Color(255, 100, 100);

	public int min = 0;

	public int max = 0;

	private String errorText;

	private InputListener listener;
	private boolean inputIsValid = true;

	public JIntField(int min, int max, int columns, String errorText) {
		super(columns);
		this.min = min;
		this.max = max;
		this.errorText = errorText;
		setDocument(new NumericDocument());
		listener = new InputListener();
		listener.checkInput(null);
		setBorder(new EmptyBorder(2, 2, 2, 0));
	}

	public void setErrorColor(Color c) {
		errorColor = c;
	}

	public int getValue() throws NumberFormatException {
		return Integer.parseInt(getText());
	}

	public void setValue(int newValue, boolean check) {
		if (newValue <= 0)
			super.setText("");
		else
			super.setText(Integer.toString(newValue));
		if (check)
			listener.checkInput(null);
	}

	public void setText(String t) {
		throw new RuntimeException("Calling setText() is not allowed!");
	}

	public boolean isInputValid() {
		return testInputValid();
	}

	private boolean testInputValid() {
		try {
			int i = Integer.parseInt(getText());
			return (i >= min) && (i <= max);
		} catch (NumberFormatException e) {
			return false;
		}
	}

	
	
	protected class InputListener implements DocumentListener {

		private Color defaultColor;

		private InputListener() {
			defaultColor = JIntField.this.getBackground();
			JIntField.this.getDocument().addDocumentListener(this);
		}

		private void checkInput(DocumentEvent de) {
			boolean valid = false;
			try {
				valid = testInputValid();
			} catch (Exception e) {
				valid = false;
			}
			if (valid != inputIsValid)
				setDisplayedValidMode(valid);
			inputIsValid = valid;
		}

		private void setDisplayedValidMode(boolean valid) {
			Color newC = valid ? defaultColor : errorColor;
			JIntField.this.setBackground(newC);
			String toolTip = valid ? "" : String.format(errorText, new Object[] { min, max });
			JIntField.this.setToolTipText(toolTip);
			if (toolTip.length() > 0)
				Utilities.showTooltipNow(JIntField.this);
		}

		public void changedUpdate(DocumentEvent e) {
			checkInput(e);
		}

		public void insertUpdate(DocumentEvent e) {
			checkInput(e);
		}

		public void removeUpdate(DocumentEvent e) {
			checkInput(e);
		}

	}
}
