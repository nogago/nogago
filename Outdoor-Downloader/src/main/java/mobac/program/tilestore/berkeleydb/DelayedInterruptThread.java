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
package mobac.program.tilestore.berkeleydb;

import java.util.concurrent.ThreadFactory;

/**
 * The Berkeley DB has some problems when someone interrupts the thread that is currently performing IO activity.
 * Therefore before executing any DB we allow to disable the {@link #interrupt()} method via {@link #pauseInterrupt()}.
 * After the "interrupt sensitive section" {@link #resumeInterrupt()} restores the regular behavior. If the thread has
 * been interrupted while interrupt was disabled {@link #resumeInterrupt()} catches up this.
 */
public class DelayedInterruptThread extends Thread {

	private boolean interruptPaused = false;
	private boolean interruptedWhilePaused = false;

	public DelayedInterruptThread(String name) {
		super(name);
	}

	public DelayedInterruptThread(Runnable target) {
		super(target);
	}

	public DelayedInterruptThread(Runnable target, String name) {
		super(target, name);
	}

	@Override
	public void interrupt() {
		if (interruptPaused)
			interruptedWhilePaused = true;
		else
			super.interrupt();
	}

	public void pauseInterrupt() {
		interruptPaused = true;
	}

	public void resumeInterrupt() {
		interruptPaused = false;
		if (interruptedWhilePaused)
			this.interrupt();
	}

	public boolean interruptedWhilePaused() {
		return interruptedWhilePaused;
	}

	public static ThreadFactory createThreadFactory() {
		return new DIThreadFactory();
	}

	private static class DIThreadFactory implements ThreadFactory {

		public Thread newThread(Runnable r) {
			return new DelayedInterruptThread(r);
		}

	}
}
