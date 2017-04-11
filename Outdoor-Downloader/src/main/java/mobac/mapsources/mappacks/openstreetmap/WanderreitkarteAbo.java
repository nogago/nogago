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
package mobac.mapsources.mappacks.openstreetmap;

import java.io.IOException;

import mobac.exceptions.TileException;
import mobac.program.model.Settings;

public class WanderreitkarteAbo extends AbstractOsmMapSource {

	public static final String ABO = "http://abo.wanderreitkarte.de";

	public WanderreitkarteAbo() {
		super("WanderreitkarteAbo");
		minZoom = 2;
		maxZoom = 16;
	}

	@Override
	public byte[] getTileData(int zoom, int x, int y, LoadMethod loadMethod) throws IOException, TileException,
			InterruptedException {
		if (loadMethod == LoadMethod.CACHE)
			return super.getTileData(zoom, x, y, loadMethod);

		// No multi threaded download possible/allowed
		// if we don't synchronize here we get a high percentage of errors
		synchronized (this) {
			return super.getTileData(zoom, x, y, loadMethod);
		}
	}

	@Override
	public String getTileUrl(int zoom, int tilex, int tiley) {
		String ticket = Settings.getInstance().osmHikingTicket;
		if (ticket != null && ticket.length() > 0) {
			return ABO + super.getTileUrl(zoom, tilex, tiley) + "/ticket/" + ticket;
		} else
			return null;
	}

	@Override
	public String toString() {
		return "Reit- und Wanderkarte ($Abo)";
	}

}
