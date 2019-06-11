package uk.ac.nottingham.psyja2.ATCAutomation.PathFinding;

public class Agent implements Comparable<Agent>
{

	protected Node currentPosition;
	protected Node goal;
	protected AgentManager manager;
	protected double pathCost = 1;
	protected double heuristicCost = 1;
	
	public Agent(Node start, Node goal)
	{
		this.currentPosition = start;
		this.goal = goal;
	}
	
	/**
	 * @return the node that the agent is currently at
	 */
	public Node getPosition()
	{
		return currentPosition;
	}
	
	/**
	 * @return the agent's goal node
	 */
	public Node getGoal()
	{
		return goal;
	}
	
	/**
	 * The priority level indicates the agent's priority compared to other agents
	 * A higher priority level gives the agent a higher priority in the future
	 * @return the level of priority
	 */
	public double getPriority()
	{		
		// Method 1: Path Optimality (agitation)
		// Compare the actual cost to the heuristic to find out how 'good' the path is
		return pathCost / heuristicCost;
	}
	
	/**
	 * Called by the manager, telling the agent to run the given path
	 * @param path
	 */
	public synchronized void runPath(Path path)
	{
		// Simulate moving through the path
		for(Node node : path)
		{
			currentPosition = node;
		}
	}
	
	/**
	 * Returns true if the agent has reached their goal node
	 */
	public boolean isAtGoal()
	{
		return getGoal().equals(getPosition());
	}

	@Override
	public int compareTo(Agent agent)
	{
		return -1 * Double.compare(getPriority(), agent.getPriority());
	}
	
}
