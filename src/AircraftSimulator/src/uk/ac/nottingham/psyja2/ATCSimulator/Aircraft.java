package uk.ac.nottingham.psyja2.ATCSimulator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The Aircraft class represents a single aircraft within the simulation.
 * It provies getter methods for accessing properties of the aircraft 
 * 
 * @author Josh Argent
 *
 */
public class Aircraft
{
	
	private static double EARTH_RADIUS = 3440.0647948164D; // Earth radius in nautical miles
	protected double heading;
	protected double speed;
	protected double altitude;
	protected Coordinate location;
	protected volatile List<Instruction> instructions;
	protected String callsign;
	protected AircraftProfile profile;
	protected boolean isAtMaxCruise = false;
	protected Double airborneTime = 0D;
	
	public final static int MAX_CRUISE_SPEED = -1;
	
	public Aircraft(String callsign, Coordinate start, double heading, double speed, double altitude, AircraftProfile profile)
	{
		this.callsign = callsign;
		this.location = start;
		this.heading = heading;
		this.speed = speed;
		this.altitude = altitude;
		this.profile = profile;
		instructions = new CopyOnWriteArrayList<Instruction>();
		
		// Set the aircraft to max cruise speed mode (will always fly as fast as possible)
		if(speed == MAX_CRUISE_SPEED)
		{
			isAtMaxCruise = true;
			speed = profile.maxSpeed - (altitude / 200);
		}
	}
	
	protected synchronized void updatePosition(double time)
	{		
		// Execute instructions
		ArrayList<Instruction> complete = new ArrayList<Instruction>();
		synchronized(instructions)
		{
			for (Iterator<Instruction> it = instructions.iterator(); it.hasNext();)
			{
				Instruction i = it.next();
				i.execute(this, time);
				if(i.isComplete())
				{
					complete.add(i);
				}
			}
			
			// Remove complete instructions
			for(Instruction i : complete)
			{
				instructions.remove(i);
			}
		}
		
		// Move the plane
		// CREDIT: https://www.movable-type.co.uk/scripts/latlong.html
		double distance = getGroundSpeed() * (time / 60f / 60f);
		double headingR = getTrack() * (Math.PI / 180);
		double lat1 = (location.getLatitude() * (Math.PI / 180));
		double lng1 = (location.getLongitude() * (Math.PI / 180));
		double lat2 = Math.asin(Math.sin(lat1) * Math.cos(distance / EARTH_RADIUS) + Math.cos(lat1) * Math.sin(distance / EARTH_RADIUS) * Math.cos(headingR));
		double lng2 = (lng1 + Math.atan2(Math.sin(headingR) * Math.sin(distance / EARTH_RADIUS) * Math.cos(lat1), Math.cos(distance / EARTH_RADIUS) - Math.sin(lat1) * Math.sin(lat2)));
		location.setLatitude(lat2 / (Math.PI / 180));
		location.setLongitude(lng2 / (Math.PI / 180));
		
		// Ensure that the plane is at the maximum cruise speed
		if(isAtMaxCruise)
		{
			speed = profile.maxSpeed - (altitude / 200);
		}
		
		// Increment airborne time variable
		synchronized(airborneTime)
		{
			airborneTime += time;
		}
	}
	
	/**
	 * Remove all instruction, the aircraft will continue on it's current trajectory
	 */
	public synchronized void clearInstructions()
	{
		synchronized(instructions)
		{
			instructions.clear();
		}
	}
	
	public synchronized List<Instruction> getInstructions()
	{
		synchronized(instructions)
		{
			return instructions;
		}
	}
	
	/**
	 * Returns the current heading of the aircraft (the direction the aircraft is facing) [0 to 360]
	 */
	public double getHeading()
	{
		return heading;
	}
	
	/**
	 * Returns the current track of the aircraft (the direction of the path it is moving) [0 to 360]
	 */
	public double getTrack()
	{
		// Treat the aircraft and wind as two vectors
		double tas = getTrueAirSpeed();
		double v1X = Math.sin(toRadians(heading)) * tas;
		double v1Y = Math.cos(toRadians(heading)) * tas;
		double v2X = -Math.sin(toRadians(Simulator.getInstance().getWindDirection())) * Simulator.getInstance().getWindSpeed();
		double v2Y = -Math.cos(toRadians(Simulator.getInstance().getWindDirection())) * Simulator.getInstance().getWindSpeed();
		double sumX = (v1X + v2X);
		double sumY = (v1Y + v2Y);
		double angle = Math.atan(sumX / sumY);
		
		//  Arc tan returns results between -90 and 90, need to add 180 to correct this
		if(heading > 90 && heading <= 270)
			angle += Math.PI;
		
		// Calculate the resultant vector's direction
		return toDegrees(angle);
	}
	
	/**
	 * Returns the indicated airspeed of the aircraft
	 */
	public double getSpeed()
	{
		return speed;
	}
	
	/**
	 * Returns the true airspeed of the aircraft (indicated airspeed corrected for altitude)
	 */
	public double getTrueAirSpeed()
	{
		// TAS formula from: https://www.ivao.aero/training/documentation/books/PP_ADC_airspeed.pdf
		return speed + (altitude / 200);
	}
	
	/**
	 * Returns the ground speed of the aircraft (rate at which it moves along the ground)
	 */
	public double getGroundSpeed()
	{		
		// Treat the aircraft and wind as two vectors
		double tas = getTrueAirSpeed();
		double v1X = Math.sin(toRadians(heading)) * tas;
		double v1Y = Math.cos(toRadians(heading)) * tas;
		double v2X = -Math.sin(toRadians(Simulator.getInstance().getWindDirection())) * Simulator.getInstance().getWindSpeed();
		double v2Y = -Math.cos(toRadians(Simulator.getInstance().getWindDirection())) * Simulator.getInstance().getWindSpeed();
		double sumX = v1X + v2X;
        double sumY = v1Y + v2Y;

        // Calculate the resultant vector's magnitude
		return Math.sqrt(sumX * sumX + sumY * sumY);
	}
	
	/**
	 * Returns the altitude of the aircraft in feet
	 */
	public double getAltitude()
	{
		return altitude;
	}
	
	/**
	 * Returns the flight level of aircraft in string format (eg. 30,000ft = FL300)
	 */
	public String getFlightLevel()
	{
		int hundredsFeet = (int) Math.round(getAltitude() / 100f);
		return "FL" + String.valueOf(hundredsFeet);
	}
	
	/**
	 * Returns the latitude and longitude coordinates of the aircraft
	 */
	public Coordinate getLocation()
	{
		return location;
	}
	
	/**
	 * Returns the aircraft's callsign
	 */
	public String getCallsign()
	{
		return callsign;
	}
	
	public AircraftProfile getProfile()
	{
		return profile;
	}
	
	public double getAirborneTime()
	{
		synchronized(airborneTime)
		{
			return airborneTime;
		}
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
