package uk.ac.nottingham.psyja2.ATCAutomation;

import uk.ac.nottingham.psyja2.ATCSimulator.Coordinate;

public class SpatialNodeIntersection extends SpatialNode
{
	
	protected SpatialNode parentA1;
	protected SpatialNode parentA2;
	protected SpatialNode parentB1;
	protected SpatialNode parentB2;

	public SpatialNodeIntersection(String name, Coordinate location, int altitude, SpatialNode parentA1, SpatialNode parentA2, SpatialNode parentB1, SpatialNode parentB2)
	{
		super(name, location, altitude);
		
		this.parentA1 = parentA1;
		this.parentA2 = parentA2;
		this.parentB1 = parentB1;
		this.parentB2 = parentB2;
	}

}
