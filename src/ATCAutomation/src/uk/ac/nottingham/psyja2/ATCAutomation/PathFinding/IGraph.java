package uk.ac.nottingham.psyja2.ATCAutomation.PathFinding;

import java.util.List;
import java.util.Set;

/**
 * A generic representation of a graph with Nodes and connections
 * @author Josh
 *
 */
public interface IGraph
{
	public void addNode(Node node, List<Node> connected);
	
	public int getCardinality();
	
	public int getNumberOfEdges();
	
	public Set<Node> getAllNodes();
	
	public List<Node> getSuccessors(Node node);
}
