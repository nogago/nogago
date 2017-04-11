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
package mobac.tools;

import java.util.HashMap;

import mobac.mapsources.mappacks.region_europe_east.FreemapSlovakia;
import mobac.mapsources.mappacks.region_europe_east.FreemapSlovakiaCycling;
import mobac.mapsources.mappacks.region_europe_east.FreemapSlovakiaHiking;
import mobac.mapsources.mappacks.region_oceania.NzTopoMaps;
import mobac.program.interfaces.MapSource;
import mobac.program.model.EastNorthCoordinate;

public class Cities {

	private static final HashMap<Class<? extends MapSource>, EastNorthCoordinate> TEST_COORDINATES;

	public static final EastNorthCoordinate NEY_YORK = new EastNorthCoordinate(40.75, -73.88);
	public static final EastNorthCoordinate BERLIN = new EastNorthCoordinate(52.50, 13.39);
	public static final EastNorthCoordinate MOSCOW = new EastNorthCoordinate(55.75, 37.63);
	public static final EastNorthCoordinate PRAHA = new EastNorthCoordinate(50.00, 14.41);
	public static final EastNorthCoordinate BANGALORE = new EastNorthCoordinate(12.95, 77.616667);
	public static final EastNorthCoordinate SHANGHAI = new EastNorthCoordinate(31.2333, 121.4666);
	public static final EastNorthCoordinate WARSZAWA = new EastNorthCoordinate(52.2166, 21.0333);
	public static final EastNorthCoordinate VIENNA = new EastNorthCoordinate(48.20, 16.37);
	public static final EastNorthCoordinate BRATISLAVA = new EastNorthCoordinate(48.154, 17.14);
	public static final EastNorthCoordinate SEOUL = new EastNorthCoordinate(37.55, 126.98);
	public static final EastNorthCoordinate SYDNEY = new EastNorthCoordinate(-33.8, 151.3);
	public static final EastNorthCoordinate PERTH = new EastNorthCoordinate(-31.9, 115.8);
	public static final EastNorthCoordinate BUDAPEST = new EastNorthCoordinate(47.47, 19.05);
	public static final EastNorthCoordinate MUNICH = new EastNorthCoordinate(48.13, 11.58);
	public static final EastNorthCoordinate OSLO = new EastNorthCoordinate(59.91, 10.75);
	public static final EastNorthCoordinate BERN = new EastNorthCoordinate(46.95, 7.45);
	public static final EastNorthCoordinate LONDON = new EastNorthCoordinate(51.51, -0.11);
	public static final EastNorthCoordinate INNSBRUCK = new EastNorthCoordinate(47.26, 11.39);
	public static final EastNorthCoordinate TOKYO = new EastNorthCoordinate(35.683889, 139.774444);
	public static final EastNorthCoordinate TAIPEI = new EastNorthCoordinate(25.033333, 121.633333);
	public static final EastNorthCoordinate WELLINGTON = new EastNorthCoordinate(-41.283333, 174.766667);

	static {
		TEST_COORDINATES = new HashMap<Class<? extends MapSource>, EastNorthCoordinate>();
		// TEST_COORDINATES.put(GoogleMapMaker.class, Cities.BANGALORE);
		// TEST_COORDINATES.put(Cykloatlas.class, Cities.PRAHA);
		// TEST_COORDINATES.put(CykloatlasRelief.class, Cities.PRAHA);
		// TEST_COORDINATES.put(GoogleMapsChina.class, Cities.SHANGHAI);
		// TEST_COORDINATES.put(GoogleMapsKorea.class, Cities.SEOUL);
		// TEST_COORDINATES.put(MicrosoftMapsChina.class, Cities.SHANGHAI);
		// TEST_COORDINATES.put(MicrosoftVirtualEarth.class, Cities.NEY_YORK);
		// TEST_COORDINATES.put(MultimapCom.class, Cities.LONDON);
		// TEST_COORDINATES.put(MultimapOSUkCom.class, Cities.LONDON);
		// TEST_COORDINATES.put(DoCeluPL.class, Cities.WARSZAWA);
		// TEST_COORDINATES.put(YahooMapsJapan.class, TOKYO);
		// TEST_COORDINATES.put(YahooMapsTaiwan.class, TAIPEI);
		// TEST_COORDINATES.put(AustrianMap.class, Cities.VIENNA);
		TEST_COORDINATES.put(FreemapSlovakia.class, Cities.BRATISLAVA);
		TEST_COORDINATES.put(FreemapSlovakiaHiking.class, Cities.BRATISLAVA);
		TEST_COORDINATES.put(FreemapSlovakiaCycling.class, Cities.BRATISLAVA);
		// TEST_COORDINATES.put(NearMap.class, Cities.PERTH);
		// TEST_COORDINATES.put(HubermediaBavaria.class, Cities.MUNICH);
		// TEST_COORDINATES.put(OpenPisteMap.class, Cities.MUNICH);
		// TEST_COORDINATES.put(StatkartTopo2.class, Cities.OSLO);
		// TEST_COORDINATES.put(Turaterkep.class, Cities.BUDAPEST);
		// TEST_COORDINATES.put(Bergfex.class, Cities.INNSBRUCK);
		// TEST_COORDINATES.put(AeroChartsIFR.class, Cities.NEY_YORK);
		// TEST_COORDINATES.put(AeroChartsIFRH.class, Cities.NEY_YORK);
		// TEST_COORDINATES.put(AeroChartsVFR.class, Cities.NEY_YORK);
		// TEST_COORDINATES.put(MicrosoftOrdnanceSurveyExplorer.class, Cities.LONDON);
		// TEST_COORDINATES.put(YandexMap.class, Cities.MOSCOW);
		// TEST_COORDINATES.put(YandexSat.class, Cities.MOSCOW);
		TEST_COORDINATES.put(NzTopoMaps.class, Cities.WELLINGTON);
	}

	public static EastNorthCoordinate getTestCoordinate(MapSource mapSource, EastNorthCoordinate defaultCoordinate) {
		return getTestCoordinate(mapSource.getClass(), defaultCoordinate);
	}

	public static EastNorthCoordinate getTestCoordinate(Class<? extends MapSource> mapSourceClass,
			EastNorthCoordinate defaultCoordinate) {
		EastNorthCoordinate coord = TEST_COORDINATES.get(mapSourceClass);
		if (coord != null)
			return coord;
		else
			return defaultCoordinate;
	}

}
