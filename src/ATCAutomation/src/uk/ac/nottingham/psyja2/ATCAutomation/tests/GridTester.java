package uk.ac.nottingham.psyja2.ATCAutomation.tests;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import uk.ac.nottingham.psyja2.ATCAutomation.PathFinding.Agent;
import uk.ac.nottingham.psyja2.ATCAutomation.PathFinding.AgentManager;
import uk.ac.nottingham.psyja2.ATCAutomation.PathFinding.Node;
import uk.ac.nottingham.psyja2.ATCAutomation.PathFinding.Path;
import uk.ac.nottingham.psyja2.ATCAutomation.PathFinding.WHCAStar;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import java.awt.TextArea;
import javax.swing.JLabel;
import javax.swing.JTextField;

public class GridTester extends JFrame
{

	private static final long serialVersionUID = -7795162542290112186L;
	private JPanel contentPane;
	public JPanel panel;
	public JPanel panel_1;
	public TextArea textArea;
	public JLabel lblTo;
	public TextArea textArea_1;
	public JPanel panel_2;
	public JPanel panel_3;
	public JButton btnPlay;
	public JTextField textField;
	public JLabel lblWindowSize;
	public JButton btnStep;
	WHCAStar graph;
	GridNode gridNodes[][];
	AgentManager manager;
	public JButton btnReset;
	private Color colours[] = new Color[] { Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.PINK, Color.CYAN, Color.ORANGE, Color.GRAY };
	private HashMap<GridAgent, Color> agents = new HashMap<>();
	
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
					GridTester frame = new GridTester();
					frame.setVisible(true);
				} catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public GridTester()
	{
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 529, 467);
		this.contentPane = new JPanel();
		this.contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(this.contentPane);
		this.contentPane.setLayout(new BorderLayout(0, 0));
		
		this.panel = new JPanel();
		this.contentPane.add(this.panel);
		this.panel.setLayout(new GridLayout(10, 10, 1, 1));
		
		this.panel_1 = new JPanel();
		this.contentPane.add(this.panel_1, BorderLayout.SOUTH);
		this.panel_1.setLayout(new BorderLayout(0, 0));
		
		this.panel_2 = new JPanel();
		this.panel_1.add(this.panel_2, BorderLayout.CENTER);
		
		this.textArea = new TextArea(5, 30);
		this.panel_2.add(this.textArea);
		
		this.lblTo = new JLabel("to..");
		this.panel_2.add(this.lblTo);
		
		this.textArea_1 = new TextArea(5, 30);
		this.panel_2.add(this.textArea_1);
		
		this.panel_3 = new JPanel();
		this.panel_1.add(this.panel_3, BorderLayout.SOUTH);
		
		this.lblWindowSize = new JLabel("Window Size:");
		this.panel_3.add(this.lblWindowSize);
		
		this.textField = new JTextField();
		this.panel_3.add(this.textField);
		this.textField.setColumns(10);
		
		this.btnPlay = new JButton("Build Graph");
		this.btnPlay.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				build();
			}
		});
		
		this.btnStep = new JButton("Step");
		this.btnStep.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				play();
			}
		});
		this.panel_3.add(this.btnStep);
		this.panel_3.add(this.btnPlay);
		
		this.btnReset = new JButton("Reset");
		this.btnReset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				graph = null;
				gridNodes = null;
				agents.clear();
				drawAgents();
			}
		});
		this.panel_3.add(this.btnReset);
		
		
		// Create buttons for each cell
		for(int row = 0; row < 10; row++)
		{
			for(int col = 0; col < 10; col++)
			{
				final JButton button = new JButton("(" + col + ", " + (9 - row) + ")");
				button.setBorder(null);
				button.setBackground(Color.BLACK);
				button.setFont(new Font("Arial", Font.PLAIN, 11));
				panel.add(button, row, col);	
				button.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e)
					{
						if(button.getBackground().equals(Color.WHITE))
						{
							button.setBackground(Color.BLACK);
						}
						else if(button.getBackground().equals(Color.BLACK))
						{
							button.setBackground(Color.WHITE);
						}
					} 
				});
			}
		}
		
	}
	
	
	private void build()
	{
		// Build the graph
		int grid[][] = new int[10][10];
		for(Component c : panel.getComponents())
		{
			JButton button = (JButton) c;
			String coord[] = button.getText().replace("(", "").replace(")", "").split(",");
			int x = Integer.valueOf(coord[0].trim());
			int y = Integer.valueOf(coord[1].trim());
			int value = button.getBackground().equals(Color.WHITE) ? 0 : 1;
			grid[y][x] = value;
		}
		int windowSize = Integer.valueOf(textField.getText().trim());
		graph = new WHCAStar(windowSize);
		gridNodes = GridNode.buildGraphFromGrid(graph, grid);
		
		// Setup the agents
		manager = new AgentManager(graph);
		agents.clear();
		String fromLines[] = textArea.getText().split("\n");
		String toLines[] = textArea_1.getText().split("\n");
		for(int i = 0; i < fromLines.length; i++)
		{
			String parts1[] = fromLines[i].split(",");
			String parts2[] = toLines[i].split(",");
			Node from = gridNodes[Integer.valueOf(parts1[1].trim())][Integer.valueOf(parts1[0].trim())];
			Node to = gridNodes[Integer.valueOf(parts2[1].trim())][Integer.valueOf(parts2[0].trim())];
			GridAgent agent = new GridAgent(from, to);
			manager.addAgent(agent);
			
			Color colour = colours[i % 8];
			agents.put(agent, colour);
		}
		
		// Draw agents
		drawAgents();
	}
	
	private void drawAgents()
	{
		// Reset the colours
		for(Component c : panel.getComponents())
		{
			JButton button = (JButton) c;
			if(!button.getBackground().equals(Color.WHITE) && !button.getBackground().equals(Color.BLACK))
			{
				button.setBackground(Color.WHITE);
				button.setBorder(null);
			}
		}
		
		// Draw the agent locations
		for(Component c : panel.getComponents())
		{
			JButton button = (JButton) c;
			String coord[] = button.getText().replace("(", "").replace(")", "").split(",");
			int x = Integer.valueOf(coord[0].trim());
			int y = Integer.valueOf(coord[1].trim());
			for(GridAgent agent : agents.keySet())
			{
				GridNode node = (GridNode) agent.getPosition();
				GridNode nodeGoal = (GridNode) agent.getGoal();
				if(node.x == x && node.y == y)
				{
					button.setBackground(agents.get(agent));
				}
				if(nodeGoal.x == x && nodeGoal.y == y)
				{
					button.setBorder(BorderFactory.createLineBorder(agents.get(agent), 3));
				}
			}
			
		}		
	}
	
	private void play()
	{
		boolean empty = true;
		for(GridAgent agent : agents.keySet())
		{
			if(!agent.isCurrentPathEmpty())
			{
				empty = false;
			}
		}
		
		// Need to run next iteration of WHCA*
		if(empty)
		{
			manager.recalculatePaths();
		}
		for(GridAgent agent : agents.keySet())
		{
			agent.makeStep();
		}
		
		// Redraw
		drawAgents();
	}
	
	
	class GridAgent extends Agent
	{
		
		public Path currentPath;

		public GridAgent(Node start, Node goal)
		{
			super(start, goal);
		}
		
		@Override
		public void runPath(Path path)
		{
			currentPath = path;
		}
		
		public void makeStep()
		{
			if(currentPath != null)
			{
				if(currentPath.getPathLength() > 0)
				{
					Path newPath = new Path();
					int i = 0;
					for(Node node : currentPath)
					{
						if(i == 0)
						{
							this.currentPosition = node;	
							i++;
						}
						else
						{
							newPath.addToPath(node);
						}
					}
					currentPath = newPath;
				}
			}
		}
		
		public boolean isCurrentPathEmpty()
		{
			if(currentPath != null)
			{
				if(currentPath.getPathLength() > 0)
				{
					return false;
				}
			}
			return true;
		}
		
	}

}
