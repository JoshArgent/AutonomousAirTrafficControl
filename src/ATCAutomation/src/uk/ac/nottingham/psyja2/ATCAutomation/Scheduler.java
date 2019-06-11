package uk.ac.nottingham.psyja2.ATCAutomation;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import uk.ac.nottingham.psyja2.ATCSimulator.Aircraft;
import uk.ac.nottingham.psyja2.ATCSimulator.AircraftProfile;
import uk.ac.nottingham.psyja2.ATCSimulator.Coordinate;
import uk.ac.nottingham.psyja2.ATCSimulator.Simulator;

public class Scheduler
{
	
	
	private Thread schedulerThread;
	private SchedulerRunnable schedulerRunnable;

	public Scheduler()
	{		
		// Start the scheduler thread
		schedulerRunnable = new SchedulerRunnable();
		schedulerThread = new Thread(schedulerRunnable);
		schedulerThread.setName("Scheduler");
		schedulerThread.start();
	}
	
	/**
	 * Schedule an aircraft to be added and routed at a particular time
	 * @param agent the aircraft agent to add
	 * @param time the time at which to add it
	 */
	public synchronized void scheduleAircraft(AircraftAgent agent, double time)
	{
		schedulerRunnable.schedule.put(agent, time);
	}
	
	public synchronized void clearSchedule()
	{
		schedulerRunnable.schedule.clear();
	}
	
	public synchronized void loadSchedule(File file) throws Exception
	{
		// Check the file exists
		if(!file.exists())
		{
			throw new Exception("File not found!");
		}
		
		// Parse the JSON file
		String rawJSON = new String(Files.readAllBytes(file.toPath()));
		JSONParser parser = new JSONParser();
		JSONArray scheduleArray = (JSONArray) parser.parse(rawJSON);
		for(int i = 0; i < scheduleArray.size(); i++)
		{
			// Read values from JSON file
			JSONObject item = (JSONObject) scheduleArray.get(i);
			String callsign = (String) item.get("callsign");
			String type = (String) item.get("type");
			long entryTime = ((long) item.get("entry_time")) * 60;
			//long expectedExitTime = ((long) item.get("expected_exit_time")) * 60;
			String entry = (String) item.get("entry");
			String exit = (String) item.get("exit");
			long entryAltitude = (long) item.get("entry_altitude");
			long exitAltitude = (long) item.get("exit_altitude");
			
			// Check aircraft type is valid
			AircraftProfile aircraftType = Controller.getAircraftProfile(type);
			if(aircraftType == null)
			{
				Controller.out.println("ERROR: Invalid aircraft type '" + type + "'!");
				continue;
			}
			
			// Check the entry/exit altitudes are OK for this type of aircraft
			if(entryAltitude > aircraftType.maxAltitude)
			{
				entryAltitude = aircraftType.maxAltitude;
			}
			if(exitAltitude > aircraftType.maxAltitude)
			{
				exitAltitude = aircraftType.maxAltitude;
			}
			
			// Check that start and goal waypoint/altitude exists (matches to closest altitude)
			int startFL = (int) (entryAltitude / 100);
			int goalFL = (int) (exitAltitude / 100);
			entry += "_FL" + startFL;
			exit += "_FL" + goalFL;
			SpatialNode startNode = null;
			SpatialNode goalNode = null;
			
			boolean entryIncrementUp = true;
			while(!Controller.nodes.containsKey(entry))
			{
				if(startFL > 460)
					entryIncrementUp = false;
				if(startFL <= 0)
					break;
				if(entryIncrementUp)
					startFL += 10;
				else
					startFL -= 10;
				entry = item.get("entry") + "_FL" + startFL;
			}
			if(Controller.nodes.containsKey(entry))
			{
				startNode = Controller.nodes.get(entry);
			}
			else
			{
				Controller.out.println("ERROR: The waypoint '" + entry + "' does not exist!");
				continue;
			}
			
			boolean exitIncrementUp = true;
			while(!Controller.nodes.containsKey(exit))
			{
				if(goalFL > 460)
					exitIncrementUp = false;
				if(goalFL <= 0)
					break;
				if(exitIncrementUp)
					goalFL += 10;
				else
					goalFL -= 10;
				exit = item.get("exit") + "_FL" + goalFL;
			}
			if(Controller.nodes.containsKey(exit))
			{
				goalNode = Controller.nodes.get(exit);
			}
			else
			{
				Controller.out.println("ERROR: The waypoint '" + exit + "' does not exist!");
				continue;
			}
			
			// Create Aircraft and Agent objects
			Coordinate startLocation = new Coordinate(startNode.getLocation().getLatitude(), startNode.getLocation().getLongitude());
			double initialHeading = GraphBuilder.calculateBearing(startNode.getLocation().getLatitude(), startNode.getLocation().getLongitude(), 
					goalNode.getLocation().getLatitude(), goalNode.getLocation().getLongitude());
			Aircraft aircraft = new Aircraft(callsign, startLocation, initialHeading, Aircraft.MAX_CRUISE_SPEED, startFL * 100, aircraftType);
			AircraftAgent agent = new AircraftAgent(aircraft, startNode, goalNode);
			
			// Add it to the schedule
			schedulerRunnable.schedule.put(agent, (double) entryTime);
		}
		
	}
	
	@Override
	public String toString()
	{
		String scheduleStr = "";
		for(AircraftAgent agent : schedulerRunnable.schedule.keySet())
		{
			scheduleStr += String.format("%s: %.2f\n", agent.getAircraft().getCallsign(), schedulerRunnable.schedule.get(agent));
		}
		return scheduleStr;
	}
	
	private synchronized void addAircraft(AircraftAgent agent)
	{		
		// Add the agent
		Controller.agentManager.addAgent(agent);
		Controller.out.println("SCHEDULER: Added '" + agent.getAircraft().getCallsign() + "' to the scenario.");
	}
	
	private class SchedulerRunnable implements Runnable
	{

		public volatile Map<AircraftAgent, Double> schedule = new HashMap<>();
		public boolean running = true;
		private boolean scheduleSet = false;
		
		@Override
		public void run()
		{
			while(running)
			{
				// Check if an aircraft should be added
				ArrayList<AircraftAgent> agents = new ArrayList<AircraftAgent>();
				
				for(AircraftAgent agent : schedule.keySet())
				{
					scheduleSet = true;
					if(schedule.get(agent) <= Simulator.getInstance().getTime())
					{
						agents.add(agent);
					}
				}
				
				// Add the identified aircraft
				for(AircraftAgent agent : agents)
				{
					// Remove from the schedule
					schedule.remove(agent);
					
					// Add the aircraft
					addAircraft(agent);
				}
				
				if(agents.size() > 0)
				{
					// Recalculate paths
					Controller.agentManager.recalculatePaths();
				}
				
				if(scheduleSet && schedule.isEmpty() && agents.size() == 0 && Simulator.getInstance().getNumberOfAircraft() == 0)
				{
					// If the exit on finish flag is set, exit the program
					if(Controller.exitOnFinish)
					{
						Controller.logger.stopLogger();
						System.exit(0);
						
					}
				}
				
				// Pause
				try
				{
					Thread.sleep(1000); // 1 second
				} catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
} 
