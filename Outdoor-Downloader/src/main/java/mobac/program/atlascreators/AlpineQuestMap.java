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

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;

import mobac.exceptions.MapCreationException;
import mobac.mapsources.mapspace.MercatorPower2MapSpace;
import mobac.program.ProgramInfo;
import mobac.program.annotations.AtlasCreatorName;
import mobac.program.annotations.SupportedParameters;
import mobac.program.atlascreators.impl.aqm.FlatPackCreator;
import mobac.program.atlascreators.tileprovider.ConvertedRawTileProvider;
import mobac.program.atlascreators.tileprovider.TileProvider;
import mobac.program.interfaces.LayerInterface;
import mobac.program.interfaces.MapInterface;
import mobac.program.interfaces.MapSource;
import mobac.program.interfaces.MapSpace;
import mobac.program.interfaces.MapSpace.ProjectionCategory;
import mobac.program.interfaces.TileImageDataWriter;
import mobac.program.model.TileImageParameters.Name;
import mobac.utilities.Utilities;
import mobac.utilities.stream.ArrayOutputStream;

/**
 * Creates maps using the AlpineQuestMap atlas format (AQM v2 complient).
 * 
 * AQM format pack tiles in a unique file using the FlatPack format. Supports multi-layers, tile resizing.
 * 
 * @author Camille
 */
@AtlasCreatorName("AlpineQuestMap (AQM)")
@SupportedParameters(names = { Name.format, Name.height, Name.width })
public class AlpineQuestMap extends AtlasCreator {

	public static final String AQM_VERSION = "2";

	public static final String AQM_HEADER = "V" + AQM_VERSION + "HEADER";
	public static final String AQM_LEVEL = "V" + AQM_VERSION + "LEVEL";
	public static final String AQM_LEVEL_DELIMITER = "@LEVEL";
	public static final String AQM_END_DELIMITER = "#END";

	private static final String[] SCALES = new String[] { "1:512 000 000", // 00
			"1:256 000 000", // 01
			"1:128 000 000", // 02
			"1:64 000 000", // 03
			"1:32 000 000", // 04
			"1:16 000 000", // 05
			"1:8 000 000", // 06
			"1:4 000 000", // 07
			"1:2 000 000", // 08
			"1:1 000 000", // 09
			"1:512 000", // 10
			"1:256 000", // 11
			"1:128 000", // 12
			"1:64 000", // 13
			"1:32 000", // 14
			"1:16 000", // 15
			"1:8 000", // 16
			"1:4 000", // 17
			"1:2 000", // 18
			"1:1 000", // 19
			"1:512", // 20
			"1:128", // 21
			"1:64", // 22
			"1:32", // 23
			"1:16", // 24
			"1:8", // 25
			"1:4", // 26
			"1:2", // 27
			"1:1" // 28
	};

	private FlatPackCreator packCreator = null;
	private File filePack = null;

	private double xResizeRatio = 1.0;
	private double yResizeRatio = 1.0;

	private int lastZoomLevel = 0;

	@Override
	public void initLayerCreation(final LayerInterface layer) throws IOException {
		super.initLayerCreation(layer);

		if (layer.getMapCount() > 0) {
			// create the file
			this.filePack = new File(atlasDir + "/" + layer.getName() + ".AQM");
			this.packCreator = new FlatPackCreator(filePack);
			this.lastZoomLevel = -1;

			// add map header
			this.addMapHeader(layer.getMap(0).getMapSource().toString(), layer.getName());

			// add level headers
			for (int i = 0; i < layer.getMapCount(); i++) {
				// needed to merge splitted maps due to map size (map split not needed by AQM format)
				Insets bounds = new Insets(layer.getMap(i).getMinTileCoordinate().y, layer.getMap(i)
						.getMinTileCoordinate().x, layer.getMap(i).getMaxTileCoordinate().y, layer.getMap(i)
						.getMaxTileCoordinate().x);

				// loops over all maps with the same level and add the bounds
				while (((i + 1) < layer.getMapCount()) && (layer.getMap(i).getZoom() == layer.getMap(i + 1).getZoom())) {
					i++;
					bounds.top = Math.min(bounds.top, layer.getMap(i).getMinTileCoordinate().y);
					bounds.left = Math.min(bounds.left, layer.getMap(i).getMinTileCoordinate().x);
					bounds.bottom = Math.max(bounds.bottom, layer.getMap(i).getMaxTileCoordinate().y);
					bounds.right = Math.max(bounds.right, layer.getMap(i).getMaxTileCoordinate().x);
				}

				this.addLevelHeader(layer.getMap(i), bounds);
			}
		}
	}

	@Override
	public void finishLayerCreation() throws IOException {
		// add end of file delimiter
		packCreator.add(new byte[0], AQM_END_DELIMITER);
		packCreator.close();
		packCreator = null;

		super.finishLayerCreation();
	}

	@Override
	public void abortAtlasCreation() throws IOException {
		if (packCreator != null)
			packCreator.close();
		packCreator = null;

		if (filePack != null)
			Utilities.deleteFile(filePack);
		filePack = null;

		super.abortAtlasCreation();
	}

	private final void addMapHeader(final String strID, final String strName) throws IOException {
		// version of the AQM format (internal use)
		final String strVersion = AQM_VERSION;

		// software used to create the map (displayed to user)
		final String strSoftware = ProgramInfo.getCompleteTitle();

		// date of creation (displayed to user)
		final String strDate = new SimpleDateFormat("yyyy/MM/dd").format(new Date());

		// name of the person that created the map (displayed to user)
		final String strCreator = "";

		StringWriter w = new StringWriter();
		w.write("[map]\n");
		w.write("id = " + strID + "\n");
		w.write("name = " + strName + "\n");
		w.write("version = " + strVersion + "\n");
		w.write("date = " + strDate + "\n");
		w.write("creator = " + strCreator + "\n");
		w.write("software = " + strSoftware + "\n");
		w.write("\n");
		w.flush();
		w.close();

		// add the metadata file into map
		packCreator.add(w.getBuffer().toString().getBytes(), AQM_HEADER);
	}

	private final void addLevelHeader(final MapInterface map, final Insets bounds) throws IOException {
		final int tileSize = map.getMapSource().getMapSpace().getTileSize();
		final int xMin = bounds.left / tileSize;
		final int xMax = bounds.right / tileSize;
		final int yMin = bounds.top / tileSize;
		final int yMax = bounds.bottom / tileSize;

		// unique identifier for a data source / zoom (internal use)
		final String strID = new DecimalFormat("00").format(map.getZoom());

		// name of this specific map (displayed to user)
		String strName = map.getLayer().getName();
		if (strName == null || strName.length() == 0)
			strName = "Unnamed";

		// scale of the map (displayed to user)
		String strScale = "";
		if (map.getZoom() >= 0 && map.getZoom() < SCALES.length)
			strScale = SCALES[map.getZoom()];

		// source of the map data (displayed to user)
		final String strDataSource = map.getMapSource().toString();

		// copyright of map data (displayed to user)
		final String strCopyright = map.getMapSource().toString();

		// projection of tiles (internal use)
		final String strProjection = "mercator";

		String strGeoid = "";
		if (ProjectionCategory.SPHERE.equals(map.getMapSource().getMapSpace().getProjectionCategory()))
			strGeoid = "sphere";
		else if (ProjectionCategory.ELLIPSOID.equals(map.getMapSource().getMapSpace().getProjectionCategory()))
			strGeoid = "wgs84";

		// number of tiles (internal use)
		final long nbTotalTiles = (256 * Math.round(Math.pow(2, map.getZoom()))) / tileSize;

		// check resize or resample parameters
		String strImageFormat = null;
		Dimension tilesSize = null;

		if (map.getParameters() != null) {
			strImageFormat = map.getParameters().getFormat().getFileExt();
			tilesSize = map.getParameters().getDimension();
		} else {
			strImageFormat = map.getMapSource().getTileImageType().getFileExt();
			tilesSize = map.getTileSize();
		}

		if (strImageFormat != null)
			strImageFormat = strImageFormat.toUpperCase();

		// write metadata
		StringWriter w = new StringWriter();
		w.write("[level]\n");
		w.write("id = " + strID + "\n");
		w.write("name = " + strName + "\n");
		w.write("scale = " + strScale + "\n");
		w.write("datasource = " + strDataSource + "\n");
		w.write("copyright = " + strCopyright + "\n");
		w.write("projection = " + strProjection + "\n");
		w.write("geoid = " + strGeoid + "\n");
		w.write("xtsize = " + (int) tilesSize.getWidth() + "\n");
		w.write("ytsize = " + (int) tilesSize.getHeight() + "\n");
		w.write("xtratio = " + (nbTotalTiles / 360.0) + "\n");
		w.write("ytratio = " + (nbTotalTiles / 360.0) + "\n");
		w.write("xtoffset = " + (nbTotalTiles / 2.0) + "\n");
		w.write("ytoffset = " + (nbTotalTiles / 2.0) + "\n");
		w.write("xtmin = " + xMin + "\n");
		w.write("xtmax = " + xMax + "\n");
		w.write("ytmin = " + (nbTotalTiles - yMax) + "\n");
		w.write("ytmax = " + (nbTotalTiles - yMin) + "\n");
		w.write("background = " + "#FFFFFF" + "\n");
		w.write("imgformat = " + strImageFormat + "\n");
		w.write("\n");
		w.flush();
		w.close();

		// add the metadata file into map
		packCreator.add(w.getBuffer().toString().getBytes(), AQM_LEVEL);
	}

	@Override
	public boolean testMapSource(final MapSource mapSource) {
		MapSpace mapSpace = mapSource.getMapSpace();
		return (mapSpace instanceof MercatorPower2MapSpace)
				&& (ProjectionCategory.SPHERE.equals(mapSource.getMapSpace().getProjectionCategory()) || ProjectionCategory.ELLIPSOID
						.equals(mapSource.getMapSpace().getProjectionCategory()));
	}

	@Override
	public void initializeMap(final MapInterface map, final TileProvider mapTileProvider) {
		super.initializeMap(map, mapTileProvider);

		xResizeRatio = 1.0;
		yResizeRatio = 1.0;

		if (parameters != null) {
			int mapTileSize = map.getMapSource().getMapSpace().getTileSize();
			if ((parameters.getWidth() != mapTileSize) || (parameters.getHeight() != mapTileSize)) {
				// handle image re-sampling + image re-sizing
				xResizeRatio = (double) parameters.getWidth() / (double) mapTileSize;
				yResizeRatio = (double) parameters.getHeight() / (double) mapTileSize;
			} else {
				// handle only image re-sampling
				mapDlTileProvider = new ConvertedRawTileProvider(mapDlTileProvider, parameters.getFormat());
			}
		}
	}

	@Override
	public void createMap() throws MapCreationException, InterruptedException {
		try {
			if (map.getZoom() > lastZoomLevel) {
				// metadata information at the beginning
				this.addLevelDelimiter();
				this.lastZoomLevel = map.getZoom();
			}

			// add tiles
			this.addLevelTiles();
		} catch (InterruptedException e) {
			throw e;
		} catch (Exception e) {
			throw new MapCreationException(map, e);
		}
	}

	private final void addLevelDelimiter() throws IOException {
		// add empty level delimiter file
		packCreator.add(new byte[0], AQM_LEVEL_DELIMITER);
	}

	private final void addLevelTiles() throws InterruptedException, MapCreationException {
		atlasProgress.initMapCreation((xMax - xMin + 1) * (yMax - yMin + 1));

		// number of tiles for this zoom level
		final long nbTotalTiles = (256 * Math.round(Math.pow(2, map.getZoom()))) / tileSize;

		// tile resizing
		BufferedImage tileImage = null;
		Graphics2D graphics = null;
		ArrayOutputStream buffer = null;
		TileImageDataWriter writer = null;

		if ((parameters != null) || (xResizeRatio != 1.0) || (yResizeRatio != 1.0)) {
			// resize image
			tileImage = new BufferedImage(parameters.getWidth(), parameters.getHeight(), BufferedImage.TYPE_3BYTE_BGR);

			// associated graphics with affine transform
			graphics = tileImage.createGraphics();
			graphics.setTransform(AffineTransform.getScaleInstance(xResizeRatio, yResizeRatio));
			graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

			// image compression writer
			writer = parameters.getFormat().getDataWriter();

			// buffer to store compressed image
			buffer = new ArrayOutputStream(3 * parameters.getWidth() * parameters.getHeight());

			ImageIO.setUseCache(false);
			writer.initialize();
		}

		for (int x = xMin; x <= xMax; x++) {
			for (int y = yMin; y <= yMax; y++) {
				checkUserAbort();

				atlasProgress.incMapCreationProgress();

				try {
					// retrieve the tile data (already re-sampled if needed)
					byte[] sourceTileData = mapDlTileProvider.getTileData(x, y);

					if (sourceTileData != null) {
						// there is some data
						if ((graphics != null) && (buffer != null) && (writer != null)) {
							// need to resize the tile
							final BufferedImage tile = ImageIO.read(new ByteArrayInputStream(sourceTileData));
							graphics.drawImage(tile, 0, 0, null);

							buffer.reset();

							writer.processImage(tileImage, buffer);

							sourceTileData = buffer.toByteArray();

							if (sourceTileData == null)
								throw new MapCreationException("Image resizing failed.", map);
						}

						packCreator.add(sourceTileData, "" + x + "_" + (nbTotalTiles - y)); // y tiles count began by
						// bottom in AQM
					}
				} catch (IOException e) {
					throw new MapCreationException("Error writing tile image: " + e.getMessage(), map, e);
				}
			}
		}
	}

}
