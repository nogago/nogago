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
package mobac.mapsources.loader;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * Loads all available classes from the map source packages and everything else from the <code>fallback</code>
 * {@link ClassLoader}. Therefore in difference to the standard parent {@link ClassLoader} concept this implementation
 * first tries to load the and then asks the fallback whereas usually it is the opposite (first try to load via parent
 * and only if that fails try to do it self).
 */
public class MapPackClassLoader extends URLClassLoader {

	private final ClassLoader fallback;

	public MapPackClassLoader(URL url, ClassLoader fallback) {
		this(new URL[] { url }, fallback);
	}

	protected MapPackClassLoader(URL[] urls, ClassLoader fallback) {
		super(urls, null);
		this.fallback = fallback;
	}

	@Override
	protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		try {
			return super.loadClass(name, resolve);
		} catch (ClassNotFoundException e) {
			return fallback.loadClass(name);
		}
	}

}
