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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JPanel;

import mobac.exceptions.MapCreationException;
import mobac.gui.mapview.ScaleBar;
import mobac.gui.mapview.WgsGrid;
import mobac.mapsources.mapspace.MercatorPower2MapSpace;
import mobac.program.interfaces.LayerInterface;
import mobac.program.interfaces.MapSource;
import mobac.program.interfaces.MapSpace;
import mobac.program.interfaces.MapSpace.ProjectionCategory;
import mobac.program.model.Settings;
import mobac.program.model.SettingsPaperAtlas;
import mobac.program.model.SettingsWgsGrid;
import mobac.program.model.UnitSystem;
import mobac.utilities.Utilities;

public abstract class PaperAtlas extends AtlasCreator {

	private static final Font PAGE_NUMBER_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 24), LABEL_FONT = new Font(
			Font.SANS_SERIF, Font.BOLD, 12);

	private static final Color LABEL_BACKGROUND = new Color(0, 127, 0), LABEL_FOREGROUND = Color.WHITE,
			IMAGE_BACKGROUND = new Color(0, 0, 0, 0);

	private static final String UP = " \u2191", DOWN = " \u2193";

	protected static class Page {
		protected final BufferedImage image;
		protected final int number, width, height;

		protected Page(int number, BufferedImage image) {
			this.number = number;
			this.image = image;
			this.width = image.getWidth();
			this.height = image.getHeight();
		}
	}

	private final JComponent dummy = new JPanel();
	private final WgsGrid wgsGrid;
	private final int overlap;
	private final Insets insets = new Insets(0, 0, 0, 0);

	protected final SettingsPaperAtlas s;

	private File layerFolder;
	private Dimension base, bottom, right, corner;

	protected PaperAtlas(final boolean usePadding) {

		s = Settings.getInstance().paperAtlas.clone();
		overlap = (int) UnitSystem.pointsToPixels(s.overlap, s.dpi);

		if (s.wgsEnabled) {
			SettingsWgsGrid sWgsGrid = Settings.getInstance().wgsGrid.clone();
			sWgsGrid.enabled = s.wgsEnabled;
			sWgsGrid.density = s.wgsDensity;
			wgsGrid = new WgsGrid(sWgsGrid, dummy);
		} else {
			wgsGrid = null;
		}

		if (usePadding) {
			insets.top = (int) UnitSystem.pointsToPixels(s.marginTop, s.dpi);
			insets.left = (int) UnitSystem.pointsToPixels(s.marginLeft, s.dpi);
			insets.bottom = (int) UnitSystem.pointsToPixels(s.marginBottom, s.dpi);
			insets.right = (int) UnitSystem.pointsToPixels(s.marginTop, s.dpi);
		}

		if (s.paperSize != null) {
			double width = s.paperSize.width - s.marginLeft - s.marginRight;
			double height = s.paperSize.height - s.marginTop - s.marginBottom;
			width = UnitSystem.pointsToPixels(width, s.dpi);
			height = UnitSystem.pointsToPixels(height, s.dpi);
			base = new Dimension((int) width, (int) height);
		}
	}

	@Override
	public void createMap() throws MapCreationException, InterruptedException {
		corner = right = bottom = null;

		int mapWidth = (xMax - xMin + 1) * tileSize;
		int mapHeight = (yMax - yMin + 1) * tileSize;

		if (base == null) {
			base = new Dimension(mapWidth, mapHeight);
			processPages(1, 1);
			base = null;
		} else {

			int sWidth = (mapWidth) % (base.width - overlap);
			if ((double) sWidth / base.getWidth() * 100d < s.crop) {
				sWidth = 0;
			}

			int sHeight = (mapHeight) % (base.height - overlap);
			if ((double) sHeight / base.getHeight() * 100d < s.crop) {
				sHeight = 0;
			}

			if (sHeight > 0 && sWidth > 0) {
				corner = new Dimension(sWidth, sHeight);
			}
			if (sWidth > 0) {
				right = new Dimension(sWidth, base.height);
			}
			if (sHeight > 0) {
				bottom = new Dimension(base.width, sHeight);
			}
			int rows = (mapHeight) / (base.height - overlap) + (bottom != null ? 1 : 0);
			int cols = (mapWidth) / (base.width - overlap) + (right != null ? 1 : 0);
			processPages(rows, cols);
		}
	}

	protected File getLayerFolder() {
		return layerFolder;
	}

	public void initLayerCreation(LayerInterface layer) throws IOException {
		super.initLayerCreation(layer);
		layerFolder = new File(atlasDir, layer.getName());
		Utilities.mkDirs(layerFolder);
	}

	protected abstract void processPage(BufferedImage image, int pageNumber) throws MapCreationException;

	private void processPages(final int ROWS, final int COLS) throws MapCreationException, InterruptedException {
		try {

			atlasProgress.initMapCreation(ROWS * COLS * 2);
			for (int row = 0; row < ROWS; row++) {
				for (int col = 0; col < COLS; col++) {
					log.trace(String.format("cal=%d row=%d", col, row));

					// Choose image
					Dimension size;

					boolean firstRow = row == 0;
					boolean lastRow = row + 1 == ROWS;
					boolean lastCol = col + 1 == COLS;
					if (corner != null && lastRow && lastCol) {
						size = new Dimension(corner);
					} else if (bottom != null && lastRow) {
						size = new Dimension(bottom);
					} else if (right != null && lastCol) {
						size = new Dimension(right);
					} else {
						size = new Dimension(base);
					}

					// Compute values
					int pageXMin = col * base.width - col * overlap;
					int pageYMin = row * base.height - row * overlap;
					int firstTileX = pageXMin / tileSize + xMin;
					int firstTileY = pageYMin / tileSize + yMin;
					int firstTileXOffset = pageXMin % tileSize;
					int firstTileYOffset = pageYMin % tileSize;
					int tilesInCol = (size.height + firstTileYOffset - 1) / tileSize + 1;
					int tilesInRow = (size.width + firstTileXOffset - 1) / tileSize + 1;

					// Create image and graphics
					int imageWidth = size.width + insets.left + insets.right;
					int imageHeight = size.height + insets.top + insets.bottom;
					BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_4BYTE_ABGR);
					Graphics2D g = image.createGraphics();
					g.translate(insets.left, insets.top);
					g.clipRect(0, 0, size.width, size.height);
					// Paint image

					g.setBackground(IMAGE_BACKGROUND);
					g.clearRect(0, 0, size.width, size.height);
					g.translate(-firstTileXOffset, -firstTileYOffset);
					for (int tileRow = 0; tileRow < tilesInCol; tileRow++) {
						for (int tileCol = 0; tileCol < tilesInRow; tileCol++) {
							int tileX = firstTileX + tileCol;
							int tileY = firstTileY + tileRow;
							int x = tileCol * tileSize;
							int y = tileRow * tileSize;
							BufferedImage tile = mapDlTileProvider.getTileImage(tileX, tileY);
							if (tile != null)
								g.drawImage(tile, null, x, y);
						}
					}
					g.translate(firstTileXOffset, firstTileYOffset);

					// Paint additions
					dummy.setSize(size);
					Point tlc = new Point(firstTileX * tileSize + firstTileXOffset, firstTileY * tileSize
							+ firstTileYOffset);
					if (s.wgsEnabled)
						wgsGrid.paintWgsGrid(g, mapSource.getMapSpace(), tlc, zoom);
					if (s.scaleBar)
						ScaleBar.paintScaleBar(dummy, g, mapSource.getMapSpace(), tlc, zoom);
					if (s.compass) {
						Image compassRaw = ImageIO.read(Utilities.loadResourceAsStream("images/compass.png"));
						Image compass = compassRaw.getScaledInstance(150, 150, Image.SCALE_SMOOTH);
						g.drawImage(compass, 0, 0, null);
					}
					if (s.pageNumbers) {
						g.setBackground(LABEL_BACKGROUND);
						g.setColor(LABEL_FOREGROUND);

						String pageNumber = " " + getPageNumber(row, col, ROWS, COLS) + " ";
						g.setFont(PAGE_NUMBER_FONT);
						FontMetrics fontMetrics = g.getFontMetrics();
						int fontHeight = fontMetrics.getHeight();
						int pageNumberStringWidth = fontMetrics.stringWidth(pageNumber);
						g.clearRect(0, 0, pageNumberStringWidth, fontHeight);
						g.drawString(pageNumber, 0, fontHeight - fontMetrics.getDescent());

						int centerX = size.width / 2;
						g.setFont(LABEL_FONT);
						fontMetrics = g.getFontMetrics();
						fontHeight = fontMetrics.getHeight();

						if (!firstRow) {
							String text = UP + getPageNumber(row - 1, col, ROWS, COLS) + " ";
							int stringWidth = fontMetrics.stringWidth(text);
							g.clearRect(centerX - stringWidth / 2, 8, stringWidth, fontHeight);
							g.drawString(text, centerX - stringWidth / 2, 8 + fontHeight - fontMetrics.getDescent());
						}
						if (!lastRow) {
							String text = DOWN + getPageNumber(row + 1, col, ROWS, COLS) + " ";
							int stringWidth = fontMetrics.stringWidth(text);

							g.clearRect(centerX - stringWidth / 2, size.height - 32 - fontHeight, stringWidth,
									fontHeight);
							g.drawString(text, centerX - stringWidth / 2, size.height - 32 - fontMetrics.getDescent());
						}
					}
					g.dispose();

					// Process image
					atlasProgress.incMapCreationProgress();
					checkUserAbort();
					processPage(image, getPageNumber(row, col, ROWS, COLS));
					atlasProgress.incMapCreationProgress();
					checkUserAbort();
				}
			}
		} catch (IOException e) {
			throw new MapCreationException(map, e);
		}

	}

	private int getPageNumber(int row, int col, int ROWS, int COLS) {
		return row * COLS + col + 1;
	}

	@Override
	public boolean testMapSource(MapSource mapSource) {
		MapSpace mapSpace = mapSource.getMapSpace();
		ProjectionCategory projection = mapSpace.getProjectionCategory();
		return (mapSpace instanceof MercatorPower2MapSpace)
				&& (ProjectionCategory.SPHERE.equals(projection) || ProjectionCategory.ELLIPSOID.equals(projection));
	}

}
