/**
 * TODO GeoBlock class
 * 
 * @since jdk1.5 or upper
 * @author CCWU
 * @version 2.0
 * @date 2013.05.29
 * 
 */

import java.util.ArrayList;
import java.util.List;


public class GeoBlock {
	
	private int blockID; //the ID of GeoBlock
	private int length;  //the size of GeoBlock
	private List<FeatureFile> featureFiles;

	/**
	 * construct method
	 * @param _ID
	 * @param _length
	 * 
	 */
	public GeoBlock(int _ID, int _length){
		
		this.blockID = _ID;
		this.length = _length;
		this.featureFiles =  new ArrayList<FeatureFile>();
		 
		//Linear Congruential Formula
	}
	
	public int getGeoBlockID(){
		
		return this.blockID;
	}
	
	/**
	 * set Feature Files
	 * @param _Files
	 * 
	 */
	public boolean setFeatureFiles(List<FeatureFile> _Files){
		if(_Files != null){
			this.featureFiles = _Files;
			return true;
		}else{
			return false;
		}
	}
	
	
	/**
	 * get Feature Files
	 * 
	 */
	public List<FeatureFile> getFeatureFiles(){
		
		return this.featureFiles;
	}
	
	public int getLengthOfGeoBlock(){
		
		return this.length;
	}
	
}
