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
package mobac.program.interfaces;

import mobac.gui.atlastree.JAtlasTree;
import mobac.program.model.AtlasTreeModel;

/**
 * Identifies nodes in {@link JAtlasTree} / {@link AtlasTreeModel} that can be
 * deleted (including sub-nodes). Nodes implementing this interface will show a
 * "delete" entry in it's context menu.
 */
public interface CapabilityDeletable {

	public void delete();
}
