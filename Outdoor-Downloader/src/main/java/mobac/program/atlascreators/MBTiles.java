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
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumSet;
import java.util.Locale;

import mobac.exceptions.AtlasTestException;
import mobac.mapsources.mapspace.MercatorPower2MapSpace;
import mobac.program.annotations.AtlasCreatorName;
import mobac.program.interfaces.LayerInterface;
import mobac.program.interfaces.MapInterface;
import mobac.program.interfaces.MapSource;
import mobac.program.interfaces.MapSpace;
import mobac.program.model.TileImageParameters;
import mobac.program.model.TileImageType;

/**
 * https://github.com/mapbox/mbtiles-spec/tree/master/1.1
 */
@AtlasCreatorName(value = "MBTiles SQLite")
public class MBTiles extends RMapsSQLite {

	private static final String INSERT_SQL = "INSERT or REPLACE INTO tiles (tile_column,tile_row,zoom_level,tile_data) VALUES (?,?,?,?)";
	private static final String TABLE_TILES = "CREATE TABLE IF NOT EXISTS tiles (zoom_level integer, tile_column integer, tile_row integer, tile_data blob);";
	private static final String INDEX_TILES = "CREATE INDEX IF NOT EXISTS tiles_idx on tiles (zoom_level, tile_column, tile_row)";
	private static final String TABLE_METADATA = "CREATE TABLE IF NOT EXISTS metadata (name text, value text);";
	private static final String INSERT_METADATA = "INSERT INTO metadata (name,value) VALUES (?,?);";
	private static final String INDEX_METADATA = "CREATE UNIQUE INDEX IF NOT EXISTS metadata_idx  ON metadata (name);";

	private double boundsLatMin;
	private double boundsLatMax;
	private double boundsLonMin;
	private double boundsLonMax;

	private TileImageType atlasTileImageType;

	@Override
	public boolean testMapSource(MapSource mapSource) {
		return MercatorPower2MapSpace.INSTANCE_256.equals(mapSource.getMapSpace());
	}

	@Override
	protected void testAtlas() throws AtlasTestException {
		EnumSet<TileImageType> allowed = EnumSet.of(TileImageType.JPG, TileImageType.PNG);
		// Test of output format - only jpg xor png is allowed
		TileImageType tit = null;
		for (LayerInterface layer : atlas) {
			for (MapInterface map : layer) {
				TileImageParameters parameters = map.getParameters();
				TileImageType currentTit;
				if (parameters == null) {
					currentTit = map.getMapSource().getTileImageType();
					if (!allowed.contains(currentTit))
						throw new AtlasTestException(
								"Map source format incompatible - tile format conversion to PNG or JPG is required for this map.",
								map);
				} else {
					currentTit = parameters.getFormat().getType();
					if (!allowed.contains(currentTit))
						throw new AtlasTestException(
								"Selected custom tile format not supported - only JPG and PNG formats are supported.",
								map);
				}
				if (tit != null && !currentTit.equals(tit)) {
					throw new AtlasTestException("All maps within one atlas must use the same format (PNG or JPG). "
							+ "Use tile format conversion converting maps with a different format.", map);
				}
				tit = currentTit;
			}
		}
		atlasTileImageType = tit;
	}

	@Override
	protected void openConnection() throws SQLException {
		if (databaseFile.isFile()) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
			databaseFile = new File(atlasDir, atlas.getName() + "_" + sdf.format(new Date()) + ".mbtiles");
		}
		super.openConnection();
	}

	@Override
	protected void initializeDB() throws SQLException {
		Statement stat = conn.createStatement();
		stat.executeUpdate(TABLE_TILES);
		stat.executeUpdate(INDEX_TILES);
		stat.executeUpdate(TABLE_METADATA);
		stat.executeUpdate(INDEX_METADATA);
		stat.close();
		boundsLatMin = Double.MAX_VALUE;
		boundsLatMax = Double.MIN_VALUE;
		boundsLonMin = Double.MAX_VALUE;
		boundsLonMax = Double.MIN_VALUE;
	}

	@Override
	protected void updateTileMetaInfo() throws SQLException {
		MapSpace ms = map.getMapSource().getMapSpace();
		double lon1 = ms.cXToLon(map.getMinTileCoordinate().x, zoom);
		double lon2 = ms.cXToLon(map.getMaxTileCoordinate().x, zoom);
		double lat1 = ms.cYToLat(map.getMinTileCoordinate().y, zoom);
		double lat2 = ms.cYToLat(map.getMaxTileCoordinate().y, zoom);

		boundsLatMin = Math.min(boundsLatMin, Math.min(lat1, lat2));
		boundsLatMax = Math.max(boundsLatMax, Math.max(lat1, lat2));
		boundsLonMin = Math.min(boundsLonMin, Math.min(lon1, lon2));
		boundsLonMax = Math.max(boundsLonMin, Math.max(lon1, lon2));
	}

	@Override
	public void finishAtlasCreation() throws IOException, InterruptedException {
		PreparedStatement st;
		try {
			st = conn.prepareStatement(INSERT_METADATA);
			st.setString(1, "bounds");
			st.setString(2, String.format(Locale.ENGLISH, "%.3f,%.3f,%.3f,%.3f", boundsLonMin, boundsLatMin,
					boundsLonMax, boundsLatMax));
			st.execute();
			st.setString(1, "name");
			st.setString(2, atlas.getName());
			st.execute();
			st.setString(1, "type");
			st.setString(2, "baselayer");
			st.execute();
			st.setString(1, "version");
			st.setString(2, "1.1");
			st.execute();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			st.setString(1, "description");
			st.setString(2, atlas.getName() + " created on " + sdf.format(new Date()) + " by MOBAC");
			st.execute();
			st.setString(1, "format");
			st.setString(2, atlasTileImageType.getFileExt());
			st.execute();
			st.close();
			conn.commit();
		} catch (SQLException e) {
			throw new IOException(e);
		}
		super.finishAtlasCreation();
	}

	@Override
	protected String getTileInsertSQL() {
		return INSERT_SQL;
	}

	@Override
	protected void writeTile(int x, int y, int z, byte[] tileData) throws SQLException, IOException {
		y = (1 << z) - y - 1;
		prepStmt.setInt(1, x);
		prepStmt.setInt(2, y);
		prepStmt.setInt(3, z);
		prepStmt.setBytes(4, tileData);
		prepStmt.addBatch();
	}

	protected String getDatabaseFileName() {
		return atlas.getName() + ".mbtiles";
	}

}
