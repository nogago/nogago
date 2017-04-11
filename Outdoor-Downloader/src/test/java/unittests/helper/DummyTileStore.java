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
package unittests.helper;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;

import mobac.program.interfaces.MapSource;
import mobac.program.tilestore.TileStore;
import mobac.program.tilestore.TileStoreEntry;
import mobac.program.tilestore.TileStoreInfo;

public class DummyTileStore extends TileStore {

	public static void initialize() {
		INSTANCE = new DummyTileStore();
	}

	public DummyTileStore() {
	}

	@Override
	public void clearStore(String storeName) {
	}

	@Override
	public void closeAll() {
	}

	@Override
	public boolean contains(int x, int y, int zoom, MapSource mapSource) {
		return false;
	}

	@Override
	public TileStoreEntry createNewEntry(int x, int y, int zoom, byte[] data, long timeLastModified, long timeExpires,
			String eTag) {
		return null;
	}

	@Override
	public TileStoreEntry createNewEmptyEntry(int x, int y, int zoom) {
		return null;
	}

	@Override
	public String[] getAllStoreNames() {
		return null;
	}

	@Override
	public BufferedImage getCacheCoverage(MapSource mapSource, int zoom, Point tileNumMin, Point tileNumMax)
			throws InterruptedException {
		return null;
	}

	@Override
	public TileStoreInfo getStoreInfo(String mapSourceName) throws InterruptedException {
		return null;
	}

	@Override
	public TileStoreEntry getTile(int x, int y, int zoom, MapSource mapSource) {
		return null;
	}

	@Override
	public void prepareTileStore(MapSource mapSource) {
	}

	@Override
	public void putTile(TileStoreEntry tile, MapSource mapSource) {
	}

	@Override
	public void putTileData(byte[] tileData, int x, int y, int zoom, MapSource mapSource) throws IOException {
	}

	@Override
	public void putTileData(byte[] tileData, int x, int y, int zoom, MapSource mapSource, long timeLastModified,
			long timeExpires, String eTag) throws IOException {
	}

	@Override
	public boolean storeExists(MapSource mapSource) {
		return false;
	}

}
