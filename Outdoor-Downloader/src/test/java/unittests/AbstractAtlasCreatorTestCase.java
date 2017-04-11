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
package unittests;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.UIManager;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import junit.framework.TestCase;
import junit.framework.TestResult;
import mobac.exceptions.AtlasTestException;
import mobac.program.AtlasThread;
import mobac.program.Logging;
import mobac.program.ProgramInfo;
import mobac.program.atlascreators.AtlasCreator;
import mobac.program.interfaces.AtlasInterface;
import mobac.program.model.Atlas;
import mobac.program.model.Profile;
import mobac.program.model.TileImageType;
import mobac.program.tilestore.TileStore;
import mobac.tools.testtileserver.TestTileServer;
import mobac.tools.testtileserver.servlets.JpgTileGeneratorServlet;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import unittests.helper.TestMapSourcesManager;

/**
 * Base {@link TestCase} used for testing a specific {@link AtlasCreator} implementation and/or downloading of a Atlas.
 */
public abstract class AbstractAtlasCreatorTestCase extends TestCase {

	protected final Logger log;
	protected final File testAtlasDir;
	protected final SecureRandom rnd;

	protected static final TestTileServer TEST_TILE_SERVER;
	protected static final TestMapSourcesManager TEST_TILE_SERVER_MANAGER;

	static {
		Logging.configureConsoleLogging(Level.TRACE,Logging.ADVANCED_LAYOUT);
		ProgramInfo.initialize();
		//Logger.getLogger("mobac").setLevel(Level.INFO);
		TEST_TILE_SERVER = new TestTileServer(18888);
		// TEST_TILE_SERVER.setTileServlet(new PngFileTileServlet(0));
		TEST_TILE_SERVER.setTileServlet(new JpgTileGeneratorServlet(90));
		TEST_TILE_SERVER.start();
		TEST_TILE_SERVER_MANAGER = new TestMapSourcesManager(TEST_TILE_SERVER.getPort(), TileImageType.JPG);
	}

	public AbstractAtlasCreatorTestCase() {
		super();
		log = Logger.getLogger(this.getClass());
		log.setLevel(Level.DEBUG);
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		}
		TileStore.initialize();
		testAtlasDir = new File("target/test-atlases");
		rnd = new SecureRandom();
	}

	@Override
	protected void runTest() throws Throwable {
		log.info("running test \"" + getName() + "\"");
		super.runTest();
	}

	protected void createAtlas(String profileName, Class<? extends AtlasCreator> atlasCreatorClass)
			throws InstantiationException, IllegalAccessException, JAXBException, AtlasTestException,
			InterruptedException, IOException {
		AtlasCreator atlasCreator = atlasCreatorClass.newInstance();
		createAtlas(profileName, atlasCreator);
	}

	protected void createAtlas(String profileName, AtlasCreator atlasCreator) throws JAXBException, AtlasTestException,
			InterruptedException, IOException {
		String profileFile = "profiles/" + Profile.getProfileFileName(profileName);
		InputStream in = ClassLoader.getSystemResourceAsStream(profileFile);
		assertNotNull(in);
		AtlasInterface atlas = loadAtlas(in);
		createAtlas(atlas, atlasCreator);
	}

	protected AtlasInterface loadAtlas(String profileName) throws JAXBException {
		String profileFile = "profiles/" + Profile.getProfileFileName(profileName);
		InputStream in = ClassLoader.getSystemResourceAsStream(profileFile);
		assertNotNull(in);
		return loadAtlas(in);
	}

	protected AtlasInterface loadAtlas(InputStream in) throws JAXBException {
		JAXBContext context = JAXBContext.newInstance(Atlas.class);
		Unmarshaller um = context.createUnmarshaller();
		return (AtlasInterface) um.unmarshal(in);
	}

	/**
	 * 
	 * @param atlas
	 * @param atlasCreator
	 * @return directory in which the atlas has been created
	 * @throws AtlasTestException
	 * @throws InterruptedException
	 * @throws IOException
	 */
	protected File createAtlas(AtlasInterface atlas, AtlasCreator atlasCreator) throws AtlasTestException,
			InterruptedException, IOException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HHmmss_SSSS");
		File customAtlasDir = new File(testAtlasDir, atlasCreator.getClass().getSimpleName() + "_" + atlas.getName()
				+ "_" + sdf.format(new Date()));
		createAtlas(atlas, atlasCreator, customAtlasDir);
		return customAtlasDir;
	}

	protected void createAtlas(AtlasInterface atlas, AtlasCreator atlasCreator, File customAtlasDir)
			throws AtlasTestException, InterruptedException, IOException {
		log.debug("Creating atlas " + atlas.getName() + " using " + atlasCreator.getClass().getSimpleName() + " to \""
				+ customAtlasDir + "\"");
		AtlasThread atlasThread = new AtlasThread(atlas, atlasCreator);
		atlasThread.setCustomAtlasDir(customAtlasDir);
		atlasThread.start();
		atlasThread.join();
	}

	@Override
	public TestResult run() {
		TestResult result = super.run();
		return result;
	}

}
