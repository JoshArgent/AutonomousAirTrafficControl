package uk.ac.nottingham.psyja2.ATCAutomation;

import uk.ac.nottingham.psyja2.ATCSimulator.Waypoint;

public class SpatialNodeWaypoint extends SpatialNode
{
	
	private Waypoint waypoint;

	public SpatialNodeWaypoint(String name, Waypoint waypoint, int altitude)
	{
		super(name, waypoint.getLocation(), altitude);
		this.waypoint = waypoint;
	}

	public Waypoint getWaypoint()
	{
		return waypoint;
	}
	
}
