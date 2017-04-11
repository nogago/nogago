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

import mobac.program.interfaces.TileImageDataWriter;
import mobac.program.model.TileImageType;
import mobac.program.tiledatawriter.TileImagePng8DataWriter;
import mobac.utilities.Utilities;

/**
 * A tile provider for atlas formats that only allow PNG images. Each image processed is checked
 */
public class PngTileProvider extends FilterTileProvider {

	final TileImageDataWriter writer;

	public PngTileProvider(TileProvider tileProvider) {
		super(tileProvider);
		writer = new TileImagePng8DataWriter();
		writer.initialize();
	}

	@Override
	public byte[] getTileData(int x, int y) throws IOException {
		if (!tileProvider.preferTileImageUsage()) {
			byte[] data = super.getTileData(x, y);
			if (Utilities.getImageType(data) == TileImageType.PNG)
				return data;
		}
		ByteArrayOutputStream buffer = new ByteArrayOutputStream(32000);
		BufferedImage image = getTileImage(x, y);
		if (image == null)
			return null;
		writer.processImage(image, buffer);
		return buffer.toByteArray();
	}

	public boolean preferTileImageUsage() {
		return true;
	}

}
