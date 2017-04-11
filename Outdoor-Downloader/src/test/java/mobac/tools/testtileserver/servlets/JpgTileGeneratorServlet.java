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

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mobac.program.tiledatawriter.TileImageJpegDataWriter;

/**
 * 
 * Generates for each request a png tile of size 256x256 containing the url request broken down to multiple lines.
 * 
 * @author r_x
 * 
 */
public class JpgTileGeneratorServlet extends AbstractTileGeneratorServlet {

	private final TileImageJpegDataWriter jpgWriter;

	/**
	 * @param compressionLevel
	 *            [0..100]
	 */
	public JpgTileGeneratorServlet(int compressionLevel) {
		super();
		jpgWriter = new TileImageJpegDataWriter(compressionLevel / 100d);
		jpgWriter.initialize();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		BufferedImage tile = generateImage(request);
		response.setContentType("image/jpeg");
		ServletOutputStream out = response.getOutputStream();
		ByteArrayOutputStream bout = new ByteArrayOutputStream(32000);
		try {
			synchronized (jpgWriter) {
				jpgWriter.processImage(tile, bout);
			}
			byte[] buf = bout.toByteArray();
			response.setContentLength(buf.length);
			out.write(buf);
		} finally {
			out.close();
			response.flushBuffer();
		}
	}
}
