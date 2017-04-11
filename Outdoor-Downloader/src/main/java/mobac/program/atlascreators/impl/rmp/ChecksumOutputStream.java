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
/* ------------------------------------------------------------------------

   CheckSumStream.java

   Project: Testing

  --------------------------------------------------------------------------*/

/* ---
 created: 15.08.2008 a.sander

 $History:$
 --- */

package mobac.program.atlascreators.impl.rmp;

import java.io.IOException;
import java.io.OutputStream;

/**
 * OutputStream that calculates a 2Byte XOR checksum over the stream written
 * 
 */
public class ChecksumOutputStream extends OutputStream {
	private OutputStream nextStream;
	int checksum;
	boolean evenByte;

	/**
	 * Constructor
	 * 
	 * @param next_stream
	 *            stream to write data to
	 */
	public ChecksumOutputStream(OutputStream next_stream) {
		nextStream = next_stream;
		checksum = 0;
		evenByte = true;
	}

	/**
	 * Resets the checksum to 0
	 */
	public void clearChecksum() {
		checksum = 0;
		evenByte = true;
	}

	/**
	 * Returns the current checksum
	 */
	public int getChecksum() {
		return checksum;
	}

	/**
	 * Sets the current checksum
	 */
	public void setChecksum(int checksum) {
		this.checksum = checksum;
	}

	/**
	 * Writes the current checksum (2 bytes) to the output stream
	 */
	public void writeChecksum() throws IOException {
		nextStream.write((checksum >> 8) & 0xFF);
		nextStream.write(checksum & 0xFF);
	}

	@Override
	public void write(byte[] buf, int off, int len) throws IOException {
		int value;
		int o = off;

		for (int i = 0; i < len; i++) {
			if (evenByte)
				value = (((int) buf[o++]) & 0xFF) << 8;
			else
				value = (((int) buf[o++]) & 0xFF);

			checksum ^= value;
			evenByte = !evenByte;
		}

		/* --- Send to next stream --- */
		nextStream.write(buf, off, len);
	}

	@Override
	public void write(byte[] buf) throws IOException {
		write(buf, 0, buf.length);
	}

	@Override
	public void write(int val) throws IOException {
		int value;

		if (evenByte)
			value = (val & 0xFF) << 8;
		else
			value = (val & 0xFF);

		checksum ^= value;
		evenByte = !evenByte;

		/* --- Send to next stream --- */
		nextStream.write(val);
	}

	@Override
	public void close() throws IOException {
		nextStream.close();
	}

	@Override
	public void flush() throws IOException {
		nextStream.flush();
	}

}
