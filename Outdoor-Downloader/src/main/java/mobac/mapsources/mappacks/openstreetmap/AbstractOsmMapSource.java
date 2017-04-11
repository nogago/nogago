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
package mobac.mapsources.mappacks.openstreetmap;

import java.net.HttpURLConnection;

import mobac.mapsources.AbstractHttpMapSource;
import mobac.program.ProgramInfo;
import mobac.program.interfaces.MapSourceTextAttribution;
import mobac.program.model.TileImageType;

public abstract class AbstractOsmMapSource extends AbstractHttpMapSource implements MapSourceTextAttribution {

	public AbstractOsmMapSource(String name) {
		super(name, 0, 18, TileImageType.PNG);
	}

	public String getTileUrl(int zoom, int tilex, int tiley) {
		return "/" + zoom + "/" + tilex + "/" + tiley + ".png";
	}

	public TileImageType getTileImageType() {
		return TileImageType.PNG;
	}

	@Override
	protected void prepareTileUrlConnection(HttpURLConnection conn) {
		super.prepareTileUrlConnection(conn);
		conn.setRequestProperty("User-agent", ProgramInfo.getUserAgent());
	}

	public String getAttributionText() {
		return "© OpenStreetMap contributors, CC-BY-SA";
	}

	public String getAttributionLinkURL() {
		return "http://openstreetmap.org";
	}
}