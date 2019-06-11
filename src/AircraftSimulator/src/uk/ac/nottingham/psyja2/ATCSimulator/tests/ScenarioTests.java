package uk.ac.nottingham.psyja2.ATCSimulator.tests;

import static org.junit.Assert.*;

import java.io.IOException;

import org.json.simple.parser.ParseException;
import org.junit.Test;

import uk.ac.nottingham.psyja2.ATCSimulator.Airway;
import uk.ac.nottingham.psyja2.ATCSimulator.Coordinate;
import uk.ac.nottingham.psyja2.ATCSimulator.Scenario;
import uk.ac.nottingham.psyja2.ATCSimulator.Simulator;
import uk.ac.nottingham.psyja2.ATCSimulator.Waypoint;
import uk.ac.nottingham.psyja2.ATCSimulator.Waypoint.WaypointType;

public class ScenarioTests
{

	/*
	 * Test that a coordinate is correctly parsed from a Degrees, Minutes, Seconds string
	 * to a decimal degree value
	 */
	@Test
	public void testCoordinateFromString()
	{
		Coordinate c1 = new Coordinate("503114N", "0012000W");
		assertEquals(50.52056f, c1.getLatitude(), 0.00001);
		assertEquals(-1.333333f, c1.getLongitude(), 0.00001);
		
		Coordinate c2 = new Coordinate("565341N", "0035738E");
		assertEquals(56.89472f, c2.getLatitude(), 0.00001);
		assertEquals(3.960556f, c2.getLongitude(), 0.00001);
	}
	
	@Test
	public void testAirwayData() throws IOException, ParseException
	{
		Scenario scenario = new Scenario(Simulator.SCENARIO_FILE);
		for(Airway airway : scenario.airways)
		{
			if(airway.getName().equalsIgnoreCase("Y904") && 
					airway.getFrom().getName().equalsIgnoreCase("SMOKI") && 
					airway.getTo().getName().equalsIgnoreCase("WICK"))
			{
				assertEquals(75, airway.getLowerAltitude());
				assertEquals(155, airway.getUpperAltitude());
				return;
			}
		}
		fail();
	}
	
	@Test
	public void testWaypointData() throws IOException, ParseException
	{
		Scenario scenario = new Scenario(Simulator.SCENARIO_FILE);
		assertTrue(scenario.waypoints.containsKey("SANDY"));
		Waypoint SANDY = scenario.waypoints.get("SANDY");
		assertEquals(WaypointType.INTERSECTION, SANDY.getType());
		assertEquals(51.06416667f, SANDY.getLocation().getLatitude(), 0.00001);
		assertEquals(1.06750000f, SANDY.getLocation().getLongitude(), 0.00001);
	}


}
