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
/**
 * 
 */
package mobac.mapsources.mappacks.openstreetmap;

import mobac.program.interfaces.HttpMapSource;

public class OsmPublicTransport extends AbstractOsmMapSource {

	private static final String PATTERN = "http://tile.memomaps.de/tilegen/%d/%d/%d.png";

	public OsmPublicTransport() {
		super("OSMPublicTransport");
		this.maxZoom = 16;
		this.minZoom = 2;
		this.tileUpdate = HttpMapSource.TileUpdate.IfNoneMatch;
	}

	@Override
	public String getTileUrl(int zoom, int tilex, int tiley) {
		String url = String.format(PATTERN, new Object[] { zoom, tilex, tiley });
		return url;
	}

	@Override
	public String toString() {
		return "OpenStreetMap Public Transport";
	}

}