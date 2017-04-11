/* 
 * Copyright (c) 2007 Matthias Mann - www.matthiasmann.de
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */
package mobac.utilities.imageio;

import static mobac.utilities.imageio.PngConstants.COLOR_PALETTE;
import static mobac.utilities.imageio.PngConstants.COMPRESSION_DEFLATE;
import static mobac.utilities.imageio.PngConstants.FILTER_SET_1;
import static mobac.utilities.imageio.PngConstants.FILTER_TYPE_NONE;
import static mobac.utilities.imageio.PngConstants.IDAT;
import static mobac.utilities.imageio.PngConstants.IEND;
import static mobac.utilities.imageio.PngConstants.IHDR;
import static mobac.utilities.imageio.PngConstants.INTERLACE_NONE;
import static mobac.utilities.imageio.PngConstants.PLTE;
import static mobac.utilities.imageio.PngConstants.SIGNATURE;
import static mobac.utilities.imageio.PngConstants.TEXT;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import mobac.utilities.MyMath;

/**
 * 4 Bit PNG Writer
 * <p>
 * Writes a color png image with pallette containing 16 colors. Currently the image data is saved without any PNG
 * filtering.
 * </p>
 * 
 * Bases on the PNGWriter written by Matthias Mann - www.matthiasmann.de
 * 
 * @author r_x
 */
public class Png4BitWriter {

	public static void writeImage(File file, BufferedImage image) throws IOException {
		FileOutputStream out = new FileOutputStream(file);
		try {
			writeImage(out, image);
		} finally {
			out.close();
		}
	}

	/**
	 * 
	 * @param out
	 * @param image
	 *            Must be an image with {@link ColorModel} {@link IndexColorModel}
	 * @throws IOException
	 */
	public static void writeImage(OutputStream out, BufferedImage image) throws IOException {
		writeImage(out, image, Deflater.BEST_COMPRESSION);
	}

	/**
	 * 
	 * @param out
	 * @param image
	 *            Must be an image with {@link ColorModel} {@link IndexColorModel}
	 * @param compression
	 *            deflater method used for compression - possible values are for example
	 *            {@link Deflater#BEST_COMPRESSION}, {@link Deflater#BEST_SPEED},{@link Deflater#NO_COMPRESSION}
	 * @throws IOException
	 */
	public static void writeImage(OutputStream out, BufferedImage image, int compression) throws IOException {
		writeImage(out, image, compression, null);
	}

	/**
	 * 
	 * @param out
	 *            Must be an image with {@link ColorModel} {@link IndexColorModel}
	 * @param image
	 *            deflater method used for compression - possible values are for example
	 *            {@link Deflater#BEST_COMPRESSION}, {@link Deflater#BEST_SPEED},{@link Deflater#NO_COMPRESSION}
	 * @param description
	 *            PNG comment text (meta info)
	 * @throws IOException
	 */
	public static void writeImage(OutputStream out, BufferedImage image, int compression, String description)
			throws IOException {
		DataOutputStream dos = new DataOutputStream(out);

		int width = image.getWidth();
		int height = image.getHeight();

		ColorModel cm = image.getColorModel();

		if (!(cm instanceof IndexColorModel))
			throw new UnsupportedOperationException("Image format not compatible");

		IndexColorModel palette = (IndexColorModel) cm;

		dos.write(SIGNATURE);
		PngChunk cIHDR = new PngChunk(IHDR);
		cIHDR.writeInt(width);
		cIHDR.writeInt(height);
		cIHDR.writeByte(4); // 4 bit per component
		cIHDR.writeByte(COLOR_PALETTE);
		cIHDR.writeByte(COMPRESSION_DEFLATE);
		cIHDR.writeByte(FILTER_SET_1);
		cIHDR.writeByte(INTERLACE_NONE);
		cIHDR.writeTo(dos);

		if (description != null) {
			PngChunk cTxT = new PngChunk(TEXT);
			cTxT.write("Description".getBytes());
			cTxT.write(0);
			cTxT.write(description.getBytes());
			cTxT.writeTo(dos);
		}

		PngChunk cPLTE = new PngChunk(PLTE);
		int paletteEntries = palette.getMapSize();
		byte[] r = new byte[paletteEntries];
		byte[] g = new byte[paletteEntries];
		byte[] b = new byte[paletteEntries];
		palette.getReds(r);
		palette.getGreens(g);
		palette.getBlues(b);
		int colorCount = Math.min(paletteEntries, 16);
		for (int i = 0; i < colorCount; i++) {
			cPLTE.writeByte(r[i]);
			cPLTE.writeByte(g[i]);
			cPLTE.writeByte(b[i]);
		}
		cPLTE.writeTo(dos);

		PngChunk cIDAT = new PngChunk(IDAT);
		DeflaterOutputStream dfos = new DeflaterOutputStream(cIDAT, new Deflater(compression));

		int lineLen = MyMath.divCeil(width, 2);
		byte[] lineOut = new byte[lineLen];
		int[] samples = null;

		for (int line = 0; line < height; line++) {
			dfos.write(FILTER_TYPE_NONE);

			// Get the samples for the next line - each byte is one sample/pixel
			samples = image.getRaster().getPixels(0, line, width, 1, samples);
			int sx = 0;
			int iMax = samples.length - 2;
			for (int i = 0; i < samples.length; i += 2) {
				// Now we are packing two samples of 4 bit into one byte
				int sample1 = samples[i];
				int sample2 = (i <= iMax) ? samples[i + 1] : 0;
				int s1 = sample1 & 0x0F;
				int s2 = sample2 & 0x0F;
				if ((s1 != sample1) || (s2 != sample2))
					throw new RuntimeException("sample has more than 4 bit!");
				lineOut[sx++] = (byte) ((s1 << 4) | s2);
			}
			dfos.write(lineOut);
		}

		dfos.finish();
		cIDAT.writeTo(dos);

		PngChunk cIEND = new PngChunk(IEND);
		cIEND.writeTo(dos);
		cIEND.close();

		dos.flush();
	}

	protected static void writeColor(DataOutputStream dos, Color c) throws IOException {
		dos.writeByte(c.getRed());
		dos.writeByte(c.getGreen());
		dos.writeByte(c.getBlue());
	}

}
