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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;

import mobac.exceptions.AtlasTestException;
import mobac.exceptions.MapCreationException;
import mobac.program.annotations.AtlasCreatorName;
import mobac.program.annotations.SupportedParameters;
import mobac.program.atlascreators.tileprovider.ConvertedRawTileProvider;
import mobac.program.interfaces.AtlasInterface;
import mobac.program.interfaces.MapSource;
import mobac.program.interfaces.MapSpace;
import mobac.program.interfaces.MapSpace.ProjectionCategory;
import mobac.program.interfaces.RequiresSQLite;
import mobac.program.model.Settings;
import mobac.program.model.TileImageParameters;
import mobac.program.model.TileImageParameters.Name;
import mobac.utilities.Utilities;
import mobac.utilities.jdbc.SQLiteLoader;

/**
 * Atlas/Map creator for "BigPlanet-Maps application for Android" (offline SQLite maps)
 * http://code.google.com/p/bigplanet/
 * <p>
 * Some source parts are taken from the "android-map.blogspot.com Version of Mobile Atlas Creator":
 * http://code.google.com/p/android-map/
 * </p>
 * <p>
 * Additionally the created BigPlanet SQLite database has one additional table containing special info needed by the
 * Android application <a href="http://robertdeveloper.blogspot.com/search/label/rmaps.release" >RMaps</a>.<br>
 * (Database statements: {@link #RMAPS_TABLE_INFO_DDL} and {@link #RMAPS_UPDATE_INFO_SQL} ).<br>
 * Changes made by <a href="mailto:robertk506@gmail.com">Robert</a>, author of RMaps.
 * <p>
 */
@AtlasCreatorName(value = "RMaps SQLite", type = "RMaps")
@SupportedParameters(names = { Name.format })
public class RMapsSQLite extends AtlasCreator implements RequiresSQLite {

	private static final String TABLE_DDL = "CREATE TABLE IF NOT EXISTS tiles (x int, y int, z int, s int, image blob, PRIMARY KEY (x,y,z,s))";
	private static final String INDEX_DDL = "CREATE INDEX IF NOT EXISTS IND on tiles (x,y,z,s)";
	private static final String INSERT_SQL = "INSERT or REPLACE INTO tiles (x,y,z,s,image) VALUES (?,?,?,0,?)";
	private static final String RMAPS_TABLE_INFO_DDL = "CREATE TABLE IF NOT EXISTS info AS SELECT 99 AS minzoom, 0 AS maxzoom";
	private static final String RMAPS_CLEAR_INFO_SQL = "DELETE FROM info;";
	private static final String RMAPS_UPDATE_INFO_SQL = "INSERT INTO info SELECT MIN(z) as minzoom, MAX(z) as maxzoom FROM tiles;";

	protected File databaseFile;

	protected Connection conn = null;
	protected PreparedStatement prepStmt;

	public RMapsSQLite() {
		super();
		SQLiteLoader.loadSQLiteOrShowError();
	}

	@Override
	public boolean testMapSource(MapSource mapSource) {
		MapSpace mapSpace = mapSource.getMapSpace();
		boolean correctTileSize = (256 == mapSpace.getTileSize());
		ProjectionCategory pc = mapSpace.getProjectionCategory();
		boolean correctProjection = (ProjectionCategory.SPHERE.equals(pc) || ProjectionCategory.ELLIPSOID.equals(pc));
		return correctTileSize && correctProjection;
	}

	@Override
	public void startAtlasCreation(AtlasInterface atlas, File customAtlasDir) throws IOException, AtlasTestException,
			InterruptedException {
		if (customAtlasDir == null)
			customAtlasDir = Settings.getInstance().getAtlasOutputDirectory();
		super.startAtlasCreation(atlas, customAtlasDir);
		databaseFile = new File(atlasDir, getDatabaseFileName());
		log.debug("SQLite Database file: " + databaseFile);
	}

	@Override
	public void createMap() throws MapCreationException, InterruptedException {
		try {
			Utilities.mkDir(atlasDir);
		} catch (IOException e) {
			throw new MapCreationException(map, e);
		}
		try {
			SQLiteLoader.loadSQLite();
		} catch (SQLException e) {
			throw new MapCreationException(SQLiteLoader.MSG_SQLITE_MISSING, map, e);
		}
		try {
			openConnection();
			initializeDB();
			createTiles();
		} catch (SQLException e) {
			throw new MapCreationException("Error creating SQL database \"" + databaseFile + "\": " + e.getMessage(),
					map, e);
		}
	}

	protected void openConnection() throws SQLException {
		if (conn == null || conn.isClosed()) {
			String url = "jdbc:sqlite:/" + databaseFile.getAbsolutePath();
			conn = DriverManager.getConnection(url);
		}
	}

	@Override
	public void abortAtlasCreation() throws IOException {
		SQLiteLoader.closeConnection(conn);
		conn = null;
		super.abortAtlasCreation();
	}

	@Override
	public void finishAtlasCreation() throws IOException, InterruptedException {
		SQLiteLoader.closeConnection(conn);
		conn = null;
		super.finishAtlasCreation();
	}

	protected void initializeDB() throws SQLException {
		Statement stat = conn.createStatement();
		stat.executeUpdate(TABLE_DDL);
		stat.executeUpdate(INDEX_DDL);
		createInfoTable(stat);

		stat.executeUpdate("CREATE TABLE IF NOT EXISTS android_metadata (locale TEXT)");
		if (!(stat.executeQuery("SELECT * FROM android_metadata").next())) {
			String locale = Locale.getDefault().toString();
			stat.executeUpdate("INSERT INTO android_metadata VALUES ('" + locale + "')");
		}
		stat.close();
	}

	protected void createInfoTable(Statement stat) throws SQLException {
		stat.executeUpdate(RMAPS_TABLE_INFO_DDL);
	}

	protected void createTiles() throws InterruptedException, MapCreationException {
		int maxMapProgress = (xMax - xMin + 1) * (yMax - yMin + 1);
		atlasProgress.initMapCreation(maxMapProgress);
		TileImageParameters param = map.getParameters();
		if (param != null)
			mapDlTileProvider = new ConvertedRawTileProvider(mapDlTileProvider, param.getFormat());
		try {
			conn.setAutoCommit(false);
			int batchTileCount = 0;
			Runtime r = Runtime.getRuntime();
			long heapMaxSize = r.maxMemory();
			prepStmt = conn.prepareStatement(getTileInsertSQL());
			for (int x = xMin; x <= xMax; x++) {
				for (int y = yMin; y <= yMax; y++) {
					checkUserAbort();
					atlasProgress.incMapCreationProgress();
					try {
						byte[] sourceTileData = mapDlTileProvider.getTileData(x, y);
						if (sourceTileData != null) {
							writeTile(x, y, zoom, sourceTileData);
							long heapAvailable = heapMaxSize - r.totalMemory() + r.freeMemory();

							batchTileCount++;
							if (heapAvailable < HEAP_MIN) {
								log.trace("Executing batch containing " + batchTileCount + " tiles");
								prepStmt.executeBatch();
								prepStmt.clearBatch();
								System.gc();
								batchTileCount = 0;
								conn.commit();
								atlasProgress.incMapCreationProgress(batchTileCount);
							}
						}
					} catch (IOException e) {
						throw new MapCreationException(map, e);
					}
				}
			}
			prepStmt.executeBatch();
			prepStmt.clearBatch();
			System.gc();
			updateTileMetaInfo();
			log.trace("Final commit");
			conn.commit();
			atlasProgress.setMapCreationProgress(maxMapProgress);
		} catch (SQLException e) {
			throw new MapCreationException(map, e);
		}
	}

	protected void updateTileMetaInfo() throws SQLException {
		Statement stat = conn.createStatement();
		stat.addBatch(RMAPS_CLEAR_INFO_SQL);
		stat.addBatch(RMAPS_UPDATE_INFO_SQL);
		stat.executeBatch();
		stat.close();
	}

	protected void writeTile(int x, int y, int z, byte[] tileData) throws SQLException, IOException {
		prepStmt.setInt(1, x);
		prepStmt.setInt(2, y);
		prepStmt.setInt(3, 17 - z);
		prepStmt.setBytes(4, tileData);
		prepStmt.addBatch();
	}

	protected String getDatabaseFileName() {
		return "Custom " + atlas.getName() + ".sqlitedb";
	}

	protected String getTileInsertSQL() {
		return INSERT_SQL;
	}

}
