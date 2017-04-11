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

import mobac.program.interfaces.LayerInterface;
import mobac.program.interfaces.MapInterface;

public class AtlasTestException extends Exception {

	public AtlasTestException(String message, MapInterface map) {
		super(message + "\nError caused by map \"" + map.getName() + "\" on layer \""
				+ map.getLayer().getName() + "\"");
	}

	public AtlasTestException(String message, LayerInterface layer) {
		super(message + "\nError caused by layer \"" + layer.getName() + "\"");
	}

	public AtlasTestException(String message) {
		super(message);
	}

	public AtlasTestException(Throwable cause) {
		super(cause);
	}

	public AtlasTestException(String message, Throwable cause) {
		super(message, cause);
	}

}
