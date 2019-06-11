package uk.ac.nottingham.psyja2.ATCSimulator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import uk.ac.nottingham.psyja2.ATCSimulator.Waypoint.WaypointType;

/**
 * Represents an entire network of airways and waypoints
 * @author Josh Argent
 *
 */
public class Scenario
{

	/**
	 * All the waypoints in the scenario. The waypoint name (eg. "DESIG") is the key
	 */
	public Map<String, Waypoint> waypoints = new HashMap<>();
	
	/**
	 * All the airway segments in the scenario
	 */
	public List<Airway> airways = new ArrayList<>();
	
	/**
	 * The minimum corner coordinate of the scenario
	 */
	public Coordinate displayMin;
	
	/**
	 * The maximum corner coordinate of the scenario
	 */
	public Coordinate displayMax;
	
	/**
	 * Create a new Scenario object.
	 * Parses JSON airway and waypoint data from the dataFile
	 * @param dataFile
	 * @throws IOException
	 * @throws ParseException
	 */
	public Scenario(String dataFile) throws IOException, ParseException
	{
		String rawJSON = new String(Files.readAllBytes(Paths.get(dataFile)));
		JSONParser parser = new JSONParser();
		JSONObject rootObject = (JSONObject) parser.parse(rawJSON);
		JSONArray waypointsArray = (JSONArray) rootObject.get("waypoints");
		JSONArray airwaysArray = (JSONArray) rootObject.get("airways");
		// Get the specified bounds
		String minLat = (String) rootObject.get("minLat");
		String minLng = (String) rootObject.get("minLng");
		String maxLat = (String) rootObject.get("maxLat");
		String maxLng = (String) rootObject.get("maxLng");
		Coordinate minCorner = new Coordinate(minLat, minLng);
		Coordinate maxCorner = new Coordinate(maxLat, maxLng);
		
		// Parse the waypoints from JSON array
		for (int i = 0; i < waypointsArray.size(); i++)
		{
			JSONObject waypointObject = (JSONObject) waypointsArray.get(i);
			String name = (String) waypointObject.get("name");
			String lat = (String) waypointObject.get("lat");
			String lng = (String) waypointObject.get("lng");
			String type = (String) waypointObject.get("type");
			Coordinate location = new Coordinate(lat, lng);
			if(location.getLatitude() < minCorner.getLatitude() || location.getLatitude() > maxCorner.getLatitude() ||
					location.getLongitude() < minCorner.getLongitude() || location.getLongitude() > maxCorner.getLongitude())
			{
				// This waypoint is outside the specified bounds
				continue;
			}
			WaypointType waypointType = WaypointType.VOR;
			if(type.equalsIgnoreCase("INTERSECTION"))
				waypointType = WaypointType.INTERSECTION;
			else if(type.equalsIgnoreCase("INTERSECTION"))
				waypointType = WaypointType.NDB;
			Waypoint waypoint = new Waypoint(name, new Coordinate(lat, lng), waypointType);
			// Add to the list of waypoints, ensuring no duplicate waypoints
			if(!waypoints.containsKey(name))
				waypoints.put(name, waypoint);
		}
		
		// Parse the airways from JSON array
		for (int i = 0; i < airwaysArray.size(); i++)
		{
			JSONObject airwayObject = (JSONObject) airwaysArray.get(i);
			String name = (String) airwayObject.get("name");
			String from = (String) airwayObject.get("from");
			String to = (String) airwayObject.get("to");
			int min = ((Long) airwayObject.get("minLevel")).intValue();
			int max = ((Long) airwayObject.get("maxLevel")).intValue();
			
			Airway existing = getAirway(from, to);
			if(existing != null)
			{
				// An existing airway with this route exists
				// Just modify the min/max levels
				existing.lowerAltitude = Math.min(existing.getLowerAltitude(), min);
				existing.upperAltitude = Math.max(existing.getUpperAltitude(), max);
			}
			else
			{
				if(waypoints.containsKey(from) && waypoints.containsKey(to))
				{
					// This is a new airway, create and add the object to the list
					Airway airway = new Airway(name, waypoints.get(from), waypoints.get(to), min, max);
					airways.add(airway);
				}				
			}						
		}
		
		// Remove waypoints which are not connected to any airway
		ArrayList<Waypoint> toRemove = new ArrayList<>();
		for(Waypoint waypoint : waypoints.values())
		{
			boolean used = false;
			for(Airway airway : airways)
			{
				if(airway.getFrom().equals(waypoint) || airway.getTo().equals(waypoint))
				{
					used = true;
					break;
				}
			}
			if(!used)
			{
				toRemove.add(waypoint);
			}
		}
		for(Waypoint waypoint : toRemove)
		{
			waypoints.remove(waypoint.getName());
		}
		
		// Find the min and max bounds of the scenario area
		double minLatD = 200f;
		double maxLatD = 0f;
		double minLngD = 200f;
		double maxLngD = 0f;
		for (Waypoint waypoint : waypoints.values())
		{
			if(waypoint.getLocation().getLatitude() < minLatD)
				minLatD = waypoint.getLocation().getLatitude();
			if(waypoint.getLocation().getLongitude() < minLngD)
				minLngD = waypoint.getLocation().getLongitude();
			if(waypoint.getLocation().getLatitude() > maxLatD)
				maxLatD = waypoint.getLocation().getLatitude();
			if(waypoint.getLocation().getLongitude() > maxLngD)
				maxLngD = waypoint.getLocation().getLongitude();
		}
		displayMin = new Coordinate(minLatD, minLngD);
		displayMax = new Coordinate(maxLatD, maxLngD);
	}
	
	private Airway getAirway(String from, String to)
	{
		for(Airway airway : airways)
		{
			if(airway.getFrom().getName().equalsIgnoreCase(from) && 
					airway.getTo().getName().equalsIgnoreCase(to))
			{
				return airway;
			}
		}
		return null;
	}
	
}
