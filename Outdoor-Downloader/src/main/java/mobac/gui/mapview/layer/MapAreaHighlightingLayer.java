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
package mobac.gui.mapview.layer;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.util.Iterator;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import mobac.gui.MainGUI;
import mobac.gui.atlastree.JAtlasTree;
import mobac.gui.mapview.JMapViewer;
import mobac.gui.mapview.PreviewMap;
import mobac.gui.mapview.interfaces.MapLayer;
import mobac.program.interfaces.AtlasInterface;
import mobac.program.interfaces.AtlasObject;
import mobac.program.interfaces.LayerInterface;
import mobac.program.interfaces.MapInterface;
import mobac.program.model.MapPolygon;


public class MapAreaHighlightingLayer implements MapLayer, TreeModelListener {

	private final JAtlasTree tree;

	private TreeSelectionListener treeListener;

	private AtlasObject object;

	public static void removeHighlightingLayers() {
		PreviewMap previewMap = MainGUI.getMainGUI().previewMap;
		Iterator<MapLayer> mapLayers = previewMap.mapLayers.iterator();
		MapLayer ml;
		while (mapLayers.hasNext()) {
			ml = mapLayers.next();
			if (ml instanceof MapAreaHighlightingLayer) {
				mapLayers.remove();
				((MapAreaHighlightingLayer) ml).unregisterTreeListener();
			}
		}
	}

	public MapAreaHighlightingLayer(JAtlasTree tree) {
		this.tree = tree;
		object = (AtlasObject) tree.getSelectionPath().getLastPathComponent();
		MainGUI.getMainGUI().previewMap.repaint();
		treeListener = new TreeSelectionListener() {

			public void valueChanged(TreeSelectionEvent event) {
				try {
					object = (AtlasObject) event.getNewLeadSelectionPath().getLastPathComponent();
				} catch (Exception e) {
					object = null;
				}
				MainGUI.getMainGUI().previewMap.repaint();
			}
		};
		tree.addTreeSelectionListener(treeListener);
		tree.getModel().addTreeModelListener(this);
	}

	public void paint(JMapViewer mapViewer, Graphics2D g, int zoom, int minX, int minY, int maxX,
			int maxY) {
		if (object == null)
			return;
		if (object instanceof AtlasInterface) {
			for (LayerInterface layer : (AtlasInterface) object) {
				for (MapInterface map : layer) {
					paintMap(map, g, zoom, minX, minY, maxX, maxY);
				}
			}
		} else if (object instanceof LayerInterface) {
			for (MapInterface map : (LayerInterface) object) {
				paintMap(map, g, zoom, minX, minY, maxX, maxY);
			}
		} else {
			paintMap((MapInterface) object, g, zoom, minX, minY, maxX, maxY);
		}
	}

	protected void paintMap(MapInterface map, Graphics2D g, int zoom, int minX, int minY, int maxX,
			int maxY) {
		if (map instanceof MapPolygon)
			paintMapPolygon((MapPolygon) map, g, zoom, minX, minY, maxX, maxY);
		else
			paintMapRectangle(map, g, zoom, minX, minY, maxX, maxY);
	}

	protected void paintMapRectangle(MapInterface map, Graphics2D g, int zoom, int minX, int minY,
			int maxX, int maxY) {
		Point max = map.getMaxTileCoordinate();
		Point min = map.getMinTileCoordinate();
		int zoomDiff = map.getZoom() - zoom;
		int mapX = applyZoomDiff(min.x, zoomDiff);
		int mapY = applyZoomDiff(min.y, zoomDiff);
		int mapW = applyZoomDiff(max.x - min.x + 1, zoomDiff);
		int mapH = applyZoomDiff(max.y - min.y + 1, zoomDiff);
		int x = mapX - minX;
		int y = mapY - minY;
		int w = mapW;
		int h = mapH;
		g.setColor(PreviewMap.MAP_COLOR);
		g.fillRect(x, y, w, h);
		g.setColor(PreviewMap.GRID_COLOR);
		g.drawRect(x, y, w, h);
	}

	protected void paintMapPolygon(MapPolygon map, Graphics2D g, int zoom, int minX, int minY,
			int maxX, int maxY) {
		Polygon p = map.getPolygon();
		int zoomDiff = map.getZoom() - zoom;

		int[] px = new int[p.npoints];
		int[] py = new int[p.npoints];
		for (int i = 0; i < px.length; i++) {
			px[i] = applyZoomDiff(p.xpoints[i], zoomDiff) - minX;
			py[i] = applyZoomDiff(p.ypoints[i], zoomDiff) - minY;
		}
		g.setColor(PreviewMap.MAP_COLOR);
		g.fillPolygon(px, py, px.length);
	}

	private static int applyZoomDiff(int pixelCoord, int zoomDiff) {
		return (zoomDiff > 0) ? pixelCoord >> zoomDiff : pixelCoord << -zoomDiff;
	}

	protected void unregisterTreeListener() {
		if (treeListener == null)
			return;
		try {
			tree.getModel().removeTreeModelListener(this);
			tree.removeTreeSelectionListener(treeListener);
			treeListener = null;
		} catch (Exception e) {
		}
	}

	@Override
	protected void finalize() throws Throwable {
		unregisterTreeListener();
		super.finalize();
	}

	public void treeNodesChanged(TreeModelEvent e) {
		MainGUI.getMainGUI().previewMap.repaint();
	}

	public void treeNodesInserted(TreeModelEvent e) {
		MainGUI.getMainGUI().previewMap.repaint();
	}

	public void treeNodesRemoved(TreeModelEvent e) {
		MainGUI.getMainGUI().previewMap.repaint();
	}

	public void treeStructureChanged(TreeModelEvent e) {
		MainGUI.getMainGUI().previewMap.repaint();
	}

	
}
