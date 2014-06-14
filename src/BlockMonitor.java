/**
 * TODO Block Monitor Class
 * 
 * @since jdk1.5 or upper
 * @author CCWU
 * @version 2.0
 * @date 2013.05.29
 * 
 */

public class BlockMonitor {
	
	private boolean crossBlock;
	private long accessTime;
	private int block_center_x;
	private int block_center_y;
	
	/**
	 * construct method
	 * 
	 */
	public BlockMonitor(){
		this.crossBlock = false;
		this.accessTime = 0;
		this.block_center_x=0;
		this.block_center_y=0;
	}
	
	/**
	 * get change block flag
	 * 
	 * @return boolean
	 * 
	 */
	public boolean getCrossFlag(){
		return this.crossBlock;
	}
	
	/**
	 * set change block flag
	 * 
	 * @param _flag
	 * 
	 */
	public void setCrossFlag(boolean _flag){
		this.crossBlock = _flag;
	}
	
	
	/**
	 * get access time stamp
	 * 
	 * @return long
	 * 
	 */
	public long getAccessTime(){
		return this.accessTime;
	}
	
	/**
	 * set access time stamp
	 * 
	 * @param _time
	 * 
	 */
	public void setAccessTime(long _time){
		this.accessTime = _time;
	}
	
	/**
	 * get block center location_x
	 * 
	 * @return int
	 * 
	 */
	public int getBlockCenterX(){
		return this.block_center_x;
	}
	
	/**
	 * set block center location_x
	 * 
	 * @param _x
	 * 
	 */
	public void setBlockCenterX(int _x){
		this.block_center_x = _x;
	}
	
	/**
	 * get block center location_y
	 * 
	 * @return int
	 * 
	 */
	public int getBlockCenterY(){
		return this.block_center_y;
	}
	
	/**
	 * set block center location_y
	 * 
	 * @param _y
	 * 
	 */
	public void setBlockCenterY(int _y){
		this.block_center_y = _y;
	}
	
}
