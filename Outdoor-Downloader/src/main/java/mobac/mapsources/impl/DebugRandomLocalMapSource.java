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
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;

import javax.imageio.ImageIO;

import mobac.exceptions.UnrecoverableDownloadException;
import mobac.gui.mapview.PreviewMap;
import mobac.mapsources.mapspace.MercatorPower2MapSpace;
import mobac.program.interfaces.FileBasedMapSource;
import mobac.program.interfaces.MapSource;
import mobac.program.interfaces.MapSpace;
import mobac.program.model.MapSourceLoaderInfo;
import mobac.program.model.TileImageType;

/**
 * A {@link FileBasedMapSource} for debugging and testing purposes
 */
public class DebugRandomLocalMapSource implements MapSource, FileBasedMapSource {

	BufferedImage image = null;
	byte[] imageData = null;

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

	@Override
	public TileImageType getTileImageType() {
		return TileImageType.PNG;
	}

	public void initialize() {
	}

	public void reinitialize() {
	}

	@Override
	public String getName() {
		return "DebugRandomLocal";
	}

	@Override
	public String toString() {
		return "Debug Random (local)";
	}

	public byte[] getTileData(int zoom, int x, int y, LoadMethod loadMethod) throws IOException,
			UnrecoverableDownloadException, InterruptedException {
		if (imageData != null)
			return imageData;
		synchronized (this) {
			if (imageData != null)
				return imageData;
			ByteArrayOutputStream buf = new ByteArrayOutputStream(16000);
			BufferedImage image = getTileImage(zoom, x, y, loadMethod);
			if (image == null)
				return null;
			ImageIO.write(image, "png", buf);
			imageData = buf.toByteArray();
			return imageData;
		}
	}

	public BufferedImage getTileImage(int zoom, int x, int y, LoadMethod loadMethod) throws IOException,
			UnrecoverableDownloadException, InterruptedException {
		if (image != null)
			return image;
		synchronized (this) {
			if (image != null)
				return image;
			BufferedImage image = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2 = image.createGraphics();
			SecureRandom rnd = new SecureRandom();
			try {
				g2.setColor(Color.WHITE);
				g2.fillRect(0, 0, 255, 255);
				for (int i = 0; i < 100; i++) {
					g2.setColor(new Color(rnd.nextInt()));
					int x1 = rnd.nextInt(256);
					int y1 = rnd.nextInt(256);
					int x2 = rnd.nextInt(256);
					int y2 = rnd.nextInt(256);
					g2.drawLine(x1, y1, x2, y2);
				}
				g2.setColor(Color.RED);
				this.image = image;
				return image;
			} finally {
				g2.dispose();
			}
		}
	}

	@Override
	public MapSourceLoaderInfo getLoaderInfo() {
		return null;
	}

	@Override
	public void setLoaderInfo(MapSourceLoaderInfo loaderInfo) {
	}
}
