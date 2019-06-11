package uk.ac.nottingham.psyja2.ATCAutomation.PathFinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.TimeoutException;

import uk.ac.nottingham.psyja2.ATCAutomation.PathFinding.AStar.DeadlockException;
import uk.ac.nottingham.psyja2.ATCAutomation.PathFinding.AStar.NoPathFoundException;

public class AgentManager
{

	protected ArrayList<Agent> agents;
	public WHCAStar graph;
	private Thread agentManagerThread;
	private AgentManagerRunnable agentManagerThreadRunnable;
	private ArrayList<RecalculatePathsListener> listeners = new ArrayList<>();
	
	public AgentManager(WHCAStar graph)
	{
		this.graph = graph;
		this.agents = new ArrayList<>();
		
		// Init thread
		agentManagerThreadRunnable = new AgentManagerRunnable();
		agentManagerThread = new Thread(agentManagerThreadRunnable);
		agentManagerThread.setName("Agent Manager");
		agentManagerThread.start();
	}
	
	/**
	 * Returns the moving phase length (K), the amount of steps before it should recalculate paths
	 * 0 < K < W
	 */
	public double getMovingPhaseLength()
	{
		return graph.getWindowSize() / 2; // K = W / 2
	}
	
	/**
	 * Add an agent to the system
	 * @param agent
	 */
	public synchronized void addAgent(Agent agent)
	{
		agents.add(agent);
		agent.manager = this;
	}
	
	/**
	 * @return a list of all the agents in the system
	 */
	public ArrayList<Agent> getAgents()
	{
		return agents;
	}
	
	/**
	 * Remove an agent from the system
	 * @param agent
	 */
	public synchronized void removeAgent(Agent agent)
	{
		agents.remove(agent);
	}
	
	public void removeAllAgents()
	{
		agents.clear();
	}
	
	/**
	 * Returns true if all the agents have reached their goals
	 */
	public boolean allAgentsAtGoal()
	{
		for(Agent agent : agents)
		{
			if(!agent.isAtGoal())
				return false;
		}
		return true;
	}
	
	public synchronized void recalculatePaths()
	{
		agentManagerThreadRunnable.invalidated = true;
	}
	
	private class AgentManagerRunnable implements Runnable
	{

		public boolean running = true;
		public volatile boolean invalidated = false;
		
		@Override
		public void run()
		{
			while(running)
			{
				// See if all paths have been invalidated (eg. new aircraft has been added!)
				if(invalidated)
				{
					invalidated = false;
					recalculatePaths();
				}
			}
		}
		
		private synchronized void recalculatePaths()
		{
			// Reset the reservation table
			graph.resetReservationTable();
			
			// Sort the list of agents by agitation level
			Agent agentsSorted[] = agents.toArray(new Agent[0]);
			Arrays.sort(agentsSorted);
			
			// Fire the recalculate paths listeners
			for(RecalculatePathsListener listener : listeners)
			{
				listener.recalculatePaths(agentsSorted);
			}
			
			// Reserve all the agent's current positions
			for(Agent agent : agentsSorted)
			{
				if(!agent.isAtGoal())
					graph.reserveNodeAtTime(agent.getPosition(), -graph.getReservationTime());
			}			
			HashMap<Agent, Path> calculatedPaths = new HashMap<>();
			
			// Loop through the agents, starting with the highest agitation level
			for(Agent agent : agentsSorted)
			{						
				// See if the agent has reached it's goal
				if(agent.isAtGoal())
				{
					continue;
				}
				
				// Calculate a path within the moving phase (K) size
				graph.setAgent(agent);

				try
				{
					Path path = graph.findPath(agent.getPosition(), agent.getGoal());
					
					// Trim the path to the moving phase (K) size
					Path trimmedPath = new Path();
					for(Node node : path)
					{
						if(node.g <= getMovingPhaseLength())
						{
							trimmedPath.addToPath(node);
						}
					}
					
					// Save the path
					calculatedPaths.put(agent, trimmedPath);
				}
				catch (Exception ex)
				{
					if(ex instanceof DeadlockException || ex instanceof NoPathFoundException || ex instanceof TimeoutException)
					{
						System.out.println(agent + ": " + ex.getMessage());
						// If a deadlock occurs, reserve the agent's current location
						// and tell the agent to wait at it's current location
						Path path = new Path();
						path.addToPath(agent.getPosition());
						calculatedPaths.put(agent, path);
						graph.reserveNodeAtTime(agent.getPosition(), agent.getPosition().g);
					}
				}
			}
			
			// Run all the calculated paths
			for(Agent agent : calculatedPaths.keySet())
			{
				Path path = calculatedPaths.get(agent);
				if(path != null)
				{
					agent.runPath(path);
				}
			}
		}
		
	}
	
	public void addRecalculatePathsListener(RecalculatePathsListener listener)
	{
		listeners.add(listener);
	}
	
	public void removeRecalculatePathsListener(RecalculatePathsListener listener)
	{
		listeners.remove(listener);
	}
	
	public interface RecalculatePathsListener
	{
		/**
		 * Fired when the agent manager recalculates all the agent paths
		 * @param agentsSorted an array of agents in the order of their priority/agitation
		 */
		void recalculatePaths(Agent agentsSorted[]);
	}
	
	
	
}
