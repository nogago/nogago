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
package mobac.gui.atlastree;

import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.dnd.Autoscroll;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import mobac.gui.MainGUI;
import mobac.gui.mapview.PreviewMap;
import mobac.gui.mapview.layer.MapAreaHighlightingLayer;
import mobac.program.interfaces.AtlasInterface;
import mobac.program.interfaces.AtlasObject;
import mobac.program.interfaces.CapabilityDeletable;
import mobac.program.interfaces.LayerInterface;
import mobac.program.interfaces.MapInterface;
import mobac.program.interfaces.RequiresSQLite;
import mobac.program.interfaces.ToolTipProvider;
import mobac.program.model.Atlas;
import mobac.program.model.AtlasOutputFormat;
import mobac.program.model.AtlasTreeModel;
import mobac.program.model.EastNorthCoordinate;
import mobac.program.model.MapSelection;
import mobac.program.model.Profile;
import mobac.program.model.TileImageParameters;
import mobac.utilities.GUIExceptionHandler;
import mobac.utilities.jdbc.SQLiteLoader;

import org.apache.log4j.Logger;

public class JAtlasTree extends JTree implements Autoscroll {

	private static final long serialVersionUID = 1L;
	private static final int margin = 12;

	private static final String MSG_ATLAS_VERSION_MISMATCH = ""
			+ "The loaded atlas belongs to an older version Mobile Atlas Creator. "
			+ "This old version \nused a somehow different atlas profile format "
			+ "which is incompatible to this version.\n\n"
			+ "It is recommended to clear the loaded atlas and delete the affected profile.\n"
			+ "Otherwise various exceptions may be thrown while working with this atlas.";

	private static final String MSG_ATLAS_DATA_CHECK_FAILED = ""
			+ "At least one problem was detected while loading the saved atlas profile.\n"
			+ "Usually this indicates that the profile file is inconsistent " + "or the file format \n"
			+ "has changed.\n\n" + "It is recommended to clear the loaded atlas and delete the affected profile.\n"
			+ "Otherwise various exceptions may be thrown while working with this atlas.";

	private static final String MSG_ATLAS_EMPTY = "Atlas is empty - "
			+ "please add at least one selection to atlas content.";

	private static final String ACTION_DELETE_NODE = "DELETE_NODE";

	private static final Logger log = Logger.getLogger(JAtlasTree.class);

	private AtlasTreeModel treeModel;

	private PreviewMap mapView;

	protected NodeRenderer nodeRenderer;

	protected String defaultToolTiptext;

	protected KeyStroke deleteNodeKS;

	protected DragDropController ddc;

	protected boolean displaySelectedMapArea = false;

	public JAtlasTree(PreviewMap mapView) {
		super(new AtlasTreeModel());
		if (mapView == null)
			throw new NullPointerException("MapView parameter is null");
		this.mapView = mapView;
		getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		ddc = new DragDropController(this);
		treeModel = (AtlasTreeModel) getModel();
		// setRootVisible(false);
		setShowsRootHandles(true);
		nodeRenderer = new NodeRenderer();
		setCellRenderer(nodeRenderer);
		setCellEditor(new NodeEditor(this));
		setToolTipText("");
		defaultToolTiptext = "<html>Use context menu of the entries to see all available commands.</html>";
		setAutoscrolls(true);
		addMouseListener(new MouseController(this));

		InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		ActionMap actionMap = getActionMap();

		// map moving
		inputMap.put(deleteNodeKS = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), ACTION_DELETE_NODE);
		actionMap.put(ACTION_DELETE_NODE, new AbstractAction("Delete") {

			public void actionPerformed(ActionEvent e) {
				deleteSelectedNode();
				JAtlasTree.this.mapView.repaint();
			}

		});

	}

	public boolean testAtlasContentValid() {
		AtlasInterface atlas = getAtlas();
		if (RequiresSQLite.class.isAssignableFrom(atlas.getOutputFormat().getMapCreatorClass())) {
			if (!SQLiteLoader.loadSQLiteOrShowError())
				return false;
		}
		if (atlas.calculateTilesToDownload() == 0) {
			JOptionPane.showMessageDialog(null, "<html>" + MSG_ATLAS_EMPTY + "</html>", "Error - atlas has no content",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}

	@Override
	public String getToolTipText(MouseEvent event) {
		if (getRowForLocation(event.getX(), event.getY()) == -1)
			return defaultToolTiptext;
		TreePath curPath = getPathForLocation(event.getX(), event.getY());
		Object o = curPath.getLastPathComponent();
		if (o == null || !(o instanceof ToolTipProvider))
			return null;
		return ((ToolTipProvider) o).getToolTip();
	}

	@Override
	public boolean isPathEditable(TreePath path) {
		return super.isPathEditable(path) && (path.getLastPathComponent() instanceof AtlasObject);
	}

	public AtlasTreeModel getTreeModel() {
		return treeModel;
	}

	public void newAtlas(String name, AtlasOutputFormat format) {
		log.debug("Creating new atlas");
		Atlas newAtlas = Atlas.newInstance();
		newAtlas.setOutputFormat(format);
		newAtlas.setName(name);
		treeModel.setAtlas(newAtlas);
		mapView.repaint();
	}

	public void newAtlas() {
		log.debug("Resetting atlas tree model");
		Atlas newAtlas = Atlas.newInstance();
		newAtlas.setName(MainGUI.getMainGUI().getUserText());
		treeModel.setAtlas(newAtlas);
		mapView.repaint();
	}

	/**
	 * Changes the atlas format
	 */
	public void convertAtlas(AtlasOutputFormat format) {
		log.debug("Converting the atlas format to " + format);
		treeModel.getAtlas().setOutputFormat(format);
	}

	public void deleteSelectedNode() {
		TreePath path = getSelectionPath();
		if (path == null)
			return;
		TreeNode selected = (TreeNode) path.getLastPathComponent();
		int[] selectedRows = getSelectionRows();

		if (!(selected instanceof CapabilityDeletable))
			return;
		treeModel.notifyNodeDelete(selected);
		((CapabilityDeletable) selected).delete();

		int selRow = Math.min(selectedRows[0], getRowCount() - 1);
		TreePath path1 = path.getParentPath();
		TreePath path2 = getPathForRow(selRow).getParentPath();
		if (path1 != path2) {
			// next row belongs to different parent node -> we select parent
			// node instead
			setSelectionPath(path1);
		} else {
			setSelectionRow(selRow);
			scrollRowToVisible(selRow);
		}
	}

	public AtlasInterface getAtlas() {
		return treeModel.getAtlas();
	}

	public boolean load(Profile profile) {
		log.debug("Loading profile " + profile);
		try {
			treeModel.load(profile);
			if (treeModel.getAtlas() instanceof Atlas) {
				Atlas atlas = (Atlas) treeModel.getAtlas();
				if (atlas.getVersion() < Atlas.CURRENT_ATLAS_VERSION) {
					JOptionPane.showMessageDialog(null, MSG_ATLAS_VERSION_MISMATCH, "Outdated atlas version",
							JOptionPane.WARNING_MESSAGE);
					return true;
				}
			}
			boolean problemsDetected = Profile.checkAtlas(treeModel.getAtlas());
			if (problemsDetected) {
				JOptionPane.showMessageDialog(null, MSG_ATLAS_DATA_CHECK_FAILED, "Atlas loading problem",
						JOptionPane.WARNING_MESSAGE);
			}
			return true;
		} catch (Exception e) {
			GUIExceptionHandler.processException(e);
			return false;
		}
	}

	public boolean save(Profile profile) {
		try {
			treeModel.save(profile);
			return true;
		} catch (Exception e) {
			GUIExceptionHandler.processException(e);
			return false;
		}
	}

	protected void showNodePopupMenu(MouseEvent event) {
		JPopupMenu pm = new JPopupMenu();
		final TreePath selPath = getPathForLocation(event.getX(), event.getY());
		setSelectionPath(selPath);
		JMenuItem mi = null;
		if (selPath != null) {
			// not clicked on empty area
			final Object o = selPath.getLastPathComponent();
			if (o == null)
				return;
			if (o instanceof ToolTipProvider) {
				mi = new JMenuItem("Show item details");
				mi.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						ToolTipProvider ttp = (ToolTipProvider) o;
						JOptionPane.showMessageDialog(MainGUI.getMainGUI(), ttp.getToolTip());
					}
				});
				pm.add(mi);
			}
			if (o instanceof AtlasObject) {
				final JCheckBoxMenuItem cbmi = new JCheckBoxMenuItem("Display selected areas");
				final MapAreaHighlightingLayer msl = new MapAreaHighlightingLayer(this);
				cbmi.setSelected(displaySelectedMapArea);
				cbmi.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if (displaySelectedMapArea) {
							MapAreaHighlightingLayer.removeHighlightingLayers();
						} else {
							mapView.setSelectionByTileCoordinate(null, null, false);
							MapAreaHighlightingLayer.removeHighlightingLayers();
							mapView.mapLayers.add(msl);
						}
						displaySelectedMapArea = !displaySelectedMapArea;
						mapView.repaint();
					}
				});
				pm.add(cbmi);
			}
			if (o instanceof MapInterface) {
				mi = new JMenuItem("Select map bounding box");
				mi.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						MapInterface map = (MapInterface) o;
						mapView.setMapSource(map.getMapSource());
						mapView.setSelectionByTileCoordinate(map.getZoom(), map.getMinTileCoordinate(), map
								.getMaxTileCoordinate(), true);
					}
				});
				pm.add(mi);
				mi = new JMenuItem("Zoom to map bounding box");
				mi.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						MapInterface map = (MapInterface) o;
						MapSelection ms = new MapSelection(map);
						mapView.setMapSource(map.getMapSource());
						mapView.setSelectionAndZoomTo(ms, true);
						mapView.setSelectionByTileCoordinate(map.getZoom(), map.getMinTileCoordinate(), map
								.getMaxTileCoordinate(), true);
					}
				});
				pm.add(mi);
			}
			if (o instanceof LayerInterface) {
				mi = new JMenuItem("Zoom to");
				mi.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						LayerInterface layer = (LayerInterface) o;
						EastNorthCoordinate max = new EastNorthCoordinate(Double.NEGATIVE_INFINITY,
								Double.NEGATIVE_INFINITY);
						EastNorthCoordinate min = new EastNorthCoordinate(Double.POSITIVE_INFINITY,
								Double.POSITIVE_INFINITY);
						for (MapInterface map : layer) {
							MapSelection ms = new MapSelection(map);
							EastNorthCoordinate mapMax = ms.getMax();
							EastNorthCoordinate mapMin = ms.getMin();
							max.lat = Math.max(max.lat, mapMax.lat);
							max.lon = Math.max(max.lon, mapMax.lon);
							min.lat = Math.min(min.lat, mapMin.lat);
							min.lon = Math.min(min.lon, mapMin.lon);
						}
						MapSelection ms = new MapSelection(mapView.getMapSource(), max, min);
						mapView.zoomTo(ms);
					}
				});
				pm.add(mi);
			}
			if (o instanceof AtlasObject) {
				mi = new JMenuItem("Rename");
				mi.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						JAtlasTree.this.startEditingAtPath(selPath);
					}
				});
				pm.add(mi);
				mi = new JMenuItem("Apply tile processing options");
				mi.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						AtlasObject atlasObject = (AtlasObject) o;
						TileImageParameters p = MainGUI.getMainGUI().getSelectedTileImageParameters();
						applyTileImageParameters(atlasObject, p);
					}
				});
				pm.add(mi);
			}
			if (o instanceof CapabilityDeletable) {
				pm.addSeparator();
				mi = new JMenuItem(getActionMap().get(ACTION_DELETE_NODE));
				mi.setAccelerator(deleteNodeKS);
				pm.add(mi);
			}
		}
		if (pm.getComponentCount() > 0)
			pm.addSeparator();
		mi = new JMenuItem("Clear atlas");
		mi.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				newAtlas();
			}
		});
		pm.add(mi);
		pm.show(this, event.getX(), event.getY());
	}

	protected void applyTileImageParameters(Object o, TileImageParameters p) {
		if (o instanceof Iterable<?>) {
			Iterable<?> it = (Iterable<?>) o;
			for (Object ao : it) {
				applyTileImageParameters(ao, p);
			}
		} else if (o instanceof MapInterface) {
			((MapInterface) o).setParameters(p);
		}
	}

	protected void selectElementOnMap(Object o) {
		if (o instanceof MapInterface) {
			MapInterface map = (MapInterface) o;
			mapView.setMapSource(map.getMapSource());
			mapView.setSelectionByTileCoordinate(map.getZoom(), map.getMinTileCoordinate(), map.getMaxTileCoordinate(),
					true);
		}
	}

	public void autoscroll(Point cursorLocn) {
		int realrow = getRowForLocation(cursorLocn.x, cursorLocn.y);
		Rectangle outer = getBounds();
		realrow = (cursorLocn.y + outer.y <= margin ? realrow < 1 ? 0 : realrow - 1
				: realrow < getRowCount() - 1 ? realrow + 1 : realrow);
		scrollRowToVisible(realrow);
	}

	public Insets getAutoscrollInsets() {
		Rectangle outer = getBounds();
		Rectangle inner = getParent().getBounds();
		return new Insets(inner.y - outer.y + margin, inner.x - outer.x + margin, outer.height - inner.height - inner.y
				+ outer.y + margin, outer.width - inner.width - inner.x + outer.x + margin);
	}

}
