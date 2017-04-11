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
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import mobac.utilities.Utilities;

/**
 * Provides the five common directories used within Mobile Atlas Creator:
 * <ul>
 * <li>current directory</li>
 * <li>program directory</li>
 * <li>user home directory</li>
 * <li>user settings directory</li>
 * <li>temporary directory</li>
 * </ul>
 * 
 */
public class DirectoryManager {

	public static final File currentDir;
	public static final File programDir;
	public static final File userHomeDir;
	public static final File tempDir;
	public static final File userAppDataDir;

	public static final File userSettingsDir;
	public static final File mapSourcesDir;
	public static final File toolsDir;
	public static final File atlasProfilesDir;
	public static final File tileStoreDir;

	private static Properties dirConfig = null;

	static {
		currentDir = new File(System.getProperty("user.dir"));
		userHomeDir = new File(System.getProperty("user.home"));
		programDir = getProgramDir();
		loadDirectoriesIni();

		userAppDataDir = getUserAppDataDir();
		tempDir = applyDirConfig("mobac.tmpdir", new File(System.getProperty("java.io.tmpdir")));

		mapSourcesDir = applyDirConfig("mobac.mapsourcesdir", new File(programDir, "mapsources"));
		toolsDir = applyDirConfig("mobac.toolsdir", new File(programDir, "tools"));
		userSettingsDir = applyDirConfig("mobac.usersettingsdir", programDir);
		atlasProfilesDir = applyDirConfig("mobac.atlasprofilesdir", currentDir);
		tileStoreDir = applyDirConfig("mobac.tilestoredir", new File(programDir, "tilestore"));
	}

	private static File applyDirConfig(String propertyName, File defaultDir) {
		if (dirConfig == null)
			return defaultDir;
		try {
			final String dirCfg = dirConfig.getProperty(propertyName);
			if (dirCfg == null) {
				return defaultDir;
			} else {
				return expandCommandLine(dirCfg);
			}
		} catch (Exception e) {
			Logging.LOG.error("Error reading directory configuration: " + e.getMessage(), e);
			JOptionPane.showMessageDialog(null, "<html><p>Failed to load directory.ini - entry \"" + propertyName
					+ "\":<p><p>" + e.getMessage() + "</p></html>", "Faile do load directory.ini",
					JOptionPane.ERROR_MESSAGE);
			return defaultDir;
		}
	}

	/**
	 * Modified version of
	 * http://stackoverflow.com/questions/2090647/evaluation-of-environment-variables-in-command-run-
	 * by-javas-runtime-exec
	 * 
	 * @param cmd
	 * @return
	 */
	private static File expandCommandLine(final String cmd) {
		final Pattern vars = Pattern.compile("[$]\\{(\\S+)\\}");
		final Matcher m = vars.matcher(cmd.trim());

		final StringBuffer sb = new StringBuffer(cmd.length());
		int lastMatchEnd = 0;
		while (m.find()) {
			sb.append(cmd.substring(lastMatchEnd, m.start()));
			final String envVar = m.group(1);
			String envVal = System.getenv(envVar);
			if (envVal == null) {
				File defPath = null;

				if ("mobac-prog".equalsIgnoreCase(envVar))
					defPath = programDir;
				else if ("home".equalsIgnoreCase(envVar))
					defPath = userHomeDir;
				else if ("XDG_CONFIG_HOME".equalsIgnoreCase(envVar))
					defPath = new File(userHomeDir, ".config");
				else if ("XDG_CACHE_HOME".equalsIgnoreCase(envVar))
					defPath = new File(userHomeDir, ".cache");
				else if ("XDG_DATA_HOME".equalsIgnoreCase(envVar)) {
					File localDataDir = new File(userHomeDir, ".local");
					defPath = new File(localDataDir, "share");
				}

				if (defPath != null)
					envVal = defPath.getAbsolutePath();
			}
			if (envVal == null)
				sb.append(cmd.substring(m.start(), m.end()));
			else
				sb.append(envVal);
			lastMatchEnd = m.end();
		}
		sb.append(cmd.substring(lastMatchEnd));

		return new File(sb.toString());
	}

	public static void initialize() {
		if (currentDir == null || userAppDataDir == null || tempDir == null || programDir == null)
			throw new RuntimeException("DirectoryManager failed");
	}

	private static void loadDirectoriesIni() {
		File dirIniFile = new File(programDir, "directories.ini");
		if (!dirIniFile.isFile())
			return;
		dirConfig = new Properties();
		FileInputStream in = null;
		try {
			in = new FileInputStream(dirIniFile);
			dirConfig.load(in);
		} catch (IOException e) {
			System.err.println("Failed to load " + dirIniFile.getName());
			e.printStackTrace();
		} finally {
			Utilities.closeStream(in);
		}
	}

	/**
	 * Returns the directory from which this java program is executed
	 * 
	 * @return
	 */
	private static File getProgramDir() {
		File f = null;
		try {
			f = Utilities.getClassLocation(DirectoryManager.class);
		} catch (Exception e) {
			System.err.println(e.getMessage());
			return currentDir;
		}
		if ("bin".equals(f.getName())) // remove the bin dir -> this usually
			// happens only in a development environment
			return f.getParentFile();
		else
			return f;
	}

	/**
	 * Returns the directory where Mobile Atlas Creator saves it's application settings.
	 * 
	 * Examples:
	 * <ul>
	 * <li>English Windows XP:<br>
	 * <tt>C:\Document and Settings\%username%\Application Data\Mobile Atlas Creator</tt>
	 * <li>Vista:<br>
	 * <tt>C:\Users\%username%\Application Data\Mobile Atlas Creator</tt>
	 * <li>Linux:<br>
	 * <tt>/home/$username$/.mobac</tt></li>
	 * </ul>
	 * 
	 * @return
	 */
	private static File getUserAppDataDir() {
		String appData = System.getenv("APPDATA");
		if (appData != null) {
			File appDataDir = new File(appData);
			if (appDataDir.isDirectory()) {
				File mobacDataDir = new File(appData, "Mobile Atlas Creator");
				if (mobacDataDir.isDirectory() || mobacDataDir.mkdir())
					return mobacDataDir;
				else
					throw new RuntimeException("Unable to create directory \"" + mobacDataDir.getAbsolutePath() + "\"");
			}
		}
		File userDir = new File(System.getProperty("user.home"));
		File mobacUserDir = new File(userDir, ".mobac");
		if (!mobacUserDir.exists() && !mobacUserDir.mkdir())
			throw new RuntimeException("Unable to create directory \"" + mobacUserDir.getAbsolutePath() + "\"");
		return mobacUserDir;
	}
}
