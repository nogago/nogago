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
package mobac.mapsources.mappacks.region_oceania;

import java.net.HttpURLConnection;

import mobac.mapsources.AbstractHttpMapSource;
import mobac.program.interfaces.MapSourceTextAttribution;
import mobac.program.model.TileImageType;

/**
 * <pre>
 * New Zealand Topographic Maps produced by Land Information New Zealand (Government Department).
 * http://www.linz.govt.nz/topography/topo-maps/index.aspx 
 * 
 * Licence: Creative Commons Attribution 3.0 New Zealand
 * (http://creativecommons.org/licenses/by/3.0/nz/deed.en) 
 * 
 * Tiles created by and sourced from nztopomaps.com
 * </pre>
 * http://www.nztopomaps.com/
 */
public class NzTopoMaps extends AbstractHttpMapSource implements MapSourceTextAttribution {

	public NzTopoMaps() {
		super("New Zealand Topographic Maps", 6, 15, TileImageType.PNG, TileUpdate.IfNoneMatch);
	}

	public String getTileUrl(int zoom, int x, int y) {
		// nzy = 2^zoom - 1 - y
		int nzy = (1 << zoom) - 1 - y;
		return "http://nz1.nztopomaps.com/" + zoom + "/" + x + "/" + nzy + ".png";
	}

	@Override
	protected void prepareTileUrlConnection(HttpURLConnection conn) {
		super.prepareTileUrlConnection(conn);
		conn.addRequestProperty("Referer", "http://m.nztopomaps.com");
	}

	@Override
	public String toString() {
		return "nztopomaps.com (New Zealand only)";
	}

	public String getAttributionText() {
		return "Images sourced from NZTopo database";
	}

	public String getAttributionLinkURL() {
		return "http://nztopomaps.com";
	}

}
