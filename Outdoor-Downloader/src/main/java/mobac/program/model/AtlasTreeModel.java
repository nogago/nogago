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

import java.awt.Toolkit;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import mobac.exceptions.InvalidNameException;
import mobac.program.interfaces.AtlasInterface;
import mobac.program.interfaces.AtlasObject;
import mobac.program.interfaces.LayerInterface;
import mobac.program.interfaces.MapInterface;

import org.apache.log4j.Logger;


public class AtlasTreeModel implements TreeModel {

	private static Logger log = Logger.getLogger(AtlasTreeModel.class);

	protected AtlasInterface atlasInterface;

	protected Set<TreeModelListener> listeners = new HashSet<TreeModelListener>();

	public AtlasTreeModel() {
		super();
		atlasInterface = Atlas.newInstance();
	}

	public void addTreeModelListener(TreeModelListener l) {
		listeners.add(l);
	}

	public void removeTreeModelListener(TreeModelListener l) {
		listeners.remove(l);
	}

	public void notifyStructureChanged() {
		notifyStructureChanged((TreeNode) atlasInterface);
	}

	public void notifyStructureChanged(TreeNode root) {
		notifyStructureChanged(new TreeModelEvent(this, new Object[] { root }));
	}

	/**
	 * IMPORTANT: This method have to be called BEFORE deleting the element in
	 * the data model!!! Otherwise the child index can not be retrieved anymore
	 * which is important.
	 * 
	 * @param node
	 */
	public void notifyNodeDelete(TreeNode node) {
		TreeNode parent = node.getParent();
		Object[] children = new Object[] { node };
		int childrenIdx = parent.getIndex(node);
		if (childrenIdx == -1) {
			// A problem detected - use fall back solution
			notifyStructureChanged();
			return;
		}
		TreePath path = getNodePath(parent);

		TreeModelEvent event = new TreeModelEvent(this, path, new int[] { childrenIdx }, children);
		for (TreeModelListener l : listeners)
			l.treeNodesRemoved(event);
	}

	protected void notifyStructureChanged(TreeModelEvent event) {
		for (TreeModelListener l : listeners)
			l.treeStructureChanged(event);
	}

	public void notifyNodeInsert(TreeNode insertedNode) {
		TreeNode parent = insertedNode.getParent();
		TreePath path = getNodePath(parent);
		TreeNode[] childs = new TreeNode[] { insertedNode };
		int childId = parent.getIndex(insertedNode);
		assert (childId <= 0);
		TreeModelEvent event = new TreeModelEvent(this, path, new int[] { childId }, childs);
		for (TreeModelListener l : listeners)
			l.treeNodesInserted(event);
	}

	public TreePath getNodePath(TreeNode node) {
		LinkedList<TreeNode> path = new LinkedList<TreeNode>();
		TreeNode n = node;
		while (n != null) {
			path.addFirst(n);
			n = n.getParent();
		}
		return new TreePath(path.toArray());
	}

	public Object getChild(Object parent, int index) {
		return ((TreeNode) parent).getChildAt(index);
	}

	public int getChildCount(Object parent) {
		return ((TreeNode) parent).getChildCount();
	}

	public int getIndexOfChild(Object parent, Object child) {
		return ((TreeNode) parent).getIndex((TreeNode) child);
	}

	public Object getRoot() {
		return atlasInterface;
	}

	public boolean isLeaf(Object node) {
		return ((TreeNode) node).isLeaf();
	}

	public void valueForPathChanged(TreePath path, Object newValue) {
		Object o = path.getLastPathComponent();
		boolean success = false;
		try {
			AtlasObject sel = (AtlasObject) o;
			String newName = (String) newValue;
			if (newName.length() == 0)
				return;
			sel.setName(newName);
			success = true;
		} catch (ClassCastException e) {
			log.error("", e);
		} catch (InvalidNameException e) {
			log.error(e.getLocalizedMessage());
			JOptionPane.showMessageDialog(null, e.getLocalizedMessage(), "Renaming failed",
					JOptionPane.ERROR_MESSAGE);
		} finally {
			if (!success) {
				Toolkit.getDefaultToolkit().beep();
			}
		}
	}

	public void mergeLayers(LayerInterface source, LayerInterface target)
			throws InvalidNameException {

		boolean sourceFound = false;
		boolean targetFound = false;
		for (LayerInterface l : atlasInterface) {
			if (l.equals(source))
				sourceFound = true;
			if (l.equals(target))
				targetFound = true;
		}
		if (!targetFound)
			return;
		// Check for duplicate names
		HashSet<String> names = new HashSet<String>();
		for (MapInterface map : source)
			names.add(map.getName());
		for (MapInterface map : target)
			names.add(map.getName());
		if (names.size() < (source.getMapCount() + target.getMapCount()))
			throw new InvalidNameException("Map naming conflict:\n"
					+ "The layers to be merged contain map(s) of the same name.");

		if (sourceFound)
			atlasInterface.deleteLayer(source);
		for (MapInterface map : source) {
			target.addMap(map);
		}
		notifyNodeDelete((TreeNode) source);
		notifyStructureChanged((TreeNode) target);
	}

	public void moveMap(MapInterface map, LayerInterface targetLayer) {
		notifyNodeDelete((TreeNode) map);
		map.delete();
		targetLayer.addMap(map);
		notifyNodeInsert((TreeNode) map);
	}

	public AtlasInterface getAtlas() {
		return atlasInterface;
	}

	public void setAtlas(Atlas atlas) {
		this.atlasInterface = atlas;
		notifyStructureChanged();
	}

	public void save(Profile profile) throws Exception {
		profile.save(atlasInterface);
	}

	public void load(Profile profile) throws Exception {
		atlasInterface = profile.load();
		notifyStructureChanged();
	}

}
