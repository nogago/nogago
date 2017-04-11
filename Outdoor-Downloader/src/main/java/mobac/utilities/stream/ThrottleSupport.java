/*
 *  XNap - A P2P framework and client.
 *
 *  See the file AUTHORS for copyright information.
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package mobac.utilities.stream;

import org.apache.log4j.Logger;

/**
 * Provides a throttle implementation based on time ticks. Clients can allocate a limited amount of bytes for each tick.
 * If all bandwidth has been allocated clients are stalled until the next tick.
 */
public class ThrottleSupport {

	// --- Constant(s) ---

	/**
	 * The default length of a tick.
	 */
	public static final long TICK_LENGTH = 512; // = 2^9

	// --- Data field(s) ---

	private static Logger logger = Logger.getLogger(ThrottleSupport.class);

	/**
	 * bytes per tick
	 */
	protected long bandwidth = 0;
	protected long allocated = 0;
	protected long tick = 0;
	protected Object lock = new Object();

	// --- Constructor(s) ---

	public ThrottleSupport() {
	}

	/**
	 * Sets the maximum bandwidth.
	 * 
	 * @param bandwidth
	 *            byte / s
	 */
	public void setBandwidth(long bandwidth) {
		synchronized (lock) {
			this.bandwidth = (bandwidth * TICK_LENGTH) / 1024;
		}
	}

	/**
	 * Returns the number of bytes that the calling thread is allowed to send. Blocks until at least one byte could be
	 * allocated.
	 * 
	 * @return -1, if interrupted;
	 */
	public int allocate(int bytes) {
		if (bytes == 0)
			return 0;
		synchronized (lock) {
			while (true) {
				if (bandwidth == 0) {
					// no limit
					return bytes;
				}
				long currentTick = System.currentTimeMillis() >> 9;
				if (currentTick > tick) {
					logger.debug("* new tick: " + bandwidth + " to allocate *");
					tick = currentTick;
					allocated = 0;
					lock.notifyAll();
				}
				if (bytes < bandwidth - allocated) {
					// we still have some bandwidth left
					allocated += bytes;
					logger.debug("returning " + bytes + " allocated now " + allocated);
					return bytes;
				}
				if (bandwidth - allocated > 0) {
					// don't have enough, but return all we have left
					bytes = (int) (bandwidth - allocated);
					allocated = bandwidth;
					logger.debug("returning " + bytes + " allocated now " + allocated);
					return bytes;
				}

				// we could not allocate any bandwidth, wait until the next
				// tick is started

				// this is a bit too long
				long t = TICK_LENGTH - (System.currentTimeMillis() % TICK_LENGTH);
				if (t > 0) {
					try {
						logger.debug("waiting for " + t);
						lock.wait(t);
					} catch (InterruptedException e) {
						return -1;
					}
				}
			}
		}
	}
}
