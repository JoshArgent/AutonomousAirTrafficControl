package uk.ac.nottingham.psyja2.ATCAutomation.PathFinding;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FloydWarshall implements IGraph
{
	
	protected final Map<Node, List<Node>> adjacencyList;
	private int numberOfEdges = 0;	
	private short[][] dist;
	
	public FloydWarshall()
	{
		// Initialise the adjacency list
		adjacencyList = new HashMap<Node, List<Node>>();
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

	public void initialise()
	{
		dist = new short[getCardinality()][getCardinality()];
		
		int id = 0;
		for(Node node : getAllNodes())
		{
			// Save the assigned node ID
			node.id = id;
			id++;
		}
		
		// Initialise all matrix values
		for(int i = 0; i < dist.length; i++)
		{
			for(int j = 0; j < dist.length; j++)
			{
				dist[i][j] = Short.MAX_VALUE;
			}
		}
		
		for(Node node : getAllNodes())
		{
			// Get the node ID
			id = node.id;
			
			// Set distances to self to 0
			dist[id][id] = 0;
			
			// Set immediate successors values
			for(Node successor : getSuccessors(node))
			{
				if(successor != node)
				{
					int succID = successor.id;
					dist[id][succID] = (short) node.getCostTo(successor);
				}
			}
		}
		
		// Calculate the length of all the shortest paths
		for(int k = 0; k < dist.length; k++)
		{
			for(int i = 0; i < dist.length; i++)
			{
				for(int j = 0; j < dist.length; j++)
				{
					dist[i][j] = (short) Math.min(dist[i][j], dist[i][k] + dist[k][j]);
				}
			}			
		}
		
	}
	
	public void clearAdjacencyList()
	{
		// This will free some memory
		adjacencyList.clear();
	}
	
	public double shortestPathLength(Node from, Node goal)
	{
		return dist[from.id][goal.id];
	}
	
	
}
