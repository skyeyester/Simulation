/**
 * TODO CacheElement class
 * 
 * @since jdk1.5 or upper
 * @author CCWU
 * @version 1.0
 * @date 2013.05.19
 * 
 */



public class CacheElement implements Comparable<CacheElement>{
	
	private int fileType; 
	private long accessTime;    // the file's latest access time
	private double priority;    // the lower value means the higher priority
								// the range from 0 to 3
	
	/**
	 * construct method
	 * @param _type
	 * @param priority the priority of feature file
	 * @param aTime the access time of feature file
	 * 
	 */
	public CacheElement(int _type, double pri, long aTime){
		// TODO Auto-generated constructor stub
		this.accessTime = aTime;
		this.priority = pri;
		this.fileType = _type;
	}
	
	/**
	 * method to get access time
	 * 
	 */
	public long getAccessTime(){
		return this.accessTime;
	}


	public void setAccessTime(long latestTime){
		this.accessTime = latestTime;
	}
	
	public double getPriority(){
		return this.priority;
	}


	public void setPriority(double pri){
		this.priority = pri;
	}
	
	public int getFileType(){
		return this.fileType;
	}


	public void setFileType(int type){
		this.fileType = type;
	}

	@Override
	public int compareTo(CacheElement arg0) {
		// TODO Auto-generated method stub
		if(this.priority < arg0.priority){
			return -1;
		}else if(this.priority > arg0.priority){
			return 1;
		}else{
			return 0;
		}
	}

		

}
