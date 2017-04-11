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
package mobac.program.atlascreators.impl.rungps;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import mobac.utilities.Charsets;
import mobac.utilities.Utilities;

/**
 * 
 * @author Tom Henne, eSymetric
 */
public class RunGPSAtlasFile {

	public final static String SUFFIX = ".ratlas";
	RandomAccessFile raf;
	RunGPSAtlasHeader rah = new RunGPSAtlasHeader();
	File cacheFile;
	OutputStream cacheOutStream = null;
	long positionInCacheFile = 0L;
	int offset = 0;
	int indexLength = 0;

	public class RAINode {

		RAINode() {
			key = -1;
			value = -1L;
			next = -1;
			child = -1;
			index = -1;
		}

		int key;
		long value;
		int next;
		int child;
		int index;

		void readFrom(int index) throws IOException {
			this.index = index;
			raf.seek(index);
			key = raf.readInt();
			value = raf.readLong();
			next = raf.readInt();
			child = raf.readInt();
		}

		void writeNew() throws IOException {
			raf.seek(indexLength); // go to end
			raf.writeInt(key);
			raf.writeLong(value);
			raf.writeInt(next);
			raf.writeInt(child);
			this.index = indexLength;
			indexLength += 20;
		}

		void write() throws IOException {
			raf.seek(index + 4);
			raf.writeLong(value);
			raf.writeInt(next);
			raf.writeInt(child);
		}

		String listAll() throws IOException {
			return listAll("");
		}

		String listAll(String path) throws IOException {
			List<RAINode> children = getChildren();
			StringBuilder sb = new StringBuilder();
			if (key != -1) { // not for root
				path += "" + key + "/";
				if (value != -1L) {
					sb.append(path).append('=').append(value).append("\n");

				}
			}
			for (RAINode childNode : children) {
				sb.append(childNode.listAll(path));

			}
			return sb.toString();
		}

		List<RAINode> getChildren() throws IOException {
			// currentIndexPos is now at fathers node index pos or -1

			List<RAINode> children = new ArrayList<RAINode>();

			int indexPos = child;

			for (;;) {
				if (indexPos == -1) {
					break;
				}
				RAINode n = new RAINode();
				n.readFrom(indexPos);
				children.add(n);
				indexPos = n.next;
			}

			return children;
		}

		RAINode getChildNode(int key, boolean createNew) throws IOException {
			// currentIndexPos is now at fathers node index pos or -1

			// check if father has a child

			if (this.child == -1) {
				RAINode newNode = new RAINode();
				newNode.key = key;
				newNode.writeNew();

				this.child = newNode.index;
				this.write();
				return newNode;
			}

			// find node on this level

			RAINode n = new RAINode();
			int currentIndexPos = this.child;
			for (;;) {
				if (currentIndexPos == -1) {
					break;
				}
				n.readFrom(currentIndexPos);
				if (n.key == key) {
					return n;
				}
				currentIndexPos = n.next;
			}

			if (!createNew) {
				return null;
			}

			RAINode newNode = new RAINode();
			newNode.key = key;
			newNode.writeNew();

			n.next = newNode.index;
			n.write();
			return newNode;
		}

		public int getSize() {
			return indexLength;
		}

		public RandomAccessFile getRandomAccessFile() {
			return raf;
		}

		RAINode getChildNode(List<Integer> hierarchy) throws IOException {
			if (hierarchy.isEmpty()) {
				return this;

			}
			int key_ = hierarchy.get(0);
			hierarchy.remove(0);

			RAINode childNode = getChildNode(key_, false);
			if (childNode == null) {
				return null;

			} else {
				return childNode.getChildNode(hierarchy);

			}
		}
	}

	public boolean readSuccess() {
		return rah.readSuccess();
	}

	public class RunGPSAtlasHeader {

		public final static long FILETYPE_KEY = 8723487877262773L;
		public final static int HEADER_SIZE = 128; // bytes
		public final static int FILE_VERSION = 3;
		int indexSize; // bytes

		public void writeHeader() throws IOException {
			raf.seek(0);
			raf.writeLong(FILETYPE_KEY);
			raf.writeInt(FILE_VERSION);
			raf.writeInt(indexSize);
		}

		public boolean readHeader() throws IOException {
			if (raf.readLong() != FILETYPE_KEY) {
				return false;

			}
			if (raf.readInt() != FILE_VERSION) {
				return false;

			}
			indexSize = raf.readInt();

			return true;
		}

		public boolean readSuccess() {
			return indexSize > 0;
		}
	}

	public RunGPSAtlasFile(String filePath, boolean write) throws IOException {

		this.offset = RunGPSAtlasHeader.HEADER_SIZE;

		if (write) {
			cacheFile = new File(filePath + ".cac");
			cacheOutStream = new BufferedOutputStream(new FileOutputStream(cacheFile), 8216);

			raf = new RandomAccessFile(filePath, "rw");
			raf.setLength(0); // delete existing content

			indexLength = offset;
			RAINode root = new RAINode();
			root.writeNew();
		} else {
			raf = new RandomAccessFile(filePath, "r");
			rah.readHeader();
		}
	}

	public RAINode getRootNode() throws IOException {
		RAINode root = new RAINode();
		root.readFrom(offset);
		return root;
	}

	public String listAll() throws IOException {
		return getRootNode().listAll();
	}

	public void close() throws IOException {
		if (cacheOutStream != null) {
			cacheOutStream.close();
		}
		if (raf != null) {
			raf.close();
		}
	}

	public void finishArchive() throws IOException {
		cacheOutStream.close();
		cacheOutStream = null;

		FileInputStream fis = new FileInputStream(cacheFile);

		rah.indexSize = indexLength - RunGPSAtlasHeader.HEADER_SIZE;
		rah.writeHeader();

		byte[] buf = new byte[4096];

		raf.seek(indexLength);
		for (;;) {
			int l = fis.read(buf);
			if (l <= 0) {
				break;

			}
			raf.write(buf, 0, l);
		}

		fis.close();

		Utilities.deleteFile(cacheFile);
		close();
	}

	public void addData(List<Integer> hierarchy, byte[] data) throws IOException {
		addData(hierarchy, data, data.length);
	}

	public void addData(List<Integer> hierarchy, byte[] data, int length) throws IOException {
		cacheOutStream.write(data, 0, length);

		List<Integer> posHierarchy = new ArrayList<Integer>();
		posHierarchy.addAll(hierarchy);
		posHierarchy.add(1); // position
		setValue(posHierarchy, positionInCacheFile);

		List<Integer> lenHierarchy = new ArrayList<Integer>();
		lenHierarchy.addAll(hierarchy);
		lenHierarchy.add(2); // size
		setValue(lenHierarchy, length);

		positionInCacheFile += length;

	}

	public static List<Integer> makeHierarchyFromPath(String path) {
		// e.g. /10/3487/8345.png

		// sure that replace() is correct? may be replaceAll() would be better?
		path = path.replace(File.separatorChar, '/');

		int p = path.indexOf('.');
		if (p > 0) {
			path = path.substring(0, p); // remove suffix
		}
		if (path.startsWith("/")) {
			path = path.substring(1);
		}

		try {
			List<Integer> hierarchy = new ArrayList<Integer>();
			for (;;) {
				p = path.indexOf('/');
				if (p > 0) {
					hierarchy.add(Integer.parseInt(path.substring(0, p)));
					path = path.substring(p + 1);
				} else {
					hierarchy.add(Integer.parseInt(path));
					break;
				}
			}

			return hierarchy;
		} catch (NumberFormatException e) {
			return null;
		}

	}

	public byte[] getData(String path) throws IOException {
		return getData(makeHierarchyFromPath(path));
	}

	public byte[] getData(List<Integer> keyHierarchy) throws IOException {
		ArrayList<Integer> posHierarchy = new ArrayList<Integer>();
		posHierarchy.addAll(keyHierarchy);
		posHierarchy.add(1); // position
		long indexPosition = getValue(posHierarchy);
		if (indexPosition == -1) {
			return null;
		}
		ArrayList<Integer> sizeHierarchy = new ArrayList<Integer>();
		sizeHierarchy.addAll(keyHierarchy);
		sizeHierarchy.add(2); // size
		int size = (int) getValue(sizeHierarchy);
		if (size == -1) {
			return null;
		}

		byte[] buf = new byte[size];
		raf.seek(RunGPSAtlasHeader.HEADER_SIZE + rah.indexSize + indexPosition);
		raf.readFully(buf);
		return buf;
	}

	public void setValue(List<Integer> keyHierarchy, long value) throws IOException {
		setValueImpl(getRootNode(), keyHierarchy, value);
	}

	public void setValue(String path, long value) throws IOException {
		setValue(makeHierarchyFromPath(path), value);
	}

	private void setValueImpl(RAINode currentNode, List<Integer> keyHierarchy, long value) throws IOException {
		int key = keyHierarchy.get(0);
		keyHierarchy.remove(0);
		RAINode n = currentNode.getChildNode(key, true);
		if (keyHierarchy.isEmpty()) {
			n.value = value;
			n.write();
		} else {
			setValueImpl(n, keyHierarchy, value);
		}
	}

	public long getValue(List<Integer> keyHierarchy) throws IOException {
		RAINode n = getRootNode().getChildNode(keyHierarchy);
		return n == null ? -1L : n.value;
	}

	public long getValue(String path) throws IOException {
		return getValue(makeHierarchyFromPath(path));
	}

	public void setString(List<Integer> keyHierarchy, String value) throws IOException {
		byte[] data = value.getBytes(Charsets.UTF_8);
		addData(keyHierarchy, data);
	}

	public void setString(String path, String value) throws IOException {
		setString(makeHierarchyFromPath(path), value);
	}

	public String getString(List<Integer> keyHierarchy) throws IOException {
		byte[] data = getData(keyHierarchy);
		if (data != null) {
			return new String(data, Charsets.UTF_8);
		}
		return null;
	}

	public String getString(String path) throws IOException {
		return getString(makeHierarchyFromPath(path));
	}

}
