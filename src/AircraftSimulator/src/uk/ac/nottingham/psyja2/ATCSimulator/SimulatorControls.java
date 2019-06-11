package uk.ac.nottingham.psyja2.ATCSimulator;

import javax.swing.JPanel;
import java.awt.FlowLayout;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;

import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.BoxLayout;
import java.awt.GridLayout;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import javax.swing.JSlider;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import java.awt.Component;
import javax.swing.border.BevelBorder;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.Box;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.ScrollPaneConstants;

/**
 * The GUI controls for the simulator
 * @author Josh Argent
 *
 */
public class SimulatorControls extends JPanel
{

	private static final long serialVersionUID = 1L;
	private JPanel panel;
	private JLabel lblWind;
	private JLabel label;
	private JSpinner windDirectionSpinner;
	private JLabel lblSpeed;
	private JSpinner windSpeedSpinner;
	private JPanel panel_1;
	private JPanel panel_2;
	private JLabel lblKts;
	private JLabel label_1;
	private JLabel lblSimulation;
	private JLabel lblSpeed_1;
	private JLabel speedSliderLabel;
	private JSlider speedSlider;
	private JPanel panel_5;
	private JLabel lblStatistics;
	private JPanel panel_3;
	private JLabel lblNumberOfAircraft;
	private JPanel panel_4;
	private JLabel flowRateLabel;
	private JPanel panel_6;
	private JLabel label_2;
	private JLabel numberOfAircraftLabel;
	private JPanel panel_7;
	private JLabel lblNumberOfConflicts;
	private JLabel conflictsLabel;
	private JPanel panel_8;
	private JLabel lblNumberOfCrms;
	private JLabel CRMLabel;
	private JLabel lblAircraft;
	private String aircraftListCache[];
	private JList<String> aircraftList;
	private JPanel panel_9;
	private JPanel panel_10;
	private JPanel panel_11;
	private JPanel panel_12;
	private JPanel panel_13;
	private JLabel lblInstructionLog;
	private JTextArea instructionLogTextarea;
	private JScrollPane scrollPane;
	private Component verticalStrut;
	private Component verticalStrut_1;
	private Component verticalStrut_2;
	private Component verticalStrut_3;
	
	private boolean isUpdating = false;

	/**
	 * Create the panel.
	 */
	protected SimulatorControls()
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e)
		{
			e.printStackTrace();
		}
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		this.label = new JLabel("Wind");
		this.label.setAlignmentX(Component.CENTER_ALIGNMENT);
		this.label.setHorizontalAlignment(SwingConstants.CENTER);
		this.label.setFont(new Font("Tahoma", Font.BOLD, 12));
		add(this.label);
		
		this.panel = new JPanel();
		this.panel.setLayout(new GridLayout(2, 2, 10, 5));
		
		this.lblWind = new JLabel(" Direction:");
		this.panel.add(this.lblWind);
		this.lblWind.setFont(new Font("Tahoma", Font.PLAIN, 12));
		
		this.panel_1 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) this.panel_1.getLayout();
		flowLayout.setVgap(0);
		flowLayout.setAlignment(FlowLayout.LEFT);
		this.panel.add(this.panel_1);
		
		this.windDirectionSpinner = new JSpinner();
	
		this.windDirectionSpinner.setModel(new SpinnerNumberModel(Simulator.getInstance().getWindDirection(), 0, 359, 1));
		this.windDirectionSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e)
			{
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run()
					{
						// Update the simulator wind direction with the selected value
						int value = (Integer)windDirectionSpinner.getValue();
						if(value >= 0 && value < 360)
							Simulator.getInstance().setWindDirection(value);		
					}
				});
			}
		});
		this.panel_1.add(this.windDirectionSpinner);
		this.windDirectionSpinner.setFont(new Font("Tahoma", Font.PLAIN, 12));
		
		this.label_1 = new JLabel("\u00B0");
		this.label_1.setFont(new Font("Tahoma", Font.PLAIN, 12));
		this.panel_1.add(this.label_1);
		
		this.lblSpeed = new JLabel(" Speed:");
		this.panel.add(this.lblSpeed);
		this.lblSpeed.setFont(new Font("Tahoma", Font.PLAIN, 12));
		
		this.panel_2 = new JPanel();
		FlowLayout flowLayout_1 = (FlowLayout) this.panel_2.getLayout();
		flowLayout_1.setVgap(0);
		flowLayout_1.setAlignment(FlowLayout.LEFT);
		this.panel.add(this.panel_2);
		
		this.windSpeedSpinner = new JSpinner();
		this.windSpeedSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e)
			{
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run()
					{
						// Update the simulator wind speed with the selected value
						int value = (Integer)windSpeedSpinner.getValue();
						if(value >= 0 && value < 100) // Limit speed to 100kt
							Simulator.getInstance().setWindSpeed(value);			
					}
				});
			}
		});
		this.panel_2.add(this.windSpeedSpinner);
		this.windSpeedSpinner.setModel(new SpinnerNumberModel(Simulator.getInstance().getWindSpeed(), 0, 100, 1));
		this.windSpeedSpinner.setFont(new Font("Tahoma", Font.PLAIN, 12));
		
		this.lblKts = new JLabel("kts");
		this.lblKts.setFont(new Font("Tahoma", Font.PLAIN, 12));
		this.panel_2.add(this.lblKts);
		add(this.panel);
		
		this.verticalStrut = Box.createVerticalStrut(5);
		add(this.verticalStrut);
		
		this.panel_10 = new JPanel();
		this.panel_10.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		FlowLayout flowLayout_6 = (FlowLayout) this.panel_10.getLayout();
		flowLayout_6.setVgap(0);
		add(this.panel_10);
		
		this.lblSimulation = new JLabel("Simulation");
		this.lblSimulation.setAlignmentX(Component.CENTER_ALIGNMENT);
		this.lblSimulation.setHorizontalAlignment(SwingConstants.CENTER);
		this.lblSimulation.setFont(new Font("Tahoma", Font.BOLD, 12));
		add(this.lblSimulation);
		
		this.panel_5 = new JPanel();
		FlowLayout flowLayout_10 = (FlowLayout) this.panel_5.getLayout();
		flowLayout_10.setAlignment(FlowLayout.LEFT);
		add(this.panel_5);
		
		this.lblSpeed_1 = new JLabel("Speed:");
		this.panel_5.add(this.lblSpeed_1);
		this.lblSpeed_1.setVerticalAlignment(SwingConstants.BOTTOM);
		this.lblSpeed_1.setFont(new Font("Tahoma", Font.PLAIN, 12));
		
		this.speedSlider = new JSlider();
		this.speedSlider.setPaintTicks(true);
		this.speedSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e)
			{
				if(isUpdating)
					return;
				
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run()
					{
						// Update the simulator speed with the selected value
						int value = speedSlider.getValue();
						if(value >= 0 && value <= 32)
							Simulator.getInstance().setTimeSpeed((float) value);	
						
						// Update the label
						speedSliderLabel.setText("x" + value);											
					}
				});
			}
		});
		this.speedSlider.setMinorTickSpacing(1);
		this.speedSlider.setMajorTickSpacing(5);
		this.panel_5.add(this.speedSlider);
		this.speedSlider.setMaximum(10);
		this.speedSlider.setValue(1);
		
		this.speedSliderLabel = new JLabel("x2");
		this.speedSliderLabel.setPreferredSize(new Dimension(this.speedSliderLabel.getPreferredSize().width + 20, this.speedSliderLabel.getPreferredSize().height));
		this.speedSliderLabel.setLabelFor(this.speedSlider);
		this.panel_5.add(this.speedSliderLabel);
		this.speedSliderLabel.setFont(new Font("Tahoma", Font.PLAIN, 12));
		
		this.verticalStrut_1 = Box.createVerticalStrut(5);
		add(this.verticalStrut_1);
		
		this.panel_11 = new JPanel();
		FlowLayout flowLayout_7 = (FlowLayout) this.panel_11.getLayout();
		flowLayout_7.setVgap(0);
		this.panel_11.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		add(this.panel_11);
		
		this.lblStatistics = new JLabel("Statistics");
		this.lblStatistics.setAlignmentX(Component.CENTER_ALIGNMENT);
		this.lblStatistics.setHorizontalAlignment(SwingConstants.CENTER);
		this.lblStatistics.setFont(new Font("Tahoma", Font.BOLD, 12));
		add(this.lblStatistics);
		
		this.panel_3 = new JPanel();
		add(this.panel_3);
		this.panel_3.setLayout(new GridLayout(4, 1, 0, 0));
		
		this.panel_6 = new JPanel();
		FlowLayout flowLayout_5 = (FlowLayout) this.panel_6.getLayout();
		flowLayout_5.setAlignment(FlowLayout.LEFT);
		this.panel_3.add(this.panel_6);
		
		this.label_2 = new JLabel("Number of aircraft:");
		this.label_2.setVerticalAlignment(SwingConstants.BOTTOM);
		this.label_2.setFont(new Font("Tahoma", Font.PLAIN, 12));
		this.panel_6.add(this.label_2);
		
		this.numberOfAircraftLabel = new JLabel("4");
		this.numberOfAircraftLabel.setVerticalAlignment(SwingConstants.BOTTOM);
		this.numberOfAircraftLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		this.numberOfAircraftLabel.setFont(new Font("Tahoma", Font.PLAIN, 12));
		this.panel_6.add(this.numberOfAircraftLabel);
		
		this.panel_4 = new JPanel();
		FlowLayout flowLayout_2 = (FlowLayout) this.panel_4.getLayout();
		flowLayout_2.setAlignment(FlowLayout.LEFT);
		this.panel_3.add(this.panel_4);
		
		this.lblNumberOfAircraft = new JLabel("Flow rate (per hour):");
		this.panel_4.add(this.lblNumberOfAircraft);
		this.lblNumberOfAircraft.setVerticalAlignment(SwingConstants.BOTTOM);
		this.lblNumberOfAircraft.setFont(new Font("Tahoma", Font.PLAIN, 12));
		
		this.flowRateLabel = new JLabel("4");
		this.flowRateLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		this.flowRateLabel.setVerticalAlignment(SwingConstants.BOTTOM);
		this.flowRateLabel.setFont(new Font("Tahoma", Font.PLAIN, 12));
		this.panel_4.add(this.flowRateLabel);
		
		this.panel_7 = new JPanel();
		FlowLayout flowLayout_4 = (FlowLayout) this.panel_7.getLayout();
		flowLayout_4.setAlignment(FlowLayout.LEFT);
		this.panel_3.add(this.panel_7);
		
		this.lblNumberOfConflicts = new JLabel("Number of conflicts:");
		this.lblNumberOfConflicts.setVerticalAlignment(SwingConstants.BOTTOM);
		this.lblNumberOfConflicts.setFont(new Font("Tahoma", Font.PLAIN, 12));
		this.panel_7.add(this.lblNumberOfConflicts);
		
		this.conflictsLabel = new JLabel("4");
		this.conflictsLabel.setVerticalAlignment(SwingConstants.BOTTOM);
		this.conflictsLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		this.conflictsLabel.setFont(new Font("Tahoma", Font.PLAIN, 12));
		this.panel_7.add(this.conflictsLabel);
		
		this.panel_8 = new JPanel();
		FlowLayout flowLayout_3 = (FlowLayout) this.panel_8.getLayout();
		flowLayout_3.setAlignment(FlowLayout.LEFT);
		this.panel_3.add(this.panel_8);
		
		this.lblNumberOfCrms = new JLabel("Number of CRMs:");
		this.lblNumberOfCrms.setVerticalAlignment(SwingConstants.BOTTOM);
		this.lblNumberOfCrms.setFont(new Font("Tahoma", Font.PLAIN, 12));
		this.panel_8.add(this.lblNumberOfCrms);
		
		this.CRMLabel = new JLabel("4");
		this.CRMLabel.setVerticalAlignment(SwingConstants.BOTTOM);
		this.CRMLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		this.CRMLabel.setFont(new Font("Tahoma", Font.PLAIN, 12));
		this.panel_8.add(this.CRMLabel);
		
		this.verticalStrut_2 = Box.createVerticalStrut(5);
		add(this.verticalStrut_2);
		
		this.panel_12 = new JPanel();
		FlowLayout flowLayout_8 = (FlowLayout) this.panel_12.getLayout();
		flowLayout_8.setVgap(0);
		this.panel_12.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		add(this.panel_12);
		
		this.lblAircraft = new JLabel("Aircraft");
		this.lblAircraft.setAlignmentX(Component.CENTER_ALIGNMENT);
		this.lblAircraft.setHorizontalAlignment(SwingConstants.CENTER);
		this.lblAircraft.setFont(new Font("Tahoma", Font.BOLD, 12));
		add(this.lblAircraft);
		
		this.panel_9 = new JPanel();
		add(this.panel_9);
		this.panel_9.setLayout(new BorderLayout(0, 0));
		
		this.aircraftList = new JList<String>();
		this.panel_9.add(this.aircraftList, BorderLayout.CENTER);
		this.aircraftList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.aircraftList.setFont(new Font("Tahoma", Font.PLAIN, 12));
		
		this.verticalStrut_3 = Box.createVerticalStrut(5);
		add(this.verticalStrut_3);
		
		this.panel_13 = new JPanel();
		add(this.panel_13);
		FlowLayout flowLayout_9 = (FlowLayout) this.panel_13.getLayout();
		flowLayout_9.setVgap(0);
		this.panel_13.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		
		this.lblInstructionLog = new JLabel("Instruction Log");
		this.lblInstructionLog.setHorizontalAlignment(SwingConstants.CENTER);
		this.lblInstructionLog.setFont(new Font("Tahoma", Font.BOLD, 12));
		this.lblInstructionLog.setAlignmentX(0.5f);
		add(this.lblInstructionLog);

		this.instructionLogTextarea = new JTextArea();
		this.instructionLogTextarea.setFont(new Font("Monospaced", Font.PLAIN, 12));
		this.instructionLogTextarea.setEditable(false);
		
		this.scrollPane = new JScrollPane(this.instructionLogTextarea);
		this.scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		this.scrollPane.setPreferredSize(new Dimension(this.scrollPane.getPreferredSize().width, 200));
		
		add(this.scrollPane);
		
		updateControlValues();
	}
	
	protected void updateControlValues()
	{
		isUpdating = true;
		
		// Set the wind control values
		this.windDirectionSpinner.setValue(Simulator.getInstance().getWindDirection());
		this.windSpeedSpinner.setValue(Simulator.getInstance().getWindSpeed());
		
		// Set the simulation speed slider
		this.speedSlider.setValue((int)Simulator.getInstance().getTimeSpeed());
		
		// Update the statistics
		this.numberOfAircraftLabel.setText(String.valueOf(Simulator.getInstance().getNumberOfAircraft()));
		this.conflictsLabel.setText(String.valueOf(Simulator.getInstance().getNumberOfConflicts()));
		if(Simulator.getInstance().getFlowRate() == -1)
			this.flowRateLabel.setText("Not available");
		else
			this.flowRateLabel.setText(String.valueOf(Simulator.getInstance().getFlowRate()));
		this.CRMLabel.setText(String.valueOf(Simulator.getInstance().getNumberOfInstructions()));
		
		// Update the instruction log
		instructionLogTextarea.setText(Simulator.getInstance().getInstructionLog());
		
		// Update the aircraft list
		String aircrafts[] = new String[Simulator.getInstance().getNumberOfAircraft()];
		int i = 0;
		for(Aircraft aircraft : Simulator.getInstance().getAllAircraft())
		{
			if(aircrafts.length > i)
			{
				aircrafts[i] = aircraft.getCallsign() + " (" + aircraft.profile.type + ")";
				i++;
			}
		}
		// Keep a cache of the array to prevent it continually updating the aircraft list
		// Will only update if there has been a change
		if(aircraftListCache == null || !areArraysTheSame(aircrafts, aircraftListCache))
		{
			this.aircraftList.setListData(aircrafts);
			aircraftListCache = aircrafts;
		}
		
		// Redraw the UI
		this.revalidate();	
		this.repaint();
		
		isUpdating = false;
	}
	
	/*
	 * Will compare two arrays
	 * If they are exactly the same (same item, same order) will return true
	 */
	private static <T> boolean areArraysTheSame(T[] array1, T[] array2)
	{
		if(array1.length != array2.length)
			return false;
		for(int i = 0; i < array1.length; i++)
		{
			if(!array1[i].equals(array2[i]))
				return false;
		}
		return true;
	}
	
}
