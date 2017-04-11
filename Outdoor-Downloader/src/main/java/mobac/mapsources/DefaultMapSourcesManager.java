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

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.JOptionPane;

import mobac.mapsources.custom.StandardMapSourceLayer;
import mobac.mapsources.impl.DebugLocalMapSource;
import mobac.mapsources.impl.DebugMapSource;
import mobac.mapsources.impl.DebugRandomLocalMapSource;
import mobac.mapsources.impl.SimpleMapSource;
import mobac.mapsources.loader.BeanShellMapSourceLoader;
import mobac.mapsources.loader.CustomMapSourceLoader;
import mobac.mapsources.loader.EclipseMapPackLoader;
import mobac.mapsources.loader.MapPackManager;
import mobac.program.interfaces.MapSource;
import mobac.program.model.Settings;

import org.apache.log4j.Logger;

public class DefaultMapSourcesManager extends MapSourcesManager {

	private Logger log = Logger.getLogger(DefaultMapSourcesManager.class);

	/**
	 * All map sources visible to the user independent of it is enabled or disabled
	 */
	private LinkedHashMap<String, MapSource> allMapSources = new LinkedHashMap<String, MapSource>(50);

	/**
	 * All means all visible map sources to the user plus all layers of multi-layer map sources
	 */
	private HashMap<String, MapSource> allAvailableMapSources = new HashMap<String, MapSource>(50);

	public DefaultMapSourcesManager() {
		// Check for user specific configuration of mapsources directory
	}

	protected void loadMapSources() {
		try {
			boolean devMode = Settings.getInstance().devMode;
			if (devMode) {
				addMapSource(new DebugMapSource());
				addMapSource(new DebugLocalMapSource());
				addMapSource(new DebugRandomLocalMapSource());
			}
			File mapSourcesDir = Settings.getInstance().getMapSourcesDirectory();
			if (mapSourcesDir == null)
				throw new RuntimeException("Map sources directory is unset");
			if (!mapSourcesDir.isDirectory()) {
				JOptionPane.showMessageDialog(null,
						"Map sources directory does not exist - path:\n" + mapSourcesDir.getAbsolutePath()
								+ "\nPlease make sure you extracted the release zip file\n"
								+ "of MOBAC correctly including all subdirectories!", "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			try {
				MapPackManager mpm = new MapPackManager(mapSourcesDir);
				mpm.installUpdates();
				if (!devMode || !loadMapPacksEclipseMode()) {
					mpm.loadMapPacks(this);
				}
			} catch (Exception e) {
				throw new RuntimeException("Failed to load map packs: " + e.getMessage(), e);
			}
			BeanShellMapSourceLoader bsmsl = new BeanShellMapSourceLoader(this, mapSourcesDir);
			bsmsl.loadBeanShellMapSources();

			CustomMapSourceLoader cmsl = new CustomMapSourceLoader(this, mapSourcesDir);
			cmsl.loadCustomMapSources();

		} finally {
			// If no map sources are available load the simple map source which shows the informative message
			if (allMapSources.size() == 0)
				addMapSource(new SimpleMapSource());
		}
	}

	private boolean loadMapPacksEclipseMode() {
		EclipseMapPackLoader empl;
		try {
			empl = new EclipseMapPackLoader(this);
			if (!empl.loadMapPacks())
				return false;
			return true;
		} catch (IOException e) {
			log.error("Failed to load map packs directly from classpath");
		}
		return false;
	}

	public void addMapSource(MapSource mapSource) {
		if (mapSource instanceof StandardMapSourceLayer)
			mapSource = ((StandardMapSourceLayer) mapSource).getMapSource();
		allAvailableMapSources.put(mapSource.getName(), mapSource);
		if (mapSource instanceof AbstractMultiLayerMapSource) {
			for (MapSource lms : ((AbstractMultiLayerMapSource) mapSource)) {
				if (lms instanceof StandardMapSourceLayer)
					lms = ((StandardMapSourceLayer) lms).getMapSource();
				MapSource old = allAvailableMapSources.put(lms.getName(), lms);
				if (old != null) {
					allAvailableMapSources.put(old.getName(), old);
					if (mapSource.equals(old))
						JOptionPane.showMessageDialog(null,
								"Error: Duplicate map source name found: " + mapSource.getName(), "Duplicate name",
								JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		allMapSources.put(mapSource.getName(), mapSource);
	}

	public static void initialize() {
		INSTANCE = new DefaultMapSourcesManager();
		((DefaultMapSourcesManager) INSTANCE).loadMapSources();
	}

	public static void initializeEclipseMapPacksOnly() {
		INSTANCE = new DefaultMapSourcesManager();
		((DefaultMapSourcesManager) INSTANCE).loadMapPacksEclipseMode();
	}

	@Override
	public Vector<MapSource> getAllAvailableMapSources() {
		return new Vector<MapSource>(allMapSources.values());
	}

	@Override
	public Vector<MapSource> getAllMapSources() {
		return new Vector<MapSource>(allMapSources.values());
	}

	@Override
	public Vector<MapSource> getAllLayerMapSources() {
		Vector<MapSource> all = getAllMapSources();
		TreeSet<MapSource> uniqueSources = new TreeSet<MapSource>(new Comparator<MapSource>() {

			public int compare(MapSource o1, MapSource o2) {
				return o1.getName().compareTo(o2.getName());
			}

		});
		for (MapSource ms : all) {
			if (ms instanceof AbstractMultiLayerMapSource) {
				for (MapSource lms : ((AbstractMultiLayerMapSource) ms)) {
					uniqueSources.add(lms);
				}
			} else
				uniqueSources.add(ms);
		}
		Vector<MapSource> result = new Vector<MapSource>(uniqueSources);
		return result;
	}

	@Override
	public Vector<MapSource> getEnabledOrderedMapSources() {
		Vector<MapSource> mapSources = new Vector<MapSource>(allMapSources.size());

		Vector<String> enabledMapSources = Settings.getInstance().mapSourcesEnabled;
		TreeSet<String> notEnabledMapSources = new TreeSet<String>(allMapSources.keySet());
		notEnabledMapSources.removeAll(enabledMapSources);
		for (String mapSourceName : enabledMapSources) {
			MapSource ms = getSourceByName(mapSourceName);
			if (ms != null) {
				mapSources.add(ms);
			}
		}
		// remove all disabled map sources so we get those that are neither enabled nor disabled
		notEnabledMapSources.removeAll(Settings.getInstance().mapSourcesDisabled);
		for (String mapSourceName : notEnabledMapSources) {
			MapSource ms = getSourceByName(mapSourceName);
			if (ms != null) {
				mapSources.add(ms);
			}
		}
		if (mapSources.size() == 0)
			mapSources.add(new SimpleMapSource());
		return mapSources;

	}

	@Override
	public Vector<MapSource> getDisabledMapSources() {
		Vector<String> disabledMapSources = Settings.getInstance().mapSourcesDisabled;
		Vector<MapSource> mapSources = new Vector<MapSource>(disabledMapSources.size());
		for (String mapSourceName : disabledMapSources) {
			MapSource ms = getSourceByName(mapSourceName);
			if (ms != null) {
				mapSources.add(ms);
			}
		}
		return mapSources;
	}

	@Override
	public MapSource getDefaultMapSource() {
		MapSource ms = getSourceByName("MapQuest");// DEFAULT;
		if (ms != null)
			return ms;
		// Fallback: return first
		return allMapSources.values().iterator().next();
	}

	@Override
	public MapSource getSourceByName(String name) {
		return allAvailableMapSources.get(name);
	}

}
