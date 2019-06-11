package uk.ac.nottingham.psyja2.ATCSimulator;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;

import uk.ac.nottingham.psyja2.ATCSimulator.Waypoint.WaypointType;

/**
 * The GUI radar display for the simulator
 * @author Josh Argent
 *
 */
public class SimulatorDisplay extends JComponent implements MouseListener, MouseMotionListener, MouseWheelListener, ComponentListener
{

	private static final long serialVersionUID = 1L;
	private static final Color BACKGROUND_COLOUR = new Color(1, 11, 20);
	private static final Color SYMBOLS_COLOUR = new Color(116, 126, 138);
	private static final Color AIRCRAFT_COLOUR = new Color(0, 220, 20);
	private static final Color CONFLICT_COLOUR = Color.RED;
	
	private float zoom = 4f;
	private int scrollX = 0;
	private int scrollY = 0;
	private Point mousePoint;
	private boolean displayLabels = true;
	
	private BufferedImage backgroundBuffer;
	private boolean invalidateBackground = true;
	
	protected SimulatorDisplay()
	{
		// Attach mouse listener
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
		addComponentListener(this);
	}
	
	@Override
	public void paintComponent(Graphics g)
	{
		// Turn on anti-aliasing
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g.setFont(new Font("Tahoma", Font.PLAIN, 12));
		
		// See if the background is invalidated
		if(invalidateBackground)
		{
			// Redraw the background buffer
			backgroundBuffer = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
			Graphics2D backgroundG2 = backgroundBuffer.createGraphics();
			backgroundG2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			backgroundG2.setFont(new Font("Tahoma", Font.PLAIN, 12));
			// Clear the background
			backgroundG2.setColor(BACKGROUND_COLOUR);
			backgroundG2.fillRect(0, 0, getWidth(), getHeight());
			// Draw the waypoints and airways
			for(Airway airway : Simulator.getInstance().getScenario().airways)
			{
				drawAirway(backgroundG2, airway);
			}
			for(Waypoint waypoint : Simulator.getInstance().getScenario().waypoints.values())
			{
				drawWaypoint(backgroundG2, waypoint);
			}
			invalidateBackground = false;
		}
		
		// Draw the background buffer to the screen
		g2.drawImage(backgroundBuffer, 0, 0, this);
		
		// Draw the aircraft
		for(Aircraft aircraft : Simulator.getInstance().getAllAircraft())
		{
			// Calculate the x,y coordinates of the aircraft from it's lat/lng values
			double x = (zoom * (scrollX + aircraft.getLocation().getX(Simulator.getInstance().getScenario().displayMin.getLongitude(), 
				Simulator.getInstance().getScenario().displayMax.getLongitude(), getWidth())));
			double y = (zoom * (scrollY + aircraft.getLocation().getY(Simulator.getInstance().getScenario().displayMin.getLatitude(), 
				Simulator.getInstance().getScenario().displayMax.getLatitude(), getHeight())));	
			
			// If the aircraft is not conflicting paint it green, otherwise make it red
			Color aircraftColour = AIRCRAFT_COLOUR;
			for(String conflictingPair : Simulator.getInstance().conflictingHistory.keySet())
			{
				if(conflictingPair.contains(aircraft.getCallsign()))
					aircraftColour = CONFLICT_COLOUR;
			}
			g.setColor(aircraftColour);
			
			// Draw the aircraft's heading line
			double radians = aircraft.getHeading() * (Math.PI / 180) - Math.PI / 2;
			double u = x + 30 * Math.cos(radians);
			double v = y + 30 * Math.sin(radians);
			g2.draw(new Line2D.Double(x, y, u, v));
			
			// Define the square path (must use a Path2D.Double to get double precision)
			double[] xs = new double[] { x - 4, x + 4, x + 4, x - 4 };
			double[] ys = new double[] { y + 4, y + 4, y - 4, y - 4};
			Path2D p = new Path2D.Double();
			p.moveTo(xs[0], ys[0]);
			for(int i = 1; i < xs.length; i++)
				p.lineTo(xs[i], ys[i]);
			p.lineTo(xs[0], ys[0]);
		
			// Draw the plane marker
			g.setColor(BACKGROUND_COLOUR);
			g2.fill(p);
			g.setColor(aircraftColour);
			g2.draw(p);
			
			// Draw the text label for the aircraft
			String line1 = aircraft.getCallsign();
			String line2 = aircraft.profile.type;
			String line3 = aircraft.getFlightLevel() + "  " + ((int)aircraft.getGroundSpeed()) + "kt";
			if(aircraft.getHeading() > 180)
			{
				FontMetrics fontMetrics = g2.getFontMetrics();
				g.drawString(line1, (int) u - 5 - fontMetrics.stringWidth(line1), (int) v);
				g.drawString(line2, (int) u - 5 - fontMetrics.stringWidth(line2), (int) v + 14);
				g.drawString(line3, (int)  u - 5 - fontMetrics.stringWidth(line3),(int)  v + 28);
			}
			else
			{
				g.drawString(line1, (int) u + 5, (int) v);
				g.drawString(line2, (int) u + 5, (int) v + 14);
				g.drawString(line3, (int) u + 5, (int) v + 28);
			}
		}
	}
	
	private void drawWaypoint(Graphics g, Waypoint waypoint)
	{		
		// Calculate the waypoint x and y position
		int x = (int) (zoom * (scrollX + waypoint.getLocation().getX(Simulator.getInstance().getScenario().displayMin.getLongitude(), 
				Simulator.getInstance().getScenario().displayMax.getLongitude(), getWidth())));
		int y = (int) (zoom * (scrollY + waypoint.getLocation().getY(Simulator.getInstance().getScenario().displayMin.getLatitude(), 
				Simulator.getInstance().getScenario().displayMax.getLatitude(), getHeight())));
		
		// Draw the waypoint icon
		if(waypoint.getType() == WaypointType.INTERSECTION)
		{
			g.setColor(BACKGROUND_COLOUR);
			g.fillPolygon(new int[] { x - 3, x + 3, x }, 
					new int[] { y + 3, y + 3, y - 3 }, 3);
			g.setColor(SYMBOLS_COLOUR);
			g.drawPolygon(new int[] { x - 3, x + 3, x }, 
					new int[] { y + 3, y + 3, y - 3 }, 3);
		}
		else if(waypoint.getType() == WaypointType.VOR)
		{
			g.setColor(BACKGROUND_COLOUR);
			g.fillPolygon(new int[] { x - 5, x + 5, x + 5, x - 5 }, 
					new int[] { y + 5, y + 5, y - 5, y - 5}, 4);
			g.setColor(SYMBOLS_COLOUR);
			g.drawPolygon(new int[] { x - 5, x + 5, x + 5, x - 5 }, 
					new int[] { y + 5, y + 5, y - 5, y - 5}, 4);
			g.drawLine(x - 5, y, x - 2, y - 5);
			g.drawLine(x + 5, y, x + 2, y - 5);
			g.drawLine(x - 5, y, x - 2, y + 5);
			g.drawLine(x + 5, y, x + 2, y + 5);
			g.fillOval(x - 1, y - 1, 3, 3);
		}
		else if(waypoint.getType() == WaypointType.NDB)
		{
			g.setColor(BACKGROUND_COLOUR);
			g.fillPolygon(new int[] { x - 3, x + 3, x }, 
					new int[] { y + 3, y + 3, y - 3 }, 3);
			g.setColor(SYMBOLS_COLOUR);
			g.drawPolygon(new int[] { x - 3, x + 3, x }, 
					new int[] { y + 3, y + 3, y - 3 }, 3);
		}
		
		// Draw the waypoint label
		if(displayLabels)
			g.drawString(waypoint.getName(), x + 8, y + 4);
		
	}
	
	private void drawAirway(Graphics g, Airway airway)
	{
		g.setColor(SYMBOLS_COLOUR);
		int x1 = (int) (zoom * (scrollX + airway.getFrom().getLocation().getX(Simulator.getInstance().getScenario().displayMin.getLongitude(), 
				Simulator.getInstance().getScenario().displayMax.getLongitude(), getWidth())));
		int y1 = (int) (zoom * (scrollY + airway.getFrom().getLocation().getY(Simulator.getInstance().getScenario().displayMin.getLatitude(), 
				Simulator.getInstance().getScenario().displayMax.getLatitude(), getHeight())));
		int x2 = (int) (zoom * (scrollX + airway.getTo().getLocation().getX(Simulator.getInstance().getScenario().displayMin.getLongitude(), 
				Simulator.getInstance().getScenario().displayMax.getLongitude(), getWidth())));
		int y2 = (int) (zoom * (scrollY + airway.getTo().getLocation().getY(Simulator.getInstance().getScenario().displayMin.getLatitude(), 
				Simulator.getInstance().getScenario().displayMax.getLatitude(), getHeight())));
		g.drawLine(x1, y1, x2, y2);
	}

	@Override
	public void mouseClicked(MouseEvent e) {} 

	@Override
	public void mouseEntered(MouseEvent e) {} 

	@Override
	public void mouseExited(MouseEvent e) {} 

	@Override
	public void mousePressed(MouseEvent e)
	{
		mousePoint = e.getPoint();
		invalidateBackgroundBuffer();
		repaint();
	}

	@Override
	public void mouseReleased(MouseEvent e) {} 

	@Override
	public void mouseDragged(MouseEvent e)
	{
		// Work out the amount of x and y movement and apply this to the scroll values
		int dx = e.getX() - mousePoint.x;
		int dy = e.getY() - mousePoint.y;
		scrollX += dx / zoom;
		scrollY += dy / zoom;
		mousePoint = e.getPoint();
		invalidateBackgroundBuffer();
		repaint();
	}

	@Override
	public void mouseMoved(MouseEvent e) {} 

	@Override
	public void mouseWheelMoved(MouseWheelEvent e)
	{
		if(e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL)
		{
			float scaleChange = (float) (e.getPreciseWheelRotation() / 5f);
			zoom -= scaleChange;			
			invalidateBackgroundBuffer();
			repaint();
		}			
	}

	/**
	 * Returns whether or not waypoint labels are been displayed
	 */
	public boolean isDisplayingLabels()
	{
		return displayLabels;
	}

	/**
	 * Set whether or not waypoint labels should be displayed
	 * @param displayLabels
	 */
	public void setDisplayLabels(boolean displayLabels)
	{
		this.displayLabels = displayLabels;
		invalidateBackgroundBuffer();
		repaint();
	}
	
	protected void invalidateBackgroundBuffer()
	{
		invalidateBackground = true;
	}

	@Override
	public void componentHidden(ComponentEvent e)
	{
		invalidateBackgroundBuffer();
	}

	@Override
	public void componentMoved(ComponentEvent e)
	{
		invalidateBackgroundBuffer();
	}

	@Override
	public void componentResized(ComponentEvent e)
	{
		invalidateBackgroundBuffer();
	}

	@Override
	public void componentShown(ComponentEvent e)
	{
		invalidateBackgroundBuffer();		
	}
	

}
