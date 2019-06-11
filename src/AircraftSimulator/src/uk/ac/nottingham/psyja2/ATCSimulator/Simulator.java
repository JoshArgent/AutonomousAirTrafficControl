package uk.ac.nottingham.psyja2.ATCSimulator;

import java.awt.EventQueue;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;

import org.json.simple.parser.ParseException;

/**
 * A singleton class to represent an instance of a simulator
 * @author Josh Argent
 *
 */
public class Simulator
{
	
	private static Simulator instance;
	public static String SCENARIO_FILE = "C:/Users/Josh/Desktop/scenario.json";
	private static double EARTH_RADIUS = 6371e3;
	private static double FIVE_MILES_METRES = 9260; // 5 nautical miles in metres
	
	private List<Aircraft> aircrafts;
	private int windSpeed;
	private int windDirection;
	private Scenario scenario;
	private float timeSpeed;
	private SimulatorDisplay display;
	private SimulatorControls controls;
	private Thread simulatorThread;
	private int numberOfInstructions;
	private String instructionLog = "";
	
	private double time;
	private Queue<Double> flowHistory;
	
	private int numberOfConflicts;
	protected Map<String, Double> conflictingHistory; // Map an aircraft pair (eg. 'ABC/DEF' to time of last conflict)
	private List<ConflictListener> conflictListeners;	
	
	/**
	 * Returns the singleton instance of the Simulator
	 */
	public static Simulator getInstance()
	{
		if(instance == null)
			instance = new Simulator();
		return instance;
	}
	
	private Simulator()
	{
		// Init variables
		aircrafts = new CopyOnWriteArrayList<Aircraft>();
		windSpeed = 0;
		windDirection = 270;
		timeSpeed = 1f;
		numberOfInstructions = 0;
		time = 0;
		
		// Load the scenario file
		try
		{
			scenario = new Scenario(SCENARIO_FILE);
		} catch (IOException | ParseException e)
		{
			e.printStackTrace();
		}
		
		// Create a display object
		display = new SimulatorDisplay();
				
		// Initialise the flow rate data structure
		flowHistory = new LinkedList<Double>();
		
		// Initialise conflicting aircraft data structure
		conflictingHistory = new HashMap<String, Double>();
		conflictListeners = new ArrayList<ConflictListener>();
		
		// Start the simulator thread
		simulatorThread = new Thread(new AircraftUpdateRunnable());
		simulatorThread.setName("Simulator");
		simulatorThread.start();
	}
	
	private void updateUI()
	{
		// Run repaint/GUI updates on the UI thread
		EventQueue.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				display.repaint();
				getControls().updateControlValues();
			}
		});		
	}
	
	/**
	 * Add an aircraft to the simulation
	 */
	public synchronized void addAircraft(Aircraft aircraft)
	{
		synchronized(aircrafts)
		{
			aircrafts.add(aircraft);
		}
		updateUI();	
	}
	
	/**
	 * Return an aircraft by its callsign
	 */
	public synchronized Aircraft getAircraft(String callsign)
	{
		synchronized(aircrafts)
		{
			for(Aircraft aircraft : aircrafts)
			{
				if(aircraft.getCallsign().equalsIgnoreCase(callsign))
					return aircraft;
			}
		}
		return null;
	}
	
	/**
	 * Remove an aircraft from the simulation
	 */
	public synchronized void removeAircraft(Aircraft aircraft)
	{
		// Remove the aircraft and log the time it exited the sim
		synchronized(aircrafts)
		{
			flowHistory.add(time);
			aircrafts.remove(aircraft);
		}
		
		updateUI();
	}
	
	/**
	 * Set the simulation speed;
	 * 1 = real time,
	 * 0 = pause
	 */
	public synchronized void setTimeSpeed(float timeSpeed)
	{
		this.timeSpeed = timeSpeed;
		updateUI();	
	}
	
	/**
	 * Get the simulation speed;
	 * 1 = real time,
	 * 0 = paused
	 */
	public synchronized float getTimeSpeed()
	{
		return timeSpeed;
	}
	
	/**
	 * Return the time in the simulator (in seconds since start)
	 */
	public synchronized double getTime()
	{
		return time;
	}
	
	/**
	 * Set the simulator wind speed (in knots)
	 */
	public synchronized void setWindSpeed(int speed)
	{
		windSpeed = speed;
		updateUI();	
	}
	
	/**
	 * Get the simulator wind speed (in knots)
	 */
	public synchronized int getWindSpeed()
	{
		return windSpeed;
	}
	
	/**
	 * Set the simulator wind direction (in degrees 0 to 360)
	 */
	public synchronized void setWindDirection(int direction)
	{
		windDirection = direction;
		updateUI();	
	}
	
	/**
	 * Get the simulator wind direction (in degrees 0 to 360)
	 */
	public synchronized int getWindDirection()
	{
		return windDirection;
	}
	
	public synchronized List<Aircraft> getAllAircraft()
	{
		return aircrafts;
	}
	
	
	/**
	 * Get the simulator GUI radar display
	 */
	public SimulatorDisplay getDisplay()
	{
		return display;
	}
	
	/**
	 * Get the simulator controls GUI object
	 */
	public SimulatorControls getControls()
	{
		if(controls == null)
		{
			// Create the controls object
			controls = new SimulatorControls();
		}
		return controls;
	}

	/**
	 * Issue an instruction to an aircraft
	 * @param instruction the instruction to execute
	 * @param aircraft the aircraft to execute the instruction on
	 */
	public synchronized void sendInstruction(Instruction instruction, Aircraft aircraft)
	{
		synchronized(aircraft.instructions)
		{
			// Check if there are any conflicting instructions..
			if(instruction instanceof HeadingInstruction || instruction instanceof WaypointInstruction ||
					instruction instanceof HoldInstruction)
			{
				List<Instruction> instructionListCopy = new ArrayList<Instruction>(aircraft.instructions);
				// Remove any 'HeadingInstruction' or 'WaypointInstruction' or 'HoldInstruction'
				for(Instruction instr : aircraft.instructions)
				{
					if(instr instanceof HeadingInstruction || instr instanceof WaypointInstruction
							|| instr instanceof HoldInstruction)
					{
						instructionListCopy.remove(instr);
					}
				}
				aircraft.instructions = instructionListCopy;
			}
			// Add the instruction to the aircraft's instruction list
			aircraft.instructions.add(instruction);
			instructionLog += aircraft.getCallsign() + ", " + instruction.toString() + "\n";
			numberOfInstructions++;
		}
		
		updateUI();	
	}
	
	/**
	 * Returns the Scenario object
	 */
	public Scenario getScenario()
	{
		return scenario;
	}
	
	/**
	 * Returns the number of aircraft within the simulator
	 */
	public synchronized int getNumberOfAircraft()
	{
		return aircrafts.size();
	}
	
	/**
	 * Returns the hourly flow rate of the simulation
	 * (The amount of aircraft which left the simulation in the last hour)<br>
	 * Returns -1 if not enough data is available
	 */
	public synchronized float getFlowRate()
	{
		if(time < 3600) // 1 hr 
			return -1; // If the sim has not ran for 1 hour; no flow rate is available
		
		// Remove items from the queue that are over an hour old
		Queue<Double> queue2 = new LinkedList<Double>();
		while(!flowHistory.isEmpty())
		{
			Double item = flowHistory.poll();
			if(item > time - 3600)
				queue2.add(item);
		}
		flowHistory = queue2;
		
		// The flow rate is the number of items left in the queue
		return flowHistory.size();
	}
	
	/**
	 * Returns the number of conflicts
	 */
	public synchronized int getNumberOfConflicts()
	{
		return numberOfConflicts;
	}
	
	/**
	 * Returns the number of instructions/conflict resolution manoeuvres
	 */
	public synchronized int getNumberOfInstructions()
	{
		return numberOfInstructions;
	}	
	
	/**
	 * Returns a string log of all instructions sent to aircraft
	 */
	public synchronized String getInstructionLog()
	{
		return instructionLog;
	}
	
	/**
	 * Add a conflict listener to the Simulator
	 */
	public synchronized void addConflictListener(ConflictListener listener)
	{
		conflictListeners.add(listener);
	}
	
	/**
	 * Remove a conflict listener from the Simulator
	 */
	public synchronized void removeConflictListener(ConflictListener listener)
	{
		conflictListeners.remove(listener);
	}
	
	/**
	 * Fires all the conflict listeners
	 */
	private void fireConflictListeners(Aircraft aircraftA, Aircraft aircraftB)
	{
		for(ConflictListener listener : conflictListeners)
		{
			listener.conflictEvent(aircraftA, aircraftB);
		}
		
		updateUI();	
	}
	
	/**
	 * Returns true/false for if the given pair of aircraft are conflicting.<br>
	 * Conflict minima = within 1000ft AND less than 5NM apart
	 */
	private boolean isConflicting(Aircraft aircraftA, Aircraft aircraftB)
	{
		// First check if the aircraft are vertically within 1000ft of each other
		if(aircraftA.altitude - 1000 >= aircraftB.altitude || aircraftA.altitude + 1000 <= aircraftB.altitude)
		{
			return false;
		}
		
		// Now check if the aircraft 
		// CREDIT: https://www.movable-type.co.uk/scripts/latlong.html
		double lat1 = toRadians(aircraftA.getLocation().getLatitude());
		double lat2 = toRadians(aircraftB.getLocation().getLatitude());
		double deltaLat = toRadians(aircraftB.getLocation().getLatitude() - aircraftA.getLocation().getLatitude());
		double deltaLng = toRadians(aircraftB.getLocation().getLongitude() - aircraftA.getLocation().getLongitude());
		double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) + Math.cos(lat1) * Math.cos(lat2) * Math.sin(deltaLng / 2) * Math.sin(deltaLng / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double distanceBetweenAircraft = EARTH_RADIUS * c;
		if(distanceBetweenAircraft < FIVE_MILES_METRES)
			return true;
		
		// No conflict exists
		return false;
	}
	
	private double toRadians(double angle)
	{
		return angle * (Math.PI / 180);
	}
	
	/**
	 * Update aircraft positions, check for conflicts and update the GUI
	 */
	private synchronized void update()
	{
		// Update the time
		double previousTime = time;
		double timeElapsed = 0.1D * getTimeSpeed();
		time += timeElapsed;
		
		// Update the position of all aircraft and execute instructions
		synchronized(aircrafts)
		{
			for (Iterator<Aircraft> it = aircrafts.iterator(); it.hasNext();)
			{
				Aircraft aircraft = it.next();
				aircraft.updatePosition(timeElapsed);
			}
		}
		
		// Check for conflicts after all aircraft have moved
		for(int i = 0; i < aircrafts.size(); i++)
		{
			for(int j = i + 1; j < aircrafts.size(); j++)
			{
				boolean conflicting = isConflicting(aircrafts.get(i), aircrafts.get(j));
				if(conflicting)
				{
					String pair = aircrafts.get(i).callsign + "/" + aircrafts.get(j).callsign;
					if(conflictingHistory.containsKey(pair))
					{
						// Check if this is a new conflict
						if(conflictingHistory.get(pair) > previousTime)
						{
							// This is a new conflict!
							fireConflictListeners(aircrafts.get(i), aircrafts.get(j));
							// Update the time reference
							conflictingHistory.remove(pair);
							conflictingHistory.put(pair, time);
							numberOfConflicts++;
						}
						else
						{
							// Conflict happened last iteration, not a new conflict
							// Update the time reference
							conflictingHistory.remove(pair);
							conflictingHistory.put(pair, time);
							continue;
						}
					}
					else
					{
						// This is a new conflict!
						fireConflictListeners(aircrafts.get(i), aircrafts.get(j));
						conflictingHistory.put(pair, time);
						numberOfConflicts++;
					}
				}
				else
				{
					// Aircraft not conflicting, remove any history of it
					String key1 = aircrafts.get(i).callsign + "/" + aircrafts.get(j).callsign;
					String key2 = aircrafts.get(j).callsign + "/" + aircrafts.get(i).callsign;
					if(conflictingHistory.containsKey(key1))
						conflictingHistory.remove(key1);
					if(conflictingHistory.containsKey(key2))
						conflictingHistory.remove(key2);
				}
			}
		}
		
		// Run repaint/GUI updates on the UI thread
		EventQueue.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				display.repaint();
			}
		});
	}
	
	/**
	 * Thread to continually update aircraft positions
	 */
	private class AircraftUpdateRunnable implements Runnable
	{

		public boolean running = true;
		
		@Override
		public void run()
		{
			while(running)
			{
				update();
				try
				{
					Thread.sleep(100);
				} catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

}
