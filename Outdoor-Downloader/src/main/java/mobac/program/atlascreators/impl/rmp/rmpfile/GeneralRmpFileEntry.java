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
/* *********************************************
 * Copyright: Andreas Sander
 *
 *
 * ********************************************* */

package mobac.program.atlascreators.impl.rmp.rmpfile;

import java.io.IOException;
import java.io.OutputStream;

import mobac.program.atlascreators.impl.rmp.interfaces.RmpFileEntry;


/**
 * General class for storing the content of a rmp file
 * 
 */
public class GeneralRmpFileEntry implements RmpFileEntry {
	protected final byte[] content;
	protected final String filename;
	protected final String extension;

	public GeneralRmpFileEntry(byte[] content, String filename, String extension) {
		this.content = content;
		this.filename = filename;
		this.extension = extension;
	}

	public void writeFileContent(OutputStream os) throws IOException {
		os.write(content);
	}

	public String getFileExtension() {
		return extension;
	}

	public String getFileName() {
		return filename;
	}

	@Override
	public String toString() {
		return "GeneralRmpFileEntry \"" + filename + "." + extension + "\" content-len="
				+ content.length;
	}
}
