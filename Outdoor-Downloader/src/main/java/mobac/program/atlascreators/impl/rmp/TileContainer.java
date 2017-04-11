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

package mobac.program.atlascreators.impl.rmp;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Container for tiles and link to subtrees
 * 
 */
public class TileContainer {

	private final TileContainer previous;
	private final ArrayList<Tiledata> tiles;
	private final ArrayList<TileContainer> followUps;

	public TileContainer() {
		this(null);
	}

	public TileContainer(TileContainer previous) {
		this.previous = previous;
		tiles = new ArrayList<Tiledata>(100);
		followUps = new ArrayList<TileContainer>();
	}

	/**
	 * Set tile for given position. next is only valid if a previous value is
	 * set
	 */
	public void addTile(Tiledata tile, TileContainer next) {
		tiles.add(tile);

		if (previous != null)
			followUps.add(next);
	}

	/**
	 * Returns the number of tiles in this container including all sub
	 * containers
	 */
	public int getTileCount() {
		int count = tiles.size();

		if (previous != null)
			count += previous.getTileCount();

		for (TileContainer next : followUps)
			count += next.getTileCount();

		return count;
	}

	/**
	 * returns the number of containers in the tree
	 */
	public int getContainerCount() {
		return 1 + followUps.size();
	}

	/**
	 * Write the whole tree into the stream
	 */
	public void writeTree(OutputStream os) throws IOException {
		/* --- if this container has subtrees --- */
		if (previous != null) {
			/* --- Write previous --- */
			previous.writeTree(os);

			/* --- Write ourselves --- */
			writeContainer(os);

			/* --- And all subtrees --- */
			for (TileContainer tc : followUps)
				tc.writeTree(os);
		} else {
			/* --- Just write the tile itself, if it does not have subtrees --- */
			writeContainer(os);
		}
	}

	/**
	 * Write the content of the container
	 */
	public void writeContainer(OutputStream os) throws IOException {

		/* --- Number of tiles in subtree --- */
		RmpTools.writeValue(os, getTileCount(), 4);

		/* --- Number of tiles in node --- */
		RmpTools.writeValue(os, tiles.size(), 2);

		/* --- Last node flag --- */
		RmpTools.writeValue(os, previous == null ? 1 : 0, 2);

		/* --- Info about 99 tiles --- */
		for (int i = 0; i < 99; i++) {
			int x = 0;
			int y = 0;
			int offset = 0;

			if (i < tiles.size()) {
				x = tiles.get(i).posx;
				y = tiles.get(i).posy;
				offset = tiles.get(i).totalOffset;
			}

			RmpTools.writeValue(os, x, 4);
			RmpTools.writeValue(os, y, 4);
			RmpTools.writeValue(os, 0, 4);
			RmpTools.writeValue(os, offset, 4);
		}

		/* --- Offset to previous --- */
		if (previous == null)
			RmpTools.writeValue(os, 0, 4);
		else
			RmpTools.writeValue(os, 0x0f5c, 4);

		/* --- Offset to following --- */
		for (int i = 0; i < 99; i++) {
			if (i < followUps.size())
				RmpTools.writeValue(os, 0x0f5c + (i + 2) * 1992, 4);
			else
				RmpTools.writeValue(os, 0, 4);
		}
	}
}
