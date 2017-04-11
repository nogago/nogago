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
package unittests.helper;

import java.util.Vector;

import mobac.mapsources.MapSourcesManager;
import mobac.mapsources.impl.LocalhostTestSource;
import mobac.program.interfaces.MapSource;
import mobac.program.model.TileImageType;

public class TestMapSourcesManager extends MapSourcesManager {

	private final MapSource theMapSource;

	public TestMapSourcesManager(int port, TileImageType tileType) {
		super();
		theMapSource = new LocalhostTestSource("Localhost test", port, tileType);
		install();
	}

	public TestMapSourcesManager(MapSource mapSource) {
		super();
		theMapSource = mapSource;
		install();
	}

	public void install() {
		INSTANCE = this;
	}

	@Override
	public Vector<MapSource> getAllMapSources() {
		Vector<MapSource> v = new Vector<MapSource>(1);
		v.add(theMapSource);
		return v;
	}

	@Override
	public Vector<MapSource> getAllLayerMapSources() {
		return getAllMapSources();
	}

	@Override
	public MapSource getDefaultMapSource() {
		return theMapSource;
	}

	@Override
	public Vector<MapSource> getEnabledOrderedMapSources() {
		return getAllMapSources();
	}

	@Override
	public Vector<MapSource> getDisabledMapSources() {
		return new Vector<MapSource>();
	}

	@Override
	public MapSource getSourceByName(String name) {
		return theMapSource;
	}

	@Override
	public void addMapSource(MapSource mapSource) {
		throw new RuntimeException("not implemented");
	}

	@Override
	public Vector<MapSource> getAllAvailableMapSources() {
		return getAllMapSources();
	}
	
	

}
