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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

import mobac.exceptions.AtlasTestException;
import mobac.exceptions.MapCreationException;
import mobac.mapsources.mapspace.MercatorPower2MapSpace;
import mobac.program.annotations.AtlasCreatorName;
import mobac.program.annotations.SupportedParameters;
import mobac.program.atlascreators.tileprovider.ConvertedRawTileProvider;
import mobac.program.interfaces.AtlasInterface;
import mobac.program.interfaces.MapSource;
import mobac.program.interfaces.RequiresSQLite;
import mobac.program.model.Settings;
import mobac.program.model.TileImageParameters.Name;
import mobac.utilities.Utilities;
import mobac.utilities.jdbc.SQLiteLoader;

@AtlasCreatorName("NaviComputer (NMAP)")
@SupportedParameters(names = { Name.format })
public class NaviComputer extends AtlasCreator implements RequiresSQLite {

	private static final String NAVI_TABLES = "CREATE TABLE  MapInfo (MapType TEXT, Zoom INTEGER NOT NULL, MinX INTEGER, MaxX INTEGER, MinY INTEGER, MaxY INTEGER);\n"
			+ "CREATE TABLE  Tiles (id INTEGER NOT NULL PRIMARY KEY, X INTEGER NOT NULL, Y INTEGER NOT NULL, Zoom INTEGER NOT NULL);\n"
			+ "CREATE TABLE  TilesData (id INTEGER NOT NULL PRIMARY KEY CONSTRAINT fk_Tiles_id REFERENCES Tiles(id) ON DELETE CASCADE, Tile BLOB NULL);\n"

			+ "CREATE TRIGGER fkdc_TilesData_id_Tiles_id "
			+ "BEFORE DELETE ON Tiles "
			+ "FOR EACH ROW BEGIN "
			+ "DELETE FROM TilesData WHERE TilesData.id = OLD.id; "
			+ "END;\n"
			+ "CREATE TRIGGER fki_TilesData_id_Tiles_id "
			+ "BEFORE INSERT ON [TilesData] "
			+ "FOR EACH ROW BEGIN "
			+ "SELECT RAISE(ROLLBACK, 'insert on table TilesData violates foreign key constraint fki_TilesData_id_Tiles_id') "
			+ "WHERE (SELECT id FROM Tiles WHERE id = NEW.id) IS NULL; "
			+ "END;\n"
			+ "CREATE TRIGGER fku_TilesData_id_Tiles_id "
			+ "BEFORE UPDATE ON [TilesData] "
			+ "FOR EACH ROW BEGIN "
			+ "SELECT RAISE(ROLLBACK, 'update on table TilesData violates foreign key constraint fku_TilesData_id_Tiles_id') "
			+ "WHERE (SELECT id FROM Tiles WHERE id = NEW.id) IS NULL; "
			+ "END;\n"
			+ "CREATE INDEX IndexOfTiles ON Tiles (X, Y, Zoom);";

	private static final String INSERT_TILES = "INSERT INTO Tiles (id,X,Y,Zoom) VALUES (?,?,?,?)";
	private static final String INSERT_TILES_DATA = "INSERT  INTO TilesData (id,Tile) VALUES (?,?)";
	private static final String INSERT_MAP_INFO = "INSERT INTO MapInfo (MapType,Zoom,MinX,MaxX,MinY,MaxY) "
			+ "SELECT ?,Min(Zoom),Min(x),Max(x),Min(y),Max(y) FROM Tiles WHERE Zoom=?;";

	private String databaseFile;
	private int wmsTileCount = 1;

	private static final int COMMIT_RATE = 100;
	private int tileCommitCounter = 0;
	protected Connection conn = null;
	private PreparedStatement prepTilesData = null, prepTiles = null;

	@Override
	public boolean testMapSource(MapSource mapSource) {
		return MercatorPower2MapSpace.INSTANCE_256.equals(mapSource.getMapSpace());
	}

	@Override
	protected void testAtlas() throws AtlasTestException {
		performTest_MaxMapZoom(18);
	}

	@Override
	public void startAtlasCreation(AtlasInterface atlas, File customAtlasDir) throws IOException, AtlasTestException,
			InterruptedException {
		if (customAtlasDir == null)
			customAtlasDir = Settings.getInstance().getAtlasOutputDirectory();
		super.startAtlasCreation(atlas, customAtlasDir);

		databaseFile = getDatabaseFileName();
		log.debug("SQLite Database file: " + databaseFile);

		try {
			SQLiteLoader.loadSQLite();
		} catch (SQLException e) {
			throw new IOException(SQLiteLoader.MSG_SQLITE_MISSING, e);
		}
		try {
			Utilities.mkDir(atlasDir);
			openConnection();
			initializeDB();

			prepTilesData = conn.prepareStatement(INSERT_TILES_DATA);
			prepTiles = conn.prepareStatement(INSERT_TILES);

		} catch (SQLException e) {
			throw new AtlasTestException("Error creating SQL database \"" + databaseFile + "\": " + e.getMessage(), e);
		}

	}

	@Override
	public void createMap() throws MapCreationException, InterruptedException {
		if (parameters != null)
			mapDlTileProvider = new ConvertedRawTileProvider(mapDlTileProvider, parameters.getFormat());

		createTiles();

	}

	private void openConnection() throws SQLException {
		if (conn == null || conn.isClosed()) {
			String url = "jdbc:sqlite:/" + this.databaseFile;
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
		String mapName = mapSource.getName();
		try {
			PreparedStatement prepStat = conn.prepareStatement(INSERT_MAP_INFO);
			Statement stat = conn.createStatement();
			ResultSet rs = stat.executeQuery("SELECT Distinct Zoom From Tiles");
			while (rs.next()) {
				int zoom = rs.getInt(1);
				prepStat.setString(1, mapName);
				prepStat.setInt(2, zoom);
				prepStat.execute();
			}
			conn.commit();
			prepStat.close();
		} catch (SQLException e) {
			log.error(e.getMessage());
		} finally {
			SQLiteLoader.closeConnection(conn);
			conn = null;
		}
		super.finishAtlasCreation();
	}

	protected void initializeDB() throws SQLException {
		Statement stat = conn.createStatement();
		String[] sqlList = NAVI_TABLES.split("\\n");
		for (String sql : sqlList)
			stat.addBatch(sql);
		stat.executeBatch();
		stat.close();
		log.debug("Database initialization complete: tables, trigges and index created");
	}

	protected void createTiles() throws InterruptedException, MapCreationException {
		int maxMapProgress = (xMax - xMin + 1) * (yMax - yMin + 1);
		atlasProgress.initMapCreation(maxMapProgress);
		try {
			tileCommitCounter = 0;
			conn.setAutoCommit(false);
			Runtime r = Runtime.getRuntime();

			for (int x = xMin; x <= xMax; x++) {
				for (int y = yMin; y <= yMax; y++) {
					checkUserAbort();
					atlasProgress.incMapCreationProgress();
					try {

						byte[] sourceTileData = mapDlTileProvider.getTileData(x, y);
						if (sourceTileData != null) {
							tileCommitCounter++;
							writeTile(wmsTileCount++, x, y, zoom, sourceTileData);

							long heapAvailable = r.maxMemory() - r.totalMemory() + r.freeMemory();
							if (heapAvailable < HEAP_MIN || tileCommitCounter > COMMIT_RATE) {
								conn.commit();
								tileCommitCounter = 0;
								System.gc();
							}
						}
					} catch (IOException e) {
						throw new MapCreationException(map, e);
					}
				}
			}
			conn.commit();
			atlasProgress.setMapCreationProgress(maxMapProgress);
		} catch (SQLException e) {
			throw new MapCreationException(map, e);
		}
	}

	protected void writeTile(int id, int x, int y, int z, byte[] tileData) throws SQLException, IOException {
		prepTiles.setInt(1, id);
		prepTiles.setInt(2, x);
		prepTiles.setInt(3, y);
		prepTiles.setInt(4, z);
		prepTiles.execute();
		prepTilesData.setInt(1, id);
		prepTilesData.setBytes(2, tileData);
		prepTilesData.execute();
	}

	protected String getDatabaseFileName() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HHmmss_");
		databaseFile = new File(atlasDir, sdf.format(new Date()) + atlas.getName() + ".nmap").getAbsolutePath();
		return databaseFile;
	}

}
