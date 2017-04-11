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
/**
 * 
 */
package mobac.program.atlascreators.impl.gemf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeSet;

/**
 * GEMF File creator class.
 * 
 * Reference about GEMF format: https://sites.google.com/site/abudden/android-map-store
 * 
 * @author A. S. Budden
 * @author Erik Burrows
 * 
 *         This class is a stripped-down version of the GEMFFile.java class at:
 *         http://code.google.com/p/osmdroid/source/
 *         browse/trunk/osmdroid-android/src/main/java/org/osmdroid/util/GEMFFile.java
 * 
 *         (Date: Wed, 11th of April 2012)
 * 
 *         The original GEMFFile.java from above has been reduced to the functionality of writing a GEMF archive, as
 *         reading the archive seems not to be necessary.
 * 
 * @author M. Reiter
 * 
 */
public class GEMFFileCreator {
	private static final long FILE_SIZE_LIMIT = 1 * 1024 * 1024 * 1024; // 1GB
	private static final int FILE_COPY_BUFFER_SIZE = 1024;

	private static final int VERSION = 4;
	private static final int TILE_SIZE = 256;

	private static final int U32_SIZE = 4;
	private static final int U64_SIZE = 8;

	/*
	 * Constructor to create new GEMF file from directory of sources/tiles.
	 * 
	 * @param pLocation String object representing path to first GEMF archive file. Additional files (if archive size
	 * exceeds FILE_SIZE_LIMIT will be created with numerical suffixes, eg: test.gemf-1, test.gemf-2.
	 * 
	 * @param pSourceFolders Each specified folder will be imported into the GEMF archive as a seperate source. The name
	 * of the folder will be the name of the source in the archive.
	 */
	public GEMFFileCreator(final String pLocation, final List<File> pSourceFolders) throws FileNotFoundException,
			IOException {
		/**
		 * <pre>
		 * 1. For each source folder
		 *   1. Create array of zoom levels, X rows, Y rows
		 * 2. Build index data structure index[source][zoom][range]
		 *   1. For each S-Z-X find list of Ys values
		 *   2. For each S-Z-X-Ys set, find complete X ranges
		 *   3. For each S-Z-Xr-Ys set, find complete Y ranges, create Range record
		 * 3. Write out index
		 *   1. Header
		 *   2. Sources
		 *   3. For each Range
		 *     1. Write Range record
		 * 4. For each Range record
		 *   1. For each Range entry
		 *     1. If over file size limit, start new data file
		 *   2. Write tile data
		 * </pre>
		 */

		// this.mLocation = pLocation;

		// Create in-memory array of sources, X and Y values.
		final LinkedHashMap<String, LinkedHashMap<Integer, LinkedHashMap<Integer, LinkedHashMap<Integer, File>>>> dirIndex = new LinkedHashMap<String, LinkedHashMap<Integer, LinkedHashMap<Integer, LinkedHashMap<Integer, File>>>>();

		for (final File sourceDir : pSourceFolders) {

			final LinkedHashMap<Integer, LinkedHashMap<Integer, LinkedHashMap<Integer, File>>> zList = new LinkedHashMap<Integer, LinkedHashMap<Integer, LinkedHashMap<Integer, File>>>();

			for (final File zDir : sourceDir.listFiles()) {
				// Make sure the directory name is just a number
				try {
					Integer.parseInt(zDir.getName());
				} catch (final NumberFormatException e) {
					continue;
				}

				final LinkedHashMap<Integer, LinkedHashMap<Integer, File>> xList = new LinkedHashMap<Integer, LinkedHashMap<Integer, File>>();

				for (final File xDir : zDir.listFiles()) {

					// Make sure the directory name is just a number
					try {
						Integer.parseInt(xDir.getName());
					} catch (final NumberFormatException e) {
						continue;
					}

					final LinkedHashMap<Integer, File> yList = new LinkedHashMap<Integer, File>();
					for (final File yFile : xDir.listFiles()) {

						try {
							Integer.parseInt(yFile.getName().substring(0, yFile.getName().indexOf('.')));
						} catch (final NumberFormatException e) {
							continue;
						}

						yList.put(Integer.parseInt(yFile.getName().substring(0, yFile.getName().indexOf('.'))), yFile);
					}

					xList.put(new Integer(xDir.getName()), yList);
				}

				zList.put(Integer.parseInt(zDir.getName()), xList);
			}

			dirIndex.put(sourceDir.getName(), zList);
		}

		// Create a source index list
		final LinkedHashMap<String, Integer> sourceIndex = new LinkedHashMap<String, Integer>();
		final LinkedHashMap<Integer, String> indexSource = new LinkedHashMap<Integer, String>();
		int si = 0;
		for (final String source : dirIndex.keySet()) {
			sourceIndex.put(source, new Integer(si));
			indexSource.put(new Integer(si), source);
			++si;
		}

		// Create the range objects
		final List<GEMFRange> ranges = new ArrayList<GEMFRange>();

		for (final String source : dirIndex.keySet()) {
			for (final Integer zoom : dirIndex.get(source).keySet()) {

				// Get non-contiguous Y sets for each Z/X
				final LinkedHashMap<List<Integer>, List<Integer>> ySets = new LinkedHashMap<List<Integer>, List<Integer>>();

				for (final Integer x : new TreeSet<Integer>(dirIndex.get(source).get(zoom).keySet())) {

					final List<Integer> ySet = new ArrayList<Integer>();
					for (final Integer y : dirIndex.get(source).get(zoom).get(x).keySet()) {
						ySet.add(y);
					}

					if (ySet.size() == 0) {
						continue;
					}

					Collections.sort(ySet);

					if (!ySets.containsKey(ySet)) {
						ySets.put(ySet, new ArrayList<Integer>());
					}

					ySets.get(ySet).add(x);
				}

				// For each Y set find contiguous X sets
				final LinkedHashMap<List<Integer>, List<Integer>> xSets = new LinkedHashMap<List<Integer>, List<Integer>>();

				for (final List<Integer> ySet : ySets.keySet()) {

					final TreeSet<Integer> xList = new TreeSet<Integer>(ySets.get(ySet));

					List<Integer> xSet = new ArrayList<Integer>();
					for (int i = xList.first(); i < xList.last() + 1; ++i) {
						if (xList.contains(new Integer(i))) {
							xSet.add(new Integer(i));
						} else {
							if (xSet.size() > 0) {
								xSets.put(ySet, xSet);
								xSet = new ArrayList<Integer>();
							}
						}
					}

					if (xSet.size() > 0) {
						xSets.put(ySet, xSet);
					}
				}

				// For each contiguous X set, find contiguous Y sets and create GEMFRange object
				for (final List<Integer> xSet : xSets.keySet()) {

					final TreeSet<Integer> yList = new TreeSet<Integer>(xSet);
					final TreeSet<Integer> xList = new TreeSet<Integer>(ySets.get(xSet));

					GEMFRange range = new GEMFRange();
					range.zoom = zoom;
					range.sourceIndex = sourceIndex.get(source);
					range.xMin = xList.first();
					range.xMax = xList.last();

					for (int i = yList.first(); i < yList.last() + 1; ++i) {
						if (yList.contains(new Integer(i))) {
							if (range.yMin == null) {
								range.yMin = i;
							}
							range.yMax = i;
						} else {

							if (range.yMin != null) {
								ranges.add(range);

								range = new GEMFRange();
								range.zoom = zoom;
								range.sourceIndex = sourceIndex.get(source);
								range.xMin = xList.first();
								range.xMax = xList.last();
							}
						}
					}

					if (range.yMin != null) {
						ranges.add(range);
					}
				}
			}
		}

		// Calculate size of header for computation of data offsets
		int source_list_size = 0;
		for (final String source : sourceIndex.keySet()) {
			source_list_size += (U32_SIZE + U32_SIZE + source.length());
		}

		long offset = U32_SIZE + // GEMF Version
				U32_SIZE + // Tile size
				U32_SIZE + // Number of sources
				source_list_size + ranges.size() * ((U32_SIZE * 6) + U64_SIZE) + U32_SIZE; // Number of ranges

		// Calculate offset for each range in the data set
		for (final GEMFRange range : ranges) {
			range.offset = offset;

			for (int x = range.xMin; x < range.xMax + 1; ++x) {
				for (int y = range.yMin; y < range.yMax + 1; ++y) {
					offset += (U32_SIZE + U64_SIZE);
				}
			}
		}

		final long headerSize = offset;

		RandomAccessFile gemfFile = new RandomAccessFile(pLocation, "rw");

		// Write version header
		gemfFile.writeInt(VERSION);

		// Write file size header
		gemfFile.writeInt(TILE_SIZE);

		// Write number of sources
		gemfFile.writeInt(sourceIndex.size());

		// Write source list
		for (final String source : sourceIndex.keySet()) {
			gemfFile.writeInt(sourceIndex.get(source));
			gemfFile.writeInt(source.length());
			gemfFile.write(source.getBytes());
		}

		// Write number of ranges
		gemfFile.writeInt(ranges.size());

		// Write range objects
		for (final GEMFRange range : ranges) {
			gemfFile.writeInt(range.zoom);
			gemfFile.writeInt(range.xMin);
			gemfFile.writeInt(range.xMax);
			gemfFile.writeInt(range.yMin);
			gemfFile.writeInt(range.yMax);
			gemfFile.writeInt(range.sourceIndex);
			gemfFile.writeLong(range.offset);
		}

		// Write file offset list
		for (final GEMFRange range : ranges) {
			for (int x = range.xMin; x < range.xMax + 1; ++x) {
				for (int y = range.yMin; y < range.yMax + 1; ++y) {
					gemfFile.writeLong(offset);
					final long fileSize = dirIndex.get(indexSource.get(range.sourceIndex)).get(range.zoom).get(x)
							.get(y).length();
					gemfFile.writeInt((int) fileSize);
					offset += fileSize;
				}
			}
		}

		//
		// Write tiles
		//

		final byte[] buf = new byte[FILE_COPY_BUFFER_SIZE];

		long currentOffset = headerSize;
		int fileIndex = 0;

		for (final GEMFRange range : ranges) {
			for (int x = range.xMin; x < range.xMax + 1; ++x) {
				for (int y = range.yMin; y < range.yMax + 1; ++y) {

					final long fileSize = dirIndex.get(indexSource.get(range.sourceIndex)).get(range.zoom).get(x)
							.get(y).length();

					if (currentOffset + fileSize > FILE_SIZE_LIMIT) {
						gemfFile.close();
						++fileIndex;
						gemfFile = new RandomAccessFile(pLocation + "-" + fileIndex, "rw");
						currentOffset = 0;
					} else {
						currentOffset += fileSize;
					}

					final FileInputStream tile = new FileInputStream(dirIndex.get(indexSource.get(range.sourceIndex))
							.get(range.zoom).get(x).get(y));

					int read = tile.read(buf, 0, FILE_COPY_BUFFER_SIZE);
					while (read != -1) {
						gemfFile.write(buf, 0, read);
						read = tile.read(buf, 0, FILE_COPY_BUFFER_SIZE);
					}

					tile.close();
				}
			}
		}

		gemfFile.close();

		// Complete construction of GEMFFile object
		// openFiles();
		// readHeader();
	}

	// Class to represent a range of stored tiles within the archive.
	private class GEMFRange {
		Integer zoom;
		Integer xMin;
		Integer xMax;
		Integer yMin;
		Integer yMax;
		Integer sourceIndex;
		Long offset;

		@Override
		public String toString() {
			return String.format("GEMF Range: source=%d, zoom=%d, x=%d-%d, y=%d-%d, offset=0x%08X", sourceIndex, zoom,
					xMin, xMax, yMin, yMax, offset);
		}
	};
}
