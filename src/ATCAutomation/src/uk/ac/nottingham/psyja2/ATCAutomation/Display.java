package uk.ac.nottingham.psyja2.ATCAutomation;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

import uk.ac.nottingham.psyja2.ATCAutomation.Controller.Input;
import uk.ac.nottingham.psyja2.ATCAutomation.Controller.Output;
import uk.ac.nottingham.psyja2.ATCSimulator.Simulator;
import uk.ac.nottingham.psyja2.ATCSimulator.SimulatorControls;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import java.awt.Color;
import javax.swing.border.BevelBorder;
import javax.swing.text.DefaultCaret;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JScrollPane;
import java.awt.Font;

public class Display extends JFrame
{
	
	private static final long serialVersionUID = 2753265634254229500L;
	private JPanel contentPane;
	public JComponent panel;
	public SimulatorControls panel_1;
	public JPanel panel_2;
	public JTextField textField;
	public JTextArea textArea;
	public CommandInput is;
	public CommandOutput out;
	public JScrollPane scrollPane;

	/**
	 * Create the frame.
	 */
	public Display()
	{
		is = new CommandInput();
		out = new CommandOutput();
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 886, 596);
		this.contentPane = new JPanel();
		this.contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(this.contentPane);
		
		this.panel = Simulator.getInstance().getDisplay();
		this.panel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		this.contentPane.add(this.panel, BorderLayout.CENTER);
		
		this.panel_1 = Simulator.getInstance().getControls();
		this.panel_1.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		this.contentPane.add(this.panel_1, BorderLayout.EAST);
		
		this.panel_2 = new JPanel();
		this.panel_2.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		this.contentPane.add(this.panel_2, BorderLayout.SOUTH);
		this.panel_2.setLayout(new BorderLayout(0, 0));
		
		this.textField = new JTextField();
		this.textField.setFont(new Font("Monospaced", Font.PLAIN, 13));
		this.textField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg) 
			{
				is.addCommand(textField.getText());
				out.println("> " + textField.getText());
				textField.setText("");
			}
		});
		this.textField.setForeground(Color.WHITE);
		this.textField.setBackground(Color.BLACK);
		this.panel_2.add(this.textField, BorderLayout.SOUTH);
		this.textField.setColumns(10);
				
		this.textArea = new JTextArea();
		DefaultCaret caret = (DefaultCaret) textArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		this.textArea.setEditable(false);
		this.textArea.setForeground(Color.WHITE);
		this.textArea.setBackground(Color.BLACK);
		this.textArea.setRows(8);
		this.scrollPane = new JScrollPane(textArea);
		this.panel_2.add(this.scrollPane, BorderLayout.CENTER);
	}
	
	class CommandInput implements Input
	{

		protected String data;
		protected boolean available = false;
		
		protected void addCommand(String message)
		{
			data = message;
			available = true;
		}

		@Override
		public String nextLine()
		{
			while(!available)
			{
				try
				{
					Thread.sleep(1);
				} catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
			available = false;
			return data;
		}
		
	}
	
	class CommandOutput implements Output
	{

		@Override
		public void println(String str)
		{
			print(str + "\n");			
		}

		@Override
		public void print(String str)
		{
			textArea.append(str);
		}
		
	}
	
}
