/*
 *  XNap - A P2P framework and client.
 *
 *  See the file AUTHORS for copyright information.
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package mobac.utilities.stream;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * The global throtteled InputStream. All downloads should pipe their data through this stream.
 */
public class ThrottledInputStream extends FilterInputStream {

	// --- Data Field(s) ---

	private static ThrottleSupport ts = new ThrottleSupport();
	private int unused = 0;

	// --- Constructor(s) ---

	/**
	 * @param in
	 *            {@link InputStream} with implemented/working {@link InputStream#available()} method.
	 */
	public ThrottledInputStream(InputStream in) {
		super(in);
	}

	// --- Method(s) ---

	public static void setBandwidth(long newValue) {
		ts.setBandwidth(newValue);
	}

	public int read(byte[] b, int off, int len) throws IOException {
		int allowedlen = ts.allocate(len);
		if (allowedlen < len) {
			allowedlen = Math.min(allowedlen + unused, len);
		}
		int read = in.read(b, off, allowedlen);
		unused = len - read;
		return read;
	}
}
