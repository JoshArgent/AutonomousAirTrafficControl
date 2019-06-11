package uk.ac.nottingham.psyja2.ATCSimulator;

/**
 * Waypoint represents a single navigational waypoint used in a {@link uk.ac.nottingham.psyja2.ATCSimulator.Scenario}
 * @author Josh Argent
 *
 */
public class Waypoint
{
	
	private String name;
	private Coordinate location;
	private WaypointType type;
	
	/**
	 * @param name the unique waypoint name
	 * @param location the location of the waypoint
	 * @param type the type of waypoint
	 */
	public Waypoint(String name, Coordinate location, WaypointType type)
	{
		this.name = name;
		this.location = location;
		this.type = type;
	}
	
	/**
	 * Returns the unique name of the waypoint
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Returns the location of the waypoint
	 */
	public Coordinate getLocation()
	{
		return location;
	}

	/**
	 * Returns the type of waypoint
	 */
	public WaypointType getType()
	{
		return type;
	}
	
	/**
	 * An enum of the types of waypoint/navigation beacon
	 * @author Josh Argent
	 *
	 */
	public enum WaypointType
	{
		VOR, NDB, INTERSECTION	
	}
}

