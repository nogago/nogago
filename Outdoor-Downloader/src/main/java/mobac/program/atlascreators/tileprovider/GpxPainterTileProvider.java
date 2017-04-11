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
package mobac.program.atlascreators.tileprovider;

import java.awt.image.BufferedImage;
import java.io.IOException;

import mobac.data.gpx.gpx11.Gpx;
import mobac.program.interfaces.MapSource;
import mobac.program.interfaces.MapSpace;
import mobac.program.model.TileImageFormat;

/**
 * 
 * Incomplete!
 * 
 * TODO: Fully implement this class so that the content (points, tracks, ...) can be painted on each tile. If the
 * implementation is complete the {@link GpxPainterTileProvider} can be chained into the tile provider chain after the
 * {@link DownloadedTileProvider} (see AtlasThread line ~348).
 * 
 * Problem: texts and lines that span multiple tiles.
 * 
 */
public class GpxPainterTileProvider extends ConvertedRawTileProvider {

	private final MapSpace mapSpace;

	public GpxPainterTileProvider(MapSourceProvider tileProvider, TileImageFormat tileImageFormat, Gpx gpx) {
		super(tileProvider, tileImageFormat);
		int zoom = tileProvider.getZoom();
		MapSource mapSource = tileProvider.getMapSource();
		mapSpace = mapSource.getMapSpace();

		// TODO Prepare GPX points
	}

	@Override
	public BufferedImage getTileImage(int x, int y) throws IOException {
		BufferedImage image = super.getTileImage(x, y);

		// TODO Perform GPX painting

		return image;
	}

}
