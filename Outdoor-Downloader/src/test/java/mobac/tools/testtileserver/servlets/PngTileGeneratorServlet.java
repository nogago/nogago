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

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mobac.utilities.imageio.Png4BitWriter;

/**
 * 
 * Generates for each request a png tile of size 256x256 containing the url request broken down to multiple lines.
 * 
 * @author r_x
 * 
 */
public class PngTileGeneratorServlet extends AbstractTileGeneratorServlet {

	private int pngCompressionLevel;

	public PngTileGeneratorServlet(int pngCompressionLevel) {
		super();
		this.pngCompressionLevel = pngCompressionLevel;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		BufferedImage tile = generateImage(request);
		response.setContentType("image/png");
		OutputStream out = response.getOutputStream();
		ByteArrayOutputStream bout = new ByteArrayOutputStream(16000);
		Png4BitWriter.writeImage(bout, tile, pngCompressionLevel, request.getRequestURL().toString());
		byte[] buf = bout.toByteArray();
		response.setContentLength(buf.length);
		out.write(buf);
		out.close();
		response.flushBuffer();
	}
}
