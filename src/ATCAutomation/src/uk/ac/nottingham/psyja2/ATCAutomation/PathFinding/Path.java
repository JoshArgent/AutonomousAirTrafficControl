package uk.ac.nottingham.psyja2.ATCAutomation.PathFinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class Path implements Iterable<Node>
{

	protected ArrayList<Node> path;

	public Path()
	{
		path = new ArrayList<Node>();
	}
	
	public Path(ArrayList<Node> nodes)
	{
		path = nodes;
	}
	
	public void addToPath(Node node)
	{
		path.add(node);
	}
	
	public int getPathLength()
	{
		return path.size();
	}
	
	public void clearPath()
	{
		path.clear();
	}
	
	public Path getSubPath(int startIndex, int endIndex)
	{
		return new Path((ArrayList<Node>) path.subList(startIndex, endIndex));
	}
	
	public Path getSubPath(int endIndex)
	{
		return getSubPath(0, endIndex);
	}
	
	public void reverse()
	{
		Collections.reverse(path);
	}
	
	@Override
	public Iterator<Node> iterator()
	{
		return path.iterator();
	}
	
	@Override
	public String toString()
	{
		String result = "[";
		for(Node node : path)
		{
			result += node + ", ";
		}
		if(result.length() > 1)
		{
			result = result.substring(0, result.length() - 2);
		}
		result += "]";
		return result;
	}
	
}
