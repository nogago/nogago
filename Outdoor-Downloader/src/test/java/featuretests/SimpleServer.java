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
package featuretests;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Properties;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import Acme.Serve.Serve;

public class SimpleServer {
	static SecureRandom RND = new SecureRandom();
	private static final byte[] COLORS = { 0,// 
			(byte) 0xff, (byte) 0xff, (byte) 0xff, // white
			(byte) 0xff, (byte) 0x00, (byte) 0x00 // red
	};

	private static final IndexColorModel COLORMODEL = new IndexColorModel(8, 2, COLORS, 1, false);

	private static final Font FONT_LARGE = new Font("Sans Serif", Font.BOLD, 30);
	private static final Font FONT_SMALL = new Font("Sans Serif", Font.BOLD, 20);

	public static class DummyDataServlet extends HttpServlet {

		@Override
		protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,
				IOException {
			BufferedImage image = new BufferedImage(256, 256, BufferedImage.TYPE_BYTE_INDEXED, COLORMODEL);
			Graphics2D g2 = image.createGraphics();
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
				String[] strings = url.split("[\\&\\?]");
				int y = 40;
				g2.setFont(FONT_SMALL);
				for (String s : strings) {
					g2.drawString(s, 8, y);
					g2.setFont(FONT_LARGE);
					y += 35;
				}
				ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
				writer.setOutput(ImageIO.createImageOutputStream(response.getOutputStream()));
				IIOImage ioImage = new IIOImage(image, null, null);
				writer.write(null, ioImage, null);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				g2.dispose();
			}
		}

	}

	public static void main(String[] args) {
		Properties properties = new Properties();

		properties.put("port", 80);
		properties.put("z", "20");
		properties.put("keep-alive", Boolean.TRUE);
		properties.put("bind-address", "127.0.0.1");
		properties.setProperty(Acme.Serve.Serve.ARG_NOHUP, "nohup");
		Serve tjws = new Serve(properties, System.out);

		tjws.addServlet("/", new DummyDataServlet());
		tjws.serve();
	}
}
