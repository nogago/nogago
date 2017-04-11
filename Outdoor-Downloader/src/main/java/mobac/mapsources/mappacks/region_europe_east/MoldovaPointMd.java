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
import mobac.program.model.TileImageType;

/**
 * Institute of Geodesy Technical Research and Cadastre "INGEOCAD" under Sate Agency of Land
 * Relations and Cadastre Tiles created by and sourced from point.md
 * 
 * http://point.md/Map/#x=28.870983&y=47.017756&z=10
 * 
 * https://sourceforge.net/tracker/?func=detail&atid=1105497&aid=3321793&group_id=238075
 */
public class MoldovaPointMd extends AbstractHttpMapSource {

	public MoldovaPointMd() {
		super("MoldovaPointMd", 8, 18, TileImageType.PNG, TileUpdate.None);
	}

	public String getTileUrl(int zoom, int x, int y) {
		return "http://point.md/map/Map/GetTile?path=1/" + zoom + "/" + x + "/" + y + ".png";
	}

	@Override
	public String toString() {
		return "Moldova (point.md)";
	}

}
