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
package mobac.utilities.file;

import java.io.File;
import java.io.FileFilter;

/**
 * A {@link FileFilter} that deletes every file in the directory specified by
 * the {@link File} on which {@link File#listFiles(FileFilter)} using
 * {@link DeleteFileFilter} is executed. Therefore the {@link FileFilter}
 * concept is abused as {@link File} enumerator.
 * <p>
 * Example: <code>new File("C:/Temp").listFiles(new DeleteFileFilter());</code>
 * deletes all files but no directories in the directory C:\Temp.
 * </p>
 */
public class DeleteFileFilter implements FileFilter {

	int countSuccess = 0;
	int countFailed = 0;
	int countError = 0;

	public boolean accept(File file) {
		try {
			if (file.isDirectory())
				// We only delete files
				return false;
			boolean success = file.delete();
			if (success)
				countSuccess++;
			else
				countFailed++;
		} catch (Exception e) {
			countError++;
		}
		// We don't care about the filter result
		return false;
	}

	public int getCountSuccess() {
		return countSuccess;
	}

	public int getCountFailed() {
		return countFailed;
	}

	public int getCountError() {
		return countError;
	}

	@Override
	public String toString() {
		return "Delete file filter status (success, failed, error): " + countSuccess + " / "
				+ countFailed + " / " + countError + " files";
	}

}
