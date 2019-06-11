package uk.ac.nottingham.psyja2.ATCSimulator;

/**
 * Instruction to make an aircraft turn to a given heading.
 * When the heading is reached, the instruction is complete
 * @author Josh Argent
 *
 */
public class HeadingInstruction extends Instruction
{
	
	private double heading;
	
	/**
	 * @param heading the target heading to turn to
	 */
	public HeadingInstruction(double heading)
	{
		this.heading = normaliseHeading(heading);
	}

	@Override
	public void execute(Aircraft aircraft, double time)
	{
		// Perform the turn as a 'rate 1 turn' eg. 3 degrees per second
		if(!isComplete())
		{
			// Calculate the angle to turn through
			double changeAmount = time * 3;
			
			// Calculate the difference between the heading and target heading
			double difference = heading - aircraft.heading;
		    difference = normaliseHeading(difference);
		    
		    // See if the will complete this iteration or not
		    if(difference < changeAmount)
		    {
		    	// Turn complete
		    	aircraft.heading = heading;
	    		fireInstructionComplete(aircraft);
		    }
		    else
		    {
			    // Work out which way to turn
			    if(difference > 180)
			    	aircraft.heading -= changeAmount; // right turn
			    else
			    	aircraft.heading += changeAmount; // left turn
		    }
		    
		    // Normalise the heading value to 0 to 360
		    aircraft.heading = normaliseHeading(aircraft.heading);
		}		
	}
	
	/*
	 * Will take an angle and normalise it to between 1 and 360
	 */
	private static double normaliseHeading(double heading)
	{
		return (heading + 360D) % 360D;
	}


	@Override
	public String toString()
	{
		return "fly heading " + heading + "°";
	}

}
