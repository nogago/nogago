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
package mobac.utilities.imageio;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;

class PngChunk extends DataOutputStream {

	final CRC32 crc;
	final ByteArrayOutputStream baos;

	PngChunk(int chunkType) throws IOException {
		this(chunkType, new ByteArrayOutputStream(), new CRC32());
	}

	private PngChunk(int chunkType, ByteArrayOutputStream baos, CRC32 crc) throws IOException {
		super(new CheckedOutputStream(baos, crc));
		this.crc = crc;
		this.baos = baos;

		writeInt(chunkType);
	}

	public void writeTo(DataOutputStream out) throws IOException {
		flush();
		out.writeInt(baos.size() - 4);
		baos.writeTo(out);
		out.writeInt((int) crc.getValue());
	}
}
