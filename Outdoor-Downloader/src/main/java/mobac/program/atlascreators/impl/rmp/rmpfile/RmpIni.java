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
/* *********************************************
 * Copyright: Andreas Sander
 *
 *
 * ********************************************* */

package mobac.program.atlascreators.impl.rmp.rmpfile;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import mobac.program.atlascreators.impl.rmp.RmpTools;

import org.apache.log4j.Logger;


/**
 * Instance of the rmp.ini file
 * 
 * @author Andreas
 * 
 */
public class RmpIni extends GeneralRmpFileEntry {

	private static final Logger log = Logger.getLogger(RmpIni.class);

	public RmpIni(String layername, int count) {
		super(generateContent(layername, count), "rmp", "ini");
	}

	private static byte[] generateContent(String layername, int count) {
		ByteArrayOutputStream bos;
		PrintStream ps;
		int i;

		bos = new ByteArrayOutputStream();
		ps = new PrintStream(bos);

		/* --- Content of rmp.ini is a simple INI file --- */
		ps.print("[T_Layers]\r\n");

		for (i = 0; i < count; i++) {
			String layerName = RmpTools.buildTileName(layername, i);
			log.trace("layer name: " + layerName);
			ps.print(i + "=" + layerName + "\r\n");
		}
		ps.flush();
		return bos.toByteArray();
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " size=" + content.length;
	}

}
