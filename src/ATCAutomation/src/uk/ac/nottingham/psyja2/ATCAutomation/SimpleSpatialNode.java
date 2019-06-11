package uk.ac.nottingham.psyja2.ATCAutomation;

import uk.ac.nottingham.psyja2.ATCAutomation.PathFinding.Node;
import uk.ac.nottingham.psyja2.ATCSimulator.Coordinate;

public class SimpleSpatialNode extends Node
{

	protected Coordinate location;
	protected String name;
	
	public SimpleSpatialNode(String name, Coordinate location)
	{
		this.name = name;
		this.location = location;
	}	
	
	@Override
	public double getHeuristicTo(Node node)
	{
		return getCostTo(node);
	}

	@Override
	public double getCostTo(Node node)
	{
		Coordinate goal = ((SimpleSpatialNode)node).location;
		return GraphBuilder.calculateDistance(location.getLatitude(), location.getLongitude(), goal.getLatitude(), goal.getLongitude());
	}

	@Override
	public String toString()
	{
		return name;
	}
	
	@Override
    public boolean equals(Object other)
    {
    	if(other instanceof SimpleSpatialNode)
    	{
    		return ((SimpleSpatialNode)other).name.equals(name);
    	}
    	return false;
    }

}
