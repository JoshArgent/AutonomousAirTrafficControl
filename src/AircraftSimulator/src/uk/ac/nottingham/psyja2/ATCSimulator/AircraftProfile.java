package uk.ac.nottingham.psyja2.ATCSimulator;

/**
 * Defines the properties and behaviour of a particular type of aircraft.
 * Also contains some static profiles for common types of aircraft
 * @author Josh Argent
 */
public class AircraftProfile
{

	/**
	 * The minimum true airspeed the aircraft can cruise at
	 */
	public int minSpeed;
	
	/**
	 * The maximum true airspeed the aircraft can cruise at
	 */
	public int maxSpeed;
	
	/**
	 * The normal climb rate of the aircraft (in feet per minute)
	 */
	public int climbRate;
	
	/**
	 * The maximum cruise altitude for the aircraft (in feet)
	 */
	public int maxAltitude;
	
	/**
	 * The type identifier of the aircraft (eg. B738)
	 */
	public String type;
	
	public AircraftProfile(String type, int minSpeed, int maxSpeed, int climbRate, int maxAltitude)
	{
		this.type = type;
		this.minSpeed = minSpeed;
		this.maxSpeed = maxSpeed;
		this.climbRate = climbRate;
		this.maxAltitude = maxAltitude;
	}
	
	// Boeing 737-700 and 737-800 data from: https://en.wikipedia.org/wiki/Boeing_737_Next_Generation
	public static AircraftProfile B738 = new AircraftProfile("B738", 200, 455, 1800, 41000);
	public static AircraftProfile B737 = new AircraftProfile("B737", 200, 450, 1800, 41000);
	
	// Boeing 737-300 data from: https://en.wikipedia.org/wiki/Boeing_737_Classic
	public static AircraftProfile B733 = new AircraftProfile("B737", 200, 430, 1800, 37000);
	
	// Boeing 757-200/300 data from: https://en.wikipedia.org/wiki/Boeing_757
	public static AircraftProfile B752 = new AircraftProfile("B752", 200, 462, 2000, 42000);
	public static AircraftProfile B753 = new AircraftProfile("B753", 200, 462, 2000, 42000);
	
	// Boeing 767-200 data from: https://en.wikipedia.org/wiki/Boeing_767
	public static AircraftProfile B762 = new AircraftProfile("B762", 200, 459, 1800, 43100);
	
	// Boeing 747-400 data from: https://en.wikipedia.org/wiki/Boeing_747
	public static AircraftProfile B744 = new AircraftProfile("B744", 200, 504, 1800, 39000);
	
	// Boeing 777-200/300 data from: https://en.wikipedia.org/wiki/Boeing_777
	public static AircraftProfile B772 = new AircraftProfile("B772", 200, 481, 1800, 43100);
	public static AircraftProfile B773 = new AircraftProfile("B773", 200, 481, 1800, 43100);
	
	// Boeing 787-800 data from: https://en.wikipedia.org/wiki/Boeing_787_Dreamliner
	public static AircraftProfile B788 = new AircraftProfile("B788", 200, 490, 2000, 43000);
	
	// Dash-8 Q400 data from: https://en.wikipedia.org/wiki/Bombardier_Dash_8
	public static AircraftProfile DH8D = new AircraftProfile("DH8D", 180, 350, 1600, 27000);
	
	// A319/A320/A321 data from: https://en.wikipedia.org/wiki/Airbus_A320_family
	public static AircraftProfile A319 = new AircraftProfile("A319", 200, 450, 1800, 40000);
	public static AircraftProfile A320 = new AircraftProfile("A320", 200, 450, 1800, 40000);
	public static AircraftProfile A321 = new AircraftProfile("A321", 200, 450, 1800, 40000);
	
	// A330-200/300 data from: https://en.wikipedia.org/wiki/Airbus_A330
	public static AircraftProfile A332 = new AircraftProfile("A332", 200, 490, 1800, 41100);
	public static AircraftProfile A333 = new AircraftProfile("A333", 200, 490, 1800, 41100);
	
	// A340-200 data from: https://en.wikipedia.org/wiki/Airbus_A340
	public static AircraftProfile A342 = new AircraftProfile("A342", 200, 447, 1800, 41000);
	
	// A380-800 data from: https://en.wikipedia.org/wiki/Airbus_A380
	public static AircraftProfile A388 = new AircraftProfile("A388", 200, 488, 1800, 43000);
}
