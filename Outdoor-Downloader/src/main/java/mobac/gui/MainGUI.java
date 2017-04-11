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
package mobac.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.xml.bind.JAXBException;

import mobac.externaltools.ExternalToolDef;
import mobac.externaltools.ExternalToolsLoader;
import mobac.gui.actions.AddGpxTrackPolygonMap;
import mobac.gui.actions.AddMapLayer;
import mobac.gui.actions.AtlasConvert;
import mobac.gui.actions.AtlasCreate;
import mobac.gui.actions.AtlasNew;
import mobac.gui.actions.BookmarkAdd;
import mobac.gui.actions.BookmarkManage;
import mobac.gui.actions.DebugSetLogLevel;
import mobac.gui.actions.DebugShowLogFile;
import mobac.gui.actions.DebugShowMapSourceNames;
import mobac.gui.actions.DebugShowMapTileGrid;
import mobac.gui.actions.DebugShowReport;
import mobac.gui.actions.HelpLicenses;
import mobac.gui.actions.PanelShowHide;
import mobac.gui.actions.RefreshCustomMapsources;
import mobac.gui.actions.SelectionModeCircle;
import mobac.gui.actions.SelectionModePolygon;
import mobac.gui.actions.SelectionModeRectangle;
import mobac.gui.actions.ShowAboutDialog;
import mobac.gui.actions.ShowHelpAction;
import mobac.gui.actions.ShowReadme;
import mobac.gui.atlastree.JAtlasTree;
import mobac.gui.components.FilledLayeredPane;
import mobac.gui.components.JAtlasNameField;
import mobac.gui.components.JBookmarkMenuItem;
import mobac.gui.components.JCollapsiblePanel;
import mobac.gui.components.JMenuItem2;
import mobac.gui.components.JZoomCheckBox;
import mobac.gui.listeners.AtlasModelListener;
import mobac.gui.mapview.GridZoom;
import mobac.gui.mapview.JMapViewer;
import mobac.gui.mapview.PreviewMap;
import mobac.gui.mapview.WgsGrid.WgsDensity;
import mobac.gui.mapview.controller.JMapController;
import mobac.gui.mapview.controller.PolygonCircleSelectionMapController;
import mobac.gui.mapview.controller.PolygonSelectionMapController;
import mobac.gui.mapview.controller.RectangleSelectionMapController;
import mobac.gui.mapview.interfaces.MapEventListener;
import mobac.gui.panels.JCoordinatesPanel;
import mobac.gui.panels.JGpxPanel;
import mobac.gui.panels.JProfilesPanel;
import mobac.gui.panels.JTileImageParametersPanel;
import mobac.gui.panels.JTileStoreCoveragePanel;
import mobac.gui.settings.SettingsGUI;
import mobac.mapsources.MapSourcesManager;
import mobac.program.ProgramInfo;
import mobac.program.interfaces.AtlasInterface;
import mobac.program.interfaces.FileBasedMapSource;
import mobac.program.interfaces.MapSource;
import mobac.program.model.Bookmark;
import mobac.program.model.MapSelection;
import mobac.program.model.MercatorPixelCoordinate;
import mobac.program.model.Profile;
import mobac.program.model.SelectedZoomLevels;
import mobac.program.model.Settings;
import mobac.program.model.SettingsWgsGrid;
import mobac.program.model.TileImageParameters;
import mobac.utilities.GBC;
import mobac.utilities.GUIExceptionHandler;
import mobac.utilities.Utilities;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class MainGUI extends JFrame implements MapEventListener {

	private static final long serialVersionUID = 1L;

	private static Logger log = Logger.getLogger(MainGUI.class);

	private static Color labelBackgroundColor = new Color(0, 0, 0, 127);
	private static Color labelForegroundColor = Color.WHITE;

	private static MainGUI mainGUI = null;
	public static final ArrayList<Image> MOBAC_ICONS = new ArrayList<Image>(3);

	static {
		MOBAC_ICONS.add(Utilities.loadResourceImageIcon("mobac48.png").getImage());
		MOBAC_ICONS.add(Utilities.loadResourceImageIcon("mobac32.png").getImage());
		MOBAC_ICONS.add(Utilities.loadResourceImageIcon("mobac16.png").getImage());
	}

	protected JMenuBar menuBar;
	protected JMenu toolsMenu = null;

	private JMenu bookmarkMenu = new JMenu("Bookmarks");

	public final PreviewMap previewMap = new PreviewMap();
	public final JAtlasTree jAtlasTree = new JAtlasTree(previewMap);

	private JCheckBox wgsGridCheckBox;
	private JComboBox wgsGridCombo;

	private JLabel zoomLevelText;
	private JComboBox gridZoomCombo;
	private JSlider zoomSlider;
	private JComboBox mapSourceCombo;
	private JButton settingsButton;
	private JAtlasNameField atlasNameTextField;
	private JButton createAtlasButton;
	private JPanel zoomLevelPanel;
	private JZoomCheckBox[] cbZoom = new JZoomCheckBox[0];
	private JLabel amountOfTilesLabel;

	private AtlasCreate atlasCreateAction = new AtlasCreate(jAtlasTree);

	private JCoordinatesPanel coordinatesPanel;
	private JProfilesPanel profilesPanel;
	public JTileImageParametersPanel tileImageParametersPanel;
	private JTileStoreCoveragePanel tileStoreCoveragePanel;
	public JGpxPanel gpxPanel;

	private JPanel mapControlPanel = new JPanel(new BorderLayout());
	private JPanel leftPanel = new JPanel(new GridBagLayout());
	private JPanel leftPanelContent = null;
	private JPanel rightPanel = new JPanel(new GridBagLayout());

	public JMenu logLevelMenu;
	private JMenuItem smRectangle;
	private JMenuItem smPolygon;
	private JMenuItem smCircle;

	private MercatorPixelCoordinate mapSelectionMax = null;
	private MercatorPixelCoordinate mapSelectionMin = null;

	public static void createMainGui() {
		if (mainGUI != null)
			return;
		mainGUI = new MainGUI();
		mainGUI.setVisible(true);
		log.trace("MainGUI now visible");
	}

	public static MainGUI getMainGUI() {
		return mainGUI;
	}

	private MainGUI() {
		super();
		mainGUI = this;
		setIconImages(MOBAC_ICONS);
		GUIExceptionHandler.registerForCurrentThread();
		setTitle(ProgramInfo.getCompleteTitle());

		log.trace("Creating main dialog - " + getTitle());
		setResizable(true);
		Dimension dScreen = Toolkit.getDefaultToolkit().getScreenSize();
		setMinimumSize(new Dimension(Math.min(800, dScreen.width), Math.min(590, dScreen.height)));
		setSize(getMinimumSize());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		addWindowListener(new WindowDestroyer());
		addComponentListener(new MainWindowListener());

		previewMap.addMapEventListener(this);

		createControls();
		calculateNrOfTilesToDownload();
		setLayout(new BorderLayout());
		add(leftPanel, BorderLayout.WEST);
		add(rightPanel, BorderLayout.EAST);
		JLayeredPane layeredPane = new FilledLayeredPane();
		layeredPane.add(previewMap, Integer.valueOf(0));
		layeredPane.add(mapControlPanel, Integer.valueOf(1));
		add(layeredPane, BorderLayout.CENTER);

		updateMapControlsPanel();
		updateLeftPanel();
		updateRightPanel();
		updateZoomLevelCheckBoxes();
		calculateNrOfTilesToDownload();

		menuBar = new JMenuBar();
		prepareMenuBar();
		setJMenuBar(menuBar);

		loadSettings();
		profilesPanel.initialize();
		mapSourceChanged(previewMap.getMapSource());
		updateZoomLevelCheckBoxes();
		updateGridSizeCombo();
		tileImageParametersPanel.updateControlsState();
		zoomChanged(previewMap.getZoom());
		gridZoomChanged(previewMap.getGridZoom());
		previewMap.updateMapSelection();
		previewMap.grabFocus();
	}

	private void createControls() {

		// zoom slider
		zoomSlider = new JSlider(JMapViewer.MIN_ZOOM, previewMap.getMapSource().getMaxZoom());
		zoomSlider.setOrientation(JSlider.HORIZONTAL);
		zoomSlider.setMinimumSize(new Dimension(50, 10));
		zoomSlider.setSize(50, zoomSlider.getPreferredSize().height);
		zoomSlider.addChangeListener(new ZoomSliderListener());
		zoomSlider.setOpaque(false);

		// zoom level text
		zoomLevelText = new JLabel(" 00 ");
		zoomLevelText.setOpaque(true);
		zoomLevelText.setBackground(labelBackgroundColor);
		zoomLevelText.setForeground(labelForegroundColor);
		zoomLevelText.setToolTipText("The current zoom level");

		// grid zoom combo
		gridZoomCombo = new JComboBox();
		gridZoomCombo.setEditable(false);
		gridZoomCombo.addActionListener(new GridZoomComboListener());
		gridZoomCombo.setToolTipText("Projects a grid of the specified zoom level over the map");

		SettingsWgsGrid s = Settings.getInstance().wgsGrid;

		// WGS Grid label
		wgsGridCheckBox = new JCheckBox(" WGS Grid: ", s.enabled);
		// wgsGridCheckBox.setOpaque(true);
		wgsGridCheckBox.setBackground(labelBackgroundColor);
		wgsGridCheckBox.setForeground(labelForegroundColor);
		wgsGridCheckBox.setToolTipText("Projects WGS Grid on map preview (not included in atlas)");
		wgsGridCheckBox.setMargin(new Insets(0, 0, 0, 0));
		wgsGridCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean enabled = wgsGridCheckBox.isSelected();
				Settings.getInstance().wgsGrid.enabled = enabled;
				wgsGridCombo.setVisible(enabled);
				previewMap.repaint();
			}
		});

		// WGS Grid combo
		wgsGridCombo = new JComboBox(WgsDensity.values());
		wgsGridCombo.setMaximumRowCount(WgsDensity.values().length);
		wgsGridCombo.setVisible(s.enabled);
		wgsGridCombo.setSelectedItem(s.density);
		wgsGridCombo.setToolTipText("Specifies density for WGS Grid");
		wgsGridCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				WgsDensity d = (WgsDensity) wgsGridCombo.getSelectedItem();
				Settings.getInstance().wgsGrid.density = d;
				previewMap.repaint();
			}
		});

		// map source combo
		mapSourceCombo = new JComboBox(MapSourcesManager.getInstance().getEnabledOrderedMapSources());
		mapSourceCombo.setMaximumRowCount(20);
		mapSourceCombo.addActionListener(new MapSourceComboListener());
		mapSourceCombo.setToolTipText("Select map source");

		// settings button
		settingsButton = new JButton("Settings");
		settingsButton.addActionListener(new SettingsButtonListener());
		settingsButton.setToolTipText("Open the preferences dialogue panel.");

		// atlas name text field
		atlasNameTextField = new JAtlasNameField();
		atlasNameTextField.setColumns(12);
		atlasNameTextField.setActionCommand("atlasNameTextField");
		atlasNameTextField.setToolTipText("Enter a name for the atlas here");

		// create atlas button
		createAtlasButton = new JButton("Create atlas");
		createAtlasButton.addActionListener(atlasCreateAction);
		createAtlasButton.setToolTipText("Create the atlas");

		// zoom level check boxes
		zoomLevelPanel = new JPanel();
		zoomLevelPanel.setBorder(BorderFactory.createEmptyBorder());
		zoomLevelPanel.setOpaque(false);

		// amount of tiles to download
		amountOfTilesLabel = new JLabel();
		amountOfTilesLabel.setToolTipText("Total amount of tiles to download");
		amountOfTilesLabel.setOpaque(true);
		amountOfTilesLabel.setBackground(labelBackgroundColor);
		amountOfTilesLabel.setForeground(labelForegroundColor);

		coordinatesPanel = new JCoordinatesPanel();
		tileImageParametersPanel = new JTileImageParametersPanel();
		profilesPanel = new JProfilesPanel(jAtlasTree);
		profilesPanel.getLoadButton().addActionListener(new LoadProfileListener());
		tileStoreCoveragePanel = new JTileStoreCoveragePanel(previewMap);
	}

	private void prepareMenuBar() {
		// Atlas menu
		JMenu atlasMenu = new JMenu("Atlas");
		atlasMenu.setMnemonic(KeyEvent.VK_A);

		JMenuItem newAtlas = new JMenuItem("New Atlas");
		newAtlas.setMnemonic(KeyEvent.VK_N);
		newAtlas.addActionListener(new AtlasNew());
		atlasMenu.add(newAtlas);

		JMenuItem convertAtlas = new JMenuItem("Convert Atlas Format");
		convertAtlas.setMnemonic(KeyEvent.VK_V);
		convertAtlas.addActionListener(new AtlasConvert());
		atlasMenu.add(convertAtlas);
		atlasMenu.addSeparator();

		JMenuItem createAtlas = new JMenuItem("Create Atlas");
		createAtlas.setMnemonic(KeyEvent.VK_C);
		createAtlas.addActionListener(atlasCreateAction);
		atlasMenu.add(createAtlas);

		// Maps menu
		JMenu mapsMenu = new JMenu("Maps");
		mapsMenu.setMnemonic(KeyEvent.VK_M);
		JMenu selectionModeMenu = new JMenu("Selection Mode");
		selectionModeMenu.setMnemonic(KeyEvent.VK_M);
		mapsMenu.add(selectionModeMenu);

		smRectangle = new JRadioButtonMenuItem("Rectangle");
		smRectangle.addActionListener(new SelectionModeRectangle());
		smRectangle.setSelected(true);
		selectionModeMenu.add(smRectangle);

		smPolygon = new JRadioButtonMenuItem("Polygon");
		smPolygon.addActionListener(new SelectionModePolygon());
		selectionModeMenu.add(smPolygon);

		smCircle = new JRadioButtonMenuItem("Circle");
		smCircle.addActionListener(new SelectionModeCircle());
		selectionModeMenu.add(smCircle);

		JMenuItem addSelection = new JMenuItem("Add selection");
		addSelection.addActionListener(AddMapLayer.INSTANCE);
		addSelection.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
		addSelection.setMnemonic(KeyEvent.VK_A);
		mapsMenu.add(addSelection);

		JMenuItem addGpxTrackSelection = new JMenuItem2("Add selection by GPX track", AddGpxTrackPolygonMap.class);
		mapsMenu.add(addGpxTrackSelection);

		// Bookmarks menu
		bookmarkMenu.setMnemonic(KeyEvent.VK_B);
		JMenuItem addBookmark = new JMenuItem("Save current view");
		addBookmark.setMnemonic(KeyEvent.VK_S);
		addBookmark.addActionListener(new BookmarkAdd(previewMap));
		bookmarkMenu.add(addBookmark);
		JMenuItem manageBookmarks = new JMenuItem2("Manage Bookmarks", BookmarkManage.class);
		manageBookmarks.setMnemonic(KeyEvent.VK_S);
		bookmarkMenu.add(addBookmark);
		bookmarkMenu.add(manageBookmarks);
		bookmarkMenu.addSeparator();

		// Panels menu
		JMenu panelsMenu = new JMenu("Panels");
		panelsMenu.setMnemonic(KeyEvent.VK_P);
		JMenuItem showLeftPanel = new JMenuItem("Show/hide left panel");
		showLeftPanel.addActionListener(new PanelShowHide(leftPanel));
		JMenuItem showRightPanel = new JMenuItem("Show/hide GPX editor panel");
		showRightPanel.addActionListener(new PanelShowHide(rightPanel));
		panelsMenu.add(showLeftPanel);
		panelsMenu.add(showRightPanel);

		menuBar.add(atlasMenu);
		menuBar.add(mapsMenu);
		menuBar.add(bookmarkMenu);
		menuBar.add(panelsMenu);

		loadToolsMenu();

		menuBar.add(Box.createHorizontalGlue());

		// Debug menu
		JMenu debugMenu = new JMenu("Debug");
		JMenuItem mapGrid = new JCheckBoxMenuItem("Show/Hide map tile borders", false);
		mapGrid.addActionListener(new DebugShowMapTileGrid());
		debugMenu.add(mapGrid);
		debugMenu.addSeparator();

		debugMenu.setMnemonic(KeyEvent.VK_D);
		JMenuItem mapSourceNames = new JMenuItem2("Show all map source names", DebugShowMapSourceNames.class);
		mapSourceNames.setMnemonic(KeyEvent.VK_N);
		debugMenu.add(mapSourceNames);
		debugMenu.addSeparator();

		JMenuItem refreshCustomMapSources = new JMenuItem2("Refresh custom map sources", RefreshCustomMapsources.class);
		debugMenu.add(refreshCustomMapSources);
		debugMenu.addSeparator();
		JMenuItem showLog = new JMenuItem2("Show log file", DebugShowLogFile.class);
		showLog.setMnemonic(KeyEvent.VK_S);
		debugMenu.add(showLog);

		logLevelMenu = new JMenu("General log level");
		logLevelMenu.setMnemonic(KeyEvent.VK_L);
		Level[] list = new Level[] { Level.TRACE, Level.DEBUG, Level.INFO, Level.ERROR, Level.FATAL, Level.OFF };
		ActionListener al = new DebugSetLogLevel();
		Level rootLogLevel = Logger.getRootLogger().getLevel();
		for (Level level : list) {
			String name = level.toString();
			JRadioButtonMenuItem item = new JRadioButtonMenuItem(name, (rootLogLevel.toString().equals(name)));
			item.setName(name);
			item.addActionListener(al);
			logLevelMenu.add(item);
		}
		debugMenu.add(logLevelMenu);
		debugMenu.addSeparator();
		JMenuItem report = new JMenuItem2("Generate system report", DebugShowReport.class);
		report.setMnemonic(KeyEvent.VK_R);
		debugMenu.add(report);
		menuBar.add(debugMenu);

		// Help menu
		JMenu help = new JMenu("Help");
		JMenuItem readme = new JMenuItem("Show Readme");
		JMenuItem howToMap = new JMenuItem("How to use preview map");
		JMenuItem licenses = new JMenuItem("Licenses");
		JMenuItem about = new JMenuItem("About");
		readme.addActionListener(new ShowReadme());
		about.addActionListener(new ShowAboutDialog());
		howToMap.addActionListener(new ShowHelpAction());
		licenses.addActionListener(new HelpLicenses());
		help.add(readme);
		help.add(howToMap);
		help.addSeparator();
		help.add(licenses);
		help.addSeparator();
		help.add(about);

		menuBar.add(help);
	}

	public void loadToolsMenu() {
		if (ExternalToolsLoader.load()) {
			if (toolsMenu == null) {
				toolsMenu = new JMenu("Tools");
				toolsMenu.addMenuListener(new MenuListener() {

					public void menuSelected(MenuEvent e) {
						loadToolsMenu();
						System.out.println("Loaded");
					}

					public void menuDeselected(MenuEvent e) {
					}

					public void menuCanceled(MenuEvent e) {
					}
				});
				menuBar.add(toolsMenu);
			}
			toolsMenu.removeAll();
			for (ExternalToolDef t : ExternalToolsLoader.tools) {
				JMenuItem m = new JMenuItem(t.name);
				m.addActionListener(t);
				toolsMenu.add(m);
			}
		}
	}

	private void updateLeftPanel() {
		leftPanel.removeAll();

		coordinatesPanel.addButtonActionListener(new ApplySelectionButtonListener());

		JCollapsiblePanel mapSourcePanel = new JCollapsiblePanel("Map source", new GridBagLayout());
		mapSourcePanel.addContent(mapSourceCombo, GBC.std().insets(2, 2, 2, 2).fill());

		JCollapsiblePanel zoomLevelsPanel = new JCollapsiblePanel("Zoom Levels", new GridBagLayout());
		zoomLevelsPanel.addContent(zoomLevelPanel, GBC.eol().insets(2, 4, 2, 0));
		zoomLevelsPanel.addContent(amountOfTilesLabel, GBC.std().anchor(GBC.WEST).insets(0, 5, 0, 2));

		GBC gbc_std = GBC.std().insets(5, 2, 5, 3);
		GBC gbc_eol = GBC.eol().insets(5, 2, 5, 3);

		JCollapsiblePanel atlasContentPanel = new JCollapsiblePanel("Atlas Content", new GridBagLayout());
		JScrollPane treeScrollPane = new JScrollPane(jAtlasTree, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jAtlasTree.getTreeModel().addTreeModelListener(new AtlasModelListener(jAtlasTree, profilesPanel));

		treeScrollPane.setMinimumSize(new Dimension(100, 150));
		treeScrollPane.setPreferredSize(new Dimension(100, 200));
		treeScrollPane.setAutoscrolls(true);
		atlasContentPanel.addContent(treeScrollPane, GBC.eol().fill().insets(0, 1, 0, 0));
		JButton clearAtlas = new JButton("New");
		atlasContentPanel.addContent(clearAtlas, GBC.std());
		clearAtlas.addActionListener(new AtlasNew());
		JButton addLayers = new JButton("Add selection");
		atlasContentPanel.addContent(addLayers, GBC.eol());
		addLayers.addActionListener(AddMapLayer.INSTANCE);
		atlasContentPanel.addContent(new JLabel("Name: "), gbc_std);
		atlasContentPanel.addContent(atlasNameTextField, gbc_eol.fill(GBC.HORIZONTAL));

		gbc_eol = GBC.eol().insets(5, 2, 5, 2).fill(GBC.HORIZONTAL);

		leftPanelContent = new JPanel(new GridBagLayout());
		leftPanelContent.add(coordinatesPanel, gbc_eol);
		leftPanelContent.add(mapSourcePanel, gbc_eol);
		leftPanelContent.add(zoomLevelsPanel, gbc_eol);
		leftPanelContent.add(tileImageParametersPanel, gbc_eol);
		leftPanelContent.add(atlasContentPanel, gbc_eol);

		leftPanelContent.add(profilesPanel, gbc_eol);
		leftPanelContent.add(createAtlasButton, gbc_eol);
		leftPanelContent.add(settingsButton, gbc_eol);
		leftPanelContent.add(tileStoreCoveragePanel, gbc_eol);
		leftPanelContent.add(Box.createVerticalGlue(), GBC.eol().fill(GBC.VERTICAL));

		JScrollPane scrollPane = new JScrollPane(leftPanelContent);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		// Set the scroll pane width large enough so that the
		// scroll bar has enough space to appear right to it
		Dimension d = scrollPane.getPreferredSize();
		d.width += 5 + scrollPane.getVerticalScrollBar().getWidth();
		// scrollPane.setPreferredSize(d);
		scrollPane.setMinimumSize(d);
		leftPanel.add(scrollPane, GBC.std().fill());
		// leftPanel.add(leftPanelContent, GBC.std().fill());

	}

	private void updateRightPanel() {
		GBC gbc_eol = GBC.eol().insets(5, 2, 5, 2).fill();
		gpxPanel = new JGpxPanel(previewMap);
		rightPanel.add(gpxPanel, gbc_eol);
	}

	private JPanel updateMapControlsPanel() {
		mapControlPanel.removeAll();
		mapControlPanel.setOpaque(false);

		// zoom label
		JLabel zoomLabel = new JLabel(" Zoom: ");
		zoomLabel.setOpaque(true);
		zoomLabel.setBackground(labelBackgroundColor);
		zoomLabel.setForeground(labelForegroundColor);

		// top panel
		JPanel topControls = new JPanel(new GridBagLayout());
		topControls.setOpaque(false);
		topControls.add(zoomLabel, GBC.std().insets(5, 5, 0, 0));
		topControls.add(zoomSlider, GBC.std().insets(0, 5, 0, 0));
		topControls.add(zoomLevelText, GBC.std().insets(0, 5, 0, 0));
		topControls.add(gridZoomCombo, GBC.std().insets(10, 5, 0, 0));
		topControls.add(wgsGridCheckBox, GBC.std().insets(10, 5, 0, 0));
		topControls.add(wgsGridCombo, GBC.std().insets(5, 5, 0, 0));
		topControls.add(Box.createHorizontalGlue(), GBC.std().fillH());
		mapControlPanel.add(topControls, BorderLayout.NORTH);

		// bottom panel
		// JPanel bottomControls = new JPanel(new GridBagLayout());
		// bottomControls.setOpaque(false);
		// bottomControls.add(Box.createHorizontalGlue(),
		// GBC.std().fill(GBC.HORIZONTAL));
		// mapControlPanel.add(bottomControls, BorderLayout.SOUTH);

		return mapControlPanel;
	}

	public void updateMapSourcesList() {
		MapSource ms = (MapSource) mapSourceCombo.getSelectedItem();
		mapSourceCombo
				.setModel(new DefaultComboBoxModel(MapSourcesManager.getInstance().getEnabledOrderedMapSources()));
		mapSourceCombo.setSelectedItem(ms);
		MapSource ms2 = (MapSource) mapSourceCombo.getSelectedItem();
		if (!ms.equals(ms2))
			previewMap.setMapSource(ms2);
	}

	public void updateBookmarksMenu() {
		LinkedList<JMenuItem> items = new LinkedList<JMenuItem>();
		for (int i = 0; i < bookmarkMenu.getMenuComponentCount(); i++) {
			JMenuItem item = bookmarkMenu.getItem(i);
			if (!(item instanceof JBookmarkMenuItem))
				items.add(item);
		}
		bookmarkMenu.removeAll();
		for (JMenuItem item : items) {
			if (item != null)
				bookmarkMenu.add(item);
			else
				bookmarkMenu.addSeparator();
		}
		for (Bookmark b : Settings.getInstance().placeBookmarks) {
			bookmarkMenu.add(new JBookmarkMenuItem(b));
		}
	}

	private void loadSettings() {
		if (Profile.DEFAULT.exists())
			jAtlasTree.load(Profile.DEFAULT);
		else
			new AtlasNew().actionPerformed(null);

		Settings settings = Settings.getInstance();
		atlasNameTextField.setText(settings.elementName);
		previewMap.settingsLoad();
		int nextZoom = 0;
		List<Integer> zoomList = settings.selectedZoomLevels;
		if (zoomList != null) {
			for (JZoomCheckBox currentZoomCb : cbZoom) {
				for (int i = nextZoom; i < zoomList.size(); i++) {
					int currentListZoom = zoomList.get(i);
					if (currentZoomCb.getZoomLevel() == currentListZoom) {
						currentZoomCb.setSelected(true);
						nextZoom = 1;
						break;
					}
				}
			}
		}
		coordinatesPanel.setNumberFormat(settings.coordinateNumberFormat);

		tileImageParametersPanel.loadSettings();
		tileImageParametersPanel.atlasFormatChanged(jAtlasTree.getAtlas().getOutputFormat());
		// mapSourceCombo
		// .setSelectedItem(MapSourcesManager.getSourceByName(settings.
		// mapviewMapSource));

		setSize(settings.mainWindow.size);
		Point windowLocation = settings.mainWindow.position;
		if (windowLocation.x == -1 && windowLocation.y == -1) {
			setLocationRelativeTo(null);
		} else {
			setLocation(windowLocation);
		}
		if (settings.mainWindow.maximized)
			setExtendedState(Frame.MAXIMIZED_BOTH);

		leftPanel.setVisible(settings.mainWindow.leftPanelVisible);
		rightPanel.setVisible(settings.mainWindow.rightPanelVisible);

		if (leftPanelContent != null) {
			for (Component c : leftPanelContent.getComponents()) {
				if (c instanceof JCollapsiblePanel) {
					JCollapsiblePanel cp = (JCollapsiblePanel) c;
					String name = cp.getName();
					if (name != null && settings.mainWindow.collapsedPanels.contains(name))
						cp.setCollapsed(true);
				}
			}
		}

		updateBookmarksMenu();
	}

	private void saveSettings() {
		try {
			jAtlasTree.save(Profile.DEFAULT);

			Settings s = Settings.getInstance();
			previewMap.settingsSave();
			s.mapviewMapSource = previewMap.getMapSource().getName();
			s.selectedZoomLevels = new SelectedZoomLevels(cbZoom).getZoomLevelList();

			s.elementName = atlasNameTextField.getText();
			s.coordinateNumberFormat = coordinatesPanel.getNumberFormat();

			tileImageParametersPanel.saveSettings();
			boolean maximized = (getExtendedState() & Frame.MAXIMIZED_BOTH) != 0;
			s.mainWindow.maximized = maximized;
			if (!maximized) {
				s.mainWindow.size = getSize();
				s.mainWindow.position = getLocation();
			}
			s.mainWindow.collapsedPanels.clear();
			if (leftPanelContent != null) {
				for (Component c : leftPanelContent.getComponents()) {
					if (c instanceof JCollapsiblePanel) {
						JCollapsiblePanel cp = (JCollapsiblePanel) c;
						if (cp.isCollapsed())
							s.mainWindow.collapsedPanels.add(cp.getName());
					}
				}
			}
			s.mainWindow.leftPanelVisible = leftPanel.isVisible();
			s.mainWindow.rightPanelVisible = rightPanel.isVisible();
			checkAndSaveSettings();
		} catch (Exception e) {
			GUIExceptionHandler.showExceptionDialog(e);
			JOptionPane.showMessageDialog(null, "Error on writing program settings to \"settings.xml\"", "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	public void checkAndSaveSettings() throws JAXBException {
		if (Settings.checkSettingsFileModified()) {
			int x = JOptionPane.showConfirmDialog(this,
					"The settings.xml files has been changed by another application.\n"
							+ "Do you want to overwrite these changes?\n"
							+ "All changes made by the other application will be lost!", "Overwrite changes?",
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (x != JOptionPane.YES_OPTION)
				return;
		}
		Settings.save();

	}

	public JTileImageParametersPanel getParametersPanel() {
		return tileImageParametersPanel;
	}

	public String getUserText() {
		return atlasNameTextField.getText();
	}

	public void refreshPreviewMap() {
		previewMap.refreshMap();
	}

	private class ZoomSliderListener implements ChangeListener {
		public void stateChanged(ChangeEvent e) {
			previewMap.setZoom(zoomSlider.getValue());
		}
	}

	private class GridZoomComboListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (!gridZoomCombo.isEnabled())
				return;
			GridZoom g = (GridZoom) gridZoomCombo.getSelectedItem();
			if (g == null)
				return;
			log.debug("Selected grid zoom combo box item has changed: " + g.getZoom());
			previewMap.setGridZoom(g.getZoom());
			repaint();
			previewMap.updateMapSelection();
		}
	}

	private void updateGridSizeCombo() {
		int maxZoom = previewMap.getMapSource().getMaxZoom();
		int minZoom = previewMap.getMapSource().getMinZoom();
		GridZoom lastGridZoom = (GridZoom) gridZoomCombo.getSelectedItem();
		gridZoomCombo.setEnabled(false);
		gridZoomCombo.removeAllItems();
		gridZoomCombo.setMaximumRowCount(maxZoom - minZoom + 2);
		gridZoomCombo.addItem(new GridZoom(-1) {

			@Override
			public String toString() {
				return "Grid disabled";
			}

		});
		for (int i = maxZoom; i >= minZoom; i--) {
			gridZoomCombo.addItem(new GridZoom(i));
		}
		if (lastGridZoom != null)
			gridZoomCombo.setSelectedItem(lastGridZoom);
		gridZoomCombo.setEnabled(true);
	}

	private class ApplySelectionButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			setSelectionByEnteredCoordinates();
		}
	}

	private class MapSourceComboListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			MapSource mapSource = (MapSource) mapSourceCombo.getSelectedItem();
			if (mapSource == null) {
				mapSourceCombo.setSelectedIndex(0);
				mapSource = (MapSource) mapSourceCombo.getSelectedItem();
			}
			if (mapSource instanceof FileBasedMapSource)
				// initialize the map source e.g. detect available zoom levels
				((FileBasedMapSource) mapSource).initialize();
			previewMap.setMapSource(mapSource);
			zoomSlider.setMinimum(previewMap.getMapSource().getMinZoom());
			zoomSlider.setMaximum(previewMap.getMapSource().getMaxZoom());
			updateGridSizeCombo();
			updateZoomLevelCheckBoxes();
			calculateNrOfTilesToDownload();
		}
	}

	private class LoadProfileListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			Profile profile = profilesPanel.getSelectedProfile();
			profilesPanel.getDeleteButton().setEnabled(profile != null);
			if (profile == null)
				return;

			jAtlasTree.load(profile);
			previewMap.repaint();
			tileImageParametersPanel.atlasFormatChanged(jAtlasTree.getAtlas().getOutputFormat());
		}
	}

	private class SettingsButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			SettingsGUI.showSettingsDialog(MainGUI.this);
		}
	}

	private void updateZoomLevelCheckBoxes() {
		MapSource tileSource = previewMap.getMapSource();
		int zoomLevels = tileSource.getMaxZoom() - tileSource.getMinZoom() + 1;
		zoomLevels = Math.max(zoomLevels, 0);
		JCheckBox oldZoomLevelCheckBoxes[] = cbZoom;
		int oldMinZoom = 0;
		if (cbZoom.length > 0)
			oldMinZoom = cbZoom[0].getZoomLevel();
		cbZoom = new JZoomCheckBox[zoomLevels];
		zoomLevelPanel.removeAll();

		zoomLevelPanel.setLayout(new GridLayout(0, 10, 1, 2));
		ZoomLevelCheckBoxListener cbl = new ZoomLevelCheckBoxListener();

		for (int i = cbZoom.length - 1; i >= 0; i--) {
			int cbz = i + tileSource.getMinZoom();
			JZoomCheckBox cb = new JZoomCheckBox(cbz);
			cb.setPreferredSize(new Dimension(22, 11));
			cb.setMinimumSize(cb.getPreferredSize());
			cb.setOpaque(false);
			cb.setFocusable(false);
			cb.setName(Integer.toString(cbz));
			int oldCbIndex = cbz - oldMinZoom;
			if (oldCbIndex >= 0 && oldCbIndex < (oldZoomLevelCheckBoxes.length))
				cb.setSelected(oldZoomLevelCheckBoxes[oldCbIndex].isSelected());
			cb.addActionListener(cbl);
			// cb.setToolTipText("Select zoom level " + cbz + " for atlas");
			zoomLevelPanel.add(cb);
			cbZoom[i] = cb;

			JLabel l = new JLabel(Integer.toString(cbz));
			zoomLevelPanel.add(l);
		}
		amountOfTilesLabel.setOpaque(false);
		amountOfTilesLabel.setForeground(Color.black);
	}

	private class ZoomLevelCheckBoxListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			calculateNrOfTilesToDownload();
		}
	}

	public void selectionChanged(MercatorPixelCoordinate max, MercatorPixelCoordinate min) {
		mapSelectionMax = max;
		mapSelectionMin = min;
		coordinatesPanel.setSelection(max, min);
		calculateNrOfTilesToDownload();
	}

	public void zoomChanged(int zoomLevel) {
		zoomLevelText.setText(" " + zoomLevel + " ");
		zoomSlider.setValue(zoomLevel);
	}

	public void gridZoomChanged(int newGridZoomLevel) {
		gridZoomCombo.setSelectedItem(new GridZoom(newGridZoomLevel));
	}

	public MapSource getSelectedMapSource() {
		return (MapSource) mapSourceCombo.getSelectedItem();
	}

	public SelectedZoomLevels getSelectedZoomLevels() {
		return new SelectedZoomLevels(cbZoom);
	}

	public void selectNextMapSource() {
		if (mapSourceCombo.getSelectedIndex() == mapSourceCombo.getItemCount() - 1) {
			Toolkit.getDefaultToolkit().beep();
		} else {
			mapSourceCombo.setSelectedIndex(mapSourceCombo.getSelectedIndex() + 1);
		}
	}

	public void selectPreviousMapSource() {
		if (mapSourceCombo.getSelectedIndex() == 0) {
			Toolkit.getDefaultToolkit().beep();
		} else {
			mapSourceCombo.setSelectedIndex(mapSourceCombo.getSelectedIndex() - 1);
		}
	}

	public void mapSourceChanged(MapSource newMapSource) {
		// TODO update selected area if new map source has different projectionCategory
		calculateNrOfTilesToDownload();
		// if (newMapSource != null && newMapSource.equals(mapSourceCombo.getSelectedItem()))
		// return;
		mapSourceCombo.setSelectedItem(newMapSource);
	}

	public void mapSelectionControllerChanged(JMapController newMapController) {
		smPolygon.setSelected(false);
		smCircle.setSelected(false);
		smRectangle.setSelected(false);
		if (newMapController instanceof PolygonSelectionMapController)
			smPolygon.setSelected(true);
		else if (newMapController instanceof PolygonCircleSelectionMapController)
			smCircle.setSelected(true);
		else if (newMapController instanceof RectangleSelectionMapController)
			smRectangle.setSelected(true);
	}

	private void setSelectionByEnteredCoordinates() {
		coordinatesPanel.correctMinMax();
		MapSelection ms = coordinatesPanel.getMapSelection(previewMap.getMapSource());
		if (ms.isAreaSelected()) {
			mapSelectionMax = ms.getBottomRightPixelCoordinate();
			mapSelectionMin = ms.getTopLeftPixelCoordinate();
			previewMap.setSelectionAndZoomTo(ms, false);
		} else {
			Toolkit.getDefaultToolkit().beep();
		}
	}

	public MapSelection getMapSelectionCoordinates() {
		if (mapSelectionMax == null || mapSelectionMin == null)
			return null;
		return new MapSelection(previewMap.getMapSource(), mapSelectionMax, mapSelectionMin);
	}

	public TileImageParameters getSelectedTileImageParameters() {
		return tileImageParametersPanel.getSelectedTileImageParameters();
	}

	private void calculateNrOfTilesToDownload() {
		MapSelection ms = getMapSelectionCoordinates();
		String baseText;
		baseText = " %s tiles ";
		if (ms == null || !ms.isAreaSelected()) {
			amountOfTilesLabel.setText(String.format(baseText, "0"));
			amountOfTilesLabel.setToolTipText("");
		} else {
			try {
				SelectedZoomLevels sZL = new SelectedZoomLevels(cbZoom);

				int[] zoomLevels = sZL.getZoomLevels();

				long totalNrOfTiles = 0;

				StringBuilder hint = new StringBuilder(1024);
				hint.append("Total amount of tiles to download:");
				for (int i = 0; i < zoomLevels.length; i++) {
					int zoom = zoomLevels[i];
					long[] info = ms.calculateNrOfTilesEx(zoom);
					totalNrOfTiles += info[0];
					hint.append("<br>Level " + zoomLevels[i] + ": " + info[0] + " (" + info[1] + "*" + info[2] + ")");
				}
				String hintText = "<html>" + hint.toString() + "</html>";
				amountOfTilesLabel.setText(String.format(baseText, Long.toString(totalNrOfTiles)));
				amountOfTilesLabel.setToolTipText(hintText);
			} catch (Exception e) {
				amountOfTilesLabel.setText(String.format(baseText, "?"));
				log.error("", e);
			}
		}
	}

	public AtlasInterface getAtlas() {
		return jAtlasTree.getAtlas();
	}

	private class WindowDestroyer extends WindowAdapter {

		@Override
		public void windowOpened(WindowEvent e) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					previewMap.setEnabled(true);
				}
			});
		}

		public void windowClosing(WindowEvent event) {
			saveSettings();
		}
	}

	/**
	 * Saves the window position and size when window is moved or resized. This is necessary because of the maximized
	 * state. If a window is maximized it is impossible to retrieve the window size & position of the non-maximized
	 * window - therefore we have to collect the information every time they change.
	 */
	private class MainWindowListener extends ComponentAdapter {
		public void componentResized(ComponentEvent event) {
			// log.debug(event.paramString());
			updateValues();
		}

		public void componentMoved(ComponentEvent event) {
			// log.debug(event.paramString());
			updateValues();
		}

		private void updateValues() {
			// only update old values while window is in NORMAL state
			// Note(Java bug): Sometimes getExtendedState() says the window is
			// not maximized but maximizing is already in progress and therefore
			// the window bounds are already changed.
			if ((getExtendedState() & MAXIMIZED_BOTH) != 0)
				return;
			Settings s = Settings.getInstance();
			s.mainWindow.size = getSize();
			s.mainWindow.position = getLocation();
		}
	}

}
