package uk.ac.nottingham.psyja2.ATCSimulator;

/**
 * An implementable interface for listening to an {@link uk.ac.nottingham.psyja2.ATCSimulator.Instruction}
 * @author Josh Argent
 *
 */
public interface InstructionListener
{
	/**
	 * Will be called when an instruction completes
	 * @param instruction the instruction that is been listened to
	 * @param aircraft the aircraft the instruction was executing on
	 */
	void onInstructionComplete(Instruction instruction, Aircraft aircraft);
}
