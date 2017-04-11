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
package mobac.exceptions;

import java.io.IOException;
import java.net.HttpURLConnection;

public class DownloadFailedException extends IOException {

	private final int httpResponseCode;
	private HttpURLConnection connection;

	public DownloadFailedException(HttpURLConnection connection, int httpResponseCode)
			throws IOException {
		super("Invaild HTTP response: " + httpResponseCode);
		this.connection = connection;
		this.httpResponseCode = httpResponseCode;
	}

	public int getHttpResponseCode() {
		return httpResponseCode;
	}

	@Override
	public String getMessage() {
		return super.getMessage() + "\n" + connection.getURL();
	}

}
