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

import java.awt.Point;

import javax.xml.bind.UnmarshalException;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Required {@link XmlAdapter} implementation for serializing a {@link Point} as
 * the default one creates a {@link StackOverflowError}
 * 
 */
public class PointAdapter extends XmlAdapter<String, Point> {

	@Override
	public String marshal(Point point) throws Exception {
		return point.x + "/" + point.y;
	}

	@Override
	public Point unmarshal(String value) throws Exception {
		int i = value.indexOf('/');
		if (i < 0)
			throw new UnmarshalException("Invalid format");
		int x = Integer.parseInt(value.substring(0, i).trim());
		int y = Integer.parseInt(value.substring(i + 1).trim());
		return new Point(x, y);
	}
}
