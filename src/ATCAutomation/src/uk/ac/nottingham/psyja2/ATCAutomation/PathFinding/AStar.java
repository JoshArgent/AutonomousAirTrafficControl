package uk.ac.nottingham.psyja2.ATCAutomation.PathFinding;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeoutException;

public class AStar implements IGraph
{
	
	public long TIMEOUT = 10000L; // Milliseconds to time (0 indicates no timeout)
	public static int numberOfNodesExplored = 0;
	protected final Map<Node, List<Node>> adjacencyList;
	protected final TreeSet<Node> openList;
	protected final HashSet<Node> closedList;
	protected int previousHashCode = 0;
	private int numberOfEdges = 0;
	
	public AStar()
	{
		// Initialise the adjacency list
		adjacencyList = new HashMap<Node, List<Node>>();
		
		// Initialise the open + closed lists
		openList = new TreeSet<Node>();
		closedList = new HashSet<Node>();
	}
	
	@Override
	public void addNode(Node node, List<Node> connected)
	{
		// Keep a reference to this graph, inside the node
		node.graph = this;
		
		// Add the node to the adjacency list
		adjacencyList.put(node, connected);
		
		// Link all connected nodes to this new node (if they exist)
		for(Node child : connected)
		{
			if(adjacencyList.containsKey(child))
			{
				List<Node> children = adjacencyList.get(child);
				if(!children.contains(node))
				{
					children.add(node);
				}
				numberOfEdges++;
			}
			
		}
	}
	
	@Override
	public int getCardinality()
	{
		return adjacencyList.size();
	}
	
	@Override
	public int getNumberOfEdges()
	{
		return numberOfEdges;
	}
	
	@Override
	public Set<Node> getAllNodes()
	{
		return adjacencyList.keySet();
	}
	
	@Override
	public List<Node> getSuccessors(Node node)
	{
		return adjacencyList.get(node);
	}
	
	@Override
	public String toString()
	{
		// Convert the adjacency list into a nicely formatted string
		StringBuilder builder = new StringBuilder();
		for(Node node : adjacencyList.keySet())
		{
			builder.append(node.toString() + ": ");
			for(Node child : getSuccessors(node))
			{
				builder.append(child.toString() + " ");
			}
			builder.append("\n");			
		}
		builder.append("Cardinality = " + getCardinality() + "\n");
		builder.append("Number of Edges = " + getNumberOfEdges());
		return builder.toString();
	}	
	
	// Credit:
	// https://en.wikipedia.org/wiki/A%2a_search_algorithm#Pseudocode
	public Path findPath(Node from, Node to) throws Exception
	{
		long startTime = System.currentTimeMillis();
		
		// Clear the open and closed lists
		openList.clear();
		closedList.clear();
		
		// Calculate the g + h values for the from node
		from.g = 0;
		from.h = from.getHeuristicTo(to);
		
		// Put starting node on the open list
		openList.add(from);
		
		// While the open list is not empty
		Node current;
		while(!openList.isEmpty())
		{
			// Pop the node with lowest f() from the open list
			current = openList.pollFirst();
			
			// If current is the goal, stop the search
			if(current.equals(to))
			{
				// Stop the search and return the path
				return constructPath(from, to);
			}

			// Loop through the successor nodes to the current node
			for(Node successor : getSuccessors(current))
			{						
				// Calculate g, h and time values
				double g = current.g + getCost(current, successor);
				double h = getHeuristic(successor, to);
				
				if(!openList.contains(successor) && !closedList.contains(successor))
				{	
					// The successor has not been found yet, add it to the open list
					if(!successor.equals(current))
					{
						successor.previous = current;
					}
					successor.g = g;
					successor.h = h;				
					openList.add(successor);
				}
				else if(openList.contains(successor) && g + h < successor.f())
				{
					// A better route to has been found, update the g and h values
					openList.remove(successor);
					if(!successor.equals(current))
					{
						successor.previous = current;
					}
					successor.g = g;
					successor.h = h;
					openList.add(successor);
				}
				
				numberOfNodesExplored++;
			}
			
			// Push the current node to the closed list
			closeNode(current);
			
			// Check for deadlocks
			if(isInDeadlock())
			{
				// throw an exception
				throw new DeadlockException();
			}		
			// Check for timeout
			if(TIMEOUT > 0 && startTime + TIMEOUT < System.currentTimeMillis())
			{
				// throw an exception
				throw new TimeoutException("Search timeout");
			}
		}
		
		throw new NoPathFoundException();
	}
	
	/*
	 * Re-constructs a path from the labeled nodes (returns it in reverse order)
	 */
	protected Path constructPath(Node from, Node to)
	{
		Path path = new Path();
		Node current = to;
		while(current.previous != null)
		{
			path.addToPath(current);
			current = current.previous;
			if(current.equals(from))
				break;
		}
		path.addToPath(from);
		path.reverse();
		return path;
	}
	
	/*
	 * Called when the A* algorithm is about to put a node on the closed list
	 */
	protected void closeNode(Node node)
	{
		// Push the current node to the closed list
		closedList.add(node);
	}
	
	/**
	 * Get the heuristic cost h() between these two nodes
	 */
	protected double getHeuristic(Node from, Node to)
	{
		return from.getHeuristicTo(to);
	}
	
	/**
	 * Get the actual cost g() between these two nodes
	 */
	protected double getCost(Node from, Node to)
	{
		return from.getCostTo(to);
	}
	
	protected boolean isInDeadlock()
	{
		int hashCode = (closedList.hashCode() / 2) + openList.hashCode();
		if(previousHashCode == hashCode)
		{
			return true;
		}
		previousHashCode = hashCode;	
		return false;
	}
	
	public class DeadlockException extends Exception { 
		private static final long serialVersionUID = 704703618969538902L; 
		public DeadlockException() { super("Deadlock"); }
	}	
	
	public class NoPathFoundException extends Exception {
		private static final long serialVersionUID = 8325399812546436595L;
		public NoPathFoundException() { super("No path found"); }
	}
			
}
