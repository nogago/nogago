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

import java.awt.Dimension;

import javax.xml.bind.UnmarshalException;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Required {@link XmlAdapter} implementation for serializing a
 * {@link Dimension} as the default one creates a {@link StackOverflowError}
 */
public class DimensionAdapter extends XmlAdapter<String, Dimension> {

	@Override
	public String marshal(Dimension dimension) throws Exception {
		return dimension.width + "/" + dimension.height;
	}

	@Override
	public Dimension unmarshal(String value) throws Exception {
		int i = value.indexOf('/');
		if (i < 0)
			throw new UnmarshalException("Invalid format");
		int width = Integer.parseInt(value.substring(0, i));
		int height = Integer.parseInt(value.substring(i + 1));
		return new Dimension(width, height);
	}
}
