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
import mobac.program.atlascreators.impl.MapTileWriter;
import mobac.program.atlascreators.tileprovider.CacheTileProvider;
import mobac.program.interfaces.AtlasInterface;
import mobac.program.interfaces.LayerInterface;
import mobac.program.interfaces.RequiresSQLite;
import mobac.utilities.jdbc.SQLiteLoader;

/**
 * Creates maps using the OruxMaps (Android) atlas format.
 * 
 * @author orux Some code based on BigPlanetSql atlascreator
 */
@AtlasCreatorName("OruxMaps Sqlite")
public class OruxMapsSqlite extends OruxMaps implements RequiresSQLite {

	private static final String TABLE_TILES_DDL = "CREATE TABLE IF NOT EXISTS tiles (x int, y int, z int, image blob, PRIMARY KEY (x,y,z))";
	private static final String INDEX_DDL = "CREATE INDEX IF NOT EXISTS IND on tiles (x,y,z)";
	private static final String INSERT_SQL = "INSERT or IGNORE INTO tiles (x,y,z,image) VALUES (?,?,?,?)";
	private static final String TABLE_ANDROID_METADATA_DDL = "CREATE TABLE IF NOT EXISTS android_metadata (locale TEXT)";

	private static final String DATABASE_FILENAME = "OruxMapsImages.db";

	private String databaseFile;

	private Connection conn = null;
	private PreparedStatement prepStmt;

	private StringBuilder otrk2MapsContent;

	private int customTileCount;

	public OruxMapsSqlite() {
		super();
		SQLiteLoader.loadSQLiteOrShowError();
		calVersionCode = "3.0";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see mobac.program.atlascreators.AtlasCreator#startAtlasCreation(mobac.program.interfaces.AtlasInterface)
	 */
	@Override
	public void startAtlasCreation(AtlasInterface atlas, File customAtlasDir) throws AtlasTestException, IOException,
			InterruptedException {
		super.startAtlasCreation(atlas, customAtlasDir);
		try {
			SQLiteLoader.loadSQLite();
		} catch (SQLException e) {
			throw new AtlasTestException(SQLiteLoader.MSG_SQLITE_MISSING, e);
		}
	}

	/*
	 * @see mobac.program.atlascreators.AtlasCreator#initLayerCreation(mobac.program .interfaces.LayerInterface)
	 */
	@Override
	public void initLayerCreation(LayerInterface layer) throws IOException {

		super.initLayerCreation(layer);
		databaseFile = new File(oruxMapsMainDir, DATABASE_FILENAME).getAbsolutePath();
		log.debug("SQLite Database file: " + databaseFile);
		otrk2MapsContent = new StringBuilder();
		try {
			conn = getConnection();
			initializeDB();
			conn.close();
		} catch (SQLException e) {
			throw new IOException(e);
		}

	}

	private Connection getConnection() throws SQLException {

		String url = "jdbc:sqlite:/" + this.databaseFile;
		Connection conn = DriverManager.getConnection(url);
		return conn;
	}

	private void initializeDB() throws SQLException {

		Statement stat = conn.createStatement();
		stat.executeUpdate(TABLE_TILES_DDL);
		stat.executeUpdate(INDEX_DDL);
		stat.executeUpdate(TABLE_ANDROID_METADATA_DDL);
		stat.executeUpdate("INSERT INTO android_metadata VALUES ('" + Locale.getDefault().toString() + "')");
		stat.close();
	}

	@Override
	public void createMap() throws MapCreationException, InterruptedException {

		otrk2MapsContent.append(prepareOtrk2File());

		try {
			conn = getConnection();
			conn.setAutoCommit(false);
			prepStmt = conn.prepareStatement(INSERT_SQL);
			createTiles();
			conn.close();
		} catch (InterruptedException e) {
			// User has aborted process
			return;
		} catch (MapCreationException e) {
			throw e;
		} catch (Exception e) {
			throw new MapCreationException(map, e);
		}
	}

	@Override
	protected void createTiles() throws InterruptedException, MapCreationException {

		CacheTileProvider ctp = new CacheTileProvider(mapDlTileProvider);
		try {
			mapDlTileProvider = ctp;
			OruxMapTileBuilder mapTileBuilder = new OruxMapTileBuilder(this, new OruxMapTileWriterDB());
			customTileCount = mapTileBuilder.getCustomTileCount();
			atlasProgress.initMapCreation(mapTileBuilder.getCustomTileCount());
			mapTileBuilder.createTiles();
		} finally {
			ctp.cleanup();
		}
	}

	@Override
	protected String appendMapContent() {
		return otrk2MapsContent.toString();
	}

	private class OruxMapTileWriterDB implements MapTileWriter {

		private int tileCounter = 0;
		private Runtime r = Runtime.getRuntime();

		public void writeTile(int tilex, int tiley, String tileType, byte[] tileData) throws IOException {

			try {
				prepStmt.setInt(1, tilex);
				prepStmt.setInt(2, tiley);
				prepStmt.setInt(3, zoom);
				prepStmt.setBytes(4, tileData);
				prepStmt.addBatch();
				long heapAvailable = r.maxMemory() - r.totalMemory() + r.freeMemory();

				tileCounter++;
				if (heapAvailable < HEAP_MIN || tileCounter == customTileCount) {
					commit();
				}
			} catch (SQLException e) {
				throw new IOException(e);
			}

		}

		private void commit() throws SQLException {
			prepStmt.executeBatch();
			prepStmt.clearBatch();
			atlasProgress.incMapCreationProgress(tileCounter);
			tileCounter = 0;
			conn.commit();
			System.gc();
		}

		public void finalizeMap() throws IOException {
			try {
				commit();
			} catch (SQLException e) {
				throw new IOException(e);
			}
		}

	}
}
