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
package mobac.utilities.collections;

import java.lang.ref.SoftReference;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A HashMap that uses internally SoftReferences for the value (if the app is
 * running out of memory the values stored in a {@link SoftReference} will be
 * automatically freed.
 * 
 * Therefore it may happen that this map "looses" values.
 * 
 * @param <K>
 * @param <V>
 */
public class SoftHashMap<K, V> implements Map<K, V> {

	HashMap<K, SoftReference<V>> map;

	public SoftHashMap(int initialCapacity) {
		map = new HashMap<K, SoftReference<V>>(initialCapacity);
	}

	public V get(Object key) {
		SoftReference<V> ref = map.get(key);
		return (ref != null) ? ref.get() : null;
	}

	public V put(K key, V value) {
		SoftReference<V> ref = map.put(key, new SoftReference<V>(value));
		return (ref != null) ? ref.get() : null;
	}

	public V remove(Object key) {
		SoftReference<V> ref = map.remove(key);
		return (ref != null) ? ref.get() : null;
	}

	public void clear() {
		map.clear();
	}

	public boolean containsKey(Object key) {
		return map.containsKey(key);
	}

	public boolean containsValue(Object value) {
		return map.containsValue(value);
	}

	public boolean isEmpty() {
		return map.isEmpty();
	}

	public Set<K> keySet() {
		return map.keySet();
	}

	public int size() {
		return map.size();
	}

	public Set<Map.Entry<K, V>> entrySet() {
		throw new RuntimeException("Not implemented");
	}

	public void putAll(Map<? extends K, ? extends V> m) {
		throw new RuntimeException("Not implemented");
	}

	public Collection<V> values() {
		throw new RuntimeException("Not implemented");
	}
}
