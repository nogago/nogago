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
package mobac.program.atlascreators.tileprovider;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import mobac.program.interfaces.TileImageDataWriter;
import mobac.program.model.TileImageFormat;

/**
 * Loads a tile from the underlying {@link TileProvider}, loads the tile to memory, converts it to the desired
 * {@link TileImageFormat} and returns the binary representation of the image in the specified format.
 */
public class ConvertedRawTileProvider extends FilterTileProvider {

	private TileImageDataWriter writer;

	public ConvertedRawTileProvider(TileProvider tileProvider, TileImageFormat tileImageFormat) {
		super(tileProvider);
		writer = tileImageFormat.getDataWriter();
		writer.initialize();
		ImageIO.setUseCache(false);
	}

	public byte[] getTileData(int x, int y) throws IOException {
		BufferedImage image = getTileImage(x, y);
		if (image == null)
			return null;
		ByteArrayOutputStream buffer = new ByteArrayOutputStream(32000);
		writer.processImage(image, buffer);
		return buffer.toByteArray();
	}

	public boolean preferTileImageUsage() {
		return true;
	}

}
