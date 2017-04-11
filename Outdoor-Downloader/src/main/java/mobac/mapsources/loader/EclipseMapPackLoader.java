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
package mobac.mapsources.loader;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import mobac.mapsources.MapSourcesManager;
import mobac.program.interfaces.MapSource;
import mobac.utilities.Charsets;
import mobac.utilities.Utilities;
import mobac.utilities.file.DirectoryFileFilter;

import org.apache.log4j.Logger;

/**
 * For map sources debugging inside eclipse. Allows to load the map sources directly from program class path instead of
 * the map packs.
 * 
 */
public class EclipseMapPackLoader {

	private final Logger log = Logger.getLogger(EclipseMapPackLoader.class);

	private final MapSourcesManager mapSourcesManager;

	public EclipseMapPackLoader(MapSourcesManager mapSourcesManager) throws IOException {
		this.mapSourcesManager = mapSourcesManager;
	}

	public boolean loadMapPacks() throws IOException {
		ClassLoader cl = EclipseMapPackLoader.class.getClassLoader();
		boolean success = false;
		File binDir;
		try {
			binDir = new File(cl.getResource(".").toURI());
		} catch (URISyntaxException e) {
			throw new IOException(e);
		}
		File mapPackDir = new File(binDir, "mobac/mapsources/mappacks");
		if (!mapPackDir.isDirectory())
			return false;
		File[] mapPacks = mapPackDir.listFiles(new DirectoryFileFilter());
		for (File d : mapPacks) {
			File list = new File(d, "mapsources.list");
			if (!list.isFile())
				continue;
			String listContent = new String(Utilities.getFileBytes(list), Charsets.UTF_8);
			String[] classNames = listContent.split("\\s+");
			for (String className : classNames) {
				try {
					Class<?> clazz = Class.forName(className);
					MapSource ms = (MapSource) clazz.newInstance();
					mapSourcesManager.addMapSource(ms);
					success = true;
				} catch (Exception e) {
					log.error("className: \"" + className + "\"", e);
				}
			}
		}
		return success;
	}

}
