package uk.ac.nottingham.psyja2.ATCAutomation;

import java.awt.EventQueue;

import uk.ac.nottingham.psyja2.ATCSimulator.Simulator;

public class Main
{
	static Display display;
	
	public static void main(String[] args)
	{
	
		float simSpeed = 0.0f;
		if(args.length > 0)
		{
			// Read scenario file
			Simulator.SCENARIO_FILE = args[0];
			
			if(args.length > 1)
			{
				// Read params
				if(args.length >= 6)
				{
					switch(args[1])
					{
					case "manhattan": Preferences.HEURISTIC = Preferences.MANHATTAN; break;
					case "straight": Preferences.HEURISTIC = Preferences.STRAIGHT_LINE; break;
					case "true": Preferences.HEURISTIC = Preferences.TRUE_DISTANCE; break;
					}
					
					switch(args[2])
					{
					case "agitation": Preferences.PRIORITY_SYSTEM = Preferences.AGITATION; break;
					case "fifo": Preferences.PRIORITY_SYSTEM = Preferences.FIFO; break;
					case "furthest": Preferences.PRIORITY_SYSTEM = Preferences.FURTHEST_DESTINATION; break;
					case "closest": Preferences.PRIORITY_SYSTEM = Preferences.CLOSEST_DESTINATION; break;
					}
					
					Preferences.INTERMEDIATE_SPACING = Integer.valueOf(args[3]);
					
					Preferences.RESERVATION_TIME = Integer.valueOf(args[4]);
					
					Preferences.WINDOW_SIZE = Integer.valueOf(args[5]);

					
					if(args.length >= 9)
					{
						Controller.scheduleArg = args[6];
						Controller.logArg = args[7];
						simSpeed = Float.valueOf(args[8]);
						if(args.length == 10)
						{
							if(args[9].equalsIgnoreCase("-exit"))
								Controller.exitOnFinish = true;
						}
							
					}
					else
					{
						System.out.println("Too few/many arguements!");
						return;
					}
					
				}
				else
				{
					System.out.println("Too few/many arguements!");
					return;
				}
			}
		}
		else
		{
			System.out.println("Please specify a scenario file to load!");
			return;
		}
		
		Preferences.printPreferences();
		
		Simulator.getInstance();
		Simulator.getInstance().setTimeSpeed(simSpeed);

		
		EventQueue.invokeLater(new Runnable() {

			@Override
			public void run()
			{
				display = new Display();
				display.setVisible(true);	
				
			}
			
		});
	
		while(display == null)
		{
			try
			{
				Thread.sleep(1);
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		
		Controller.start(Simulator.SCENARIO_FILE, display.out, display.is);
		
	}
		
}
