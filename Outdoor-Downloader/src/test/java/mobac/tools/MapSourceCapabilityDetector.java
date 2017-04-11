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
package mobac.tools;

import static mobac.tools.Cities.BERLIN;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import mobac.mapsources.DefaultMapSourcesManager;
import mobac.mapsources.MapSourcesManager;
import mobac.mapsources.mappacks.region_oceania.NzTopoMaps;
import mobac.program.Logging;
import mobac.program.ProgramInfo;
import mobac.program.download.TileDownLoader;
import mobac.program.interfaces.HttpMapSource;
import mobac.program.interfaces.MapSource;
import mobac.program.interfaces.MapSpace;
import mobac.program.model.EastNorthCoordinate;
import mobac.program.model.Settings;
import mobac.program.model.TileImageType;
import mobac.utilities.Utilities;

import org.apache.log4j.Logger;

public class MapSourceCapabilityDetector {

	public static final Logger log = Logger.getLogger(MapSourceCapabilityDetector.class);

	public static final SecureRandom RND = new SecureRandom();

	public static final EastNorthCoordinate C_DEFAULT = BERLIN;

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		// ***************************************************************************
		Class<? extends HttpMapSource> mapSourceClass = NzTopoMaps.class;
		// ***************************************************************************

		Logging.configureLogging();
		ProgramInfo.initialize();
		try {
			Settings.load();
		} catch (JAXBException e) {
			System.err.println(e.getMessage());
		}
		DefaultMapSourcesManager.initialize();
		MapSourcesManager.getInstance().getAllMapSources();
		List<MapSourceCapabilityDetector> result = testMapSource(mapSourceClass);
		MapSourceCapabilityGUI gui = new MapSourceCapabilityGUI(result);
		gui.setVisible(true);
	}

	public static List<MapSourceCapabilityDetector> testMapSource(Class<? extends HttpMapSource> mapSourceClass) {
		return testMapSource(mapSourceClass, Cities.getTestCoordinate(mapSourceClass, C_DEFAULT));
	}

	public static List<MapSourceCapabilityDetector> testMapSource(Class<? extends HttpMapSource> mapSourceClass,
			EastNorthCoordinate coordinate) {
		if (coordinate == null)
			throw new NullPointerException("Coordinate not set for " + mapSourceClass.getSimpleName());
		try {
			return testMapSource(mapSourceClass.newInstance(), coordinate);
		} catch (Exception e) {
			System.err.println("Error while testing map source: " + e.getMessage());
			return null;
		}
	}

	public static List<MapSourceCapabilityDetector> testMapSource(String mapSourceName, EastNorthCoordinate coordinate) {
		MapSource mapSource = MapSourcesManager.getInstance().getSourceByName(mapSourceName);
		if (!(mapSource instanceof HttpMapSource))
			throw new RuntimeException("Not an HTTP map source: " + mapSource.getName());
		return testMapSource((HttpMapSource) mapSource, coordinate);
	}

	public static List<MapSourceCapabilityDetector> testMapSource(HttpMapSource mapSource,
			EastNorthCoordinate coordinate) {
		if (!(mapSource instanceof HttpMapSource))
			throw new RuntimeException("Not an HTTP map source: " + mapSource.getName());
		ArrayList<MapSourceCapabilityDetector> result = new ArrayList<MapSourceCapabilityDetector>();
		for (int zoom = mapSource.getMinZoom(); zoom < mapSource.getMaxZoom(); zoom++) {
			MapSourceCapabilityDetector mstd = new MapSourceCapabilityDetector((HttpMapSource) mapSource, coordinate,
					zoom);
			mstd.testMapSource();
			result.add(mstd);
			// log.trace(mstd);
		}
		return result;
	}

	private final HttpMapSource mapSource;
	private final EastNorthCoordinate coordinate;
	private final int zoom;

	private URL url;
	private HttpURLConnection c;

	private boolean eTagPresent = false;
	private boolean expirationTimePresent = false;
	private boolean lastModifiedTimePresent = false;
	private boolean ifNoneMatchSupported = false;
	private boolean ifModifiedSinceSupported = false;

	private String contentType = "?";

	public MapSourceCapabilityDetector(Class<? extends HttpMapSource> mapSourceClass, EastNorthCoordinate coordinate,
			int zoom) throws InstantiationException, IllegalAccessException {
		this(mapSourceClass.newInstance(), coordinate, zoom);
	}

	public MapSourceCapabilityDetector(HttpMapSource mapSource, EastNorthCoordinate coordinate, int zoom) {
		this.mapSource = mapSource;
		if (mapSource == null)
			throw new NullPointerException("MapSource not set");
		this.coordinate = coordinate;
		this.zoom = zoom;
	}

	public void testMapSource() {
		try {
			log.debug("Testing " + mapSource.toString());

			MapSpace mapSpace = mapSource.getMapSpace();
			int tilex = mapSpace.cLonToX(coordinate.lon, zoom) / mapSpace.getTileSize();
			int tiley = mapSpace.cLatToY(coordinate.lat, zoom) / mapSpace.getTileSize();

			c = mapSource.getTileUrlConnection(zoom, tilex, tiley);
			url = c.getURL();
			log.trace("Sample url: " + c.getURL());
			log.trace("Connecting...");
			c.setReadTimeout(3000);
			c.setRequestProperty("User-agent", ProgramInfo.getUserAgent());
			c.setRequestProperty("Accept", TileDownLoader.ACCEPT);
			c.connect();
			log.debug("Connection established - response HTTP " + c.getResponseCode());
			if (c.getResponseCode() != 200)
				return;

			// printHeaders();

			byte[] content = Utilities.getInputBytes(c.getInputStream());
			TileImageType detectedContentType = Utilities.getImageType(content);

			contentType = c.getContentType();
			contentType = contentType.substring(6);
			if ("png".equals(contentType))
				contentType = "png";
			else if ("jpeg".equals(contentType))
				contentType = "jpg";
			else
				contentType = "unknown: " + c.getContentType();
			if (contentType.equals(detectedContentType.getFileExt()))
				contentType += " (verified)";
			else
				contentType += " (unverified)";
			log.debug("Image format          : " + contentType);

			String eTag = c.getHeaderField("ETag");
			eTagPresent = (eTag != null);
			if (eTagPresent) {
				// log.debug("eTag                  : " + eTag);
				testIfNoneMatch(content);
			}
			// else log.debug("eTag                  : -");

			// long date = c.getDate();
			// if (date == 0)
			// log.debug("Date time             : -");
			// else
			// log.debug("Date time             : " + new Date(date));

			long exp = c.getExpiration();
			expirationTimePresent = (c.getHeaderField("expires") != null) && (exp != 0);
			if (exp == 0) {
				// log.debug("Expiration time       : -");
			} else {
				// long diff = (exp - System.currentTimeMillis()) / 1000;
				// log.debug("Expiration time       : " + new Date(exp)
				// + " => "
				// + Utilities.formatDurationSeconds(diff));
			}
			long modified = c.getLastModified();
			lastModifiedTimePresent = (c.getHeaderField("last-modified") != null) && (modified != 0);
			// if (modified == 0)
			// log.debug("Last modified time    : not set");
			// else
			// log.debug("Last modified time    : " + new
			// Date(modified));

			testIfModified();

		} catch (Exception e) {
			e.printStackTrace();
		}
		log.debug("\n");
	}

	private void testIfNoneMatch(byte[] content) throws Exception {
		String eTag = c.getHeaderField("ETag");
		MessageDigest md5 = MessageDigest.getInstance("MD5");
		byte[] digest = md5.digest(content);
		String hexDigest = getHexString(digest);
		// log.debug("content MD5           : " + hexDigest);
		if (hexDigest.equals(eTag))
			log.debug("eTag content          : md5 hex string");
		String quotedHexDigest = "\"" + hexDigest + "\"";
		if (quotedHexDigest.equals(eTag))
			log.debug("eTag content          : quoted md5 hex string");

		HttpURLConnection c2 = (HttpURLConnection) url.openConnection();
		c2.addRequestProperty("If-None-Match", eTag);
		c2.connect();
		int code = c2.getResponseCode();
		boolean supported = (code == 304);
		ifNoneMatchSupported = supported;
		// System.out.print("If-None-Match response: ");
		// log.debug(b2s(supported) + " - " + code + " (" +
		// c2.getResponseMessage() + ")");
		c2.disconnect();
	}

	private void testIfModified() throws IOException {
		HttpURLConnection c2 = (HttpURLConnection) url.openConnection();
		c2.setIfModifiedSince(System.currentTimeMillis() + 1000); // future date
		c2.connect();
		int code = c2.getResponseCode();
		boolean supported = (code == 304);
		ifModifiedSinceSupported = supported;
		// System.out.print("If-Modified-Since     : ");
		// log.debug(b2s(supported) + " - " + code + " (" +
		// c2.getResponseMessage() + ")");
	}

	protected void printHeaders() {
		log.trace("\nHeaders:");
		for (Map.Entry<String, List<String>> entry : c.getHeaderFields().entrySet()) {
			String key = entry.getKey();
			for (String elem : entry.getValue()) {
				if (key != null)
					log.debug(key + " = ");
				log.debug(elem);
			}
		}
	}

	@Override
	public String toString() {
		StringWriter sw = new StringWriter();
		sw.append("Mapsource.........: " + mapSource.getName() + "\n");
		sw.append("Current TileUpdate: " + mapSource.getTileUpdate() + "\n");
		sw.append("If-None-Match.....: " + b2s(ifNoneMatchSupported) + "\n");
		sw.append("ETag..............: " + b2s(eTagPresent) + "\n");
		sw.append("If-Modified-Since.: " + b2s(ifModifiedSinceSupported) + "\n");
		sw.append("LastModified......: " + b2s(lastModifiedTimePresent) + "\n");
		sw.append("Expires...........: " + b2s(expirationTimePresent) + "\n");

		return sw.toString();
	}

	public int getZoom() {
		return zoom;
	}

	public boolean iseTagPresent() {
		return eTagPresent;
	}

	public boolean isExpirationTimePresent() {
		return expirationTimePresent;
	}

	public boolean isLastModifiedTimePresent() {
		return lastModifiedTimePresent;
	}

	public boolean isIfModifiedSinceSupported() {
		return ifModifiedSinceSupported;
	}

	public boolean isIfNoneMatchSupported() {
		return ifNoneMatchSupported;
	}

	public String getContentType() {
		return contentType;
	}

	private static String b2s(boolean b) {
		if (b)
			return "supported";
		else
			return "-";
	}

	static final byte[] HEX_CHAR_TABLE = { (byte) '0', (byte) '1', (byte) '2', (byte) '3', (byte) '4', (byte) '5',
			(byte) '6', (byte) '7', (byte) '8', (byte) '9', (byte) 'a', (byte) 'b', (byte) 'c', (byte) 'd', (byte) 'e',
			(byte) 'f' };

	public static String getHexString(byte[] raw) throws UnsupportedEncodingException {
		byte[] hex = new byte[2 * raw.length];
		int index = 0;

		for (byte b : raw) {
			int v = b & 0xFF;
			hex[index++] = HEX_CHAR_TABLE[v >>> 4];
			hex[index++] = HEX_CHAR_TABLE[v & 0xF];
		}
		return new String(hex, "ASCII");
	}
}
