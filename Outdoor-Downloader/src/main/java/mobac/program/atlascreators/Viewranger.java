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

import java.io.File;

import mobac.exceptions.AtlasTestException;
import mobac.program.annotations.AtlasCreatorName;
import mobac.program.annotations.SupportedParameters;
import mobac.program.atlascreators.tileprovider.ConvertedRawTileProvider;
import mobac.program.atlascreators.tileprovider.PngTileProvider;
import mobac.program.atlascreators.tileprovider.TileProvider;
import mobac.program.interfaces.LayerInterface;
import mobac.program.interfaces.MapInterface;
import mobac.program.model.TileImageParameters.Name;
import mobac.program.model.TileImageType;


/**
 * http://sourceforge.net/tracker/index.php?func=detail&aid=3456454&group_id=238075&atid=1105497 
 */
@AtlasCreatorName("Viewranger")
@SupportedParameters(names = { Name.format_png })
public class Viewranger extends OSMTracker {

	public Viewranger() {
		super();
		tileFileNamePattern = "%d/%d/%d";
	}

	@Override
	protected void testAtlas() throws AtlasTestException {
		for (LayerInterface layer : atlas) {
			for (MapInterface map : layer) {
				if (map.getParameters() == null)
					continue;
				if (!TileImageType.PNG.equals(map.getParameters().getFormat().getType()))
					throw new AtlasTestException("Only PNG output format allowed for Viewranger", map);
				if (map.getZoom() > 18)
					throw new AtlasTestException("Unsupported zoom level: " + map.getZoom()
							+ "\nMaximum supported zoom level is 18", map);
				if (map.getZoom() < 3)
					throw new AtlasTestException("Unsupported zoom level: " + map.getZoom()
							+ "\nMinimum suupported toom level is 3", map);
			}
		}
	}

	@Override
	public void initializeMap(MapInterface map, TileProvider mapTileProvider) {
		super.initializeMap(map, mapTileProvider);
		mapDir = new File(atlasDir, map.getLayer().getName());
		tileType = "";
		if (parameters == null)
			mapDlTileProvider = new PngTileProvider(mapDlTileProvider);
		else
			mapDlTileProvider = new ConvertedRawTileProvider(mapDlTileProvider, parameters.getFormat());
	}

}
