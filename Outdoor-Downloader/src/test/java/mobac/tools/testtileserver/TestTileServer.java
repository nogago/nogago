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
package mobac.tools.testtileserver;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Properties;

import javax.servlet.Servlet;
import javax.swing.JOptionPane;

import mobac.program.Logging;
import mobac.tools.testtileserver.servlets.AbstractTileServlet;
import mobac.tools.testtileserver.servlets.JpgTileGeneratorServlet;
import mobac.tools.testtileserver.servlets.PngFileTileServlet;
import mobac.tools.testtileserver.servlets.PngTileGeneratorServlet;
import mobac.tools.testtileserver.servlets.ShutdownServlet;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import Acme.Serve.Serve;

/**
 * 
 * <p>
 * Provides a dummy HTTP server that returns a png for each request.
 * </p>
 * 
 * @author r_x
 */
public class TestTileServer extends Serve {

	private static final Logger log = Logger.getLogger(TestTileServer.class);

	private final int port;

	public TestTileServer(int port) {
		this.port = port;
		// setting aliases, for an optional file servlet
		PathTreeDictionary aliases = new PathTreeDictionary();
		setMappingTable(aliases);
		// setting properties for the server, and exchangable Acceptors
		Properties properties = new Properties();

		properties.put("port", port);
		properties.put("z", "20"); // max number of created threads in a thread
		properties.put("keep-alive", Boolean.TRUE);
		properties.put("bind-address", "127.0.0.1");
		// pool
		properties.setProperty(Acme.Serve.Serve.ARG_NOHUP, "nohup");
		this.arguments = properties;

		Runtime.getRuntime().addShutdownHook(new ShutdownHook());
	}

	public void start() {
		Thread t = new Thread() {

			@Override
			public void run() {
				TestTileServer.this.serve();
			}

		};
		t.setDaemon(true);
		t.start();
	}

	public void stop() {
		try {
			notifyStop();
		} catch (Exception e) {
		}
		destroyAllServlets();
	}

	@Override
	public void log(String message) {
		log.debug(message);
	}

	@Override
	public void log(Exception e, String message) {
		log.error(message, e);
	}

	@Override
	public void log(String message, Throwable t) {
		log.error(message, t);
	}

	public void setTileServlet(AbstractTileServlet tileServlet) {
		Servlet oldServlet = getServlet("/");
		if (oldServlet != null)
			unloadServlet(oldServlet);
		addServlet("/", tileServlet);
	}

	protected class ShutdownHook extends Thread {
		public void run() {
			TestTileServer.this.stop();
		}
	}

	public int getPort() {
		return port;
	}

	public static void stopOtherTileServer(int port) {
		try {
			HttpURLConnection c = (HttpURLConnection) new URL("http://127.0.0.1:" + port + "/shutdown")
					.openConnection();
			c.setConnectTimeout(100);
			c.setRequestMethod("DELETE");
			c.connect();
			if (c.getResponseCode() == 202)
				Thread.sleep(1000);
			c.disconnect();
		} catch (SocketTimeoutException e) {
			// port is unused -> OK
		} catch (Exception e) {
			log.error("failed to stop other tile server instance on port " + port + ": " + e.getMessage());
		}
	}

	public static void main(String[] args) {
		Logging.configureConsoleLogging(Level.DEBUG);
		try {
			Properties prop = new Properties();
			FileInputStream fi = new FileInputStream("TestTileServer.properties");
			prop.load(fi);
			fi.close();
			System.getProperties().putAll(prop);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Unable to load file DebugTileServer.properties: " + e.getMessage()
					+ "\nUsing default values", "Error loading properties", JOptionPane.ERROR_MESSAGE);
		}
		int port = Integer.getInteger("TestHttpServer.port", 80);
		TestTileServer server = new TestTileServer(port);

		int errorRate = Integer.getInteger("TestHttpServer.errorRate", 0);
		boolean errorOnUrl = Boolean.getBoolean("TestHttpServer.errorOnSpecificUrls");
		int delay = Integer.getInteger("TestHttpServer.delay", 0);

		boolean generatePngForEachRequest = Boolean.getBoolean("TestHttpServer.generatePNGperRequest");

		server.addServlet("/shutdown", new ShutdownServlet(server));
		AbstractTileServlet tileServlet;
		if (generatePngForEachRequest) {
			int pngCompression = Integer.getInteger("TestHttpServer.generatedPNGcompression", 1);
			tileServlet = new PngTileGeneratorServlet(pngCompression);
		} else {
			int imageNum = Integer.getInteger("TestHttpServer.staticImage", 0);
			tileServlet = new PngFileTileServlet(imageNum);
		}
		tileServlet.setErrorRate(errorRate);
		tileServlet.setErrorOnUrl(errorOnUrl);
		tileServlet.setDelay(delay);
		// server.setTileServlet(tileServlet);

		server.addServlet("/", new JpgTileGeneratorServlet(90));

		stopOtherTileServer(port);
		server.serve();
	}
}
