/**
 * TODO Database Class
 * 
 * @since jdk1.5 or upper
 * @author CCWU
 * @version 2.0
 * @date 2013.05.29
 * 
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Database {
	
	private int mapSize;
	private int geoBlocklength;
	private int num_blocks;
	private int num_files; //the average number of FeatureFiles per GeoBlock
	private long gen_fileSeed; //the seed of Random number to generate Feature File
	private int numOfFileKinds; //the kinds of feature file
	private Map<Integer, Integer> fileOccurredFreqTable = new HashMap<Integer, Integer>(); //the file occurred frequency table
	private Map<Integer, Integer> fileAccessProbTable = new HashMap<Integer, Integer>();   //the file access probability table (inverse normal distribution)
	private Map<Integer, Integer> fileAccessFreqTable = new HashMap<Integer, Integer>();   //the file access frequency table (normal distribution)
	private GeoBlock [][] GeoBlockSet;
	private int numOfFilesOnMap;
	//private ExponentialGenerator exponential;
	//private ZipfGenerator zipfgen;
	
	/**
	 * construct method
	 * 
	 * @param _size         //the edge length of map
	 * @param _blocklength  //the edge length of block
	 * @param _totalFiles   //the number of feature files on map
	 * @param _genfileSeed
	 * @param _kinds        //the number of feature file kinds
	 * 
	 */
	public Database(int _size, int _blocklength, int _totalFiles, long _genfileSeed, int _kinds){
		//Initialize parameter
		this.mapSize = _size;
		this.geoBlocklength = _blocklength;
		this.numOfFilesOnMap = _totalFiles;
		this.gen_fileSeed = _genfileSeed;
		this.numOfFileKinds = _kinds;
		
		num_blocks = _size/_blocklength; //assume can be divided with no remainder
		this.num_files = _totalFiles/(num_blocks*num_blocks); //assume can be divided with no remainder
		//Generate GeoBlock Set
		GeoBlockSet = new GeoBlock [num_blocks][num_blocks];

		//Initialize every GeoBlock
		for (int i=0; i < num_blocks; i++){
			for (int j=0; j < num_blocks; j++){
				//Generate a GeoBlock
				int ID = i*num_blocks+j;
				GeoBlockSet [i][j]= new GeoBlock(ID,_blocklength);
			}// for loop
		}// for loop
		
		//generate all feature files on the map
		genFeatureFileList(numOfFilesOnMap, gen_fileSeed);
	}//constructor
	
	/**
	 * get File Occurred Frequency Table in DB
	 * 
	 * */
	public Map<Integer, Integer> getOccurredFreqTable(){
		return this.fileOccurredFreqTable;
	}
	
	/**
	 * get File Access Probability Table in DB
	 * 
	 * */
	public Map<Integer, Integer> getAccessProbTable(){
		return this.fileAccessProbTable;
	}
	
	/**
	 * get File Access Frequency Table in DB
	 * 
	 * */
	public Map<Integer, Integer> getAccessFreqTable(){
		return this.fileAccessFreqTable;
	}
	
	/**
	 * * get file's Block ID from location_x and location_y
	 * 
	 * @param x location_x
	 * @param y location_y
	 * 
	 */
	private int getBlockID(int x, int y){
		
		int blockID = -1; //default= -1 means error
		int bloc_x = (x/geoBlocklength);
		int bloc_y = (y/geoBlocklength);
		blockID = bloc_x*num_blocks+bloc_y;
		if(blockID <0 ){System.out.println("Error in Class Database's method getBlockID");}
		return blockID;
	}
	
	/**
	 * add a Feature File into a specific GeoBlock
	 * 
	 * @param id GeoBlock
	 * @param f Feature File
	 * */
	private boolean addFileToGeoBlock(int id, FeatureFile f){
		if(id >=0 && f != null){
			int index_y = id%this.num_blocks;
			int index_x = id/this.num_blocks;
			List<FeatureFile> tempfeatureFiles = this.GeoBlockSet[index_x][index_y].getFeatureFiles();
			tempfeatureFiles.add(f);
			this.GeoBlockSet[index_x][index_y].setFeatureFiles(tempfeatureFiles);
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * generate Feature Files List on the Map
	 * 
	 * */
	private void genFeatureFileList(int size, long seed){
				
		/*get an random number*/
		//uniform and normal generator
		Random rnd = new Random(seed);
		//exponential generator
		//this.exponential = new ExponentialGenerator(seed);
		//zipf generator
		//this.zipfgen = new ZipfGenerator(this.mapSize,2.0);
		
		/*generate all files */
		Map<Integer, Location> cityList = new HashMap<Integer, Location>();
		//used normal distribution
		double probabilitylevel = (double)1/this.numOfFileKinds;

		//assign every kinds of file at least 1
		for (int type=0; type<this.numOfFileKinds; type++){
			
			
			//File location use uniform distribution
			int tempX = rnd.nextInt(this.mapSize);
			int tempY = rnd.nextInt(this.mapSize);
			
			/*
			//File location use zipf distribution
			int tempX = zipfgen.next();
			int tempY = zipfgen.next();
			*/
			//File location use normal distribution
			/*
			long randomX = Math.round(Math.abs(rnd.nextGaussian()*this.mapSize))+(type*this.geoBlocklength);
			int tempX = (int)(randomX%(this.mapSize));
			long randomY= Math.round(Math.abs(rnd.nextGaussian()*this.mapSize))+(type*this.geoBlocklength);
			int tempY = (int)(randomY%(this.mapSize));
			*/
			FeatureFile tempfile = new FeatureFile(type, tempX, tempY);
			//add to the GeoBlock
			int blockid = getBlockID(tempX, tempY);
			boolean resault = addFileToGeoBlock(blockid, tempfile);
			Location city = new Location(tempX, tempY);
			cityList.put(type,city);
			if(resault == false){
				System.out.println("Error in class Database's method addFileToGeoBlock");
			}

			//Update file occurred frequency table
			fileOccurredFreqTable.put(type, 1);
		}
		
		//the rest
		for(int i=0; i<size-this.numOfFileKinds; i++){
			//File type use normal distribution
			//need to modify
			/*
			//uniform distribution
			int tempFileType = rnd.nextInt(numOfFileKinds);
			*/

			//exponential distribution
			/*
			double type_tmp =  ExponentialGenerator.exp(0.05);
			int tempFileType = (int)(type_tmp%numOfFileKinds);
			*/
			
			//get a type
			double type_tmp = Math.abs(rnd.nextGaussian());
			int filetype = -1;
			if(type_tmp >1){
				double predig = Math.floor(type_tmp);
				type_tmp = (type_tmp - predig)/probabilitylevel;
				filetype = (int)Math.floor(type_tmp);
			}else{
				type_tmp = type_tmp/probabilitylevel;
				filetype = (int)Math.floor(type_tmp);
			}
			Location currentCity = cityList.get(filetype);
			
			//File location use uniform distribution
			int tempX = rnd.nextInt(this.mapSize);
			int tempY = rnd.nextInt(this.mapSize);
			
			/*
			//File location use normal distribution
			long randomX = Math.round(Math.abs(rnd.nextGaussian()*this.mapSize))+(i*this.geoBlocklength);
			int tempX = (int)(randomX%(this.mapSize));
			long randomY= Math.round(Math.abs(rnd.nextGaussian()*this.mapSize))+(i*this.geoBlocklength);
			int tempY = (int)(randomY%(this.mapSize));
			*/
			//File location use normal distribution
			/*
			long randomX = Math.round(Math.abs(rnd.nextGaussian())*this.mapSize)+(currentCity.getX());
			int tempX = (int)(randomX%(this.mapSize));
			long randomY= Math.round(Math.abs(rnd.nextGaussian())*this.mapSize)+(currentCity.getY());
			int tempY = (int)(randomY%(this.mapSize));
			*/
			FeatureFile tempfile = new FeatureFile(filetype, tempX, tempY);
			//add to the GeoBlock
			int blockid = getBlockID(tempX, tempY);

			boolean resault = addFileToGeoBlock(blockid, tempfile);
			if(resault == false){
				System.out.println("Error in class Database's method addFileToGeoBlock");
			}
			//Update file occurred frequency table
			if (fileOccurredFreqTable.containsKey(filetype)) {
				int temp_file_freq=fileOccurredFreqTable.get(filetype);
				fileOccurredFreqTable.put(filetype, temp_file_freq+1);
			}else {
				fileOccurredFreqTable.put(filetype, 1);
			}
		}//for loop
		
		//assign file access probability table
		//normal distribution & uniform distribution
		//need to modify
		double problevel = 0.1;
		for(int key: fileOccurredFreqTable.keySet()){
			double r = rnd.nextGaussian();
			double positive_r = Math.abs(r);
			int prob = 100;
			if(positive_r > 1){
				double predig = Math.floor(positive_r);
				positive_r = (positive_r - predig)/problevel;
				prob = (int)Math.floor(positive_r );
			}else{
				positive_r = positive_r/problevel;
				prob = (int)Math.floor(positive_r);
			}
			if(prob == 9){
				//set probability=100%
				fileAccessProbTable.put(key, 0);
			}else if(prob == 8){
				//set probability=80%
				fileAccessProbTable.put(key, 20);
			}else if(prob == 7){
				//set probability=70%
				fileAccessProbTable.put(key, 30);
			}else if (prob == 6){
				//set probability=60%
				fileAccessProbTable.put(key, 40);
			}else if (prob == 5){
				//set probability=50%
				fileAccessProbTable.put(key, 50);
			}else if (prob == 4){
				//set probability=40%
				fileAccessProbTable.put(key, 60);
			}else if (prob == 3){
				//set probability=30%
				fileAccessProbTable.put(key, 70);
			}else if (prob == 2){
				//set probability=20%
				fileAccessProbTable.put(key, 80);
			}else if (prob == 1){
				//set probability=10%
				fileAccessProbTable.put(key, 90);
			}else if (prob == 0){
				//set probability=5%
				fileAccessProbTable.put(key, 95);
			}else{
				//error
				System.out.println(prob);
				System.out.println("Error during generate  file access probability table");
			}
		}

		//Update file access frequency table
		//need to modify
		for(int key: fileOccurredFreqTable.keySet()){
			int freq = fileOccurredFreqTable.get(key);
			int prob = fileAccessProbTable.get(key);
			int afreq=0;
			if(prob == 0){
				afreq = freq*100;
			}else if(prob == 20){
				afreq = freq*80;
			}else if(prob == 30){
				afreq = freq*70;
			}else if(prob == 40){
				afreq = freq*60;
			}else if(prob == 50){
				afreq = freq*50;
			}else if(prob == 60){
				afreq = freq*40;
			}else if(prob == 70){
				afreq = freq*30;
			}else if(prob == 80){
				afreq = freq*20;
			}else if(prob == 90){
				afreq = freq*10;
			}else if(prob == 95){
				afreq = freq*5;
			}else{
				//error
				System.out.println("Error during generate  file access frequency table");
			}
			fileAccessFreqTable.put(key, afreq);
		}

	}
	
	/**
	 * search a GeoBlock's Feature Files at Database
	 * @param _blockID GeoBlock
	 * 
	 * */
	public List<FeatureFile> searchDB(int _blockID){
		if(_blockID >= 0){
			int index_y = _blockID%this.num_blocks;
			int index_x = _blockID/this.num_blocks;
			return this.GeoBlockSet[index_x][index_y].getFeatureFiles();
		}else{
			System.out.println("Error in class Database's method searchDB ID:"+_blockID);
			return null;
		}
	}
	
	/**
	 * print out the whole Database
	 * 
	 * */
	public void printDB(){
		System.out.println("The average number of FeatureFiles per GeoBlock:"+this.num_files);
		for (int i=0; i < num_blocks; i++){
			for (int j=0; j < num_blocks; j++){
				List<FeatureFile> tempfeatureFiles = GeoBlockSet[i][j].getFeatureFiles();
				/*
				if(i==27 && j==18){
					System.out.println("BlockID:"+GeoBlockSet[i][j].getGeoBlockID());
					System.out.println("Num of feature Files:"+tempfeatureFiles.size());
					for(FeatureFile f: tempfeatureFiles){
						System.out.println("Type:"+f.getFileType()+" X:"+f.getFileLocationX()+" Y:"+f.getFileLocationY());
					}
				}
				*/
				System.out.println("BlockID:"+GeoBlockSet[i][j].getGeoBlockID());
				System.out.println("Num of feature Files:"+tempfeatureFiles.size());
				for(FeatureFile f: tempfeatureFiles){
					System.out.println("Type:"+f.getFileType()+" X:"+f.getFileLocationX()+" Y:"+f.getFileLocationY());
				}
			}//for loop
		}//for loop
	}

}
