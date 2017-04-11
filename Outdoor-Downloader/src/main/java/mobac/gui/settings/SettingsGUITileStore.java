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
package mobac.gui.settings;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;

import mobac.mapsources.MapSourcesManager;
import mobac.program.interfaces.MapSource;
import mobac.program.tilestore.TileStore;
import mobac.program.tilestore.TileStoreInfo;
import mobac.program.tilestore.berkeleydb.DelayedInterruptThread;
import mobac.utilities.Utilities;

public class SettingsGUITileStore extends JPanel {

	public final JCheckBox tileStoreEnabled;
	private final JPanel tileStoreInfoPanel;

	private List<TileSourceInfoComponents> tileStoreInfoList = new LinkedList<TileSourceInfoComponents>();
	private JLabel totalTileCountLabel;
	private JLabel totalTileSizeLabel;

	protected DelayedInterruptThread tileStoreAsyncThread = null;

	public SettingsGUITileStore(SettingsGUI gui) {
		super();

		gui.addTab("Tile store", this);

		tileStoreEnabled = new JCheckBox("Enable tile store for map preview and atlas download");

		JPanel tileStorePanel = new JPanel(new BorderLayout());
		tileStorePanel.setBorder(SettingsGUI.createSectionBorder("Tile store settings"));
		tileStorePanel.add(tileStoreEnabled, BorderLayout.CENTER);
		tileStoreInfoPanel = new JPanel(new GridBagLayout());
		// tileStoreInfoPanel.setBorder(createSectionBorder("Information"));

		prepareTileStoreInfoPanel();

		setLayout(new BorderLayout());
		add(tileStorePanel, BorderLayout.NORTH);
		JScrollPane scrollPane = new JScrollPane(tileStoreInfoPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		tileStoreInfoPanel.setMinimumSize(new Dimension(200, 300));
		// scrollPane.setMinimumSize(new Dimension(100, 100));
		scrollPane.setPreferredSize(new Dimension(520, 100));
		scrollPane.setBorder(SettingsGUI.createSectionBorder("Information"));

		add(scrollPane, BorderLayout.CENTER);
	}

	/**
	 * 
	 * @param updateStoreName
	 *            name of the tile store to update or <code>null</code> in case of all tile stores to be updated
	 */
	private void updateTileStoreInfoPanel(String updateStoreName) {
		try {
			TileStore tileStore = TileStore.getInstance();

			long totalTileCount = 0;
			long totalTileSize = 0;
			for (final TileSourceInfoComponents info : tileStoreInfoList) {
				String storeName = info.name;
				Utilities.checkForInterruption();
				int count;
				long size;
				if (updateStoreName == null || info.name.equals(updateStoreName)) {
					TileStoreInfo tsi = tileStore.getStoreInfo(storeName);
					count = tsi.getTileCount();
					size = tsi.getStoreSize();
					info.count = count;
					info.size = size;
					final String mapTileCountText = (count < 0) ? "??" : Integer.toString(count);
					final String mapTileSizeText = Utilities.formatBytes(size);
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							info.countLabel.setText("<html><b>" + mapTileCountText + "</b></html>");
							info.sizeLabel.setText("<html><b>" + mapTileSizeText + "</b></html>");
						}
					});
				} else {
					count = info.count;
					size = info.size;
				}
				totalTileCount += count;
				totalTileSize += size;
			}
			final String totalTileCountText = "<html><b>" + Long.toString(totalTileCount) + "</b></html>";
			final String totalTileSizeText = "<html><b>" + Utilities.formatBytes(totalTileSize) + "</b></html>";
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					totalTileCountLabel.setText(totalTileCountText);
					totalTileSizeLabel.setText(totalTileSizeText);
				}
			});
		} catch (InterruptedException e) {
			SettingsGUI.log.debug("Tile store information retrieval was canceled");
		}

	}

	synchronized void updateTileStoreInfoPanelAsync(final String storeName) {
		if (tileStoreAsyncThread != null)
			return; // An update is currently running
		tileStoreAsyncThread = new DelayedInterruptThread("TileStoreInfoRetriever") {

			@Override
			public void run() {
				if (storeName == null)
					SettingsGUI.log.debug("Updating tilestore information in background");
				else
					SettingsGUI.log.debug("Updating tilestore information for \"" + storeName + "\" in background");
				updateTileStoreInfoPanel(storeName);
				SettingsGUI.log.debug("Updating tilestore information finished");
				tileStoreAsyncThread = null;
			}
		};
		tileStoreAsyncThread.start();
	}

	private void prepareTileStoreInfoPanel() {

		final GridBagConstraints gbc_mapSource = new GridBagConstraints();
		gbc_mapSource.insets = new Insets(5, 10, 5, 10);
		gbc_mapSource.anchor = GridBagConstraints.WEST;
		final GridBagConstraints gbc_mapTiles = new GridBagConstraints();
		gbc_mapTiles.insets = gbc_mapSource.insets;
		gbc_mapTiles.anchor = GridBagConstraints.EAST;
		final GridBagConstraints gbc_eol = new GridBagConstraints();
		gbc_eol.gridwidth = GridBagConstraints.REMAINDER;

		TileStore tileStore = TileStore.getInstance();
		MapSourcesManager mapSourcesManager = MapSourcesManager.getInstance();

		tileStoreInfoPanel.add(new JLabel("<html><b>Map source</b></html>"), gbc_mapSource);
		tileStoreInfoPanel.add(new JLabel("<html><b>Tiles</b></html>"), gbc_mapTiles);
		tileStoreInfoPanel.add(new JLabel("<html><b>Size</b></html>"), gbc_eol);

		ImageIcon trash = Utilities.loadResourceImageIcon("trash.png");

		for (String name : tileStore.getAllStoreNames()) {
			String mapTileCountText = "  ?  ";
			String mapTileSizeText = "    ?    ";
			MapSource mapSource = mapSourcesManager.getSourceByName(name);
			final JLabel mapSourceNameLabel;
			if (mapSource != null)
				mapSourceNameLabel = new JLabel(name);
			else
				mapSourceNameLabel = new JLabel(name + " (unused)");
			final JLabel mapTileCountLabel = new JLabel(mapTileCountText);
			final JLabel mapTileSizeLabel = new JLabel(mapTileSizeText);
			final JButton deleteButton = new JButton(trash);
			TileSourceInfoComponents info = new TileSourceInfoComponents();
			info.name = name;
			info.countLabel = mapTileCountLabel;
			info.sizeLabel = mapTileSizeLabel;
			tileStoreInfoList.add(info);
			deleteButton.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
			deleteButton.setToolTipText("Delete all stored " + name + " tiles.");
			deleteButton.addActionListener(new ClearTileCacheAction(name));

			tileStoreInfoPanel.add(mapSourceNameLabel, gbc_mapSource);
			tileStoreInfoPanel.add(mapTileCountLabel, gbc_mapTiles);
			tileStoreInfoPanel.add(mapTileSizeLabel, gbc_mapTiles);
			tileStoreInfoPanel.add(deleteButton, gbc_eol);
		}
		JSeparator hr = new JSeparator(JSeparator.HORIZONTAL);
		hr.setBorder(BorderFactory.createEtchedBorder(BevelBorder.LOWERED));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		tileStoreInfoPanel.add(hr, gbc);

		JLabel totalMapLabel = new JLabel("<html><b>Total</b></html>");
		totalTileCountLabel = new JLabel("<html><b>??</b></html>");
		totalTileSizeLabel = new JLabel("<html><b>??</b></html>");
		tileStoreInfoPanel.add(totalMapLabel, gbc_mapSource);
		tileStoreInfoPanel.add(totalTileCountLabel, gbc_mapTiles);
		tileStoreInfoPanel.add(totalTileSizeLabel, gbc_mapTiles);
	}

	public void stopThread() {
		Thread t = tileStoreAsyncThread;
		if (t != null)
			t.interrupt();
	}

	private static class TileSourceInfoComponents {
		JLabel sizeLabel;
		JLabel countLabel;
		String name;

		int count = -1;
		long size = 0;
	}

	private class ClearTileCacheAction implements ActionListener {

		String storeName;

		public ClearTileCacheAction(String storeName) {
			this.storeName = storeName;
		}

		public void actionPerformed(ActionEvent e) {
			final JButton b = (JButton) e.getSource();
			b.setEnabled(false);
			b.setToolTipText("Deleting in progress - please wait");
			Thread t = new DelayedInterruptThread("TileStore_" + storeName + "_DeleteThread") {

				@Override
				public void run() {
					try {
						TileStore ts = TileStore.getInstance();
						ts.clearStore(storeName);
						SettingsGUITileStore.this.updateTileStoreInfoPanelAsync(storeName);
						SettingsGUITileStore.this.repaint();
					} catch (Exception e) {
						SettingsGUI.log.error("An error occured while cleaning tile cache: ", e);
					}
				}
			};
			t.start();
		}
	}
}
