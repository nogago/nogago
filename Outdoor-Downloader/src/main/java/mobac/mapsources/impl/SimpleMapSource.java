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

import javax.imageio.ImageIO;

import mobac.exceptions.UnrecoverableDownloadException;
import mobac.mapsources.mapspace.MercatorPower2MapSpace;
import mobac.program.interfaces.MapSource;
import mobac.program.interfaces.MapSpace;
import mobac.program.model.MapSourceLoaderInfo;
import mobac.program.model.TileImageType;

/**
 * A simple {@link MapSource} implementation serving as fall-back if no other map source is available/can be loaded.
 */
public class SimpleMapSource implements MapSource {

	public SimpleMapSource() {
	}

	public Color getBackgroundColor() {
		return Color.WHITE;
	}

	public MapSpace getMapSpace() {
		return MercatorPower2MapSpace.INSTANCE_256;
	}

	public int getMaxZoom() {
		return 2;
	}

	public int getMinZoom() {
		return 0;
	}

	public String getName() {
		return "Simple";
	}

	public byte[] getTileData(int zoom, int x, int y, LoadMethod loadMethod) throws IOException,
			UnrecoverableDownloadException, InterruptedException {
		ByteArrayOutputStream buf = new ByteArrayOutputStream(16000);
		ImageIO.write(getTileImage(zoom, x, y, LoadMethod.DEFAULT), "png", buf);
		return buf.toByteArray();
	}

	public BufferedImage getTileImage(int zoom, int x, int y, LoadMethod loadMethod) throws IOException,
			UnrecoverableDownloadException, InterruptedException {
		BufferedImage image = new BufferedImage(256, 256, BufferedImage.TYPE_3BYTE_BGR);
		Graphics2D g2 = image.createGraphics();
		try {
			g2.setColor(Color.WHITE);
			g2.fillRect(0, 0, 255, 255);
			g2.setColor(Color.BLACK);
			g2.drawString("No map sources available", 8, 40);
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
		return "";
	}

}
