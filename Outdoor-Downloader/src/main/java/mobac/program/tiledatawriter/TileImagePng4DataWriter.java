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
package mobac.program.tiledatawriter;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

import mobac.optional.JavaAdvancedImaging;
import mobac.program.interfaces.TileImageDataWriter;
import mobac.program.model.TileImageType;
import mobac.utilities.imageio.Png4BitWriter;

public class TileImagePng4DataWriter implements TileImageDataWriter {

	public TileImagePng4DataWriter() {
	}

	public void initialize() {
	}

	public void processImage(BufferedImage image, OutputStream out) throws IOException {
		BufferedImage image2 = JavaAdvancedImaging.colorReduceMedianCut(image, 16);
		Png4BitWriter.writeImage(out, image2);
	}

	public void dispose() {
	}

	public TileImageType getType() {
		return TileImageType.PNG;
	}
}
