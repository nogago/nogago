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
package mobac.mapsources.mapspace;

import mobac.program.interfaces.MapSpace;

/**
 * 
 * Provides support for true Ellipsoidal Mercator projections;
 * 
 * Based on:
 * 
 * GeoTools - The Open Source Java GIS Toolkit http://geotools.org
 * 
 * (C) 1999-2008, Open Source Geospatial Foundation (OSGeo)
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation; version 2.1 of the License.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * This package contains formulas from the PROJ package of USGS. USGS's work is fully acknowledged here. This derived
 * work has been relicensed under LGPL with Frank Warmerdam's permission.
 * 
 */
public class MercatorPower2MapSpaceEllipsoidal extends MercatorPower2MapSpace {

	/**
	 * Difference allowed in iterative computations.
	 */
	private static final double ITERATION_TOLERANCE = 1E-10;

	/**
	 * Maximum number of iterations for iterative computations.
	 */
	private static final int MAXIMUM_ITERATIONS = 15;

	/**
	 * The square of excentricity: eÂ² = (aÂ²-bÂ²)/aÂ² where <var>e</var> is the excentricity, <var>a</var> is the semi
	 * major axis length and <var>b</var> is the semi minor axis length.
	 * 
	 * For WGS84 ellipsoid a = 6378137 b = 6356752.3142
	 */
	protected final double excentricitySquared = 0.00669438;

	/**
	 * Ellipsoid excentricity, equals to <code>sqrt({@link
	 * #excentricitySquared})</code>. Value 0 means that the ellipsoid is spherical.
	 */
	protected final double excentricity = Math.sqrt(excentricitySquared);

	public static final MapSpace INSTANCE_256 = new MercatorPower2MapSpaceEllipsoidal(256);

	protected MercatorPower2MapSpaceEllipsoidal(int tileSize) {
		super(tileSize);
	}

	@Override
	public ProjectionCategory getProjectionCategory() {
		return ProjectionCategory.ELLIPSOID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see mobac.mapsources.mapspace.MercatorPower2MapSpace#cLatToY(double, int)
	 */
	@Override
	public int cLatToY(double lat, int zoom) {
		lat = Math.max(MIN_LAT, Math.min(MAX_LAT, lat));
		lat = Math.toRadians(lat);
		lat = -Math.log(tsfn(lat, Math.sin(lat)));
		int mp = getMaxPixels(zoom);
		int y = (-1) * (int) (mp * lat / (2 * Math.PI));
		y = y - falseNorthing(zoom) - (y > 0 ? -1 : 1);
		y = Math.min(y, mp - 1);
		return y;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see mobac.mapsources.mapspace.MercatorPower2MapSpace#cYToLat(int, int)
	 */
	@Override
	public double cYToLat(int y, int zoom) {

		int y2 = y + falseNorthing(zoom);
		double latitude = Math.exp(-y2 / radius(zoom));
		try {
			latitude = cphi2(latitude);
		} catch (Exception e) {
			// No convergence; try spheric aproximation.
			return super.cYToLat(y, zoom);
		}
		return -1 * Math.toDegrees(latitude);
	}

	/**
	 * Iteratively solve equation (7-9) from Snyder.
	 */
	private double cphi2(final double ts) throws Exception {
		final double eccnth = 0.5 * excentricity;
		double phi = (Math.PI / 2) - 2.0 * Math.atan(ts);
		for (int i = 0; i < MAXIMUM_ITERATIONS; i++) {
			final double con = excentricity * Math.sin(phi);
			final double dphi = (Math.PI / 2) - 2.0 * Math.atan(ts * Math.pow((1 - con) / (1 + con), eccnth)) - phi;
			phi += dphi;
			if (Math.abs(dphi) <= ITERATION_TOLERANCE) {
				return phi;
			}
		}
		// No convergence, wrong parameters.
		throw new Exception();
	}

	/**
	 * Computes function (15-9) and (9-13) from Snyder. Equivalent to negative of function (7-7).
	 */
	private double tsfn(final double phi, double sinphi) {
		sinphi *= excentricity;
		/*
		 * NOTE: change sign to get the equivalent of Snyder (7-7).
		 */
		return Math.tan(0.5 * (Math.PI / 2 - phi)) / Math.pow((1 - sinphi) / (1 + sinphi), 0.5 * excentricity);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		long temp;
		temp = Double.doubleToLongBits(excentricity);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(excentricitySquared);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		MercatorPower2MapSpaceEllipsoidal other = (MercatorPower2MapSpaceEllipsoidal) obj;
		if (Double.doubleToLongBits(excentricity) != Double.doubleToLongBits(other.excentricity))
			return false;
		if (Double.doubleToLongBits(excentricitySquared) != Double.doubleToLongBits(other.excentricitySquared))
			return false;
		return true;
	}

}
