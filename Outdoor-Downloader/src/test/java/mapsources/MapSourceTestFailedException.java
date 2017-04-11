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
package mapsources;

import java.io.IOException;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import mobac.program.interfaces.MapSource;

public class MapSourceTestFailedException extends Exception {

	private static final long serialVersionUID = 1L;

	final int httpResponseCode;
	final HttpURLConnection conn;
	final URL url;
	final Class<? extends MapSource> mapSourceClass;

	public MapSourceTestFailedException(MapSource mapSource, String msg, HttpURLConnection conn)
			throws IOException {
		super(msg);
		this.mapSourceClass = mapSource.getClass();
		this.conn = conn;
		this.url = conn.getURL();
		this.httpResponseCode = conn.getResponseCode();
	}

	public MapSourceTestFailedException(MapSource mapSource, HttpURLConnection conn) throws IOException {
		this(mapSource, "", conn);
	}

	public MapSourceTestFailedException(Class<? extends MapSource> mapSourceClass, URL url,
			int httpResponseCode) {
		super();
		this.mapSourceClass = mapSourceClass;
		this.url = url;
		this.conn = null;
		this.httpResponseCode = httpResponseCode;
	}

	public int getHttpResponseCode() {
		return httpResponseCode;
	}

	@Override
	public String getMessage() {
		String msg = super.getMessage();
		msg = "MapSource test failed: " + msg + " " + mapSourceClass.getSimpleName() + " HTTP "
				+ httpResponseCode + "\n" + conn.getURL();
		if (conn != null)
			msg += "\n" + printHeaders(conn);
		return msg;
	}

	protected String printHeaders(HttpURLConnection conn) {
		StringWriter sw = new StringWriter();
		sw.append("Headers:\n");
		for (Map.Entry<String, List<String>> entry : conn.getHeaderFields().entrySet()) {
			String key = entry.getKey();
			for (String elem : entry.getValue()) {
				if (key != null)
					sw.append(key + " = ");
				sw.append(elem);
				sw.append("\n");
			}
		}
		return sw.toString();
	}

}
