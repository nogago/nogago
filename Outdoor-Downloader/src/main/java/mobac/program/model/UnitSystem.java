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

public enum UnitSystem {

	Metric(6367.5d, 1000, 2.54d, "km", "m", "cm"), Imperial(3963.192d, 5280, 1d, "mi", "ft", "in");

	/**
	 * Points per inch, default value for PDF format.
	 */
	public static final double PPI = 72d;

	public static double pointsToPixels(double points, int dpi) {
		return points / PPI * dpi;
	}

	public static double pixelsToPoints(double pixels, int dpi) {
		return pixels / dpi * PPI;
	}

	public final double earthRadius;
	public final String unitLarge;
	public final String unitSmall;
	public final String unitTiny;
	public final int unitFactor;
	public final double inchFactor;
	public final double maxAngularDistSmall;

	private UnitSystem(double earthRadius, int unitFactor, double inchFactor, String unitLarge, String unitSmall,
			String unitTiny) {
		this.earthRadius = earthRadius;
		this.unitFactor = unitFactor;
		this.inchFactor = inchFactor;
		this.unitLarge = unitLarge;
		this.unitSmall = unitSmall;
		this.unitTiny = unitTiny;
		this.maxAngularDistSmall = 1d / (earthRadius * unitFactor);
	}

	private double unitsToInches(double units) {
		return units / inchFactor;
	}

	private double inchesToUnits(double inches) {
		return inches * inchFactor;
	}

	public double unitsToPoints(double units) {
		return unitsToInches(units) * PPI;
	}

	public double pointsToUnits(double points) {
		return inchesToUnits(points / PPI);
	}

	public double unitsToPixels(double units, int dpi) {
		return unitsToInches(units) * dpi;
	}

	public double pixelsToUnits(double pixels, int dpi) {
		return inchesToUnits(pixels / dpi);
	}

}
