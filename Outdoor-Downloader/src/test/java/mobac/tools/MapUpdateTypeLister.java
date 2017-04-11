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
package mobac.tools;

import java.util.Vector;

import mobac.mapsources.MapSourcesManager;
import mobac.program.Logging;
import mobac.program.interfaces.HttpMapSource;
import mobac.program.interfaces.MapSource;

public class MapUpdateTypeLister {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Logging.configureConsoleLogging();
		Vector<MapSource> mapSources = MapSourcesManager.getInstance().getAllMapSources();
		for (MapSource mapSource : mapSources) {
			if (mapSource instanceof HttpMapSource) {
				HttpMapSource httpMapSource = (HttpMapSource) mapSource;
				String name = mapSource.getName();
				name = name.substring(0, Math.min(25, name.length()));
				System.out.println(String.format("%25s  %s", name, httpMapSource.getTileUpdate()));
			}
		}
	}
}
