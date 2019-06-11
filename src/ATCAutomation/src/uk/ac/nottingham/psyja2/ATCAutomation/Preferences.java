package uk.ac.nottingham.psyja2.ATCAutomation;

public abstract class Preferences
{
	/* Heuristic */
	public static final int MANHATTAN = 0;
	public static final int STRAIGHT_LINE = 1;
	public static final int TRUE_DISTANCE = 2;
	public static int HEURISTIC = TRUE_DISTANCE;
	
	/* Priority System */
	public static final int AGITATION = 0;
	public static final int FIFO = 1;
	public static final int FURTHEST_DESTINATION = 2;
	public static final int CLOSEST_DESTINATION = 3;
	public static int PRIORITY_SYSTEM = FURTHEST_DESTINATION;
	
	/* Intermediate Spacing Distance (NM) */
	public static int INTERMEDIATE_SPACING = 4;
	
	/* Reservation Time (minutes) */
	public static int RESERVATION_TIME = 10;
	
	/* Window Time (minutes) */
	public static int WINDOW_SIZE = 90;
	
	public static void printPreferences()
	{
		System.out.println("Running with:-");
		System.out.println("	Heuristic: " + ((HEURISTIC == MANHATTAN) ? "Manhattan Distance" : (HEURISTIC == STRAIGHT_LINE) ? "Straight Line Distance" : "True Distance"));
		System.out.println("	Priority System: " + ((PRIORITY_SYSTEM == AGITATION) ? "Most Inconvinienced" : (PRIORITY_SYSTEM == FIFO) ? "FIFO" : (PRIORITY_SYSTEM == FURTHEST_DESTINATION) ? "Furthest Distance" : "Closest Distance"));
		System.out.println("	Intermediate Spacing: " + INTERMEDIATE_SPACING);
		System.out.println("	Reservation Time: " + RESERVATION_TIME);
		System.out.println("	Window Size: " + WINDOW_SIZE);
	}

}
