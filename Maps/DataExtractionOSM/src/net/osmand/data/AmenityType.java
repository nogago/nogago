package net.osmand.data;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import net.osmand.osm.MapRenderingTypes;


// http://wiki.openstreetmap.org/wiki/Amenity
// POI tags : amenity, leisure, shop, sport, tourism, historic; accessories (internet-access), natural ?
public enum AmenityType {
	// Some of those types are subtypes of Amenity tag 
	HIGHWAY("highway"),
	BARRIER("barrier"),
	WATERWAY("waterway"),
	LOCK("lock"),
	RAILWAY("railway"),
	AEROWAY("aeroway"),
	AERIALWAY("aerialway"),
	POWER("power"),
	MAN_MADE("man_made"),
	LEISURE("leisure"),
	AMENITY("amenity"),
	SHOP("shop"),
	TOURISM("tourism"),
	HISTORIC("historic"),
	MILITARY("military"),
	NATURAL("natural"),
	SPORT("sport"),
	;
	
	private final String defaultTag;
	
	private AmenityType(String defaultTag) {
		this.defaultTag = defaultTag;	
	}
	
	public static AmenityType fromString(String s){
		try {
			return AmenityType.valueOf(s.toUpperCase());
		} catch (IllegalArgumentException e) {
			return AmenityType.AMENITY;
		}
	}
	
	public String getDefaultTag() {
		return defaultTag;
	}
	
	public static String valueToString(AmenityType t){
		return t.toString().toLowerCase();
	}
	
	public static AmenityType[] getCategories(){
		return AmenityType.values();
	}
	
	public static Collection<String> getSubCategories(AmenityType t, MapRenderingTypes renderingTypes){
		Map<AmenityType, Map<String, String>> amenityTypeNameToTagVal = renderingTypes.getAmenityTypeNameToTagVal();
		if(!amenityTypeNameToTagVal.containsKey(t)){
			return Collections.emptyList(); 
		}
		return amenityTypeNameToTagVal.get(t).keySet();
	}
	
	
}