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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import mobac.utilities.stream.CountingOutputStream;


/**
 * Creates a new tar file and allows to add files from the file system or
 * "virtual files" that only exist in memory as <code>byte[]</code>.
 */
public class TarArchive {

	protected CountingOutputStream tarFileStream;
	protected File tarFile;
	protected File baseDir;

	/**
	 * 
	 * @param tarFile
	 * @param baseDir
	 *            root directory used for getting the relative path when adding
	 *            a file from the file system. If only in memory files are added
	 *            this parameter can be <code>null</code>
	 * @throws FileNotFoundException
	 */
	public TarArchive(File tarFile, File baseDir) throws FileNotFoundException {
		this.tarFile = tarFile;
		this.tarFileStream = new CountingOutputStream(new BufferedOutputStream(
				new FileOutputStream(tarFile, false)));
		this.baseDir = baseDir;
	}

	public long getTarFilePos() {
		return tarFileStream.getBytesWritten();
	}

	public boolean writeContentFromDir(File dirToAdd) throws IOException {
		if (!dirToAdd.isDirectory())
			return false;
		TarHeader th = new TarHeader(dirToAdd, baseDir);
		writeTarHeader(th);
		File[] files = dirToAdd.listFiles();
		Arrays.sort(files);
		for (File f : files) {
			if (!f.isDirectory())
				writeFile(f);
			else
				writeContentFromDir(f);
		}
		return true;
	}

	public void writeFile(File fileOrDirToAdd) throws IOException {
		TarHeader th = new TarHeader(fileOrDirToAdd, baseDir);
		writeTarHeader(th);

		if (!fileOrDirToAdd.isDirectory()) {
			TarRecord tr = new TarRecord(fileOrDirToAdd);
			tarFileStream.write(tr.getRecordContent());
		}
	}

	public void writeDirectory(String dirName) throws IOException {
		TarHeader th = new TarHeader(dirName, 0, true);
		writeTarHeader(th);
	}

	/**
	 * Writes a "file" into tar archive that does only exists in memory
	 * 
	 * @param fileName
	 * @param data
	 * @throws IOException
	 */
	public void writeFileFromData(String fileName, byte[] data) throws IOException {
		writeFileFromData(fileName, data, 0, data.length);
	}

	/**
	 * Writes a "file" into tar archive that does only exists in memory
	 * 
	 * @param fileName
	 * @param data
	 * @param off
	 * @param len
	 * @throws IOException
	 */
	public void writeFileFromData(String fileName, byte[] data, int off, int len)
			throws IOException {
		TarHeader th = new TarHeader(fileName, len, false);
		writeTarHeader(th);
		TarRecord tr = new TarRecord(data, off, len);
		tarFileStream.write(tr.getRecordContent());
	}

	protected void writeTarHeader(TarHeader th) throws IOException {
		tarFileStream.write(th.getBytes());
	}

	public void writeEndofArchive() throws IOException {
		byte[] endOfArchive = new byte[1024];
		tarFileStream.write(endOfArchive);
		tarFileStream.flush();
	}

	public void close() {
		try {
			tarFileStream.close();
		} catch (Exception e) {
		}
		tarFileStream = null;
	}

	public File getTarFile() {
		return tarFile;
	}
	
}
