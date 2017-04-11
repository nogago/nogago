/*
 * Copyright (c) 2009-2012
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 */
package com.nogago.desktop.util;


/**
 * A map area in map units. There is a constructor available for creating in
 * lat/long form.
 * 
 * Derived from uk.me.parabola.splitter.Area and uk.me.parabola.splitter.Utils
 * 
 * @author Steve Ratcliffe
 * @author Raphael Volz moved some utility functions from Utils to here
 */
public class Area {

	public static final Area EMPTY = new Area();

	private int mapId;
	private String name;
	private final int minLat;
	private final int minLong;
	private final int maxLat;
	private final int maxLong;

	/**
	 * Create an area from the given Garmin coordinates. We ensure that no
	 * dimension is zero.
	 * 
	 * @param minLat
	 *            The western latitude.
	 * @param minLong
	 *            The southern longitude.
	 * @param maxLat
	 *            The eastern lat.
	 * @param maxLong
	 *            The northern long.
	 */
	public Area(int minLat, int minLong, int maxLat, int maxLong) {
		this.minLat = minLat;
		if (maxLat == minLat)
			this.maxLat = minLat + 1;
		else
			this.maxLat = maxLat;

		this.minLong = minLong;
		if (minLong == maxLong)
			this.maxLong = maxLong + 1;
		else
			this.maxLong = maxLong;
	}

	/**
	 * Creates an empty area.
	 */
	private Area() {
		minLat = 0;
		maxLat = 0;
		minLong = 0;
		maxLong = 0;
	}

	public void setMapId(int mapId) {
		this.mapId = mapId;
	}

	public int getMapId() {
		return mapId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getMinLat() {
		return minLat;
	}

	public int getMinLong() {
		return minLong;
	}

	public int getMaxLat() {
		return maxLat;
	}

	public int getMaxLong() {
		return maxLong;
	}

	public int getWidth() {
		return maxLong - minLong;
	}

	public int getHeight() {
		return maxLat - minLat;
	}

	public String toString() {
		return "(" + toDegrees(minLat) + ',' + toDegrees(minLong) + ") to ("
				+ toDegrees(maxLat) + ',' + toDegrees(maxLong) + ')';
	}
	
	/** Only one feature, header and footer missing */
	public String toGeoJSON() {
		/*
		 { "type": "Feature", "id": 0, "properties": 
		 { 
		  "NAME": "", 
          "href": "http://example.com/crs/42"
         }, 
          "geometry": { "type": "Polygon", "coordinates": [ [ 
          [ -118.456090, 34.242706 ], 
          [ -118.456090, 34.242706 ],
          [ -118.456090, 34.242706 ],
          [ -118.456090, 34.242706 ],
          [ -118.456090, 34.242706 ]
          ] ]
          }
          }
		 */
		StringBuffer b = new StringBuffer();
		b.append("{ type\": \"Feature\", \"id\":");
		b.append(mapId);
		b.append(", \"properties\":\n{\"name\": \"");
		b.append(mapId + ".obf");
		b.append("\", \n \"href\": \"https://download.nogago.com/latest/maps/");
		b.append(mapId);
		b.append(".obf\" }, \n");
		b.append("\"geometry\": { \"type\": \"Polygon\", \"coordinates\": [ [ ");
		// Coordinate Start
		b.append("[" + toDegrees(minLong) + "," + toDegrees(minLat) + "],");
		b.append("[" + toDegrees(minLong) + "," + toDegrees(maxLat) + "],");
		b.append("[" + toDegrees(maxLong) + "," + toDegrees(maxLat) + "],");
		b.append("[" + toDegrees(maxLong) + "," + toDegrees(minLat) + "],");
		b.append("[" + toDegrees(minLong) + "," + toDegrees(minLat) + "]");
		// Coordinate End
		b.append("] ] } }");
		return b.toString();
	}
	
	public String toHexString() {
		return "(0x" + Integer.toHexString(minLat) + ",0x"
				+ Integer.toHexString(minLong) + ") to (0x"
				+ Integer.toHexString(maxLat) + ",0x"
				+ Integer.toHexString(maxLong) + ')';
	}

	public boolean contains(int lat, int lon) {
		return lat >= minLat && lat <= maxLat && lon >= minLong
				&& lon <= maxLong;
	}

	public Area add(Area area) {
		return new Area(Math.min(minLat, area.minLat), Math.min(minLong,
				area.minLong), Math.max(maxLat, area.maxLat), Math.max(maxLong,
				area.maxLong));
	}

	public boolean contains(double lat, double lon) {
		int latu = toMapUnit(lat);
		int lonu = toMapUnit(lon);
		return contains(latu, lonu);
	}

	public static double toDegrees(int val) {
		return (double) val / ((1 << 24) / 360.0);
	}

	/**
	 * A map unit is an integer value that is 1/(2^24) degrees of latitude or
	 * longitude.
	 * 
	 * @param l
	 *            The lat or long as decimal degrees.
	 * @return An integer value in map units.
	 */
	public static int toMapUnit(double l) {
		double DELTA = 0.000001; // TODO check if we really mean this
		if (l > 0)
			return (int) ((l + DELTA) * (1 << 24) / 360);
		else
			return (int) ((l - DELTA) * (1 << 24) / 360);
	}

	public static double toRadians(int latitude) {
		return toDegrees(latitude) * Math.PI / 180;
	}

	public String getResourceName(String name, Area ref){
		String filename = formatMapFileName(name);
		String suffix = "";
		if (minLat < ref.minLat) {
			suffix += "s";
		} else if (maxLat > ref.maxLat) {
			suffix += "n";
		}
		if (minLong < ref.minLong) {
			suffix += "w";
		} else if (maxLong > ref.maxLong) {
			suffix += "e";
		}
		if (minLong==ref.minLong && minLat == ref.minLat) {
			suffix = "c";
		}
		return filename + ";" + suffix   +  "." + mapId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + maxLat;
		result = prime * result + maxLong;
		result = prime * result + minLat;
		result = prime * result + minLong;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Area other = (Area) obj;
		if (maxLat != other.maxLat)
			return false;
		if (maxLong != other.maxLong)
			return false;
		if (minLat != other.minLat)
			return false;
		if (minLong != other.minLong)
			return false;
		return true;
	}
	
	public static String formatMapFileName(String name){
		name = name.trim().replace(',', '_');
		name = name.replace('/', '_');;
		name = name.replace('.', '_');
		name = name.replace('\\', '_');
		name = name.replace(' ', '_');
		name = name.replace("__", "_");
		return name;
	}
}