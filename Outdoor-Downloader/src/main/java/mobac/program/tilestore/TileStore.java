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
package mobac.program.tilestore;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;

import mobac.exceptions.TileStoreException;
import mobac.program.DirectoryManager;
import mobac.program.interfaces.MapSource;
import mobac.program.model.Settings;
import mobac.program.tilestore.berkeleydb.BerkeleyDbTileStore;

import org.apache.log4j.Logger;

public abstract class TileStore {

	protected static TileStore INSTANCE = null;

	protected Logger log;

	protected File tileStoreDir;

	public static synchronized void initialize() {
		if (INSTANCE != null)
			return;
		try {
			INSTANCE = new BerkeleyDbTileStore();
		} catch (TileStoreException e) {
			String errMsg = "Multiple instances of Mobile Atlas Creator are trying "
					+ "to access the same tile store.\n"
					+ "The tile store can only be used by used by one instance at a time.\n"
					+ "Please close the other instance and try again.";
			JOptionPane.showMessageDialog(null, errMsg, "Multiple instances of Mobile " + "Atlas Creator running",
					JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
	}

	public static TileStore getInstance() {
		return INSTANCE;
	}

	protected TileStore() {
		log = Logger.getLogger(this.getClass());
		String tileStorePath = Settings.getInstance().directories.tileStoreDirectory;
		if (tileStorePath != null)
			tileStoreDir = new File(tileStorePath);
		else
			tileStoreDir = DirectoryManager.tileStoreDir;
		log.debug("Tile store path: " + tileStoreDir);
	}

	public abstract void putTileData(byte[] tileData, int x, int y, int zoom, MapSource mapSource) throws IOException;

	public abstract void putTileData(byte[] tileData, int x, int y, int zoom, MapSource mapSource,
			long timeLastModified, long timeExpires, String eTag) throws IOException;

	/**
	 * 
	 * @param x
	 * @param y
	 * @param zoom
	 * @param mapSource
	 * @return
	 */
	public abstract TileStoreEntry getTile(int x, int y, int zoom, MapSource mapSource);

	public abstract boolean contains(int x, int y, int zoom, MapSource mapSource);

	public abstract void prepareTileStore(MapSource mapSource);

	public abstract void clearStore(String storeName);

	public abstract String[] getAllStoreNames();

	/**
	 * Returns <code>true</code> if the tile store directory of the specified {@link MapSource} exists.
	 * 
	 * @param mapSource
	 * @return
	 */
	public abstract boolean storeExists(MapSource mapSource);

	/**
	 * 
	 * @param mapSourceName
	 * @return
	 * @throws InterruptedException
	 */
	public abstract TileStoreInfo getStoreInfo(String mapSourceName) throws InterruptedException;

	/**
	 * 
	 * @param mapSource
	 * @param zoom
	 * @param tileNumMin
	 * @param tileNumMax
	 * @return
	 * @throws InterruptedException
	 */
	public abstract BufferedImage getCacheCoverage(MapSource mapSource, int zoom, Point tileNumMin, Point tileNumMax)
			throws InterruptedException;

	public abstract void closeAll();

	public abstract void putTile(TileStoreEntry tile, MapSource mapSource);

	public abstract TileStoreEntry createNewEntry(int x, int y, int zoom, byte[] data, long timeLastModified,
			long timeExpires, String eTag);

	/**
	 * Creates a new {@link TileStoreEntry} that represents a missing tile in a sparse map source
	 * 
	 * @param x
	 * @param y
	 * @param zoom
	 * @return
	 */
	public abstract TileStoreEntry createNewEmptyEntry(int x, int y, int zoom);
}
