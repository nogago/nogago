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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import mobac.program.annotations.AtlasCreatorName;
import mobac.program.atlascreators.impl.MapTileWriter;
import mobac.utilities.Utilities;
import mobac.utilities.tar.TarArchive;
import mobac.utilities.tar.TarTmiArchive;

@AtlasCreatorName(value = "TrekBuddy tared atlas", type = "TaredAtlas")
public class TrekBuddyTared extends TrekBuddy {

	@Override
	public void finishAtlasCreation() {
		createAtlasTarArchive("cr");
	}

	private void createAtlasTarArchive(String name) {
		log.trace("Creating cr.tar for atlas in dir \"" + atlasDir.getPath() + "\"");

		File[] atlasLayerDirs = Utilities.listSubDirectories(atlasDir);
		List<File> atlasMapDirs = new LinkedList<File>();
		for (File dir : atlasLayerDirs)
			Utilities.addSubDirectories(atlasMapDirs, dir, 0);

		TarArchive ta = null;
		File crFile = new File(atlasDir, name + ".tar");
		try {
			ta = new TarArchive(crFile, atlasDir);

			ta.writeFileFromData(name + ".tba", "Atlas 1.0\r\n".getBytes());

			for (File mapDir : atlasMapDirs) {
				ta.writeFile(mapDir);
				File mapFile = new File(mapDir, mapDir.getName() + ".map");
				ta.writeFile(mapFile);
				try {
					mapFile.delete();
				} catch (Exception e) {
				}
			}
			ta.writeEndofArchive();
		} catch (IOException e) {
			log.error("Failed writing tar file \"" + crFile.getPath() + "\"", e);
		} finally {
			if (ta != null)
				ta.close();
		}
	}

	@Override
	protected MapTileWriter createMapTileWriter() throws IOException {
		return new TarTileWriter();
	}

	private class TarTileWriter implements MapTileWriter {

		TarArchive ta = null;
		int tileHeight = 256;
		int tileWidth = 256;

		public TarTileWriter() {
			super();
			if (parameters != null) {
				tileHeight = parameters.getHeight();
				tileWidth = parameters.getWidth();
			}
			File mapTarFile = new File(mapDir, map.getName() + ".tar");
			log.debug("Writing tiles to tared map: " + mapTarFile);
			try {
				ta = new TarTmiArchive(mapTarFile, null);
				ByteArrayOutputStream buf = new ByteArrayOutputStream(8192);
				writeMapFile(buf);
				ta.writeFileFromData(map.getName() + ".map", buf.toByteArray());
			} catch (IOException e) {
				log.error("", e);
			}
		}

		public void writeTile(int tilex, int tiley, String imageFormat, byte[] tileData) throws IOException {
			String tileFileName = String.format(FILENAME_PATTERN, (tilex * tileWidth), (tiley * tileHeight),
					imageFormat);

			ta.writeFileFromData("set/" + tileFileName, tileData);
		}

		public void finalizeMap() {
			try {
				ta.writeEndofArchive();
			} catch (IOException e) {
				log.error("", e);
			}
			ta.close();
		}

	}

}
