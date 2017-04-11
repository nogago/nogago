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
package mobac.gui.mapview;

//License: GPL. Copyright 2008 by Jan Peter Stotz

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryNotificationInfo;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.Hashtable;

import javax.management.Notification;
import javax.management.NotificationBroadcaster;
import javax.management.NotificationListener;

import mobac.gui.mapview.Tile.TileState;
import mobac.program.interfaces.MapSource;

import org.apache.log4j.Logger;

/**
 * {@link TileImageCache} implementation that stores all {@link Tile} objects in memory up to a certain limit (
 * {@link #getCacheSize()}). If the limit is exceeded the least recently used {@link Tile} objects will be deleted.
 * 
 * @author Jan Peter Stotz
 * @author r_x
 */
public class MemoryTileCache implements NotificationListener {

	protected final Logger log;

	/**
	 * Default cache size
	 */
	protected int cacheSize = 200;

	protected Hashtable<String, CacheEntry> hashtable;

	/**
	 * List of all tiles in their last recently used order
	 */
	protected CacheLinkedListElement lruTiles;

	public MemoryTileCache() {
		log = Logger.getLogger(this.getClass());
		hashtable = new Hashtable<String, CacheEntry>(cacheSize);
		lruTiles = new CacheLinkedListElement();
		
		cacheSize = 500;
		MemoryMXBean mbean = ManagementFactory.getMemoryMXBean();
		NotificationBroadcaster emitter = (NotificationBroadcaster) mbean;
		emitter.addNotificationListener(this, null, null);
		// Set-up each memory pool to notify if the free memory falls below 10%
		for (MemoryPoolMXBean memPool : ManagementFactory.getMemoryPoolMXBeans()) {
			if (memPool.isUsageThresholdSupported()) {
				MemoryUsage memUsage = memPool.getUsage();
				memPool.setUsageThreshold((long) (memUsage.getMax() * 0.95));
			}
		}
	}

	/**
	 * In case we are running out of memory we free half of the cached down to a minimum of 25 cached tiles.
	 */
	public void handleNotification(Notification notification, Object handback) {
		log.trace("Memory notification: " + notification.toString());
		if (!MemoryNotificationInfo.MEMORY_THRESHOLD_EXCEEDED.equals(notification.getType()))
			return;
		synchronized (lruTiles) {
			int count_half = lruTiles.getElementCount() / 2;
			count_half = Math.max(25, count_half);
			if (lruTiles.getElementCount() <= count_half)
				return;
			log.warn("memory low - freeing cached tiles: " + lruTiles.getElementCount() + " -> " + count_half);
			try {
				while (lruTiles.getElementCount() > count_half) {
					removeEntry(lruTiles.getLastElement());
				}
			} catch (Exception e) {
				log.error("", e);
			}
		}
	}

	public void addTile(Tile tile) {
		CacheEntry entry = createCacheEntry(tile);
		hashtable.put(tile.getKey(), entry);
		lruTiles.addFirst(entry);
		if (hashtable.size() > cacheSize)
			removeOldEntries();
	}

	public Tile getTile(MapSource source, int x, int y, int z) {
		CacheEntry entry = hashtable.get(Tile.getTileKey(source, x, y, z));
		if (entry == null)
			return null;
		// We don't care about placeholder tiles and hourglass image tiles, the
		// important tiles are the loaded ones
		if (entry.tile.getTileState() == TileState.TS_LOADED)
			lruTiles.moveElementToFirstPos(entry);
		return entry.tile;
	}

	/**
	 * Removes the least recently used tiles
	 */
	protected void removeOldEntries() {
		synchronized (lruTiles) {
			try {
				while (lruTiles.getElementCount() > cacheSize) {
					removeEntry(lruTiles.getLastElement());
				}
			} catch (Exception e) {
				log.warn("", e);
			}
		}
	}

	protected void removeEntry(CacheEntry entry) {
		hashtable.remove(entry.tile.getKey());
		lruTiles.removeEntry(entry);
	}

	protected CacheEntry createCacheEntry(Tile tile) {
		return new CacheEntry(tile);
	}

	/**
	 * Clears the cache deleting all tiles from memory
	 */
	public void clear() {
		synchronized (lruTiles) {
			hashtable.clear();
			lruTiles.clear();
		}
	}

	public int getTileCount() {
		return hashtable.size();
	}

	public int getCacheSize() {
		return cacheSize;
	}

	/**
	 * Changes the maximum number of {@link Tile} objects that this cache holds.
	 * 
	 * @param cacheSize
	 *            new maximum number of tiles
	 */
	public void setCacheSize(int cacheSize) {
		this.cacheSize = cacheSize;
		if (hashtable.size() > cacheSize)
			removeOldEntries();
	}

	/**
	 * Linked list element holding the {@link Tile} and links to the {@link #next} and {@link #prev} item in the list.
	 */
	protected static class CacheEntry {
		Tile tile;

		CacheEntry next;
		CacheEntry prev;

		protected CacheEntry(Tile tile) {
			this.tile = tile;
		}

		public Tile getTile() {
			return tile;
		}

		public CacheEntry getNext() {
			return next;
		}

		public CacheEntry getPrev() {
			return prev;
		}

	}

	/**
	 * Special implementation of a double linked list for {@link CacheEntry} elements. It supports element removal in
	 * constant time - in difference to the Java implementation which needs O(n).
	 * 
	 * @author Jan Peter Stotz
	 */
	protected static class CacheLinkedListElement {
		protected CacheEntry firstElement = null;
		protected CacheEntry lastElement;
		protected int elementCount;

		public CacheLinkedListElement() {
			clear();
		}

		public synchronized void clear() {
			elementCount = 0;
			firstElement = null;
			lastElement = null;
		}

		/**
		 * Add the element to the head of the list.
		 * 
		 * @param new element to be added
		 */
		public synchronized void addFirst(CacheEntry element) {
			if (elementCount == 0) {
				firstElement = element;
				lastElement = element;
				element.prev = null;
				element.next = null;
			} else {
				element.next = firstElement;
				firstElement.prev = element;
				element.prev = null;
				firstElement = element;
			}
			elementCount++;
		}

		/**
		 * Removes the specified elemntent form the list.
		 * 
		 * @param element
		 *            to be removed
		 */
		public synchronized void removeEntry(CacheEntry element) {
			if (element.next != null) {
				element.next.prev = element.prev;
			}
			if (element.prev != null) {
				element.prev.next = element.next;
			}
			if (element == firstElement)
				firstElement = element.next;
			if (element == lastElement)
				lastElement = element.prev;
			element.next = null;
			element.prev = null;
			elementCount--;
		}

		public synchronized void moveElementToFirstPos(CacheEntry entry) {
			if (firstElement == entry)
				return;
			removeEntry(entry);
			addFirst(entry);
		}

		public int getElementCount() {
			return elementCount;
		}

		public CacheEntry getLastElement() {
			return lastElement;
		}

		public CacheEntry getFirstElement() {
			return firstElement;
		}

	}
}
