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

import java.io.StringWriter;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.tree.TreeNode;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import mobac.program.interfaces.AtlasInterface;
import mobac.program.interfaces.LayerInterface;
import mobac.program.interfaces.ToolTipProvider;
import mobac.utilities.Utilities;

@XmlRootElement
public class Atlas implements AtlasInterface, ToolTipProvider, TreeNode {

	public static final int CURRENT_ATLAS_VERSION = 1;

	@XmlAttribute
	private int version = 0;

	private String name = "Unnamed";

	@XmlElements({ @XmlElement(name = "Layer", type = Layer.class) })
	private List<LayerInterface> layers = new LinkedList<LayerInterface>();

	private AtlasOutputFormat outputFormat = AtlasOutputFormat.FORMATS.get(0);

	public static Atlas newInstance() {
		Atlas atlas = new Atlas();
		atlas.version = CURRENT_ATLAS_VERSION;
		return atlas;
	}

	private Atlas() {
		super();
	}

	public void addLayer(LayerInterface l) {
		layers.add(l);
	}

	public void deleteLayer(LayerInterface l) {
		layers.remove(l);
	}

	public LayerInterface getLayer(int index) {
		return layers.get(index);
	}

	public int getLayerCount() {
		return layers.size();
	}

	@XmlAttribute
	public String getName() {
		return name;
	}

	public void setName(String newName) {
		this.name = newName;
	}

	@XmlAttribute
	public AtlasOutputFormat getOutputFormat() {
		return outputFormat;
	}

	public void setOutputFormat(AtlasOutputFormat atlasOutputFormat) {
		if (atlasOutputFormat == null)
			throw new NullPointerException();
		this.outputFormat = atlasOutputFormat;
	}

	@Override
	public String toString() {
		return getName() + " (" + outputFormat + ")";
	}

	public Iterator<LayerInterface> iterator() {
		return layers.iterator();
	}

	public long calculateTilesToDownload() {
		long tiles = 0;
		for (LayerInterface layer : layers)
			tiles += layer.calculateTilesToDownload();
		return tiles;
	}

	public boolean checkData() {
		if (name == null) // name set?
			return true;
		// Check for duplicate layer names
		HashSet<String> names = new HashSet<String>(layers.size());
		for (LayerInterface layer : layers)
			names.add(layer.getName());
		if (names.size() < layers.size())
			return true; // at least one duplicate name found
		return false;
	}

	public double getMinLat() {
		double lat = 90d;
		for (LayerInterface l : layers) {
			lat = Math.min(lat, l.getMinLat());
		}
		return lat;
	}

	public double getMaxLat() {
		double lat = -90d;
		for (LayerInterface l : layers) {
			lat = Math.max(lat, l.getMaxLat());
		}
		return lat;
	}

	public double getMinLon() {
		double lon = 180d;
		for (LayerInterface l : layers) {
			lon = Math.min(lon, l.getMinLon());
		}
		return lon;
	}

	public double getMaxLon() {
		double lon = -180d;
		for (LayerInterface l : layers) {
			lon = Math.max(lon, l.getMaxLon());
		}
		return lon;
	}

	public String getToolTip() {
		StringWriter sw = new StringWriter(1024);
		sw.write("<html>");
		sw.write("<b>Atlas</b><br>");
		sw.write("Name: " + name + "<br>");
		sw.write("Layer count: " + layers.size() + "<br>");
		sw.write("Atlas format: " + outputFormat + "<br>");
		sw.write("Maximum tiles to download: " + calculateTilesToDownload() + "<br>");
		sw.write(String.format("Area start: %s %s<br>", Utilities.prettyPrintLatLon(getMaxLat(), true),
				Utilities.prettyPrintLatLon(getMinLon(), false)));
		sw.write(String.format("Area end: %s %s<br>", Utilities.prettyPrintLatLon(getMinLat(), true),
				Utilities.prettyPrintLatLon(getMaxLon(), false)));
		sw.write("</html>");
		return sw.toString();
	}

	public Enumeration<?> children() {
		return Collections.enumeration(layers);
	}

	public boolean getAllowsChildren() {
		return true;
	}

	public TreeNode getChildAt(int childIndex) {
		return (TreeNode) layers.get(childIndex);
	}

	public int getChildCount() {
		return layers.size();
	}

	public int getIndex(TreeNode node) {
		return layers.indexOf(node);
	}

	public TreeNode getParent() {
		return null;
	}

	public boolean isLeaf() {
		return false;
	}

	public int getVersion() {
		return version;
	}

	public AtlasInterface deepClone() {
		Atlas atlas = new Atlas();
		atlas.version = version;
		atlas.name = name;
		atlas.outputFormat = outputFormat;
		for (LayerInterface layer : layers) {
			atlas.layers.add(layer.deepClone(atlas));
		}
		return atlas;
	}

}
