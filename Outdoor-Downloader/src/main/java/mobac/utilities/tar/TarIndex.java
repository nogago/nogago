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
package mobac.utilities.tar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.apache.log4j.Logger;

public class TarIndex {

	private static final Logger log = Logger.getLogger(TarIndex.class);
	private File tarFile;
	private RandomAccessFile tarRAFile;

	private TarIndexTable tarIndex;

	public TarIndex(File tarFile, TarIndexTable tarIndex) throws FileNotFoundException {
		super();
		this.tarFile = tarFile;
		this.tarIndex = tarIndex;
		tarRAFile = new RandomAccessFile(tarFile, "r");
	}

	public byte[] getEntryContent(String entryName) throws IOException {
		long off = tarIndex.getEntryOffset(entryName);
		if (off < 0)
			return null;
		tarRAFile.seek(off);
		byte[] buf = new byte[512];
		tarRAFile.readFully(buf);
		TarHeader th = new TarHeader();
		th.read(buf);
		int fileSize = th.getFileSizeInt();
		log.trace("reading file " + entryName + " off=" + off + " size=" + fileSize);
		byte[] data = new byte[fileSize];
		tarRAFile.readFully(data);
		return data;
	}

	public int size() {
		return tarIndex.size();
	}

	public void close() {
		try {
			tarRAFile.close();
		} catch (IOException e) {
		}
	}

	public void closeAndDelete() {
		close();
		tarFile.deleteOnExit();
		tarFile.delete();
	}
}
