package uk.ac.nottingham.psyja2.ATCSimulator;


/**
 * An instruction to make an aircraft fly directly to a navigation {@link uk.ac.nottingham.psyja2.ATCSimulator.Waypoint}
 * When the aircraft reaches the waypoint it will continue on its current trajectory
 * @author Josh Argent
 *
 */
public class WaypointInstruction extends Instruction implements InstructionListener
{

	private static double EARTH_RADIUS = 6371e3; // earth radius in metres
	private Waypoint waypoint;
	private double targetHeading;
	private HeadingInstruction headingInstr;
	private boolean initial = true;
	public double completionDistance = 1000; // distance to goal waypoint before instruction completes
	
	private double prevDistance = -1;
	private boolean onCourse = false;
	
	/**
	 * @param waypoint the waypoint to fly to
 	 */
	public WaypointInstruction(Waypoint waypoint)
	{
		this.waypoint = waypoint;
	}
	
	@Override
	public void execute(Aircraft aircraft, double time)
	{
		if(initial)
		{
			// First time being executed - calculate the bearing
			calculateBearing(aircraft);
			
			// Turn the aircraft to the heading of the bearing
			headingInstr = new HeadingInstruction(targetHeading);
			headingInstr.addInstructionCompleteListener(this);
			
			initial = false;
		}
				
		// Execute any change to heading instructions
		if(headingInstr != null)
			headingInstr.execute(aircraft, time);
		
		// Check if the aircraft is at the waypoint yet
		// Calculate the distance from aircraft to waypoint
		// CREDIT: https://www.movable-type.co.uk/scripts/latlong.html
		double lat1 = toRadians(aircraft.getLocation().getLatitude());
		double lat2 = toRadians(waypoint.getLocation().getLatitude());
		double deltaLat = toRadians(waypoint.getLocation().getLatitude() - aircraft.getLocation().getLatitude());
		double deltaLng = toRadians(waypoint.getLocation().getLongitude() - aircraft.getLocation().getLongitude());
		double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) + Math.cos(lat1) * Math.cos(lat2) * Math.sin(deltaLng / 2) * Math.sin(deltaLng / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double distanceToWaypoint = EARTH_RADIUS * c;
		if(distanceToWaypoint <= completionDistance)
		{
			fireInstructionComplete(aircraft);
		}	
		
		// Check that the aircraft has not overshot the waypoint
		// This is a bug where an aircraft occationally overshoots
		// TODO: Should try to fix the source of the bug
		if(distanceToWaypoint > prevDistance && prevDistance >= 0)
		{
			if(onCourse)
				fireInstructionComplete(aircraft);
		}
		else
		{
			if(prevDistance >= 0)
			{
				onCourse = true;
			}
			prevDistance = distanceToWaypoint;
		}
	}
	
	/**
	 * Calculates the bearing to the waypoint from the aircrafts position
	 */
	private void calculateBearing(Aircraft aircraft)
	{
		// Get the raw lat and long values
		double lat1 = toRadians(aircraft.getLocation().getLatitude());
		double lng1 = toRadians(aircraft.getLocation().getLongitude());
		double lat2 = toRadians(waypoint.getLocation().getLatitude());
		double lng2 = toRadians(waypoint.getLocation().getLongitude());
		
		// Calculate the bearing between aircraft and waypoint using formula
		// CREDIT: https://www.movable-type.co.uk/scripts/latlong.html
		double diff = lng1 - lng2;
		double y = Math.sin(diff) * Math.cos(lat2);
		double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(diff);
		double targetBearing = toDegrees(Math.atan2(y, x));
		targetBearing = (360 - ((targetBearing + 360) % 360));
		
		// Make a correction for the wind
		double maxWindCorrectionAngle = Math.asin(Simulator.getInstance().getWindSpeed() / aircraft.getTrueAirSpeed());
		double correctionAngle = maxWindCorrectionAngle * Math.cos(toRadians(Simulator.getInstance().getWindDirection()) - toRadians(targetBearing));
		targetHeading = targetBearing - toDegrees(correctionAngle);
	}
	
	@Override
	public void onInstructionComplete(Instruction instruction, Aircraft aircraft)
	{
		// When the heading instruction completes, recalculate the bearing and issue a new instruction
		calculateBearing(aircraft);
		headingInstr = new HeadingInstruction(targetHeading);
		headingInstr.addInstructionCompleteListener(this);
	}

	@Override
	public String toString()
	{
		return "fly direct to " + waypoint.getName();
	}

	private double toRadians(double angle)
	{
		return angle * (Math.PI / 180);
	}
	
	private double toDegrees(double radians)
	{
		return radians / (Math.PI / 180);
	}

}
