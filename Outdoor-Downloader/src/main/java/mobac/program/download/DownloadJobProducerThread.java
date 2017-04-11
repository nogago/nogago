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
package mobac.program.download;

import java.util.Enumeration;

import mobac.program.AtlasThread;
import mobac.program.JobDispatcher;
import mobac.program.JobDispatcher.Job;
import mobac.program.interfaces.DownloadableElement;
import mobac.utilities.tar.TarIndexedArchive;

import org.apache.log4j.Logger;


/**
 * Creates the jobs for downloading tiles. If the job queue is full it will
 * block on {@link JobDispatcher#addJob(Job)}
 */
public class DownloadJobProducerThread extends Thread {

	private Logger log = Logger.getLogger(DownloadJobProducerThread.class);

	final JobDispatcher downloadJobDispatcher;

	final Enumeration<Job> jobEnumerator;

	public DownloadJobProducerThread(AtlasThread atlasThread, JobDispatcher downloadJobDispatcher,
			TarIndexedArchive tileArchive, DownloadableElement de) {
		this.downloadJobDispatcher = downloadJobDispatcher;
		jobEnumerator = de.getDownloadJobs(tileArchive, atlasThread);
		start();
	}

	@Override
	public void run() {
		try {
			while (jobEnumerator.hasMoreElements()) {
				Job job = jobEnumerator.nextElement();
				downloadJobDispatcher.addJob(job);
				log.trace("Job added: " + job);
			}
			log.debug("All download jobs has been generated");
		} catch (InterruptedException e) {
			downloadJobDispatcher.cancelOutstandingJobs();
			log.error("Download job generation interrupted");
		}
	}

	public void cancel() {
		try {
			interrupt();
		} catch (Exception e) {
		}
	}

}
