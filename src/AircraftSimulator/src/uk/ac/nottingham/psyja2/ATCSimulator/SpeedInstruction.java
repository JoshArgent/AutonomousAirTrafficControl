package uk.ac.nottingham.psyja2.ATCSimulator;

/**
 * An instruction to make an aircraft reduce/increase its speed to an assigned value
 * @author Josh Argent
 *
 */
public class SpeedInstruction extends Instruction
{
	private double targetSpeed;
	private static double ACCELERATION = 2; // Speed increase/decrease per second
	
	/**
	 * Accelerate to the maximum aircraft speed
	 */
	public static int MAX_SPEED = Integer.MAX_VALUE;
	
	/**
	 * Deccelerate to the minimum aircraft speed
	 */
	public static int MIN_SPEED = Integer.MIN_VALUE;

	public SpeedInstruction(double targetSpeed)
	{
		this.targetSpeed = targetSpeed;
	}
	
	@Override
	public void execute(Aircraft aircraft, double time)
	{
		// Calculate the speed change
		double accel = ACCELERATION * time;
		
		// Check that the target speed is within limits for the aircraft
		// If it's not - cap it off at the max/min
		double targetTAS = targetSpeed + (aircraft.getAltitude() / 200);
		if(targetTAS > aircraft.profile.maxSpeed)
			targetSpeed = aircraft.profile.maxSpeed - (aircraft.getAltitude() / 200);
		else if(targetTAS < aircraft.profile.minSpeed)
			targetSpeed = aircraft.profile.minSpeed - (aircraft.getAltitude() / 200);

		// Determine if the speed should increase or decrease
		if(aircraft.speed < targetSpeed)
		{
			// Acceleration
			aircraft.speed += Math.min(targetSpeed - aircraft.speed, accel);
		}
		else if(aircraft.speed > targetSpeed)
		{
			// Deceleration
			aircraft.speed -= Math.min(aircraft.speed - targetSpeed, accel);
		}
		
		if(aircraft.speed == targetSpeed)
			fireInstructionComplete(aircraft);
	}

	@Override
	public String toString()
	{
		return "maintain " + targetSpeed + " knots";
	}

}
