package uk.ac.nottingham.psyja2.ATCSimulator.tests;

import static org.junit.Assert.*;
import org.junit.Test;

import uk.ac.nottingham.psyja2.ATCSimulator.Aircraft;
import uk.ac.nottingham.psyja2.ATCSimulator.AircraftProfile;
import uk.ac.nottingham.psyja2.ATCSimulator.Coordinate;
import uk.ac.nottingham.psyja2.ATCSimulator.Simulator;

public class AircraftTests
{

	@Test
	public void testTrueAirspeedCalculation()
	{
		Aircraft aircraft = new Aircraft("BAW225", new Coordinate(52f, -2f), 265, 200, 27000, AircraftProfile.A321);
		assertEquals(335, aircraft.getTrueAirSpeed(), 0.01);		
	}
	
	@Test
	public void testGroundSpeedCalculation()
	{
		Aircraft aircraft = new Aircraft("BAW225", new Coordinate(52f, -2f), 270, 250, 15000, AircraftProfile.A321);
		Simulator.getInstance().setWindDirection(270);
		Simulator.getInstance().setWindSpeed(30);
		assertEquals(295, aircraft.getGroundSpeed(), 0.01);	
	}
	
	@Test
	public void testTrackCalculation()
	{
		Aircraft aircraft = new Aircraft("BAW225", new Coordinate(52f, -2f), 0, 250, 30000, AircraftProfile.A321);
		Simulator.getInstance().setWindDirection(270);
		Simulator.getInstance().setWindSpeed(30);
		assertEquals(4, aircraft.getTrack(), 0.5);	
	}
	
}
