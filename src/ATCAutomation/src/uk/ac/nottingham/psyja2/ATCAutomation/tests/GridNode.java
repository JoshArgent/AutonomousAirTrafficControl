package uk.ac.nottingham.psyja2.ATCAutomation.tests;

import java.util.ArrayList;

import uk.ac.nottingham.psyja2.ATCAutomation.PathFinding.AStar;
import uk.ac.nottingham.psyja2.ATCAutomation.PathFinding.Node;

public class GridNode extends Node
{

	public int x;
	public int y;
	
	public GridNode(int x, int y)
	{
		this.x = x;
		this.y = y;
	}
	
	@Override
	public double getHeuristicTo(Node node)
	{
		if(this.equals(node))
		{
			return 1;
		}
		
		// Use manhatten distance
		GridNode gridNode = (GridNode) node;
		int xDif = Math.abs(gridNode.x - x);
		int yDif = Math.abs(gridNode.y - y);
		return xDif + yDif;
	}

	@Override
	public double getCostTo(Node node)
	{
		return getHeuristicTo(node);
	}

	@Override
	public String toString()
	{
		return "(" + x + ", " + y + ")";
	}
	
	/*
	 * Builds an AStar graph data structure from a int bitmap
	 * 0's = nodes
	 * >0 = walls
	 */
	public static GridNode[][] buildGraphFromGrid(AStar graph, int[][] grid)
	{
		GridNode[][] nodes = new GridNode[grid.length][grid[0].length];
		for(int y = 0; y < grid.length; y++)
		{
			for(int x = 0; x < grid[y].length; x++)
			{
				if(grid[y][x] == 0)
				{
					nodes[y][x] = new GridNode(x, y);
					ArrayList<Node> connected = new ArrayList<>();
					if(y - 1 >= 0 && x - 1 >= 0 && nodes[y-1][x] != null && nodes[y][x-1] != null)
					{
						connected.add(nodes[y-1][x]);
						connected.add(nodes[y][x-1]);
					}
					else if(y - 1 >= 0 && nodes[y-1][x] != null)
					{
						connected.add(nodes[y-1][x]);
					}
					else if(x - 1 >= 0 && nodes[y][x-1] != null)
					{
						connected.add(nodes[y][x-1]);
					}
					graph.addNode(nodes[y][x], connected);
				}
			}
		}
		return nodes;
	}

}
