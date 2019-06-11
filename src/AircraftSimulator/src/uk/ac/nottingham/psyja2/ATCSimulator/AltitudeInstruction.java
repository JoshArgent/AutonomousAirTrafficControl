package uk.ac.nottingham.psyja2.ATCSimulator;

/**
 * An instruction to make an aircraft climb/decend to an assigned altitude
 * @author Josh Argent
 *
 */
public class AltitudeInstruction extends Instruction
{

	private double altitude;
	
	/**
	 * @param altitude the target altitude (in feet)
	 */
	public AltitudeInstruction(double altitude)
	{
		this.altitude = altitude;
	}
	
	
	@Override
	public void execute(Aircraft aircraft, double time)
	{
		// Calculate the amount of climb/decent for the given time
		double climbAmount = (aircraft.profile.climbRate / 60) * time;
		
		// Climb or decend the aircraft
		if(aircraft.altitude < altitude)
		{
			aircraft.altitude += Math.min(climbAmount, altitude - aircraft.altitude);
		}
		else if(aircraft.altitude > altitude)
		{
			aircraft.altitude -= Math.min(climbAmount, aircraft.altitude - altitude);
		}

		// Test if the instruction is complete
		if(aircraft.altitude == altitude)
		{
			fireInstructionComplete(aircraft);
		}
	}

	@Override
	public String toString()
	{
		return "climb/descend FL" + (altitude / 100);
	}

}
