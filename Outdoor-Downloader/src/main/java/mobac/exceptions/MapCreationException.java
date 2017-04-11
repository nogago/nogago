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
package mobac.exceptions;

import java.io.StringWriter;

import mobac.program.interfaces.ExceptionExtendedInfo;
import mobac.program.interfaces.MapInterface;
import mobac.program.interfaces.MapSource;
import mobac.program.model.MapSourceLoaderInfo;

public class MapCreationException extends Exception implements ExceptionExtendedInfo {

	private static final long serialVersionUID = 1L;
	private MapInterface map;

	public MapCreationException(String message, MapInterface map, Throwable cause) {
		super(message, cause);
		this.map = map;
	}

	public MapCreationException(String message, MapInterface map) {
		super(message);
		this.map = map;
	}

	public MapCreationException(MapInterface map, Throwable cause) {
		super(cause);
		this.map = map;
	}

	public String getExtendedInfo() {
		StringWriter sw = new StringWriter();
		if (map != null) {
			sw.append(map.getInfoText());
			MapSource mapSource = map.getMapSource();
			if (mapSource != null) {
				MapSourceLoaderInfo loaderInfo = map.getMapSource().getLoaderInfo();
				if (loaderInfo != null) {
					sw.append("\nMap type: " + loaderInfo.getLoaderType());
					if (loaderInfo.getSourceFile() != null)
						sw.append("\nMap implementation: " + loaderInfo.getSourceFile().getName());
					sw.append("\nMap revision: " + loaderInfo.getRevision());
				}
			}
		}
		return sw.toString();
	}

	public MapInterface getMap() {
		return map;
	}

}
