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
package mobac.tools.testtileserver.servlets;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

public abstract class AbstractTileServlet extends HttpServlet {

	protected static final SecureRandom RND = new SecureRandom();

	protected final Logger log;

	/**
	 * Error rate in percent [0..100]
	 */
	protected int errorRate = 0;

	protected boolean errorOnUrl = true;

	protected int delay = 0;

	private final MessageDigest md5;

	public AbstractTileServlet() {
		log = Logger.getLogger(this.getClass());
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException {
		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
		}
		if (errorResponse(request, response))
			return;
		super.service(request, response);
	}

	public boolean errorResponse(HttpServletRequest request, HttpServletResponse response) throws IOException {
		if (errorRate == 0)
			return false;
		if (errorOnUrl) {
			String url = request.getRequestURL() + request.getQueryString();
			byte[] digest = md5.digest(url.getBytes());
			int hash = Math.abs(digest[4] % 100);
			log.debug(url.toString() + " -> " + hash + ">" + errorRate + "?");
			if (hash > errorRate)
				return false;
		} else {
			int rnd = RND.nextInt(100);
			if (rnd > errorRate)
				return false;
		}
		response.sendError(404);
		log.debug("Error response sent");
		return true;
	}

	public int getErrorRate() {
		return errorRate;
	}

	public void setErrorRate(int errorRate) {
		this.errorRate = errorRate;
	}

	public boolean isErrorOnUrl() {
		return errorOnUrl;
	}

	public void setErrorOnUrl(boolean errorOnUrl) {
		this.errorOnUrl = errorOnUrl;
	}

	public int getDelay() {
		return delay;
	}

	public void setDelay(int delay) {
		this.delay = delay;
	}
	
}
