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
package mobac.utilities.imageio;

/**
 * Common constants used in the PNG file format. http://www.libpng.org/pub/png/spec/1.2/PNG-Chunks.html#C.IHDR
 */
public class PngConstants {

	/**
	 * PNG magic file bytes
	 */
	public static final byte[] SIGNATURE = { (byte) 137, 80, 78, 71, 13, 10, 26, 10 };

	/**
	 * Chunk header identifiers
	 */
	public static final int IHDR = (int) 0x49484452; // png header
	public static final int TEXT = (int) 0x74455874; // textual information
	public static final int PLTE = (int) 0x504C5445; // color palette
	public static final int IDAT = (int) 0x49444154; // image data
	public static final int IEND = (int) 0x49454E44; // end of file

	/**
	 * Each pixel is a grayscale sample. Allowed bit depths: 1,2,4,8,16
	 */
	public static final byte COLOR_GRAYSCALE = 0;

	/**
	 * Each pixel is an R,G,B triple. Allowed bit depths: 8,16
	 */
	public static final byte COLOR_TRUECOLOR = 2;

	/**
	 * Each pixel is a palette index; a PLTE chunk must appear. Allowed bit depths: 1,2,4,8
	 */
	public static final byte COLOR_PALETTE = 3;

	/**
	 * Each pixel is a grayscale sample, followed by an alpha sample. Allowed bit depths: 8,16
	 */
	public static final byte COLOR_GRAYSCALE_ALPHA = 4;

	/**
	 * Each pixel is an R,G,B triple, followed by an alpha sample. Allowed bit depths: 6 8,16
	 */
	public static final byte COLOR_TRUECOLOR_ALPHA = 6;

	public static final byte COMPRESSION_DEFLATE = 0;

	public static final byte FILTER_SET_1 = 0;

	public static final byte INTERLACE_NONE = 0;

	public static final byte FILTER_TYPE_NONE = 0;
	public static final byte FILTER_TYPE_SUB = 1;
	public static final byte FILTER_TYPE_UP = 2;
	public static final byte FILTER_TYPE_AVERAGE = 0;
	public static final byte FILTER_TYPE_PAETH = 4;

}
