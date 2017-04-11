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
package mobac.gui.atlastree;

import java.awt.Component;

import javax.swing.DefaultCellEditor;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeCellRenderer;

import mobac.gui.components.JAtlasNameField;


public class NodeEditor extends DefaultTreeCellEditor {

	public NodeEditor(JAtlasTree atlasTree) {
		super(atlasTree, null);
		atlasTree.setEditable(true);
	}

	@Override
	public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected,
			boolean expanded, boolean leaf, int row) {
		// Each node type has it's own TreeCellRenderer implementation
		// this not covered by DefaultTreeCellEditor - therefore we have to
		// correct the renderer each time an editorComponent is requested
		TreeCellRenderer tcr = tree.getCellRenderer();
		renderer = (DefaultTreeCellRenderer) tcr.getTreeCellRendererComponent(tree, value,
				isSelected, expanded, leaf, row, true);
		return super.getTreeCellEditorComponent(tree, value, isSelected, expanded, leaf, row);
	}

	@Override
	protected TreeCellEditor createTreeCellEditor() {
		return new DefaultCellEditor(new JAtlasNameField());
	}

}
