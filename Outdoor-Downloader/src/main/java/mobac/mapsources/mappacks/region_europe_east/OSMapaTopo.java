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
 * http://osm.trail.pl/ol.xhtml
 * http://sourceforge.net/tracker/?func=detail&aid=3379692&group_id=238075&atid=1105497
 */
public class OSMapaTopo extends AbstractHttpMapSource implements MapSourceTextAttribution {

	public OSMapaTopo() {
		super("OSMapaTopo", 7, 18, TileImageType.PNG, TileUpdate.IfNoneMatch);
	}

	public String getTileUrl(int zoom, int x, int y) {
		return "http://osm.trail.pl/bezpoziomic/" + zoom + "/" + x + "/" + y + ".png";
	}

	@Override
	public String toString() {
		return "OSMapa-Topo (Poland)";
	}

	public String getAttributionText() {
		// http://wiki.openstreetmap.pl/Serwer_kafelk%C3%B3w_TRAIL
		return "© Data OpenStreetMap, Hosting TRAIL.PL and centuria.pl";
	}

	public String getAttributionLinkURL() {
		return null;
	}

}
