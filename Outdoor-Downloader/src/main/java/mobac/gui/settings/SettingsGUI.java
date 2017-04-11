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
package mobac.gui.settings;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.bind.JAXBException;

import mobac.StartMOBAC;
import mobac.exceptions.UpdateFailedException;
import mobac.gui.MainGUI;
import mobac.gui.actions.OpenInWebbrowser;
import mobac.gui.components.JDirectoryChooser;
import mobac.gui.components.JMapSizeCombo;
import mobac.gui.components.JTimeSlider;
import mobac.mapsources.DefaultMapSourcesManager;
import mobac.mapsources.MapSourcesManager;
import mobac.mapsources.loader.MapPackManager;
import mobac.program.Logging;
import mobac.program.ProgramInfo;
import mobac.program.interfaces.MapSource;
import mobac.program.model.MapSourcesListModel;
import mobac.program.model.ProxyType;
import mobac.program.model.Settings;
import mobac.program.model.UnitSystem;
import mobac.program.tilestore.TileStore;
import mobac.utilities.GBC;
import mobac.utilities.GUIExceptionHandler;
import mobac.utilities.Utilities;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class SettingsGUI extends JDialog {
	private static final long serialVersionUID = -5227934684609357198L;

	public static Logger log = Logger.getLogger(SettingsGUI.class);

	private static final Integer[] THREADCOUNT_LIST = { 1, 2, 4, 6 };

	private static final long MBIT1 = 1000000 / 8;

	private enum Bandwidth {
		UNLIMUTED("Unlimited", 0), MBit1("1 MBit", MBIT1), MBit5("5 MBit", MBIT1 * 5), MBit10("10 MBit", MBIT1 * 10), MBit15(
				"15 MBit", MBIT1 * 15), MBit20("20 MBit", MBIT1 * 20);

		public final long limit;
		public final String description;

		private Bandwidth(String description, long limit) {
			this.description = description;
			this.limit = limit;
		}

		@Override
		public String toString() {
			return description;
		}
	};

	private final Settings settings = Settings.getInstance();

	private JComboBox unitSystem;

	private JButton mapSourcesOnlineUpdate;
	private JTextField osmHikingTicket;

	private SettingsGUITileStore tileStoreTab;

	private JTimeSlider defaultExpirationTime;
	private JTimeSlider minExpirationTime;
	private JTimeSlider maxExpirationTime;

	private JMapSizeCombo mapSize;

	private JTextField atlasOutputDirectory;

	private JComboBox threadCount;
	private JComboBox bandwidth;

	private JComboBox proxyType;
	private JTextField proxyHost;
	private JTextField proxyPort;

	private JTextField proxyUserName;
	private JTextField proxyPassword;

	private JCheckBox ignoreDlErrors;

	private JButton okButton;
	private JButton cancelButton;

	private JTabbedPane tabbedPane;

	private JList enabledMapSources;

	private MapSourcesListModel enabledMapSourcesModel;

	private JList disabledMapSources;

	private MapSourcesListModel disabledMapSourcesModel;

	private final SettingsGUIPaper paperAtlas;
	private final SettingsGUIWgsGrid display;

	public static void showSettingsDialog(final JFrame owner) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new SettingsGUI(owner);
			}
		});
	}

	private SettingsGUI(JFrame owner) {
		super(owner);
		setIconImages(MainGUI.MOBAC_ICONS);
		GUIExceptionHandler.registerForCurrentThread();
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setModal(true);
		setMinimumSize(new Dimension(300, 300));

		paperAtlas = new SettingsGUIPaper();
		display = new SettingsGUIWgsGrid();

		createJFrame();
		createTabbedPane();
		createJButtons();
		loadSettings();
		addListeners();
		pack();
		// don't allow shrinking, but allow enlarging
		setMinimumSize(getSize());
		Dimension dScreen = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((dScreen.width - getWidth()) / 2, (dScreen.height - getHeight()) / 2);
		setVisible(true);
	}

	private void createJFrame() {
		setLayout(new BorderLayout());
		setTitle("Settings");
	}

	// Create tabbed pane
	public void createTabbedPane() {
		tabbedPane = new JTabbedPane();
		tabbedPane.setBounds(0, 0, 492, 275);
		addDisplaySettingsPanel();
		try {
			addMapSourceSettingsPanel();
		} catch (URISyntaxException e) {
			log.error("", e);
		}
		addMapSourceManagerPanel();
		addTileUpdatePanel();
		tileStoreTab = new SettingsGUITileStore(this);
		addMapSizePanel();
		addDirectoriesPanel();
		addNetworkPanel();
		tabbedPane.addTab(paperAtlas.getName(), paperAtlas);

		add(tabbedPane, BorderLayout.CENTER);
	}

	private JPanel createNewTab(String tabTitle) {
		JPanel tabPanel = new JPanel();
		addTab(tabTitle, tabPanel);
		return tabPanel;
	}

	protected void addTab(String tabTitle, JPanel tabPanel) {
		tabPanel.setName(tabTitle);
		tabPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		tabbedPane.add(tabPanel, tabTitle);
	}

	private void addDisplaySettingsPanel() {
		JPanel tab = createNewTab("Display");
		tab.setLayout(new GridBagLayout());

		JPanel unitSystemPanel = new JPanel(new GridBagLayout());
		unitSystemPanel.setBorder(createSectionBorder("Unit System"));

		UnitSystem[] us = UnitSystem.values();
		unitSystem = new JComboBox(us);
		unitSystemPanel.add(new JLabel("Unit system for map scale bar: "), GBC.std());
		unitSystemPanel.add(unitSystem, GBC.std());
		unitSystemPanel.add(Box.createHorizontalGlue(), GBC.eol().fill(GBC.HORIZONTAL));
		tab.add(unitSystemPanel, GBC.eol().fill(GBC.HORIZONTAL));
		tab.add(display, GBC.eol().fill(GBC.HORIZONTAL));
		tab.add(Box.createVerticalGlue(), GBC.std().fill(GBC.VERTICAL));
	}

	private void addMapSourceSettingsPanel() throws URISyntaxException {

		JPanel tab = createNewTab("Map sources config");
		tab.setLayout(new GridBagLayout());

		JPanel updatePanel = new JPanel(new GridBagLayout());
		updatePanel.setBorder(createSectionBorder("Map packs online update"));

		mapSourcesOnlineUpdate = new JButton("Perform online update");
		mapSourcesOnlineUpdate.addActionListener(new MapPacksOnlineUpdateAction());
		updatePanel.add(mapSourcesOnlineUpdate, GBC.std());

		JPanel osmHikingPanel = new JPanel(new GridBagLayout());
		osmHikingPanel.setBorder(createSectionBorder("Reit- und Wanderkarte ($Abo)"));

		osmHikingTicket = new JTextField(20);

		osmHikingPanel.add(new JLabel("Purchased ticket ID:"), GBC.std());
		osmHikingPanel.add(osmHikingTicket, GBC.std().insets(2, 0, 10, 0));
		JLabel osmHikingTicketUrl = new JLabel("<html><u>How to get a ticket (German)</u></html>");
		osmHikingTicketUrl.addMouseListener(new OpenInWebbrowser("http://www.wanderreitkarte.de/shop_abo_de.php"));
		osmHikingPanel.add(osmHikingTicketUrl, GBC.eol());

		tab.add(updatePanel, GBC.eol().fill(GBC.HORIZONTAL));
		tab.add(osmHikingPanel, GBC.eol().fill(GBC.HORIZONTAL));
		tab.add(Box.createVerticalGlue(), GBC.eol().fill(GBC.VERTICAL));
	}

	private void addMapSourceManagerPanel() {
		JPanel tab = createNewTab("Map sources");
		tab.setLayout(new GridBagLayout());

		JPanel leftPanel = new JPanel(new BorderLayout());
		leftPanel.setBorder(createSectionBorder("Enabled Map Sources"));

		JPanel centerPanel = new JPanel(new GridBagLayout());
		JPanel rightPanel = new JPanel(new BorderLayout());
		rightPanel.setBorder(createSectionBorder("Disabled Map Sources"));

		JButton up = new JButton(Utilities.loadResourceImageIcon("arrow_blue_up.png"));
		up.setToolTipText("<html>Move selected enabled map<br>source(s) one positon up</html>");
		JButton down = new JButton(Utilities.loadResourceImageIcon("arrow_blue_down.png"));
		down.setToolTipText("<html>Move selected enabled map<br>source(s) one positon down</html>");
		JButton toLeft = new JButton(Utilities.loadResourceImageIcon("arrow_blue_left.png"));
		toLeft.setToolTipText("<html>Enable the selected disabled map source(s)</html>");
		JButton toRight = new JButton(Utilities.loadResourceImageIcon("arrow_blue_right.png"));
		toRight.setToolTipText("<html>Disable the selected enabled map source(s)</html>");
		Insets buttonInsets = new Insets(4, 4, 4, 4);
		Dimension buttonDimension = new Dimension(40, 40);
		up.setPreferredSize(buttonDimension);
		down.setPreferredSize(buttonDimension);
		toLeft.setPreferredSize(buttonDimension);
		toRight.setPreferredSize(buttonDimension);
		up.setMargin(buttonInsets);
		down.setMargin(buttonInsets);
		toLeft.setMargin(buttonInsets);
		toRight.setMargin(buttonInsets);

		toLeft.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				int[] idx = disabledMapSources.getSelectedIndices();
				for (int i = 0; i < idx.length; i++) {
					MapSource ms = disabledMapSourcesModel.removeElement(idx[i] - i);
					enabledMapSourcesModel.addElement(ms);
				}
			}
		});
		toRight.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				int[] idx = enabledMapSources.getSelectedIndices();
				for (int i = 0; i < idx.length; i++) {
					MapSource ms = enabledMapSourcesModel.removeElement(idx[i] - i);
					disabledMapSourcesModel.addElement(ms);
				}
				disabledMapSourcesModel.sort();
			}
		});
		up.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				int[] idx = enabledMapSources.getSelectedIndices();
				if (idx.length == 0)
					return;
				for (int i = 0; i < idx.length; i++) {
					int index = idx[i];
					if (index == 0)
						return;
					if (enabledMapSourcesModel.moveUp(index))
						idx[i]--;
				}
				enabledMapSources.setSelectedIndices(idx);
				enabledMapSources.ensureIndexIsVisible(idx[0]);
			}
		});
		down.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				int[] idx = enabledMapSources.getSelectedIndices();
				if (idx.length == 0)
					return;
				for (int i = idx.length - 1; i >= 0; i--) {
					int index = idx[i];
					if (index == enabledMapSourcesModel.getSize() - 1)
						return;
					if (enabledMapSourcesModel.moveDown(index))
						idx[i]++;
				}
				enabledMapSources.setSelectedIndices(idx);
				enabledMapSources.ensureIndexIsVisible(idx[idx.length - 1]);
			}
		});
		GBC buttonGbc = GBC.eol();
		centerPanel.add(Box.createVerticalStrut(25), GBC.eol());
		centerPanel.add(toLeft, buttonGbc);
		centerPanel.add(toRight, buttonGbc);
		centerPanel.add(up, buttonGbc);
		centerPanel.add(down, buttonGbc);
		centerPanel.add(Box.createVerticalGlue(), GBC.std().fill());

		MapSourcesManager msManager = MapSourcesManager.getInstance();

		enabledMapSourcesModel = new MapSourcesListModel(msManager.getEnabledOrderedMapSources());
		enabledMapSources = new JList(enabledMapSourcesModel);
		JScrollPane leftScrollPane = new JScrollPane(enabledMapSources, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		leftPanel.add(leftScrollPane, BorderLayout.CENTER);

		disabledMapSourcesModel = new MapSourcesListModel(msManager.getDisabledMapSources());
		disabledMapSourcesModel.sort();
		disabledMapSources = new JList(disabledMapSourcesModel);
		JScrollPane rightScrollPane = new JScrollPane(disabledMapSources, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		rightPanel.add(rightScrollPane, BorderLayout.CENTER);

		JPanel mapSourcesInnerPanel = new JPanel();

		Color c = UIManager.getColor("List.background");
		mapSourcesInnerPanel.setBackground(c);

		GBC lr = GBC.std().fill();
		lr.weightx = 1.0;

		tab.add(leftPanel, lr);
		tab.add(centerPanel, GBC.std().fill(GBC.VERTICAL));
		tab.add(rightPanel, lr);
	}

	private void addTileUpdatePanel() {
		JPanel backGround = createNewTab("Tile update");
		backGround.setLayout(new GridBagLayout());

		ChangeListener sliderChangeListener = new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				JTimeSlider slider = ((JTimeSlider) e.getSource());
				long x = slider.getTimeSecondsValue();
				JPanel panel = (JPanel) slider.getParent();
				TitledBorder tb = (TitledBorder) panel.getBorder();
				tb.setTitle(panel.getName() + ": " + Utilities.formatDurationSeconds(x));
				panel.repaint();
			}
		};
		GBC gbc_ef = GBC.eol().fill(GBC.HORIZONTAL);

		JPanel defaultExpirationPanel = new JPanel(new GridBagLayout());
		defaultExpirationPanel.setName("Default expiration time");
		defaultExpirationPanel.setBorder(createSectionBorder(""));
		defaultExpirationTime = new JTimeSlider();
		defaultExpirationTime.addChangeListener(sliderChangeListener);
		JLabel descr = new JLabel("<html>The default exipration time is used for map sources that do not <br>"
				+ "provide an expiration time for each map tile.</html>", JLabel.CENTER);

		defaultExpirationPanel.add(descr, gbc_ef);
		defaultExpirationPanel.add(defaultExpirationTime, gbc_ef);

		JPanel maxExpirationPanel = new JPanel(new BorderLayout());
		maxExpirationPanel.setName("Maximum expiration time");
		maxExpirationPanel.setBorder(createSectionBorder(""));
		maxExpirationTime = new JTimeSlider();
		maxExpirationTime.addChangeListener(sliderChangeListener);
		maxExpirationPanel.add(maxExpirationTime, BorderLayout.CENTER);

		JPanel minExpirationPanel = new JPanel(new BorderLayout());
		minExpirationPanel.setName("Minimum expiration time");
		minExpirationPanel.setBorder(createSectionBorder(""));
		minExpirationTime = new JTimeSlider();
		minExpirationTime.addChangeListener(sliderChangeListener);
		minExpirationPanel.add(minExpirationTime, BorderLayout.CENTER);

		descr = new JLabel("<html>Tiles are updated automatically base on the settings below. "
				+ "Each map tile has <br>an expiry date that is sometimes provided by "
				+ "the server. If the server does <br> not provide one, the default expiration "
				+ "time is used.</html>", JLabel.CENTER);

		backGround.add(descr, gbc_ef);
		backGround.add(defaultExpirationPanel, gbc_ef);
		backGround.add(minExpirationPanel, gbc_ef);
		backGround.add(maxExpirationPanel, gbc_ef);
		backGround.add(Box.createVerticalGlue(), GBC.std().fill());
	}

	private void addMapSizePanel() {
		JPanel backGround = createNewTab("Map size");
		backGround.setLayout(new GridBagLayout());
		mapSize = new JMapSizeCombo();
		mapSize.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				log.trace("Map size: " + mapSize.getValue());
			}
		});

		JLabel mapSizeLabel = new JLabel("Maximum size (width & height) of rectangular maps: ");
		JLabel mapSizeText = new JLabel(
				"<html>If the area of the selected rectangular region to download "
						+ "is larger in height or width than <br>the map size it will be splitted into "
						+ "several maps <b>when adding the map selection</b>.<br>"
						+ "Each map is no larger than the specified maximum map size.<br>"
						+ "Note that polygonal maps are not affected by this setting!<br>"
						+ "You can see the number of maps and their region in the atlas content tree.<br>"
						+ "Changing the maximum map size after an area has been added the atlas has no effect on the atlas.<br><br>"
						+ "<b>Note for TrekBuddy users:</b><br>TrekBuddy versions before v0.9.88 "
						+ "do not support map sizes larger than 32767.<br>"
						+ "Newer versions can handle maps up to a size of 1048575.</html>");

		JPanel leftPanel = new JPanel(new GridBagLayout());
		leftPanel.setBorder(createSectionBorder("Map size settings"));

		GBC gbc = GBC.eol().insets(0, 5, 0, 5);
		leftPanel.add(mapSizeLabel, GBC.std());
		leftPanel.add(mapSize, GBC.eol());
		leftPanel.add(mapSizeText, gbc.fill(GBC.HORIZONTAL));
		leftPanel.add(Box.createVerticalGlue(), GBC.std().fill(GBC.VERTICAL));

		backGround.add(leftPanel, GBC.std().fill(GBC.HORIZONTAL).anchor(GBC.NORTHEAST));
		backGround.add(Box.createVerticalGlue(), GBC.std().fill(GBC.VERTICAL));
	}

	private void addDirectoriesPanel() {
		JPanel backGround = createNewTab("Directories");
		backGround.setLayout(new GridBagLayout());
		JPanel atlasOutputDirPanel = new JPanel(new GridBagLayout());
		atlasOutputDirPanel.setBorder(createSectionBorder("Atlas output directory"));

		atlasOutputDirectory = new JTextField();
		atlasOutputDirectory.setToolTipText("<html>If empty the default directory " + "is used: <br><tt>"
				+ settings.getAtlasOutputDirectory() + "</tt></html>");
		JButton selectAtlasOutputDirectory = new JButton("Select");
		selectAtlasOutputDirectory.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				JDirectoryChooser dc = new JDirectoryChooser();
				dc.setCurrentDirectory(settings.getAtlasOutputDirectory());
				if (dc.showDialog(SettingsGUI.this, "Select Directory") != JFileChooser.APPROVE_OPTION)
					return;
				atlasOutputDirectory.setText(dc.getSelectedFile().getAbsolutePath());
			}
		});

		atlasOutputDirPanel.add(atlasOutputDirectory, GBC.std().fillH());
		atlasOutputDirPanel.add(selectAtlasOutputDirectory, GBC.std());

		backGround.add(atlasOutputDirPanel, GBC.eol().fillH());
		backGround.add(Box.createVerticalGlue(), GBC.eol().fill(GBC.VERTICAL));
	}

	private void addNetworkPanel() {
		JPanel backGround = createNewTab("Network");
		backGround.setLayout(new GridBagLayout());
		GBC gbc_eolh = GBC.eol().fill(GBC.HORIZONTAL);
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(createSectionBorder("Network connections"));
		threadCount = new JComboBox(THREADCOUNT_LIST);
		threadCount.setMaximumRowCount(THREADCOUNT_LIST.length);
		panel.add(threadCount, GBC.std().insets(5, 5, 5, 5).anchor(GBC.EAST));
		panel.add(new JLabel("Number of parallel network connections for tile downloading"),
				GBC.eol().fill(GBC.HORIZONTAL));

		bandwidth = new JComboBox(Bandwidth.values());
		bandwidth.setMaximumRowCount(bandwidth.getItemCount());
		panel.add(bandwidth, GBC.std().insets(5, 5, 5, 5));
		panel.add(new JLabel("Bandwidth limitation for tile downloading"), GBC.eol().fill(GBC.HORIZONTAL));

		backGround.add(panel, gbc_eolh);

		// panel = new JPanel(new GridBagLayout());
		// panel.setBorder(createSectionBorder("HTTP User-Agent"));
		// backGround.add(panel, gbc_eolh);

		panel = new JPanel(new GridBagLayout());
		panel.setBorder(createSectionBorder("HTTP Proxy"));
		final JLabel proxyTypeLabel = new JLabel("Proxy settings: ");
		proxyType = new JComboBox(ProxyType.values());
		proxyType.setSelectedItem(settings.getProxyType());

		final JLabel proxyHostLabel = new JLabel("Proxy host name: ");
		proxyHost = new JTextField(settings.getCustomProxyHost());

		final JLabel proxyPortLabel = new JLabel("Proxy port: ");
		proxyPort = new JTextField(settings.getCustomProxyPort());

		final JLabel proxyUserNameLabel = new JLabel("Proxy user: ");
		proxyUserName = new JTextField(settings.getCustomProxyUserName());

		final JLabel proxyPasswordLabel = new JLabel("Proxy password: ");
		proxyPassword = new JTextField(settings.getCustomProxyPassword());

		ActionListener al = new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				boolean b = ProxyType.CUSTOM.equals(proxyType.getSelectedItem());
				boolean c = ProxyType.CUSTOM_W_AUTH.equals(proxyType.getSelectedItem());
				proxyHost.setEnabled(b || c);
				proxyPort.setEnabled(b || c);
				proxyHostLabel.setEnabled(b || c);
				proxyPortLabel.setEnabled(b || c);
				proxyUserName.setEnabled(c);
				proxyPassword.setEnabled(c);
				proxyUserNameLabel.setEnabled(c);
				proxyPasswordLabel.setEnabled(c);
			}
		};
		al.actionPerformed(null);
		proxyType.addActionListener(al);

		panel.add(proxyTypeLabel, GBC.std());
		panel.add(proxyType, gbc_eolh.insets(5, 2, 5, 2));

		panel.add(proxyHostLabel, GBC.std());
		panel.add(proxyHost, gbc_eolh);

		panel.add(proxyPortLabel, GBC.std());
		panel.add(proxyPort, gbc_eolh);

		panel.add(proxyUserNameLabel, GBC.std());
		panel.add(proxyUserName, gbc_eolh);

		panel.add(proxyPasswordLabel, GBC.std());
		panel.add(proxyPassword, gbc_eolh);

		backGround.add(panel, GBC.eol().fillH());

		ignoreDlErrors = new JCheckBox("Ignore download errors and continue automatically", settings.ignoreDlErrors);
		JPanel jPanel = new JPanel(new GridBagLayout());
		jPanel.setBorder(createSectionBorder("Default"));
		jPanel.add(ignoreDlErrors, GBC.std());
		jPanel.add(Box.createHorizontalGlue(), GBC.eol().fillH());
		backGround.add(jPanel, GBC.eol().fillH());

		backGround.add(Box.createVerticalGlue(), GBC.eol().fill(GBC.VERTICAL));
	}

	public void createJButtons() {
		JPanel buttonPanel = new JPanel(new GridBagLayout());
		okButton = new JButton("Ok");
		cancelButton = new JButton("Cancel");

		GBC gbc = GBC.std().insets(5, 5, 5, 5);
		buttonPanel.add(okButton, gbc);
		buttonPanel.add(cancelButton, gbc);
		add(buttonPanel, BorderLayout.SOUTH);
	}

	private void loadSettings() {
		Settings s = settings;

		unitSystem.setSelectedItem(s.unitSystem);
		tileStoreTab.tileStoreEnabled.setSelected(s.tileStoreEnabled);

		mapSize.setValue(s.maxMapSize);

		atlasOutputDirectory.setText(s.getAtlasOutputDirectoryString());

		long limit = s.getBandwidthLimit();
		for (Bandwidth b : Bandwidth.values()) {
			if (limit <= b.limit) {
				bandwidth.setSelectedItem(b);
				break;
			}
		}

		int index = Arrays.binarySearch(THREADCOUNT_LIST, s.downloadThreadCount);
		if (index < 0) {
			if (s.downloadThreadCount > THREADCOUNT_LIST[THREADCOUNT_LIST.length - 1])
				index = THREADCOUNT_LIST.length - 1;
			else
				index = 0;
		}
		threadCount.setSelectedIndex(index);

		defaultExpirationTime.setTimeMilliValue(s.tileDefaultExpirationTime);
		maxExpirationTime.setTimeMilliValue(s.tileMaxExpirationTime);
		minExpirationTime.setTimeMilliValue(s.tileMinExpirationTime);

		osmHikingTicket.setText(s.osmHikingTicket);

		ignoreDlErrors.setSelected(s.ignoreDlErrors);

		paperAtlas.loadSettings(s);
		display.loadSettings(s);
	}

	/**
	 * Reads the user defined settings from the gui and updates the {@link Settings} values according to the read gui
	 * settings.
	 */
	private void applySettings() {
		Settings s = settings;

		s.unitSystem = (UnitSystem) unitSystem.getSelectedItem();
		s.tileStoreEnabled = tileStoreTab.tileStoreEnabled.isSelected();
		s.tileDefaultExpirationTime = defaultExpirationTime.getTimeMilliValue();
		s.tileMinExpirationTime = minExpirationTime.getTimeMilliValue();
		s.tileMaxExpirationTime = maxExpirationTime.getTimeMilliValue();
		s.maxMapSize = mapSize.getValue();

		s.setAtlasOutputDirectory(atlasOutputDirectory.getText());
		int threads = ((Integer) threadCount.getSelectedItem()).intValue();
		s.downloadThreadCount = threads;

		s.setBandwidthLimit(((Bandwidth) bandwidth.getSelectedItem()).limit);

		s.setProxyType((ProxyType) proxyType.getSelectedItem());
		s.setCustomProxyHost(proxyHost.getText());
		s.setCustomProxyPort(proxyPort.getText());
		s.setCustomProxyUserName(proxyUserName.getText());
		s.setCustomProxyPassword(proxyPassword.getText());

		s.applyProxySettings();

		Vector<String> disabledMaps = new Vector<String>();
		for (MapSource ms : disabledMapSourcesModel.getVector()) {
			disabledMaps.add(ms.getName());
		}
		s.mapSourcesDisabled = disabledMaps;

		Vector<String> enabledMaps = new Vector<String>();
		for (MapSource ms : enabledMapSourcesModel.getVector()) {
			enabledMaps.add(ms.getName());
		}
		s.mapSourcesEnabled = enabledMaps;

		s.ignoreDlErrors = ignoreDlErrors.isSelected();

		paperAtlas.applySettings(s);
		display.applySettings(s);

		if (MainGUI.getMainGUI() == null)
			return;

		MainGUI.getMainGUI().updateMapSourcesList();

		s.osmHikingTicket = osmHikingTicket.getText().trim();
		try {
			MainGUI.getMainGUI().checkAndSaveSettings();
		} catch (Exception e) {
			log.error("Error saving settings to file", e);
			JOptionPane.showMessageDialog(null, "Error saving settings to file:\n" + e.toString() + " ("
					+ e.getClass().getSimpleName() + ")", "Error saving settings to file", JOptionPane.ERROR_MESSAGE);
		}

		MainGUI.getMainGUI().previewMap.repaint();
	}

	private void addListeners() {

		addWindowListener(new WindowCloseListener());

		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				applySettings();
				// Close the dialog window
				SettingsGUI.this.dispose();
			}
		});
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SettingsGUI.this.dispose();
			}
		});

		tabbedPane.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				if (tabbedPane.getSelectedComponent() == null)
					return;
				// First time the tile store tab is selected start updating the tile store information
				if ("Tile store".equals(tabbedPane.getSelectedComponent().getName())) {
					tabbedPane.removeChangeListener(this);
					tileStoreTab.updateTileStoreInfoPanelAsync(null);
				}
			}
		});

		KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
		Action escapeAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				SettingsGUI.this.dispose();
			}
		};
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeKeyStroke, "ESCAPE");
		getRootPane().getActionMap().put("ESCAPE", escapeAction);
	}

	public static final TitledBorder createSectionBorder(String title) {
		TitledBorder tb = BorderFactory.createTitledBorder(title);
		Border border = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
		Border margin = new EmptyBorder(3, 3, 3, 3);
		tb.setBorder(new CompoundBorder(border, margin));
		return tb;
	}

	private class WindowCloseListener extends WindowAdapter {

		@Override
		public void windowClosed(WindowEvent event) {
			// On close we check if the tile store information retrieval thread
			// is still running and if yes we interrupt it
			tileStoreTab.stopThread();
		}

	}

	private class MapPacksOnlineUpdateAction implements ActionListener {

		public void actionPerformed(ActionEvent event) {
			// try {
			// boolean result = MapSourcesUpdater.mapsourcesOnlineUpdate();
			// String msg = (result) ? "Online update successfull" : "No new update avilable";
			// DateFormat df = DateFormat.getDateTimeInstance();
			// Date date = MapSourcesUpdater.getMapSourcesDate(System.getProperties());
			// msg += "\nCurrent map source date: " + df.format(date);
			// JOptionPane.showMessageDialog(SettingsGUI.this, msg);
			// if (result)
			// MainGUI.getMainGUI().refreshPreviewMap();
			// } catch (MapSourcesUpdateException e) {
			// JOptionPane.showMessageDialog(SettingsGUI.this, e.getMessage(), "Mapsources online update failed",
			// JOptionPane.ERROR_MESSAGE);
			// }
			MapPackManager mpm;
			try {
				mpm = new MapPackManager(Settings.getInstance().getMapSourcesDirectory());
				int result = mpm.updateMapPacks();
				switch (result) {
				case -1:
					JOptionPane.showMessageDialog(SettingsGUI.this,
							"This version of MOBAC is no longer supported for online updates. "
									+ "Please upgrade to the most recent version.", "No updates available",
							JOptionPane.ERROR_MESSAGE);
					break;
				case 0:
					JOptionPane.showMessageDialog(SettingsGUI.this, "You have already the latest map packs installed.",
							"No updates available", JOptionPane.INFORMATION_MESSAGE);
					break;
				default:
					JOptionPane.showMessageDialog(SettingsGUI.this, result
							+ " map packs has been updated.\nPlease restart MOBAC for installing the updates.",
							"Updates has been downloaded", JOptionPane.INFORMATION_MESSAGE);
				}
			} catch (UpdateFailedException e) {
				JOptionPane.showMessageDialog(SettingsGUI.this, e.getMessage(), "Update failed",
						JOptionPane.ERROR_MESSAGE);
			} catch (Exception e) {
				Settings.getInstance().mapSourcesUpdate.etag = null;
				GUIExceptionHandler.processException(e);
			}
		}
	}

	public static void main(String[] args) {
		Logging.configureConsoleLogging(Level.TRACE);
		ProgramInfo.initialize();
		DefaultMapSourcesManager.initialize();
		TileStore.initialize();
		StartMOBAC.setLookAndFeel();

		try {
			Settings.load();
		} catch (JAXBException e1) {
			e1.printStackTrace();
		}
		new SettingsGUI(null);
		Runtime.getRuntime().addShutdownHook(new Thread() {

			@Override
			public void run() {
				try {
					Settings.save();
				} catch (JAXBException e) {
					e.printStackTrace();
				}
			}

		});
	}
}
