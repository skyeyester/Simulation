import java.util.Random;

/**
 * Random Waypoint movement model. Creates zig-zag paths within the
 * simulation area.
 * @since jdk1.5 or upper
 * @author CCWU
 * @version 1.0
 * @date 2013.05.19
 * 
 */

public class RandomWayPointModel {
	
	/** how many waypoints should there be per path */
	private static final int PATH_LENGTH = 1;
	private Location lastWaypoint;
	protected static Random rng; 
	
	/** thickness of border (for float calculations). */
	public static final float BORDER = (float)0.0005;
	
	private int mapSize;
	private int blockSize;
	
	/** Movement boundaries. */
	private Location bounds;
	
	/** Waypoint pause time. */
	private long pauseTime;
	
	/** Step granularity. */
	private float precision;
	
	/** Minimum movement speed. */
	private int minSpeed; 
	
	/** Maximum movement speed. */
	private int maxSpeed;
	
	/**
	 * Initialize random waypoint mobility model.
	 *
	 * @param mapsize the number of blocks on the map
	 * @param blocksize the size of blocks
	 * @param bounds boundaries of movement
	 * @param pausetime waypoint pause time
	 * @param precision step granularity
	 * @param minspeed minimum speed
	 * @param maxspeed maximum speed
	 */
	
	public RandomWayPointModel(long mobseed, int mapsize, int blocksize, Location bounds, long pausetime, float precision, int minspeed, int maxspeed)
	{
		init(mobseed, mapsize, blocksize, bounds, pausetime, precision, minspeed, maxspeed);
	}
	
	/**
	 * Initialize random waypoint mobility model.
	 * 
	 * @param mapsize the number of blocks on the map
	 * @param blocksize the size of blocks
	 * @param bounds boundaries of movement
	 * @param pausetime waypoint pause time (in ticks)
	 * @param precision step granularity
	 * @param minspeed minimum speed
	 * @param maxspeed maximum speed
	 */
	private void init(long seed, int mapsize, int blocksize, Location bounds, long pausetime, float precision, int minspeed, int maxspeed)
	{	
		this.mapSize = mapsize;
		this.blockSize = blocksize;
		this.bounds = bounds;
		this.pauseTime = pausetime;
		this.precision = precision;
		this.minSpeed = minspeed;
		this.maxSpeed = maxspeed;
		//
		rng = new Random(seed);
		lastWaypoint = randomLocation();
	}
	
	public Path getPath() {
		Path p;
		p = new Path(generateSpeed());
		p.addWaypoint(lastWaypoint.clone());
		Location c = lastWaypoint;

		for (int i=0; i<PATH_LENGTH; i++) {
			c = randomLocation();
			p.addWaypoint(c);	
		}

		this.lastWaypoint = c;
		return p;
	}

	/**
	 * Random waypoint state object.
	 *
	 */
	
	public static class RandomWaypointInfo
	{
		/** number of steps remaining to waypoint. */
		public int steps;
		
		/** duration of each step. */
		public long stepTime;
		
		/** waypoint. */
		public Location waypoint;
	}
	
	/**
	 * Generates and returns a speed value between min and max of the setting.
	 * 
	 * @return A new speed between min and max values 
	 */
	protected double generateSpeed() {
		if (rng == null) {
			return 1;
		}
		return rng.nextInt();
		//return (maxSpeed - minSpeed) * rng.nextDouble() + minSpeed;
	}
	
	protected Location randomLocation() {
		//return an integer pair from a uniform distribution 
		//between 0 and one less than the parameter mapSize
		return new Location(rng.nextInt(mapSize), rng.nextInt(mapSize));
	}
	
	public Location getLastWayPoint(){
		return this.lastWaypoint;
	}
	
}
