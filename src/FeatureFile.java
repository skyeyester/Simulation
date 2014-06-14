/**
 * TODO Feature File class
 * 
 * @since jdk1.5 or upper
 * @author CCWU
 * @version 2.0
 * @date 2013.05.29
 * 
 */

public class FeatureFile {
	private int fileType; 		// the type of feature file
	private int file_loc_x;     // the feature file's location at X
	private int file_loc_y;     // the feature file's location at Y
	
	/**
	 * construct method
	 * @param _type
	 * @param _loc_x
	 * @param _loc_y
	 * 
	 */
	public FeatureFile(int _type, int _loc_x, int _loc_y){
		 this.fileType = _type;
		 this.file_loc_x = _loc_x;
		 this.file_loc_y = _loc_y;
	}
	
	public int getFileType(){
		return this.fileType;
	}
	
	public int getFileLocationX(){
		return this.file_loc_x;
	}
	
	public int getFileLocationY(){
		return this.file_loc_y;
	}
}
