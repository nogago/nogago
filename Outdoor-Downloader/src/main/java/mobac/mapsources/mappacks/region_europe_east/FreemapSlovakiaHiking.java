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
package mobac.mapsources.mappacks.region_europe_east;

import mobac.mapsources.AbstractHttpMapSource;
import mobac.program.interfaces.MapSourceTextAttribution;
import mobac.program.model.TileImageType;

/**
 * http://www.freemap.sk
 * 
 * @author SourceForge.net user didoa, nickn17
 */
public class FreemapSlovakiaHiking extends AbstractHttpMapSource implements MapSourceTextAttribution {

	public FreemapSlovakiaHiking() {
		super("FreemapSlovakiaHiking", 6, 16, TileImageType.PNG, TileUpdate.IfModifiedSince);
	}

	public String getTileUrl(int zoom, int tilex, int tiley) {
		return "http://a.freemap.sk/data/layers/presets/T/" + zoom + "/" + tilex + "/" + tiley + ".png";
	}

	@Override
	public String toString() {
		return "Freemap Slovakia Hiking";
	}

	public String getAttributionText() {
		return "© OpenStreetMap contributors, CC-BY-SA";
	}

	public String getAttributionLinkURL() {
		return "http://openstreetmap.org";
	}
}
