package uk.ac.nottingham.psyja2.ATCAutomation.PathFinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class CAStar extends AStar
{

	protected HashMap<Node, HashSet<Double[]>> reservationTable = new HashMap<>();
	
	public void resetReservationTable()
	{
		reservationTable.clear();
	}
	
	protected boolean isNodeFreeAtTime(Node node, double time)
	{
		if(reservationTable.containsKey(node))
		{
			for(Double[] entry : reservationTable.get(node))
			{
				if(entry[0] <= time && entry[1] >= time)
				{
					return false;
				}
			}
		}
		return true;
	}
	
	public void reserveNodeAtTime(Node node, double time)
	{
		if(node == null)
			return;
		if(reservationTable.containsKey(node))
		{
			reservationTable.get(node).add(new Double[] {time - (getReservationTime() / 2D), time + (getReservationTime() / 2D)});
		}
		else
		{
			HashSet<Double[]> times = new HashSet<>();
			times.add(new Double[] {time - (getReservationTime() / 2D), time + (getReservationTime() / 2D)});
			reservationTable.put(node, times);
		}
	}
	
	/**
	 * The amount of time an agent should reserve a node for, when it uses it
	 * @return
	 */
	protected double getReservationTime()
	{
		return 1;
	}
	
	@Override
	public List<Node> getSuccessors(Node node)
	{
		// Only return successors that are not occupied in the reservation table
		ArrayList<Node> successors = new ArrayList<>();
		for(Node successor : adjacencyList.get(node))
		{
			if(isNodeFreeAtTime(successor, node.g + getCost(node, successor)))
			{
				successors.add(successor);
			}
		}
		if(isNodeFreeAtTime(node, node.g + getCost(node, node)))
		{
			// Add the option of 'waiting' on the same node
			successors.add(node); 
		}
		return successors;
	}
	
	/*
	 * Re-constructs a path from the labeled nodes (returns it in reverse order)
	 * CA* => Reserve all these nodes
	 */
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
			while(time < current.g)
			{
				reserveNodeAtTime(current, time);
				path.addToPath(current);
				time += getCost(current.previous, current);
			}
			
			// Next node
			current = current.previous;
			if(current.equals(from))
				break;
		}
		path.reverse();
		return path;
	}
	
	@Override
	protected void closeNode(Node node)
	{
		// Add the option of waiting
		if(openList.isEmpty())
		{
			// The search is abouts to fail.
			// Increment the current node's time and try again - just need to be patient!
			if(isNodeFreeAtTime(node, node.g + getCost(node, node)))
			{
				node.g += getCost(node, node);
				openList.add(node);
			}
		}
		else
		{
			// Push the current node to the closed list
			closedList.add(node);
		}
	}
	
}
