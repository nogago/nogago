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
package mobac.program.tilestore.berkeleydb;

import java.util.Date;

import mobac.program.tilestore.TileStoreEntry;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.KeyField;
import com.sleepycat.persist.model.Persistent;
import com.sleepycat.persist.model.PrimaryKey;

@Entity(version = 3)
public class TileDbEntry implements TileStoreEntry {

	@PrimaryKey
	protected TileDbKey tileKey;

	private byte[] data;
	private String eTag = null;

	private long timeDownloaded;

	private long timeLastModified;
	private long timeExpires;

	protected TileDbEntry() {
		// required for deserialization
	}

	public TileDbEntry(int x, int y, int zoom, byte[] data) {
		tileKey = new TileDbKey(x, y, zoom);
		if (data == null)
			throw new NullPointerException("Tile data can not be null!");
		this.data = data;
		this.timeDownloaded = System.currentTimeMillis();
	}

	public TileDbEntry(int x, int y, int zoom, byte[] data, long timeLastModified, long timeExpires, String eTag) {
		this(x, y, zoom, data);
		this.timeLastModified = timeLastModified;
		this.timeExpires = timeExpires;
		this.eTag = eTag;
	}

	public void update(long timeExpires) {
		timeDownloaded = System.currentTimeMillis();
		this.timeExpires = timeExpires;
	}

	public int getX() {
		return tileKey.x;
	}

	public int getY() {
		return tileKey.y;
	}

	public int getZoom() {
		return tileKey.zoom;
	}

	public byte[] getData() {
		return data;
	}

	public String geteTag() {
		return eTag;
	}

	public long getTimeLastModified() {
		return timeLastModified;
	}

	public long getTimeDownloaded() {
		return timeDownloaded;
	}

	public long getTimeExpires() {
		return timeExpires;
	}

	public String shortInfo() {
		return String.format("Tile z%d/%d/%d", tileKey.zoom, tileKey.x, tileKey.y);
	}

	@Override
	public String toString() {
		String tlm = (timeLastModified <= 0) ? "-" : new Date(timeLastModified).toString();
		String txp = (timeExpires <= 0) ? "-" : new Date(timeExpires).toString();
		return String.format("Tile z%d/%d/%d dl[%s] lm[%s] exp[%s] eTag[%s]", tileKey.zoom, tileKey.x, tileKey.y,
				new Date(timeDownloaded), tlm, txp, eTag);
	}

	@Persistent(version = 3)
	public static class TileDbKey {

		@KeyField(1)
		public int zoom;

		@KeyField(2)
		public int x;

		@KeyField(3)
		public int y;

		protected TileDbKey() {
		}

		public TileDbKey(int x, int y, int zoom) {
			super();
			this.x = x;
			this.y = y;
			this.zoom = zoom;
		}

		@Override
		public String toString() {
			return "[x=" + x + ", y=" + y + ", zoom=" + zoom + "]";
		}

	}

}
