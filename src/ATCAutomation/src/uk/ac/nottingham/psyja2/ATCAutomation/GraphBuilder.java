package uk.ac.nottingham.psyja2.ATCAutomation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Stack;

import uk.ac.nottingham.psyja2.ATCAutomation.PathFinding.IGraph;
import uk.ac.nottingham.psyja2.ATCAutomation.PathFinding.Node;
import uk.ac.nottingham.psyja2.ATCSimulator.Airway;
import uk.ac.nottingham.psyja2.ATCSimulator.Coordinate;
import uk.ac.nottingham.psyja2.ATCSimulator.Scenario;
import uk.ac.nottingham.psyja2.ATCSimulator.Waypoint;


public abstract class GraphBuilder
{
	
	private final static double EARTH_RADIUS = 6371e3;

	/**
	 * This is the algorithm that takes a Scenario object (the airway and waypoint data)
	 * and builds a graph object from it. This graph can then be used for pathfinding purposes.
	 * Nodes are named in the form 'WAYPOINT-FLX' where X is the flight level (altitude in 100's feet)
	 * @return a map of node names to their node objects
	 */
	public static Map<String, SpatialNode> buildGraph(Scenario scenario, IGraph graph)
	{
		/******************************************
		 * PART 1.
		 * This part identifies the navigational waypoints and connections.
		 * + It creates some initial SpatialNode objects and a map of their connections.
		 * + Creates a record of the waypoints highest and lowest allowed altitude.
		 * 
		 * Runtime: O(n)
		 *****************************************/

		// Create a map of waypoint names to their associated SpatialNode
		HashMap<String, SpatialNode> nodes = new HashMap<>();
		HashMap<SpatialNode, ArrayList<Node>> connections = new HashMap<>();
		for(String waypointName : scenario.waypoints.keySet())
		{
			Waypoint waypoint = scenario.waypoints.get(waypointName);
			SpatialNode node = new SpatialNodeWaypoint(waypointName, waypoint, 0);
			nodes.put(waypointName, node);
			connections.put(node, new ArrayList<Node>());
		}
		
		// Iterare through the airways and add the node connections
		// Also keep track of the highest and lowest altitudes the waypoints can be flown at
		HashMap<String,Integer> waypointLows = new HashMap<>();
		HashMap<String,Integer> waypointHighs = new HashMap<>();
		for(Airway airway : scenario.airways)
		{
			SpatialNode from = nodes.get(airway.getFrom().getName());
			SpatialNode to = nodes.get(airway.getTo().getName());
			connections.get(from).add(to);
			connections.get(to).add(from);
			
			// Update the list of waypoint highest and lowest altitudes
			// Check 'from' waypoint against lowest altitude
			if(waypointLows.containsKey(airway.getFrom().getName()))
			{
				if(waypointLows.get(airway.getFrom().getName()) > airway.getLowerAltitude())
				{
					waypointLows.remove(airway.getFrom().getName());
					waypointLows.put(airway.getFrom().getName(), airway.getLowerAltitude());
				}
			}
			else
			{
				waypointLows.put(airway.getFrom().getName(), airway.getLowerAltitude());
			}
			// Check 'to' waypoint against lowest altitude
			if(waypointLows.containsKey(airway.getTo().getName()))
			{
				if(waypointLows.get(airway.getTo().getName()) > airway.getLowerAltitude())
				{
					waypointLows.remove(airway.getTo().getName());
					waypointLows.put(airway.getTo().getName(), airway.getLowerAltitude());
				}
			}
			else
			{
				waypointLows.put(airway.getTo().getName(), airway.getLowerAltitude());
			}
			// Check 'from' waypoint against highest altitude
			if(waypointHighs.containsKey(airway.getFrom().getName()))
			{
				if(waypointHighs.get(airway.getFrom().getName()) < airway.getUpperAltitude())
				{
					waypointHighs.remove(airway.getFrom().getName());
					waypointHighs.put(airway.getFrom().getName(), airway.getUpperAltitude());
				}
			}
			else
			{
				waypointHighs.put(airway.getFrom().getName(), airway.getUpperAltitude());
			}
			// Check 'to' waypoint against highest altitude
			if(waypointHighs.containsKey(airway.getTo().getName()))
			{
				if(waypointHighs.get(airway.getTo().getName()) < airway.getUpperAltitude())
				{
					waypointHighs.remove(airway.getTo().getName());
					waypointHighs.put(airway.getTo().getName(), airway.getUpperAltitude());
				}
			}
			else
			{
				waypointHighs.put(airway.getTo().getName(), airway.getUpperAltitude());
			}
		}
		
		/******************************************
		 * PART 2.
		 * This part identifies any locations where two airways intersect without
		 * an official waypoint/intersection. It create a new node for these places.
		 * 
		 * Runtime: O(n^2) 
		 * However, the algorithm removes unlikely cases so the runtime is much better in practice.
		 *****************************************/
		
		// Build a list of edges in the graph
		Stack<Edge> openEdgeList = new Stack<>();
		HashSet<Node> closedList = new HashSet<>();
		for(SpatialNode node : nodes.values())
		{
			for(Node connection : connections.get(node))
			{
				if(!closedList.contains(connection))
				{
					Edge edge = new Edge(node, (SpatialNode) connection);
					openEdgeList.push(edge);
				}
			}
			closedList.add(node);
		}
		
		// Find intersections and replace them with edges that don't intersect
		int n = 1;
		HashSet<Edge> edges = new HashSet<>();
		while(!openEdgeList.isEmpty())
		{
			// Pop an edge from the open list
			Edge edge1 = openEdgeList.pop();
			
			boolean hasIntersections = false;
			Stack<Edge> toAdd = new Stack<>();
			Stack<Edge> toRemove = new Stack<>();
			
			for(Edge edge2 : openEdgeList)
			{
				// Skip edge2 if it shares nodes with edge1
				if(edge2.from.equals(edge1.to) || edge2.from.equals(edge1.from) || 
						edge2.to.equals(edge1.to) || edge2.to.equals(edge1.from))
					continue;
				
				// Find out if the edges intersect or not
				Coordinate result = doLinesIntersect(edge1.from.location.getLatitude(), edge1.from.location.getLongitude(),
						edge1.to.location.getLatitude(), edge1.to.location.getLongitude(), edge2.from.location.getLatitude(),
						edge2.from.location.getLongitude(), edge2.to.location.getLatitude(), edge2.to.location.getLongitude());
				if(result != null)
				{
					// Create a new intersection spatial node
					SpatialNode intersection = new SpatialNodeIntersection("INTERSECTION" + n, result, 0, edge1.from, edge1.to, edge2.from, edge2.to);
			
					// Add a lows/highs reference
					int lowest = Math.min(Integer.MAX_VALUE, waypointLows.get(edge1.from.name));
					lowest = Math.min(lowest, waypointLows.get(edge1.to.name));
					lowest = Math.min(lowest, waypointLows.get(edge2.from.name));
					lowest = Math.min(lowest, waypointLows.get(edge2.to.name));
					int highest = Math.max(Integer.MIN_VALUE, waypointHighs.get(edge1.from.name));
					highest = Math.max(highest, waypointHighs.get(edge1.to.name));
					highest = Math.max(highest, waypointHighs.get(edge2.from.name));
					highest = Math.max(highest, waypointHighs.get(edge2.to.name));
					waypointLows.put(intersection.name, lowest);
					waypointHighs.put(intersection.name, highest);
					
					// Create new edges
					toAdd.push(new Edge(edge1.from, intersection));
					toAdd.push(new Edge(intersection, edge1.to));
					toAdd.push(new Edge(edge2.from, intersection));
					toAdd.push(new Edge(intersection, edge2.to));
					
					// Tell it to remove edge2 from the stack too
					toRemove.add(edge2);

					// Increment n
					n++;
					hasIntersections = true;
					break;
				}
			}

			// Add/remove the items in the temporary stack
			openEdgeList.addAll(toAdd);
			openEdgeList.removeAll(toRemove);
			
			// This edge has no intersections, add it to the final list
			if(!hasIntersections)
			{
				edges.add(edge1);
			}
			
		}
		
		// Convert the Edges to nodes and connections
		nodes.clear();
		connections.clear();
		
		// Add the nodes to the node list
		for(Edge edge : edges)
		{
			if(!nodes.containsKey(edge.from.name))
			{
				nodes.put(edge.from.name, edge.from);
				connections.put(edge.from, new ArrayList<>());
			}
			if(!nodes.containsKey(edge.to.name))
			{
				nodes.put(edge.to.name, edge.to);
				connections.put(edge.to, new ArrayList<>());
			}
		}
		
		// Add the connections
		for(Edge edge : edges)
		{
			connections.get(edge.from).add(edge.to);
			connections.get(edge.to).add(edge.from);
		}
		

				
		/******************************************
		 * PART 3.
		 * This part finds edges which are particuarly long and splits them up.
		 * The aim of this is to ensure all edges in the graph are of a similar length which sets the graph
		 * up for allowing descents and ascents through 3D space via connections between intermediate nodes.
		 * 
		 * Runtime: O(n) where n = number of edges
		 *****************************************/
		// Store the new intermediate nodes { a1, intermediate-1, ..., intermediate-x, a2 }
		ArrayList<SpatialNode[]> intermediateNodes = new ArrayList<>();
		HashSet<SpatialNode> visited = new HashSet<>();
		n = 1;
		for(SpatialNode a1 : nodes.values())
		{
			visited.add(a1);
			for(Node connectedNodeA : connections.get(a1))
			{
				SpatialNode a2 = (SpatialNode) connectedNodeA;
				if(!visited.contains(a2))
				{
					double distance = calculateDistance(a1.location.getLatitude(), a1.location.getLongitude(),
							a2.location.getLatitude(), a2.location.getLongitude());
					if(distance > Preferences.INTERMEDIATE_SPACING * 18520)
					{
						// Airway needs to be split into sections
						// Calculate how many sections (legs) there should be
						double legLength = distance / 2D;
						int numberLegs = 2;
						while(legLength > Preferences.INTERMEDIATE_SPACING * 18520)
						{
							numberLegs *= 2;
							legLength /= 2;
						}
						
						// An array to hold the 'chain' of nodes
						SpatialNode newNodes[] = new SpatialNode[numberLegs + 1];
						newNodes[0] = a1;
						newNodes[numberLegs] = a2;
						
						// Calculate all the intermediate locations
						Coordinate[] points = calculateIntermediatePoints(a1.location, a2.location, numberLegs);
						
						// Create the intermediate nodes
						for(int i = 1; i < numberLegs; i++)
						{
							Coordinate location = points[i - 1];
							newNodes[i] = new SpatialNodeIntermediate("INTERMEDIATE" + n, location, 0, a1, a2);
							n++;
						}
						intermediateNodes.add(newNodes);
					}			
				}
			}
		}
		
		// Add the new intermediate spatial nodes to the graph
		// Also keep a record of the low and high altitudes for this intermediate node
		for(SpatialNode[] intermediate : intermediateNodes)
		{
			// Add the new intersection node to the list
			for(int i = 1; i < intermediate.length - 1; i++)
			{
				nodes.put(intermediate[i].name, intermediate[i]);
			}
			
			// Break the connections between the original two nodes
			connections.get(intermediate[0]).remove(intermediate[intermediate.length - 1]);
			connections.get(intermediate[intermediate.length - 1]).remove(intermediate[0]);
			
			// Create connections between all the intermediate nodes
			for(int i = 1; i < intermediate.length; i++)
			{
				if(!connections.containsKey(intermediate[i]))
				{
					connections.put(intermediate[i], new ArrayList<>());
				}
				connections.get(intermediate[i - 1]).add(intermediate[i]);
				connections.get(intermediate[i]).add(intermediate[i - 1]);
			}
			
			// Find the lowest/highest acceptable altitude and use this for the new nodes
			int lowest = Math.min(Integer.MAX_VALUE, waypointLows.get(intermediate[0].name));
			lowest = Math.min(lowest, waypointLows.get(intermediate[intermediate.length - 1].name));
			int highest = Math.max(Integer.MIN_VALUE, waypointHighs.get(intermediate[0].name));
			highest = Math.max(highest, waypointHighs.get(intermediate[intermediate.length - 1].name));
			for(int i = 1; i < intermediate.length - 1; i++)
			{
				waypointLows.put(intermediate[i].name, lowest);
				waypointHighs.put(intermediate[i].name, highest);
			}
		}			
		
		/******************************************
		 * PART 4.
		 * Convert the 2D graph into 3D by considering the altitude limits of the airways.
		 * Split into 1000ft levels (eg. 10,000; 11,000; 12,000...)
		 * 
		 * Runtime: O(n)
		 *****************************************/
		// Step 1: Recreate nodes for each altitude
		HashMap<String, ArrayList<SpatialNode>> allNodes = new HashMap<>();
		for(SpatialNode node : nodes.values())
		{
			// Calculate the low/high altitudes for this node
			int lowest = waypointLows.get(node.name);
			int highest = waypointHighs.get(node.name);
			// round to nearest whole flight level (eg. 100, 120, 300)
			lowest = (lowest / 10) * 10;
			highest = (highest / 10) * 10;
			
			// Create a new node for each 1000ft level
			ArrayList<SpatialNode> levels = new ArrayList<SpatialNode>();
			SpatialNode previous = null;
			for(int altitude = lowest; altitude <= highest; altitude += 10)
			{
				// Recreate the node
				if(node instanceof SpatialNodeIntersection)
				{
					SpatialNodeIntersection nodeI = (SpatialNodeIntersection) node;
					SpatialNodeIntersection newNode = new SpatialNodeIntersection(nodeI.name + "_FL" + altitude, nodeI.location, altitude,
							nodeI.parentA1, nodeI.parentA2, nodeI.parentB1, nodeI.parentB2);
					// Add references for above/below nodes
					if(previous != null)
					{
						newNode.below = previous;
						previous.above = newNode;
					}
					previous = newNode;
					levels.add(newNode);
				}
				else if(node instanceof SpatialNodeIntermediate)
				{
					SpatialNodeIntermediate nodeI = (SpatialNodeIntermediate) node;
					SpatialNodeIntermediate newNode = new SpatialNodeIntermediate(nodeI.name + "_FL" + altitude, nodeI.location, altitude,
							nodeI.parentA, nodeI.parentB);
					// Add references for above/below nodes
					if(previous != null)
					{
						newNode.below = previous;
						previous.above = newNode;
					}
					previous = newNode;
					levels.add(newNode);
				}
				else if(node instanceof SpatialNodeWaypoint)
				{
					SpatialNodeWaypoint nodeW = (SpatialNodeWaypoint) node;
					SpatialNode newNode = new SpatialNodeWaypoint(nodeW.name + "_FL" + altitude, nodeW.getWaypoint(), altitude);
					// Add references for above/below nodes
					if(previous != null)
					{
						newNode.below = previous;
						previous.above = newNode;
					}
					previous = newNode;
					levels.add(newNode);
				}
			}
			
			// Add it to the map
			allNodes.put(node.name, levels);
		}
		
		// Step 2: Recreate connections
		HashMap<SpatialNode, ArrayList<Node>> allConnections = new HashMap<>();
		for(SpatialNode oldNode : connections.keySet())
		{
			// Get the old connections
			ArrayList<Node> oldConnections = connections.get(oldNode);
			// Get all the new nodes with this ID
			ArrayList<SpatialNode> newNodes = allNodes.get(oldNode.name);
			
			// Iterate through all the old connections
			for(Node _oldConnection : oldConnections)
			{
				SpatialNode oldConnection = (SpatialNode) _oldConnection;
				String connectedID = oldConnection.name;
				// Iterate through all the new nodes with this ID
				for(SpatialNode newNode : newNodes)
				{
					// Specify the min and max altitudes each node can connect to.
					// In this case, +/- 2,000ft is allowed
					int minAlt = newNode.altitude - 20;
					int maxAlt = newNode.altitude + 20;
					
					// Find a valid connection
					for(SpatialNode connnectedNewNode : allNodes.get(connectedID))
					{
						if(connnectedNewNode.altitude <= maxAlt && connnectedNewNode.altitude >= minAlt)
						{
							// Is a valid node to connect to
							if(!allConnections.containsKey(newNode))
							{
								allConnections.put(newNode, new ArrayList<Node>());
							}
							allConnections.get(newNode).add(connnectedNewNode);
						}
					}
				}
			}
		}		
		
		// Iterate through each node and add it to the graph, with it's connections
		HashMap<String, SpatialNode> result = new HashMap<>();
		for(ArrayList<SpatialNode> nodeList : allNodes.values())
		{
			for(SpatialNode node : nodeList)
			{
				result.put(node.name, node);
				graph.addNode(node, allConnections.get(node));
			}
		}
		
		// Return the node list
		return result;
	}
	
	public static Map<String, SimpleSpatialNode> flattenGraph(IGraph threeDimensionalGraph, IGraph graph)
	{
		// Build a list of all the unique nodes
		HashMap<String, SimpleSpatialNode> flatNodes = new HashMap<>();
		for(Node _node : threeDimensionalGraph.getAllNodes())
		{
			SpatialNode node = (SpatialNode) _node;
			String name = node.toString().substring(0, node.toString().indexOf("_"));
			if(!flatNodes.containsKey(name))
			{
				SimpleSpatialNode flatNode = new SimpleSpatialNode(name, node.location);
				flatNodes.put(name, flatNode);
			}
		}
		
		// Find the connections
		HashMap<SimpleSpatialNode, ArrayList<Node>> connections = new HashMap<>();
		
		
		for(Node _node : threeDimensionalGraph.getAllNodes())
		{
			SpatialNode node = (SpatialNode) _node;
			String name = node.toString().substring(0, node.toString().indexOf("_"));
			SimpleSpatialNode flatNode = flatNodes.get(name);
			
			for(Node _successor : threeDimensionalGraph.getSuccessors(node))
			{
				SpatialNode successor = (SpatialNode) _successor;
				String successorName = successor.toString().substring(0, successor.toString().indexOf("_"));
				SimpleSpatialNode flatSuccessor = flatNodes.get(successorName);
				if(!connections.containsKey(flatNode))
				{
					connections.put(flatNode, new ArrayList<>());
				}
				
				boolean skip = false;
				for(Node child : connections.get(flatNode))
				{
					if(((SimpleSpatialNode)child).name.equals(successorName))
						skip = true;
					if(flatSuccessor.name.equals(flatNode.name))
						skip = true;
				}
				if(!skip)
					connections.get(flatNode).add(flatSuccessor);				
			}	
			
		}
		
		// Add the nodes and connections to the graph
		for(SimpleSpatialNode node : connections.keySet())
		{
			graph.addNode(node, connections.get(node));
		}
		
		return flatNodes;		
	}
	
	
	/**
	 * Determines the place at which two lines (defined by there start/end point coordinates)
	 * intersect each other.
	 * @return The coordinate of intersection or null if they do not intersect 
	 */
	public static Coordinate doLinesIntersect(double latA1, double lngA1, double latA2, double lngA2,
			double latB1, double lngB1, double latB2, double lngB2)
	{
		// See if the lines are likely to cross or if they're nowhere near each other
		// This massively reduces number of calculations - removes impossible cases
		double minLatA = Math.min(latA1, latA2);
		double maxLatA = Math.max(latA1, latA2);
		double minLngA = Math.min(lngA1, lngA2);
		double maxLngA = Math.max(lngA1, lngA2);
		double minLatB = Math.min(latB1, latB2);
		double maxLatB = Math.max(latB1, latB2);
		double minLngB = Math.min(lngB1, lngB2);
		double maxLngB = Math.max(lngB1, lngB2);
		if(minLngA > maxLngB || maxLngA < minLngB ||
				minLatA > maxLatB || maxLatA < minLatB)
		{
			// Not possible for lines to intersect
			return null;
		}
		
		// Calculate the bearing of both lines
		double lineABearing = calculateBearing(latA1, lngA1, latA2, lngA2);
		double lineBBearing = calculateBearing(latB1, lngB1, latB2, lngB2);
		
		// Calculate the length of the lines
		double lineALength = calculateDistance(latA1, lngA1, latA2, lngA2);
		double lineBLength = calculateDistance(latB1, lngB1, latB2, lngB2);
		
		// Convert everything to radians
		latA1 = Math.toRadians(latA1);
		lngA1 = Math.toRadians(lngA1);
		latA2 = Math.toRadians(latA2);
		lngA2 = Math.toRadians(lngA2);
		latB1 = Math.toRadians(latB1);
		lngB1 = Math.toRadians(lngB1);
		latB2 = Math.toRadians(latB2);
		lngB2 = Math.toRadians(lngB2);
		lineABearing = Math.toRadians(lineABearing);
		lineBBearing = Math.toRadians(lineBBearing);

		// Use the formula from https://www.movable-type.co.uk/scripts/latlong.html
		// to calculate the point at which the lines intersept (lat3, lng3)
		
		double lat1 = latA1;
		double lng1 = lngA1;
		double lat2 = latB1;
		double lng2 = lngB1;
		double theta13 = lineABearing;
		double theta23 = lineBBearing;
		double deltaLat = lat2 - lat1;
		double deltaLng = lng2 - lng1;
		
		double delta12 = 2 * Math.asin(Math.sqrt(Math.sin(deltaLat / 2) * Math.sin(deltaLat /2) + 
				Math.cos(lat1) * Math.cos(lat2) * Math.sin(deltaLng / 2) * Math.sin(deltaLng / 2)));
		if(delta12 == 0)
			return null;
		
		double thetaA = Math.acos((Math.sin(lat2) - Math.sin(lat1) * Math.cos(delta12)) / (Math.sin(delta12) * Math.cos(lat1)));
		double thetaB = Math.acos((Math.sin(lat1) - Math.sin(lat2) * Math.cos(delta12)) / (Math.sin(delta12) * Math.cos(lat2)));
	
		double theta12 = Math.sin(lng2 - lng1) > 0 ? thetaA : 2 * Math.PI - thetaA;
		double theta21 = Math.sin(lng2 - lng1) > 0 ? 2 * Math.PI - thetaB : thetaB;
		
		double alpha1 = theta13 - theta12;
		double alpha2 = theta21 - theta23;
		
		if (Math.sin(alpha1) == 0 && Math.sin(alpha2) == 0) return null; // infinite intersections
		if (Math.sin(alpha1) * Math.sin(alpha2) < 0) return null;      // ambiguous intersection
	
		double alpha3 = Math.acos(-Math.cos(alpha1) * Math.cos(alpha2) + Math.sin(alpha1) * Math.sin(alpha2) * Math.cos(delta12));
		double delta13 = Math.atan2(Math.sin(delta12) * Math.sin(alpha1) * Math.sin(alpha2), Math.cos(alpha2) + Math.cos(alpha1) * Math.cos(alpha3));
		double lat3 = Math.asin(Math.sin(lat1) * Math.cos(delta13) + Math.cos(lat1) * Math.sin(delta13) * Math.cos(theta13));
		double deltaLng13 = Math.atan2(Math.sin(theta13) * Math.sin(delta13) * Math.cos(lat1), Math.cos(delta13) - Math.sin(lat1) * Math.sin(lat3));
		double lng3 = lng1 + deltaLng13;
		
		
		// Now see if the intersection is within the range of the 4 coordinates or ~ 0
		double distanceToA1 = calculateDistance(Math.toDegrees(latA1), Math.toDegrees(lngA1), Math.toDegrees(lat3), Math.toDegrees(lng3));	
		double distanceToB1 = calculateDistance(Math.toDegrees(latB1), Math.toDegrees(lngB1), Math.toDegrees(lat3), Math.toDegrees(lng3));	
		if(distanceToA1 < 500 || distanceToB1 < 500 || Double.isNaN(distanceToA1) || Double.isNaN(distanceToB1) || distanceToA1 > lineALength || distanceToB1 > lineBLength)
		{
			// Intersection is outside the range of coordinates
			return null;
		}
		
		return new Coordinate(Math.toDegrees(lat3), Math.toDegrees(lng3));
	}
	
	
	/**
	 * Calculate the bearing from one coordinate to another
	 * @return the bearing in degrees between 0 and 360
	 */
	public static double calculateBearing(double lat1, double lng1, double lat2, double lng2)
	{
		// Formula for bearing calculation:
		// https://www.movable-type.co.uk/scripts/latlong.html
		
		// Convert to radians
		lat1 = Math.toRadians(lat1);
		lng1 = Math.toRadians(lng1);
		lat2 = Math.toRadians(lat2);
		lng2 = Math.toRadians(lng2);
		
		// Calculate the bearing
		double y = Math.sin(lng2 - lng1) * Math.cos(lat2);
		double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(lng2 - lng1);
		double bearing = Math.toDegrees(Math.atan2(y, x));
		
		return (bearing + 360) % 360;
	}
	
	/**
	 * Calculate the distance, in metres, from one location to another
	 * @return the distance in metres
	 */
	public static double calculateDistance(double lat1, double lng1, double lat2, double lng2)
	{
		// Formula for distance calculation:
		// https://www.movable-type.co.uk/scripts/latlong.html
		
		// Convert to radians
		lat1 = Math.toRadians(lat1);
		lng1 = Math.toRadians(lng1);
		lat2 = Math.toRadians(lat2);
		lng2 = Math.toRadians(lng2);		
		
		// Calculate distance between points
		double deltaLat = lat2 - lat1;
		double deltaLng = lng2 - lng1;
		double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) + Math.cos(lat1) * Math.cos(lat2) * Math.sin(deltaLng / 2) * Math.sin(deltaLng / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double distance = EARTH_RADIUS * c;
		
		return distance;
	}
	
	/**
	 * Calculate the midpoint between two coordinates
	 * @return the coordinates of the midpoint
	 */
	public static Coordinate calculateMidpoint(double lat1, double lng1, double lat2, double lng2)
	{
		// Formula for midpoint calculation:
		// https://www.movable-type.co.uk/scripts/latlong.html
				
		// Convert to radians
		lat1 = Math.toRadians(lat1);
		lng1 = Math.toRadians(lng1);
		lat2 = Math.toRadians(lat2);
		lng2 = Math.toRadians(lng2);	
				
		// Calculate the midpoint
		double Bx = Math.cos(lat2) * Math.cos(lng2 - lng1);
		double By = Math.cos(lat2) * Math.sin(lng2 - lng1);
		double lat3 = Math.atan2(Math.sin(lat1) + Math.sin(lat2), Math.sqrt((Math.cos(lat1) + Bx) * (Math.cos(lat1) + Bx) + By * By));
		double lng3 = lng1 + Math.atan2(By, Math.cos(lat1) + Bx);
		
		return new Coordinate(Math.toDegrees(lat3), Math.toDegrees(lng3));
	}
	
	/**
	 * Calculates several intermediates along a line between two coordinates
	 * @return an array of coordinates for the intermediate points
	 */
	public static Coordinate[] calculateIntermediatePoints(Coordinate a1, Coordinate a2, int numSegments)
	{
		// Use divide and conquer to recursively find intermediate points
		if(numSegments == 2)
		{
			// Base case:
			Coordinate result[] = new Coordinate[1];
			result[0] = calculateMidpoint(a1.getLatitude(), a1.getLongitude(), a2.getLatitude(), a2.getLongitude());
			return result;
		}
		else if(numSegments > 2)
		{
			// Recursive case:
			// Divide
			Coordinate mid = calculateMidpoint(a1.getLatitude(), a1.getLongitude(), a2.getLatitude(), a2.getLongitude());
			
			// Conquer
			Coordinate left[] = calculateIntermediatePoints(a1, mid, numSegments / 2);
			Coordinate right[] = calculateIntermediatePoints(mid, a2, numSegments / 2);
			
			// Combine
			Coordinate result[] = new Coordinate[left.length + right.length + 1];
			System.arraycopy(left, 0, result, 0, left.length);
			System.arraycopy(right, 0, result, left.length + 1, right.length);
			result[left.length] = mid;
			return result;
		}
		else
		{
			// Invalid param
			return null;
		}		
	}
	
	private static class Edge
	{
		SpatialNode from;
		SpatialNode to;
		
		public Edge(SpatialNode from, SpatialNode to)
		{
			this.from = from;
			this.to = to;
		}
	}

	
}
