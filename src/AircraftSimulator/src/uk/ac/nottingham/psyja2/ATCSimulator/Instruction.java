package uk.ac.nottingham.psyja2.ATCSimulator;

import java.util.HashSet;

/**
 * A generic instruction that provides basic methods for implementing an instruction.
 * @author Josh Argent
 *
 */
public abstract class Instruction
{
	
	private boolean complete = false;
	private HashSet<InstructionListener> instructionListeners = new HashSet<InstructionListener>();
	
	/**
	 * Execute the instruction on the aircraft over a specified amount of time (in seconds)
	 * @param aircraft the aircraft to perform the instruction on
	 * @param time the time elapsed since the last update (in seconds)
	 */
	public abstract void execute(Aircraft aircraft, double time);
	
	@Override
	public abstract String toString();
	
	/**
	 * Returns true if the instruction has been completed eg. reached asigned altitude
	 */
	public boolean isComplete()
	{
		return complete;
	}
		
	/**
	 * Add an InstructionListener to this instruction
	 * @param listener
	 */
	public void addInstructionCompleteListener(InstructionListener listener)
	{
		instructionListeners.add(listener);
	}
	
	/**
	 * Remove an InstructionListener from this instruction
	 * @param listener
	 */
	public void removeInstructionCompleteListener(InstructionListener listener)
	{
		instructionListeners.remove(listener);
	}
	
	/**
	 * Fire all the InstructionListener's
	 */
	protected void fireInstructionComplete(Aircraft aircraft)
	{
		complete = true;
		for(InstructionListener listener : instructionListeners)
		{
			listener.onInstructionComplete(this, aircraft);
		}
	}

}
