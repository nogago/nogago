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
import java.io.UnsupportedEncodingException;

public class TarHeader {

	// private static Logger log = Logger.getLogger(TarHeader.class);

	private File baseFilePath;

	private int fileNameLength;
	private final char[] fileName = new char[100];
	private final char[] fileMode = new char[8];
	private final char[] fileOwnerUserID = new char[8];
	private final char[] fileOwnerGroupID = new char[8];
	private final char[] fileSize = new char[12];
	private final char[] lastModificationTime = new char[12];
	private final char[] linkIndicator = new char[1];
	private final char[] nameOfLinkedFile = new char[100];
	private static final char[] padding = new char[255];

	public TarHeader() {
	}

	public TarHeader(File theFile, File theBaseFilePath) {
		this();
		baseFilePath = theBaseFilePath;

		this.setFileName(theFile, baseFilePath);
		this.setFileMode();
		this.setFileOwnerUserID();
		this.setFileOwnerGroupID();
		this.setFileSize(theFile);
		this.setLastModificationTime(theFile);
		this.setLinkIndicator(theFile);
	}

	public TarHeader(String fileName, int fileSize, boolean isDirectory) {
		this();
		this.setFileName(fileName);
		this.setFileMode();
		this.setFileOwnerUserID();
		this.setFileOwnerGroupID();
		this.setFileSize(fileSize);
		this.setLastModificationTime(System.currentTimeMillis());
		this.setLinkIndicator(isDirectory);
	}

	public void read(byte[] buffer) {
		String fn = new String(buffer, 0, 512);
		fn.getChars(0, 100, fileName, 0);
		fileNameLength = fn.indexOf((char) 0);
		fn.getChars(100, 108, fileMode, 0);
		fn.getChars(108, 116, fileOwnerUserID, 0);
		fn.getChars(116, 124, fileOwnerGroupID, 0);
		fn.getChars(124, 136, fileSize, 0);
		fn.getChars(136, 148, lastModificationTime, 0);
		// fn.getChars(148, 156, checksum, 0); we ignore the checksum
		fn.getChars(156, 157, linkIndicator, 0);
		fn.getChars(157, 257, nameOfLinkedFile, 0);
	}

	// S E T - Methods
	public void setFileName(File theFile, File theBaseFilePath) {

		String filePath = theFile.getAbsolutePath();
		String basePath = theBaseFilePath.getAbsolutePath();
		if (!filePath.startsWith(basePath))
			throw new RuntimeException("File \"" + filePath
					+ "\" is outside of archive base path \"" + basePath + "\"!");

		String tarFileName = filePath.substring(basePath.length(), filePath.length());

		tarFileName = tarFileName.replace('\\', '/');

		if (tarFileName.startsWith("/"))
			tarFileName = tarFileName.substring(1, tarFileName.length());

		if (theFile.isDirectory())
			tarFileName = tarFileName + "/";
		setFileName(tarFileName);
	}

	public void setFileName(String newFileName) {
		char[] theFileName = newFileName.toCharArray();

		fileNameLength = newFileName.length();
		for (int i = 0; i < fileName.length; i++) {
			if (i < theFileName.length) {
				fileName[i] = theFileName[i];
			} else {
				fileName[i] = 0;
			}
		}
	}

	public void setFileMode() {
		"   777 ".getChars(0, 7, fileMode, 0);
	}

	public void setFileOwnerUserID() {
		"     0  ".getChars(0, 7, fileOwnerUserID, 0);
	}

	public void setFileOwnerGroupID() {
		"     0  ".getChars(0, 7, fileOwnerGroupID, 0);
	}

	public void setFileSize(File theFile) {
		long fileSizeLong = 0;
		if (!theFile.isDirectory()) {
			fileSizeLong = theFile.length();
		}
		setFileSize(fileSizeLong);
	}

	public void setFileSize(long fileSize) {
		char[] fileSizeCharArray = Long.toString(fileSize, 8).toCharArray();

		int offset = 11 - fileSizeCharArray.length;

		for (int i = 0; i < 12; i++) {
			if (i < offset) {
				this.fileSize[i] = ' ';
			} else if (i == 11) {
				this.fileSize[i] = ' ';
			} else {
				this.fileSize[i] = fileSizeCharArray[i - offset];
			}
		}
	}

	public void setLastModificationTime(File theFile) {

		setLastModificationTime(theFile.lastModified());
	}

	public void setLastModificationTime(long lastModifiedTime) {
		lastModifiedTime /= 1000;

		char[] fileLastModifiedTimeCharArray = Long.toString(lastModifiedTime, 8).toCharArray();

		for (int i = 0; i < fileLastModifiedTimeCharArray.length; i++) {
			lastModificationTime[i] = fileLastModifiedTimeCharArray[i];
		}

		if (fileLastModifiedTimeCharArray.length < 12) {
			for (int i = fileLastModifiedTimeCharArray.length; i < 12; i++) {
				lastModificationTime[i] = ' ';
			}
		}
	}

	public void setLinkIndicator(File theFile) {
		setLinkIndicator(theFile.isDirectory());
	}

	public void setLinkIndicator(boolean isDirectory) {
		if (isDirectory) {
			linkIndicator[0] = '5';
		} else {
			linkIndicator[0] = '0';
		}
	}

	// G E T - Methods
	public String getFileName() {
		return new String(fileName, 0, fileNameLength);
	}

	public int getFileNameLength() {
		return fileNameLength;
	}

	public char[] getFileMode() {
		return fileMode;
	}

	public char[] getFileOwnerUserID() {
		return fileOwnerUserID;
	}

	public char[] getFileOwnerGroupID() {
		return fileOwnerGroupID;
	}

	public char[] getFileSize() {
		return fileSize;
	}

	public int getFileSizeInt() {
		return Integer.parseInt(new String(fileSize).trim(), 8);
	}

	public char[] getLastModificationTime() {
		return lastModificationTime;
	}

	public char[] getLinkIndicator() {
		return linkIndicator;
	}

	public char[] getNameOfLinkedFile() {
		return nameOfLinkedFile;
	}

	public char[] getPadding() {
		return padding;
	}

	/**
	 * <p>
	 * Checksum field content:<br>
	 * Header checksum, stored as an octal number in ASCII. To compute the
	 * checksum, set the checksum field to all spaces, then sum all bytes in the
	 * header using unsigned arithmetic. This field should be stored as six
	 * octal digits followed by a null and a space character. Note that many
	 * early implementations of tar used signed arithmetic for the checksum
	 * field, which can cause inter- operability problems when transferring
	 * archives between systems. Modern robust readers compute the checksum both
	 * ways and accept the header if either computation matches.<br>
	 * <a href="http://www.freebsd.org/cgi/man.cgi?query=tar&sektion=5&manpath=FreeBSD+8-current"
	 * >definition source</a>
	 * </p>
	 * 
	 * @param header
	 *            array containing a tar header at offset 0 (512 bytes of size)
	 *            with prepared checksum field (filled with spaces)
	 */
	public void correctCheckSum(byte[] header) {
		// Compute the checksum
		// theoretical max = 512 bytes * 255 = 130560 = o377000
		int checksum = 0;
		for (int i = 0; i < 512; i++) {
			// compute the checksum with unsigned arithmetic
			checksum = checksum + (header[i] & 0xFF);
		}
		String s = Integer.toOctalString(checksum);
		while (s.length() < 6)
			s = '0' + s;
		byte[] checksumBin = (s).getBytes();
		System.arraycopy(checksumBin, 0, header, 148, 6);
		header[154] = 0;
	}

	public byte[] getBytes() {

		StringBuffer sb = new StringBuffer(512);

		sb.append(fileName);
		sb.append(fileMode);
		sb.append(fileOwnerUserID);
		sb.append(fileOwnerGroupID);
		sb.append(fileSize);
		sb.append(lastModificationTime);
		sb.append("        "); // empty/prepared checksum
		sb.append(linkIndicator);
		sb.append(nameOfLinkedFile);
		sb.append(padding);

		byte[] result;
		try {
			result = sb.toString().getBytes("US-ASCII");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e); // should never happen
		}
		if (result.length != 512)
			throw new RuntimeException("Invalid tar header size: " + result.length);
		correctCheckSum(result);

		return result;
	}
}
