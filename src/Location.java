/**
 * TODO Location class
 * 
 * @since jdk1.5 or upper
 * @author CCWU
 * @version 1.0
 * @date 2013.05.19
 * 
 */

public class Location implements Cloneable, Comparable<Location> {
	
	/** co-ordinates. */
    private int x;
    /** co-ordinates. */
    private int y;
    
	/**
	 * construct method
	 * @param _block
	 * @param _x
	 * @param _y
	 * 
	 */
    public Location(int _x, int _y){
    	this.x = _x;
    	this.y = _y;
    }
    /*
    public float distanceSqr(Location l){
    	Location l2d = (Location)l;
    	float dx = x - l2d.x, dy = y - l2d.y;
    	return dx*dx + dy*dy;
    }
    
    public float distance(Location l){
    	return (float)Math.sqrt(distanceSqr(l));
    }
    */
    public int getX()
    {
      return x;
    }
    
    public int getY()
    {
      return y;
    }
    
    public String toString()
    {
      return "("+x+","+y+")";
    }

	/**
	 * Compares this location to other location. location whose y
	 * value is smaller comes first and if y values are equal, the one with
	 * smaller x value comes first.
	 * @return -1, 0 or 1 if this node is before, in the same place or
	 * after the other location
	 */
    
	@Override
	public int compareTo(Location other) {
		// TODO Auto-generated method stub

		if (this.y < other.y) {
			return -1;
		}
		else if (this.y > other.y) {
			return 1;
		}
		else if (this.x < other.x) {
			return -1;
		}
		else if (this.x > other.x) {
			return 1;
		}
		else {
			return 0;
		}
	}
	
	/**
	 * Returns a clone of this location
	 */
	public Location clone() {
		Location clone = null;
		try {
			clone = (Location) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		return clone;
	}
	
	/**
	 * Checks if this location is equal to other one
	 * @param c The other coordinate
	 * @return True if locations are the same
	 */
	public boolean equals(Location c) {
		if (c == this) {
			return true;
		}
		else {
			return (x == c.x && y == c.y);
		}
	}
}
