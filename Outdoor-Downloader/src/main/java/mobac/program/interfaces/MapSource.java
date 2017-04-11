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

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import mobac.exceptions.TileException;
import mobac.exceptions.UnrecoverableDownloadException;
import mobac.gui.mapview.JMapViewer;
import mobac.program.jaxb.MapSourceAdapter;
import mobac.program.model.MapSourceLoaderInfo;
import mobac.program.model.TileImageType;

//License: GPL. Copyright 2008 by Jan Peter Stotz

/**
 * 
 * @author Jan Peter Stotz
 */
@XmlJavaTypeAdapter(MapSourceAdapter.class)
public interface MapSource {

	public enum LoadMethod {
		DEFAULT, CACHE, SOURCE
	};

	/**
	 * Specifies the maximum zoom value. The number of zoom levels is [0.. {@link #getMaxZoom()}].
	 * 
	 * @return maximum zoom value that has to be smaller or equal to {@link JMapViewer#MAX_ZOOM}
	 */
	public int getMaxZoom();

	/**
	 * Specifies the minimum zoom value. This value is usually 0. Only for maps that cover a certain region up to a
	 * limited zoom level this method should return a value different than 0.
	 * 
	 * @return minimum zoom value - usually 0
	 */
	public int getMinZoom();

	/**
	 * A tile layer name has to be unique and has to consist only of characters valid for filenames.
	 * 
	 * @return Name of the tile layer
	 */
	public String getName();

	/**
	 * 
	 * @param zoom
	 * @param x
	 * @param y
	 * @param loadMethod
	 *            TODO
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws UnrecoverableDownloadException
	 */
	public byte[] getTileData(int zoom, int x, int y, LoadMethod loadMethod) throws IOException, TileException,
			InterruptedException;

	/**
	 * 
	 * @param zoom
	 * @param x
	 * @param y
	 * @param loadMethod
	 * @return
	 * @throws IOException
	 * @throws UnrecoverableDownloadException
	 * @throws InterruptedException
	 */
	public BufferedImage getTileImage(int zoom, int x, int y, LoadMethod loadMethod) throws IOException, TileException,
			InterruptedException;

	/**
	 * Specifies the tile image type. For tiles rendered by Mapnik or Osmarenderer this is usually
	 * {@link TileImageType#PNG}.
	 * 
	 * @return file extension of the tile image type
	 */
	public TileImageType getTileImageType();

	public MapSpace getMapSpace();

	public Color getBackgroundColor();

	@XmlTransient
	public MapSourceLoaderInfo getLoaderInfo();

	public void setLoaderInfo(MapSourceLoaderInfo loaderInfo);

}
