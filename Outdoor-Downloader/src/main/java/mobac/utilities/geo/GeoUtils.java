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
package mobac.utilities.geo;

import java.util.Locale;

public class GeoUtils {

	public static String getDegMinFormat(double coord, boolean isLatitude) {
	
		boolean neg = (coord < 0.0);
		coord = Math.abs(coord);
		int deg = (int) coord;
		double min = (coord - deg) * 60.0;
	
		String degMinFormat = "%d, %3.6f, %c";
	
		char dirC;
		if (isLatitude)
			dirC = (neg ? 'S' : 'N');
		else
			dirC = (neg ? 'W' : 'E');
	
		return String.format(Locale.ENGLISH, degMinFormat, deg, min, dirC);
	}

}
