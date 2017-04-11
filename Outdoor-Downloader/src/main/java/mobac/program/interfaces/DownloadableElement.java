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
package mobac.program.interfaces;

import java.util.Enumeration;

import mobac.program.JobDispatcher.Job;
import mobac.utilities.tar.TarIndexedArchive;


/**
 * Classes that implement this interface identify themselves as responsible for
 * specifying what tiles should be downloaded.
 * 
 * In general this interface should be implemented in combination with
 * {@link MapInterface}, {@link LayerInterface} or {@link AtlasInterface}.
 * 
 */
public interface DownloadableElement {

	/**
	 * 
	 * @param tileArchive
	 * @param listener
	 * @return An enumeration that returns {@link Job} objects. Each job should
	 *         download one map tile from the providing web server (or from the
	 *         tile cache).
	 */
	public Enumeration<Job> getDownloadJobs(TarIndexedArchive tileArchive,
			DownloadJobListener listener);
	
}
