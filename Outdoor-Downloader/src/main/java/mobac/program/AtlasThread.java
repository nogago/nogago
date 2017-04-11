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
package mobac.program;

import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import mobac.exceptions.AtlasTestException;
import mobac.exceptions.MapDownloadSkippedException;
import mobac.gui.AtlasProgress;
import mobac.gui.AtlasProgress.AtlasCreationController;
import mobac.program.atlascreators.AtlasCreator;
import mobac.program.atlascreators.tileprovider.DownloadedTileProvider;
import mobac.program.atlascreators.tileprovider.FilteredMapSourceProvider;
import mobac.program.atlascreators.tileprovider.TileProvider;
import mobac.program.download.DownloadJobProducerThread;
import mobac.program.interfaces.AtlasInterface;
import mobac.program.interfaces.DownloadJobListener;
import mobac.program.interfaces.DownloadableElement;
import mobac.program.interfaces.FileBasedMapSource;
import mobac.program.interfaces.LayerInterface;
import mobac.program.interfaces.MapInterface;
import mobac.program.interfaces.MapSource;
import mobac.program.interfaces.MapSource.LoadMethod;
import mobac.program.model.AtlasOutputFormat;
import mobac.program.model.Settings;
import mobac.program.tilestore.TileStore;
import mobac.utilities.GUIExceptionHandler;
import mobac.utilities.Utilities;
import mobac.utilities.tar.TarIndex;
import mobac.utilities.tar.TarIndexedArchive;

import org.apache.log4j.Logger;

public class AtlasThread extends Thread implements DownloadJobListener, AtlasCreationController {

	private static final String MSG_TILESMISSING = "Something is wrong with download of atlas tiles.\n"
			+ "The amount of downladed tiles is not as high as it was calculated.\nTherfore tiles "
			+ "will be missing in the created atlas.\n %d tiles are missing.\n\n"
			+ "Are you sure you want to continue " + "and create the atlas anyway?";

	private static final String MSG_DOWNLOADERRORS = "<html>Multiple tile downloads have failed. "
			+ "Something may be wrong with your connection to the download server or your selected area. "
			+ "<br>Do you want to:<br><br>"
			+ "<u>Continue</u> map download and ignore the errors? (results in blank/missing tiles)<br>"
			+ "<u>Retry</u> to download this map, by starting over?<br>"
			+ "<u>Skip</u> the current map and continue to process other maps in the atlas?<br>"
			+ "<u>Abort</u> the current map and atlas creation process?<br></html>";

	private static final Logger log = Logger.getLogger(AtlasThread.class);
	private static int threadNum = 0;

	private File customAtlasDir = null;

	private DownloadJobProducerThread djp = null;
	private JobDispatcher downloadJobDispatcher;
	private AtlasProgress ap; // The GUI showing the progress

	private AtlasInterface atlas;
	private AtlasCreator atlasCreator = null;
	private PauseResumeHandler pauseResumeHandler;

	private int activeDownloads = 0;
	private int jobsCompleted = 0;
	private int jobsRetryError = 0;
	private int jobsPermanentError = 0;
	private int maxDownloadRetries = 1;

	public AtlasThread(AtlasInterface atlas) throws AtlasTestException {
		this(atlas, atlas.getOutputFormat().createAtlasCreatorInstance());
	}

	public AtlasThread(AtlasInterface atlas, AtlasCreator atlasCreator) throws AtlasTestException {
		super("AtlasThread " + getNextThreadNum());
		ap = new AtlasProgress(this);
		this.atlas = atlas;
		this.atlasCreator = atlasCreator;
		testAtlas();
		TileStore.getInstance().closeAll();
		maxDownloadRetries = Settings.getInstance().downloadRetryCount;
		pauseResumeHandler = new PauseResumeHandler();
	}

	private void testAtlas() throws AtlasTestException {
		try {
			for (LayerInterface layer : atlas) {
				for (MapInterface map : layer) {
					MapSource mapSource = map.getMapSource();
					if (!atlasCreator.testMapSource(mapSource))
						throw new AtlasTestException("The selected atlas output format \"" + atlas.getOutputFormat()
								+ "\" does not support the map source \"" + map.getMapSource() + "\"");
				}
			}
		} catch (AtlasTestException e) {
			throw e;
		} catch (Exception e) {
			throw new AtlasTestException(e);
		}
	}

	private static synchronized int getNextThreadNum() {
		threadNum++;
		return threadNum;
	}

	public void run() {
		GUIExceptionHandler.registerForCurrentThread();
		log.info("Starting altas creation");
		ap.setDownloadControlerListener(this);
		try {
			createAtlas();
			log.info("Altas creation finished");
		} catch (OutOfMemoryError e) {
			System.gc();
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					String message = "Mobile Atlas Creator has run out of memory.";
					int maxMem = Utilities.getJavaMaxHeapMB();
					if (maxMem > 0)
						message += "\nCurrent maximum memory associated to MOBAC: " + maxMem + " MiB";
					JOptionPane.showMessageDialog(null, message, "Out of memory", JOptionPane.ERROR_MESSAGE);
					ap.closeWindow();
				}
			});
			log.error("Out of memory: ", e);
		} catch (InterruptedException e) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					JOptionPane.showMessageDialog(null, "Atlas download aborted", "Information",
							JOptionPane.INFORMATION_MESSAGE);
					ap.closeWindow();
				}
			});
			log.info("Altas creation was interrupted by user");
		} catch (Exception e) {
			log.error("Altas creation aborted because of an error: ", e);
			GUIExceptionHandler.showExceptionDialog(e);
		}
		System.gc();
	}

	/**
	 * Create atlas: For each map download the tiles and perform atlas/map creation
	 */
	protected void createAtlas() throws InterruptedException, IOException {

		long totalNrOfOnlineTiles = atlas.calculateTilesToDownload();

		for (LayerInterface l : atlas) {
			for (MapInterface m : l) {
				// Offline map sources are not relevant for the maximum tile limit.
				if (m.getMapSource() instanceof FileBasedMapSource)
					totalNrOfOnlineTiles -= m.calculateTilesToDownload();
			}
		}

		if (totalNrOfOnlineTiles > 500000) {
			NumberFormat f = DecimalFormat.getInstance();
			JOptionPane.showMessageDialog(null, "Mobile Atlas Creator has detected that you are trying to\n"
					+ "download an extra ordinary large atlas " + "with a very high number of tiles.\n"
					+ "Please reduce the selected areas on high zoom levels and try again.\n"
					+ "The maximum allowed amount of tiles per atlas is " + f.format(500000)
					+ "\nThe number of tile in the currently selected atlas is: " + f.format(totalNrOfOnlineTiles),
					"Atlas download prohibited", JOptionPane.ERROR_MESSAGE);
			return;
		}
		try {
			atlasCreator.startAtlasCreation(atlas, customAtlasDir);
		} catch (AtlasTestException e) {
			JOptionPane.showMessageDialog(null, e.getMessage(), "Atlas format restriction violated",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		ap.initAtlas(atlas);
		ap.setVisible(true);

		Settings s = Settings.getInstance();

		downloadJobDispatcher = new JobDispatcher(s.downloadThreadCount, pauseResumeHandler, ap);
		try {
			for (LayerInterface layer : atlas) {
				atlasCreator.initLayerCreation(layer);
				for (MapInterface map : layer) {
					try {
						while (!createMap(map))
							;
					} catch (InterruptedException e) {
						throw e; // User has aborted
					} catch (MapDownloadSkippedException e) {
						// Do nothing and continue with next map
					} catch (Exception e) {
						log.error("", e);
						String[] options = { "Continue", "Abort", "Show error report" };
						int a = JOptionPane.showOptionDialog(null, "An error occured: " + e.getMessage() + "\n["
								+ e.getClass().getSimpleName() + "]\n\n", "Error", 0, JOptionPane.ERROR_MESSAGE, null,
								options, options[0]);
						switch (a) {
						case 2:
							GUIExceptionHandler.processException(e);
						case 1:
							throw new InterruptedException();
						}
					}
				}
				atlasCreator.finishLayerCreation();
			}
		} catch (InterruptedException e) {
			atlasCreator.abortAtlasCreation();
			throw e;
		} catch (Error e) {
			atlasCreator.abortAtlasCreation();
			throw e;
		} finally {
			// In case of an abort: Stop create new download jobs
			if (djp != null)
				djp.cancel();
			downloadJobDispatcher.terminateAllWorkerThreads();
			if (!atlasCreator.isAborted())
				atlasCreator.finishAtlasCreation();
			ap.atlasCreationFinished();
		}

	}

	/**
	 * 
	 * @param map
	 * @return true if map creation process was finished and false if something went wrong and the user decided to retry
	 *         map download
	 * @throws Exception
	 */
	public boolean createMap(MapInterface map) throws Exception {
		TarIndex tileIndex = null;
		TarIndexedArchive tileArchive = null;

		jobsCompleted = 0;
		jobsRetryError = 0;
		jobsPermanentError = 0;

		ap.initMapDownload(map);
		if (currentThread().isInterrupted())
			throw new InterruptedException();

		// Prepare the tile store directory
		// ts.prepareTileStore(map.getMapSource());

		/***
		 * In this section of code below, tiles for Atlas is being downloaded and saved in the temporary layer tar file
		 * in the system temp directory.
		 **/
		int zoom = map.getZoom();

		final int tileCount = (int) map.calculateTilesToDownload();

		ap.setZoomLevel(zoom);
		try {
			tileArchive = null;
			TileProvider mapTileProvider;
			if (!(map.getMapSource() instanceof FileBasedMapSource)) {
				// For online maps we download the tiles first and then start creating the map if
				// we are sure we got all tiles
				if (!AtlasOutputFormat.TILESTORE.equals(atlas.getOutputFormat())) {
					String tempSuffix = "MOBAC_" + atlas.getName() + "_" + zoom + "_";
					File tileArchiveFile = File.createTempFile(tempSuffix, ".tar", DirectoryManager.tempDir);
					// If something goes wrong the temp file only persists until the VM exits
					tileArchiveFile.deleteOnExit();
					log.debug("Writing downloaded tiles to " + tileArchiveFile.getPath());
					tileArchive = new TarIndexedArchive(tileArchiveFile, tileCount);
				} else
					log.debug("Downloading to tile store only");

				djp = new DownloadJobProducerThread(this, downloadJobDispatcher, tileArchive, (DownloadableElement) map);

				boolean failedMessageAnswered = false;

				while (djp.isAlive() || (downloadJobDispatcher.getWaitingJobCount() > 0)
						|| downloadJobDispatcher.isAtLeastOneWorkerActive()) {
					Thread.sleep(500);
					if (!failedMessageAnswered && (jobsRetryError > 50) && !ap.ignoreDownloadErrors()) {
						pauseResumeHandler.pause();
						String[] answers = new String[] { "Continue", "Retry", "Skip", "Abort" };
						int answer = JOptionPane.showOptionDialog(ap, MSG_DOWNLOADERRORS,
								"Multiple download errors - how to proceed?", 0, JOptionPane.QUESTION_MESSAGE, null,
								answers, answers[0]);
						failedMessageAnswered = true;
						switch (answer) {
						case 0: // Continue
							pauseResumeHandler.resume();
							break;
						case 1: // Retry
							djp.cancel();
							djp = null;
							downloadJobDispatcher.cancelOutstandingJobs();
							return false;
						case 2: // Skip
							downloadJobDispatcher.cancelOutstandingJobs();
							throw new MapDownloadSkippedException();
						default: // Abort or close dialog
							downloadJobDispatcher.cancelOutstandingJobs();
							downloadJobDispatcher.terminateAllWorkerThreads();
							throw new InterruptedException();
						}
					}
				}
				djp = null;
				log.debug("All download jobs has been completed!");
				if (tileArchive != null) {
					tileArchive.writeEndofArchive();
					tileArchive.close();
					tileIndex = tileArchive.getTarIndex();
					if (tileIndex.size() < tileCount && !ap.ignoreDownloadErrors()) {
						int missing = tileCount - tileIndex.size();
						log.debug("Expected tile count: " + tileCount + " downloaded tile count: " + tileIndex.size()
								+ " missing: " + missing);
						int answer = JOptionPane.showConfirmDialog(ap, String.format(MSG_TILESMISSING, missing),
								"Error - tiles are missing - do you want to continue anyway?",
								JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
						if (answer != JOptionPane.YES_OPTION)
							throw new InterruptedException();
					}
				}
				downloadJobDispatcher.cancelOutstandingJobs();
				log.debug("Starting to create atlas from downloaded tiles");
				mapTileProvider = new DownloadedTileProvider(tileIndex, map);
			} else {
				// We don't need to download anything. Everything is already stored locally therefore we can just use it
				mapTileProvider = new FilteredMapSourceProvider(map, LoadMethod.DEFAULT);
			}
			atlasCreator.initializeMap(map, mapTileProvider);
			atlasCreator.createMap();
		} catch (Error e) {
			log.error("Error in createMap: " + e.getMessage(), e);
			throw e;
		} finally {
			if (tileIndex != null)
				tileIndex.closeAndDelete();
			else if (tileArchive != null)
				tileArchive.delete();
		}
		return true;
	}

	public void pauseResumeAtlasCreation() {
		if (pauseResumeHandler.isPaused()) {
			log.debug("Atlas creation resumed");
			pauseResumeHandler.resume();
		} else {
			log.debug("Atlas creation paused");
			pauseResumeHandler.pause();
		}
	}

	public boolean isPaused() {
		return pauseResumeHandler.isPaused();
	}

	public PauseResumeHandler getPauseResumeHandler() {
		return pauseResumeHandler;
	}

	/**
	 * Stop listener from {@link AtlasProgress}
	 */
	public void abortAtlasCreation() {
		try {
			DownloadJobProducerThread djp_ = djp;
			if (djp_ != null)
				djp_.cancel();
			if (downloadJobDispatcher != null)
				downloadJobDispatcher.terminateAllWorkerThreads();
			pauseResumeHandler.resume();
			this.interrupt();
		} catch (Exception e) {
			log.error("Exception thrown in stopDownload()" + e.getMessage());
		}
	}

	public int getActiveDownloads() {
		return activeDownloads;
	}

	public synchronized void jobStarted() {
		activeDownloads++;
	}

	public void jobFinishedSuccessfully(int bytesDownloaded) {
		synchronized (this) {
			ap.incMapDownloadProgress();
			activeDownloads--;
			jobsCompleted++;
		}
		ap.updateGUI();
	}

	public void jobFinishedWithError(boolean retry) {
		synchronized (this) {
			activeDownloads--;
			if (retry)
				jobsRetryError++;
			else {
				jobsPermanentError++;
				ap.incMapDownloadProgress();
			}
		}
		if (!ap.ignoreDownloadErrors())
			Toolkit.getDefaultToolkit().beep();
		ap.setErrorCounter(jobsRetryError, jobsPermanentError);
		ap.updateGUI();
	}

	public int getMaxDownloadRetries() {
		return maxDownloadRetries;
	}

	public AtlasProgress getAtlasProgress() {
		return ap;
	}

	public File getCustomAtlasDir() {
		return customAtlasDir;
	}

	public void setCustomAtlasDir(File customAtlasDir) {
		this.customAtlasDir = customAtlasDir;
	}

}
