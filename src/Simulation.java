import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * TODO Simulation class
 * 
 * @since jdk1.5 or upper
 * @author CCWU
 * @version 1.0
 * @date 2013.02.16
 * 
 */
public class Simulation {
	private long simulationLength;      // the duration of Simulation
	private long currentsimulationTime; //the current time
	private boolean simEndFlag;    //simulation is end or not
	private int numUsers; //the number of user,default:1
	private User user;
	private long pauseTime; //User stay time Unit:0.001s
	private int userSpeed;  //User move speed Unit:1/0.001s
	private int pauseLow, pauseHigh; // 
	private UserStatus[] ustat = new UserStatus[11]; //at most 10 users : need to modify
	private PQueue pq = new PQueue(); // the event Queue
	private Random rnd = new Random(); // used for arrival and serve time
	private Random userActivityGen;	   // used for user access activity
	private Database TargetDB;
	private int mapSize;
	private int blockSize;
	private int numOfBlocksOnMap;
	private int cache_size;  //default: tCache=sCache
	private int totalFilesOnMap;
	private int numOfFileKinds;
	private long generateFileSeed;
	private long userBehaveiorSeed;      //random seed of user access AR activity
	private long userMobBehaveiorSeed;   //random seed of user mobility
	private long userQueryBehaveiorSeed; //random seed of query AR activity
	private double arrivalRate;            //unit: Num/second
	private int usedAlgorithm;
	private float priorityweight_w1;
	private float priorityweight_w2;
	private float priorityweight_w3;
	private float chgblock_alpha;
	private float chgblock_beta;
	private int ageThreshold;
	private int tSizeThreshold;
	private int sSizeThreshold;
	private BlockMonitor userMonitor;
	private double chgBlock_D_threshold;
	private long chgBlock_T_threshold;
	private boolean debugmode;
	
	/**
	 * Constructor
	 * 
	 * @param simulationLength
	 * @param numUsers
	 * @param pauseLow
	 * @param pauseHigh
	 * @param userMoveSpeed
	 * @param mapSize
	 * @param blockLength
	 * @param cacheSize
	 * @param filesOnMap
	 * @param kinds
	 * @param gFileSeed
	 * @param userBevSeed
	 * @param alg          //the algorithm used in simulation
	 * @param age_T
	 * @param sizeT_T
	 * @param weight_w1
	 * @param weight_w2
	 * @param weight_w3
	 * @param alpha
	 * @param beta
	 * @param debug
	 * @throws PQueueException
	 */
	public Simulation(long simulationLength, int numUsers, int pauseLow, int pauseHigh, int userMoveSpeed,
					  int mapSize, int blockLength, int cacheSize, int filesOnMap, 
					  int kinds, long gFileSeed, long userBevSeed, long userMobBevSeed, long userQueryBevSeed, double arrivalRate, int alg, int age_T, int sizeT_T,
					  float weight_w1, float weight_w2, float weight_w3, float alpha, float beta, boolean debug)throws PQueueException {
		
		//generate simulation End event
		Event simEndEvent = new Event(simulationLength, EventType.SIM_END, 1, 0, 0, 0, 0, 0, 0, 0);

		//generate first update location event
		Event firstEvent = new Event(0, EventType.UpdateLocation, 1, 0, 0, 0, 0, 0, 0, 0);
		
		//generate first query AR event 
		//PS: a query AR event ----------> a serious access AR events
		Event firstAccessAREvent = new Event(0, EventType.QUERY_AR, 1, 0, 0, 0, 0, 0, 0, 0);
		
		//initial user log
		//User ID start from 1
		for (int i = 1; i <= 10; i++) { 
			ustat[i] = new UserStatus();
			ustat[i].totalRequestCount = 0;
			ustat[i].cacheHitCount = 0;
			ustat[i].cacheMissCount = 0;
			ustat[i].totalTrafficLoad = 0;
			ustat[i].totalPowerConsumption = 0;
			ustat[i].totalUserWait =0;
		}

		this.simulationLength = simulationLength; // the simulation time (Unit: 0.001s)
		this.currentsimulationTime = 0;
		this.simEndFlag = false;
		this.numUsers = numUsers; //the number of user
		
		this.pauseLow = pauseLow; //the range of user random stay in a block
		this.pauseHigh = pauseHigh;
		this.userSpeed = userMoveSpeed; //the initial user speed
				
		this.mapSize = mapSize;
		this.blockSize = blockLength;
		this.numOfBlocksOnMap = mapSize/blockLength; //assume divide with no remainder
		this.cache_size = cacheSize; //the cache capacity of T and S cache
		this.totalFilesOnMap = filesOnMap;
		this.numOfFileKinds = kinds;
		this.generateFileSeed = gFileSeed;
		this.userMobBehaveiorSeed = userMobBevSeed;
		this.userBehaveiorSeed = userBevSeed;
		this.userQueryBehaveiorSeed = userQueryBevSeed;
		this.arrivalRate = arrivalRate;
		this.userActivityGen = new Random(userBevSeed);
		this.usedAlgorithm = alg;
		this.ageThreshold = age_T;
		this.tSizeThreshold = sizeT_T;
		this.sSizeThreshold = (2*cacheSize)-sizeT_T;
		this.priorityweight_w1 = weight_w1;
		this.priorityweight_w2 = weight_w2;
		this.priorityweight_w3 = weight_w3;
		this.chgblock_alpha = alpha;
		this.chgblock_beta = beta;
		//need to modify
		this.chgBlock_D_threshold = (this.chgblock_alpha*this.blockSize)*(this.chgblock_alpha*this.blockSize);
		this.chgBlock_T_threshold = (int)(this.chgblock_beta*this.ageThreshold);
		this.debugmode = debug;
		
		//generate Database
		this.TargetDB = new Database(mapSize,blockLength,filesOnMap,generateFileSeed, numOfFileKinds);
		
		//generate User
		this.user = new User(userMobBevSeed, mapSize, blockLength, 0, cache_size, userQueryBevSeed, arrivalRate);
		
		//initial User Block Monitor
		this.userMonitor = new BlockMonitor();
		
		//insert simulation finished event
		pq.enPQueue(simEndEvent);
		//insert initial update location event
		pq.enPQueue(firstEvent);
		//insert first query AR event
		pq.enPQueue(firstAccessAREvent);
		
		//if use Our Schema,the pre-load feature files at beginning
		if(usedAlgorithm == 1 || usedAlgorithm == 2){
			//generate change block event
			Event firstChanBlockEvent = new Event(0, EventType.CHANGE_BLOCK, 1, 0, 0, 0, 0, 0, 0, 0);
			//insert change block event
			pq.enPQueue(firstChanBlockEvent);
		}else{
			//usedAlgorithm == 0
			//usedAlgorithm == 3
			//usedAlgorithm == 4
			//do nothing
		}

		if(debugmode == true){
			Map<Integer, Integer> fileFreqTable = this.TargetDB.getOccurredFreqTable();
			int index=1;
			System.out.println("------------------------Occurred Frequency Table Content--------------------------------------------");
			for(Integer KeyValue : fileFreqTable.keySet()){
				System.out.println(index+"-Key: "+ KeyValue + " Value: " + fileFreqTable.get(KeyValue) );
				index=index+1;
			}
			//
			Map<Integer, Integer> fileProbTable = this.TargetDB.getAccessProbTable();
			int index2=1;
			System.out.println("------------------------Access Probability Table Content--------------------------------------------");
			for(Integer KeyValue : fileProbTable.keySet()){
				System.out.println(index2+"-Key: "+ KeyValue + " Value: " + fileProbTable.get(KeyValue) );
				index2=index2+1;
			}
			//
			System.out.println("------------------------------Database Content------------------------------------------------------");
			this.TargetDB.printDB();
		}

	}
	
	
	/**
	 * check user access AR or not
	 * if yes return a list Target Feature Files
	 * if no  return null
	 * 
	 * @param block_id //user's location in which block
	 * @param userLoc  //user's location
	 * @return List<FeatureFile>
	 * 
	 */
	private List<FeatureFile> accessARTarget(int block_id, Location userLoc){
		List<FeatureFile> access = new ArrayList<FeatureFile>();
		List<FeatureFile> filesInBlock = this.TargetDB.searchDB(block_id);
		Map<Integer, Integer> probability = this.TargetDB.getAccessProbTable();
		Map<Integer, Integer> frequency = this.TargetDB.getOccurredFreqTable();
		/*
		List<FeatureFile> finalaccess = new ArrayList<FeatureFile>();
		for(int i ;i<access;)
		finalaccess = access;
		*/
		for(FeatureFile f :filesInBlock){
			int prob_value = probability.get(f.getFileType());
			int diffX = userLoc.getX()-f.getFileLocationX();
			int diffY = userLoc.getY()-f.getFileLocationY();
			int distance = diffX*diffX+diffY*diffY;
			//need to modify
			//uniform distribution
			//if random value > prob_value means access (probability=2.27%)
			//normal distribution
			int accessRange = this.blockSize*this.blockSize/25;
			if((distance <= accessRange) && (userActivityGen.nextInt(100)> prob_value)){
				access.add(f);
			}
		}//for loop
		/*
		for(FeatureFile f :access){
			frequency.get(f.getFileType());
			finalaccess.add(f);
			if(()/10) > 0){
				finalaccess.add(f);
			}
		}*/
		return access;
	}

	/**
	 * Convert Path to Move events
	 * 
	 * @param currentsimulationTime //the time in simulation
	 * @param speed //user's move speed
	 * @param nextPath //user's move path
	 * 
	 * @return List<Event>
	 */
	private List<Event> pathToEvents(long currentsimulationTime, int speed, Path nextPath){
		List<Event> list = new ArrayList<Event>();
		List<Location> loc = null;
		if(nextPath != null){
			loc = nextPath.getCoords();
		}
		
		Location currentLoc = loc.get(0);
		Location nextLoc = loc.get(1);
		
		int currentX = currentLoc.getX();
		int nextX = nextLoc.getX();
		int currentY = currentLoc.getY();
		int nextY = nextLoc.getY();
		
		double x_diff = currentX -nextX;
		double y_diff = currentY -nextY;
		double abs_x_diff = Math.abs(x_diff);
		double abs_y_diff = Math.abs(y_diff);
		//
		long time = (int)Math.ceil(Math.sqrt((x_diff*x_diff)+(y_diff*y_diff))/speed);
		if(time ==0 ){
			System.out.println("Current location:"+currentX+","+currentY);
			System.out.println("Next location:"+nextX+","+nextY);
			System.out.println("Error in method pathToEvents");
		}
		double x_speed = Math.ceil(abs_x_diff/time);
		double y_speed = Math.ceil(abs_y_diff/time);
		long x_timeInterval;
		long y_timeInterval;
		if(x_speed > 0){
			x_timeInterval = (int)((1/x_speed)*1000);
		}else{
			x_timeInterval = 0;
		}
		if(y_speed >0){
			y_timeInterval = (int)((1/y_speed)*1000);
		}else{
			y_timeInterval = 0;
		}
		//for debug
		if(debugmode == true){
			System.out.println("Used Time to next Destination = "+time+"s");
		}
		/*
		System.out.println("x_diff = "+x_diff);
		System.out.println("abs_x_diff = "+abs_x_diff);
		System.out.println("x_speed = "+x_speed);
		System.out.println("x_timeInterval = "+x_timeInterval+"ms");
		System.out.println("y_diff = "+y_diff);
		System.out.println("abs_y_diff = "+abs_y_diff);
		System.out.println("y_speed = "+y_speed);
		System.out.println("y_timeInterval = "+y_timeInterval+"ms");
		*/
		Event moveEvent;
		//Move X direction
		;
		int mov_x=0;
		for (int i=1; i<=abs_x_diff; i++){
			if (x_diff>0){
	        	mov_x = -1;
	        }else{
	        	mov_x = 1;
	        }
			long time_x=currentsimulationTime+x_timeInterval*i;
	        moveEvent = new Event(time_x, EventType.Move, 1, 0, 0, mov_x, 0, 0, 0, 0);
	        list.add(moveEvent);
	    }
		//Move Y direction
		int mov_y=0;
		for (int i=1; i<=abs_y_diff; i++){
			if (y_diff>0){
	        	mov_y = -1;
	        }else{
	        	mov_y = 1;
	        }
			long time_y = currentsimulationTime+y_timeInterval*i;
	        moveEvent = new Event(time_y, EventType.Move, 1, 0, 0, 0, mov_y, 0, 0, 0);
	        list.add(moveEvent);
	    }
		//add UpdateLocationEvent
		long nextupdate_time = time*1000;
		moveEvent = new Event(currentsimulationTime+nextupdate_time, EventType.UpdateLocation, 1, 0, 0, 0, 0, 0, 0, 0);
		list.add(moveEvent);
		return list;
	}
	
	/**
	 * Convert to Access AR events
	 * 
	 * @param currentsimulationTime current simulation time in the simulation
	 * @param wantAccess the accessed AR Target files
	 * @return List<Event>
	 */
	private List<Event> idToEvents(long currentsimulationTime, List<FeatureFile> wantAccess){
		List<Event> list = new ArrayList<Event>();
		Event accessEvent;
		for (int i=0; i<wantAccess.size(); i++){
			long time=currentsimulationTime;
			int filetype =  wantAccess.get(i).getFileType();
			int fileLocX = wantAccess.get(i).getFileLocationX();
			int fileLocY = wantAccess.get(i).getFileLocationY();
			accessEvent = new Event(time, EventType.ACCESS_AR, 1, 0, 0, 0, 0, filetype, fileLocX, fileLocY);
			list.add(accessEvent);
		}
		return list;
	}
	
	/**
	 * compute the priority of CacheElement
	 * 
	 * @param _type
	 * @param _loc_x  file's location x
	 * @param _loc_y  file's location y
	 * @param _maxSFreq
	 * @param _maxAFreq
	 * @return double
	 */
	private double computePriority(int _type, int _loc_x, int _loc_y, int _maxSFreq, int _maxAFreq){
		double priority = 0;
		int sfrequency = this.TargetDB.getOccurredFreqTable().get(_type);
		int afrequency = this.TargetDB.getAccessFreqTable().get(_type);
		int user_loc_x = user.getCurrentLocation().getX();
		int user_loc_y = user.getCurrentLocation().getY();
		int xdiff = user_loc_x - _loc_x;
		int ydiff = user_loc_y - _loc_y;
		if(this.debugmode == true){
			/*
			System.out.println("in method computePriority");
			System.out.println(_type+","+_loc_x+","+user_loc_x+","+_loc_y+","+user_loc_y);
			System.out.println(sfrequency+","+_maxSFreq+","+afrequency+","+ _maxAFreq);
			*/
		}
		//compute L(f)
		double normaler = 1.414*this.blockSize;
		double locOfFile = Math.sqrt(((xdiff*xdiff)+(ydiff*ydiff)))/normaler;
		//compute S(f) and A(f)
		//need to modify weight number
		if((_maxSFreq == 0) || (_maxAFreq ==0)){
			System.out.println("Error in method computePriority:"+_maxSFreq+";"+_maxAFreq);
		}else{
			double diffS = (double)(_maxSFreq-sfrequency);
			double diffA = (double)(_maxAFreq-afrequency);
			double showOfFile = diffS/_maxSFreq;
			double accessOfFile = diffA/_maxAFreq;

			priority = (this.priorityweight_w1*locOfFile)+(this.priorityweight_w2*showOfFile)+(this.priorityweight_w3*accessOfFile);
			if(this.debugmode == true){
				/*
				System.out.println(locOfFile);
				System.out.println(showOfFile);
				System.out.println(accessOfFile);
				System.out.println("priority value:"+priority);
				*/
			}
		}

		return priority;
	}
	
	private List<CacheElement> getDownloadElements(int availableSize, List<CacheElement> orderedPriFileinNeighbor){
		int available_S_CacheSize = availableSize;
		List<CacheElement> currentScache = this.user.getScache();
		List<CacheElement> currentTcache = this.user.getTcache();
		
		List<CacheElement> downloadFromDB = new ArrayList<CacheElement>();
		List<CacheElement> keepInCache = new ArrayList<CacheElement>();
		int index=0; //already have download
		int p = 0;  //pointer the wanted download
		while(index < available_S_CacheSize){
			if(p >= orderedPriFileinNeighbor.size()){
				break;
			}
			boolean addflag = false;
			boolean addflag2 = false;
			int type1 = orderedPriFileinNeighbor.get(p).getFileType();
			//check the new element is already in SCache or not
			if(currentScache.size() == 0){addflag = true;}
			for(int i=0;i<currentScache.size();i++){
				if(type1 == currentScache.get(i).getFileType()){
					CacheElement keep = currentScache.get(i);
					keepInCache.add(keep);
					addflag = false;
					break;
				}else{
					if(i == (currentScache.size()-1)){
						addflag = true;
					}
				}

			}
			//check the new element is already in TCache or not
			if(currentTcache.size() == 0){addflag2 = true;}
			for(int i=0;i<currentTcache.size();i++){
				if(type1 == currentTcache.get(i).getFileType()){
					addflag2 = false;
					break;
				}else{
					if(i == (currentTcache.size()-1)){
						addflag2 = true;
					}
				}

			}
			if(addflag == false){
				p=p+1;
				index=index+1;
				continue;
			}else if(addflag2 == false){
				p=p+1;
				continue;
			}else{
				CacheElement tempCache_E = orderedPriFileinNeighbor.get(p); 
				downloadFromDB.add(tempCache_E);
				p=p+1;
				index=index+1;
			}
		}//while
		
		return downloadFromDB;
	}
	
	/**
	 * get ordered CacheElements form Feature Files in the Neighbor blocks around user
	 * and Re-order by priority
	 * 
	 * @param list un-sort Feature Files
	 * @return List<CacheElement>
	 */
	private List<CacheElement> getSortCacheElementInNeighbor(List<FeatureFile> list){
		//sort for ascending order
		List<CacheElement> orderedList = new ArrayList<CacheElement>();
		int maxSFreq=0;
		int maxAFreq=0;
		//get the max freq in neighbor list
		for(FeatureFile f : list){
			int t_type = f.getFileType();
			int t_freq = this.TargetDB.getOccurredFreqTable().get(t_type);
			if(maxSFreq < t_freq){
				maxSFreq = t_freq;
			}
		}
		//
		for(FeatureFile f : list){
			int t_type = f.getFileType();
			int t_freq = this.TargetDB.getAccessFreqTable().get(t_type);
			if(maxAFreq < t_freq){
				maxAFreq = t_freq;
			}
		}
		
		//list re-Sort by priority
		for(FeatureFile f : list){
			int temp_type = f.getFileType();
			int temp_loc_x = f.getFileLocationX();
			int temp_loc_y = f.getFileLocationY();
			double temp_pri = computePriority(temp_type, temp_loc_x, temp_loc_y, maxSFreq, maxAFreq);
			long temp_access_t = this.currentsimulationTime;
			CacheElement temp_element = new CacheElement(temp_type, temp_pri, temp_access_t);
			orderedList.add(temp_element);
		}
		Iterator<CacheElement> i = orderedList.iterator();
		
		//remove the low possibility element
		//need to modify
		/*
		while (i.hasNext()) {
			CacheElement s = i.next();
			if(s.getPriority() >1){
				i.remove();
			}
		}*/
		Collections.sort(orderedList);
		return orderedList;
	}
	
	/**
	 * get Feature Files in the Neighbor blocks around user
	 * 
	 * @param now user's current location
	 * @param pre user's previous location
	 * @return List<FeatureFile>
	 */
	private List<FeatureFile> getFeatureFileInNeighbor(Location now, Location pre){
		int current_locX = now.getX();
		int current_locY = now.getY();
		int previous_locX = pre.getX();
		int previous_locY = pre.getY();
		int xDiff = current_locX-previous_locX;
		int yDiff = current_locY-previous_locY;
		//0:left -> right
		//1:right -> left 
		//2: up -> down
		//3: down -> up
		int direction=-1; 
		if(xDiff>0){
			//left -> right
			direction = 0;
		}else if(xDiff<0){
			//right -> left
			direction = 1;
		}else if(yDiff>0){
			//up -> down
			direction = 2;
		}else if(yDiff<0){
			//down -> up
			direction = 3;
		}else{
			//at most one time in the beginning
			System.out.println("Error in method getFeatureFileInNeighbor, direction");
		}
		//for debug
		/*
		System.out.print("In getFeatureFileInNeighbor:");
		System.out.print("(xDiff="+xDiff+";");
		System.out.print("yDiff="+yDiff+";");
		System.out.println("(direction="+direction+")");
		*/
		//
		List<FeatureFile> list = new ArrayList<FeatureFile>();
		int currentblockID = user.getBlockID(now);
		List<FeatureFile> tempFiles = this.TargetDB.searchDB(currentblockID);
		list = contactList(list, tempFiles, now);
		//for debug
		/*
		System.out.println("IN function getFeatureFileInNeighbor");
		System.out.println("Add current Block");
		for(int j=0; j<list.size(); j++){
			System.out.println(j+"-Type:"+list.get(j).getFileType());
		}
		*/
		int currentblockID_X = currentblockID/this.numOfBlocksOnMap;
		int currentblockID_Y = currentblockID%this.numOfBlocksOnMap;
		int currentblockID_X_left = currentblockID_X-1;
		int currentblockID_X_right = currentblockID_X+1;
		int currentblockID_Y_up = currentblockID_Y-1;
		int currentblockID_Y_down = currentblockID_Y+1;
		
		//Left
		/*
		if((direction != 0) && (currentblockID_X_left >= 0)){
			int tempID = currentblockID_X_left*this.numOfBlocksOnMap+currentblockID_Y;
			List<FeatureFile> tempFiles1 = this.TargetDB.searchDB(tempID);
			list = contactList(list, tempFiles1, now);
		}*/
		//for debug
		/*
		System.out.println("Add Left Block");
		for(int j=0; j<list.size(); j++){
			System.out.println(j+"-Type:"+list.get(j).getFileType());
		}
		*/
		/*
		//Right
		if((direction != 1) && (currentblockID_X_right < this.numOfBlocksOnMap)){
			int tempID = currentblockID_X_right*this.numOfBlocksOnMap+currentblockID_Y;
			List<FeatureFile> tempFiles1 = this.TargetDB.searchDB(tempID);
			list = contactList(list, tempFiles1, now);
		}
		*/
		//for debug
		/*
		System.out.println("Add Right Block");
		for(int j=0; j<list.size(); j++){
			System.out.println(j+"-Type:"+list.get(j).getFileType());
		}
		*/
		//Upper
		/*
		if((direction != 3) && (currentblockID_Y_up >= 0)){
			int tempID = currentblockID_X*this.numOfBlocksOnMap+currentblockID_Y_up;
			List<FeatureFile> tempFiles1 = this.TargetDB.searchDB(tempID);
			list = contactList(list, tempFiles1, now);
		}
		*/
		//for debug
		/*
		System.out.println("Add Upper Block");
		for(int j=0; j<list.size(); j++){
			System.out.println(j+"-Type:"+list.get(j).getFileType());
		}
		*/
		//Down
		/*
		if((direction != 2) && (currentblockID_Y_down < this.numOfBlocksOnMap)){
			int tempID = currentblockID_X*this.numOfBlocksOnMap+currentblockID_Y_down;
			List<FeatureFile> tempFiles1 = this.TargetDB.searchDB(tempID);
			list = contactList(list, tempFiles1, now);
		}
		*/
		//for debug
		/*
		System.out.println("Add Down Block");
		for(int j=0; j<list.size(); j++){
			System.out.println(j+"-Type:"+list.get(j).getFileType());
		}
		*/
		//UpLeft
		/*
		if((direction != 0) && (direction != 3) && (currentblockID_Y_up >= 0) && (currentblockID_X_left >= 0)){
			int tempID = currentblockID_X_left*this.numOfBlocksOnMap+currentblockID_Y_up;
			List<FeatureFile> tempFiles1 = this.TargetDB.searchDB(tempID);
			list = contactList(list, tempFiles1, now);
		}*/
		//for debug
		/*
		System.out.println("Add UpLeft Block");
		for(int j=0; j<list.size(); j++){
			System.out.println(j+"-Type:"+list.get(j).getFileType());
		}
		*/
		//UpRight
		/*
		if((direction != 1) && (direction != 3) && (currentblockID_Y_up >= 0) && (currentblockID_X_right < this.numOfBlocksOnMap)){
			int tempID = currentblockID_X_right*this.numOfBlocksOnMap+currentblockID_Y_up;
			List<FeatureFile> tempFiles1 = this.TargetDB.searchDB(tempID);
			list = contactList(list, tempFiles1, now);
		}
		*/
		//for debug
		/*
		System.out.println("Add UpRight Block");
		for(int j=0; j<list.size(); j++){
			System.out.println(j+"-Type:"+list.get(j).getFileType());
		}
		*/
		//DownLeft
		/*
		if((direction != 0) && (direction != 2) && (currentblockID_Y_down < this.numOfBlocksOnMap) && (currentblockID_X_left >= 0)){
			int tempID = currentblockID_X_left*this.numOfBlocksOnMap+currentblockID_Y_down;
			List<FeatureFile> tempFiles1 = this.TargetDB.searchDB(tempID);
			list = contactList(list, tempFiles1, now);
		}*/
		//for debug
		/*
		System.out.println("Add DownLeft Block");
		for(int j=0; j<list.size(); j++){
			System.out.println(j+"-Type:"+list.get(j).getFileType());
		}
		*/
		//DownRight
		/*
		if((direction != 1) && (direction != 2) && (currentblockID_Y_down < this.numOfBlocksOnMap) && (currentblockID_X_right < this.numOfBlocksOnMap)){
			int tempID = currentblockID_X_right*this.numOfBlocksOnMap+currentblockID_Y_down;
			List<FeatureFile> tempFiles1 = this.TargetDB.searchDB(tempID);
			list = contactList(list, tempFiles1, now);
		}
		*/
		//for debug
		/*
		System.out.println("Add DownRight Block");
		for(int j=0; j<list.size(); j++){
			System.out.println(j+"-Type:"+list.get(j).getFileType());
		}
		*/
		return list;
	}
	
	/**
	 * contact List b at the end of List a as a single list
	 * if there are some FeatureFiles have duplicated types, add the nearest one(compare the distance between user and file)
	 * 
	 * @param a list1 want to be added into container
	 * @param b list2 want to be added into container
	 * @param currentLoc user's current location
	 * @return List<FeatureFile>
	 */
	private List<FeatureFile> contactList(List<FeatureFile> a, List<FeatureFile> b, Location currentLoc){
		List<FeatureFile> list = new ArrayList<FeatureFile>();
		//deal with list a
		for(FeatureFile element: a){
			boolean addflag = false;
			//check duplicated
			if(list.size() ==0){addflag = true;}
			for(Iterator<FeatureFile> it = list.iterator(); it.hasNext() ;){
				FeatureFile now = it.next();
				if(element.getFileType() == now.getFileType()){
					//if two file's type are the same, compare the distance
					int diffX_e = element.getFileLocationX()-currentLoc.getX();
					int diffY_e = element.getFileLocationY()-currentLoc.getY();
					int dist_e = (diffX_e*diffX_e)+(diffY_e*diffY_e);
					int diffX_now = now.getFileLocationX()-currentLoc.getX();
					int diffY_now = now.getFileLocationY()-currentLoc.getY();
					int dist_now = (diffX_now*diffX_now)+(diffY_now*diffY_now);
					if(dist_e < dist_now){
						it.remove();
						addflag = true;
						break;
					}else{
						addflag = false;
						break;
					}
				}else{
					//after compare all element in list,and not the same then add into list
					if(it.hasNext() == false) {addflag = true;}
				}
			}//for every element in list
			
			if(addflag == true){
				list.add(element);
			}
		}//for every element in a
		
		//deal with list b
		for(FeatureFile element: b){
			boolean addflag = false;
			//check duplicate
			if(list.size() ==0){addflag = true;}
			for(Iterator<FeatureFile> it = list.iterator(); it.hasNext() ;){
				FeatureFile now = it.next();
				if(element.getFileType() == now.getFileType()){
					//if two file's type are the same, compare the distance
					int diffX_e = element.getFileLocationX()-currentLoc.getX();
					int diffY_e = element.getFileLocationY()-currentLoc.getY();
					int dist_e = (diffX_e*diffX_e)+(diffY_e*diffY_e);
					int diffX_now = now.getFileLocationX()-currentLoc.getX();
					int diffY_now = now.getFileLocationY()-currentLoc.getY();
					int dist_now = (diffX_now*diffX_now)+(diffY_now*diffY_now);
					if(dist_e < dist_now){
						it.remove();
						addflag = true;
						break;
					}else{
						addflag = false;
						break;
					}
				}else{
					//after compare all element in list,and not the same then add into list
					if(it.hasNext() == false) {addflag = true;}
				}
			}//for every element in list
			
			if(addflag == true){
				list.add(element);
			}
		}//for every element in b
		//for degug
		if(debugmode == true){
			System.out.print("In method contactList return Size:");
			System.out.println(list.size());
		}
		return list;
	}
	
	/**
	 * Check change block or not
	 * 
	 * @param oldLoc user's previous location
	 * @param lastLoc user's current location
	 * @return boolean true means change, false means no change
	 */
	private boolean changeBlock(Location oldLoc, Location lastLoc){
		int oldblock_id = user.getBlockID(oldLoc);
		int nowblock_id = user.getBlockID(lastLoc);
		
		//No change block monitor
		if(oldblock_id == nowblock_id){
			return false;
		}else{
			return true;
		}
	}
	
	/**
	 * Check change block or not
	 * 
	 * @param oldLoc user's previous location
	 * @param lastLoc user's current location
	 * @return boolean true means change, false means no change
	 */
	
	private boolean changeBlockWithBlockMonitor(Location oldLoc, Location newLoc){
		int oldblock_id = user.getBlockID(oldLoc);
		//
		int nowblock_id = user.getBlockID(newLoc);
		int bloc_y = nowblock_id%this.numOfBlocksOnMap;
		int bloc_x = nowblock_id/this.numOfBlocksOnMap;
		int center_x = (this.blockSize*bloc_x)+(this.blockSize/2);
		int center_y = (this.blockSize*bloc_y)+(this.blockSize/2);
		
		//for debug
		if(debugmode == true){
			System.out.println("nowblock_id="+nowblock_id);
			System.out.println("bloc_y="+bloc_y);
			System.out.println("bloc_x="+bloc_x);
			System.out.println("center_x="+center_x);
			System.out.println("center_y="+center_y);
			System.out.println("now_x="+newLoc.getX());
			System.out.println("now_y="+newLoc.getY());
			System.out.println("chgBlock_D_threshold="+this.chgBlock_D_threshold);
			System.out.println("chgBlock_T_threshold="+this.chgBlock_T_threshold);
		}
		
		if (oldblock_id != nowblock_id){
			if(this.userMonitor.getCrossFlag() == false){
				//toggle the cross block flag
				this.userMonitor.setCrossFlag(true);
				this.userMonitor.setAccessTime(this.currentsimulationTime);
				this.userMonitor.setBlockCenterX(center_x);
				this.userMonitor.setBlockCenterY(center_y);
				return false;
			}else{
				//monitor expire and still no block change
				this.userMonitor.setAccessTime(this.currentsimulationTime);
				this.userMonitor.setBlockCenterX(center_x);
				this.userMonitor.setBlockCenterY(center_y);
				return false;
			}
		}else{
			if(this.userMonitor.getCrossFlag() == false){
				//still no change block
				//do nothing
				return false;
			}else{
				//check the time and distance rule
				long time_diff = this.currentsimulationTime - this.userMonitor.getAccessTime();
				int distance_diffX = newLoc.getX()-this.userMonitor.getBlockCenterX();
				int distance_diffY = newLoc.getY()-this.userMonitor.getBlockCenterY();
				int distance = (distance_diffX*distance_diffX) + (distance_diffY*distance_diffY);
				if((distance <= this.chgBlock_D_threshold) && (time_diff >= this.chgBlock_T_threshold)){
					this.userMonitor.setCrossFlag(false);
					return true;
				}else{
					return false;
				}
			}
		}

	}
	
	/**
	 * generate a random time for user staying
	 * 
	 * @return long
	 */
	private long getPauseTime() {
		return pauseLow + rnd.nextInt((pauseHigh - pauseLow + 1));
	}
	
	/**
	 * recompute the element's priority  
	 * @param now //user's current location
	 * @return null
	 */
	private void reComputeElementPriority(Location now){
		int nowblockid = user.getBlockID(now);
		List<CacheElement> currentTcache = user.getTcache();
		List<CacheElement> currentScache = user.getScache();
		List<FeatureFile> curentlist = this.TargetDB.searchDB(nowblockid);
		int maxSFreq=0;
		int maxAFreq=0;
		//get the max frequency in current geo-block
		for(FeatureFile f : curentlist){
			int t_type = f.getFileType();
			int t_freq1 = this.TargetDB.getOccurredFreqTable().get(t_type);
			if(maxSFreq < t_freq1){
				maxSFreq = t_freq1;
			}
		}
		//
		for(FeatureFile f : curentlist){
			int t_type = f.getFileType();
			int t_freq = this.TargetDB.getAccessFreqTable().get(t_type);
			if(maxAFreq < t_freq){
				maxAFreq = t_freq;
			}
		}
		//re-compute tCache priority
		Iterator<CacheElement> tIt = currentTcache.iterator();
		//for every tCache element
		while(tIt.hasNext()){
			CacheElement old = tIt.next();
			int type = old.getFileType();
			//System.out.println("T oldpri:"+old.getPriority());
			//check contain and find closest one 
			boolean countain = false;
			int mindistance=this.blockSize*this.blockSize;
			int newX=-1;
			int newY=-1;
			
			for(int i=0;i<curentlist.size();i++){
				if(curentlist.get(i).getFileType() == type){
					countain = true;
					int xdiff = curentlist.get(i).getFileLocationX()- now.getX();
					int ydiff = curentlist.get(i).getFileLocationY()- now.getY();
					int distance = xdiff*xdiff+ydiff*ydiff;
					if(distance<mindistance){
						mindistance=distance;
						newX=curentlist.get(i).getFileLocationX();
						newY=curentlist.get(i).getFileLocationY();
					}
				}
			}
			//
			if(countain == true){
				double newpri = computePriority(type, newX, newY, maxSFreq, maxAFreq);
				//System.out.println("T newpri:"+newpri);
				old.setPriority(newpri);
			}else{
				old.setPriority(3);
			}
		}
		
		//re-compute sCache priority
		Iterator<CacheElement> sIt = currentScache.iterator();
		//for every sCache element
		while(sIt.hasNext()){
			CacheElement old = sIt.next();
			int type = old.getFileType();
			//System.out.println("S oldpri:"+old.getPriority());
			//check contain and find closest one 
			boolean countain = false;
			int mindistance=this.blockSize*this.blockSize;
			int newX=-1;
			int newY=-1;
			for(int i=0;i<curentlist.size();i++){
				if(curentlist.get(i).getFileType() == type){
					countain = true;
					int xdiff = curentlist.get(i).getFileLocationX()- now.getX();
					int ydiff = curentlist.get(i).getFileLocationY()- now.getY();
					int distance = xdiff*xdiff+ydiff*ydiff;
					if(distance<mindistance){
						mindistance=distance;
						newX=curentlist.get(i).getFileLocationX();
						newY=curentlist.get(i).getFileLocationY();
					}
				}
			}
			//
			if(countain == true){
				double newpri = computePriority(type, newX, newY, maxSFreq, maxAFreq);
				//System.out.println("S newpri:"+newpri);
				old.setPriority(newpri);
			}else{
				old.setPriority(3);
			}
		}
		
	}
	
	/**
	 * select next user
	 * 
	 * @return
	 */
	private int nextAvailableUser() {
		long minfinish = simulationLength; //assume all server in the finish time
		int minfinishindex = rnd.nextInt(numUsers) + 1; //给下班前到达但在下班后得到服务的客户提供一个随机的出纳窗口编号

		for (int i = 1; i <= numUsers; i++) { // 找一个可用的窗口
			if (ustat[i].totalUserWait < minfinish) { // 寻找窗口空闲时间最小者
				minfinish = ustat[i].totalUserWait;
				minfinishindex = i;
			}
		}
		return minfinishindex; //return minimum
	}

	/**
	 * the main method of Simulation class
	 * 
	 * @throws PQueueException
	 */
	public void runSimulation() throws PQueueException {
		while (!pq.isEmpty()) {
			//check Simulation End and leave while loop
			if(simEndFlag == true) break;
			//choose the height priority event form queue
			Event e = pq.delPQueue();
			currentsimulationTime = e.getTime();
			/* ###################################### */
			//print the event
			/*
			System.out.println("User" + e.getUserID() + " at Time:" + e.getTime()
					+ "ms " + e.getEType());
			*/
			/* ###################################### */
			//for debug
			/*
			System.out.println("######################################");
			System.out.println("CustomerID = " + e.getUserID());
			System.out.println("WaitTime = " + e.getWaitTime());
			System.out.println("ServiceTime = " + e.getServiceTime());
			System.out.println("######################################");
			*/
			switch (e.eType){
				case UpdateLocation:
					//generate a series of move events by new path
					Path nextPath = user.getPath();
					//compute event occurrence time 
					this.pauseTime = getPauseTime();  //generate a random user stay time
					//need to modify (Unit: grid/s)
					//this.userSpeed = 100;
					
					//for debug
					if(debugmode == true){
						System.out.println("/* #############  UpdateLocation ##################### */");
						System.out.println("The new Path is:");
						nextPath.printLoc();
						System.out.println("Current Location is:");
						System.out.println(user.getCurrentLocation().toString());
					}
					
					//transform path to move events
					List<Event> nextMoveEvents = pathToEvents(currentsimulationTime,userSpeed,nextPath);
					
					//insert into queue
					for(int i=0;i<nextMoveEvents.size();i++){
					 Event temp = nextMoveEvents.get(i);
					 pq.enPQueue(temp);
					}
					break;
				case Move:
					//for debug
					if(debugmode == true){
						System.out.println("/* #############  MOVE ##################### */");
					}
					/*Update User's Location*/
					Location oldLoc = user.getCurrentLocation();
					int moveX = e.getXMovement();
					int moveY = e.getYMovement();
					int newLocX = oldLoc.getX()+moveX;
					int newLocY = oldLoc.getY()+moveY;
					Location newLoc = new Location(newLocX, newLocY);
					user.setLastLocation(oldLoc);
					user.setCurrentLocation(newLoc);
					//for debug
					if(debugmode == true){
						System.out.println("("+newLocX+","+newLocY+")");
					}
					/*********Update element's priority in cache by current location******************************************/
					reComputeElementPriority(newLoc);
					
					/************check ChangeBlock and pre-loading ************************************************/
					//check change block?
					if(usedAlgorithm == 1 && changeBlockWithBlockMonitor(oldLoc, newLoc) == true ){
						//generate change block event
						Event chanBlockEvent= new Event(currentsimulationTime, EventType.CHANGE_BLOCK, 1, 0, 0, 0, 0, 0, 0, 0);
						//insert change block event
						pq.enPQueue(chanBlockEvent);
					}else if(usedAlgorithm == 2 && changeBlock(oldLoc, newLoc) == true ){
						//generate change block event
						Event chanBlockEvent= new Event(currentsimulationTime, EventType.CHANGE_BLOCK, 1, 0, 0, 0, 0, 0, 0, 0);
						//insert change block event
						pq.enPQueue(chanBlockEvent);
					}else{
						//do nothing
						//System.out.println("In Move event do nothing");
					}
					break;
				case QUERY_AR:
					//generate Access AR Event
					Location nowLoc = user.getCurrentLocation();
					int block_id = user.getBlockID(nowLoc);
					List<FeatureFile> wantAccess = accessARTarget(block_id, nowLoc);
					//transform to access AR events
					List<Event> nextAccessEvents = idToEvents(currentsimulationTime, wantAccess);
					for(int i=0; i<nextAccessEvents.size(); i++){
						Event temp = nextAccessEvents.get(i);
						pq.enPQueue(temp);
					}
					//insert next QUERY_AR event
					long nextaccesstime = user.getPoissonInterArrivalTime(this.currentsimulationTime);
					Event nextAccessAREvent = new Event(nextaccesstime, EventType.QUERY_AR, 1, 0, 0, 0, 0, 0, 0, 0);
					pq.enPQueue(nextAccessAREvent);
					break;
				case ACCESS_AR:
					int file_type = e.getFileType();
					int file_locx = e.getFileLocationX();
					int file_locy = e.getFileLocationY();
					//update user log
					//by different algorithm
					if(usedAlgorithm == 0){
						//vuforia
						if(debugmode == true){
							System.out.print("#############  ACCESS_AR_BruteForce ###### ");
							System.out.println("("+file_type+")");
						}
						ustat[1].totalRequestCount = ustat[1].totalRequestCount+1;           //the request times =preload+cacheMiss
						ustat[1].totalTrafficLoad = ustat[1].totalTrafficLoad+14000;         //data traffic load(Unit:byte) 13000(U)+1000(D)
						ustat[1].totalPowerConsumption = ustat[1].totalPowerConsumption+14;  //energy consumption (Unit:W) 13(U)+1(D)  assume 1000byte/1W
						//need to modify
						ustat[1].totalUserWait=ustat[1].totalUserWait+1000; 	             //the AR access delay(assume RemoteAccess RoundTrip Time is 1000 ms)
					}else if(usedAlgorithm == 1 || usedAlgorithm == 2){
						//1: our sechema
						//2: our schema without Block monitor
						if(debugmode == true){
							if(usedAlgorithm == 1){System.out.print("#############  ACCESS_AR_OurMethod ####### ");}
							if(usedAlgorithm == 2){System.out.print("#############  ACCESS_AR_OurMethod without Block Monitor####### ");}
							System.out.println("("+file_type+")");
						}
						//check local cache?
						List<CacheElement> sCache = user.getScache();
						List<CacheElement> tCache = user.getTcache();
						//update user log
						//generate Cache_Miss or Cache_Hit event
						boolean hitflag = false;
						for(CacheElement e1 : sCache){
							//generate Cache hit Event
							if(e1.getFileType() == file_type){
								hitflag = true;
								long new_accesstime = e.getTime();
								Event hitEvent = new Event(new_accesstime, EventType.SCache_Hit, 1, 0, 0, 0, 0, file_type, file_locx, file_locy);
								pq.enPQueue(hitEvent);
								break;
							}
						}
						for(CacheElement e2 : tCache){
							//generate T-Cache hit Event
							if(e2.getFileType() == file_type){
								hitflag = true;
								long new_accesstime = e.getTime();
								Event hitEvent = new Event(new_accesstime, EventType.TCache_Hit, 1, 0, 0, 0, 0, file_type, file_locx, file_locy);
								pq.enPQueue(hitEvent);
								break;
							}
						}
						//generate Cache Miss event
						if(hitflag == false){
							long new_accesstime = e.getTime();
							Event missEvent = new Event(new_accesstime, EventType.Cache_Miss, 1, 0, 0, 0, 0, file_type, file_locx, file_locy);
							pq.enPQueue(missEvent);
						}
					}else if(usedAlgorithm == 3){
						//3: our schema without Pre-load & Block monitor 
						if(debugmode == true){
							System.out.print("#############  ACCESS_AR (used PRI) ###### ");
							System.out.println("("+file_type+")");
						}
						//check local cache?
						List<CacheElement> tCache = user.getTcache();
						boolean thitflag = false;
						for(CacheElement obj : tCache){
							//generate T-Cache hit Event
							if(obj.getFileType() == file_type){
								thitflag = true;
								long new_accesstime = e.getTime();
								Event hitEvent = new Event(new_accesstime, EventType.TCache_Hit, 1, 0, 0, 0, 0, file_type, file_locx, file_locy);
								pq.enPQueue(hitEvent);
								break;
							}
						}
						//generate Cache Miss event
						if(thitflag == false){
							long new_accesstime = e.getTime();
							Event missEvent = new Event(new_accesstime, EventType.Cache_Miss, 1, 0, 0, 0, 0, file_type, file_locx, file_locy);
							pq.enPQueue(missEvent);
						}
					}else if(usedAlgorithm == 4){
						//4: LRU
						if(debugmode == true){
							System.out.print("#############  ACCESS_AR (LRU) ###### ");
							System.out.println("("+file_type+")");
						}
						//check local cache?
						List<CacheElement> tCache = user.getTcache();
						//check Cache_Miss or Cache_Hit
						boolean thitflag = false;
						for(CacheElement obj : tCache){
							//generate T-Cache hit Event
							if(obj.getFileType() == file_type){
								thitflag = true;
								long new_accesstime = e.getTime();
								Event hitEvent = new Event(new_accesstime, EventType.TCache_Hit, 1, 0, 0, 0, 0, file_type, file_locx, file_locy);
								pq.enPQueue(hitEvent);
								break;
							}
						}
						//generate Cache Miss event
						if(thitflag == false){
							long new_accesstime = e.getTime();
							Event missEvent = new Event(new_accesstime, EventType.Cache_Miss, 1, 0, 0, 0, 0, file_type, file_locx ,file_locy);
							pq.enPQueue(missEvent);
						}
					}else{
						System.out.print("#############  ACCESS_AR UnImplement Algorithm ####### ");
					}
					break;
				case Cache_Miss:
					int ftype = e.getFileType();
					int filelocx = e.getFileLocationX();
					int filelocy = e.getFileLocationY();
					//for debug
					if(debugmode == true){
						System.out.print("#############  Cache_Miss ####### ");
						System.out.println("("+ftype+")");
						user.printCacheContent();
					}
					
					List<CacheElement> sCache = user.getScache();
					List<CacheElement> tCache = user.getTcache();
					/*******Create cache element*******/
					//user's location
					//Location now = this.user.getCurrentLocation();
					Location fileLoc = new Location(filelocx, filelocy);
					int blockid = this.user.getBlockID(fileLoc);
					List<FeatureFile> flist = this.TargetDB.searchDB(blockid);
					int maxSFreq=0;
					int maxAFreq=0;
					if(this.debugmode == true){
						System.out.println("X:"+filelocx+" Y:"+filelocy);
						System.out.println(flist.size()+" files in this block:"+blockid);
					}
					//get the max freq in neighbor list
					for(FeatureFile f : flist){
						int t_type = f.getFileType();
						int t_freq1 = this.TargetDB.getOccurredFreqTable().get(t_type);
						if(maxSFreq < t_freq1){
							maxSFreq = t_freq1;
						}
					}
					//
					for(FeatureFile f : flist){
						int t_type = f.getFileType();
						int t_freq = this.TargetDB.getAccessFreqTable().get(t_type);
						if(maxAFreq < t_freq){
							maxAFreq = t_freq;
						}
					}
					double temp_pri = computePriority(ftype, filelocx, filelocy, maxSFreq, maxAFreq);
					CacheElement download = new CacheElement(ftype, temp_pri, e.getTime());
					
					//Request+Response update user log
					ustat[1].cacheMissCount = ustat[1].cacheMissCount +1;
					ustat[1].totalUserWait = ustat[1].totalUserWait+1100;  //assume miss time cost=1s+0.1s
					ustat[1].totalRequestCount = ustat[1].totalRequestCount+1;
					ustat[1].totalTrafficLoad = ustat[1].totalTrafficLoad+14000; //13k+1k
					ustat[1].totalPowerConsumption = ustat[1].totalPowerConsumption+14; // 14k
					//updateLocal Cache
					if(this.usedAlgorithm ==1 || this.usedAlgorithm ==2){
						int sumOfelement = tCache.size()+sCache.size();
						int cacheCapacity = 2*this.cache_size;
						//check free space or not?
						if( sumOfelement < cacheCapacity){
							//insert the new element to T-Cache
							tCache.add(download);
						}else{//full
							if(tCache.size() > this.tSizeThreshold){
								int removeIndex = -1;
								/*
								//remove the lowest priority element in T-Cache
								double lowerpri=-1;
								for(int i=0; i<tCache.size(); i++){
									double temppri = tCache.get(i).getPriority();
									if(lowerpri < temppri){
										removeIndex=i;
										//need to modify
										lowerpri = temppri;
									}

								}*/
								//remove the oldest element in T-Cache
								long maxage = -1;
								for(int i=0; i<tCache.size(); i++){
									long tempage = this.currentsimulationTime - tCache.get(i).getAccessTime();
									if(tempage > maxage){
										maxage = tempage;
										removeIndex =i;
									}
								}
								if(removeIndex >= 0 ){
									tCache.remove(removeIndex);
									//insert the new element to T-Cache
									tCache.add(download);
								}else{
									System.out.println("Error in remove the lowest priority element at T-Cache");
								}
							}else{
								//remove the lowest priority element in S-Cache
								double lowerpri=-1;
								int removeIndex = -1;
								for(int i=0;i<sCache.size();i++){
									double pri = sCache.get(i).getPriority();
									if(pri > lowerpri){
										removeIndex=i;
										//need to modify
										lowerpri = pri;
									}
								}
								if(removeIndex >= 0 ){
									sCache.remove(removeIndex);
									//insert the new element to T-Cache
									tCache.add(download);
								}else{
									System.out.println("Error in remove the lowest priority element at S-Cache");
								}
							}
						}
					}else if(this.usedAlgorithm == 3){

						//remove the lowest priority element in  T-Cache
						if(tCache.size() >= this.cache_size){
							double lowerpri=-1;
							int removeIndex = -1;
							for(int i=0;i<tCache.size();i++){
								double pri = tCache.get(i).getPriority();
								if(pri > lowerpri){
									removeIndex=i;
									lowerpri = pri;
								}
							}
							if(removeIndex >= 0 ) {tCache.remove(removeIndex);}
							//insert the new element to T-Cache
							//
							tCache.add(download);
						}else{
							//insert the new element to T-Cache
							tCache.add(download);
						}
						//remove the oldest element in T-Cache
						
					}else if(this.usedAlgorithm == 4){
						if(tCache.size() >= this.cache_size){
							//remove the oldest element in T-Cache
							long oldage=0;
							int removeIndex = -1;
							for(int i=0; i<tCache.size(); i++){
								long age = this.currentsimulationTime-tCache.get(i).getAccessTime();
								if(oldage < age){
									removeIndex=i;
									oldage = age;
								}
							}
							if(removeIndex >= 0 ){tCache.remove(removeIndex);}
							//insert the new element to T-Cache
							tCache.add(download);
						}else{
							tCache.add(download);
						}
					}else{
						System.out.println("Error in Cahce Miss Event: Unimplement algorithm");
					}
					break;
				case TCache_Hit:
					int f_type = e.getFileType();
					//for debug
					if(debugmode == true){
						System.out.print("#############  T Cache_Hit ####### ");
						System.out.println("("+f_type+")");
					}
					
					List<CacheElement> t_Cache_temp = user.getTcache();
					for(CacheElement element1 : t_Cache_temp){
						if(element1.getFileType() == f_type){
							t_Cache_temp.remove(element1);
							element1.setAccessTime(e.getTime());
							t_Cache_temp.add(element1);
							break;
						}
					}//for loop
					
					//for debug
					if(debugmode == true){
						user.printCacheContent();
					}
					//update user log
					ustat[1].cacheHitCount = ustat[1].cacheHitCount +1;
					//need to modify
					ustat[1].totalUserWait = ustat[1].totalUserWait+100;  //assume local access response time=0.1s
					break;
				case SCache_Hit:
					int f_type1 = e.getFileType();
					if(debugmode == true){
						System.out.print("#############  S Cache_Hit ####### ");
						System.out.println("("+f_type1+")");
					}
					List<CacheElement> s_Cache1 = user.getScache();
					List<CacheElement> t_Cache1 = user.getTcache();
					for(CacheElement element1 : s_Cache1){
						if(element1.getFileType() == f_type1){
							s_Cache1.remove(element1);
							element1.setAccessTime(e.getTime());
							t_Cache1.add(element1);
							break;
						}
					}//for loop
					
					//for debug
					if(debugmode == true){
						user.printCacheContent();
					}
					//update user log
					ustat[1].cacheHitCount = ustat[1].cacheHitCount +1;
					//need to modify
					ustat[1].totalUserWait = ustat[1].totalUserWait+100;  //assume recognized at local only need 0.1s
					break;
				case CHANGE_BLOCK:
					List<CacheElement> currentTcache = user.getTcache();
					List<CacheElement> currentScache = user.getScache();
					//check available T-cache capacity
					boolean removeTflag = false; 
					int available_T_CacheSize = user.checkAgeExpiredSizeInT(ageThreshold, currentsimulationTime);
					if((currentTcache.size()-available_T_CacheSize) < this.tSizeThreshold){
						removeTflag = false;
					}else{
						removeTflag = true;
					}
					//send request
					//available cache space in local cache(available cache space =Total Local Cache Capacity- Size_T)
					int available_S_CacheSize = 0;
					if(removeTflag == true){
						available_S_CacheSize=cache_size-this.tSizeThreshold+cache_size;
					}else{
						available_S_CacheSize=cache_size-available_T_CacheSize+cache_size;
					}
					//update user log
					int numCacheElement = currentTcache.size()+currentScache.size();
					double chgBlockReqMessageSize = 8+4*numCacheElement;  //(4 byte)location_x+(4 byte)location_y+4*numCacheElement
					int chgBlockReqPowerConsum = (int)Math.round(chgBlockReqMessageSize/1000);   //assume 1000byte/1W
					ustat[1].totalRequestCount=ustat[1].totalRequestCount+1;
					ustat[1].totalTrafficLoad = ustat[1].totalTrafficLoad+(int)chgBlockReqMessageSize;
					ustat[1].totalPowerConsumption = ustat[1].totalPowerConsumption+chgBlockReqPowerConsum;
					if(debugmode == true){
						System.out.println("------Request Resource Consumption--------------");
						System.out.println("Request TrafficLoad:"+chgBlockReqMessageSize);
					}
					//get feature files in the Neighbor blocks and if there are the same file type,keep the one which close user
					List<FeatureFile> filesInNeighbor = getFeatureFileInNeighbor(user.getCurrentLocation(), user.getLastLocation());
					if(filesInNeighbor.size() == 0 ){
						System.out.println("filesInNeighbor null ERROR!!!!");
						break;
					}
					//re-compute neighbor's file priority
					List<CacheElement> orderedPriFileinNeighbor = getSortCacheElementInNeighbor(filesInNeighbor);
					if(orderedPriFileinNeighbor.size() == 0){
						System.out.println("orderedPriFileinNeighbor null ERROR!!!!");
						break;
					}
					//compare reply and available and remove elements in cache
					if(orderedPriFileinNeighbor.size() >= available_S_CacheSize){
						//can downloaded size >= available size 
						user.removeAgeExpiredInT(ageThreshold, this.currentsimulationTime);
						user.removeOldestInT(tSizeThreshold, this.currentsimulationTime);
						//decide which files need to be download? which files need to be keep
						List<CacheElement> downloadFromDB = new ArrayList<CacheElement>();
						List<CacheElement> keepInCache = new ArrayList<CacheElement>();
						int index=0; //already have download
						int p = 0;  //pointer the wanted download
						while(index < available_S_CacheSize){
							if(p >= orderedPriFileinNeighbor.size()){
								break;
							}
							boolean addflag = false;
							boolean addflag2 = false;
							int type1 = orderedPriFileinNeighbor.get(p).getFileType();
							//check the new element is already in SCache or not
							if(currentScache.size() == 0){addflag = true;}
							for(int i=0;i<currentScache.size();i++){
								if(type1 == currentScache.get(i).getFileType()){
									CacheElement keep = currentScache.get(i);
									keepInCache.add(keep);
									addflag = false;
									break;
								}else{
									if(i == (currentScache.size()-1)){
										addflag = true;
									}
								}

							}
							//check the new element is already in TCache or not
							if(currentTcache.size() == 0){addflag2 = true;}
							for(int i=0;i<currentTcache.size();i++){
								if(type1 == currentTcache.get(i).getFileType()){
									addflag2 = false;
									break;
								}else{
									if(i == (currentTcache.size()-1)){
										addflag2 = true;
									}
								}

							}
							if(addflag == false){
								p=p+1;
								index=index+1;
								continue;
							}else if(addflag2 == false){
								p=p+1;
								continue;
							}else{
								CacheElement tempCache_E = orderedPriFileinNeighbor.get(p); 
								downloadFromDB.add(tempCache_E);
								p=p+1;
								index=index+1;
							}
						}//while
						
						//update user log
						int powerSize = downloadFromDB.size()*14;     //assume feature file 13kbyte, augmented information 1kbyte
						ustat[1].totalTrafficLoad = ustat[1].totalTrafficLoad+(powerSize*1000);   //1000byte/1W
						ustat[1].totalPowerConsumption = ustat[1].totalPowerConsumption+(powerSize);
						if(debugmode == true){
							System.out.println("------Download Resource Consumption--------------");
							System.out.println("TrafficLoad:"+powerSize*1000);
						}
						//update User Cache
						//1.remove element
						//need to modify
						currentScache.clear();
						//2.add the keeped elements and new elements
						for (int i=0;i< keepInCache.size();i++){
							CacheElement ele = keepInCache.get(i);
							currentScache.add(ele);
						}
						if(debugmode == true){
							System.out.println("------Content download From DB--------------");
						}
						for (int i=0;i< downloadFromDB.size();i++){
							CacheElement ele = downloadFromDB.get(i);
							if(currentScache.size() < this.sSizeThreshold){
								currentScache.add(ele);
								if(debugmode == true){
									System.out.println(i+"-Type:"+ele.getFileType()+" add to Scache");
								}
							}else if(currentTcache.size() < this.tSizeThreshold){
								currentScache.add(ele);
								if(debugmode == true){
									System.out.println(i+"-Type:"+ele.getFileType()+" add to Scache");
								}
							}else{
								System.out.println("CHANGE_BLOCK Error in Simulation at Update user cache");
							}
						}//for loop
					}else{
						//can downloaded size < available size 
						if(orderedPriFileinNeighbor.size() < this.sSizeThreshold){
							//can downloaded size < S-Cache available size
							//decide which files need to be download? which files need to be keep
							List<CacheElement> downloadFromDB = new ArrayList<CacheElement>();
							List<CacheElement> keepInCache = new ArrayList<CacheElement>();
							for(int i=0;i<orderedPriFileinNeighbor.size();i++){
								//check S-Cache
								for(int j=0; j<currentScache.size(); j++){
									if(orderedPriFileinNeighbor.get(i).getFileType() == currentScache.get(j).getFileType()){
										keepInCache.add(orderedPriFileinNeighbor.get(i));
									}else{
										//check T-Cache
										for(int k=0;k<currentTcache.size(); k++){
											if(orderedPriFileinNeighbor.get(i).getFileType() == currentScache.get(k).getFileType()){
												break;
											}else{
												downloadFromDB.add(orderedPriFileinNeighbor.get(i));
											}
										}
									}
								}
							}
							//update user log
							int powerSize = downloadFromDB.size()*14;     //assume feature file 13kbyte, augmented information 1kbyte
							ustat[1].totalTrafficLoad = ustat[1].totalTrafficLoad+(powerSize*1000);   //1000byte/1W
							ustat[1].totalPowerConsumption = ustat[1].totalPowerConsumption+(powerSize);
							if(debugmode == true){
								System.out.println("------Download Resource Consumption--------------");
								System.out.println("TrafficLoad:"+powerSize*1000);
							}
							//update User Cache
							//1.remove element
							//need to modify
							user.removeSCachebySize(orderedPriFileinNeighbor.size());
							//2.add the keeped elements and new elements
							for (int i=0;i< keepInCache.size();i++){
								CacheElement ele = keepInCache.get(i);
								currentScache.add(ele);
							}
							if(debugmode == true){
								System.out.println("------Content download From DB--------------");
							}
							for (int i=0;i< downloadFromDB.size();i++){
								CacheElement ele = downloadFromDB.get(i);
								if(currentScache.size() < this.sSizeThreshold){
									currentScache.add(ele);
									if(debugmode == true){
										System.out.println(i+"-Type:"+ele.getFileType()+" add to Scache");
									}
								}else if(currentTcache.size() < this.tSizeThreshold){
									currentScache.add(ele);
									if(debugmode == true){
										System.out.println(i+"-Type:"+ele.getFileType()+" add to Scache");
									}
								}else{
									System.out.println("CHANGE_BLOCK Error in Simulation at Update user cache");
								}
							}//for loop
						}else{
							int removeT = orderedPriFileinNeighbor.size()-this.sSizeThreshold;
							user.removeAgeExpiredInTbySize(ageThreshold, this.currentsimulationTime, removeT);
							//decide which files need to be download? which files need to be keep
							List<CacheElement> downloadFromDB = new ArrayList<CacheElement>();
							List<CacheElement> keepInCache = new ArrayList<CacheElement>();
							for(int i=0;i<orderedPriFileinNeighbor.size();i++){
								//check S-Cache
								for(int j=0; j<currentScache.size(); j++){
									if(orderedPriFileinNeighbor.get(i).getFileType() == currentScache.get(j).getFileType()){
										keepInCache.add(orderedPriFileinNeighbor.get(i));
									}else{
										//check T-Cache
										for(int k=0;k<currentTcache.size(); k++){
											if(orderedPriFileinNeighbor.get(i).getFileType() == currentScache.get(k).getFileType()){
												break;
											}else{
												downloadFromDB.add(orderedPriFileinNeighbor.get(i));
											}
										}
									}
								}
							}
							//update user log
							int powerSize = downloadFromDB.size()*14;     //assume feature file 13kbyte, augmented information 1kbyte
							ustat[1].totalTrafficLoad = ustat[1].totalTrafficLoad+(powerSize*1000);   //1000byte/1W
							ustat[1].totalPowerConsumption = ustat[1].totalPowerConsumption+(powerSize);
							if(debugmode == true){
								System.out.println("------Download Resource Consumption--------------");
								System.out.println("TrafficLoad:"+powerSize*1000);
							}
							//update User Cache
							//1.remove element
							//need to modify
							currentScache.clear();
							//2.add the keeped elements and new elements
							for (int i=0;i< keepInCache.size();i++){
								CacheElement ele = keepInCache.get(i);
								currentScache.add(ele);
							}
							if(debugmode == true){
								System.out.println("------Content download From DB--------------");
							}
							for (int i=0;i< downloadFromDB.size();i++){
								CacheElement ele = downloadFromDB.get(i);
								if(currentScache.size() < this.sSizeThreshold){
									currentScache.add(ele);
									if(debugmode == true){
										System.out.println(i+"-Type:"+ele.getFileType()+" add to Scache");
									}
								}else if(currentTcache.size() < this.tSizeThreshold){
									currentScache.add(ele);
									if(debugmode == true){
										System.out.println(i+"-Type:"+ele.getFileType()+" add to Scache");
									}
								}else{
									System.out.println("CHANGE_BLOCK Error in Simulation at Update user cache");
								}
							}//for loop
						}//download size > S-Cache available size
					}//download size < available size
					if(debugmode == true){
						//for debug
						user.printCacheContent();
					}
					break;
				case SIM_END:
					System.out.println("Simulation End !!!!");
					simEndFlag = true;
					break;
				default:
					System.out.println("Event Type Error");
			}//switch
		}//while loop
	}

	/**
	 * Print the Simulation Result
	 */
	public void printSimulationResults() {
		int cumUsers = numUsers;  //the number of user in this simulation 
		/*
		int cumWait = 0; // the waiting time of all users
		for (int i = 1; i <= numUsers; i++) {
			cumWait += ustat[i].totalCustomerWait;
		}
		*/
		
		System.out.println();
		System.out.println("**************************** Simulation Summary ******************************");
		System.out.println("Simulation Time:" + this.simulationLength);
		System.out.println("Num of User:"+ cumUsers);
		System.out.println("User Speed:"+ this.userSpeed);
		System.out.println("Age Threshold:"+ this.ageThreshold);
		System.out.println("Map edge Size:"+ this.mapSize);
		System.out.println("Block edge Size:"+ this.blockSize);
		System.out.println("Num of Blocks On Map:"+ this.numOfBlocksOnMap*this.numOfBlocksOnMap);
		System.out.println("Num of Files On Map:"+ this.totalFilesOnMap);
		System.out.println("Num of File Kinds:"+ this.numOfFileKinds);
		System.out.println("S cache Size:"+ this.cache_size);
		System.out.println("T cache Size:"+ this.cache_size);
		System.out.println("Used Algorithm:"+this.usedAlgorithm);                       //the used algorithm
		System.out.println("Generate File Seed:"+this.generateFileSeed);              	//the random seed of file distribution
		System.out.println("User Behaveior Seed:"+this.userBehaveiorSeed);              //the random seed of user activity
		//print out every User's log
		for (int i = 1; i <= numUsers; i++) {
			System.out.println("Num of Request:"+ustat[i].totalRequestCount);           //the number of request from client to server =preload+cacheMiss
			System.out.println("Num of Cache Miss:"+ustat[i].cacheMissCount);           //the number of Cache Miss
			System.out.println("Num of Cache Hit:"+ustat[i].cacheHitCount);	            //the number of Cache Hit
			System.out.println("Data Traffic(Byte):"+ustat[i].totalTrafficLoad);        //network traffic load  (Unit:byte)
			System.out.println("Power Consumption(W):"+ustat[i].totalPowerConsumption); //transmission power consumption (Unit:W) assume 1W per 1000byte
			System.out.println("Accumented Response Time(ms):"+ustat[i].totalUserWait); //access AR response delay (Unit:ms)
		}

	}
}