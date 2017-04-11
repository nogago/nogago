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
package mobac.mapsources.mappacks.region_america_north;

import mobac.mapsources.AbstractHttpMapSource;
import mobac.program.model.TileImageType;

public class FAA_Aeromaps extends AbstractHttpMapSource {

	public FAA_Aeromaps() {
		super("FAA Sectional Charts", 4, 12, TileImageType.PNG, TileUpdate.LastModified);
	}

	public String getTileUrl(int zoom, int tilex, int tiley) {
		return "http://aeromaps.us/Z" + zoom + "/" + tiley + "/" + tilex + ".png";
	}

}