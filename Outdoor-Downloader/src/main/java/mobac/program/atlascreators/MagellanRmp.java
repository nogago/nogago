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
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import mobac.exceptions.AtlasTestException;
import mobac.exceptions.MapCreationException;
import mobac.mapsources.mapspace.MercatorPower2MapSpace;
import mobac.program.annotations.AtlasCreatorName;
import mobac.program.annotations.SupportedParameters;
import mobac.program.atlascreators.impl.rmp.BoundingRect;
import mobac.program.atlascreators.impl.rmp.MultiImage;
import mobac.program.atlascreators.impl.rmp.RmpLayer;
import mobac.program.atlascreators.impl.rmp.RmpLayer.TLMEntry;
import mobac.program.atlascreators.impl.rmp.RmpTools;
import mobac.program.atlascreators.impl.rmp.RmpWriter;
import mobac.program.atlascreators.impl.rmp.Tiledata;
import mobac.program.atlascreators.impl.rmp.rmpfile.Bmp2bit;
import mobac.program.atlascreators.impl.rmp.rmpfile.Bmp4bit;
import mobac.program.atlascreators.tileprovider.TileProvider;
import mobac.program.interfaces.AtlasInterface;
import mobac.program.interfaces.LayerInterface;
import mobac.program.interfaces.MapInterface;
import mobac.program.interfaces.MapSource;
import mobac.program.interfaces.MapSpace;
import mobac.program.interfaces.TileImageDataWriter;
import mobac.program.model.TileImageParameters.Name;
import mobac.program.tiledatawriter.TileImageJpegDataWriter;

@AtlasCreatorName(value = "Magellan (RMP)", type = "Rmp")
@SupportedParameters(names = { Name.format_jpg })
public class MagellanRmp extends AtlasCreator {

	TileImageDataWriter tileWriter = null;
	RmpWriter rmpWriter = null;
	String imageName = null;
	int layerNum = 0;

	@Override
	public boolean testMapSource(MapSource mapSource) {
		return MercatorPower2MapSpace.INSTANCE_256.equals(mapSource.getMapSpace());
	}

	@Override
	public void startAtlasCreation(AtlasInterface atlas, File customAtlasDir) throws IOException, InterruptedException,
			AtlasTestException {
		super.startAtlasCreation(atlas, customAtlasDir);
		int mapCount = 0;
		for (LayerInterface layer : atlas)
			mapCount += layer.getMapCount();
		imageName = RmpTools.buildImageName(atlas.getName());
		rmpWriter = new RmpWriter(imageName, mapCount, new File(atlasDir, imageName + ".rmp"));
	}

	@Override
	protected void testAtlas() throws AtlasTestException {
		for (LayerInterface layer : atlas) {
			for (MapInterface map : layer) {
				Point max = map.getMaxTileCoordinate();
				Point min = map.getMinTileCoordinate();
				// if (max.x - min.x > 18000 || max.y - min.y > 18000)
				// throw new AtlasTestException("Map too large. Max size 18000x18000");
				if (map.getParameters() != null) {
					if (!(map.getParameters().getFormat().getDataWriter() instanceof TileImageJpegDataWriter))
						throw new AtlasTestException("Only JPEG formats are supported", map);
				}
			}
		}
	}

	@Override
	public void initializeMap(MapInterface map, TileProvider mapTileProvider) {
		super.initializeMap(map, mapTileProvider);
		if (parameters != null) {
			tileWriter = parameters.getFormat().getDataWriter();
		} else
			tileWriter = new TileImageJpegDataWriter(0.9);
		tileWriter.initialize();
	}

	@Override
	public void createMap() throws MapCreationException, InterruptedException {
		createTiles();
	}

	protected void createTiles() throws InterruptedException, MapCreationException {
		atlasProgress.initMapCreation(1000);
		ImageIO.setUseCache(false);

		MultiImage layerImage = new MultiImage(mapSource, mapDlTileProvider, map);
		try {
			RmpLayer layer = createLayer(layerImage, layerNum);
			String layerName = RmpTools.buildTileName(imageName, layerNum);
			TLMEntry tlmEntry = layer.getTLMFile(layerName);
			rmpWriter.prepareFileEntry(tlmEntry);
			rmpWriter.writeFileEntry(layer.getA00File(layerName));
			tlmEntry.updateContent();
			rmpWriter.writePreparedFileEntry(tlmEntry);
			atlasProgress.setMapCreationProgress(1000);
		} catch (IOException e) {
			throw new MapCreationException(map, e);
		}
		layerNum++;
	}

	@Override
	public void finishAtlasCreation() throws IOException, InterruptedException {
		if (rmpWriter == null)
			return; // Creation already aborted
		try {
			rmpWriter.writeFileEntry(new Bmp2bit());
			rmpWriter.writeFileEntry(new Bmp4bit());
			rmpWriter.writeDirectory();
		} finally {
			rmpWriter.close();
			rmpWriter = null;
		}
	}

	@Override
	public void abortAtlasCreation() throws IOException {
		super.abortAtlasCreation();
		rmpWriter.delete();
		rmpWriter = null;
	}

	/**
	 * Create a new instance of a TLM file and fill it with the data of a calibrated image
	 * 
	 * @param si
	 *            image to get data from
	 * @param layer
	 *            Layer number - for status output only
	 * @return TLM instance
	 * @throws InterruptedException
	 * @throws MapCreationException
	 * @throws IOException
	 */
	public RmpLayer createLayer(MultiImage si, int layer) throws InterruptedException, MapCreationException,
			IOException {

		int count = 0;

		/* --- Create instance --- */
		RmpLayer rmpLayer = new RmpLayer(this);

		/* --- Get the coordinate space of the image --- */

		MapSpace mapSpace = mapSource.getMapSpace();

		double north = mapSpace.cYToLat(map.getMinTileCoordinate().y, zoom);
		double south = mapSpace.cYToLat(map.getMaxTileCoordinate().y, zoom);
		double west = mapSpace.cXToLon(map.getMinTileCoordinate().x, zoom);
		double east = mapSpace.cXToLon(map.getMaxTileCoordinate().x, zoom);

		BoundingRect rect = new BoundingRect(-north, -south, west, east);

		Point max = map.getMaxTileCoordinate();
		Point min = map.getMinTileCoordinate();
		int imageWidth = max.x - min.x;
		int imageHeight = max.y - min.y;

		/* --- Calculate tile dimensions --- */
		double tile_width = (rect.getEast() - rect.getWest()) * 256 / imageWidth;
		double tile_height = (rect.getSouth() - rect.getNorth()) * 256 / imageHeight;

		/* --- Calculate the positions of the upper left tile --- */
		int x_start = (int) Math.floor((rect.getWest() + 180) / tile_width);
		int y_start = (int) Math.floor((rect.getNorth() + 90) / tile_height);

		double x_end = (rect.getEast() + 180.0) / tile_width;
		double y_end = (rect.getSouth() + 90.0) / tile_height;

		/*
		 * Create the tiles - process works column wise, starting on the top left corner of the destination area.
		 */
		for (int x = x_start; x < x_end; x++) {
			for (int y = y_start; y < y_end; y++) {
				count++;

				/* --- Create tile --- */
				BoundingRect subrect = new BoundingRect(y * tile_height - 90, (y + 1) * tile_height - 90, x
						* tile_width - 180, (x + 1) * tile_width - 180);
				Tiledata td = new Tiledata(tileWriter);
				td.posx = x;
				td.posy = y;
				td.rect = subrect;
				td.si = si;
				rmpLayer.addPreparedImage(td);
			}
		}

		/* --- Build the TLM file --- */
		rmpLayer.buildTLMFile(tile_width, tile_height, rect.getWest(), rect.getEast(), rect.getNorth(), rect.getSouth());

		return rmpLayer;
	}

}
