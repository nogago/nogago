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

import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;

/**
 * Allows to write to an {@link RandomAccessFile} through an
 * {@link OutputStream}.
 * 
 * <p>
 * Notes:
 * <ul>
 * <li>Closing this stream does not have any effect on the underlying
 * {@link RandomAccessFile}.</li>
 * <li>Seeking or changing the {@link RandomAccessFile} file position directly
 * affects {@link RandomAccessFileOutputStream}.</li>
 * </ul>
 * 
 */
public class RandomAccessFileOutputStream extends OutputStream {

	private final RandomAccessFile file;

	public RandomAccessFileOutputStream(RandomAccessFile f) {
		this.file = f;
	}

	@Override
	public void write(int b) throws IOException {
		file.write(b);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		file.write(b, off, len);
	}

	@Override
	public void write(byte[] b) throws IOException {
		file.write(b);
	}

	public RandomAccessFile getFile() {
		return file;
	}

}
