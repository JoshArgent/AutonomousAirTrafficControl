package uk.ac.nottingham.psyja2.ATCSimulator;

/**
 * Implementable interface to listen for when conflicts between two aircraft occur
 * @author Josh Argent
 *
 */
public interface ConflictListener
{
	/**
	 * Called when two aircraft conflict
	 */
	void conflictEvent(Aircraft aircraftA, Aircraft aircraftB);
}
