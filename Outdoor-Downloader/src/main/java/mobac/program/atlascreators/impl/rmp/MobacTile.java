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
package mobac.program.atlascreators.impl.rmp;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;

import mobac.program.atlascreators.tileprovider.TileProvider;
import mobac.program.interfaces.MapSpace;

import org.apache.log4j.Logger;


public class MobacTile {
	private static final Logger log = Logger.getLogger(MobacTile.class);

	private final TileProvider tileProvider;
	private final int tilex;
	private final int tiley;
	private BufferedImage image;

	private BoundingRect boundingRect;

	public MobacTile(TileProvider tileProvider, MapSpace mapSpace, int tilex, int tiley, int zoom) {
		this.tileProvider = tileProvider;
		this.tilex = tilex;
		this.tiley = tiley;
		image = null;

		int tileSize = mapSpace.getTileSize();
		int x = tilex * tileSize;
		int y = tiley * tileSize;
		double north = mapSpace.cYToLat(y, zoom);
		double south = mapSpace.cYToLat(y + tileSize - 1, zoom);
		double west = mapSpace.cXToLon(x, zoom);
		double east = mapSpace.cXToLon(x + tileSize - 1, zoom);

		// north and south have to be negated - this really strange!
		boundingRect = new BoundingRect(-north, -south, west, east);
	}

	/**
	 * Returns the image of the tile. Creates on if necessary
	 */
	public BufferedImage getImage() {

		/* --- Load image if none is present --- */
		if (image == null)
			image = loadImage();

		return image;
	}

	private BufferedImage loadImage() {
		try {
			image = tileProvider.getTileImage(tilex, tiley);
		} catch (IOException e) {
			log.error("", e);
			image = createBlack(256, 256);
		}
		return image;
	}

	/**
	 * create a black Tile
	 * 
	 * @return image of black square
	 */
	private BufferedImage createBlack(int width, int height) {
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics graph = img.getGraphics();
		graph.setColor(Color.BLACK);
		graph.fillRect(0, 0, width, height);
		return img;
	}

	public void drawSubImage(BoundingRect dest_area, BufferedImage dest_image) {
		BufferedImage src_image;
		WritableRaster src_graph, dst_graph;
		BoundingRect src_area;
		int maxx, maxy;
		double src_c_x, src_c_y;
		int pix_x, pix_y;
		BufferedImage imageBuffer;
		Graphics graphics;
		int[] pixel = new int[3];

		/* --- Get the coordination rectangle of the source image --- */
		src_area = boundingRect;

		/* --- Get Graphics context --- */
		src_image = getImage();

		if (src_image == null)
			return;

		/* --- Convert it to RGB color space --- */
		imageBuffer = new BufferedImage(src_image.getWidth(), src_image.getHeight(),
				BufferedImage.TYPE_INT_RGB);
		graphics = imageBuffer.createGraphics();
		try {
			graphics.drawImage(src_image, 0, 0, null);
		} finally {
			graphics.dispose();
		}
		src_graph = imageBuffer.getRaster();
		dst_graph = dest_image.getRaster();

		/*
		 * --- Iterate over all pixels of the destination image. Unfortunately
		 * we need this technique because source and dest do not have exactly
		 * the same zoom level, so the source image has to be compressed or
		 * expanded to match the destination image ---
		 */
		maxx = dest_image.getWidth();
		maxy = dest_image.getHeight();
		for (int y = 0; y < maxy; y++) {

			/* --- Calculate the y-coordinate of the current line --- */
			src_c_y = dest_area.getNorth() + (dest_area.getSouth() - dest_area.getNorth()) * y
					/ maxy;

			/* --- Calculate the pixel line of the source image --- */
			pix_y = (int) ((src_c_y - src_area.getNorth()) * 256
					/ (src_area.getSouth() - src_area.getNorth()) + 0.5);

			/* --- Ignore line that are out of the source area --- */
			if (pix_y < 0 || pix_y > 255)
				continue;

			// log.trace("scale factor y: " + (pix_y / (double) y));

			for (int x = 0; x < maxx; x++) {
				/* --- Calculate the x-coordinate of the current row --- */
				src_c_x = dest_area.getWest() + (dest_area.getEast() - dest_area.getWest()) * x
						/ maxx;

				/* --- Calculate the pixel row of the source image --- */
				pix_x = (int) ((src_c_x - src_area.getWest()) * 256
						/ (src_area.getEast() - src_area.getWest()) + 0.5);

				/* --- Ignore the row if it is outside the source area --- */
				if (pix_x < 0 || pix_x > 255)
					continue;

				/* --- Transfer the pixel --- */
				src_graph.getPixel(pix_x, pix_y, pixel);
				dst_graph.setPixel(x, y, pixel);
			}
		}
	}

	public int getImageHeight() {
		/* --- A tile is always 256 pixels high --- */
		return 256;
	}

	public int getImageWidth() {
		/* --- A tile is always 256 pixels wide --- */
		return 256;
	}

	public BufferedImage getSubImage(BoundingRect area, int width, int height) {
		BufferedImage result = createBlack(width, height);
		drawSubImage(area, result);
		return result;
	}

	@Override
	public String toString() {
		return String.format("MobacTile x/y [%d/%d] = %s", tilex, tiley, boundingRect);
	}

}
