package uk.ac.nottingham.psyja2.ATCAutomation.PathFinding;

import java.text.SimpleDateFormat;
import java.util.TimeZone;


public class WHCAStar extends CAStar
{
	
	private double windowSize;
	private Agent currentAgent;
	
	public WHCAStar(double windowSize)
	{
		this.windowSize = windowSize;
	}
	
	public double getWindowSize()
	{
		return windowSize;
	}

	public void setWindowSize(double windowSize)
	{
		this.windowSize = windowSize;
	}
	
	public void setAgent(Agent agent)
	{
		this.currentAgent = agent;
	}
	
	public Agent getAgent()
	{
		return currentAgent;
	}
	
	public String getReservationTableString()
	{
		String out =  "";
		for(Node key : reservationTable.keySet())
		{
			out += key + ":\n";
			for(Double[] times : reservationTable.get(key))
			{
				SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
				formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
			    String time1 = formatter.format(times[0] * 3600000);
			    String time2 = formatter.format(times[1] * 3600000);
			    
				out += time1 + " - " + time2 + "\n";
			}
			out += "\n";
		}
		return out;
	}

	@Override
	public void reserveNodeAtTime(Node node, double time)
	{
		if(time > windowSize)
			return;
		super.reserveNodeAtTime(node, time);
	}
	
	@Override
	public Path findPath(Node from, Node to) throws Exception
	{
		Path path = super.findPath(from, to);
		
		// Calculate the cost of this path
		double pathCost = to.g;
		
		// Calculate the heuristic cost
		double heuristicCost = getHeuristic(from, to);
		
		if(currentAgent != null)
		{
			currentAgent.pathCost = pathCost;
			currentAgent.heuristicCost = heuristicCost;
		}
		
		return path;
	}
	
	
	
}
