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

/*
 * PNGWriter.java
 *
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

import static mobac.utilities.imageio.PngConstants.*;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import javax.activation.UnsupportedDataTypeException;

/**
 * A PNG writer that is able to write extra large PNG images using incremental
 * writing.
 * <p>
 * The image is processed incremental in "tile lines" - e.g. an PNG image of
 * 30000 x 20000 pixels (width x height) can be written by 200 "tile lines" of
 * size 30000 x 100 pixels. Each tile line can be written via the method
 * {@link #writeTileLine(BufferedImage)}. After writing the last line you have
 * to call {@link #finish()} which will write the final PNG structure
 * information into the {@link OutputStream}.
 * </p>
 * <p>
 * Please note that this writer creates 24bit/truecolor PNGs. Transparency and
 * alpha masks are not supported.
 * </p>
 * Bases on the PNGWriter written by Matthias Mann - www.matthiasmann.de
 * 
 * @author r_x
 */
public class PngXxlWriter {

	private static final int BUFFER_SIZE = 128 * 1024;

	private int width;
	private int height;
	private DataOutputStream dos;

	ImageDataChunkWriter imageDataChunkWriter;

	/**
	 * Creates an PNG writer instance for an image with the specified width and
	 * height.
	 * 
	 * @param width
	 *            width of the PNG image to be written
	 * @param height
	 *            height of the PNG image to be written
	 * @param os
	 *            destination to write the PNG image data to
	 * @throws IOException
	 */
	public PngXxlWriter(int width, int height, OutputStream os) throws IOException {
		this.width = width;
		this.height = height;
		this.dos = new DataOutputStream(os);

		dos.write(SIGNATURE);

		PngChunk cIHDR = new PngChunk(IHDR);
		cIHDR.writeInt(this.width);
		cIHDR.writeInt(this.height);
		cIHDR.writeByte(8); // 8 bit per component
		cIHDR.writeByte(COLOR_TRUECOLOR);
		cIHDR.writeByte(COMPRESSION_DEFLATE);
		cIHDR.writeByte(FILTER_SET_1);
		cIHDR.writeByte(INTERLACE_NONE);
		cIHDR.writeTo(dos);
		imageDataChunkWriter = new ImageDataChunkWriter(dos);
	}

	/**
	 * 
	 * @param tileLineImage
	 * @throws IOException
	 */
	public void writeTileLine(BufferedImage tileLineImage) throws IOException {

		int tileLineHeight = tileLineImage.getHeight();
		int tileLineWidth = tileLineImage.getWidth();

		if (width != tileLineWidth)
			throw new RuntimeException("Invalid width");

		ColorModel cm = tileLineImage.getColorModel();

		if (!(cm instanceof DirectColorModel))
			throw new UnsupportedDataTypeException(
					"Image uses wrong color model. Only DirectColorModel is supported!");

		// We process the image line by line, from head to bottom
		Rectangle rect = new Rectangle(0, 0, tileLineWidth, 1);

		DataOutputStream imageDataStream = imageDataChunkWriter.getStream();

		byte[] curLine = new byte[width * 3];
		for (int line = 0; line < tileLineHeight; line++) {
			rect.y = line;
			DataBuffer db = tileLineImage.getData(rect).getDataBuffer();
			if (db.getNumBanks() > 1)
				throw new UnsupportedDataTypeException("Image data has more than one data bank");
			if (db instanceof DataBufferByte)
				curLine = ((DataBufferByte) db).getData();
			else if (db instanceof DataBufferInt) {
				int[] intLine = ((DataBufferInt) db).getData();
				int c = 0;
				for (int i = 0; i < intLine.length; i++) {
					int pixel = intLine[i];
					curLine[c++] = (byte) (pixel >> 16 & 0xFF);
					curLine[c++] = (byte) (pixel >> 8 & 0xFF);
					curLine[c++] = (byte) (pixel & 0xFF);
				}
			} else
				throw new UnsupportedDataTypeException(db.getClass().getName());

			imageDataStream.write(FILTER_TYPE_NONE);
			imageDataStream.write(curLine);
		}
	}

	public void finish() throws IOException {
		imageDataChunkWriter.finish();
		PngChunk cIEND = new PngChunk(IEND);
		cIEND.writeTo(dos);
		cIEND.close();
		dos.flush();
	}

	static class ImageDataChunkWriter extends OutputStream {

		DeflaterOutputStream dfos;
		DataOutputStream stream;
		DataOutputStream out;
		CRC32 crc = new CRC32();

		public ImageDataChunkWriter(DataOutputStream out) throws IOException {
			this.out = out;
			dfos = new DeflaterOutputStream(new BufferedOutputStream(this, BUFFER_SIZE),
					new Deflater(Deflater.BEST_COMPRESSION));
			stream = new DataOutputStream(dfos);
		}

		public DataOutputStream getStream() {
			return stream;
		}

		public void finish() throws IOException {
			stream.flush();
			stream.close();
			dfos.finish();
			dfos = null;
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			crc.reset();
			out.writeInt(len);
			out.writeInt(IDAT);
			out.write(b, off, len);
			crc.update("IDAT".getBytes());
			crc.update(b, off, len);
			out.writeInt((int) crc.getValue());
		}

		@Override
		public void write(byte[] b) throws IOException {
			write(b, 0, b.length);
		}

		@Override
		public void write(int b) throws IOException {
			throw new IOException("Simgle byte writing not supported");
		}
	}
}
