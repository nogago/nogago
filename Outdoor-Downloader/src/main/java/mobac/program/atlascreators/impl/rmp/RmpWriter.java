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
/* *********************************************
 * Copyright: Andreas Sander
 *
 *
 * ********************************************* */

package mobac.program.atlascreators.impl.rmp;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import mobac.program.atlascreators.impl.rmp.interfaces.RmpFileEntry;
import mobac.program.atlascreators.impl.rmp.rmpfile.RmpIni;
import mobac.utilities.Utilities;
import mobac.utilities.stream.CountingOutputStream;
import mobac.utilities.stream.RandomAccessFileOutputStream;

import org.apache.commons.io.output.NullOutputStream;
import org.apache.log4j.Logger;

/**
 * Class that writes files in RMP archive format
 * 
 */
public class RmpWriter {

	/**
	 * Max file size: 2147483647 bytes = 2047,99 MiB
	 */
	public static final long MAX_FILE_SIZE = 0xffffffffl;

	private static final Logger log = Logger.getLogger(RmpWriter.class);

	private final ArrayList<EntryInfo> entries = new ArrayList<EntryInfo>();
	private final File rmpFile;
	private final RandomAccessFile rmpOutputFile;
	private int projectedEntryCount;

	private ChecksumOutputStream entryOut;

	/**
	 * @param imageName
	 * @param layerCount
	 *            projected number of layers that will be written to this rmp file
	 * @param rmpFile
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public RmpWriter(String imageName, int layerCount, File rmpFile) throws IOException, InterruptedException {
		this.rmpFile = rmpFile;
		// We only use one A00 entry per map/layer - therefore we can
		// pre-calculate the number of entries:
		// RmpIni + (TLM & A00) per layer + Bmp2Bit + Bmp4bit
		this.projectedEntryCount = (3 + (2 * layerCount));
		if (rmpFile.exists())
			Utilities.deleteFile(rmpFile);
		log.debug("Writing data to " + rmpFile.getAbsolutePath());
		rmpOutputFile = new RandomAccessFile(rmpFile, "rw");
		// Calculate offset to the directory end
		int directoryEndOffset = projectedEntryCount * 24 + 10;
		rmpOutputFile.seek(directoryEndOffset);
		entryOut = new ChecksumOutputStream(new RandomAccessFileOutputStream(rmpOutputFile));
		/* --- Write the directory-end marker --- */
		RmpTools.writeFixedString(entryOut, "MAGELLAN", 30);

		RmpIni rmpIni = new RmpIni(imageName, layerCount);

		/* --- Create packer and fill it with content --- */
		writeFileEntry(rmpIni);
	}

	public void writeFileEntry(RmpFileEntry entry) throws IOException, InterruptedException {
		EntryInfo info = new EntryInfo();
		info.name = entry.getFileName();
		info.extendsion = entry.getFileExtension();
		info.offset = rmpOutputFile.getFilePointer();
		entry.writeFileContent(entryOut);
		info.length = rmpOutputFile.getFilePointer() - info.offset;
		if ((info.length % 2) != 0)
			entryOut.write(0);
		entries.add(info);
		if (rmpOutputFile.getFilePointer() > MAX_FILE_SIZE)
			throwRmpTooLarge();
		log.debug("Written data of entry " + entry + " bytes=" + info.length);
	}

	public void prepareFileEntry(RmpFileEntry entry) throws IOException, InterruptedException {
		EntryInfo info = new EntryInfo();
		info.name = entry.getFileName();
		info.extendsion = entry.getFileExtension();
		long pos = rmpOutputFile.getFilePointer();
		info.offset = pos;
		CountingOutputStream cout = new CountingOutputStream(new NullOutputStream());
		entry.writeFileContent(cout);
		info.length = cout.getBytesWritten();
		long newPos = pos + info.length;
		if ((info.length % 2) != 0)
			newPos++;
		if (newPos > MAX_FILE_SIZE)
			throwRmpTooLarge();
		rmpOutputFile.seek(newPos);
		entries.add(info);
		log.debug("Prepared data of entry " + entry + " bytes=" + info.length);
	}

	public void writePreparedFileEntry(RmpFileEntry entry) throws IOException, InterruptedException {
		long pos = rmpOutputFile.getFilePointer();
		EntryInfo info = new EntryInfo();
		info.name = entry.getFileName();
		info.extendsion = entry.getFileExtension();
		int index = entries.indexOf(info);
		if (index < 0)
			throw new RuntimeException("Index for entry not found");
		info = entries.get(index);

		rmpOutputFile.seek(info.offset);
		entry.writeFileContent(entryOut);
		if (rmpOutputFile.getFilePointer() > MAX_FILE_SIZE)
			throwRmpTooLarge();
		long newLength = rmpOutputFile.getFilePointer() - info.offset;
		if (newLength != info.length)
			throw new RuntimeException("Length of entry has changed!");
		if ((newLength % 2) != 0)
			entryOut.write(0);

		// restore old file position
		rmpOutputFile.seek(pos);
	}

	private void throwRmpTooLarge() throws IOException {
		throw new IOException("RMP file size exeeds 2GiB! The RMP file format does not support that.");
	}

	/**
	 * Writes the directory of the archive into the rmp file
	 * 
	 * @throws IOException
	 *             Error accessing disk
	 */
	public void writeDirectory() throws IOException {
		if (projectedEntryCount != entries.size())
			throw new RuntimeException("Entry count does not correspond "
					+ "to the projected layer count: \nProjected: " + projectedEntryCount + "\nPresent:"
					+ entries.size());

		// Finalize the list of written entries
		RmpTools.writeFixedString(entryOut, "MAGELLAN", 8);
		entryOut.writeChecksum();

		log.debug("Finished writing entries, updating directory");

		/* --- Create file --- */
		rmpOutputFile.seek(0);
		OutputStream out = new RandomAccessFileOutputStream(rmpOutputFile);
		ChecksumOutputStream cout = new ChecksumOutputStream(out);

		/* --- Write header with number of files --- */
		RmpTools.writeValue(cout, entries.size(), 4);
		RmpTools.writeValue(cout, entries.size(), 4);

		/* --- Write the directory --- */
		log.debug("Writing directory: " + entries.size() + " entries");
		for (EntryInfo entryInfo : entries) {

			log.trace("Entry: " + entryInfo);
			/* --- Write directory entry --- */
			RmpTools.writeFixedString(cout, entryInfo.name, 9);
			RmpTools.writeFixedString(cout, entryInfo.extendsion, 7);
			RmpTools.writeValue(cout, entryInfo.offset, 4);
			RmpTools.writeValue(cout, entryInfo.length, 4);
		}

		/* --- Write the header checksum (2 bytes) --- */
		cout.writeChecksum();

	}

	public void close() {
		try {
			rmpOutputFile.close();
		} catch (Exception e) {
			log.error("", e);
		}
	}

	public void delete() throws IOException {
		close();
		Utilities.deleteFile(rmpFile);
	}

	private static class EntryInfo {
		String name;
		String extendsion;
		long offset;
		long length;

		@Override
		public String toString() {
			return "\"" + name + "." + extendsion + "\" offset=" + offset + " length=" + length;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((extendsion == null) ? 0 : extendsion.hashCode());
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			EntryInfo other = (EntryInfo) obj;
			if (extendsion == null) {
				if (other.extendsion != null)
					return false;
			} else if (!extendsion.equals(other.extendsion))
				return false;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			return true;
		}

	}
}
