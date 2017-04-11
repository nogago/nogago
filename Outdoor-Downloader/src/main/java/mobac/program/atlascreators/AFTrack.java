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
package mobac.program.atlascreators;

import java.awt.Point;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;

import mobac.program.annotations.AtlasCreatorName;
import mobac.program.atlascreators.tileprovider.TileProvider;
import mobac.program.interfaces.LayerInterface;
import mobac.program.interfaces.MapInterface;
import mobac.utilities.Utilities;
import mobac.utilities.stream.ZipStoreOutputStream;

/**
 * AFTrack OSZ Atlas format
 */
@AtlasCreatorName("AFTrack (OSZ)")
public class AFTrack extends OSMTracker {

	private ArrayList<Integer> zoomLevel = new ArrayList<Integer>();

	private int maxZoom;
	private Point min;
	private Point max;

	@Override
	public void initLayerCreation(LayerInterface layer) throws IOException {
		super.initLayerCreation(layer);
		File oszFile = new File(atlasDir, layer.getName() + ".osz");
		mapTileWriter = new OszTileWriter(oszFile);
		zoomLevel.clear();
		min = new Point();
		max = new Point();
		maxZoom = -1;
	}

	@Override
	public void finishLayerCreation() throws IOException {
		mapTileWriter.finalizeMap();
		mapTileWriter = null;

		super.finishLayerCreation();
	}

	@Override
	public void initializeMap(MapInterface map, TileProvider mapTileProvider) {
		super.initializeMap(map, mapTileProvider);
		zoomLevel.add(new Integer(map.getZoom()));
		if (map.getZoom() > maxZoom) {
			maxZoom = map.getZoom();
			min.x = map.getMinTileCoordinate().x / 256;
			min.y = map.getMinTileCoordinate().y / 256;
			max.x = map.getMaxTileCoordinate().x / 256;
			max.y = map.getMaxTileCoordinate().y / 256;
		}
	}

	private class OszTileWriter extends OSMTileWriter {

		ZipStoreOutputStream zipStream;
		FileOutputStream out;

		public OszTileWriter(File oszFile) throws FileNotFoundException {
			super();
			out = new FileOutputStream(oszFile);
			zipStream = new ZipStoreOutputStream(out);
		}

		public void writeTile(int tilex, int tiley, String tileType, byte[] tileData) throws IOException {
			String entryName = String.format(tileFileNamePattern, zoom, tilex, tiley, tileType);
			zipStream.writeStoredEntry(entryName, tileData);
		}

		public void finalizeMap() throws IOException {
			ByteArrayOutputStream bout = new ByteArrayOutputStream(100);
			OutputStreamWriter writer = new OutputStreamWriter(bout);

			Collections.sort(zoomLevel);
			for (Integer zoom : zoomLevel)
				writer.append(String.format("zoom=%d\r\n", zoom.intValue()));
			writer.append(String.format("minx=%d\r\n", min.x));
			writer.append(String.format("maxx=%d\r\n", max.x));
			writer.append(String.format("miny=%d\r\n", min.y));
			writer.append(String.format("maxy=%d\r\n", max.y));
			writer.close();
			zipStream.writeStoredEntry("Manifest.txt", bout.toByteArray());
			Utilities.closeStream(zipStream);
		}

	}

}
