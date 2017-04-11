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

import java.awt.image.BufferedImage;
import java.net.ConnectException;

import mobac.exceptions.DownloadFailedException;
import mobac.gui.mapview.Tile.TileState;
import mobac.gui.mapview.interfaces.TileLoaderListener;
import mobac.program.download.TileDownLoader;
import mobac.program.interfaces.MapSource;
import mobac.program.interfaces.MapSource.LoadMethod;
import mobac.program.tilestore.TileStore;
import mobac.program.tilestore.TileStoreEntry;

import org.apache.log4j.Logger;

/**
 * A {@link TileLoaderJobCreator} implementation that loads tiles from OSM via HTTP and saves all loaded files in a
 * directory located in the the temporary directory. If a tile is present in this file cache it will not be loaded from
 * OSM again.
 * 
 * @author Jan Peter Stotz
 * @author r_x
 */
public class TileLoader {

	private static final Logger log = Logger.getLogger(TileLoader.class);

	protected TileStore tileStore;
	protected TileLoaderListener listener;

	public TileLoader(TileLoaderListener listener) {
		super();
		this.listener = listener;
		tileStore = TileStore.getInstance();
	}

	public Runnable createTileLoaderJob(final MapSource source, final int tilex, final int tiley, final int zoom) {
		return new TileAsyncLoadJob(source, tilex, tiley, zoom);
	}

	protected class TileAsyncLoadJob implements Runnable {

		final int tilex, tiley, zoom;
		final MapSource mapSource;
		Tile tile;
		boolean fileTilePainted = false;
		protected TileStoreEntry tileStoreEntry = null;

		public TileAsyncLoadJob(MapSource source, int tilex, int tiley, int zoom) {
			super();
			this.mapSource = source;
			this.tilex = tilex;
			this.tiley = tiley;
			this.zoom = zoom;
		}

		public void run() {
			MemoryTileCache cache = listener.getTileImageCache();
			synchronized (cache) {
				tile = cache.getTile(mapSource, tilex, tiley, zoom);
				if (tile == null || tile.tileState != TileState.TS_NEW)
					return;
				tile.setTileState(TileState.TS_LOADING);
			}
			if (loadTileFromStore())
				return;
			if (fileTilePainted) {
				Runnable job = new Runnable() {

					public void run() {
						loadOrUpdateTile();
					}
				};
				JobDispatcher.getInstance().addJob(job);
			} else {
				loadOrUpdateTile();
			}
		}

		protected void loadOrUpdateTile() {
			try {
				BufferedImage image = mapSource.getTileImage(zoom, tilex, tiley, LoadMethod.DEFAULT);
				if (image != null) {
					tile.setImage(image);
					tile.setTileState(TileState.TS_LOADED);
					listener.tileLoadingFinished(tile, true);
				} else {
					tile.setErrorImage();
					listener.tileLoadingFinished(tile, false);
				}
				return;
			} catch (ConnectException e) {
				log.warn("Downloading of " + tile + " failed " + e.getMessage());
			} catch (DownloadFailedException e) {
				log.warn("Downloading of " + tile + " failed " + e.getMessage());
			} catch (Exception e) {
				log.trace("Downloading of " + tile + " failed", e);
			}
			tile.setErrorImage();
			listener.tileLoadingFinished(tile, false);
		}

		protected boolean loadTileFromStore() {
			try {
				BufferedImage image = mapSource.getTileImage(zoom, tilex, tiley, LoadMethod.CACHE);
				if (image == null)
					return false;
				tile.setImage(image);
				listener.tileLoadingFinished(tile, true);
				if (TileDownLoader.isTileExpired(tileStoreEntry))
					return false;
				fileTilePainted = true;
				return true;
			} catch (Exception e) {
				log.error("Failed to load tile (z=" + zoom + ",x=" + tilex + ",y=" + tiley + ") from tile store", e);
			}
			return false;
		}

	}

}
