package uk.ac.nottingham.psyja2.ATCAutomation;

import java.awt.EventQueue;
import java.util.LinkedList;
import java.util.Queue;

import uk.ac.nottingham.psyja2.ATCAutomation.PathFinding.*;
import uk.ac.nottingham.psyja2.ATCSimulator.Aircraft;
import uk.ac.nottingham.psyja2.ATCSimulator.AltitudeInstruction;
import uk.ac.nottingham.psyja2.ATCSimulator.HoldInstruction;
import uk.ac.nottingham.psyja2.ATCSimulator.Instruction;
import uk.ac.nottingham.psyja2.ATCSimulator.InstructionListener;
import uk.ac.nottingham.psyja2.ATCSimulator.Simulator;
import uk.ac.nottingham.psyja2.ATCSimulator.Waypoint;
import uk.ac.nottingham.psyja2.ATCSimulator.WaypointInstruction;
import uk.ac.nottingham.psyja2.ATCSimulator.Waypoint.WaypointType;

public class AircraftAgent extends Agent
{
	
	private Aircraft aircraft;
	protected Queue<Node> nodeQueue;
	private boolean inScenario = false;
	
	private Instruction currentInstruction;
	private InstructionListener instructionListener;

	public AircraftAgent(Aircraft aircraft, SpatialNode start, SpatialNode goal)
	{
		super(start, goal);
		this.aircraft = aircraft;
		nodeQueue = new LinkedList<Node>();
	}
	
	public Aircraft getAircraft()
	{
		return aircraft;
	}
	
	@Override
	public Node getPosition()
	{
		// Find the nearest node to the aircraft
		Node closest = null;
		double closestDist = Double.MAX_VALUE;
		int alt =  (int) (Math.round(aircraft.getAltitude() / 1000) * 1000) / 100;
		for(Node _node : manager.graph.getAllNodes())
		{
			SpatialNode node = (SpatialNode) _node;
			if(node.altitude == alt)
			{
				double dist = GraphBuilder.calculateDistance(node.getLocation().getLatitude(), node.getLocation().getLongitude(), 
						aircraft.getLocation().getLatitude(), aircraft.getLocation().getLongitude());
				if(dist < closestDist)
				{
					closestDist = dist;
					closest = node;
				}
			}
		}
		if(closest == null)
			return currentPosition;
		else
			return closest;
	}
	
	@Override
	public double getPriority()
	{		
		if(Preferences.PRIORITY_SYSTEM == Preferences.AGITATION)
		{
			// Method 1: Path Optimality (agitation)
			// Compare the actual cost to the heuristic to find out how 'good' the path is
			return pathCost / heuristicCost;
		}
		else if(Preferences.PRIORITY_SYSTEM == Preferences.FIFO)
		{
			// Method 2: Airborne Time (FIFO)
			return aircraft.getAirborneTime();	
		}
		else if(Preferences.PRIORITY_SYSTEM == Preferences.FURTHEST_DESTINATION)
		{
			// Method 3: Furthest Distance To Destination
			double distanceToGoal = GraphBuilder.calculateDistance(aircraft.getLocation().getLatitude(), aircraft.getLocation().getLongitude(), 
					((SpatialNode)goal).location.getLatitude(), ((SpatialNode)goal).location.getLongitude());
			return distanceToGoal;
		}
		else if(Preferences.PRIORITY_SYSTEM == Preferences.CLOSEST_DESTINATION)
		{
			// Method 4: Earliest Deadline First (closest to destination first)
			double distanceToGoal = GraphBuilder.calculateDistance(aircraft.getLocation().getLatitude(), aircraft.getLocation().getLongitude(), 
					((SpatialNode)goal).location.getLatitude(), ((SpatialNode)goal).location.getLongitude());
			return 1D / distanceToGoal;
		}		
		return 0;
		
		// Method 5: Combination of Airborne Time & Furthest Distance To Destination
		// 			 = Airborne Time + Estimated Time To Destination
		/*double timeRemaining = SpatialNode.estimateFlightTime(aircraft, 0, (int)aircraft.getAltitude(), aircraft.getLocation(), ((SpatialNode)goal).location);
		timeRemaining *= 60D * 60D; // Convert hours to seconds
		double time = timeRemaining + aircraft.getAirborneTime(); // Add the airborne time
		return time;	*/	
	}
		
	@Override
	public synchronized void runPath(Path path)
	{
		// Announce that the route is ready
		routeReady();
		
		// Queue all the nodes in the path
		nodeQueue.clear();
		for(Node node : path)
		{
			nodeQueue.add(node);
		}
		
		// Clear any previous instruction listeners
		if(currentInstruction != null)
		{
			currentInstruction.removeInstructionCompleteListener(instructionListener);
		}
		
		// Start navigating the aircraft to the first waypoint
		nextWaypoint();
	}
	
	private void routeReady()
	{
		// If the aircraft is not in the scenario, add it
		if(!inScenario)
		{
			Simulator.getInstance().addAircraft(aircraft);
			inScenario = true;
		}
	}
	
	private synchronized void nextWaypoint()
	{
		// Get the next node
		SpatialNode node = (SpatialNode) nodeQueue.poll();
		if(node == null)
		{
			manager.recalculatePaths();
			return;
		}
		
		// Remove all instructions for the aircraft
		aircraft.clearInstructions();
		
		// Get the waypoint to navigate to
		Waypoint waypoint = null;
		if(node instanceof SpatialNodeWaypoint)
		{
			waypoint = ((SpatialNodeWaypoint) node).getWaypoint();
		}
		else if(node instanceof SpatialNode)
		{
			// Create a temporary waypoint
			waypoint = new Waypoint(node.name, node.location, WaypointType.INTERSECTION);
		}
		
		Instruction instruction = null;
		if(node.equals(currentPosition) && nodeQueue.peek() != null && nodeQueue.peek() == currentPosition)
		{
			// Hold instruction:
			instruction = new HoldInstruction(30, waypoint); // 30 second legs
		}
		else
		{
			// Navigate to a waypoint:
			// See if there is an altitude change
			int altitude = node.altitude * 100;
			if(aircraft.getAltitude() != altitude)
			{
				// Tell the aircraft to descend/climb
				Simulator.getInstance().sendInstruction(new AltitudeInstruction(altitude), aircraft);
			}
			
			// Send a navigate to waypoint instruction
			instruction = new WaypointInstruction(waypoint);
			SpatialNode next = (SpatialNode) nodeQueue.peek();
			if(next != null)
			{
				// If the next node is available, premptively estimate the distance required to turn to the next waypoint
				double defaultDistance = ((WaypointInstruction)instruction).completionDistance;
				double currentBearing = GraphBuilder.calculateBearing(aircraft.getLocation().getLatitude(), aircraft.getLocation().getLongitude(), 
						waypoint.getLocation().getLatitude(), waypoint.getLocation().getLongitude());
				double nextBearing = GraphBuilder.calculateBearing(waypoint.getLocation().getLatitude(), waypoint.getLocation().getLongitude(), 
						next.location.getLatitude(), next.location.getLongitude());
				double turnAmount = Math.abs(currentBearing - nextBearing);
				double estimatedTurnTime = (turnAmount / 180D) / 60D; // estimated turn time in hours
				double estimatedDistance = (aircraft.getTrueAirSpeed() * estimatedTurnTime) * 1852; // distance in metres
				((WaypointInstruction)instruction).completionDistance = Math.max(defaultDistance, estimatedDistance);
			}
		}
		
		// Add listener for when the instruction completes
		instructionListener = new InstructionListener() {

			@Override
			public void onInstructionComplete(Instruction instruction, Aircraft aircraft)
			{				
				currentPosition = node;
				
				// See if the goal has been reached
				if(AircraftAgent.this.isAtGoal() || currentPosition.equals(goal))
				{
					// Remove itself from the agent manager
					manager.removeAgent(AircraftAgent.this);
					Simulator.getInstance().removeAircraft(AircraftAgent.this.aircraft);
					return;
				}
				
				EventQueue.invokeLater(new Runnable() {

					@Override
					public void run()
					{
						nextWaypoint();	
					}
					
				});
								
			}
			
		};
		instruction.addInstructionCompleteListener(instructionListener);
		
		// Send instruction
		currentInstruction = instruction;
		Simulator.getInstance().sendInstruction(instruction, aircraft);
	}

	@Override
	public String toString()
	{
		return aircraft.getCallsign();
	}
	

}
