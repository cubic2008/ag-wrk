package app;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
//import java.util.logging.Logger;

import app.config.CFG;
import javafx.util.Pair;
import logging.Logger;

public class MainApp3 {
//	private static Logger logger = Logger.getLogger(WrkApp.class.getName());
//	private static logging.Logger logger = new logging.Logger();

//	class WrkBlockRunInfo {
//		int brokerIndex;
//		int blockIndex;
//		String specFilename;
//		public WrkBlockRunInfo(int brokerIndex, int blockIndex, String specFilename) {
//			this.brokerIndex = brokerIndex;
//			this.blockIndex = blockIndex;
//			this.specFilename = specFilename;
//		}
//		
//	}

	public static void main(String[] args) {
		try {
			MainApp3 app = new MainApp3();
//			Map<String, List<WrkBlockRunInfo>> specInfo = app.splitFiles();
//			app.cleanResponseFolder();
//			Map<Integer, String> specInfo = app.splitFiles();
//			app.runAllWrk(specInfo);
//			app.removeBrokerRunFiles(); // alternatively, we can delete inside the WrkRunner class, right after the wrk job returns.
			SortedMap<Long, Pair<String, String>> querySpecMap = new TreeMap<>();	// DG
			List<Pair<Long, Long>> r = app.getBrokerQueryspecIndexes(12, querySpecMap);
			System.out.println("r = " + r);
			System.out.println("querySpecMap = " + querySpecMap);
			System.out.println("Request[0] = " + app.readQuery(0, querySpecMap));
			System.out.println("Request[1] = " + app.readQuery(1, querySpecMap));
			System.out.println("Request[12345] = " + app.readQuery(12345, querySpecMap));
			System.out.println("Request[99999999] = " + app.readQuery(99999999, querySpecMap));
//			List<Long> requestIds = Arrays.asList(0L, 1L, 2L, 12345L, 12010L, 6436L, 6439L, 6437L, 6438L, 20000L, 30000L, 40000L, 80000L, 900000L, 99999999L, 123L, 100000L, 110000L, 114217L, 114218L, 120000L, 123823L, 123824L, 123825L);
			List<Long> requestIds = new ArrayList<>();
			for (long k = 0L; k < 129999L; k ++) {
				requestIds.add(k);
			}
//			System.out.println("retrieveQueries[99999999] = " + app.retrieveQueries(requestIds, querySpecMap));
			app.retrieveQueriesToFile(requestIds, "C:\\Temp\\ag-wrk\\temp\\retry-wrk.spec", querySpecMap);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	/**
	 * Reads queries by a list of requestIds, and output queries to specified spec file by queryTypes.
	 * Query set files will be generated under the same folder as spec file.
	 * The query set files take the filename of "query-set-&lt;k&gt;.txt, where k = 1..n (n = total # of queryTypes)
	 * It sorts the requestId and read through all data set files in sequence in order
	 * to gain O(n) performance (n = # of total queries)
	 * @param requestIds requestIds a list of requestIds
	 * @param specFilename the name of the spec file to be generated. All queryset files will be generated under the same folder.
	 * @param querySpecMap the querySpec that was built when processing the querySpec
	 * @return a Map<String, List<String>> while key is the queryType, value is a list of queries under the queryType
	 * a list of queries under the query
	 * @throws IOException 
	 */
	public void retrieveQueriesToFile(List<Long> requestIds, String specFilename, SortedMap<Long, Pair<String, String>> querySpecMap) throws IOException {
		File specFile = new File(specFilename);
		String specFileFolder = specFile.getParent();
		Map<String, FileWriter> querySetFileWriters = new HashMap<>();
		List<Long> querySpecMapKeyList = new ArrayList<>(querySpecMap.keySet());

		// sort the requestIds and querySepcMapKeyList for performance consideration
		requestIds.sort( (id1, id2) -> (int)(id1 - id2));
		querySpecMapKeyList.sort( (id1, id2) -> (int)(id1 - id2));

		int dataSetNo = 1;
		String querySetFilename = null;
		String queryType = null;
		long lineCount = 1;
//		Iterator<Long> querySpecMapKeyIterator = querySpecMapKeyList.iterator();'
		int currQuerySpecMapKeyListIndex = 0;
		LineNumberReader lnReader = null;
		String previousQuerySetFilename = null;
//		long nextQuerySetStartId  = 1;
		try (FileWriter specFileWriter = new FileWriter(specFile)){
			for (Long requestId : requestIds) {
				Logger.debug("Processing requestId = " + requestId);
				boolean found = false; 
				while(currQuerySpecMapKeyListIndex < querySpecMapKeyList.size()) {
					if (requestId < querySpecMapKeyList.get(currQuerySpecMapKeyListIndex)) {
						found = true;
						break;
					}
	//				nextQuerySetStartId = querySpecMapKeyList.get(currQuerySpecMapKeyListIndex);
					lineCount = querySpecMapKeyList.get(currQuerySpecMapKeyListIndex);
					querySetFilename = querySpecMap.get(lineCount).getKey();
					queryType = querySpecMap.get(lineCount).getValue();
					currQuerySpecMapKeyListIndex++;
				}
				// it's possible that found is set to true in the first iteration of above loop, 
				// which means that requestId < the first queryset's request id, which should not occur.
				// Under both conditions, return null to indicate requestId does not exist.
				if (!found || querySetFilename == null) {
					Logger.debug("not found");
					continue;
				}

//				Logger.debug("lineCount = " + lineCount + ", querySetFilename = " + querySetFilename);
				
				if (lnReader == null || !querySetFilename.equals(previousQuerySetFilename)) {
					if (lnReader != null) {
						Logger.debug("File closed - " + previousQuerySetFilename);
						lnReader.close();
					}
					lnReader = new LineNumberReader(new FileReader(querySetFilename));
					previousQuerySetFilename = querySetFilename;
				}
				String line = "";
				while(line != null) {
					if (lineCount - 1 == requestId) {
//						Logger.debug("Adding: lineCount = " + lineCount + ", requestId = " + requestId);
						FileWriter querySetWriter = querySetFileWriters.get(queryType);
						if (querySetWriter == null) {
							String dataSetFilename = String.format("DataSet-%d.txt", dataSetNo++);
							specFileWriter.write(String.format("0.0,%s,%s%n", dataSetFilename, queryType));
							querySetWriter = new FileWriter(specFileFolder + File.separator + dataSetFilename);
							querySetFileWriters.put(queryType, querySetWriter);
						}
						if (line.trim().length() == 0 || "".equals(line)) {
							System.out.println("-----------------------------------------------------------------------");
						}
						querySetWriter.write(line + "\n");
//						Logger.debug(String.format("%s is added to %s / %s", line, queryType, queryList));
						break;
					}
					line = lnReader.readLine();
					lineCount++;
				}
			}
		} finally {
			if (lnReader != null) {
				Logger.debug("File closed - " + previousQuerySetFilename);
				lnReader.close();
			}
			// Close all query set file writers.
			for (Map.Entry<String, FileWriter> querySetWriterEntry : querySetFileWriters.entrySet()) {
				querySetWriterEntry.getValue().close();
			}
		}
		
	}
	
	/**
	 * Reads queries by a list of requestIds, and returns queries by queryTypes
	 * It sorts the requestId and read through all data set files in sequence in order
	 * to gain O(n) performance (n = # of total queries)
	 * @param requestIds requestIds a list of requestIds
	 * @param querySpecMap the querySpec that was built when processing the querySpec
	 * @return a Map<String, List<String>> while key is the queryType, value is a list of queries under the queryType
	 * a list of queries under the query
	 * @throws IOException 
	 */
	public Map<String, List<String>> retrieveQueries(List<Long> requestIds, SortedMap<Long, Pair<String, String>> querySpecMap) throws IOException {
		Map<String, List<String>> queriesByType = new HashMap<>();
		List<Long> querySpecMapKeyList = new ArrayList<>(querySpecMap.keySet());

		// sort the requestIds and querySepcMapKeyList for performance consideration
		requestIds.sort( (id1, id2) -> (int)(id1 - id2));
		querySpecMapKeyList.sort( (id1, id2) -> (int)(id1 - id2));

		String querySetFilename = null;
		String queryType = null;
		long lineCount = 1;
//		Iterator<Long> querySpecMapKeyIterator = querySpecMapKeyList.iterator();'
		int currQuerySpecMapKeyListIndex = 0;
		LineNumberReader lnReader = null;
		String previousQuerySetFilename = null;
//		long nextQuerySetStartId  = 1;
		try {
			for (Long requestId : requestIds) {
				Logger.debug("Processing requestId = " + requestId);
				boolean found = false; 
				while(currQuerySpecMapKeyListIndex < querySpecMapKeyList.size()) {
					if (requestId < querySpecMapKeyList.get(currQuerySpecMapKeyListIndex)) {
						found = true;
						break;
					}
	//				nextQuerySetStartId = querySpecMapKeyList.get(currQuerySpecMapKeyListIndex);
					lineCount = querySpecMapKeyList.get(currQuerySpecMapKeyListIndex);
					querySetFilename = querySpecMap.get(lineCount).getKey();
					queryType = querySpecMap.get(lineCount).getValue();
					currQuerySpecMapKeyListIndex++;
				}
				// it's possible that found is set to true in the first iteration of above loop, 
				// which means that requestId < the first queryset's request id, which should not occur.
				// Under both conditions, return null to indicate requestId does not exist.
				if (!found || querySetFilename == null) {
					Logger.debug("not found");
					continue;
				}

				Logger.debug("lineCount = " + lineCount + ", querySetFilename = " + querySetFilename);
				
				if (lnReader == null || !querySetFilename.equals(previousQuerySetFilename)) {
					if (lnReader != null) {
						Logger.debug("File closed - " + previousQuerySetFilename);
						lnReader.close();
					}
					lnReader = new LineNumberReader(new FileReader(querySetFilename));
					previousQuerySetFilename = querySetFilename;
				}
				String line = "";
				while(line != null) {
					if (lineCount - 1 == requestId) {
//						Logger.debug("Adding: lineCount = " + lineCount + ", requestId = " + requestId);
						List<String> queryList = queriesByType.get(queryType);
						if (queryList == null) {
							queryList = new ArrayList<>();
						}
						queryList.add(line);
						queriesByType.put(queryType, queryList);
//						Logger.debug(String.format("%s is added to %s / %s", line, queryType, queryList));
						break;
					}
					line = lnReader.readLine();
					lineCount++;
				}
			}
		} finally {
			if (lnReader != null) {
				Logger.debug("File closed - " + previousQuerySetFilename);
				lnReader.close();
			}
		}
		return queriesByType;
		
	}
	
	// Read query by requestId
	/**
	 * Retrieve a query
	 * @param requestId
	 * @param querySpecMap the querySpec that was built when processing the querySpec
	 * @return a Pair of query as the key and the queryType as the value if found, null otherwise. 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public Pair<String, String> readQuery(long requestId, SortedMap<Long, Pair<String, String>> querySpecMap) throws FileNotFoundException, IOException {
		String querySetFilename = null;
		String queryType = null;
		boolean found = false; 
		long lineCount = 1;
		for (SortedMap.Entry<Long, Pair<String, String>> entry : querySpecMap.entrySet()) {
			if (requestId < entry.getKey()) {
				found = true;
				break;
			}
			lineCount = entry.getKey();
			querySetFilename = entry.getValue().getKey();
			queryType = entry.getValue().getValue();
		}
		// it's possible that found is set to true in the first iteration of above loop, 
		// which means that requestId < the first queryset's request id, which should not occur.
		// Under both conditions, return null to indicate requestId does not exist.
		if (!found || querySetFilename == null) {
			return null;
		}
		try(LineNumberReader lnReader = new LineNumberReader(new FileReader(querySetFilename))) {
			String line;
			while((line = lnReader.readLine()) != null) {
				if (lineCount == requestId) {
					return new Pair<>(line, queryType); 
				}
				lineCount++;
			}
		}
		return null;
	}
	
	/**
	 * 
	 * @param numBrokers
	 * @param querySpecMap an empty Map for query spec and query set information that is expected to be filled by the method. 
	 * The key of the map is the query set file start line number and the value is a Pair of query set filename and query type.
	 * There will be an additional entry representing the one beyond the last query set, with the key = total lines of request + 1,
	 * and the value = Pair(null, null)
	 * @return
	 * @throws IOException
	 */
	public List<Pair<Long, Long>> getBrokerQueryspecIndexes(int numBrokers, SortedMap<Long, Pair<String, String>> querySpecMap) throws IOException {
//	    String specFileName = CFG._logSpecFilename;
	    String specFileFolder = new File(CFG._logSpecFilename).getParent();
	    long totalLines = 0;

	    try (LineNumberReader specFileReader = new LineNumberReader(new FileReader(CFG._logSpecFilename))) {
	      String specLine = specFileReader.readLine();
	      while (specLine != null) {
	        String[] specLineParts = specLine.split(",");
	        if (specLine.startsWith("#") || specLineParts.length < 3) {
	          Logger.info("Skip spec file line: " + specLine);
	          specLine = specFileReader.readLine();
	          continue;
	        }
	        String querySetFile = specFileFolder + File.separatorChar + specLineParts[1];
	        querySpecMap.put(totalLines + 1, new Pair<>(querySetFile, specLineParts[2]));	// DG
	        try (LineNumberReader querySetReader = new LineNumberReader(new FileReader(querySetFile))) {
	          String reqSetLine = querySetReader.readLine();
	          while (reqSetLine != null) {
	            reqSetLine = querySetReader.readLine();
	            totalLines++;
	          }
	        }
	        specLine = specFileReader.readLine();
	      }
	      querySpecMap.put(totalLines + 1, new Pair<>(null, null));	// DG
	    }
	    Logger.debug("totalLines = " + totalLines);
	// Obtain the start and end queryspec line index for each broker
	    long numReqPerBroker = totalLines / numBrokers;
	    List<Pair<Long, Long>> indexes = new ArrayList<>();

	    long numOfRequestsForCurrBroker = numReqPerBroker;

	    for (int brokerIndex = 0; brokerIndex < numBrokers; brokerIndex++) {
	      long startIndexForCurrBroker = brokerIndex * numOfRequestsForCurrBroker + 1;
	      if (brokerIndex == numBrokers - 1) { // last broker
	        numOfRequestsForCurrBroker += totalLines % numBrokers;
	      }
	long endIndexForCurrBroker = startIndexForCurrBroker + numOfRequestsForCurrBroker - 1;
	      Logger.debug("brokerIndex = " + brokerIndex + ", numOfRequestsForCurBroker = " + numOfRequestsForCurrBroker
	          + ", startIndexForCurrBroker = " + startIndexForCurrBroker + ", endIndexForCurrBroker = "
	          + endIndexForCurrBroker);

	      indexes.add(new Pair<>(startIndexForCurrBroker, endIndexForCurrBroker));
	    }
	    return indexes;
	  }
	
	private void removeBrokerRunFiles() {
		if (CFG._logRemoveBrokerRunFiles) {
			for (int brokerIndex = 0; brokerIndex < CFG._logNumOfBrokers; brokerIndex ++) {
				String runSpecFilename = String.format("%s%s%s%d%s", 
						CFG._logBrokerSpecFileFolder, File.separator, CFG._logBrokerSpecFilenamePrefix, brokerIndex + 1, CFG._logBrokerSpecFilenameSuffix);
				String runQuerySetFilename = String.format("%s%s%s%d%s", 
						CFG._logBrokerSpecFileFolder, File.separator, CFG._logBrokerQuerySetFilePrefix, brokerIndex + 1, CFG._logBrokerQuerySetFileSuffix);
				Utils.deleteFile("Delete broker run spec file", runSpecFilename);
				Utils.deleteFile("Delete broker run query set file", runQuerySetFilename);
//				if (! new File(runSpecFilename).delete()) {
//					Logger.warn(String.format("[Clean broker run spec file] \"%s\" cannot be deleted.", runSpecFilename));
//				}
			}
		}
	}

	private void cleanResponseFolder() {
		if (CFG._logCleanUpResponseFolderBeforeRun) {
			File responseFolder = new File(CFG._logWrkOutputFoder);
			File[] responseFiles = responseFolder.listFiles();
			for (File file : responseFiles) {
				Utils.deleteFile("Clean response file", file);
//				if (!file.delete()) {
//					Logger.warn(String.format("[Clean response file] \"%s\" cannot be deleted.", file.getAbsoluteFile()));
//				}
			}
		}
		if (CFG._logCleanUpLogFolderBeforeRun) {
			File responseFolder = new File(CFG._logWrkLogFolder);
			File[] responseFiles = responseFolder.listFiles();
			for (File file : responseFiles) {
				Utils.deleteFile("Clean log file", file);
			}
		}
	
	}
	
//	private void deleteFile(String actionInfo, String filename) {
//		deleteFile(actionInfo, new File(filename));
//	}
//
//	private void deleteFile(String actionInfo, File file) {
//		if (!file.delete()) {
//			Logger.warn(String.format("[%s] \"%s\" cannot be deleted.", actionInfo, file.getAbsoluteFile()));
//		}
//	}

	private void runAllWrk(Map<Integer, String> specInfo) {
//	private void runAllWrk(Map<String, List<WrkBlockRunInfo>> specInfo) {
		ExecutorService executor = Executors.newFixedThreadPool(CFG._logNumOfBrokers);
		
//		for (Map.Entry<String, List<WrkBlockRunInfo>> brokerEntry : specInfo.entrySet()) {
////			runWrkForBroker(brokerEntry.getKey(), brokerEntry.getValue());
////			new WrkRunner(brokerEntry.getKey(), brokerEntry.getValue()).runWrkForBroker();
//			executor.execute(new WrkRunner(brokerEntry.getKey(), brokerEntry.getValue()));
//		}
		
		for (Map.Entry<Integer, String> brokerRunEntry : specInfo.entrySet()) {
//			runWrkForBroker(brokerEntry.getKey(), brokerEntry.getValue());
//			new WrkRunner(brokerEntry.getKey(), brokerEntry.getValue()).runWrkForBroker();
//			executor.execute(new WrkRunner(brokerEntry.getKey(), brokerEntry.getValue()));
//			executor.execute(new WrkRunner(brokerRunEntry.getKey(), brokerRunEntry.getValue()));
			executor.execute(new WrkRunner(brokerRunEntry.getKey(), brokerRunEntry.getValue()));
		}

		Logger.info("Shutting down...");
		executor.shutdown();
		try {
		    if (!executor.awaitTermination(600000, TimeUnit.MILLISECONDS)) {
		    	executor.shutdownNow();
		    	Logger.info("Shutdown after timeout.");
		    } else {
		    	Logger.info("All broker tasks have completed before timed out.");
		    	new Analyzer().postAnalyze();
		    }
		} catch (InterruptedException e) {
			executor.shutdownNow();
			Logger.info("Shutdown due to interrupted.");
		}
	}

//	private Map<String, List<WrkBlockRunInfo>> splitFiles() throws FileNotFoundException, IOException {
	private Map<Integer, String> splitFiles() throws FileNotFoundException, IOException {
//		String tempSpecFilename = CFG.specFileFolder + File.separator + "temp.spec";
		String tempQuerySetFilename = CFG._logTempFolder + File.separator + "queryset_temp.txt";
		String originalSpecFilename = CFG._logSpecFileFolder + File.separator + CFG._logSpecFilename;
		long totalLines = 0;
		
		// Generating temp queryset file . . .
		try (	LineNumberReader orgSpecFileReader = new LineNumberReader(new FileReader(originalSpecFilename));
//				FileWriter tempSpecWriter = new FileWriter(tempSpecFilename);
				FileWriter tempReqSetWriter = new FileWriter(tempQuerySetFilename); ) {
//			tempSpecWriter.write("1.0,%s,reqset_temp.txt\n");
			String specLine = orgSpecFileReader.readLine();
			while (specLine != null) {
				String specLineParts[] = specLine.split(",");
				if (specLineParts.length < 2) {
					Logger.error("Invalid spec file line: " + specLine);
					System.exit(-1);
				}
				String originalRequestSetFilename = CFG._logSpecFileFolder + File.separatorChar + specLineParts[1];
				try( LineNumberReader orgQuerySetReader = new LineNumberReader(new FileReader(originalRequestSetFilename))) {
					String reqSetLine = orgQuerySetReader.readLine();
					while (reqSetLine != null) {
						tempReqSetWriter.write(reqSetLine + "\n");
						reqSetLine = orgQuerySetReader.readLine();
						totalLines ++;
					}
					orgQuerySetReader.close();
				}
				specLine = orgSpecFileReader.readLine();
			}
			orgSpecFileReader.close();
			tempReqSetWriter.close();
			tempReqSetWriter.close();
		}
		Logger.debug("totalLines = " + totalLines);
		
		// Generting one pair of <Spec, QuerySet> files for each broker
		long numReqPerBroker = totalLines / CFG._logNumOfBrokers;
//		Map<String, List<WrkBlockRunInfo>> specInfo = new HashMap<>();
		Map<Integer, String> specInfo = new HashMap<>();
		try (LineNumberReader tempQuerySetReader = new LineNumberReader(new FileReader(tempQuerySetFilename))) {
			for (int brokerIndex = 0; brokerIndex < CFG._logNumOfBrokers; brokerIndex ++) {
//				List<WrkBlockRunInfo> wrkBlockRunInfos = new ArrayList<>();
//				specInfo.put("Borker-" + brokerIndex, wrkBlockRunInfos);
//				specInfo.put(brokerIndex, new ArrayList<>());
				long numOfRequestsForCurBroker = numReqPerBroker;
				if (brokerIndex == CFG._logNumOfBrokers - 1) { // last broker
					numOfRequestsForCurBroker += totalLines % CFG._logNumOfBrokers;
				}
//				int numOfBlocks = (int) (numOfRequestsForCurBroker / CFG.numOfRequestPerBlock);
//				Logger.debug("brokerIndex = " + brokerIndex + ", numOfBlocks = " + numOfBlocks + ", numOfRequestsForCurBroker = " + numOfRequestsForCurBroker);
				Logger.debug("brokerIndex = " + brokerIndex + ", numOfRequestsForCurBroker = " + numOfRequestsForCurBroker);
				String runSpecFilename = String.format("%s%s%s%d%s", 
						CFG._logBrokerSpecFileFolder, File.separator, CFG._logBrokerSpecFilenamePrefix, brokerIndex + 1, CFG._logBrokerSpecFilenameSuffix);
				String runQuerySetFilename = String.format("%s%s%s%d%s", 
						CFG._logBrokerSpecFileFolder, File.separator, CFG._logBrokerQuerySetFilePrefix, brokerIndex + 1, CFG._logBrokerQuerySetFileSuffix);
//				wrkBlockRunInfos.add(new WrkBlockRunInfo(brokerIndex, blockIndex, runSpecFilename));
				specInfo.put(brokerIndex, runSpecFilename);
				try (	FileWriter runSpecWriter = new FileWriter(runSpecFilename);
						FileWriter runQuerySetWriter = new FileWriter(runQuerySetFilename);) {
					runSpecWriter.write(String.format("1.0,%s%d%s,XXXXXX\n", CFG._logBrokerQuerySetFilePrefix, brokerIndex + 1, CFG._logBrokerQuerySetFileSuffix));
					for (int i = 0; i < numOfRequestsForCurBroker; i ++) {
						String queryLine = tempQuerySetReader.readLine();
						runQuerySetWriter.write(queryLine + "\n");
					}
					runSpecWriter.close();
					runQuerySetWriter.close();
				}
			}
			tempQuerySetReader.close();
		}
		
		if (CFG._logRemoveTempFiles) {
			Utils.deleteFile("Clean temp file", tempQuerySetFilename);
//			if (!new File(tempQuerySetFilename).delete()) {
//				Logger.warn(String.format("[Clean temp file] \"%s\" cannot be deleted.", tempQuerySetFilename));
//			}
		}

					
					
//				for (int blockIndex = 0; blockIndex < numOfBlocks; blockIndex ++) {
//					String newSpecFilename = String.format("%s%s%s%d_%d%s", 
//							CFG._logSpecFileFolder, File.separator, CFG._logBrokerSpecFilenamePrefix, brokerIndex, blockIndex, CFG._logBrokerSpecFilenameSuffix);
//					String newReqSetFilename = String.format("%s%s%s%d_%d%s", 
//							CFG._logSpecFileFolder, File.separator, CFG._logBrokerQuerySetFilePrefix, brokerIndex, blockIndex, CFG._logBrokerQuerySetFileSuffix);
//					wrkBlockRunInfos.add(new WrkBlockRunInfo(brokerIndex, blockIndex, newSpecFilename));
//					try (	FileWriter newSpecWriter = new FileWriter(newSpecFilename);
//							FileWriter newReqSetWriter = new FileWriter(newReqSetFilename);) {
//						newSpecWriter.write(String.format("1.0,%s%d_%d%s,XXXXXX\n", CFG._logBrokerQuerySetFilePrefix, brokerIndex, blockIndex, CFG._logBrokerQuerySetFileSuffix));
//						long numOfRequestForCurBlock = CFG.numOfRequestPerBlock;
//						if (blockIndex == numOfBlocks - 1) { // the last block
//							long remainder = numOfRequestsForCurBroker - (blockIndex + 1) * CFG.numOfRequestPerBlock;
//							if (remainder > CFG.numOfRequestPerBlock * 0.3) { // If the left over is more than 10% of block size, add one additional block
//								numOfBlocks ++;
//							} else {
//								numOfRequestForCurBlock += remainder;
//							}
//						}
//						Logger.debug("blockIndex = " + blockIndex + ", numOfRequestForCurBlock = " + numOfRequestForCurBlock);
//						for (int i = 0; i < numOfRequestForCurBlock; i ++) {
//							String requestLine = tempQuerySetReader.readLine();
//							newReqSetWriter.write(requestLine + "\n");
//						}
//						newSpecWriter.close();
//						newReqSetWriter.close();
//					}
//					
//				}
//			}
//			tempSpecReader.close();
//			tempQuerySetReader.close();
//		}
		return specInfo;
	}

}
