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
import java.awt.Point;
import java.io.StringWriter;
import java.util.Enumeration;

import javax.swing.tree.TreeNode;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

import mobac.exceptions.InvalidNameException;
import mobac.program.JobDispatcher.Job;
import mobac.program.download.jobenumerators.DownloadJobEnumerator;
import mobac.program.interfaces.CapabilityDeletable;
import mobac.program.interfaces.DownloadJobListener;
import mobac.program.interfaces.DownloadableElement;
import mobac.program.interfaces.LayerInterface;
import mobac.program.interfaces.MapInterface;
import mobac.program.interfaces.MapSource;
import mobac.program.interfaces.MapSpace;
import mobac.program.interfaces.TileFilter;
import mobac.program.interfaces.ToolTipProvider;
import mobac.program.tilefilter.DummyTileFilter;
import mobac.utilities.tar.TarIndexedArchive;

import org.apache.log4j.Logger;

public class Map implements MapInterface, ToolTipProvider, CapabilityDeletable, TreeNode, DownloadableElement {

	protected String name;

	protected Layer layer;

	protected TileImageParameters parameters = null;

	@XmlAttribute
	protected Point maxTileCoordinate = null;

	@XmlAttribute
	protected Point minTileCoordinate = null;

	@XmlAttribute
	protected MapSource mapSource = null;

	protected Dimension tileDimension = null;

	@XmlAttribute
	protected int zoom;

	private static Logger log = Logger.getLogger(Map.class);

	protected Map() {
	}

	protected Map(Layer layer, String name, MapSource mapSource, int zoom, Point minTileCoordinate,
			Point maxTileCoordinate, TileImageParameters parameters) {
		this.layer = layer;
		this.maxTileCoordinate = maxTileCoordinate;
		this.minTileCoordinate = minTileCoordinate;
		this.name = name;
		this.mapSource = mapSource;
		this.zoom = zoom;
		this.parameters = parameters;
		calculateRuntimeValues();
	}

	protected void calculateRuntimeValues() {
		if (parameters == null) {
			int tileSize = mapSource.getMapSpace().getTileSize();
			tileDimension = new Dimension(tileSize, tileSize);
		} else
			tileDimension = parameters.getDimension();
	}

	public LayerInterface getLayer() {
		return layer;
	}

	@XmlTransient
	public void setLayer(LayerInterface layer) {
		this.layer = (Layer) layer;
	}

	public MapSource getMapSource() {
		return mapSource;
	}

	public Point getMaxTileCoordinate() {
		return maxTileCoordinate;
	}

	public Point getMinTileCoordinate() {
		return minTileCoordinate;
	}

	@XmlAttribute
	public String getName() {
		return name;
	}

	public int getZoom() {
		return zoom;
	}

	@Override
	public String toString() {
		return getName();
	}

	public TileImageParameters getParameters() {
		return parameters;
	}

	public void setParameters(TileImageParameters parameters) {
		this.parameters = parameters;
	}

	public String getInfoText() {
		return "Map\n name=" + name + "\n mapSource=" + mapSource + "\n zoom=" + zoom + "\n maxTileCoordinate="
				+ maxTileCoordinate.x + "/" + maxTileCoordinate.y + "\n minTileCoordinate=" + minTileCoordinate.x + "/"
				+ minTileCoordinate.y + "\n parameters=" + parameters;
	}

	public String getToolTip() {
		MapSpace mapSpace = mapSource.getMapSpace();
		EastNorthCoordinate tl = new EastNorthCoordinate(mapSpace, zoom, minTileCoordinate.x, minTileCoordinate.y);
		EastNorthCoordinate br = new EastNorthCoordinate(mapSpace, zoom, maxTileCoordinate.x, maxTileCoordinate.y);

		StringWriter sw = new StringWriter(1024);
		sw.write("<html>");
		sw.write("<b>Map</b><br>");
		sw.write("Map source: " + mapSource.toString());
		sw.write(" (" + mapSource.getName() + ")<br>");
		sw.write("Zoom level: " + zoom + "<br>");
		sw.write("Area start: " + tl + " (" + minTileCoordinate.x + " / " + minTileCoordinate.y + ")<br>");
		sw.write("Area end: " + br + " (" + maxTileCoordinate.x + " / " + maxTileCoordinate.y + ")<br>");
		sw.write("Map size: " + (maxTileCoordinate.x - minTileCoordinate.x + 1) + "x"
				+ (maxTileCoordinate.y - minTileCoordinate.y + 1) + " pixel<br>");
		if (parameters != null) {
			sw.write("Tile size: " + parameters.getWidth() + "x" + parameters.getHeight() + "<br>");
			sw.write("Tile format: " + parameters.getFormat() + "<br>");
		} else
			sw.write("Tile size: 256x256 (no processing)<br>");
		sw.write("Maximum tiles to download: " + calculateTilesToDownload() + "<br>");
		sw.write("</html>");
		return sw.toString();
	}

	public Dimension getTileSize() {
		return tileDimension;
	}

	public double getMinLat() {
		return mapSource.getMapSpace().cYToLat(maxTileCoordinate.y, zoom);
	}

	public double getMaxLat() {
		return mapSource.getMapSpace().cYToLat(minTileCoordinate.y, zoom);
	}

	public double getMinLon() {
		return mapSource.getMapSpace().cXToLon(minTileCoordinate.x, zoom);
	}

	public double getMaxLon() {
		return mapSource.getMapSpace().cXToLon(maxTileCoordinate.x, zoom);
	}

	public void delete() {
		layer.deleteMap(this);
	}

	public void setName(String newName) throws InvalidNameException {
		if (layer != null) {
			for (MapInterface map : layer) {
				if ((map != this) && (newName.equals(map.getName())))
					throw new InvalidNameException("There is already a map named \"" + newName
							+ "\" in this layer.\nMap names have to unique within an layer.");
			}
		}
		this.name = newName;
	}

	public Enumeration<?> children() {
		return null;
	}

	public boolean getAllowsChildren() {
		return false;
	}

	public TreeNode getChildAt(int childIndex) {
		return null;
	}

	public int getChildCount() {
		return 0;
	}

	public int getIndex(TreeNode node) {
		return 0;
	}

	public TreeNode getParent() {
		return (TreeNode) layer;
	}

	public boolean isLeaf() {
		return true;
	}

	public long calculateTilesToDownload() {
		int tileSize = mapSource.getMapSpace().getTileSize();
		// This algorithm has to be identically to those used in
		// @DownloadJobEnumerator
		int xMin = minTileCoordinate.x / tileSize;
		int xMax = maxTileCoordinate.x / tileSize;
		int yMin = minTileCoordinate.y / tileSize;
		int yMax = maxTileCoordinate.y / tileSize;
		int width = xMax - xMin + 1;
		int height = yMax - yMin + 1;
		int tileCount = width * height;
		// TODO correct tile count in case of multi-layer maps
		// if (mapSource instanceof MultiLayerMapSource) {
		// // We have a map with two layers and for each layer we have to
		// // download the tiles - therefore double the tileCount
		// tileCount *= 2;
		// }
		return tileCount;
	}

	public boolean checkData() {
		boolean result = false;
		boolean[] checks = { name == null, // 0
				layer == null, // 1
				maxTileCoordinate == null, // 2
				minTileCoordinate == null, // 3
				mapSource == null, // 4
				zoom < 0 // 5
		};

		for (int i = 0; i < checks.length; i++)
			if (checks[i]) {
				log.error("Problem detectected with map \"" + name + "\" check: " + i);
				result = true;
			}
		// Automatically correct bad ordered min/max coordinates
		try {
			if (minTileCoordinate.x > maxTileCoordinate.x) {
				int tmp = maxTileCoordinate.x;
				maxTileCoordinate.x = minTileCoordinate.x;
				minTileCoordinate.x = tmp;
			}
			if (minTileCoordinate.y > maxTileCoordinate.y) {
				int tmp = maxTileCoordinate.y;
				maxTileCoordinate.y = minTileCoordinate.y;
				minTileCoordinate.y = tmp;
			}
		} catch (Exception e) {
		}

		return result;
	}

	public MapInterface deepClone(LayerInterface newLayer) {
		try {
			Map map = this.getClass().newInstance();
			map.layer = (Layer) newLayer;
			map.mapSource = mapSource;
			map.maxTileCoordinate = (Point) maxTileCoordinate.clone();
			map.minTileCoordinate = (Point) minTileCoordinate.clone();
			map.name = name;
			if (parameters != null)
				map.parameters = (TileImageParameters) parameters.clone();
			else
				map.parameters = null;
			map.tileDimension = (Dimension) tileDimension.clone();
			map.zoom = zoom;
			return map;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Needs to be public - otherwise it will be kicked by ProGuard!
	 */
	public void afterUnmarshal(Unmarshaller u, Object parent) {
		this.layer = (Layer) parent;
		calculateRuntimeValues();
	}

	public Enumeration<Job> getDownloadJobs(TarIndexedArchive tileArchive, DownloadJobListener listener) {
		return new DownloadJobEnumerator(this, mapSource, tileArchive, listener);
	}

	public TileFilter getTileFilter() {
		return new DummyTileFilter();
	}

}
