package uk.ac.nottingham.psyja2.ATCAutomation.PathFinding;


public abstract class Node implements Comparable<Node>
{
	public double g = 0;
	public double h = 0;
	public int id = 0;
	public Node previous;
	protected IGraph graph;
	
	public abstract double getHeuristicTo(Node node);
	
	public abstract double getCostTo(Node node);
	
	@Override
	public abstract String toString();
	
	protected double f()
	{
		return g + h;
	}
    
    @Override
    public int compareTo(Node node) 
    {
        if (this.equals(node))
            return 0;
        if (this.f() < node.f())
            return -1;
        if (this.f() > node.f())
            return 1;
        return 1;
    }
        
}