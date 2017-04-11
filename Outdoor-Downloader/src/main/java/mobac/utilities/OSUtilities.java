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
package mobac.utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.util.regex.Pattern;

import javax.naming.NameNotFoundException;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

public class OSUtilities {

	static Logger log = Logger.getLogger(OSUtilities.class);

	public enum OperatingSystem {
		Windows, Linux, MacOs, MacOsX, Solaris, Unknown
	};

	public enum DesktopType {
		Windows, Gnome, Kde, Unknown {
			@Override
			public String toString() {
				return super.toString() + " (" + System.getProperty("sun.desktop") + ")";
			}
		}
	};

	public static OperatingSystem detectOs() {
		String osName = System.getProperty("os.name").toLowerCase();
		if (osName.indexOf("windows") > -1)
			return OperatingSystem.Windows;
		if (osName.indexOf("linux") > -1)
			return OperatingSystem.Linux;
		if (osName.indexOf("mac os x") > -1)
			return OperatingSystem.MacOsX;
		if (osName.indexOf("mac os") > -1)
			return OperatingSystem.MacOs;
		if (osName.indexOf("sunos") > -1)
			return OperatingSystem.Solaris;

		return OperatingSystem.Unknown;
	}

	public static DesktopType detectDesktopType() {
		String desktopName = System.getProperty("sun.desktop");
		if (desktopName == null)
			return DesktopType.Unknown;
		desktopName = desktopName.toLowerCase().trim();
		if (desktopName.startsWith("windows"))
			return DesktopType.Windows;
		if (desktopName.startsWith("gnome"))
			return DesktopType.Gnome;
		if (desktopName.startsWith("kde"))
			return DesktopType.Kde;
		log.error("Unknown desktop type: " + desktopName);
		return DesktopType.Unknown;
	}

	public static void openFolderBrowser(File directory) throws NameNotFoundException {
		if (!directory.isDirectory())
			throw new NameNotFoundException("Directory does not exist or is not a directory");
		String[] strCmd = null;
		try {
			String dirPath = directory.getCanonicalPath();
			DesktopType dt = detectDesktopType();
			switch (dt) {
			case Windows:
				strCmd = new String[] { "rundll32.exe", "url.dll,FileProtocolHandler", "\"" + dirPath + "\"" };
				break;
			case Gnome:
				strCmd = new String[] { "nautilus", dirPath };
				break;
			case Kde:
				strCmd = new String[] { "konqueror", dirPath };
				break;
			default:
				OperatingSystem os = detectOs();
				switch (os) {
				case MacOsX:
					strCmd = new String[] { "/usr/bin/open", dirPath };
					break;
				case Linux:
					strCmd = new String[] { "/usr/bin/xdg-open", dirPath };
					break;
				default:
					JOptionPane.showMessageDialog(null, "<html>Your environment is not supported.<br>Desktop: " + dt
							+ "<br>Operating system: " + os + "</html>", "Unsupported environment",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
			for (String s : strCmd)
				log.trace("exec/params: " + s);
			Runtime.getRuntime().exec(strCmd);
		} catch (Exception e) {
			StringBuilder sb = new StringBuilder(512);
			sb.append("<html><b>Failed to open atlas output directory.</b><br>");
			sb.append(e.getMessage());
			sb.append("<br>Used command: <tt>" + strCmd[0] + "</tt><br>");
			for (int i = 1; i < strCmd.length; i++) {
				sb.append("Parameter " + i + ": " + strCmd[i]);
			}
			sb.append("</html>");
			String msg = sb.toString();
			JOptionPane
					.showMessageDialog(null, msg, "Failed to open atlas output directory", JOptionPane.ERROR_MESSAGE);
			log.error(msg, e);
		}

	}

	/**
	 * Reads the Linux distribution name (last line) from the first file that matches the pattern
	 * 
	 * <pre>
	 * /etc/*-release
	 * </pre>
	 * 
	 * @return Linux distrbution name or <code>null</code>
	 */
	public static String getLinuxDistributionName() {
		try {
			File etcDir = new File("/etc");
			if (!etcDir.exists())
				return null;
			File[] files = etcDir.listFiles(new FilenameFilter() {

				Pattern pattern = Pattern.compile(".*-release");

				public boolean accept(File dir, String name) {
					return pattern.matcher(name).matches();
				}
			});
			if (files.length == 0)
				return null;
			BufferedReader br = new BufferedReader(new FileReader(files[0]));
			String result = null;
			String line = null;
			line = br.readLine();
			while (line != null) {
				result = line;
				line = br.readLine();
			}
			br.close();
			if (result != null) {
				int index = result.indexOf('=');
				if (index > 0)
					result = result.substring(index + 1);
				if (result.startsWith("\"") && result.endsWith("\""))
					result = result.substring(1, result.length() - 2);
			}
			return result;
		} catch (Exception e) {
			log.trace("", e);
			return null;
		}
	}

	/**
	 * Replies true if we are currently running on OSX
	 * 
	 * @return true if we are currently running on OSX
	 */
	public static boolean isPlatformOsx() {
		String os = System.getProperty("os.name");
		return os != null && os.toLowerCase().startsWith("mac os x");
	}
}
