package uk.ac.nottingham.psyja2.ATCAutomation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TimeZone;

import uk.ac.nottingham.psyja2.ATCAutomation.PathFinding.Agent;
import uk.ac.nottingham.psyja2.ATCAutomation.PathFinding.AgentManager.RecalculatePathsListener;
import uk.ac.nottingham.psyja2.ATCAutomation.PathFinding.Node;
import uk.ac.nottingham.psyja2.ATCSimulator.Aircraft;
import uk.ac.nottingham.psyja2.ATCSimulator.ConflictListener;
import uk.ac.nottingham.psyja2.ATCSimulator.Instruction;
import uk.ac.nottingham.psyja2.ATCSimulator.Simulator;
import uk.ac.nottingham.psyja2.ATCSimulator.Waypoint;

public class Logger implements RecalculatePathsListener, ConflictListener
{
	
	private LoggerRunnable runnable;
	private Thread thread;
	private File outputDirectory;
	private long loggerStartTime;
	private int conflictCount = 0;
	
	public Logger(File outputDirectory)
	{
		setOutputDirectory(outputDirectory);
		Controller.agentManager.addRecalculatePathsListener(this);
		Simulator.getInstance().addConflictListener(this);
	}
	
	public void startLogger()
	{
		conflictCount = 0;
		loggerStartTime = System.currentTimeMillis();
		runnable = new LoggerRunnable();
		thread = new Thread(runnable);		
		thread.setName("Logger");
		thread.start();
	}
	
	public void stopLogger()
	{
		flush();
		runnable.prioritiesOut.close();
		runnable.running = false;
	}
	
	public void setOutputDirectory(File outputDirectory)
	{
		if(!outputDirectory.exists())
			outputDirectory.mkdir();
		this.outputDirectory = outputDirectory;
	}
	
	public void flush()
	{
		runnable.flush = true;
	}
	
	@Override
	public void recalculatePaths(Agent[] agentsSorted)
	{
		if(runnable.running)
		{
			String content = "";
			for(Agent agent : agentsSorted)
			{
				content += ((AircraftAgent)agent).getAircraft().getCallsign() + ", ";
			}
			if(content.length() > 0)
			{
				content = content.substring(0, content.length() - 2);
				content += "\n";
				runnable.priorities += content;
				runnable.flushPriorities = true;
			}			
		}
	}
	
	@Override
	public void conflictEvent(Aircraft aircraft1, Aircraft aircraft2)
	{
		// A conflict occured, create a log file
		if(runnable.running)
		{
			conflictCount++;
			try
			{
				// Create log file
				PrintWriter conflictLog = new PrintWriter(outputDirectory.getAbsolutePath() + "/conflict" + conflictCount + ".log");	
				conflictLog.println("Conflict between " + aircraft1.getCallsign() + " and " + aircraft2.getCallsign());
				
				// Write the time
				SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
				formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
			    String time = formatter.format(Simulator.getInstance().getTime() * 1000);
				conflictLog.println("Time = " + time);
				conflictLog.println();
				
				// Write aircraft + agent details
				writeAircraftConflictDetails(conflictLog, aircraft1);
				writeAircraftConflictDetails(conflictLog, aircraft2);
				
				conflictLog.println("");
				conflictLog.println(Controller.agentManager.graph.getReservationTableString());
				
				conflictLog.flush();
				conflictLog.close();
				
			} catch (FileNotFoundException e1)
			{
				e1.printStackTrace();
			}
		}
	}
	
	private static void writeAircraftConflictDetails(PrintWriter conflictLog, Aircraft aircraft)
	{
		// Aircraft 1 details
		conflictLog.println("----- " + aircraft.getCallsign() + " -----");
		conflictLog.println("Location = " + aircraft.getLocation().getLatitude() + ", " + aircraft.getLocation().getLongitude());
		conflictLog.println("Heading = " + aircraft.getHeading());
		conflictLog.println("Altitude = " + aircraft.getAltitude());
		conflictLog.println("Ground Speed = " + aircraft.getGroundSpeed());
		conflictLog.println("Airborne Time = " + aircraft.getAirborneTime());
		conflictLog.println();
		conflictLog.println("Instructions:");
		for(Instruction i : aircraft.getInstructions())
		{
			conflictLog.println(i);
		}
		conflictLog.println();
		for(Agent _agent : Controller.agentManager.getAgents())
		{
			AircraftAgent agent = (AircraftAgent) _agent;
			if(agent.getAircraft().equals(aircraft))
			{
				conflictLog.println("Current Node = " + agent.getPosition());
				conflictLog.println("Goal Node = " + agent.getGoal());
				conflictLog.println("Agitation = " + agent.getPriority());
				conflictLog.print("Node Queue = {");
				for(Node node : agent.nodeQueue)
				{
					conflictLog.print(node + ", ");
				}
				conflictLog.print("}\n");
				break;
			}
		}
		conflictLog.println();
		conflictLog.println("---------------------------");
		conflictLog.println();
	}
	
	class LoggerRunnable implements Runnable
	{
		
		public boolean running = true;
		public volatile boolean flush = false;
		
		public volatile boolean flushPriorities = false;
		public String priorities = "";
		public PrintWriter prioritiesOut = null;
		
		public HashMap<Aircraft, ArrayList<String>> recording = new HashMap<>();

		@Override
		public void run()
		{
			// Create a priorities file
			try
			{
				prioritiesOut = new PrintWriter(outputDirectory.getAbsolutePath() + "/priorities.log");
			} catch (FileNotFoundException e1)
			{
				e1.printStackTrace();
			}
			
			while(running || flush)
			{
				// Generated a formatted date/time string
				long time = (long) (loggerStartTime + (Simulator.getInstance().getTime() * 1000));
				Date dateObj = new Date(time);
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'.000Z'");
				String dateStr = formatter.format(dateObj);
				
				// Loop through each aircraft
				for(Iterator<Aircraft> it = Simulator.getInstance().getAllAircraft().iterator(); it.hasNext();)
				{
					// Add an XML GPX entry to the list
					Aircraft aircraft = it.next();
					if(!recording.containsKey(aircraft))
					{
						recording.put(aircraft, new ArrayList<>());
					}
					String item = "<trkpt lat=\"" + aircraft.getLocation().getLatitude() + "\" lon=\"" + aircraft.getLocation().getLongitude() + 
							"\"><ele>" + Logger.feetToMetres(aircraft.getAltitude()) + "</ele><time>" +
							dateStr + "</time></trkpt>\n";
					recording.get(aircraft).add(item);
				}		
				
				// See if any aircraft in the recording has been removed - can flush the data to file
				for(Aircraft aircraft : recording.keySet())
				{
					if(Simulator.getInstance().getAircraft(aircraft.getCallsign()) == null)
					{
						flush(aircraft);
					}
				}
				
				// See if the logger flush command was called
				if(flush)
				{
					flush = false;
					flush();
				}
				
				// Flush any priorities to priorities.log
				if(flushPriorities && prioritiesOut != null)
				{
					flushPriorities = false;
					prioritiesOut.write(priorities);
					priorities = "";
					prioritiesOut.flush();
				}
				
				// Pause the thread
				try
				{
					Thread.sleep(1000); // 1 second
				} catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}			
		}
		
		private void flush()
		{
			// Generate the GPX files
			for(Aircraft key : runnable.recording.keySet())
			{
				flush(key);
			}
			
			// Generate a CSV 'point of interest' file
			try
			{
				PrintWriter out = new PrintWriter(outputDirectory.getAbsolutePath() + "/POI.csv");
				for(Waypoint waypoint : Simulator.getInstance().getScenario().waypoints.values())
				{
					String line = waypoint.getLocation().getLatitude() + "," + waypoint.getLocation().getLongitude() + "," + waypoint.getName() + "\n";
					out.write(line);
				}
				out.close();
				
			} catch (FileNotFoundException e)
			{
				e.printStackTrace();
			}
			
		}
		
		private void flush(Aircraft aircraft)
		{
			// Generate the GPX file data
			File gpxFile = new File(outputDirectory.getAbsolutePath() + "/" + aircraft.getCallsign() + ".gpx");
			StringBuilder data = new StringBuilder();
			data.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\" ?>\n<gpx version=\"1.1\" creator=\"EMTAC BTGPS Trine II DataLog Dump 1.0 - http://www.ayeltd.biz\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://www.topografix.com/GPX/1/1\" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\">\n");
			data.append("<metadata><name>" + aircraft.getCallsign() + " Log</name><desc>" + aircraft.getProfile().type + "</desc></metadata>\n");
			data.append("<trk><trkseg>");
			for(String point : runnable.recording.get(aircraft))
			{
				data.append(point);
			}
			data.append("</trkseg></trk>\n</gpx>\n");
			
			// Write data to file
			try
			{
				PrintWriter out = new PrintWriter(gpxFile);
				out.print(data.toString());
				out.close();
			} catch (FileNotFoundException e)
			{
				e.printStackTrace();
			}
		}
				
	}
	
	private static double feetToMetres(double feet)
	{
		return feet * 0.3048D;
	}	
	
}
