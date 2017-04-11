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
package featuretests;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import mobac.mapsources.MapSourcesManager;
import mobac.program.Logging;
import mobac.program.tilestore.TileStore;
import mobac.program.tilestore.berkeleydb.BerkeleyDbTileStore;
import mobac.program.tilestore.berkeleydb.DelayedInterruptThread;

public class TileCoverageTest {

	public static class MyThread extends DelayedInterruptThread {

		public MyThread() {
			super("Test");
		}

		@Override
		public void run() {
			BerkeleyDbTileStore tileStore = (BerkeleyDbTileStore) TileStore.getInstance();
			try {
				int zoom = 8;
				BufferedImage image = tileStore.getCacheCoverage(
						MapSourcesManager.getInstance().getSourceByName("Google Maps"), zoom, new Point(80, 85),
						new Point(1 << zoom, 1 << zoom));
				ImageIO.write(image, "png", new File("test.png"));
				JFrame f = new JFrame("Example");
				f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				f.getContentPane().add(
						new JLabel(new ImageIcon(
								image.getScaledInstance(image.getWidth() * 4, image.getHeight() * 4, 0))));
				f.pack();
				f.setLocationRelativeTo(null);
				f.setVisible(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
			tileStore.closeAll();
		}

	}

	public static void main(String[] args) throws InterruptedException {
		Logging.configureConsoleLogging();

		TileStore.initialize();
		Thread t = new MyThread();
		t.start();
		t.join();
	}

}
