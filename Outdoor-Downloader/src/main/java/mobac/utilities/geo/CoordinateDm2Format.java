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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;

import org.apache.log4j.Logger;

public class CoordinateDm2Format extends NumberFormat {

	protected static Logger log = Logger.getLogger(CoordinateDms2Format.class);

	NumberFormat degFmt;
	NumberFormat minFmt;
	NumberFormat minFmtParser;

	public CoordinateDm2Format(DecimalFormatSymbols dfs) {
		degFmt = new DecimalFormat("00°", dfs);
		minFmt = new DecimalFormat("00.00''", dfs);
		minFmtParser = new DecimalFormat("##.##", dfs);
	}

	@Override
	public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) {
		int degrees = (int) Math.floor(number);
		number = (number - degrees) * 60;
		double minutes = number;
		toAppendTo.append(degFmt.format(degrees) + " ");
		toAppendTo.append(minFmt.format(minutes) + " ");
		return toAppendTo;
	}

	@Override
	public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos) {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public Number parse(String source, ParsePosition parsePosition) {
		String[] tokens = source.trim().split("[°\\']");
		if (tokens.length != 2)
			return null;
		try {
			int deg = Integer.parseInt(tokens[0].trim());
			double min = minFmtParser.parse(tokens[1].trim()).doubleValue();
			double coord = min / 60.0 + deg;
			return new Double(coord);
		} catch (Exception e) {
			parsePosition.setErrorIndex(0);
			log.error("e");
			return null;
		}
	}

}
