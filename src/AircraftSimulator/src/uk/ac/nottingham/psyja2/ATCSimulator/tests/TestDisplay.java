package uk.ac.nottingham.psyja2.ATCSimulator.tests;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.util.Scanner;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

import uk.ac.nottingham.psyja2.ATCSimulator.Aircraft;
import uk.ac.nottingham.psyja2.ATCSimulator.AircraftProfile;
import uk.ac.nottingham.psyja2.ATCSimulator.AltitudeInstruction;
import uk.ac.nottingham.psyja2.ATCSimulator.Coordinate;
import uk.ac.nottingham.psyja2.ATCSimulator.HeadingInstruction;
import uk.ac.nottingham.psyja2.ATCSimulator.HoldInstruction;
import uk.ac.nottingham.psyja2.ATCSimulator.Simulator;
import uk.ac.nottingham.psyja2.ATCSimulator.SimulatorControls;
import uk.ac.nottingham.psyja2.ATCSimulator.SpeedInstruction;
import uk.ac.nottingham.psyja2.ATCSimulator.Waypoint;
import uk.ac.nottingham.psyja2.ATCSimulator.WaypointInstruction;

public class TestDisplay extends JFrame
{

	private static final long serialVersionUID = 1429050201741279205L;
	private JPanel contentPane;
	public JComponent panel;
	public SimulatorControls panel_1;
	private static Scanner sc;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args)
	{		
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				try
				{
					TestDisplay frame = new TestDisplay();
					frame.setVisible(true);
				} catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
		
		System.out.println("-- INSTRUCTIONS --");
		System.out.println("<callsign> turn <heading>");
		System.out.println("<callsign> altitude <altitude>");
		System.out.println("<callsign> hold");
		System.out.println("<callsign> speed <speed>");
		System.out.println("<callsign> direct <waypoint>");
		System.out.println("-------------------\n\n");
		System.out.println("ENTER AN INSTRUCTION:");
		
		
		while(true)
		{
			sc = new Scanner(System.in);
			String instruction = sc.nextLine();
			if(instruction.contains(" "))
			{
				String parts[] = instruction.split(" ");
				if(parts.length >= 2 && parts.length <= 3)
				{
					String callsign = parts[0];		
					Aircraft aircraft = Simulator.getInstance().getAircraft(callsign);
					if(aircraft != null)
					{
						String instr = parts[1];
						if(instr.equalsIgnoreCase("turn"))
						{
							int heading = Integer.valueOf(parts[2]);
							Simulator.getInstance().sendInstruction(new HeadingInstruction(heading), aircraft);
						}
						else if(instr.equalsIgnoreCase("altitude"))
						{
							int altitude = Integer.valueOf(parts[2]);
							Simulator.getInstance().sendInstruction(new AltitudeInstruction(altitude), aircraft);
						}
						else if(instr.equalsIgnoreCase("hold"))
						{
							Simulator.getInstance().sendInstruction(new HoldInstruction(60), aircraft);
						}
						else if(instr.equalsIgnoreCase("speed"))
						{
							int speed = Integer.valueOf(parts[2]);
							Simulator.getInstance().sendInstruction(new SpeedInstruction(speed), aircraft);
						}
						else if(instr.equalsIgnoreCase("direct"))
						{
							Waypoint waypoint = Simulator.getInstance().getScenario().waypoints.get(parts[2]);
							if(waypoint != null)
								Simulator.getInstance().sendInstruction(new WaypointInstruction(waypoint), aircraft);
							else
								System.out.println("Waypoint not found!");
						}
					}
					else
					{
						System.out.println("Aircraft not found!");
					}	
				}
				else
				{
					System.out.println("Invalid instruction.");
				}				
			}
			else
			{
				System.out.println("Invalid instruction.");
			}
		}
	}

	/**
	 * Create the frame.
	 */
	public TestDisplay()
	{
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		this.contentPane = new JPanel();
		this.contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(this.contentPane);
		
		this.panel = Simulator.getInstance().getDisplay();
		this.contentPane.add(this.panel, BorderLayout.CENTER);
		
		this.panel_1 = Simulator.getInstance().getControls();
		this.contentPane.add(this.panel_1, BorderLayout.EAST);
		
		// Add a test aircraft
		Aircraft aircraft1 = new Aircraft("BAW225", new Coordinate(52f, -2f), 265, 200, 27000, AircraftProfile.A321);
		Simulator.getInstance().addAircraft(aircraft1);
		
		Aircraft aircraft2 = new Aircraft("EXS112", new Coordinate("523952N", "0021000W"), 270, 200, 34000, AircraftProfile.B733);
		Simulator.getInstance().addAircraft(aircraft2);
		
		Aircraft aircraft3 = new Aircraft("RYR77D", new Coordinate("523952N", "0021900W"), 90, 220, 34000, AircraftProfile.B738);
		Simulator.getInstance().addAircraft(aircraft3);
		
		Aircraft aircraft4 = new Aircraft("QTR22F", new Coordinate(51.5f, -1.9f), 100, 220, 37000, AircraftProfile.A333);
		Simulator.getInstance().addAircraft(aircraft4);
		
		Aircraft aircraft5 = new Aircraft("EZY31UJ", new Coordinate("525952N", "0031000W"), 190, 220, 34000, AircraftProfile.A319);
		Simulator.getInstance().addAircraft(aircraft5);
		
		Aircraft aircraft6 = new Aircraft("BEE5MH", new Coordinate("526952N", "0024000W"), 180, 220, 21000, AircraftProfile.DH8D);
		Simulator.getInstance().addAircraft(aircraft6);
		
		Aircraft aircraft7 = new Aircraft("TOM7AV", new Coordinate("521852N", "0025500W"), 130, 220, 34000, AircraftProfile.B738);
		Simulator.getInstance().addAircraft(aircraft7);
		
	}

}
