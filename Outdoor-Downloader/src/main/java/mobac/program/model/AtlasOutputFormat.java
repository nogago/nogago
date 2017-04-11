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
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Vector;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import mobac.program.annotations.AtlasCreatorName;
import mobac.program.atlascreators.AFTrack;
import mobac.program.atlascreators.Nogago;
import mobac.program.atlascreators.AlpineQuestMap;
import mobac.program.atlascreators.AndNav;
import mobac.program.atlascreators.AtlasCreator;
import mobac.program.atlascreators.BackCountryNavigator;
import mobac.program.atlascreators.BigPlanetTracks;
import mobac.program.atlascreators.CacheBox;
import mobac.program.atlascreators.CacheWolf;
import mobac.program.atlascreators.GCLive;
import mobac.program.atlascreators.Galileo;
import mobac.program.atlascreators.GarminCustom;
import mobac.program.atlascreators.Glopus;
import mobac.program.atlascreators.GlopusMapFile;
import mobac.program.atlascreators.GoogleEarthOverlay;
import mobac.program.atlascreators.GpsSportsTracker;
import mobac.program.atlascreators.MBTiles;
import mobac.program.atlascreators.MGMaps;
import mobac.program.atlascreators.MagellanRmp;
import mobac.program.atlascreators.Maplorer;
import mobac.program.atlascreators.Maverick;
import mobac.program.atlascreators.MobileTrailExplorer;
import mobac.program.atlascreators.MobileTrailExplorerCache;
import mobac.program.atlascreators.NFComPass;
import mobac.program.atlascreators.NaviComputer;
import mobac.program.atlascreators.OSMAND;
import mobac.program.atlascreators.OSMAND_SQlite;
import mobac.program.atlascreators.OSMTracker;
import mobac.program.atlascreators.OruxMaps;
import mobac.program.atlascreators.OruxMapsSqlite;
import mobac.program.atlascreators.Osmdroid;
import mobac.program.atlascreators.OsmdroidGEMF;
import mobac.program.atlascreators.OsmdroidSQLite;
import mobac.program.atlascreators.Ozi;
import mobac.program.atlascreators.PNGWorldfile;
import mobac.program.atlascreators.PaperAtlasPdf;
import mobac.program.atlascreators.PaperAtlasPng;
import mobac.program.atlascreators.PathAway;
import mobac.program.atlascreators.RMapsSQLite;
import mobac.program.atlascreators.RunGPSAtlas;
import mobac.program.atlascreators.SportsTracker;
import mobac.program.atlascreators.TTQV;
import mobac.program.atlascreators.TileStoreDownload;
import mobac.program.atlascreators.TomTomRaster;
import mobac.program.atlascreators.TrekBuddy;
import mobac.program.atlascreators.TrekBuddyTared;
import mobac.program.atlascreators.TwoNavRMAP;
import mobac.program.atlascreators.Ublox;
import mobac.program.atlascreators.Viewranger;
import mobac.program.jaxb.AtlasOutputFormatAdapter;

@XmlRootElement
@XmlJavaTypeAdapter(AtlasOutputFormatAdapter.class)
public class AtlasOutputFormat implements Comparable<AtlasOutputFormat> {

	public static List<AtlasOutputFormat> FORMATS;

	public static final AtlasOutputFormat TILESTORE = createByClass(TileStoreDownload.class);

	static {
		FORMATS = new ArrayList<AtlasOutputFormat>(1);
		FORMATS.add(createByClass(Nogago.class));
		/*
		FORMATS.add(createByClass(AFTrack.class));
		FORMATS.add(createByClass(AlpineQuestMap.class));
		FORMATS.add(createByClass(AndNav.class));
		FORMATS.add(createByClass(BackCountryNavigator.class));
		FORMATS.add(createByClass(BigPlanetTracks.class));
		FORMATS.add(createByClass(CacheBox.class));
		FORMATS.add(createByClass(CacheWolf.class));
		FORMATS.add(createByClass(Galileo.class));
		FORMATS.add(createByClass(GarminCustom.class));
		FORMATS.add(createByClass(GCLive.class));
		FORMATS.add(createByClass(Glopus.class));
		FORMATS.add(createByClass(GlopusMapFile.class));
		FORMATS.add(createByClass(GoogleEarthOverlay.class));
		FORMATS.add(createByClass(GpsSportsTracker.class));
		FORMATS.add(createByClass(MagellanRmp.class));
		FORMATS.add(createByClass(Maplorer.class));
		FORMATS.add(createByClass(Maverick.class));
		FORMATS.add(createByClass(MBTiles.class));
		FORMATS.add(createByClass(MGMaps.class));
		FORMATS.add(createByClass(MobileTrailExplorer.class));
		FORMATS.add(createByClass(MobileTrailExplorerCache.class));
		FORMATS.add(createByClass(NaviComputer.class));
		FORMATS.add(createByClass(NFComPass.class));
		FORMATS.add(createByClass(OruxMaps.class));
		FORMATS.add(createByClass(OruxMapsSqlite.class));
		FORMATS.add(createByClass(OSMAND.class));
		FORMATS.add(createByClass(OSMAND_SQlite.class));
		FORMATS.add(createByClass(Osmdroid.class));
		FORMATS.add(createByClass(OsmdroidGEMF.class));
		FORMATS.add(createByClass(OsmdroidSQLite.class));
		FORMATS.add(createByClass(OSMTracker.class));
		FORMATS.add(createByClass(Ozi.class));
		FORMATS.add(createByClass(PaperAtlasPdf.class));
		FORMATS.add(createByClass(PaperAtlasPng.class));
		FORMATS.add(createByClass(PathAway.class));
		FORMATS.add(createByClass(PNGWorldfile.class));
		FORMATS.add(createByClass(RMapsSQLite.class));
		FORMATS.add(createByClass(RunGPSAtlas.class));
		FORMATS.add(createByClass(SportsTracker.class));
		FORMATS.add(createByClass(TomTomRaster.class));
		FORMATS.add(createByClass(TTQV.class));
		FORMATS.add(createByClass(TrekBuddyTared.class));
		FORMATS.add(createByClass(TrekBuddy.class));
		FORMATS.add(createByClass(TwoNavRMAP.class));
		FORMATS.add(createByClass(Ublox.class));
		FORMATS.add(createByClass(Viewranger.class));
		FORMATS.add(TILESTORE);
		*/
	}

	public static Vector<AtlasOutputFormat> getFormatsAsVector() {
		return new Vector<AtlasOutputFormat>(FORMATS);
	}

	public static AtlasOutputFormat getFormatByName(String Name) {
		for (AtlasOutputFormat af : FORMATS) {
			if (af.getTypeName().equals(Name))
				return af;
		}
		throw new NoSuchElementException("Unknown atlas format: \"" + Name + "\"");
	}

	private Class<? extends AtlasCreator> atlasCreatorClass;
	private String typeName;
	private String name;

	private static AtlasOutputFormat createByClass(Class<? extends AtlasCreator> atlasCreatorClass) {
		AtlasCreatorName acName = atlasCreatorClass.getAnnotation(AtlasCreatorName.class);
		if (acName == null)
			throw new RuntimeException("AtlasCreator " + atlasCreatorClass.getName() + " has no name");
		String typeName = acName.type();
		if (typeName == null || typeName.length() == 0)
			typeName = atlasCreatorClass.getSimpleName();
		String name = acName.value();
		return new AtlasOutputFormat(atlasCreatorClass, typeName, name);
	}

	private AtlasOutputFormat(Class<? extends AtlasCreator> atlasCreatorClass, String typeName, String name) {
		this.atlasCreatorClass = atlasCreatorClass;
		this.typeName = typeName;
		this.name = name;
	}

	public String toString() {
		return name;
	}

	public Class<? extends AtlasCreator> getMapCreatorClass() {
		return atlasCreatorClass;
	}

	public String getTypeName() {
		return typeName;
	}

	public AtlasCreator createAtlasCreatorInstance() {
		if (atlasCreatorClass == null)
			return null;
		try {
			return atlasCreatorClass.newInstance();
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}

	public int compareTo(AtlasOutputFormat o) {
		return getTypeName().compareTo(o.toString());
	}

}
