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
package mobac.mapsources.mappacks.openstreetmap;

import java.io.IOException;
import java.util.concurrent.Semaphore;

import mobac.exceptions.TileException;
import mobac.program.model.TileImageType;

public class MapQuest extends AbstractOsmMapSource {

	private static String[] SERVERS = { "otile1", "otile2", "otile3", "otile4" };
	private static int SERVER_NUM = 0;

	private static final Semaphore SEM = new Semaphore(2);

	public MapQuest() {
		super("MapQuest");
		minZoom = 0;
		maxZoom = 18;
		tileUpdate = TileUpdate.IfModifiedSince;
	}

	@Override
	public byte[] getTileData(int zoom, int x, int y, LoadMethod loadMethod) throws IOException, TileException,
			InterruptedException {
		SEM.acquire();
		try {
			return super.getTileData(zoom, x, y, loadMethod);
		} finally {
			SEM.release();
		}
	}

	@Override
	public TileImageType getTileImageType() {
		return TileImageType.JPG;
	}

	@Override
	public String getTileUrl(int zoom, int tilex, int tiley) {
		String server = SERVERS[SERVER_NUM];
		SERVER_NUM = (SERVER_NUM + 1) % SERVERS.length;
		String baseUrl = "http://" + server + ".mqcdn.com/tiles/1.0.0/osm";
		return baseUrl + super.getTileUrl(zoom, tilex, tiley);
	}

	@Override
	public String toString() {
		return "OpenStreetMap MapQuest";
	}

}
