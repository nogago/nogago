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
package mobac.program.atlascreators;

import mobac.mapsources.mapspace.MercatorPower2MapSpace;
import mobac.program.annotations.AtlasCreatorName;
import mobac.program.interfaces.MapSource;

/**
 * Atlas/Map creator for "BigPlanet-Maps application for Android" (offline SQLite maps)
 * http://code.google.com/p/bigplanet/
 * <p>
 * Requires "SQLite Java Wrapper/JDBC Driver" (BSD-style license) http://www.ch-werner.de/javasqlite/
 * </p>
 * <p>
 * Some source parts are taken from the "android-map.blogspot.com Version of Mobile Atlas Creator":
 * http://code.google.com/p/android-map/
 * </p>
 */
@AtlasCreatorName(value = "Big Planet Tracks SQLite", type = "BigPlanet")
public class BigPlanetTracks extends RMapsSQLite {

	@Override
	public boolean testMapSource(MapSource mapSource) {
		return MercatorPower2MapSpace.INSTANCE_256.equals(mapSource.getMapSpace());
	}

}
