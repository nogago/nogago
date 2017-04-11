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
package mobac.program.jaxb;

import java.awt.Font;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import mobac.gui.dialogs.FontChooser;

/**
 * Required {@link XmlAdapter} implementation for serializing {@link Font}
 */
public class FontAdapter extends XmlAdapter<String, Font> {

	@Override
	public Font unmarshal(String value) throws Exception {
		return Font.decode(value);
	}

	@Override
	public String marshal(Font font) throws Exception {
		return FontChooser.encodeFont(font);
	}
}
