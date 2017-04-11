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
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumSet;

import mobac.exceptions.AtlasTestException;
import mobac.exceptions.MapCreationException;
import mobac.gui.AtlasProgress;
import mobac.program.AtlasThread;
import mobac.program.PauseResumeHandler;
import mobac.program.atlascreators.tileprovider.TileProvider;
import mobac.program.interfaces.AtlasInterface;
import mobac.program.interfaces.LayerInterface;
import mobac.program.interfaces.MapInterface;
import mobac.program.interfaces.MapSource;
import mobac.program.model.AtlasOutputFormat;
import mobac.program.model.Settings;
import mobac.program.model.TileImageFormat;
import mobac.program.model.TileImageParameters;
import mobac.utilities.Charsets;
import mobac.utilities.Utilities;

import org.apache.log4j.Logger;

/**
 * Abstract base class for all AtlasCreator implementations.
 * 
 * The general call schema is as follows:
 * <ol>
 * <li>AtlasCreator instantiation via {@link AtlasOutputFormat#createAtlasCreatorInstance()}</li>
 * <li>AtlasCreator atlas initialization via {@link #startAtlasCreation(AtlasInterface, File)}</li>
 * <li>1 to n times {@link #initializeMap(MapInterface, TileProvider)} followed by {@link #createMap()}</li>
 * <li>AtlasCreator atlas finalization via {@link #finishAtlasCreation()}</li>
 * </ol>
 */
public abstract class AtlasCreator {

	public static final Charset TEXT_FILE_CHARSET = Charsets.ISO_8859_1;

	protected final Logger log;

	/************************************************************/
	/** atlas specific fields **/
	/************************************************************/

	protected AtlasInterface atlas;

	protected File atlasDir;

	protected AtlasProgress atlasProgress = null;

	protected PauseResumeHandler pauseResumeHandler = null;

	/************************************************************/
	/** map specific fields **/
	/************************************************************/

	protected MapInterface map;
	protected int xMin;
	protected int xMax;
	protected int yMin;
	protected int yMax;
	protected int zoom;
	protected MapSource mapSource;
	protected int tileSize;

	/**
	 * Custom tile processing parameters. <code>null</code> if disabled in GUI
	 */
	protected TileImageParameters parameters;

	protected AtlasOutputFormat atlasOutputFormat;

	protected TileProvider mapDlTileProvider;

	private boolean aborted = false;

	/**
	 * Default constructor - initializes the logging environment
	 */
	protected AtlasCreator() {
		log = Logger.getLogger(this.getClass());
	};

	/**
	 * @param customAtlasDir
	 *            if not <code>null</code> the customAtlasDir is used instead of the generated atlas directory name
	 * @throws InterruptedException
	 * @see AtlasCreator
	 */
	public void startAtlasCreation(AtlasInterface atlas, File customAtlasDir) throws AtlasTestException, IOException,
			InterruptedException {
		this.atlas = atlas;
		testAtlas();

		if (customAtlasDir == null) {
			// No explicit atlas output directory has been set - generate a new directory name
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
			String atlasDirName = atlas.getName() + "_" + sdf.format(new Date());
			File atlasOutputDir = Settings.getInstance().getAtlasOutputDirectory();
			atlasDir = new File(atlasOutputDir, atlasDirName);
		} else
			atlasDir = customAtlasDir;
		Utilities.mkDirs(atlasDir);
	}

	protected void testAtlas() throws AtlasTestException {
	}

	public void initLayerCreation(LayerInterface layer) throws IOException {

	}

	public void finishLayerCreation() throws IOException {

	}

	/**
	 * @throws InterruptedException
	 * @see AtlasCreator
	 */
	public void finishAtlasCreation() throws IOException, InterruptedException {
	}

	public void abortAtlasCreation() throws IOException {
		this.aborted = true;
	}

	public boolean isAborted() {
		return aborted;
	}

	/**
	 * Test if the {@link AtlasCreator} instance supports the selected {@link MapSource}
	 * 
	 * @param mapSource
	 * @return <code>true</code> if supported otherwise <code>false</code>
	 * @see AtlasCreator
	 */
	public abstract boolean testMapSource(MapSource mapSource);

	/**
	 * @see AtlasCreator
	 */
	public void initializeMap(MapInterface map, TileProvider mapTileProvider) {
		LayerInterface layer = map.getLayer();
		if (mapTileProvider == null)
			throw new NullPointerException();
		this.mapDlTileProvider = mapTileProvider;
		this.map = map;
		this.mapSource = map.getMapSource();
		this.tileSize = mapSource.getMapSpace().getTileSize();
		this.parameters = map.getParameters();
		xMin = map.getMinTileCoordinate().x / tileSize;
		xMax = map.getMaxTileCoordinate().x / tileSize;
		yMin = map.getMinTileCoordinate().y / tileSize;
		yMax = map.getMaxTileCoordinate().y / tileSize;
		this.zoom = map.getZoom();
		this.atlasOutputFormat = layer.getAtlas().getOutputFormat();
		Thread t = Thread.currentThread();
		if (!(t instanceof AtlasThread))
			throw new RuntimeException("Calling thread must be AtlasThread!");
		AtlasThread at = (AtlasThread) t;
		atlasProgress = at.getAtlasProgress();
		pauseResumeHandler = at.getPauseResumeHandler();
	}

	/**
	 * @throws InterruptedException
	 * @see AtlasCreator
	 */
	public abstract void createMap() throws MapCreationException, InterruptedException;

	/**
	 * Checks if the user has aborted atlas creation and if <code>true</code> an {@link InterruptedException} is thrown.
	 * 
	 * @throws InterruptedException
	 */
	public void checkUserAbort() throws InterruptedException {
		if (Thread.currentThread().isInterrupted())
			throw new InterruptedException();
		pauseResumeHandler.pauseWait();
	}

	public AtlasProgress getAtlasProgress() {
		return atlasProgress;
	}

	public int getXMin() {
		return xMin;
	}

	public int getXMax() {
		return xMax;
	}

	public int getYMin() {
		return yMin;
	}

	public int getYMax() {
		return yMax;
	}

	public MapInterface getMap() {
		return map;
	}

	public TileImageParameters getParameters() {
		return parameters;
	}

	public TileProvider getMapDlTileProvider() {
		return mapDlTileProvider;
	}

	/**
	 * Tests all maps of the currently active atlas if a custom tile image format has been specified and if the
	 * specified format is equal to the <code>allowedFormat</code>.
	 * 
	 * @param allowedFormat
	 * @throws AtlasTestException
	 */
	protected void performTest_AtlasTileFormat(EnumSet<TileImageFormat> allowedFormats) throws AtlasTestException {
		for (LayerInterface layer : atlas) {
			for (MapInterface map : layer) {
				TileImageParameters parameters = map.getParameters();
				if (parameters == null)
					continue;
				if (!allowedFormats.contains(parameters.getFormat()))
					throw new AtlasTestException(
							"Selected custom tile format not supported - only the following format(s) are supported: "
									+ allowedFormats, map);
			}
		}
	}

	protected void performTest_MaxMapZoom(int maxZoom) throws AtlasTestException {
		for (LayerInterface layer : atlas) {
			for (MapInterface map : layer) {
				if (map.getZoom() > maxZoom)
					throw new AtlasTestException("Maximum zoom is " + maxZoom + " for this atlas format", map);
			}
		}
	}

}
