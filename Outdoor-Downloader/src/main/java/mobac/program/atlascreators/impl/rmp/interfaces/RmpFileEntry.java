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

package mobac.program.atlascreators.impl.rmp.interfaces;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Interface for all files that are stored in a RMP file
 * 
 */
public interface RmpFileEntry {
	/**
	 * Returns the content of the file as byte array
	 * 
	 * @throws InterruptedException
	 */
	public void writeFileContent(OutputStream os) throws IOException, InterruptedException;

	/**
	 * Returns the name of the file without extension
	 */
	public String getFileName();

	/**
	 * Returns the extension of the file
	 */
	public String getFileExtension();

}
