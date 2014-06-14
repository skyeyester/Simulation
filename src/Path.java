/**
 * TODO Path class
 * 
 * @since jdk1.5 or upper
 * @author CCWU
 * @version 1.0
 * @date 2013.05.19
 * 
 */

import java.util.ArrayList;
import java.util.List;


public class Path {
	/** locations of the path */
	private List<Location> locs;
	/** speeds in the path legs */
	private List<Double> speeds;
	private int nextWpIndex;
	
	/**
	 * Creates a path with zero speed.
	 */
	public Path() {
		this.nextWpIndex = 0;
		this.locs = new ArrayList<Location>();
		this.speeds = new ArrayList<Double>(1);
	}

	/**
	 * Creates a path with constant speed
	 * @param speed The speed on the path
	 */
	public Path(double speed) {
		this();
		setSpeed(speed);
	}

	/**
	 * Sets a constant speed for the whole path. Any previously set speed(s)
	 * is discarded.
	 */
	public void setSpeed(double speed) {
		this.speeds = new ArrayList<Double>(1);
		speeds.add(speed);
	}
	
	/**
	 * Returns a reference to the coordinates of this path 
	 * @return coordinates of the path
	 */
	public List<Location> getCoords() {
		if(this.locs != null){
			return this.locs;
		}else{
			System.out.print("Error in Class Path, getCoords method");
			return null;
		}
	}
	
	/**
	 * Adds a new waypoint with a speed towards that waypoint
	 * @param wp The waypoint
	 * @param speed The speed towards that waypoint
	 */
	public void addWaypoint(Location wp, double speed) {
		this.locs.add(wp);
		this.speeds.add(speed);
	}
	
	public void addWaypoint(Location wp) {
		assert this.speeds.size() <= 1 : "This method should be used only for" +
			" paths with constant speed";
		this.locs.add(wp);
	}
	
	/**
	 * Print out the locations in the path
	 * 
	 */
	public void printLoc(){
		for(int i = 0 ;  i < locs.size() ; i++){
            System.out.print(locs.get(i) + ";");
		}
        System.out.println();
	}

}
