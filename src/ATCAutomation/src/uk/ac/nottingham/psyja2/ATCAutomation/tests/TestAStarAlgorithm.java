package uk.ac.nottingham.psyja2.ATCAutomation.tests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Test;

import uk.ac.nottingham.psyja2.ATCAutomation.PathFinding.AStar;
import uk.ac.nottingham.psyja2.ATCAutomation.PathFinding.IGraph;
import uk.ac.nottingham.psyja2.ATCAutomation.PathFinding.Node;
import uk.ac.nottingham.psyja2.ATCAutomation.PathFinding.Path;

public class TestAStarAlgorithm
{

	@Test
	public void testAStarAlgorithm()
	{
		// Test AStar on some larger graphs
		testShortestPath(new AStar(), "0 : 2 3 4 7 8 9;1 : 2 3 8 9;2 : 0 1 7 9;3 : 0 1 4 7 9;4 : 0 3 6 9;5 : 8;6 : 4 7;7 : 0 2 3 6 9;8 : 0 1 5 9;9 : 0 1 2 3 4 7 8;",
				"0", "5", "0 8 5");
		testShortestPath(new AStar(), "0 : 1 6 9 10;1 : 0 11 12;2 : 3 4 7;3 : 2 9 10 12;4 : 2 12;5 : 7 8 14;6 : 0 14;7 : 2 5;8 : 5 9 10 12;9 : 0 3 8 12 14;10 : 0 3 8 12 13;11 : 1;12 : 1 3 4 8 9 10 13;13 : 10 12;14 : 5 6 9;", "11", "14", "11 1 0 6 14");
		testShortestPath(new AStar(), "0 : 3 8 14;1 : 10 19;2 : 5 11 18 19;3 : 0 14 18;4 : 7;5 : 2;6 : 12 14;7 : 4 8;8 : 0 7;9 : 12 18;10 : 1 11;11 : 2 10 18;12 : 6 9;13 : 19;14 : 0 3 6 16 18;15 : 16;16 : 14 15 17;17 : 16;18 : 2 3 9 11 14 19;19 : 1 2 13 18;", "15", "4", "15 16 14 0 8 7 4");
	}

	
	private void testShortestPath(AStar graph, String graphStr, String fromStr, String toStr, String expectedStr)
	{
		// Generate graph object
		generateGraphObject(graph, graphStr);
		
		// Find the from and to nodes
		SimpleNode from = null;
		SimpleNode to = null;
		for(Node node : graph.getAllNodes())
		{
			SimpleNode sn = (SimpleNode) node;
			if(sn.toString().equalsIgnoreCase(fromStr))
				from = sn;
			if(sn.toString().equalsIgnoreCase(toStr))
				to = sn;
		}
		
		// Calculate shortest path
		Path path = null;
		try
		{
			path = graph.findPath(from, to);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		
		// Compare to the expected
		String actual = "";
		for(Node node : path)
		{
			actual += node.toString() + " ";
		}
		actual = actual.substring(0, actual.length() - 1);
		assertEquals(expectedStr, actual);
	}
	
	/*
	 * Reads a text representation of a graph and generates an AStar object for it
	 */
	private void generateGraphObject(IGraph graph, String graphStr)
	{
		String lines[] = graphStr.split(";");
		HashMap<String, Node> nodes = new HashMap<>();
		for(String line : lines)
		{
			String parts[] = line.split(" ");
			String nodeName = parts[0];
			SimpleNode node = new SimpleNode(nodeName);
			ArrayList<Node> connectedTo = new ArrayList<>();
			for(int i = 2; i < parts.length; i++)
			{
				if(nodes.containsKey(parts[i]))
				{
					connectedTo.add(nodes.get(parts[i]));
				}
			}
			nodes.put(nodeName, node);
			graph.addNode(node, connectedTo);
		}
	}
	

}
