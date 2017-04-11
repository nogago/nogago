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
package mobac.utilities.stream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * A simple fixed-size version of {@link ByteArrayOutputStream}.
 */
public class ArrayOutputStream extends OutputStream {

	protected byte[] buf;

	protected int pos = 0;

	/**
	 * @param size
	 *            Size of the buffer available for writing.
	 */
	public ArrayOutputStream(int size) {
		buf = new byte[size];
	}

	/**
	 * @param array
	 *            Byte array used for writing to
	 */
	public ArrayOutputStream(byte[] array) {
		buf = array;
	}

	/**
	 * 
	 * @param array
	 *            Byte array used for writing to
	 * @param off
	 *            offset in <code>array</code>
	 */
	public ArrayOutputStream(byte[] array, int off) {
		buf = array;
		pos = off;
	}

	public byte[] toByteArray() {
		byte[] data = new byte[pos];
		System.arraycopy(buf, 0, data, 0, pos);
		return data;
	}

	public void reset() {
		pos = 0;
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		int newPos = pos + len;
		if (newPos > buf.length)
			throw new IOException("End of buffer reached");
		System.arraycopy(b, off, buf, pos, len);
		pos = newPos;
	}

	@Override
	public void write(byte[] b) throws IOException {
		write(b, 0, b.length);
	}

	@Override
	public void write(int b) throws IOException {
		write(new byte[] { (byte) b }, 0, 1);
	}

}
