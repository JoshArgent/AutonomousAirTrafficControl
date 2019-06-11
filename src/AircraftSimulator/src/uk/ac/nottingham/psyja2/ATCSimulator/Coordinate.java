package uk.ac.nottingham.psyja2.ATCSimulator;

/**
 * Represents a latitude and longitude coordinate
 * @author Josh Argent
 *
 */
public class Coordinate
{

	private double latitude;
	private double longitude;
	
	/**
	 * Construct from decimal latitude and longitude values (in degrees)
	 * @param lat
	 * @param lng
	 */
	public Coordinate(double lat, double lng)
	{
		setLatitude(lat);
		setLongitude(lng);
	}
	
	/**
	 * Parse latitude and longitude values from string, such as: "514741N" and "0011218E"<br>
	 * Strings should be in degrees, minutes, seconds form
	 * @param lat
	 * @param lng
	 */
	public Coordinate(String lat, String lng)
	{
		// Convert the latitude DMS to fraction
		int latDeg = Integer.valueOf(lat.substring(0, 2));
		int latMin = Integer.valueOf(lat.substring(2, 4));
		double latSec = Float.valueOf(lat.substring(4, lat.length() - 1));
		String latDir = lat.substring(lat.length() - 1, lat.length());
		latitude = latSec / 60f;
		latitude += latMin;
		latitude /= 60f;
		latitude += latDeg;
		if(latDir.equalsIgnoreCase("S"))
			latitude *= -1;
		
		// Convert longitude DMS to fraction
		int lngDeg = Integer.valueOf(lng.substring(0, 3));
		int lngMin = Integer.valueOf(lng.substring(3, 5));
		double lngSec = Float.valueOf(lng.substring(5, lng.length() - 1));
		String lngDir = lng.substring(lng.length() - 1, lng.length());
		longitude = lngSec / 60f;
		longitude += lngMin;
		longitude /= 60f;
		longitude += lngDeg;
		if(lngDir.equalsIgnoreCase("W"))
			longitude *= -1;
			
	}
	
	/**
	 * Returns the latitude in degrees
	 */
	public double getLatitude()
	{
		return latitude;
	}

	/**
	 * Set the latitude
	 * @param latitude the latitude value in degrees
	 */
	public void setLatitude(double latitude)
	{
		this.latitude = latitude;
	}

	/**
	 * Returns the longitude in degrees
	 */
	public double getLongitude()
	{
		return longitude;
	}

	/**
	 * Set the longitude
	 * @param longitude the longitude value in degrees
	 */
	public void setLongitude(double longitude)
	{
		this.longitude = longitude;
	}
	
	/**
	 * Gets the X position for a given display width
	 */
	protected double getX(double minLong, double maxLong, int displayWidth)
	{
		return displayWidth * ((longitude - minLong) / (maxLong - minLong));
	}
	
	/**
	 * Gets the Y position for a given display height
	 */
	protected double getY(double minLat, double maxLat, int displayHeight)
	{
		return displayHeight - (displayHeight * ((latitude - minLat) / (maxLat - minLat)));
	}
	
}
