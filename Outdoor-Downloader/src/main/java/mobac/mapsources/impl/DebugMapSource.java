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
package mobac.mapsources.impl;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;

import mobac.exceptions.UnrecoverableDownloadException;
import mobac.gui.mapview.PreviewMap;
import mobac.mapsources.mapspace.MercatorPower2MapSpace;
import mobac.program.interfaces.MapSource;
import mobac.program.interfaces.MapSpace;
import mobac.program.model.MapSourceLoaderInfo;
import mobac.program.model.TileImageType;
import mobac.utilities.imageio.Png4BitWriter;

public class DebugMapSource implements MapSource {

	private int pngCompressionLevel = Deflater.BEST_SPEED;

	private static final byte[] COLORS = { 0,//
			(byte) 0xff, (byte) 0xff, (byte) 0xff, // white
			(byte) 0xcc, (byte) 0xcc, (byte) 0xcc // light gray
	};

	private static final IndexColorModel COLORMODEL = new IndexColorModel(8, 2, COLORS, 1, false);

	private Color COLOR_BG = new Color(COLORS[1] & 0xFF, COLORS[2] & 0xFF, COLORS[3] & 0xFF);
	private Color COLOR_VG = new Color(COLORS[4] & 0xFF, COLORS[5] & 0xFF, COLORS[6] & 0xFF);

	private static final Font FONT_LARGE = new Font("Sans Serif", Font.BOLD, 30);

	public DebugMapSource() {
	}

	public Color getBackgroundColor() {
		return Color.BLACK;
	}

	public MapSpace getMapSpace() {
		return MercatorPower2MapSpace.INSTANCE_256;
	}

	public int getMaxZoom() {
		return PreviewMap.MAX_ZOOM;
	}

	public int getMinZoom() {
		return 0;
	}

	public String getName() {
		return "Debug";
	}

	public byte[] getTileData(int zoom, int x, int y, LoadMethod loadMethod) throws IOException,
			UnrecoverableDownloadException, InterruptedException {
		ByteArrayOutputStream buf = new ByteArrayOutputStream(16000);
		// ImageIO.write(getTileImage(zoom, x, y, LoadMethod.DEFAULT), "png", buf);
		String pngMetaText = String.format("zoom=%d x=%d y=%d", zoom, x, y);
		Png4BitWriter.writeImage(buf, getTileImage(zoom, x, y, loadMethod), pngCompressionLevel, pngMetaText);
		return buf.toByteArray();
	}

	public BufferedImage getTileImage(int zoom, int x, int y, LoadMethod loadMethod) throws IOException,
			UnrecoverableDownloadException, InterruptedException {
		BufferedImage image = new BufferedImage(256, 256, BufferedImage.TYPE_BYTE_INDEXED, COLORMODEL);
		Graphics2D g2 = image.createGraphics();
		try {
			g2.setColor(COLOR_BG);
			g2.fillRect(0, 0, 255, 255);
			g2.setColor(COLOR_VG);
			g2.drawRect(0, 0, 255, 255);
			g2.drawRect(1, 1, 254, 254);
			g2.drawLine(0, 0, 255, 255);
			g2.drawLine(255, 0, 0, 255);
			g2.setFont(FONT_LARGE);
			g2.drawString("x: " + x, 8, 40);
			g2.drawString("y: " + y, 8, 75);
			g2.drawString("z: " + zoom, 8, 110);
			return image;
		} finally {
			g2.dispose();
		}
	}

	public TileImageType getTileImageType() {
		return TileImageType.PNG;
	}

	public MapSourceLoaderInfo getLoaderInfo() {
		return null;
	}

	public void setLoaderInfo(MapSourceLoaderInfo loaderInfo) {
		throw new RuntimeException("LoaderInfo can not be set");
	}

	@Override
	public String toString() {
		return getName();
	}

}
