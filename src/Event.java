/**
 * TODO Event class
 * 
 * @since jdk1.5 or upper
 * @author CCWU
 * @version 2.0
 * @date 2013.05.29
 * 
 */
public class Event {
	private long time; // the event's execute time 
	EventType eType;  // the event's type
	private int userID; //the user's ID = 1,2,3...
	private long waitTime;  //the user's waiting time
	private int serviceTime; //the user's serve time
	private int movementX;
	private int movementY;
	private int fileType;
	private int fileLocX;
	private int fileLocY;
	/**
	 * construct method
	 * 
	 * @param _time
	 * @param _eType
	 * @param _userID
	 * @param _waitTime
	 * @param _serviceTime
	 * @param _moveX
	 * @param _moveY
	 * @param _fType
	 * @param _fLocx
	 * @param _fLocy
	 */
	public Event(long _time, EventType _eType, int _userID, long _waitTime, int _serviceTime, int _moveX, int _moveY, int _fType, int _fLocx, int _fLocy) {
		time = _time;
		eType = _eType;
		userID = _userID;
		waitTime = _waitTime;
		serviceTime = _serviceTime;
		movementX = _moveX;
		movementY = _moveY;
		fileType =  _fType;
		fileLocX = _fLocx;
		fileLocY = _fLocy;
	}

	/** ************ get and set method ************* */
	public long getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}

	public EventType getEType() {
		return eType;
	}

	public void setEType(EventType type) {
		eType = type;
	}

	public int getUserID() {
		return userID;
	}

	public void setUserID(int userID) {
		this.userID = userID;
	}

	public long getWaitTime() {
		return waitTime;
	}

	public void setWaitTime(int waitTime) {
		this.waitTime = waitTime;
	}

	public int getServiceTime() {
		return serviceTime;
	}

	public void setServiceTime(int serviceTime) {
		this.serviceTime = serviceTime;
	}
	
	public int getXMovement() {
		return movementX;
	}
	
	public int getYMovement() {
		return movementY;
	}
	
	public int getFileType() {
		return fileType;
	}
	
	public void setFileType(int t) {
		this.fileType = t;
	}
	
	public int getFileLocationX() {
		return fileLocX;
	}
	
	public int getFileLocationY() {
		return fileLocY;
	}
}