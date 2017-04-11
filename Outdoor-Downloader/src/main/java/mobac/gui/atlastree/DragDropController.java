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

import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetContext;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;

import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import mobac.exceptions.InvalidNameException;
import mobac.program.interfaces.LayerInterface;
import mobac.program.interfaces.MapInterface;
import mobac.program.model.AtlasTreeModel;

import org.apache.log4j.Logger;


public class DragDropController {

	static Logger log = Logger.getLogger(DragDropController.class);

	public DragDropController(JAtlasTree atlasTree) {
		super();
		this.atlasTree = atlasTree;
		new AtlasDragSource();
		new AtlasDropTarget();
	}

	JAtlasTree atlasTree;

	protected class AtlasDragSource implements DragSourceListener, DragGestureListener {

		final DragGestureRecognizer recognizer;
		final DragSource source;

		public AtlasDragSource() {
			source = new DragSource();
			recognizer = source.createDefaultDragGestureRecognizer(atlasTree,
					DnDConstants.ACTION_MOVE, this);
		}

		public void dragGestureRecognized(DragGestureEvent dge) {
			TreePath path = atlasTree.getSelectionPath();
			if ((path == null) || (path.getPathCount() <= 1))
				// We can't move the root node or an empty selection
				return;
			TreeNode oldNode = (TreeNode) path.getLastPathComponent();
			if (!(oldNode instanceof LayerInterface || oldNode instanceof MapInterface))
				return;
			Transferable transferable = new NodeTransferWrapper(oldNode);
			source.startDrag(dge, DragSource.DefaultMoveNoDrop, transferable, this);
		}

		/**
		 * Called whenever the drop target changes and it has bee accepted (
		 */
		public void dragEnter(DragSourceDragEvent dsde) {
			dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
		}

		public void dragOver(DragSourceDragEvent dsde) {
		}

		public void dragDropEnd(DragSourceDropEvent dsde) {
		}

		public void dragExit(DragSourceEvent dse) {
			dse.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
		}

		public void dropActionChanged(DragSourceDragEvent dsde) {
		}

	}

	protected class AtlasDropTarget implements DropTargetListener {

		final DropTarget target;

		public AtlasDropTarget() throws HeadlessException {
			super();
			target = new DropTarget(atlasTree, this);
		}

		public synchronized void dragEnter(DropTargetDragEvent dtde) {
		}

		public synchronized void dragExit(DropTargetEvent dte) {
		}

		public synchronized void dragOver(DropTargetDragEvent dtde) {
			try {
				Transferable t = dtde.getTransferable();
				Object o = t.getTransferData(NodeTransferWrapper.ATLAS_OBJECT_FLAVOR);
				TreeNode node = getNodeForEvent(dtde);
				if (o instanceof LayerInterface && node instanceof LayerInterface) {
					dtde.acceptDrag(dtde.getDropAction());
					return;
				}
				if (o instanceof MapInterface && node instanceof LayerInterface
						|| node instanceof MapInterface) {
					dtde.acceptDrag(dtde.getDropAction());
					return;
				}
				dtde.rejectDrag();
			} catch (Exception e) {
				log.error("", e);
			}
		}

		public void dropActionChanged(DropTargetDragEvent dtde) {
		}

		public synchronized void drop(DropTargetDropEvent dtde) {
			try {
				TreeNode sourceNode = (TreeNode) dtde.getTransferable().getTransferData(
						NodeTransferWrapper.ATLAS_OBJECT_FLAVOR);

				Point pt = dtde.getLocation();
				DropTargetContext dtc = dtde.getDropTargetContext();
				JTree tree = (JTree) dtc.getComponent();
				TreePath parentpath = tree.getClosestPathForLocation(pt.x, pt.y);
				TreeNode targetNode = (TreeNode) parentpath.getLastPathComponent();

				if (targetNode.equals(sourceNode) || targetNode.getParent().equals(sourceNode)) {
					dtde.rejectDrop();
					return;
				}
				AtlasTreeModel atlasTreeModel = (AtlasTreeModel) atlasTree.getModel();
				if (sourceNode instanceof LayerInterface && targetNode instanceof LayerInterface)
					mergeLayers(atlasTreeModel, (LayerInterface) sourceNode,
							(LayerInterface) targetNode);

				if (targetNode instanceof MapInterface)
					// We can not make a map child of another map
					// -> use it's layer instead
					targetNode = targetNode.getParent();

				if (sourceNode instanceof MapInterface && targetNode instanceof LayerInterface)
					moveMap(atlasTreeModel, (MapInterface) sourceNode, (LayerInterface) targetNode);

			} catch (Exception e) {
				log.error("", e);
				atlasTree.getTreeModel().notifyStructureChanged();
				dtde.rejectDrop();
			}
		}

		protected void mergeLayers(AtlasTreeModel atlasTreeModel, LayerInterface sourceLayer,
				LayerInterface targetLayer) throws InvalidNameException {
			int answer = JOptionPane.showConfirmDialog(null,
					"Are you sure you want to merge the maps of layer\n" + "\""
							+ sourceLayer.getName() + "\" into layer " + "\""
							+ targetLayer.getName() + "\"?", "Confirm layer merging",
					JOptionPane.YES_NO_OPTION);
			if (answer != JOptionPane.YES_OPTION)
				return;
			try {
				atlasTreeModel.mergeLayers(sourceLayer, targetLayer);
			} catch (InvalidNameException e) {
				JOptionPane.showMessageDialog(null, e.getMessage(), "Layer merging failed",
						JOptionPane.ERROR_MESSAGE);
				throw e;
			}
		}

		protected void moveMap(AtlasTreeModel atlasTreeModel, MapInterface map,
				LayerInterface targetLayer) throws InvalidNameException {
			atlasTreeModel.moveMap(map, targetLayer);
		}

		private TreeNode getNodeForEvent(DropTargetDragEvent dtde) {
			Point p = dtde.getLocation();
			DropTargetContext dtc = dtde.getDropTargetContext();
			JTree tree = (JTree) dtc.getComponent();
			TreePath path = tree.getClosestPathForLocation(p.x, p.y);
			return (TreeNode) path.getLastPathComponent();
		}

	}

}
