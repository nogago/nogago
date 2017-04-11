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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

import javax.imageio.ImageIO;

import mobac.exceptions.MapCreationException;
import mobac.program.annotations.AtlasCreatorName;
import mobac.utilities.Utilities;

@AtlasCreatorName(value = "Paper Atlas (PNG)")
public class PaperAtlasPng extends PaperAtlas {

	private final DecimalFormat decimalFormat = new DecimalFormat("#000");

	private File mapFolder;

	public PaperAtlasPng() {
		super(true);
	}

	@Override
	public void createMap() throws MapCreationException, InterruptedException {
		mapFolder = new File(getLayerFolder(), map.getName());
		try {
			Utilities.mkDirs(mapFolder);
		} catch (IOException e) {
			throw new MapCreationException(map, e);
		}
		super.createMap();
		mapFolder = null;
	}

	@Override
	protected void processPage(BufferedImage image, int pageNumber) throws MapCreationException {
		String fileName = decimalFormat.format(pageNumber) + ".png";
		File file = new File(mapFolder, fileName);
		try {
			ImageIO.write(image, "PNG", file);
		} catch (IOException e) {
			throw new MapCreationException(map, e);
		}
	}
}
