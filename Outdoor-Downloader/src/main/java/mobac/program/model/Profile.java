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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.ValidationEventLocator;

import mobac.gui.panels.JProfilesPanel;
import mobac.program.DirectoryManager;
import mobac.program.interfaces.AtlasInterface;
import mobac.program.interfaces.AtlasObject;
import mobac.utilities.Utilities;

import org.apache.log4j.Logger;

/**
 * A profile is a saved atlas. The available profiles ({@link Profile} instances) are visible in the
 * <code>profilesCombo</code> in the {@link JProfilesPanel}.
 */
public class Profile implements Comparable<Profile> {

	private static Logger log = Logger.getLogger(Profile.class);

	public static final String PROFILE_NAME_REGEX = "[\\w _-]+";

	public static final String PROFILE_FILENAME_PREFIX = "mobac-profile-";

	public static final Pattern PROFILE_FILENAME_PATTERN = Pattern.compile(PROFILE_FILENAME_PREFIX + "("
			+ PROFILE_NAME_REGEX + ").xml");

	public static final Profile DEFAULT = new Profile();

	private File file;
	private String name;
	private static Vector<Profile> profiles = new Vector<Profile>();

	/**
	 * Profiles management method
	 */
	public static void updateProfiles() {
		File profilesDir = DirectoryManager.atlasProfilesDir;
		final Set<Profile> deletedProfiles = new HashSet<Profile>();
		deletedProfiles.addAll(profiles);
		profilesDir.list(new FilenameFilter() {

			public boolean accept(File dir, String fileName) {
				Matcher m = PROFILE_FILENAME_PATTERN.matcher(fileName);
				if (m.matches()) {
					String profileName = m.group(1);
					Profile profile = new Profile(new File(dir, fileName), profileName);
					if (!deletedProfiles.remove(profile))
						profiles.add(profile);
				}
				return false;
			}
		});
		for (Profile p : deletedProfiles)
			profiles.remove(p);
		Collections.sort(profiles);
	}

	/**
	 * Profiles management method
	 */
	public static Vector<Profile> getProfiles() {
		updateProfiles();
		return profiles;
	}

	/**
	 * Load a profile by it's name
	 * 
	 * @param name
	 */
	public Profile(String name) {
		this(new File(DirectoryManager.atlasProfilesDir, getProfileFileName(name)), name);
	}

	/**
	 * Default profile
	 */
	protected Profile() {
		this(new File(DirectoryManager.atlasProfilesDir, "mobac-profile.xml"), "");
	}

	protected Profile(File file, String name) {
		super();
		this.file = file;
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}

	public File getFile() {
		return file;
	}

	public String getName() {
		return name;
	}

	public boolean exists() {
		return file.isFile();
	}

	public void delete() {
		if (!file.delete())
			file.deleteOnExit();
	}

	public int compareTo(Profile o) {
		return file.compareTo(o.file);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Profile))
			return false;
		Profile p = (Profile) obj;
		return file.equals(p.file);
	}

	@Override
	public int hashCode() {
		assert false : "hashCode not designed";
		return -1;
	}

	public void save(AtlasInterface atlasInterface) throws JAXBException {
		JAXBContext context = JAXBContext.newInstance(Atlas.class);
		Marshaller m = context.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		FileOutputStream fo = null;
		try {
			fo = new FileOutputStream(file);
			m.marshal(atlasInterface, fo);
		} catch (FileNotFoundException e) {
			throw new JAXBException(e);
		} finally {
			Utilities.closeStream(fo);
		}
	}

	public AtlasInterface load() throws JAXBException {
		JAXBContext context = JAXBContext.newInstance(Atlas.class);
		Unmarshaller um = context.createUnmarshaller();
		um.setEventHandler(new ValidationEventHandler() {

			public boolean handleEvent(ValidationEvent event) {
				ValidationEventLocator loc = event.getLocator();
				String file = loc.getURL().getFile();
				int lastSlash = file.lastIndexOf('/');
				if (lastSlash > 0)
					file = file.substring(lastSlash + 1);
				int ret = JOptionPane.showConfirmDialog(null, "<html>Error loading atlas: <pre>" + event.getMessage()
						+ "</pre><pre>file: " + file + " line/column: " + loc.getLineNumber() + "/"
						+ loc.getColumnNumber() + "</pre>Do you want to continue loading the current atlas?<br>"
						+ "Continue loading may result in an incomplete or non-working atlas.<br>",
						"Error loading atlas - continue loading?", JOptionPane.YES_NO_CANCEL_OPTION,
						JOptionPane.ERROR_MESSAGE);
				log.error(event.toString());
				return (ret == JOptionPane.YES_OPTION);
			}
		});
		AtlasInterface newAtlas = (AtlasInterface) um.unmarshal(file);
		return newAtlas;
	}

	public static boolean checkAtlas(AtlasInterface atlasInterface) {
		return checkAtlasObject(atlasInterface);
	}

	public static String getProfileFileName(String profileName) {
		return PROFILE_FILENAME_PREFIX + profileName + ".xml";
	}

	private static boolean checkAtlasObject(Object o) {
		boolean result = false;
		if (o instanceof AtlasObject) {
			result |= ((AtlasObject) o).checkData();
		}
		if (o instanceof Iterable<?>) {
			Iterable<?> it = (Iterable<?>) o;
			for (Object ao : it) {
				result |= checkAtlasObject(ao);
			}
		}
		return result;
	}
}
