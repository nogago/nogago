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
package mobac.program.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import javax.swing.AbstractListModel;

import mobac.program.interfaces.MapSource;

public class MapSourcesListModel extends AbstractListModel {

	ArrayList<MapSource> mapSources;

	public MapSourcesListModel(Vector<MapSource> source) {
		this.mapSources = new ArrayList<MapSource>(source);
	}

	public Object getElementAt(int index) {
		return mapSources.get(index);
	}

	public int getSize() {
		return mapSources.size();
	}

	public Vector<MapSource> getVector() {
		return new Vector<MapSource>(mapSources);
	}

	public MapSource removeElement(int index) {
		fireIntervalRemoved((Object) this, index, index);
		return mapSources.remove(index);
	}

	public void addElement(MapSource element) {
		mapSources.add(element);
		fireIntervalAdded((Object) this, mapSources.size(), mapSources.size());
	}

	public boolean moveUp(int index) {
		if (index < 1)
			return false;
		MapSource ms = mapSources.remove(index - 1);
		mapSources.add(index, ms);
		fireContentsChanged(this, index - 1, index);
		return true;
	}

	public boolean moveDown(int index) {
		if (index + 1 >= mapSources.size())
			return false;
		MapSource ms = mapSources.remove(index + 1);
		mapSources.add(index, ms);
		fireContentsChanged(this, index, index + 1);
		return true;
	}

	public void sort() {
		Collections.sort(mapSources, new Comparator<MapSource>() {

			public int compare(MapSource o1, MapSource o2) {
				return o1.toString().compareTo(o2.toString());
			}

		});
		fireContentsChanged(mapSources, 0, mapSources.size());
	}
}
