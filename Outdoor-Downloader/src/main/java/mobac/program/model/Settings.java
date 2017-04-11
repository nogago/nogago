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
package mobac.program.model;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import javax.swing.JOptionPane;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import mobac.gui.actions.GpxLoad;
import mobac.gui.panels.JCoordinatesPanel;
import mobac.mapsources.MapSourcesManager;
import mobac.program.DirectoryManager;
import mobac.program.ProgramInfo;
import mobac.utilities.Utilities;
import mobac.utilities.stream.ThrottledInputStream;

import org.apache.log4j.Logger;

@XmlRootElement
@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
public class Settings {

	private static Logger log = Logger.getLogger(Settings.class);
	private static Settings instance = new Settings();

	public static final File FILE = new File(DirectoryManager.userSettingsDir, "settings.xml");

	private static long SETTINGS_LAST_MODIFIED = 0;

	private static final String SYSTEM_PROXY_HOST = System.getProperty("http.proxyHost");
	private static final String SYSTEM_PROXY_PORT = System.getProperty("http.proxyPort");

	@XmlElement(defaultValue = "")
	private String version;

	public int maxMapSize = 65536;

	public boolean tileStoreEnabled = true;

	/**
	 * Mapview related settings
	 */
	public int mapviewZoom = 3;
	public int mapviewGridZoom = -1;
	public EastNorthCoordinate mapviewCenterCoordinate = new EastNorthCoordinate(50, 9);

	public Point mapviewSelectionMax = null;
	public Point mapviewSelectionMin = null;
	@XmlElementWrapper(name = "selectedZoomLevels")
	@XmlElement(name = "zoomLevel")
	public List<Integer> selectedZoomLevels = null;

	@XmlElement(nillable = false)
	public String mapviewMapSource = null;

	public String elementName = "Layer name";

	private String userAgent = null;

	public int downloadThreadCount = 2;
	public int downloadRetryCount = 1;

	private boolean customTileProcessing = false;
	private Dimension tileSize = new Dimension(256, 256);
	private TileImageFormat tileImageFormat = TileImageFormat.PNG;

	public CoordinateStringFormat coordinateNumberFormat = CoordinateStringFormat.DEG_LOCAL;

	public final Directories directories = new Directories();

	public static class Directories {
		@XmlElement
		private String atlasOutputDirectory = null;

		@XmlElement
		public String tileStoreDirectory;

		@XmlElement
		private String mapSourcesDirectory;
	}

	@XmlElementWrapper(name = "placeBookmarks")
	@XmlElement(name = "bookmark")
	public List<Bookmark> placeBookmarks = new ArrayList<Bookmark>();

	/**
	 * Connection timeout in seconds (default 10 seconds)
	 */
	public int httpConnectionTimeout = 10;

	/**
	 * Read timeout in seconds (default 10 seconds)
	 */
	public int httpReadTimeout = 10;

	/**
	 * Maximum expiration (in milliseconds) acceptable. If a server sets an expiration time larger than this value it is
	 * truncated to this value on next download.
	 */
	public long tileMaxExpirationTime = TimeUnit.DAYS.toMillis(365);

	/**
	 * Minimum expiration (in milliseconds) acceptable. If a server sets an expiration time smaller than this value it
	 * is truncated to this value on next download.
	 */
	public long tileMinExpirationTime = TimeUnit.DAYS.toMillis(5);

	/**
	 * Expiration time (in milliseconds) of a tile if the server does not provide an expiration time
	 */
	public long tileDefaultExpirationTime = TimeUnit.DAYS.toMillis(28);

	public String googleLanguage = "en";
	public String osmHikingTicket = "";

	/**
	 * Development mode enabled/disabled
	 * <p>
	 * In development mode one additional map source is available for using MOBAC Debug TileServer
	 * </p>
	 */
	@XmlElement
	public boolean devMode = false;

	/**
	 * Saves the last used directory of the GPX file chooser dialog. Used in {@link GpxLoad}.
	 */
	public String gpxFileChooserDir = "";

	public final AtlasFormatSpecificSettings atlasFormatSpecificSettings = new AtlasFormatSpecificSettings();

	public static class AtlasFormatSpecificSettings {

		@XmlElement
		public Integer garminCustomMaxMapCount = 100;
	}

	public final MainWindowSettings mainWindow = new MainWindowSettings();

	public static class MainWindowSettings {
		public Dimension size = new Dimension();
		public Point position = new Point(-1, -1);
		public boolean maximized = true;

		public boolean leftPanelVisible = true;
		public boolean rightPanelVisible = true;

		@XmlElementWrapper(name = "collapsedPanels")
		@XmlElement(name = "collapsedPanel")
		public Vector<String> collapsedPanels = new Vector<String>();
	}

	/**
	 * Network settings
	 */
	private ProxyType proxyType = ProxyType.CUSTOM;
	private String customProxyHost = "";
	private String customProxyPort = "";
	private String customProxyUserName = "";
	private String customProxyPassword = "";
	private long bandwidthLimit = 0;

	@XmlElementWrapper(name = "mapSourcesDisabled")
	@XmlElement(name = "mapSource")
	public Vector<String> mapSourcesDisabled = new Vector<String>();

	@XmlElementWrapper(name = "mapSourcesEnabled")
	@XmlElement(name = "mapSource")
	public Vector<String> mapSourcesEnabled = new Vector<String>();

	@XmlElement(name = "MapSourcesUpdate")
	public final MapSourcesUpdate mapSourcesUpdate = new MapSourcesUpdate();

	public static class MapSourcesUpdate {
		/**
		 * Last ETag value retrieved while online map source update.
		 * 
		 * @see MapSourcesManager#mapsourcesOnlineUpdate()
		 * @see http://en.wikipedia.org/wiki/HTTP_ETag
		 */
		public String etag;

		public Date lastUpdate;
	}

	public transient UnitSystem unitSystem = UnitSystem.Metric;

	public final SettingsPaperAtlas paperAtlas = new SettingsPaperAtlas();
	public final SettingsWgsGrid wgsGrid = new SettingsWgsGrid();

	public boolean ignoreDlErrors = false;

	private Settings() {
		Dimension dScreen = Toolkit.getDefaultToolkit().getScreenSize();
		mainWindow.size.width = (int) (0.9f * dScreen.width);
		mainWindow.size.height = (int) (0.9f * dScreen.height);
		mainWindow.collapsedPanels.add(JCoordinatesPanel.NAME);
		mainWindow.collapsedPanels.add("Gpx");
	}

	public static Settings getInstance() {
		return instance;
	}

	public static void load() throws JAXBException {
		try {
			JAXBContext context = JAXBContext.newInstance(Settings.class);
			Unmarshaller um = context.createUnmarshaller();
			um.setEventHandler(new ValidationEventHandler() {

				public boolean handleEvent(ValidationEvent event) {

					log.warn("Problem on loading settings.xml: " + event.getMessage());
					return true;
				}
			});
			instance = (Settings) um.unmarshal(FILE);
			instance.wgsGrid.checkValues();
			instance.paperAtlas.checkValues();
			SETTINGS_LAST_MODIFIED = FILE.lastModified();
		} finally {
			Settings s = getInstance();
			s.applyProxySettings();
		}
	}

	public static boolean checkSettingsFileModified() {
		if (SETTINGS_LAST_MODIFIED == 0)
			return false;
		// Check if the settings.xml has been modified
		// since it has been loaded
		long lastModified = FILE.lastModified();
		return (SETTINGS_LAST_MODIFIED != lastModified);
	}

	public static void save() throws JAXBException {
		getInstance().version = ProgramInfo.getVersion();
		JAXBContext context = JAXBContext.newInstance(Settings.class);
		Marshaller m = context.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		ByteArrayOutputStream bo = null;
		FileOutputStream fo = null;
		try {
			// First we write to a buffer and if that works be write the buffer
			// to disk. Direct writing to file may result in an defect xml file
			// in case of an error
			bo = new ByteArrayOutputStream();
			m.marshal(getInstance(), bo);
			fo = new FileOutputStream(FILE);
			fo.write(bo.toByteArray());
			fo.close();
			SETTINGS_LAST_MODIFIED = FILE.lastModified();
		} catch (IOException e) {
			throw new JAXBException(e);
		} finally {
			Utilities.closeStream(fo);
		}
	}

	public static void loadOrQuit() {
		try {
			load();
		} catch (JAXBException e) {
			log.error(e);
			JOptionPane.showMessageDialog(null, "Could not read file settings.xml program will exit.", "Error",
					JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	}

	public String getUserAgent() {
		if (userAgent != null)
			return userAgent;
		else
			return ProgramInfo.getUserAgent();
	}

	public void setUserAgent(String userAgent) {
		if (userAgent != null) {
			userAgent = userAgent.trim();
			if (userAgent.length() == 0)
				userAgent = null;
		}
		this.userAgent = userAgent;
	}

	public boolean isCustomTileSize() {
		return customTileProcessing;
	}

	public void setCustomTileSize(boolean customTileSize) {
		this.customTileProcessing = customTileSize;
	}

	public Dimension getTileSize() {
		return tileSize;
	}

	public void setTileSize(Dimension tileSize) {
		this.tileSize = tileSize;
	}

	public TileImageFormat getTileImageFormat() {
		return tileImageFormat;
	}

	public void setTileImageFormat(TileImageFormat tileImageFormat) {
		this.tileImageFormat = tileImageFormat;
	}

	public ProxyType getProxyType() {
		return proxyType;
	}

	public void setProxyType(ProxyType proxyType) {
		this.proxyType = proxyType;
	}

	public String getCustomProxyHost() {
		return customProxyHost;
	}

	public String getCustomProxyPort() {
		return customProxyPort;
	}

	public void setCustomProxyHost(String proxyHost) {
		this.customProxyHost = proxyHost;
	}

	public void setCustomProxyPort(String proxyPort) {
		this.customProxyPort = proxyPort;
	}

	public String getCustomProxyUserName() {
		return customProxyUserName;
	}

	public void setCustomProxyUserName(String customProxyUserName) {
		this.customProxyUserName = customProxyUserName;
	}

	public String getCustomProxyPassword() {
		return customProxyPassword;
	}

	public void setCustomProxyPassword(String customProxyPassword) {
		this.customProxyPassword = customProxyPassword;
	}

	public void applyProxySettings() {
		boolean useSystemProxies = false;
		String newProxyHost = null;
		String newProxyPort = null;
		Authenticator newAuthenticator = null;
		switch (proxyType) {
		case SYSTEM:
			log.info("Applying proxy configuration: system settings");
			useSystemProxies = true;
			break;
		case APP_SETTINGS:
			newProxyHost = SYSTEM_PROXY_HOST;
			newProxyPort = SYSTEM_PROXY_PORT;
			log.info("Applying proxy configuration: host=" + newProxyHost + " port=" + newProxyPort);
			break;
		case CUSTOM:
			newProxyHost = customProxyHost;
			newProxyPort = customProxyPort;
			log.info("Applying proxy configuration: host=" + newProxyHost + " port=" + newProxyPort);
			break;
		case CUSTOM_W_AUTH:
			newProxyHost = customProxyHost;
			newProxyPort = customProxyPort;
			newAuthenticator = new Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(customProxyUserName, customProxyPassword.toCharArray());
				}
			};
			log.info("Applying proxy configuration: host=" + newProxyHost + " port=" + newProxyPort + " user="
					+ customProxyUserName);
			break;
		}
		Utilities.setHttpProxyHost(newProxyHost);
		Utilities.setHttpProxyPort(newProxyPort);
		Authenticator.setDefault(newAuthenticator);
		System.setProperty("java.net.useSystemProxies", Boolean.toString(useSystemProxies));
	}

	public long getBandwidthLimit() {
		return bandwidthLimit;
	}

	public void setBandwidthLimit(long bandwidthLimit) {
		this.bandwidthLimit = bandwidthLimit;
		ThrottledInputStream.setBandwidth(bandwidthLimit);
	}

	@XmlElement
	public void setUnitSystem(UnitSystem unitSystem) {
		if (unitSystem == null)
			unitSystem = UnitSystem.Metric;
		this.unitSystem = unitSystem;
	}

	public UnitSystem getUnitSystem() {
		return unitSystem;
	}

	@XmlTransient
	public File getMapSourcesDirectory() {
		String mapSourcesDirCfg = directories.mapSourcesDirectory;
		File mapSourcesDir;
		if (mapSourcesDirCfg == null || mapSourcesDirCfg.trim().length() == 0)
			mapSourcesDir = DirectoryManager.mapSourcesDir;
		else
			mapSourcesDir = new File(mapSourcesDirCfg);
		return mapSourcesDir;
	}

	@XmlTransient
	public File getAtlasOutputDirectory() {
		if (directories.atlasOutputDirectory != null)
			return new File(directories.atlasOutputDirectory);
		return new File(DirectoryManager.currentDir, "atlases");
	}

	public String getAtlasOutputDirectoryString() {
		if (directories.atlasOutputDirectory == null)
			return "";
		return directories.atlasOutputDirectory;
	}

	/**
	 * 
	 * @param dir
	 *            <code>null</code> or empty string resets to default directory otherwise set the new atlas output
	 *            directory.
	 */
	public void setAtlasOutputDirectory(String dir) {
		if (dir != null && dir.trim().length() == 0)
			dir = null;
		directories.atlasOutputDirectory = dir;
	}

	public String getVersion() {
		return version;
	}

}
