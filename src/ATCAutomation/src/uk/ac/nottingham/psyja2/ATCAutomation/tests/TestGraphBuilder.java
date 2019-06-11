package uk.ac.nottingham.psyja2.ATCAutomation.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import uk.ac.nottingham.psyja2.ATCAutomation.GraphBuilder;
import uk.ac.nottingham.psyja2.ATCAutomation.PathFinding.WHCAStar;
import uk.ac.nottingham.psyja2.ATCSimulator.Coordinate;
import uk.ac.nottingham.psyja2.ATCSimulator.Simulator;

public class TestGraphBuilder
{

	@Test
	public void testBearingFunction()
	{
		double result1 = GraphBuilder.calculateBearing(50.06639d, -5.714722d, 58.64389d, -3.07d);
		assertEquals(009.1198, result1, 0.0001);
		
		double result2 = GraphBuilder.calculateBearing(56, 3, 50, -6);
		assertEquals(225.7465, result2, 0.0001);
	}
	
	@Test
	public void testDistanceFunction()
	{
		double result1 = GraphBuilder.calculateDistance(50.06639d, -5.714722d, 58.64389d, -3.07d);
		assertEquals(968.9, result1 / 1000, 0.1);
		
		double result2 = GraphBuilder.calculateDistance(56, 3, 50, -6);
		assertEquals(897.4, result2 / 1000, 0.1);
	}
	
	@Test
	public void testMidpointFunction()
	{
		Coordinate coord = GraphBuilder.calculateMidpoint(50.06639d, -5.714722d, 58.64389d, -3.07d);
		assertEquals(54.3623, coord.getLatitude(), 0.0001);
		assertEquals(-4.5307, coord.getLongitude(), 0.0001);
	}
	
	@Test
	public void testIntermediatePointsFunction()
	{
		Coordinate result[] = GraphBuilder.calculateIntermediatePoints(new Coordinate(50.06639d, -5.714722d), 
				new Coordinate(58.64389d, -3.07d), 4);
		assertEquals(3, result.length);
		assertEquals(52.2158, result[0].getLatitude(), 0.0001);
		assertEquals(-5.1514, result[0].getLongitude(), 0.0001);
		assertEquals(54.3623, result[1].getLatitude(), 0.0001);
		assertEquals(-4.5307, result[1].getLongitude(), 0.0001);
		assertEquals(56.5052, result[2].getLatitude(), 0.0001);
		assertEquals(-3.8416, result[2].getLongitude(), 0.0001);
	}
	
	@Test
	public void testIntersectionFunction()
	{
		Coordinate result = GraphBuilder.doLinesIntersect(50.06639d, -5.714722d, 58.64389d, -3.07d, 56, 3, 50, -6);
		assertEquals(result.getLatitude(), 50.2693, 0.0001);
		assertEquals(result.getLongitude(), -005.6638, 0.0001);
	}
	
	@Test
	public void testPerformance()
	{
		WHCAStar graph = new WHCAStar(10);
		GraphBuilder.buildGraph(Simulator.getInstance().getScenario(), graph);
		assertTrue(true);
	}

}
