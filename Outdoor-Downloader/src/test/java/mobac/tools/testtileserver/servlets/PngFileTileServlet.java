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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 * Returns for each an every request the same static png file. For performance reasons the file "tile.png" is cached on
 * servlet initialization.
 * 
 * @author r_x
 * 
 */
public class PngFileTileServlet extends AbstractTileServlet {

	public static final String[] IMAGE_NAMES = { "tile.png", "gradient.png", "cross.png" };

	private byte[] fileContent = null;

	private final int imageNum;

	public PngFileTileServlet(int imageNum) {
		super();
		this.imageNum = imageNum;
	}

	@Override
	public void init() throws ServletException {
		super.init();
		try {
			String image = IMAGE_NAMES[imageNum];
			InputStream in = this.getClass().getResourceAsStream("images/" + image);
			fileContent = new byte[in.available()];
			in.read(fileContent);
			in.close();
			log.info("Static png file " + image + " loaded successfully");
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("image/png");
		response.setContentLength(fileContent.length);
		OutputStream out = response.getOutputStream();
		out.write(fileContent);
		out.close();
		response.flushBuffer();
	}

}
