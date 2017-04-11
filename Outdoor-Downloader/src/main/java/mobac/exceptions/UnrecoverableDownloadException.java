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
package mobac.exceptions;

/**
 * An {@link UnrecoverableDownloadException} indicates that there has been a
 * problem on client side that made it impossible to download a certain file
 * (usually a map tile image). Therefore the error is independent of the network
 * connection between client and server and the server itself.
 */
public class UnrecoverableDownloadException extends TileException {

	private static final long serialVersionUID = 1L;

	public UnrecoverableDownloadException(String message) {
		super(message);
	}

}
