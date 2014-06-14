/**
 * TODO User status class
 * 
 * @since jdk1.5 or upper
 * @author CCWU
 * @version 1.0
 * @date 2013.05.19
 * 
 */
public class UserStatus {
	public int totalRequestCount;       //the total request during simulation =preload+cacheMiss
	public int cacheMissCount;			// Cache Miss
	public int cacheHitCount;			// Cache Hit
	public long totalTrafficLoad;       // the total traffic load during simulation (Unit:byte)
	public long totalPowerConsumption;  // the total power consumption during simulation (Unit:W)
	public long totalUserWait; 	        // the total response delay during simulation
}