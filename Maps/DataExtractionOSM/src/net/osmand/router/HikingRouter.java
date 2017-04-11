package net.osmand.router;

import java.util.LinkedHashMap;
import java.util.Map;

import net.osmand.binary.BinaryMapDataObject;
import net.osmand.binary.BinaryMapIndexReader.TagValuePair;
import net.osmand.osm.MapRenderingTypes;
import net.osmand.router.BinaryRoutePlanner.RouteSegment;

public class HikingRouter extends VehicleRouter {
	// no distinguish for speed in city/outside city (for now)
	private Map<String, Double> hikingNotDefinedValues = new LinkedHashMap<String, Double>();
	private Map<String, Double> hikingPriorityValues = new LinkedHashMap<String, Double>();
	// in m/s
	{
		hikingNotDefinedValues.put("motorway", 6d);
		hikingNotDefinedValues.put("motorway_link", 6d);
		hikingNotDefinedValues.put("trunk", 6d);
		hikingNotDefinedValues.put("trunk_link", 6d);
		hikingNotDefinedValues.put("primary", 6d);
		hikingNotDefinedValues.put("primary_link", 6d);
		hikingNotDefinedValues.put("secondary", 6d);
		hikingNotDefinedValues.put("secondary_link", 6d);
		hikingNotDefinedValues.put("tertiary", 6d);
		hikingNotDefinedValues.put("tertiary_link", 6d);
		hikingNotDefinedValues.put("residential", 6d);
		hikingNotDefinedValues.put("road", 6d);
		hikingNotDefinedValues.put("service", 6d);
		hikingNotDefinedValues.put("unclassified", 6d);
		hikingNotDefinedValues.put("track", 6d);
		hikingNotDefinedValues.put("path", 6d);
		hikingNotDefinedValues.put("living_street", 6d);
		hikingNotDefinedValues.put("pedestrian", 6d);
		hikingNotDefinedValues.put("footway", 6d);
		hikingNotDefinedValues.put("byway", 6d);
		hikingNotDefinedValues.put("cycleway", 6d);
		hikingNotDefinedValues.put("bridleway", 6d);
		hikingNotDefinedValues.put("services", 6d);
		hikingNotDefinedValues.put("steps", 6d);
		
		

//		hikingPriorityValues.put("motorway", 0.6);
//		hikingPriorityValues.put("motorway_link", 0.6);
		hikingPriorityValues.put("trunk", 0.6);
		hikingPriorityValues.put("trunk_link", 0.6);
		hikingPriorityValues.put("primary", 0.7);
		hikingPriorityValues.put("primary_link", 0.7);
		hikingPriorityValues.put("secondary", 0.8);
		hikingPriorityValues.put("secondary_link", 0.8);
		hikingPriorityValues.put("tertiary", 1d);
		hikingPriorityValues.put("tertiary_link", 1d);
		hikingPriorityValues.put("residential", 1d);
		hikingPriorityValues.put("service", 1d);
		hikingPriorityValues.put("unclassified", 1d);
		hikingPriorityValues.put("road", 1d);
		hikingPriorityValues.put("track", 1.5);
		hikingPriorityValues.put("path", 1.5);
		hikingPriorityValues.put("living_street", 1.1);
		hikingPriorityValues.put("pedestrian", 1.3d);
		hikingPriorityValues.put("footway", 1.3d);
		hikingPriorityValues.put("byway", 1d);
		hikingPriorityValues.put("cycleway", 0.8);
		hikingPriorityValues.put("bridleway", 0.8);
		hikingPriorityValues.put("services", 1d);
		hikingPriorityValues.put("steps", 1.2d);
		hikingPriorityValues.put("foot", 1.7d);
		hikingPriorityValues.put("hiking", 1.7d);
	}

	@Override
	public boolean acceptLine(TagValuePair pair) {
		if (pair.tag.equals("highway") || pair.tag.equals("route")) {
			return hikingNotDefinedValues.containsKey(pair.value);
		}
		return false;
	}

	@Override
	public boolean acceptPoint(TagValuePair pair) {
		if (pair.tag.equals("highway") && pair.value.equals("traffic_signals")) {
			return true;
		} else if (pair.tag.equals("railway") && pair.value.equals("crossing")) {
			return true;
		} else if (pair.tag.equals("railway") && pair.value.equals("level_crossing")) {
			return true;
		}
		return false;
	}
	

	public boolean isOneWay(int highwayAttributes) {
		return MapRenderingTypes.isOneWayWay(highwayAttributes) || MapRenderingTypes.isRoundabout(highwayAttributes);
	}

	/**
	 * return delay in seconds
	 */
	@Override
	public double defineObstacle(BinaryMapDataObject road, int point) {
		if ((road.getTypes()[0] & 3) == MapRenderingTypes.POINT_TYPE) {
			// possibly not only first type needed ?
			TagValuePair pair = road.getTagValue(0);
			if (pair != null) {
				if (pair.tag.equals("highway") && pair.value.equals("traffic_signals")) {
					return 30;
				} else if (pair.tag.equals("railway") && pair.value.equals("crossing")) {
					return 15;
				} else if (pair.tag.equals("railway") && pair.value.equals("level_crossing")) {
					return 15;
				}
			}
		}
		return 0;
	}
	/**	with route-tags
	 * 
	 */
	/*
	@Override
	public double getRoadPriorityToCalculateRoute(BinaryMapDataObject road) {
		TagValuePair pair = road.getTagValue(0);
		TagValuePair pairRoute = road.getTagValue(1);
		boolean highway = "highway".equals(pair.tag);
		double priority = 0;
		if (road.getTypes().length > 1) {
			if (pairRoute.value.equals("hiking")) {
				priority = hikingPriorityValues.get(pairRoute.value);
			} else if  (pairRoute.value.equals("foot")) {
				priority = hikingPriorityValues.get(pairRoute.value);
			}
		} else {
			priority = highway && hikingPriorityValues.containsKey(pair.value) ? hikingPriorityValues.get(pair.value): 1d; 
		}
		return priority;
	}
/*
	/**
	 * without route-tags
	 */
	
	@Override
	public double getRoadPriorityToCalculateRoute(BinaryMapDataObject road) {
		TagValuePair pair = road.getTagValue(0);
		boolean highway = "highway".equals(pair.tag);
		double priority;
		priority = highway && hikingPriorityValues.containsKey(pair.value) ? hikingPriorityValues.get(pair.value): 1d; 
		return priority;
	}

	/**
	 * return speed in m/s
	 */
	@Override
	public double defineSpeed(BinaryMapDataObject road) {
		TagValuePair pair = road.getTagValue(0);
		double speed = 1.1d;
		boolean highway = "highway".equals(pair.tag);
		double priority = highway && hikingPriorityValues.containsKey(pair.value) ? hikingPriorityValues.get(pair.value) : 1d;
		if (speed == 0 && highway) {
			Double value = hikingNotDefinedValues.get(pair.value);
			if (value != null) {
				speed = 1.2;
			}
		}
		return speed*priority;
	}

	/**
	 * Used for A* routing to calculate g(x)
	 * 
	 * @return minimal speed at road
	 */
	@Override
	public double getMinDefaultSpeed() {
		return 2;
	}

	/**
	 * Used for A* routing to predict h(x) : it should be great than (!) any g(x)
	 * 
	 * @return maximum speed to calculate shortest distance
	 */
	@Override
	public double getMaxDefaultSpeed() {
		return 6;
	}

	@Override
	public double calculateTurnTime(RouteSegment segment, RouteSegment next, int segmentEnd) {
		boolean end = (segmentEnd == segment.road.getPointsLength() - 1 || segmentEnd == 0);
		boolean start = next.segmentStart == 0;
		if (end) {
			if(!start){
				return 5;
			}
			return 0;
		} else {
			return 5;
		}
	}

}