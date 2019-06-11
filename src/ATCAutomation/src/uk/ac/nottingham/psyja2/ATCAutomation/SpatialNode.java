package uk.ac.nottingham.psyja2.ATCAutomation;


import java.util.Map;

import uk.ac.nottingham.psyja2.ATCAutomation.PathFinding.FloydWarshall;
import uk.ac.nottingham.psyja2.ATCAutomation.PathFinding.IGraph;
import uk.ac.nottingham.psyja2.ATCAutomation.PathFinding.Node;
import uk.ac.nottingham.psyja2.ATCSimulator.Aircraft;
import uk.ac.nottingham.psyja2.ATCSimulator.Coordinate;
import uk.ac.nottingham.psyja2.ATCSimulator.Simulator;

public abstract class SpatialNode extends Node
{
	
	private static final double METRES_IN_MILE = 1852; // The number of metres in a nautical mile
	
	private static FloydWarshall flatGraph = null;
	private static Map<String, SimpleSpatialNode> flatNodes = null;
	
	protected Coordinate location;
	protected String name;
	protected int altitude;
	
	// References to the above/below SpatialNode (if any)
	protected SpatialNode above;
	protected SpatialNode below;
	
	public SpatialNode(String name, Coordinate location, int altitude)
	{
		this.name = name;
		this.location = location;
		this.altitude = altitude;
	}	

	public Coordinate getLocation()
	{
		return location;
	}
	
	@Override
	public double getHeuristicTo(Node node)
	{
		
		// Get the associated graph object
		SpatialWHCAStar graph = (SpatialWHCAStar) this.graph;

		// Safety for if the agent has not been set
		if(graph.getAgent() == null)
		{
			return 0;
		}
		
		// If nodes are the same, the cost is the equivalent to 5 minutes
		if(this.equals(node))
		{
			return 5d / 60d;
		}
		
		// Get the aircraft
		Aircraft aircraft = ((AircraftAgent)graph.getAgent()).getAircraft();	
		
		// Estimate the time for the aircraft to fly from A to B		
		if(Preferences.HEURISTIC == Preferences.TRUE_DISTANCE)
		{
			/* Floyd-Warshall Distance */
			// Make sure the static reference to the flat graph exists
			if(flatGraph == null)
				buildFlatGraph(this.graph);
			
			// Get the waypoints in the FW graph
			SimpleSpatialNode self = flatNodes.get(name.substring(0, name.indexOf("_")));
			SimpleSpatialNode goal = flatNodes.get(node.toString().substring(0, node.toString().indexOf("_")));
			
			// Calculat the distance of the shortest path and the heading
			double distance = flatGraph.shortestPathLength(self, goal) / METRES_IN_MILE;
			double heading = GraphBuilder.calculateBearing(self.location.getLatitude(), self.location.getLongitude(), goal.location.getLatitude(), goal.location.getLongitude());
			int alt = (altitude + ((((SpatialNode)node).altitude - altitude) / 2)) * 100;
			
			// Use this info to estimate the shortest flight time possible
			double time = estimateFlightTime(aircraft, 0, alt, heading, distance);
			return time;
		}
		else if(Preferences.HEURISTIC == Preferences.MANHATTAN)
		{
			/* Manhatten Distance: */
			SpatialNode to = (SpatialNode) node;
			Coordinate corner = new Coordinate(location.getLatitude(), to.location.getLongitude());
			double time1 = estimateFlightTime(aircraft, (to.altitude - altitude) * 50, to.altitude * 50, location, corner);
			double time2 = estimateFlightTime(aircraft, (to.altitude - altitude) * 50, to.altitude * 50, corner, to.location);
			return time1 + time2;
		}
		else if(Preferences.HEURISTIC == Preferences.STRAIGHT_LINE)
		{
			/*Straight line distance: */
			SpatialNode to = (SpatialNode) node;
			double time = estimateFlightTime(aircraft, (to.altitude - altitude) * 50, to.altitude * 50, location, to.location);
			return time;
		}
		
		return 1;
		
	}

	@Override
	public double getCostTo(Node node)
	{
		// Get the associated graph object
		SpatialWHCAStar graph = (SpatialWHCAStar) this.graph;

		// Safety for if the agent has not been set
		if(graph.getAgent() == null)
		{
			return 0;
		}
		
		// If nodes are the same, the cost is the equivalent to 5 minutes
		if(this.equals(node))
		{
			//return 5d / 60d;
			return 0.0;
		}
		
		// Estimate the time for the aircraft to fly from A to B		
		Aircraft aircraft = ((AircraftAgent)graph.getAgent()).getAircraft();
		SpatialNode to = (SpatialNode) node;
		double time = estimateFlightTime(aircraft, (to.altitude - altitude) * 100, to.altitude * 100, location, to.location);
			
		
		return time;
	}

	@Override
	public String toString()
	{
		return name;
	}
	
	/**
	 * Calculates the estimated flight time (in decimal hours) for an aircraft between two points
	 * (a to b) at a particular altitude.
	 */
	public static double estimateFlightTime(Aircraft aircraft, int altitudeGain, int targetAltitude, Coordinate a, Coordinate b)
	{
		// Check that the target altitude is within the aircraft's performance limits
		if(targetAltitude > aircraft.getProfile().maxAltitude)
		{
			// The climb is NOT possible, return an impossibly large cost as this is not a realistic option.
			return 10000D;
		}
		
		// Calculate the bearing and distance
		double heading = GraphBuilder.calculateBearing(a.getLatitude(), a.getLongitude(), b.getLatitude(), b.getLongitude());
		double distance = GraphBuilder.calculateDistance(a.getLatitude(), a.getLongitude(), b.getLatitude(), b.getLongitude()) / METRES_IN_MILE;

		return estimateFlightTime(aircraft, altitudeGain, targetAltitude, heading, distance);
	}
	
	
	public static double estimateFlightTime(Aircraft aircraft, int altitudeGain, int targetAltitude, double heading, double distance)
	{
		// Check that the target altitude is within the aircraft's performance limits
		if(targetAltitude > aircraft.getProfile().maxAltitude)
		{
			// The climb is NOT possible, return an impossibly large cost as this is not a realistic option.
			return 10000D;
		}

		// Estimate the true airspeed
		double tas = aircraft.getSpeed() + ((targetAltitude - (altitudeGain / 2)) / 200);
		
		// Estimate the ground speed;
		double v1X = Math.sin(Math.toRadians(heading)) * tas;
		double v1Y = Math.cos(Math.toRadians(heading)) * tas;
		double v2X = -Math.sin(Math.toRadians(Simulator.getInstance().getWindDirection())) * Simulator.getInstance().getWindSpeed();
		double v2Y = -Math.cos(Math.toRadians(Simulator.getInstance().getWindDirection())) * Simulator.getInstance().getWindSpeed();
		double sumX = v1X + v2X;
        double sumY = v1Y + v2Y;
        // Calculate the resultant vector's magnitude
        double groundSpeed = Math.sqrt(sumX * sumX + sumY * sumY);
        
        // Calculate the estimated flight time (in hours)
		double time = distance / groundSpeed;
		
		// Decide if it is possible to achieve this climb within the calculated time
		double possibleHeightGain = aircraft.getProfile().climbRate * (time * 60);		
		if(possibleHeightGain > Math.abs(altitudeGain))
		{
			// The climb IS possible, return the time
			return time;
		}
		else
		{
			// The climb is NOT possible, return an impossibly large cost as this is not a realistic option.
			return 10000D;
		}
		
	}
	
	public static void buildFlatGraph(IGraph graph)
	{
		flatGraph = new FloydWarshall();
		flatNodes = GraphBuilder.flattenGraph(graph, flatGraph);
		flatGraph.initialise();
		flatGraph.clearAdjacencyList();
	}

}
