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
package mobac.program.interfaces;

import java.awt.Dimension;
import java.awt.Point;

import mobac.program.model.TileImageParameters;



public interface MapInterface extends AtlasObject, CapabilityDeletable {

	public Point getMinTileCoordinate();

	public Point getMaxTileCoordinate();

	public int getZoom();

	public MapSource getMapSource();

	public Dimension getTileSize();

	public LayerInterface getLayer();

	public void setLayer(LayerInterface layer);

	public TileImageParameters getParameters();

	public void setParameters(TileImageParameters p);

	public long calculateTilesToDownload();

	public String getInfoText();
	
	public TileFilter getTileFilter();
	
	public MapInterface deepClone(LayerInterface newLayer);

}
