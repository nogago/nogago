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
package mobac.gui.mapview;

//License: GPL. Copyright 2008 by Jan Peter Stotz

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import mobac.program.tilestore.berkeleydb.DelayedInterruptThread;

import org.apache.log4j.Logger;

public class JobDispatcher implements ThreadFactory, RejectedExecutionHandler {

	private static final Logger log = Logger.getLogger(JobDispatcher.class);
	private static final JobDispatcher INSTANCE = new JobDispatcher();

	private static final int WORKER_THREAD_MAX_COUNT = 5;

	/**
	 * Specifies the time span in seconds that a worker thread waits for new jobs to perform. If the time span has
	 * elapsed the worker thread terminates itself. Only the first worker thread works differently, it ignores the
	 * timeout and will never terminate itself.
	 */
	private static final int WORKER_THREAD_TIMEOUT = 30;

	/**
	 * @return the singleton instance of the {@link JobDispatcher}
	 */
	public static JobDispatcher getInstance() {
		return INSTANCE;
	}

	private int WORKER_THREAD_ID = 1;

	private final BlockingQueue<Runnable> jobQueue;

	private final ThreadPoolExecutor executor;

	/**
	 * Removes all jobs from the queue that are currently not being processed.
	 */
	public void cancelOutstandingJobs() {
		jobQueue.clear();
	}

	private JobDispatcher() {
		jobQueue = new LinkedBlockingQueue<Runnable>();
		executor = new ThreadPoolExecutor(WORKER_THREAD_MAX_COUNT, WORKER_THREAD_MAX_COUNT, WORKER_THREAD_TIMEOUT,
				TimeUnit.SECONDS, jobQueue, this, this);
		executor.allowCoreThreadTimeOut(true);
	}

	public void addJob(Runnable job) {
		executor.execute(job);
	}

	public Thread newThread(Runnable r) {
		int id;
		synchronized (this) {
			id = WORKER_THREAD_ID++;
		}
		log.trace("New map preview worker thread created with id=" + id);
		return new DelayedInterruptThread(r, "Map preview thread " + id);
	}

	public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
		log.error("Map preview job rejected: " + r);
	}

}
