package uk.ac.nottingham.psyja2.ATCAutomation;

import java.util.ArrayList;
import java.util.List;

import uk.ac.nottingham.psyja2.ATCAutomation.PathFinding.Node;
import uk.ac.nottingham.psyja2.ATCAutomation.PathFinding.Path;
import uk.ac.nottingham.psyja2.ATCAutomation.PathFinding.WHCAStar;

public class SpatialWHCAStar extends WHCAStar
{
	public SpatialWHCAStar()
	{
		super(Preferences.WINDOW_SIZE / 60D);
	}
	
	@Override
	protected double getReservationTime()
	{
		return Preferences.RESERVATION_TIME / 60D;
	}
	
	@Override
	public List<Node> getSuccessors(Node node)
	{
		// Only return successors that are not occupied in the reservation table
		ArrayList<Node> successors = new ArrayList<>();
		for(Node successor : adjacencyList.get(node))
		{
			double time = node.g + getCost(node, successor);
			if(((SpatialNode)successor).altitude != ((SpatialNode)node).altitude)
			{
				// Change in altitude, check the whole altitude block is free
				SpatialNode[] blockNodes = getAltitudeBlockNodes((SpatialNode)node, (SpatialNode)successor);
				boolean free = true;
				for(Node blockNode : blockNodes)
				{
					if(!isNodeFreeAtTime(blockNode, time))
					{
						// Block not free
						free = false;
						break;
					}
				}
				if(free)
				{
					// Block is free, return the successor
					successors.add(successor);
				}
			}
			else
			{
				// Check the successor if free in the reservation table
				if(isNodeFreeAtTime(successor, time))
				{
					successors.add(successor);
				}
			}
		}
		if(isNodeFreeAtTime(node, node.g + getCost(node, node)))
		{
			// Add the option of 'waiting' on the same node
			successors.add(node); 
		}
				
		return successors;
	}
	
	@Override
	protected Path constructPath(Node from, Node to)
	{
		Path path = new Path();
		Node current = to;
		path.addToPath(to);
		
		while(current.previous != null)
		{
			// Build the path
			double time = current.previous.g;
						
			// Get the altitude block of nodes to reserve
			SpatialNode[] blockNodes = getAltitudeBlockNodes((SpatialNode)current, (SpatialNode)current.previous);
			
			// If the g value is greater than the normal cost, then the aircraft is to hold at this node
			while(time < current.g)
			{
				for(Node node : blockNodes)
					reserveNodeAtTime(node, time);
				path.addToPath(current);
				time += getCost(current.previous, current); // This has been changed! Original way current to current
			}			
			
			// Next node
			current = current.previous;
			if(current.equals(from))
				break;
		}
		path.reverse();
		return path;
	}
	
	/*
	 * Given two connected nodes, determine what nodes exist in the block of altitudes
	 * (Assuming the nodes are at different altitudes)
	 */
	private SpatialNode[] getAltitudeBlockNodes(SpatialNode current, SpatialNode next)
	{
		// Check for null references
		if(next == null)
			return new SpatialNode[] { current };
		
		// Determine if there was a change in altitude
		int currentAlt = ((SpatialNode) current).altitude;
		int previousAlt = ((SpatialNode) next).altitude;
		int differenceAlt = previousAlt - currentAlt;
		
		if(Math.abs(differenceAlt) > 0)
		{
			SpatialNode blockNodesArray[] = new SpatialNode[(Math.abs(differenceAlt) / 10) * 2 + 2];			
			SpatialNode node = (SpatialNode) current;
			for(int i = 0; i < blockNodesArray.length / 2; i += 1)
			{
				blockNodesArray[i] = node;
				if(differenceAlt > 0)
				{
					if(node != null)
						node = node.above;
				}
				else
				{
					if(node != null)
						node = node.below;
				}
			}
			node = (SpatialNode) next;
			for(int i = blockNodesArray.length / 2; i < blockNodesArray.length; i += 1)
			{
				blockNodesArray[i] = node;
				if(differenceAlt > 0)
				{
					if(node != null)
						node = node.above;
				}
				else
				{
					if(node != null)
						node = node.below;
				}
			}
			
			return blockNodesArray;
		}
		
		return new SpatialNode[] { current };

	}
	
	
}
