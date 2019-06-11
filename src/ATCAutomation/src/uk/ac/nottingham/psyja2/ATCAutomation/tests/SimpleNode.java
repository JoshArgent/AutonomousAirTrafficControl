package uk.ac.nottingham.psyja2.ATCAutomation.tests;

import uk.ac.nottingham.psyja2.ATCAutomation.PathFinding.Node;

public class SimpleNode extends Node
{
	private String name;
	
	public SimpleNode(String name)
	{
		this.name = name;
	}

	@Override
	public double getHeuristicTo(Node node)
	{
		// Return 0 => becomes a greedy search
		return 0;
	}
	
	@Override
	public double getCostTo(Node node)
	{
		return 1;
	}

	@Override
	public String toString()
	{
		return name;
	}

}