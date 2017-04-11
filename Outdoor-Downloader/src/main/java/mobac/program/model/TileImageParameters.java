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

import java.awt.Dimension;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public final class TileImageParameters implements Cloneable {

	public static enum Name {
		width, height, format, format_png, format_jpg
	}

	@XmlAnyAttribute
	protected AnyAttributeMap attr = new AnyAttributeMap();

	/**
	 * Default constructor as required by JAXB
	 */
	protected TileImageParameters() {
		super();
	}

	private TileImageParameters(AnyAttributeMap attrMap) {
		attr.putAll(attrMap);
	}

	protected void afterUnmarshal(Unmarshaller u, Object parent) {
		// read all values once for detecting problems
		attr.getInt(Name.height.name());
		attr.getInt(Name.width.name());
		TileImageFormat.valueOf(attr.getAttr("format"));
	}

	public TileImageParameters(int width, int height, TileImageFormat format) {
		super();
		attr.setAttr("format", format.name());
		attr.setInt("height", height);
		attr.setInt("width", width);
	}

	public int getWidth() {
		return attr.getInt("width");
	}

	public int getHeight() {
		return attr.getInt("height");
	}

	public Dimension getDimension() {
		return new Dimension(getWidth(), getHeight());
	}

	public TileImageFormat getFormat() {
		return TileImageFormat.valueOf(attr.getAttr("format"));
	}

	@Override
	public String toString() {
		return "Tile size: (" + getWidth() + "/" + getHeight() + ") " + getFormat().toString() + ")";
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return new TileImageParameters(attr);
	}

}
