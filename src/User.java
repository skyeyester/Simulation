import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;


/**
 * TODO User class
 * 
 * @since jdk1.5 or upper
 * @author CCWU
 * @version 2.0
 * @date 2013.05.19
 * 
 */
public class User {
	
	private long mobilityseed;
	private int map_size;  //Unit: 1
	private int block_size; //Unit: 1
	private int num_blocks;
	private int mobility_model_type; //0 means Random Waypoint Model
	private Location currentLocation; //current location
	private Location lastLocation;    //the location before the last move
	private RandomWayPointModel mobilityModel;
	private List<CacheElement> sCache;
	private List<CacheElement> tCache;
	private int cache_size; //the size limitation
	private long queryarseed;
	private double arrivalRate;
	private Random queryGenerator;
	
	//initialize user configuration
	private void init(int modeltype){
	
		//get mobility model object
		if(modeltype == 0){
			mobilityModel = new RandomWayPointModel(this.mobilityseed, map_size,block_size,currentLocation, 0, 0, 0, 0);
			//initialize user's location
			currentLocation = mobilityModel.getLastWayPoint().clone();
			lastLocation = mobilityModel.getLastWayPoint().clone();
			sCache = new ArrayList<CacheElement>(cache_size);
			tCache = new ArrayList<CacheElement>(cache_size);
		}
		else{
			System.out.println("Model Type Error");
		}
		num_blocks = map_size/block_size; //assume can be divided with no remainder
    }
	
	/**
	 * construct method
	 * @param _mobseed
	 * @param _msize
	 * @param _bsize
	 * @param _model
	 * @param _queryseed
	 * @param _cache_size the size limitation
	 * 
	 */
	public User(long _mobseed, int _msize, int _bsize, int _model, int _cache_size, long _queryseed, double _rate){
		this.mobilityseed = _mobseed;
		this.map_size=_msize;
		this.block_size=_bsize;
		this.mobility_model_type=_model;
		this.cache_size = _cache_size;
		this.queryarseed = _queryseed;
		this.queryGenerator = new Random(_queryseed);
		this.arrivalRate = _rate;
		init(mobility_model_type);
	}
	
	/**
	 * set mobility model
	 * @param _mtype
	 * 
	 */
	public void set_model(int _mtype){
		init(_mtype);
    }
	
	/**
	 * get a new path
	 * 
	 */
	public Path getPath() {
		return mobilityModel.getPath();
	}
	
	/**
	 * get user's current location
	 * 
	 */
	public Location getCurrentLocation(){
		return this.currentLocation;
	}
	
	/**
	 * set user's current location
	 * @param l user's location
	 * 
	 */
	public boolean setCurrentLocation(Location l){
		if(l != null){
			this.currentLocation = l;
			return true;
		}else{
			System.out.println("Null Location Error");
			return false;
		}
	}
	
	/**
	 * get user's last location
	 * 
	 */
	public Location getLastLocation(){
		return this.lastLocation;
	}
	
	/**
	 * set user's last location
	 * @param l user's location
	 * 
	 */
	public boolean setLastLocation(Location l){
		if(l != null){
			this.lastLocation = l;
			return true;
		}else{
			System.out.println("Null Location Error");
			return false;
		}
	}
	
	/**
	 * get user's Block ID from current location
	 * @param l user's location
	 */
	public int getBlockID(Location l){
		int blockID = -1; //default= -1 means error
		if(l != null){
			int loc_x = l.getX();
			int loc_y = l.getY();
			int bloc_x = (loc_x/block_size);
			int bloc_y = (loc_y/block_size);
			blockID = bloc_x*num_blocks+bloc_y;
		}
		return blockID;
	}
	
	/**
	 * get user's tCache
	 * 
	 */
	public List<CacheElement> getTcache(){
		return this.tCache;
	}
	
	/**
	 * set user's tCache
	 * @param cahce user's tCache
	 * 
	 */
	public boolean setTcache(List<CacheElement> cahce){
		if(cahce != null){
			this.tCache = cahce;
			return true;
		}else{
			System.out.println("Null tCache Error");
			return false;
		}
	}
	
	/**
	 * get user's sCache
	 * 
	 */
	public List<CacheElement> getScache(){
		return this.sCache;
	}
	
	/**
	 * set user's tCache
	 * @param cahce user's tCache
	 * 
	 */
	public boolean setScache(List<CacheElement> cahce){
		if(cahce != null){
			this.sCache = cahce;
			return true;
		}else{
			System.out.println("Null Scache Error");
			return false;
		}
	}
	
	/**
     * Gets the inter-arrival times of Querry AR event
     * (Poisson Process: inter-arrival times exponentially distributed).
     * Renewal Process: 
     * @return The time in milliseconds
     */
    public long getPoissonInterArrivalTime(long currentTime) {
        long interTime = Math.round(
                -Math.log(1-queryGenerator.nextDouble())
                /
                this.arrivalRate
        );
        long nextTime = currentTime + interTime;
        return nextTime;
    }

	
	/**
	 * remove the Age Expired element in T-Cache
	 * @param _age_threshold the age threshold
	 * @param _current_time the current time
	 */
	public void removeAgeExpiredInT(int _age_threshold, long _current_time){
		//Avoid ConcurrentModificationException
		//copy-on write Error
		for( Iterator<CacheElement> it = this.tCache.iterator(); it.hasNext() ; ){
			CacheElement ele = it.next();
			long age = _current_time-ele.getAccessTime();
			//check the age expire or not
			if(age >= _age_threshold){
				it.remove();
			}
		}//for loop
	}
	
	/**
	 * remove the Age Expired element in T-Cache by size
	 * @param _age_threshold the age threshold
	 * @param _current_time the current time
	 */
	public void removeAgeExpiredInTbySize(int _age_threshold, long _current_time, int size){
		//Avoid ConcurrentModificationException
		//copy-on write Error
		for(int i=0; i<size; i++){
			//find the oldest element
			long oldestage=0;
			int removeindex=-1;
			for(int j=0; j<this.tCache.size(); j++){
				CacheElement ele = tCache.get(j);
				long age = _current_time-ele.getAccessTime();
				//check the age expire or not
				if(age >= oldestage){
					removeindex=j;
					oldestage = age;
				}
			}//for loop
			if(removeindex >=0){
				tCache.remove(removeindex);
				if(oldestage< _age_threshold){
					System.out.println("Error in user method: removeAgeExpiredInTbySize");
				}
			}
		}
		
	}
	
	/**
	 * remove the lower priority elements in S-Cache by size
	 * @param _size the size threshold
	 */
	public void removeSCachebySize(int _size){
		for(int i=0; i< _size; i++){
			//find the oldest element
			double lowerpri=0;
			int removeindex=-1;
			for(int j=0; j<this.sCache.size(); j++){
				CacheElement ele = sCache.get(j);
				double pri = ele.getPriority();
				//check the age expire or not
				if(pri >= lowerpri){
					removeindex=j;
					lowerpri = pri;
				}
			}//for loop
			if(removeindex >=0){
				sCache.remove(removeindex);
			}
		}
	}
	
	/**
	 * check how many Age Expired elements in T-Cache
	 * @param _age_threshold the age threshold
	 * @param _current_time the current time
	 * @return the number of elements
	 */
	public int checkAgeExpiredSizeInT(int _age_threshold, long _current_time){
		int size=0;
		//Avoid ConcurrentModificationException
		//copy-on write Error
		for( Iterator<CacheElement> it = this.tCache.iterator(); it.hasNext() ; ){
			CacheElement ele = it.next();
			long age = _current_time-ele.getAccessTime();
			//check the age expire or not
			if(age >= _age_threshold){
				size=size+1;
			}
		}//for loop
		return size;
	}
	
	
	/**
	 * remove the the Oldest element in T-Cache until the T-Cache Size <T_CacheSize_threshold
	 * @param _tCache_Size_threshold the T-Cache Size threshold
	 * @param _current_time the current time
	 */
	public void removeOldestInT(int _tCache_Size_threshold, long _current_time){
		
		while(this.tCache.size() > _tCache_Size_threshold){
			long maxage =-1;
			int index=-1;
			for(int i=0;i<this.tCache.size();i++){
				long tempage = _current_time - this.tCache.get(i).getAccessTime();
				if(tempage > maxage){
					maxage = tempage;
					index=i;
				}
			}//for loop
			if(index >= 0 ){
				this.tCache.remove(index);
			}
		}// while loop
	}
	
	/**
	 * print out all the cache elements
	 *
	 */
	public void printCacheContent(){
		System.out.println("-------------------S-Cache Content-------------------------");
		for(int i=0;i< sCache.size(); i++){
			System.out.println(i+"-Type:"+sCache.get(i).getFileType()+"; pri="+sCache.get(i).getPriority());
		}
		System.out.println("-------------------T-Cache Content-------------------------");
		for(int i=0;i< tCache.size(); i++){
			System.out.println(i+"-Type:"+tCache.get(i).getFileType()+"; pri="+tCache.get(i).getPriority());
		}
	}

}
