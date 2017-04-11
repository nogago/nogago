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
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;

import mobac.program.interfaces.TileImageDataWriter;
import mobac.program.model.TileImageType;

import org.apache.log4j.Logger;

public class TileImagePngDataWriter implements TileImageDataWriter {

	protected Logger log;

	protected ImageWriter pngImageWriter = null;

	public TileImagePngDataWriter() {
		log = Logger.getLogger(this.getClass());
	}

	public void initialize() {
		if (log.isTraceEnabled()) {
			String s = "Available PNG image writers:";
			Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("png");
			while (writers.hasNext()) {
				ImageWriter w = writers.next();
				s += "\n\t" + w.getClass().getName();
			}
			log.trace(s);
		}
		pngImageWriter = ImageIO.getImageWritersByFormatName("png").next();
		pngImageWriter.addIIOWriteWarningListener(ImageWriterWarningListener.INSTANCE);
		log.debug("Used PNG image writer: " + pngImageWriter.getClass().getName());
	}

	public void processImage(BufferedImage image, OutputStream out) throws IOException {
		pngImageWriter.setOutput(ImageIO.createImageOutputStream(out));
		IIOImage ioImage = new IIOImage(image, null, null);
		pngImageWriter.write(ioImage);
	}

	public void dispose() {
		pngImageWriter.dispose();
		pngImageWriter = null;
	}

	public TileImageType getType() {
		return TileImageType.PNG;
	}

}
