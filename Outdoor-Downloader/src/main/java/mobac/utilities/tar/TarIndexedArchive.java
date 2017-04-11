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
import java.util.Hashtable;

/**
 * Extended version of {@link TarArchive} that automatically creates
 * {@link Hashtable} with the starting offsets of every archived file.
 */
public class TarIndexedArchive extends TarArchive {

	private TarIndexTable tarIndex;

	public TarIndexedArchive(File tarFile, int approxFileCount) throws IOException {
		super(tarFile, null);
		tarIndex = new TarIndexTable(approxFileCount);
	}

	@Override
	protected void writeTarHeader(TarHeader th) throws IOException {
		long streamPos = getTarFilePos();
		tarIndex.addTarEntry(th.getFileName(), streamPos);
		super.writeTarHeader(th);
	}

	public void delete() {
		if (tarFile != null) {
			boolean b = tarFile.delete();
			if (!b && tarFile.isFile())
				tarFile.deleteOnExit();
		}
	}

	public TarIndex getTarIndex() {
		try {
			return new TarIndex(tarFile, tarIndex);
		} catch (FileNotFoundException e) {
			// should never happen
			return null;
		}
	}

}
