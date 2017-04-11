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
package mobac.mapsources.mappacks.openstreetmap;

import java.io.IOException;
import java.net.HttpURLConnection;

import mobac.exceptions.DownloadFailedException;
import mobac.exceptions.StopAllDownloadsException;
import mobac.exceptions.TileException;
import mobac.mapsources.AbstractMultiLayerMapSource;
import mobac.program.interfaces.HttpMapSource;
import mobac.program.interfaces.MapSource;
import mobac.program.interfaces.MapSourceTextAttribution;
import mobac.program.model.TileImageType;

public class OpenPisteMap extends AbstractMultiLayerMapSource implements MapSourceTextAttribution {

	private static final String BASE = "http://tiles.openpistemap.org/nocontours";
	//private static final String CONTOURS = "http://tiles.openpistemap.org/contours-only";
	private static final String LAMDSHED = "http://tiles2.openpistemap.org/landshaded";

	public OpenPisteMap() {
		super("OpenPisteMapBCL", TileImageType.PNG);
		mapSources = new MapSource[] { new Mapnik(),  new OpenPisteMapBase(), new OpenPisteMapLandshed()/*, new OpenPisteMapContours()*/ };
		initializeValues();
	}

	@Override
	public String toString() {
		return "Open Piste Map";
	}

	public String getAttributionText() {
		return "© OpenStreetMap contributors, CC-BY-SA";
	}

	public String getAttributionLinkURL() {
		return "http://openstreetmap.org";
	}

	public static abstract class AbstractOpenPisteMap extends AbstractOsmMapSource {

		public AbstractOpenPisteMap(String name) {
			super(name);
		}

		@Override
		public byte[] getTileData(int zoom, int x, int y, LoadMethod loadMethod) throws IOException, TileException,
				InterruptedException {
			try {
				return super.getTileData(zoom, x, y, loadMethod);
			} catch (DownloadFailedException e) {
				if (e.getHttpResponseCode() == HttpURLConnection.HTTP_FORBIDDEN) {
					throw new StopAllDownloadsException("Server blocks mass download - aborting map dowload", e);
				} else
					throw e;
			}
		}

	}

	public static class OpenPisteMapBase extends AbstractOpenPisteMap {

		public OpenPisteMapBase() {
			super("OpenPisteMap");
			maxZoom = 17;
			tileUpdate = HttpMapSource.TileUpdate.LastModified;
		}

		@Override
		public String toString() {
			return "Open Piste Contours Layer";
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			return BASE + super.getTileUrl(zoom, tilex, tiley);
		}

	}

	// public static class OpenPisteMapContours extends AbstractOpenPisteMap {
	//
	// public OpenPisteMapContours() {
	// super("OpenPisteMapCont");
	// maxZoom = 17;
	// tileUpdate = HttpMapSource.TileUpdate.LastModified;
	// }
	//
	// @Override
	// public String toString() {
	// return "Open Piste Map Base Layer";
	// }
	//
	// public String getTileUrl(int zoom, int tilex, int tiley) {
	// return CONTOURS + super.getTileUrl(zoom, tilex, tiley);
	// }
	//
	// }

	public static class OpenPisteMapLandshed extends AbstractOpenPisteMap {

		public OpenPisteMapLandshed() {
			super("OpenPisteMapLandshed");
			maxZoom = 17;
			tileUpdate = HttpMapSource.TileUpdate.LastModified;
		}

		@Override
		public String toString() {
			return "Open Piste Landshed Layer";
		}

		public String getTileUrl(int zoom, int tilex, int tiley) {
			return LAMDSHED + super.getTileUrl(zoom, tilex, tiley);
		}
	}
}