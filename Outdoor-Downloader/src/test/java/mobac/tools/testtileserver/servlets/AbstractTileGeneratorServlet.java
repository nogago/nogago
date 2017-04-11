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
package mobac.tools.testtileserver.servlets;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;

import javax.servlet.http.HttpServletRequest;

public abstract class AbstractTileGeneratorServlet extends AbstractTileServlet {

	private static final byte[] COLORS = { 0,// 
			(byte) 0xff, (byte) 0xff, (byte) 0xff, // white
			(byte) 0xff, (byte) 0x00, (byte) 0x00 // red
	};

	private static final IndexColorModel COLORMODEL = new IndexColorModel(8, 2, COLORS, 1, false);

	private static final Font FONT_LARGE = new Font("Sans Serif", Font.BOLD, 30);
	private static final Font FONT_SMALL = new Font("Sans Serif", Font.BOLD, 20);

	protected BufferedImage generateImage(HttpServletRequest request) {
		BufferedImage tile = new BufferedImage(256, 256, BufferedImage.TYPE_BYTE_INDEXED, COLORMODEL);
		Graphics2D g2 = tile.createGraphics();
		try {
			g2.setColor(Color.WHITE);
			g2.fillRect(0, 0, 255, 255);
			g2.setColor(Color.RED);
			g2.drawRect(0, 0, 255, 255);
			g2.drawLine(0, 0, 255, 255);
			g2.drawLine(255, 0, 0, 255);
			String url = request.getRequestURL().toString();
			String query = request.getQueryString();
			if (query != null)
				url += "?" + query;
			log.debug(url);
			String[] strings = url.split("[\\&\\?]");
			int y = 40;
			g2.setFont(FONT_SMALL);
			for (String s : strings) {
				g2.drawString(s, 8, y);
				g2.setFont(FONT_LARGE);
				y += 35;
			}
		} finally {
			g2.dispose();
		}
		return tile;
	}
}
