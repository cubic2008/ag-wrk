package app;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import app.config.CFG;
import wrk.WrkApp;

public class MainApp2 {
////	private static Logger logger = Logger.getLogger(WrkApp.class.getName());
//	private static logging.Logger logger = new logging.Logger();
//
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
//
//	public static void main(String[] args) {
//		try {
//			MainApp2 app = new MainApp2();
//			Map<String, List<WrkBlockRunInfo>> specInfo = app.splitFiles();
//			app.runAllWrk(specInfo);
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
//	
//	private void runAllWrk(Map<String, List<WrkBlockRunInfo>> specInfo) {
//		ExecutorService executor = Executors.newFixedThreadPool(CFG.numOfBrokers);
//		
//		for (Map.Entry<String, List<WrkBlockRunInfo>> brokerEntry : specInfo.entrySet()) {
////			runWrkForBroker(brokerEntry.getKey(), brokerEntry.getValue());
////			new WrkRunner(brokerEntry.getKey(), brokerEntry.getValue()).runWrkForBroker();
//			executor.execute(new WrkRunner(brokerEntry.getKey(), brokerEntry.getValue()));
//		}
//		
//		logger.info("Shutting down...");
//		executor.shutdown();
//		try {
//		    if (!executor.awaitTermination(600000, TimeUnit.MILLISECONDS)) {
//		    	executor.shutdownNow();
//		    	logger.info("Shutdown after timeout.");
//		    } else {
//		    	logger.info("All broker tasks have completed before timed out.");
//		    	new Analyzer().postAnalyze();
//		    }
//		} catch (InterruptedException e) {
//			executor.shutdownNow();
//			logger.info("Shutdown due to interrupted.");
//		}
//	}
//
//	/**
//	 * 
//	 * @return a Map<String, List<WrkBlockRunInfo>>:
//	 * 		   - key: broker name (Broker-k)
//	 * 		   - value: List of WrkBlockRunInfo including brokerNo, blockNo, spec filenames, one for each block;
//	 * @throws FileNotFoundException
//	 * @throws IOException
//	 */
//	private Map<String, List<WrkBlockRunInfo>> splitFiles() throws FileNotFoundException, IOException {
////		String tempSpecFilename = CFG.specFileFolder + File.separator + "temp.spec";
//		String tempReqSetFilename = CFG._logSpecFileFolder + File.separator + "reqset_temp.txt";
//		String originalSpecFilename = CFG._logSpecFileFolder + File.separator + CFG._logSpecFilename;
//		long totalLines = 0;
//		try (	LineNumberReader orgSpecFileReader = new LineNumberReader(new FileReader(originalSpecFilename));
////				FileWriter tempSpecWriter = new FileWriter(tempSpecFilename);
//				FileWriter tempReqSetWriter = new FileWriter(tempReqSetFilename); ) {
////			tempSpecWriter.write("1.0,%s,reqset_temp.txt\n");
//			String specLine = orgSpecFileReader.readLine();
//			while (specLine != null) {
//				String specLineParts[] = specLine.split(",");
//				if (specLineParts.length < 2) {
//					logger.error("Invalid spec file line: " + specLine);
//					System.exit(-1);
//				}
//				String originalRequestSetFilename = CFG._logSpecFileFolder + File.separatorChar + specLineParts[1];
//				try( LineNumberReader orgReqSetReader = new LineNumberReader(new FileReader(originalRequestSetFilename))) {
//					String reqSetLine = orgReqSetReader.readLine();
//					while (reqSetLine != null) {
//						tempReqSetWriter.write(reqSetLine + "\n");
//						reqSetLine = orgReqSetReader.readLine();
//						totalLines ++;
//					}
//					orgReqSetReader.close();
//				}
//				specLine = orgSpecFileReader.readLine();
//			}
//			orgSpecFileReader.close();
//			tempReqSetWriter.close();
//			tempReqSetWriter.close();
//		}
//		logger.debug("totalLines = " + totalLines);
//		long numReqPerBroker = totalLines / CFG.numOfBrokers;
//		Map<String, List<WrkBlockRunInfo>> specInfo = new HashMap<>();
//		try (LineNumberReader tempReqSetReader = new LineNumberReader(new FileReader(tempReqSetFilename))) {
//			for (int brokerIndex = 0; brokerIndex < CFG.numOfBrokers; brokerIndex ++) {
//				List<WrkBlockRunInfo> wrkBlockRunInfos = new ArrayList<>();
//				specInfo.put("Borker-" + brokerIndex, wrkBlockRunInfos);
//				long numOfRequestsForCurBroker = numReqPerBroker;
//				if (brokerIndex == CFG.numOfBrokers - 1) { // last broker
//					numOfRequestsForCurBroker += totalLines % CFG.numOfBrokers;
//				}
//				int numOfBlocks = (int) (numOfRequestsForCurBroker / CFG.numOfRequestPerBlock);
//				logger.debug("brokerIndex = " + brokerIndex + ", numOfBlocks = " + numOfBlocks + ", numOfRequestsForCurBroker = " + numOfRequestsForCurBroker);
//				for (int blockIndex = 0; blockIndex < numOfBlocks; blockIndex ++) {
//					String newSpecFilename = String.format("%s%s%s%d_%d%s", 
//							CFG._logSpecFileFolder, File.separator, CFG._logBrokerSpecFilenamePrefix, brokerIndex, blockIndex, CFG._logBrokerSpecFilenameSuffix);
//					String newReqSetFilename = String.format("%s%s%s%d_%d%s", 
//							CFG._logSpecFileFolder, File.separator, CFG.newRequestSetFilePrefix, brokerIndex, blockIndex, CFG.requestSetFileSuffix);
//					wrkBlockRunInfos.add(new WrkBlockRunInfo(brokerIndex, blockIndex, newSpecFilename));
//					try (	FileWriter newSpecWriter = new FileWriter(newSpecFilename);
//							FileWriter newReqSetWriter = new FileWriter(newReqSetFilename);) {
//						newSpecWriter.write(String.format("1.0,%s%d_%d%s,XXXXXX\n", CFG.newRequestSetFilePrefix, brokerIndex, blockIndex, CFG.requestSetFileSuffix));
//						long numOfRequestForCurBlock = CFG.numOfRequestPerBlock;
//						if (blockIndex == numOfBlocks - 1) { // the last block
//							long remainder = numOfRequestsForCurBroker - (blockIndex + 1) * CFG.numOfRequestPerBlock;
//							if (remainder > CFG.numOfRequestPerBlock * 0.3) { // If the left over is more than 10% of block size, add one additional block
//								numOfBlocks ++;
//							} else {
//								numOfRequestForCurBlock += remainder;
//							}
//						}
//						logger.debug("blockIndex = " + blockIndex + ", numOfRequestForCurBlock = " + numOfRequestForCurBlock);
//						for (int i = 0; i < numOfRequestForCurBlock; i ++) {
//							String requestLine = tempReqSetReader.readLine();
//							newReqSetWriter.write(requestLine + "\n");
//						}
//						newSpecWriter.close();
//						newReqSetWriter.close();
//					}
//					
//				}
//			}
////			tempSpecReader.close();
//			tempReqSetReader.close();
//		}
//		return specInfo;
//	}

}
