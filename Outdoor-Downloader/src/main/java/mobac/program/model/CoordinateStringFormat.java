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
package mobac.program.model;

import java.text.NumberFormat;

import mobac.utilities.Utilities;
import mobac.utilities.geo.CoordinateDm2Format;
import mobac.utilities.geo.CoordinateDms2Format;


public enum CoordinateStringFormat {

	DEG_ENG("Degree (eng)", Utilities.FORMAT_6_DEC_ENG), // 
	DEG_LOCAL("Degree (local)", Utilities.FORMAT_6_DEC), // 
	DEG_MIN_ENG("Deg Min (eng)", new CoordinateDm2Format(Utilities.DFS_ENG)), //
	DEG_MIN_LOCAL("Deg Min (local)", new CoordinateDm2Format(Utilities.DFS_LOCAL)), //
	DEG_MIN_SEC_ENG("Deg Min Sec (eng)", new CoordinateDms2Format(Utilities.DFS_ENG)), //
	DEG_MIN_SEC_LOCAL("Deg Min Sec (local)", new CoordinateDms2Format(Utilities.DFS_LOCAL));

	/*
	 * formatButton.addDropDownItem(new JNumberFormatMenuItem());
	 * formatButton.addDropDownItem(new
	 * JNumberFormatMenuItem("Deg Min Sec,2 (local)",
	 */
	private final String displayName;
	private NumberFormat numberFormat;

	private CoordinateStringFormat(String displayName, NumberFormat numberFormat) {
		this.displayName = displayName;
		this.numberFormat = numberFormat;
	}

	public String getDisplayName() {
		return displayName;
	}

	public NumberFormat getNumberFormat() {
		return numberFormat;
	}

	@Override
	public String toString() {
		return displayName;
	}
	
}
