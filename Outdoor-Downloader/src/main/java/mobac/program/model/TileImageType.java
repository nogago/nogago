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

/**
 * Supported file extensions of all {@link TileImageFormat} enums.
 */
public enum TileImageType {
	PNG("png"), JPG("jpeg"), GIF("gif");

	private final String mime;

	private TileImageType(String mime) {
		this.mime = mime;
	}

	public String getFileExt() {
		return name().toLowerCase();
	}

	public String getMimeType() {
		return mime;
	}

	public static TileImageType getTileImageType(String type) {
		try {
			return TileImageType.valueOf(type.toUpperCase());
		} catch (IllegalArgumentException e) {
			for (TileImageType t : TileImageType.values()) {
				if (t.getFileExt().equalsIgnoreCase(type))
					return t;
			}
			throw e;
		}
	}
}
