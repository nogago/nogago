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
package mobac.mapsources.custom;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import mobac.exceptions.TileException;
import mobac.mapsources.MapSourcesManager;
import mobac.program.interfaces.MapSource;
import mobac.program.interfaces.MapSpace;
import mobac.program.model.MapSourceLoaderInfo;
import mobac.program.model.TileImageType;

@XmlRootElement
/**
 * Wraps an already existing map source so that it can be loaded by name in a custom multi-layer map  
 */
public class StandardMapSourceLayer implements MapSource {

	protected MapSource mapSource = null;

	@XmlElement(name = "name")
	protected String mapSourceName;

	public MapSource getMapSource() {
		return mapSource;
	}

	protected void afterUnmarshal(Unmarshaller u, Object parent) {
		mapSource = MapSourcesManager.getInstance().getSourceByName(mapSourceName);
		if (mapSource == null)
			throw new RuntimeException("Invalid map source name used: " + mapSourceName);
	}

	public int getMaxZoom() {
		return mapSource.getMaxZoom();
	}

	public int getMinZoom() {
		return mapSource.getMinZoom();
	}

	public String getName() {
		return mapSource.getName();
	}

	public byte[] getTileData(int zoom, int x, int y, LoadMethod loadMethod) throws IOException, TileException,
			InterruptedException {
		return mapSource.getTileData(zoom, x, y, loadMethod);
	}

	public BufferedImage getTileImage(int zoom, int x, int y, LoadMethod loadMethod) throws IOException, TileException,
			InterruptedException {
		return mapSource.getTileImage(zoom, x, y, loadMethod);
	}

	public TileImageType getTileImageType() {
		return mapSource.getTileImageType();
	}

	public MapSpace getMapSpace() {
		return mapSource.getMapSpace();
	}

	public Color getBackgroundColor() {
		return mapSource.getBackgroundColor();
	}

	@Override
	public String toString() {
		return mapSource.toString();
	}

	@Override
	public int hashCode() {
		return mapSource.hashCode();
	}

	@XmlTransient
	public MapSourceLoaderInfo getLoaderInfo() {
		return mapSource.getLoaderInfo();
	}

	public void setLoaderInfo(MapSourceLoaderInfo loaderInfo) {
		mapSource.setLoaderInfo(loaderInfo);
	}

	@Override
	public boolean equals(Object obj) {
		return mapSource.equals(obj);
	}

}
