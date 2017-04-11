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
package mobac.program;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Locale;

import javax.swing.JOptionPane;

import mobac.mapsources.MapSourcesManager;
import mobac.program.interfaces.MapSource;
import mobac.program.model.Atlas;
import mobac.program.model.EastNorthCoordinate;
import mobac.program.model.Layer;
import mobac.program.model.Profile;
import mobac.program.model.Settings;
import mobac.utilities.GUIExceptionHandler;
import mobac.utilities.Utilities;
import mobac.utilities.file.FileExtFilter;
import mobac.utilities.file.NamePatternFileFilter;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

/**
 * Creates the necessary files on first time Mobile Atlas Creator is started or tries to update the environment if the
 * version has changed.
 */
public class EnvironmentSetup {

	private static boolean FIRST_START = false;

	public static Logger log = Logger.getLogger(EnvironmentSetup.class);

	public static void checkMemory() {
		Runtime r = Runtime.getRuntime();
		long maxHeap = r.maxMemory();
		String heapMBFormatted = String.format(Locale.ENGLISH, "%3.2f MiB", maxHeap / 1048576d);
		log.info("Total available memory to MOBAC: " + heapMBFormatted);
		if (maxHeap < 200000000) {
			String msg = "<html><b>WARNING:</b> Mobile Atlas Creator has been started "
					+ "with a very small amount of memory assigned.<br>"
					+ "The current maximum usable amount of memory to Mobile Atlas Creator is <b>" + heapMBFormatted
					+ "</b>.<br><br>Please make sure to start Mobile Atlas Creator in "
					+ "the future via the provided start scripts <i>Mobile Atlas Creator.exe</i><br>"
					+ "on Windows or <i>start.sh</i> on Linux/Unix/OSX or add the "
					+ "parameter <b>-Xmx 512M</b> to your startup command.<br><br>"
					+ "Example: <i>java -Xmx512M -jar Mobile_Atlas_Creator.jar</i><br>"
					+ "<br><center>Press OK to continue and start Mobile Atlas Creator</center></html>";
			JOptionPane.showMessageDialog(null, msg, "Warning: low memory", JOptionPane.WARNING_MESSAGE);
		}
	}

	public static void upgrade() {
		FileFilter ff = new NamePatternFileFilter("tac-profile-.*.xml");
		File profilesDir = DirectoryManager.currentDir;
		File[] files = profilesDir.listFiles(ff);
		for (File f : files) {
			File dest = new File(profilesDir, f.getName().replaceFirst("tac-", "mobac-"));
			f.renameTo(dest);
		}
	}

	/**
	 * In case the <tt>mapsources</tt> directory has been moved by configuration (directories.ini or settings.xml) we
	 * need to copy the existing map packs into the configured directory
	 */
	public static void copyMapPacks() {
		File userMapSourcesDir = Settings.getInstance().getMapSourcesDirectory();
		File progMapSourcesDir = new File(DirectoryManager.programDir, "mapsources");
		if (userMapSourcesDir.equals(progMapSourcesDir))
			return; // no user specific directory configured
		if (userMapSourcesDir.isDirectory())
			return; // directory already exists - map packs should have been already copied
		try {
			Utilities.mkDirs(userMapSourcesDir);
			FileUtils.copyDirectory(progMapSourcesDir, userMapSourcesDir, new FileExtFilter(".jar"));
		} catch (IOException e) {
			log.error(e);
			JOptionPane.showMessageDialog(null, "Error on initializing mapsources directory:\n" + e.getMessage(),
					"Error", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
	}

	public static void checkFileSetup() {
		checkDirectory(DirectoryManager.userSettingsDir, "user settings", true);
		checkDirectory(DirectoryManager.atlasProfilesDir, "atlas profile", true);
		checkDirectory(DirectoryManager.tileStoreDir, "tile store", true);
		checkDirectory(DirectoryManager.tempDir, "temporary atlas download", true);
		if (!Settings.FILE.exists()) {
			try {
				FIRST_START = true;
				Settings.save();
			} catch (Exception e) {
				log.error("Error while creating settings.xml: " + e.getMessage(), e);
				String[] options = { "Exit", "Show error report" };
				int a = JOptionPane.showOptionDialog(null, "Could not create file settings.xml - program will exit.",
						"Error", 0, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
				if (a == 1)
					GUIExceptionHandler.showExceptionDialog(e);
				System.exit(1);
			}
		}
	}

	protected static void checkDirectory(File dir, String dirName, boolean checkIsWriteable) {
		try {
			Utilities.mkDirs(dir);
		} catch (IOException e) {
			GUIExceptionHandler.processFatalExceptionSimpleDialog("Error while creating " + dirName + " directory\n"
					+ dir.getAbsolutePath() + "\nProgram will exit.", e);
		}
		if (!checkIsWriteable)
			return;
		try {
			// test if we can write into that directory
			File testFile = File.createTempFile("MOBAC", "", dir);
			testFile.createNewFile();
			testFile.deleteOnExit();
			testFile.delete();
		} catch (IOException e) {
			GUIExceptionHandler.processFatalExceptionSimpleDialog(
					"Unable to write to " + dirName + "\n" + dir.getAbsolutePath()
							+ "\nPlease correct file permissions and restart MOBAC", e);
		}
	}

	public static void createDefaultAtlases() {
		if (!FIRST_START)
			return;
		Profile p = new Profile("Google Maps New York");
		Atlas atlas = Atlas.newInstance();
		try {
			EastNorthCoordinate max = new EastNorthCoordinate(40.97264, -74.142609);
			EastNorthCoordinate min = new EastNorthCoordinate(40.541982, -73.699036);
			Layer layer = new Layer(atlas, "GM New York");
			MapSource ms = MapSourcesManager.getInstance().getSourceByName("Mapnik");
			if (ms == null)
				return;
			layer.addMapsAutocut("GM New York 16", ms, max, min, 16, null, 32000);
			layer.addMapsAutocut("GM New York 14", ms, max, min, 14, null, 32000);
			atlas.addLayer(layer);
			p.save(atlas);
		} catch (Exception e) {
			log.error("Creation for example profiles failed", e);
			GUIExceptionHandler.showExceptionDialog(e);
		}
	}
}
