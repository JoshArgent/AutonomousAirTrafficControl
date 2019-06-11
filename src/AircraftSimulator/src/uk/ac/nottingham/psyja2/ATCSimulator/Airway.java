package uk.ac.nottingham.psyja2.ATCSimulator;

/**
 * Represents a segment of an airway between two waypoints
 * @author Josh Argent
 *
 */
public class Airway
{
	
	private String name;
	private Waypoint from;
	private Waypoint to;
	protected int lowerAltitude;
	protected int upperAltitude;
	
	public Airway(String name, Waypoint from, Waypoint to, int lowerAltitude, int upperAltitude)
	{
		this.name = name;
		this.from = from;
		this.to = to;
		this.lowerAltitude = lowerAltitude;
		this.upperAltitude = upperAltitude;
	}

	/**
	 * Returns the name of the airway it is part of
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Returns the waypoint the airway starts from
	 */
	public Waypoint getFrom()
	{
		return from;
	}

	/**
	 * Returns the waypoint the airways goes to
	 */
	public Waypoint getTo()
	{
		return to;
	}

	/**
	 * Returns the base altitude of the airway (in feet)
	 */
	public int getLowerAltitude()
	{
		return lowerAltitude;
	}

	/**
	 * Returns the upper limit of the airway (in feet)
	 */
	public int getUpperAltitude()
	{
		return upperAltitude;
	}

}
