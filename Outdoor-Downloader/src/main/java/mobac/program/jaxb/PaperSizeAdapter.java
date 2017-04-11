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

import javax.xml.bind.annotation.adapters.XmlAdapter;

import mobac.program.model.PaperSize;
import mobac.program.model.PaperSize.Format;

/**
 * Required {@link XmlAdapter} implementation for serializing {@link PaperSize}
 */
public class PaperSizeAdapter extends XmlAdapter<String, PaperSize> {

	private static final String LANDSCAPE = "_LANDSCAPE", SELECTION = "SELECTION", X = "x";

	@Override
	public PaperSize unmarshal(String value) throws Exception {
		if (value.equals(SELECTION)) {
			return null;
		}
		if (value.contains(X)) {
			String split[] = value.split(X);
			double width = Double.parseDouble(split[0]);
			double height = Double.parseDouble(split[1]);
			return new PaperSize(width, height);
		}
		boolean landscape = false;
		if (value.contains(LANDSCAPE)) {
			value = value.substring(0, value.indexOf(LANDSCAPE));
			landscape = true;
		}
		Format format = Format.valueOf(value);
		return new PaperSize(format, landscape);
	}

	@Override
	public String marshal(PaperSize v) throws Exception {
		if (v == null) {
			return SELECTION;
		}
		if (v.format != null) {
			return v.format.name() + (v.landscape ? LANDSCAPE : "");
		} else {
			return v.width + X + v.height;
		}
	}
}
