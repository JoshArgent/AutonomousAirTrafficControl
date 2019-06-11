package uk.ac.nottingham.psyja2.ATCAutomation;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.TimeZone;

import uk.ac.nottingham.psyja2.ATCAutomation.PathFinding.AStar;
import uk.ac.nottingham.psyja2.ATCAutomation.PathFinding.Agent;
import uk.ac.nottingham.psyja2.ATCAutomation.PathFinding.AgentManager;
import uk.ac.nottingham.psyja2.ATCSimulator.Aircraft;
import uk.ac.nottingham.psyja2.ATCSimulator.AircraftProfile;
import uk.ac.nottingham.psyja2.ATCSimulator.Coordinate;
import uk.ac.nottingham.psyja2.ATCSimulator.Simulator;

public abstract class Controller
{

	protected static Output out;
	protected static Input in;
	public static String logArg = "";
	public static String scheduleArg = "";
	private static String scenarioFile;
	
	protected static SpatialWHCAStar graph;
	protected static Map<String, SpatialNode> nodes;
	protected static AgentManager agentManager;
	protected static Scheduler scheduler;
	protected static Logger logger;
	public static boolean exitOnFinish = false;
	
	private static void loadScenario()
	{
		out.println("Loading the scenario file...");
		Simulator.SCENARIO_FILE = scenarioFile;
		Simulator.getInstance();
		
		out.println("Building graph data structure...");
		graph = new SpatialWHCAStar(); 
		graph.TIMEOUT = 5 * 1000L; // 5 second timeout
		nodes = GraphBuilder.buildGraph(Simulator.getInstance().getScenario(), graph);
		out.println("Graph complete. (" + graph.getCardinality() + " nodes, " + graph.getNumberOfEdges() + " edges)");
		
		/* Build FW graph */
		out.println("Building Floyd-Warshall matrix...");
		SpatialNode.buildFlatGraph(graph);
		
		out.println("Initialising the agent manager...");
		agentManager = new AgentManager(graph);
		scheduler = new Scheduler();
		
		out.println("Total memory = " + (Runtime.getRuntime().totalMemory() / (1024 * 1024)) + "MB");
		out.println("Loading complete!\n");
		
	}
	
	public static void start(String scenarioFile, Output out, Input in)
	{
		Controller.scenarioFile = scenarioFile;
		Controller.out = out;
		Controller.in = in;
		
		out.println("MAPF Air Traffic Control [version 0.0]");
		out.println("(C) Copyright Joshua Argent.");
		out.println("");
		loadScenario();
		help();
		
		// Check if a log folder and schedule was passed through application args
		if(!logArg.equals(""))
		{
			loadSchedule(scheduleArg);
			logger = new Logger(new File(logArg));
			logger.startLogger();
		}

		while(true)
		{
			String instruction = in.nextLine();
			String parts[] = instruction.split(" ");
			if(parts.length == 0)
			{
				parts = new String[] { instruction };
			}
			
			if(parts[0].equalsIgnoreCase("ADD"))
			{
				/* ADD command */
				if(parts.length == 6)
				{
					String callsignStr = parts[1];
					String type = parts[2];
					String startStr = parts[3];
					String goalStr = parts[4];
					String altitudeStr = parts[5];
					if(!altitudeStr.matches("-?\\d+"))
					{
						out.println("Please specify an integer altitude!");
						continue;
					}
					addAircraft(callsignStr, type, startStr, goalStr, Integer.valueOf(altitudeStr), Integer.valueOf(altitudeStr));
				}
				else
				{
					out.println("Too few/many arguments! See the 'help' command.");
				}
			}
			else if(parts[0].equalsIgnoreCase("REMOVE"))
			{
				/* REMOVE command */
				if(parts.length == 2)
				{
					if(parts[1].equalsIgnoreCase("-all"))
					{
						removeAllAircraft();
					}
					else
					{
						removeAircraft(parts[1]);
					}
				}
				else
				{
					out.println("Too few/many arguments! See the 'help' command.");
				}
			}
			else if(parts[0].equalsIgnoreCase("SCHEDULE"))
			{
				if(parts.length > 1)
				{
					if(parts[1].equalsIgnoreCase("-load"))
					{
						if(parts.length > 2)
						{
							// Get the filename, allow use of " quote " marks
							String filename = "";
							if(!parts[2].startsWith("\""))
							{
								filename = parts[2];
							}
							else
							{
								for(int i = 2; i < parts.length; i++)
								{
									filename += parts[i].replace("\"", "");
									if(parts[i].endsWith("\""))
										break;
								}
							}
							// Load the schedule
							loadSchedule(filename);
						}
						else
						{
							out.println("Too few/many arguments! See the 'help' command.");
						}
					}
					else if(parts[1].equalsIgnoreCase("-clear"))
					{
						scheduler.clearSchedule();
					}
					else if(parts[1].equalsIgnoreCase("-print"))
					{
						printSchedule();
					}
					else
					{
						out.println("Invalid command! See the 'help' command.");
					}
				}
				else
				{
					out.println("Too few/many arguments! See the 'help' command.");
				}
			}
			else if(parts[0].equalsIgnoreCase("LOGGING"))
			{
				if(parts.length == 3 || parts.length == 2)
				{
					if(parts[1].equalsIgnoreCase("-on"))
					{
						if(parts.length == 3)
						{
							if(logger == null)
							{
								logger = new Logger(new File(parts[2]));
							}
							else
							{
								logger.setOutputDirectory(new File(parts[2]));
							}
							logger.startLogger();
							out.println("Logger started!");
						}
						else
						{
							out.println("Invalid command! See the 'help' command.");
						}
					}
					else if(parts[1].equalsIgnoreCase("-off"))
					{
						if(logger != null)
						{
							logger.stopLogger();
						}
						out.println("Logger stopped!");
					}
					else
					{
						out.println("Invalid command! See the 'help' command.");
					}
				}
				else
				{
					out.println("Too few/many arguments! See the 'help' command.");
				}
			}
			else if(parts[0].equalsIgnoreCase("TIME"))
			{
				SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
				formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
			    String time = formatter.format(Simulator.getInstance().getTime() * 1000);
			    out.println("Current simulator time = " + time);
			}
			else if(parts[0].equalsIgnoreCase("STATS"))
			{
				out.println("Number of nodes explored by A* = " + AStar.numberOfNodesExplored);
			}
			else if(parts[0].equalsIgnoreCase("HELP"))
			{
				help();
			}
			else
			{
				out.println("Invalid command! See the 'help' command.");
			}				

		}
		
	}
	
	public static void help()
	{
		out.println("The following commands are available:");
		out.println("");
		out.println("	ADD callsign type start_waypoint goal_waypoint altitude");
		out.println("	REMOVE [callsign | -all]");
		out.println("	SCHEDULE [-load filename | -clear | -print]");
		out.println("	LOGGING [-on output_directory | -off]");
		out.println("	TIME");
		out.println("	STATS");
		out.println("	HELP");
		out.println("");
	}
	
	public static void addAircraft(String callsign, String type, String start, String goal, int startAltitude, int goalAltitude)
	{
		// Check callsign doesn't exist
		if(Simulator.getInstance().getAircraft(callsign) != null)
		{
			out.println("ERROR: An aircraft with the callsign '" + callsign + "' already exists!");
			return;
		}
		
		// Check aircraft type is valid
		AircraftProfile aircraftType = getAircraftProfile(type);
		if(aircraftType == null)
		{
			out.println("ERROR: Invalid aircraft type '" + type + "'!");
			return;
		}
		
		// Check that start and goal waypoint/altitude exists
		int startFL = startAltitude / 100;
		int goalFL = startAltitude / 100;
		start += "_FL" + startFL;
		goal += "_FL" + goalFL;
		SpatialNode startNode = null;
		SpatialNode goalNode = null;
		if(nodes.containsKey(start))
		{
			startNode = nodes.get(start);
		}
		else
		{
			out.println("ERROR: The waypoint '" + start + "' does not exist!");
			return;
		}
		if(nodes.containsKey(goal))
		{
			goalNode = nodes.get(goal);
		}
		else
		{
			out.println("ERROR: The waypoint '" + goal + "' does not exist!");
			return;
		}

		// Add the aircraft		
		Coordinate startLocation = new Coordinate(startNode.getLocation().getLatitude(), startNode.getLocation().getLongitude());
		double initialHeading = GraphBuilder.calculateBearing(startNode.getLocation().getLatitude(), startNode.getLocation().getLongitude(), 
				goalNode.getLocation().getLatitude(), goalNode.getLocation().getLongitude());
		Aircraft aircraft = new Aircraft(callsign, startLocation, initialHeading, Aircraft.MAX_CRUISE_SPEED, startAltitude, aircraftType);
		AircraftAgent agent = new AircraftAgent(aircraft, startNode, goalNode);
		agentManager.addAgent(agent);	
		agentManager.recalculatePaths();
		out.println("Added '" + callsign + "' to the scenario!");
	}
	
	public static AircraftProfile getAircraftProfile(String type)
	{
		AircraftProfile aircraftType = null;
		switch (type)
		{
			case "B738": aircraftType = AircraftProfile.B738; break;
			case "B737": aircraftType = AircraftProfile.B737; break;
			case "B733": aircraftType = AircraftProfile.B733; break;
			case "B752": aircraftType = AircraftProfile.B752; break;
			case "B753": aircraftType = AircraftProfile.B753; break;
			case "B762": aircraftType = AircraftProfile.B762; break;
			case "B744": aircraftType = AircraftProfile.B744; break;
			case "B772": aircraftType = AircraftProfile.B772; break;
			case "B773": aircraftType = AircraftProfile.B773; break;
			case "B788": aircraftType = AircraftProfile.B788; break;
			case "DH8D": aircraftType = AircraftProfile.DH8D; break;
			case "A319": aircraftType = AircraftProfile.A319; break;
			case "A320": aircraftType = AircraftProfile.A320; break;
			case "A321": aircraftType = AircraftProfile.A321; break;
			case "A332": aircraftType = AircraftProfile.A332; break;
			case "A333": aircraftType = AircraftProfile.A333; break;
			case "A342": aircraftType = AircraftProfile.A342; break;
			case "A388": aircraftType = AircraftProfile.A388; break;
		}	
		return aircraftType;
	}
	
	public static void removeAircraft(String callsign)
	{
		// Check callsign exists
		if(Simulator.getInstance().getAircraft(callsign) == null)
		{
			out.println("ERROR: An aircraft with the callsign '" + callsign + "' does not exist!");
			return;
		}
		
		// Remove the aircraft from the simulator and agent manager
		Aircraft aircraft = Simulator.getInstance().getAircraft(callsign);
		Simulator.getInstance().removeAircraft(aircraft);
		AircraftAgent agentMatch = null;
		for(Agent agent : agentManager.getAgents())
		{
			if(((AircraftAgent)agent).getAircraft().equals(aircraft))
			{
				agentMatch = (AircraftAgent) agent;
				break;
			}
		}
		agentManager.removeAgent(agentMatch);
		out.println("Successfully removed '" + callsign + "' from the scenario!");
	}
	
	public static void removeAllAircraft()
	{
		int limit = agentManager.getAgents().size();
		for(int i = 0; i < limit; i++)
		{
			Simulator.getInstance().removeAircraft(((AircraftAgent)agentManager.getAgents().get(i)).getAircraft());
		}
		agentManager.removeAllAgents();
		out.println("Successfully removed all aircraft!");
	}
	
	public static void loadSchedule(String filename)
	{
		try
		{
			scheduler.loadSchedule(new File(filename));
		} 
		catch (Exception e)
		{
			out.println("ERROR: " + e.getMessage());
			return;
		}
		out.println("Schedule successfully loaded.");
	}
	
	public static void printSchedule()
	{
		out.println(scheduler.toString());
	}
	
	public interface Input
	{
		/**
		 * @return Reads the next line or waits for input
		 */
		public String nextLine();
	}
	
	public interface Output
	{
		public void println(String str);
		public void print(String str);
	}
	
}
