package com.nogago.android.maps.plus;

import java.text.Collator;
import java.util.Comparator;
import java.util.List;

import net.osmand.ResultMatcher;
import net.osmand.data.Building;
import net.osmand.data.City;
import net.osmand.data.MapObject;
import net.osmand.data.PostCode;
import net.osmand.data.Street;
import net.osmand.osm.LatLon;
import net.osmand.osm.MapUtils;


public interface RegionAddressRepository {
	
	public String getName();
	
	public LatLon getEstimatedRegionCenter();
	
	// is called on low memory
	public void clearCache();
	
	// called to close resources
	public void close();
	
	public boolean useEnglishNames();
	
	public void setUseEnglishNames(boolean useEnglishNames);
	

	
	public void preloadCities(ResultMatcher<MapObject> resultMatcher);
	
	public void preloadBuildings(Street street, ResultMatcher<Building> resultMatcher);
	
	public void preloadStreets(MapObject o, ResultMatcher<Street> resultMatcher);
	
	
	public List<MapObject> getLoadedCities();
	
	public PostCode getPostcode(String name);
	
	public City getCityById(Long id);
	
	public Street getStreetByName(MapObject cityOrPostcode, String name);
	
	public List<MapObject> getPlaceByName(String city, String street, ResultMatcher<MapObject> cityResultMatcher, ResultMatcher<Street> streetResultMatcher);
	
	public Building getBuildingByName(Street street, String name);
	
	public List<Street> getStreetsIntersectStreets(City city, Street st);
	
	void addCityToPreloadedList(City city);
	
	public LatLon findStreetIntersection(Street street, Street street2);
	
	// TODO remove that method
	public List<Street> fillWithSuggestedStreets(MapObject o, ResultMatcher<Street> resultMatcher, String... names);
	
	public List<MapObject> fillWithSuggestedCities(String name, ResultMatcher<MapObject> resultMatcher, LatLon currentLocation);
	
	
	
	public static class MapObjectNameDistanceComparator implements Comparator<MapObject> {
		
		private final boolean useEnName;
		private Collator collator = Collator.getInstance();
		private final LatLon location;

		public MapObjectNameDistanceComparator(boolean useEnName, LatLon location){
			this.useEnName = useEnName;
			this.location = location;
		}

		@Override
		public int compare(MapObject object1, MapObject object2) {
			if(object1 == null || object2 == null){
				return object2 == object1 ? 0 : (object1 == null ? -1 : 1); 
			} else {
				int c = collator.compare(object1.getName(useEnName), object2.getName(useEnName));
				if(c == 0 && location != null){
					LatLon l1 = object1.getLocation();
					LatLon l2 = object2.getLocation();
					if(l1 == null || l2 == null){
						return l2 == l1 ? 0 : (l1 == null ? -1 : 1);
					}
					return Double.compare(MapUtils.getDistance(location, l1), MapUtils.getDistance(location, l2));
				}
				return c;
			}
		}
	}

}
