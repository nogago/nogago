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
package mobac.program.model;

import java.util.Comparator;
import java.util.TreeMap;

import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.namespace.QName;

/**
 * A map implementation for catching all attributes via {@link XmlAnyAttribute}
 */
public class AnyAttributeMap extends TreeMap<QName, Object> {

	public AnyAttributeMap() {
		super(new QNameComparator());
	}

	public static class QNameComparator implements Comparator<QName> {

		public int compare(QName o1, QName o2) {
			return o1.getLocalPart().compareTo(o2.getLocalPart());
		}
	}

	public String getAttr(String key) {
		return (String) get(new QName(key));
	}

	public void setAttr(String key, String value) {
		put(new QName(key), value);
	}

	public int getInt(TileImageParameters.Name key) {
		return Integer.parseInt(getAttr(key.name()));
	}

	public int getInt(String key) {
		return Integer.parseInt(getAttr(key));
	}

	public void setInt(String key, int value) {
		put(new QName(key), Integer.toString(value));
	}
}
