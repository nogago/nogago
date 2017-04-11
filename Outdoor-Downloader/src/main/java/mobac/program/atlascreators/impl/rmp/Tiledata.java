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
/* *********************************************
 * Copyright: Andreas Sander
 *
 *
 * ********************************************* */

package mobac.program.atlascreators.impl.rmp;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import mobac.exceptions.MapCreationException;
import mobac.program.interfaces.TileImageDataWriter;

/**
 * Content of a single tile
 * 
 */
public class Tiledata {

	private final TileImageDataWriter writer;

	public int posx;
	public int posy;
	public int totalOffset;
	public MultiImage si;

	public BoundingRect rect;

	private int dataSize = 0;

	public Tiledata(TileImageDataWriter writer) {
		this.writer = writer;
	}

	public int getTileDataSize() {
		return dataSize;
	}

	public void writeTileData(OutputStream out) throws IOException {
		try {
			BufferedImage image = si.getSubImage(rect, 256, 256);
			ByteArrayOutputStream bout = new ByteArrayOutputStream(16384);
			writer.processImage(image, bout);
			byte[] data = bout.toByteArray();
			dataSize = data.length;
			// Utilities.saveBytes(String.format("D:/jpg/mobac-%04d-%04d.jpg", posx, posy), data);
			RmpTools.writeValue(out, dataSize, 4);
			out.write(data);
		} catch (MapCreationException e) {
			throw new IOException(e.getCause());
		}
	}

}
