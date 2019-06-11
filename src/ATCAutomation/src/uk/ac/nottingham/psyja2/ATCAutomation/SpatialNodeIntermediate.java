package uk.ac.nottingham.psyja2.ATCAutomation;

import uk.ac.nottingham.psyja2.ATCSimulator.Coordinate;

public class SpatialNodeIntermediate extends SpatialNode
{
	
	protected SpatialNode parentA;
	protected SpatialNode parentB;

	public SpatialNodeIntermediate(String name, Coordinate location, int altitude, SpatialNode parentA, SpatialNode parentB)
	{
		super(name, location, altitude);
		
		this.parentA = parentA;
		this.parentB = parentB;
	}

}
