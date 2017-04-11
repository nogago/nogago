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
package mobac.gui.mapview;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.LinkedList;

import mobac.gui.mapview.controller.DefaultMapController;
import mobac.gui.mapview.controller.JMapController;
import mobac.gui.mapview.controller.MapKeyboardController;
import mobac.gui.mapview.controller.RectangleSelectionMapController;
import mobac.gui.mapview.interfaces.MapEventListener;
import mobac.mapsources.MapSourcesManager;
import mobac.program.interfaces.MapSource;
import mobac.program.interfaces.MapSourceTextAttribution;
import mobac.program.interfaces.MapSpace;
import mobac.program.model.Bookmark;
import mobac.program.model.EastNorthCoordinate;
import mobac.program.model.MapSelection;
import mobac.program.model.MercatorPixelCoordinate;
import mobac.program.model.Settings;
import mobac.utilities.MyMath;

import org.apache.log4j.Logger;

public class PreviewMap extends JMapViewer {

	private static final long serialVersionUID = 1L;

	public static final Color GRID_COLOR = new Color(200, 20, 20, 130);
	public static final Color SEL_COLOR = new Color(0.9f, 0.7f, 0.7f, 0.6f);
	public static final Color MAP_COLOR = new Color(1.0f, 0.84f, 0.0f, 0.4f);

	public static final int MAP_CONTROLLER_RECTANGLE_SELECT = 0;
	public static final int MAP_CONTROLLER_GPX = 1;

	protected static final Font LOADING_FONT = new Font("Sans Serif", Font.BOLD, 30);

	private static Logger log = Logger.getLogger(PreviewMap.class);

	/**
	 * Interactive map selection max/min pixel coordinates regarding zoom level <code>MAX_ZOOM</code>
	 */
	private Point iSelectionMin;
	private Point iSelectionMax;

	/**
	 * Map selection max/min pixel coordinates regarding zoom level <code>MAX_ZOOM</code> with respect to the grid zoom.
	 */
	private Point gridSelectionStart;
	private Point gridSelectionEnd;

	/**
	 * Pre-painted transparent tile with grid lines on it. This makes painting the grid a lot faster in difference to
	 * painting each line or rectangle if the grid zoom is much higher that the current zoom level.
	 */
	private BufferedImage gridTile = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);

	private int gridZoom = -1;
	private int gridSize;

	protected LinkedList<MapEventListener> mapEventListeners = new LinkedList<MapEventListener>();

	protected JMapController mapKeyboardController;
	protected JMapController mapSelectionController;

	private final WgsGrid wgsGrid = new WgsGrid(Settings.getInstance().wgsGrid, this);

	public PreviewMap() {
		super(MapSourcesManager.getInstance().getDefaultMapSource(), 5);
		setEnabled(false);
		new DefaultMapController(this);
		mapMarkersVisible = false;
		setZoomContolsVisible(false);

		mapKeyboardController = new MapKeyboardController(this, true);
		setMapSelectionController(new RectangleSelectionMapController(this));
	}

	public void setDisplayPositionByLatLon(EastNorthCoordinate c, int zoom) {
		setDisplayPositionByLatLon(new Point(getWidth() / 2, getHeight() / 2), c.lat, c.lon, zoom);
	}

	/**
	 * Updates the current position in {@link Settings} to the current view
	 */
	public void settingsSave() {
		Settings settings = Settings.getInstance();
		settings.mapviewZoom = getZoom();
		settings.mapviewCenterCoordinate = getCenterCoordinate();
		settings.mapviewGridZoom = gridZoom;
		settings.mapviewMapSource = mapSource.getName();
		settings.mapviewSelectionMin = iSelectionMin;
		settings.mapviewSelectionMax = iSelectionMax;
	}

	/**
	 * Sets the current view by the current values from {@link Settings}
	 */
	public void settingsLoad() {
		Settings settings = Settings.getInstance();
		MapSource mapSource = MapSourcesManager.getInstance().getSourceByName(settings.mapviewMapSource);
		if (mapSource != null)
			setMapSource(mapSource);
		EastNorthCoordinate c = settings.mapviewCenterCoordinate;
		gridZoom = settings.mapviewGridZoom;
		setDisplayPositionByLatLon(c, settings.mapviewZoom);
		setSelectionByTileCoordinate(MAX_ZOOM, settings.mapviewSelectionMin, settings.mapviewSelectionMax, true);
	}

	@Override
	public void setMapSource(MapSource newMapSource) {
		if (newMapSource.equals(mapSource))
			return;
		log.trace("Preview map source changed from " + mapSource + " to " + newMapSource);
		super.setMapSource(newMapSource);
		if (mapEventListeners == null)
			return;
		for (MapEventListener listener : mapEventListeners)
			listener.mapSourceChanged(mapSource);
	}

	protected void zoomChanged(int oldZoom) {
		log.trace("Preview map zoom changed from " + oldZoom + " to " + zoom);
		if (mapEventListeners != null)
			for (MapEventListener listener : mapEventListeners)
				listener.zoomChanged(zoom);
		updateGridValues();
	}

	public void setGridZoom(int gridZoom) {
		if (gridZoom == this.gridZoom)
			return;
		this.gridZoom = gridZoom;
		updateGridValues();
		applyGridOnSelection();
		updateMapSelection();
		repaint();
	}

	public int getGridZoom() {
		return gridZoom;
	}

	/**
	 * Updates the <code>gridSize</code> and the <code>gridTile</code>. This method has to called if
	 * <code>mapSource</code> or <code>zoom</code> as been changed.
	 */
	protected void updateGridValues() {
		if (gridZoom < 0)
			return;
		int zoomToGridZoom = zoom - gridZoom;
		int tileSize = mapSource.getMapSpace().getTileSize();
		if (zoomToGridZoom > 0) {
			gridSize = tileSize << zoomToGridZoom;
			gridTile = null;
		} else {
			gridSize = tileSize >> (-zoomToGridZoom);
			BufferedImage newGridTile = null;
			if (gridSize > 2) {
				newGridTile = new BufferedImage(tileSize, tileSize, BufferedImage.TYPE_INT_ARGB);
				Graphics2D g = newGridTile.createGraphics();
				int alpha = 5 + (6 + zoomToGridZoom) * 16;
				alpha = Math.max(0, alpha);
				alpha = Math.min(130, alpha);
				g.setColor(new Color(200, 20, 20, alpha));
				for (int x = 0; x < tileSize; x += gridSize)
					g.drawLine(x, 0, x, 255);
				for (int y = 0; y < tileSize; y += gridSize)
					g.drawLine(0, y, 255, y);
			}
			gridTile = newGridTile;
		}
	}

	@Override
	protected void paintComponent(Graphics graphics) {
		if (!isEnabled()) {
			graphics.setFont(LOADING_FONT);
			graphics.drawString("Please wait - loading map data", 100, 100);
			return;
		}
		if (mapSource == null)
			return;
		Graphics2D g = (Graphics2D) graphics;
		super.paintComponent(g);

		Point tlc = getTopLeftCoordinate();
		if (gridZoom >= 0) {
			// Only paint grid if it is enabled (gridZoom not -1)
			int max = (256 << zoom);
			int w = Math.min(getWidth(), max - tlc.x);
			int h = Math.min(getHeight(), max - tlc.y);
			g.setColor(GRID_COLOR);
			// g.setStroke(new BasicStroke(4.0f));
			if (gridSize > 1) {
				int tilesize = mapSource.getMapSpace().getTileSize();
				if (gridSize >= tilesize) {
					int off_x = tlc.x < 0 ? -tlc.x : -(tlc.x % gridSize);
					int off_y = tlc.y < 0 ? -tlc.y : -(tlc.y % gridSize);
					for (int x = off_x; x <= w; x += gridSize) {
						g.drawLine(x, off_y, x, h);
					}
					for (int y = off_y; y <= h; y += gridSize) {
						g.drawLine(off_x, y, w, y);
					}
				} else {
					int off_x = (tlc.x < 0) ? tlc.x : tlc.x % tilesize;
					int off_y = (tlc.y < 0) ? tlc.y : tlc.y % tilesize;
					for (int x = -off_x; x < w; x += 256) {
						for (int y = -off_y; y < h; y += 256) {
							g.drawImage(gridTile, x, y, null);
						}
					}
				}
			}
		}
		if (gridSelectionStart != null && gridSelectionEnd != null) {
			// Draw the selection rectangle widened by the current grid
			int zoomDiff = MAX_ZOOM - zoom;
			int x_min = (gridSelectionStart.x >> zoomDiff) - tlc.x;
			int y_min = (gridSelectionStart.y >> zoomDiff) - tlc.y;
			int x_max = (gridSelectionEnd.x >> zoomDiff) - tlc.x;
			int y_max = (gridSelectionEnd.y >> zoomDiff) - tlc.y;

			int w = x_max - x_min + 1;
			int h = y_max - y_min + 1;
			g.setColor(SEL_COLOR);
			g.fillRect(x_min, y_min, w, h);
		}
		if (iSelectionMin != null && iSelectionMax != null) {
			// Draw the selection rectangle exactly as it has been specified by the user
			int zoomDiff = MAX_ZOOM - zoom;
			int x_min = (iSelectionMin.x >> zoomDiff) - tlc.x;
			int y_min = (iSelectionMin.y >> zoomDiff) - tlc.y;
			int x_max = (iSelectionMax.x >> zoomDiff) - tlc.x;
			int y_max = (iSelectionMax.y >> zoomDiff) - tlc.y;

			int w = x_max - x_min + 1;
			int h = y_max - y_min + 1;
			g.setColor(GRID_COLOR);
			g.drawRect(x_min, y_min, w, h);
		}
		if (mapSource instanceof MapSourceTextAttribution) {
			MapSourceTextAttribution ta = (MapSourceTextAttribution) mapSource;
			String attributionText = ta.getAttributionText();
			if (attributionText != null) {
				Rectangle2D stringBounds = g.getFontMetrics().getStringBounds(attributionText, g);
				int text_x = getWidth() - 10 - (int) stringBounds.getWidth();
				int text_y = getHeight() - 1 - (int) stringBounds.getHeight();
				g.setColor(Color.black);
				g.drawString(attributionText, text_x + 1, text_y + 1);
				g.setColor(Color.white);
				g.drawString(attributionText, text_x, text_y);
			}
		}
		if (Settings.getInstance().wgsGrid.enabled) {
			wgsGrid.paintWgsGrid(g, mapSource.getMapSpace(), tlc, zoom);
		}
		ScaleBar.paintScaleBar(this, g, mapSource.getMapSpace(), tlc, zoom);
	}

	public Bookmark getPositionBookmark() {
		return new Bookmark(mapSource, zoom, center.x, center.y);
	}

	public void gotoPositionBookmark(Bookmark bookmark) {
		setMapSource(bookmark.getMapSource());
		setDisplayPositionByLatLon(bookmark, bookmark.getZoom());
		setZoom(bookmark.getZoom());
	}

	/**
	 * @return Coordinate of the point in the center of the currently displayed map region
	 */
	public EastNorthCoordinate getCenterCoordinate() {
		MapSpace mapSpace = mapSource.getMapSpace();
		double lon = mapSpace.cXToLon(center.x, zoom);
		double lat = mapSpace.cYToLat(center.y, zoom);
		return new EastNorthCoordinate(lat, lon);
	}

	/**
	 * @return Coordinate of the top left corner visible regarding the current map source (pixel)
	 */
	public Point getTopLeftCoordinate() {
		return new Point(center.x - (getWidth() / 2), center.y - (getHeight() / 2));
	}

	public void zoomTo(MapSelection ms) {
		if (!ms.isAreaSelected())
			return;
		log.trace("Setting selection to: " + ms);
		Point max = ms.getBottomRightPixelCoordinate(MAX_ZOOM);
		Point min = ms.getTopLeftPixelCoordinate(MAX_ZOOM);
		setDisplayToFitPixelCoordinates(max.x, max.y, min.x, min.y);
	}

	/**
	 * Zooms to the specified {@link MapSelection} and sets the selection to it;
	 * 
	 * @param ms
	 * @param notifyListeners
	 */
	public void setSelectionAndZoomTo(MapSelection ms, boolean notifyListeners) {
		if (!ms.isAreaSelected())
			return;
		log.trace("Setting selection to: " + ms);
		Point max = ms.getBottomRightPixelCoordinate(MAX_ZOOM);
		Point min = ms.getTopLeftPixelCoordinate(MAX_ZOOM);
		setDisplayToFitPixelCoordinates(max.x, max.y, min.x, min.y);
		Point pStart = ms.getTopLeftPixelCoordinate(zoom);
		Point pEnd = ms.getBottomRightPixelCoordinate(zoom);
		setSelectionByTileCoordinate(pStart, pEnd, notifyListeners);
	}

	/**
	 * 
	 * @param pStart
	 *            x/y tile coordinate of the top left tile regarding the current zoom level
	 * @param pEnd
	 *            x/y tile coordinate of the bottom right tile regarding the current zoom level
	 * @param notifyListeners
	 */
	public void setSelectionByTileCoordinate(Point pStart, Point pEnd, boolean notifyListeners) {
		setSelectionByTileCoordinate(zoom, pStart, pEnd, notifyListeners);
	}

	/**
	 * Sets the rectangular selection to the absolute tile coordinates <code>pStart</code> and <code>pEnd</code>
	 * regarding the zoom-level <code>cZoom</code>.
	 * 
	 * @param cZoom
	 * @param pStart
	 * @param pEnd
	 * @param notifyListeners
	 */
	public void setSelectionByTileCoordinate(int cZoom, Point pStart, Point pEnd, boolean notifyListeners) {
		if (pStart == null || pEnd == null) {
			iSelectionMin = null;
			iSelectionMax = null;
			gridSelectionStart = null;
			gridSelectionEnd = null;
			return;
		}

		Point pNewStart = new Point();
		Point pNewEnd = new Point();
		int mapMaxCoordinate = mapSource.getMapSpace().getMaxPixels(cZoom) - 1;
		// Sort x/y coordinate of points so that pNewStart < pnewEnd and limit selection to map size
		pNewStart.x = Math.max(0, Math.min(mapMaxCoordinate, Math.min(pStart.x, pEnd.x)));
		pNewStart.y = Math.max(0, Math.min(mapMaxCoordinate, Math.min(pStart.y, pEnd.y)));
		pNewEnd.x = Math.max(0, Math.min(mapMaxCoordinate, Math.max(pStart.x, pEnd.x)));
		pNewEnd.y = Math.max(0, Math.min(mapMaxCoordinate, Math.max(pStart.y, pEnd.y)));

		int zoomDiff = MAX_ZOOM - cZoom;

		pNewEnd.x <<= zoomDiff;
		pNewEnd.y <<= zoomDiff;
		pNewStart.x <<= zoomDiff;
		pNewStart.y <<= zoomDiff;

		iSelectionMin = pNewStart;
		iSelectionMax = pNewEnd;
		gridSelectionStart = null;
		gridSelectionEnd = null;

		updateGridValues();
		applyGridOnSelection();

		if (notifyListeners)
			updateMapSelection();
		repaint();
	}

	protected void applyGridOnSelection() {
		if (gridZoom < 0) {
			gridSelectionStart = iSelectionMin;
			gridSelectionEnd = iSelectionMax;
			return;
		}

		if (iSelectionMin == null || iSelectionMax == null)
			return;

		int gridZoomDiff = MAX_ZOOM - gridZoom;
		int gridFactor = mapSource.getMapSpace().getTileSize() << gridZoomDiff;

		Point pNewStart = new Point(iSelectionMin);
		Point pNewEnd = new Point(iSelectionMax);

		// Snap to the current grid

		pNewStart.x = MyMath.roundDownToNearest(pNewStart.x, gridFactor);
		pNewStart.y = MyMath.roundDownToNearest(pNewStart.y, gridFactor);
		pNewEnd.x = MyMath.roundUpToNearest(pNewEnd.x, gridFactor) - 1;
		pNewEnd.y = MyMath.roundUpToNearest(pNewEnd.y, gridFactor) - 1;

		gridSelectionStart = pNewStart;
		gridSelectionEnd = pNewEnd;
	}

	/**
	 * Notifies all registered {@link MapEventListener} of a
	 * {@link MapEventListener#selectionChanged(MercatorPixelCoordinate, MercatorPixelCoordinate)} event.
	 */
	public void updateMapSelection() {
		int x_min, y_min, x_max, y_max;

		if (gridZoom >= 0) {
			if (gridSelectionStart == null || gridSelectionEnd == null)
				return;
			x_min = gridSelectionStart.x;
			y_min = gridSelectionStart.y;
			x_max = gridSelectionEnd.x;
			y_max = gridSelectionEnd.y;
		} else {
			if (iSelectionMin == null || iSelectionMax == null)
				return;
			x_min = iSelectionMin.x;
			y_min = iSelectionMin.y;
			x_max = iSelectionMax.x;
			y_max = iSelectionMax.y;
		}
		MercatorPixelCoordinate min = new MercatorPixelCoordinate(mapSource.getMapSpace(), x_min, y_min, MAX_ZOOM);
		MercatorPixelCoordinate max = new MercatorPixelCoordinate(mapSource.getMapSpace(), x_max, y_max, MAX_ZOOM);
		// log.debug("sel min: [" + min + "]");
		// log.debug("sel max: [" + max + "]");
		for (MapEventListener listener : mapEventListeners)
			listener.selectionChanged(max, min);
	}

	public void addMapEventListener(MapEventListener l) {
		mapEventListeners.add(l);
	}

	public void selectPreviousMap() {
		for (MapEventListener listener : mapEventListeners) {
			listener.selectPreviousMapSource();
		}
	}

	public void selectNextMap() {
		for (MapEventListener listener : mapEventListeners) {
			listener.selectNextMapSource();
		}
	}

	/**
	 * Clears the in-memory tile cache and performs a repaint which causes a reload of all displayed tiles (from disk or
	 * if not present from the map source via network).
	 */
	public void refreshMap() {
		tileCache.clear();
		repaint();
	}

	public JMapController getMapKeyboardController() {
		return mapKeyboardController;
	}

	/**
	 * @return Currently active <code>mapSelectionController</code>
	 */
	public JMapController getMapSelectionController() {
		return mapSelectionController;
	}

	/**
	 * Sets a new mapSelectionController. Previous controller are disabled and removed.
	 * 
	 * @param mapSelectionController
	 */
	public void setMapSelectionController(JMapController mapSelectionController) {
		if (this.mapSelectionController != null)
			this.mapSelectionController.disable();
		this.mapSelectionController = mapSelectionController;
		mapSelectionController.enable();
		for (MapEventListener listener : mapEventListeners) {
			listener.mapSelectionControllerChanged(mapSelectionController);
		}
		repaint();
	}

}
