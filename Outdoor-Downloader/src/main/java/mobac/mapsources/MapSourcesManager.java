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
package mobac.mapsources;

import java.util.Vector;

import mobac.program.interfaces.MapSource;

public abstract class MapSourcesManager {

	protected static MapSourcesManager INSTANCE = null;

	public static MapSourcesManager getInstance() {
		return INSTANCE;
	}

	public abstract void addMapSource(MapSource mapSource);

	public abstract Vector<MapSource> getAllMapSources();

	/**
	 * Returns all {@link MapSource} used implementations that represent a map layer (have a visible result).
	 * Meta-map-sources like multi-layer map sources are ignored. The result does contain each {@link MapSource} only
	 * once (no duplicates).
	 * 
	 * @return
	 */
	public abstract Vector<MapSource> getAllLayerMapSources();

	public abstract Vector<MapSource> getEnabledOrderedMapSources();

	public abstract MapSource getDefaultMapSource();

	public abstract MapSource getSourceByName(String name);

	public abstract Vector<MapSource> getDisabledMapSources();

	/**
	 * All means all visible map sources to the user plus all layers of multi-layer map sources
	 * 
	 * @return
	 */
	public abstract Vector<MapSource> getAllAvailableMapSources();

}
