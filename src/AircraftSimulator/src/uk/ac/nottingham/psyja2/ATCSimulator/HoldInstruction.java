package uk.ac.nottingham.psyja2.ATCSimulator;

import uk.ac.nottingham.psyja2.ATCSimulator.Waypoint.WaypointType;

/**
 * An instruction to make an aircraft circle in it's current location.
 * @author Josh Argent
 *
 */
public class HoldInstruction extends Instruction
{

	// Holding pattern properties
	private int legLength;
	private Waypoint holdingPoint;
	private double outboundHeading;
	
	// Holding pattern state variables
	private boolean initial = true;
	private double clock; // used for tracking how long each leg has lasted
	private HeadingInstruction headingInstruction;
	private WaypointInstruction waypointInstruction;
	private int currentLeg = 0; // The current leg of the pattern
								// 0 = outbound, 1 inbound
	
	/**
	 * Create a hold instruction for an aircraft with legs of a given time
	 * @param legLength the time in seconds each leg of the holding pattern should last for
	 */
	public HoldInstruction(int legLength)
	{
		this.legLength = legLength;
	}
	
	public HoldInstruction(int legLength, Waypoint waypoint)
	{
		holdingPoint = waypoint;
		this.legLength = legLength;
	}

	@Override
	public void execute(Aircraft aircraft, double time)
	{
		if(initial)
		{
			// Create a 'fake' waypoint at the aircrafts current position if one was not specified
			// This waypoint will be used as the holding point
			if(holdingPoint == null)
				holdingPoint = new Waypoint("HOLDING_POINT", 
						new Coordinate(aircraft.getLocation().getLatitude(), aircraft.getLocation().getLongitude()), 
						WaypointType.INTERSECTION);
			
			// Calculate the 'outbound heading' for the holding pattern (recipricol of current heading)
			outboundHeading = (aircraft.getHeading() + 180) % 360;
			
			// Initially turn aircraft to the outbound heading
			headingInstruction = new HeadingInstruction(outboundHeading);
			clock = 0; // reset the clock
			currentLeg = 0;
			
			initial = false;
		}
		
		
		// Check which leg is currently been flown
		if(currentLeg == 0) // flying outbound, follow outbound heading
		{
			if(!headingInstruction.isComplete())
				headingInstruction.execute(aircraft, time);
			else
			{
				// Add time to the clock
				clock += time;
				
				// Check if the leg is complete
				if(clock >= legLength)
				{
					// Time up, switch to flying inbound to the waypoint
					waypointInstruction = new WaypointInstruction(holdingPoint);
					waypointInstruction.addInstructionCompleteListener(new InstructionListener() {

						@Override
						public void onInstructionComplete(Instruction instruction, Aircraft aircraft)
						{
							// Simply revert to flying outbound again
							// Turn aircraft to the outbound heading
							headingInstruction = new HeadingInstruction(outboundHeading);
							clock = 0; // reset the clock
							currentLeg = 0;			
							
							// Announce that the instruction is complete
							fireInstructionComplete(aircraft);
						}
						
					});
					currentLeg = 1;
				}
			}
			
		}
		else if(currentLeg == 1) // flying inbound to the waypoint, follow waypoint instr.
		{
			if(!waypointInstruction.isComplete())
				waypointInstruction.execute(aircraft, time);
		}

	}

	@Override
	public String toString()
	{
		int min = legLength % 60;
		int sec = legLength - min * 60;
		if(sec == 0)
			return "hold, " + min + " minute legs";
		else
			return "hold, " + min + " minute " + sec + " second legs";
	}
	

}
