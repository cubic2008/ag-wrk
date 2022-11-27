package app;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
//import java.util.logging.Logger;

import app.config.CFG;
import logging.Logger;

public class MainApp {
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
			MainApp app = new MainApp();
//			Map<String, List<WrkBlockRunInfo>> specInfo = app.splitFiles();
			app.cleanResponseFolder();
			Map<Integer, String> specInfo = app.splitFiles();
			app.runAllWrk(specInfo);
			app.removeBrokerRunFiles(); // alternatively, we can delete inside the WrkRunner class, right after the wrk job returns.
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
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
		if (CFG._logCleanResponseFolderBeforeRun) {
			File responseFolder = new File(CFG._logWrkOutputFoder);
			File[] responseFiles = responseFolder.listFiles();
			for (File file : responseFiles) {
				Utils.deleteFile("Clean response file", file);
//				if (!file.delete()) {
//					Logger.warn(String.format("[Clean response file] \"%s\" cannot be deleted.", file.getAbsoluteFile()));
//				}
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
