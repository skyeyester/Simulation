import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SimMainV2 {
	
	private Map<String, String> parameterTable;
	
	
	/**
	 * @param args
	 * @throws PQueueException
	 */
	public static void main(String[] args) throws PQueueException {
		Map<String, String> parameterTable = new HashMap<String, String>();
		
		//Basic simulation parameter
		long simulationLength;//60*60*1000; //simulation time 1 Hr (U:0.001s)
		int numUsers;  		  //the number of user on map
		int pauseLow;  		  //the lower bound stay time in a block
		int pauseHigh;        //the higher bound stay time in a block
		int userMoveSpeed;
		int dbsize;
		int blockLength;
		int cachesize;
		long userBevSeed;
		long userMobBevSeed;
		long userQueryBevSeed;
		double arrivalRate;
		int totalFilesOnMap;
		int numOfFileKinds;
		long generateFileSeed;
		int algorithm;       //the used algorithm  0:vuforia
		                     //                    1: our schema
		                     //                    2: our schema without Block monitor
							 //                    3: our schema without Pre-load & Block monitor (only priority function)
							 //                    4: LRU
		boolean debug;		 // if debug == true printout debug message
		
		//Simulation parameter used in our schema
		int age_threshold;
		int sizeT_threshold;
		float weight_w1;
		float weight_w2;
		float weight_w3;
		float alpha;
		float beta;
		
		//Read simulation parameter from config file
		try {
			// Open the file that is the first command line parameter
			FileInputStream fstream = new FileInputStream(args[0]);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			//Read File Line By Line
			while ((strLine = br.readLine()) != null)   {
				// Put the parameter content into the table
				//for debug
				//System.out.println (strLine);
				if(strLine.contains("=")){
					String[] lineElement = strLine.trim().split("=");
					if(lineElement.length >0){
						parameterTable.put(lineElement[0].trim(), lineElement[1].trim());
					}else{
						System.out.println ("Error in config file :"+strLine);
					}
				}
			}
			//for debug
			/*
			for (String key : parameterTable.keySet()){
				System.out.println (key+"="+ parameterTable.get(key));
			}
			*/
			//Close the input stream
			in.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.err.println("Error: " + e.getMessage());
			e.printStackTrace();
		}
		
		//Simulation parameter
		simulationLength = Long.parseLong(parameterTable.get("simulationLength"));
		numUsers = Integer.parseInt(parameterTable.get("numUsers"));
		pauseLow = Integer.parseInt(parameterTable.get("pauseLow"));
		pauseHigh = Integer.parseInt(parameterTable.get("pauseHigh"));
		userMoveSpeed = Integer.parseInt(parameterTable.get("moveSpeed"));
		dbsize = Integer.parseInt(parameterTable.get("dbsize"));
		blockLength = Integer.parseInt(parameterTable.get("blockLength"));
		cachesize = Integer.parseInt(parameterTable.get("cachesize"));
		userBevSeed = Long.parseLong(parameterTable.get("userBevSeed"));
		userMobBevSeed = Long.parseLong(parameterTable.get("userMobBevSeed"));
		userQueryBevSeed = Long.parseLong(parameterTable.get("userQueryBevSeed"));
		arrivalRate = Double.parseDouble(parameterTable.get("arrivalRate"));
		totalFilesOnMap = Integer.parseInt(parameterTable.get("totalFilesOnMap"));
		numOfFileKinds = Integer.parseInt(parameterTable.get("numOfFileKinds"));
		generateFileSeed = Long.parseLong(parameterTable.get("generateFileSeed"));
		algorithm = Integer.parseInt(parameterTable.get("algorithm"));
		
		//Simulation parameter Used in our schema
		age_threshold = Integer.parseInt(parameterTable.get("age_threshold"));;
		sizeT_threshold = Integer.parseInt(parameterTable.get("sizeT_threshold"));;
		weight_w1 = Float.parseFloat(parameterTable.get("weight_w1"));
		weight_w2 = Float.parseFloat(parameterTable.get("weight_w2"));
		weight_w3 = Float.parseFloat(parameterTable.get("weight_w3"));
		alpha = Float.parseFloat(parameterTable.get("alpha"));
		beta = Float.parseFloat(parameterTable.get("beta"));
		debug = Boolean.parseBoolean(parameterTable.get("debug"));
		
		Simulation s = new Simulation(simulationLength, numUsers, pauseLow, pauseHigh, userMoveSpeed, dbsize, blockLength,
                cachesize, totalFilesOnMap, numOfFileKinds, generateFileSeed, userBevSeed, userMobBevSeed, userQueryBevSeed, arrivalRate, algorithm, age_threshold, sizeT_threshold,
                weight_w1, weight_w2, weight_w3, alpha, beta, debug);

		/*
		////////
		Database test = new Database(50, 5, 1000, 2, 10);
		Map<Integer, Integer> fileFreqTable = test.getOccurredFreqTable();
		int index=1;
		System.out.println("------------------------Occurred Frequency Table Content--------------------------------------------");
		for(Integer KeyValue : fileFreqTable.keySet()){
			System.out.println(index+"-Key: "+ KeyValue + " Value: " + fileFreqTable.get(KeyValue) );
			index=index+1;
		}
		//
		Map<Integer, Integer> fileProbTable = test.getAccessProbTable();
		int index2=1;
		System.out.println("------------------------Access Probability Table Content--------------------------------------------");
		for(Integer KeyValue : fileProbTable.keySet()){
			System.out.println(index2+"-Key: "+ KeyValue + " Value: " + fileProbTable.get(KeyValue) );
			index2=index2+1;
		}
		//
		System.out.println("------------------------------Database Content--------------------------------------------");
		test.printDB();
		*/
		

		//execute simulation
		s.runSimulation();

		//print out result
		s.printSimulationResults();
	}
		

}
