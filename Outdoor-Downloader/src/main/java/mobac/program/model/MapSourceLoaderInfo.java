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

public class MapSourceLoaderInfo {

	public enum LoaderType {
		MAPPACK, // map pack file
		XML, // custom map xml
		BSH // BeanShell script
		;
	};

	protected final LoaderType loaderType;

	protected final File sourceFile;

	protected final String revision;

	public MapSourceLoaderInfo(LoaderType loaderType, File sourceFile) {
		this(loaderType, sourceFile, null);
	}

	public MapSourceLoaderInfo(LoaderType loaderType, File sourceFile, String revision) {
		super();
		this.loaderType = loaderType;
		this.sourceFile = sourceFile;
		this.revision = revision;
	}

	public LoaderType getLoaderType() {
		return loaderType;
	}

	public File getSourceFile() {
		return sourceFile;
	}

	public String getRevision() {
		return revision;
	}

}
