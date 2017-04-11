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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import Acme.Serve.Serve;

public class ShutdownServlet extends HttpServlet {

	private final Serve server;

	public ShutdownServlet(Serve server) {
		super();
		this.server = server;
	}

	@Override
	protected void doDelete(HttpServletRequest arg0, HttpServletResponse response) throws ServletException, IOException {
		response.setStatus(202);
		response.flushBuffer();
		server.notifyStop();
	}

}
