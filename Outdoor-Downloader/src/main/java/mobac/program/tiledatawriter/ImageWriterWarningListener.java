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

import javax.imageio.ImageWriter;
import javax.imageio.event.IIOWriteWarningListener;

import org.apache.log4j.Logger;

/**
 * Allows to capture non-fatal warnings that may occur upon writing an image. At the moment all warnings are simply
 * logged.
 */
public class ImageWriterWarningListener implements IIOWriteWarningListener {

	private static final Logger log = Logger.getLogger(ImageWriterWarningListener.class);

	public static final IIOWriteWarningListener INSTANCE = new ImageWriterWarningListener();
	
	public void warningOccurred(ImageWriter source, int imageIndex, String warning) {
		if (log.isDebugEnabled())
			log.warn(warning + " - caused by: " + source + " on imageIndex " + imageIndex);
		else
			log.warn(warning);
	}

}
