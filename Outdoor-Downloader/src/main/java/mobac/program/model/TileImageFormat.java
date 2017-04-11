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

import java.io.OutputStream;
import java.util.ArrayList;

import javax.swing.JComboBox;

import mobac.gui.MainGUI;
import mobac.program.interfaces.TileImageDataWriter;
import mobac.program.tiledatawriter.TileImageJpegDataWriter;
import mobac.program.tiledatawriter.TileImagePng4DataWriter;
import mobac.program.tiledatawriter.TileImagePng8DataWriter;
import mobac.program.tiledatawriter.TileImagePngDataWriter;

/**
 * Defines all available image formats selectable in the {@link JComboBox} in the {@link MainGUI}. Each element of this
 * enumeration contains one instance of an {@link TileImageDataWriter} instance that can perform one or more image
 * operations (e.g. color reduction) and then saves the image to an {@link OutputStream}.
 * 
 * @see TileImageDataWriter
 * @see TileImagePngDataWriter
 * @see TileImagePng4DataWriter
 * @see TileImagePng8DataWriter
 * @see TileImageJpegDataWriter
 */
public enum TileImageFormat {

	PNG("PNG", new TileImagePngDataWriter()), //
	PNG8Bit("PNG 256 colors (8 bit)", new TileImagePng8DataWriter()), //
	PNG4Bit("PNG  16 colors (4 bit)", new TileImagePng4DataWriter()), //
	JPEG100("JPEG - quality 100", new TileImageJpegDataWriter(1.00)), //
	JPEG99("JPEG - quality 99", new TileImageJpegDataWriter(0.99)), //
	JPEG95("JPEG - quality 95", new TileImageJpegDataWriter(0.95)), //
	JPEG90("JPEG - quality 90", new TileImageJpegDataWriter(0.90)), //
	JPEG85("JPEG - quality 85", new TileImageJpegDataWriter(0.85)), //
	JPEG80("JPEG - quality 80", new TileImageJpegDataWriter(0.80)), //
	JPEG70("JPEG - quality 70", new TileImageJpegDataWriter(0.70)), //
	JPEG60("JPEG - quality 60", new TileImageJpegDataWriter(0.60)), //
	JPEG50("JPEG - quality 50", new TileImageJpegDataWriter(0.50)); //

	private final String description;

	private final TileImageDataWriter dataWriter;

	private TileImageFormat(String description, TileImageDataWriter dataWriter) {
		this.description = description;
		this.dataWriter = dataWriter;
	}

	@Override
	public String toString() {
		return description;
	}

	public TileImageDataWriter getDataWriter() {
		return dataWriter;
	}

	public TileImageType getType() {
		return dataWriter.getType();
	}

	/**
	 * File extension
	 * 
	 * @return
	 */
	public String getFileExt() {
		return dataWriter.getType().getFileExt();
	}

	public static TileImageFormat[] getPngFormats() {
		return getFormats(TileImageType.PNG);
	}

	public static TileImageFormat[] getJpgFormats() {
		return getFormats(TileImageType.JPG);
	}

	protected static TileImageFormat[] getFormats(TileImageType tileImageType) {
		ArrayList<TileImageFormat> list = new ArrayList<TileImageFormat>();
		for (TileImageFormat format : values()) {
			if (tileImageType.equals(format.getType()))
				list.add(format);
		}
		TileImageFormat[] result = new TileImageFormat[0];
		result = list.toArray(result);
		return result;
	}

}
