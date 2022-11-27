package app;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import app.config.CFG;
import logging.Logger;

public class WrkRunner implements Runnable{
//	private static Logger logger = Logger.getLogger(WrkApp.class.getName());
//	private static logging.Logger logger = new logging.Logger();
	
	private static String runWrkCommand[] = {
			"java", "-cp", "C:\\work\\workspaces\\java-se3(2)\\ag-wrk\\bin", "wrk.WrkApp", 
			"-spec", "C:\\work\\workspaces\\java-se3(2)\\ag-wrk\\wrk.specs", 
			"-server", "https://server-info", 
			"-output", "C:\\work\\workspaces\\java-se3(2)\\ag-wrk\\response.txt",
			"-env", "experimental or staging"
	};

	private int brokerIndex;
	private String runSpecFilename;
	private String responseExperientalFilename;
	private String responseStagingFilename;
	
//	public WrkRunner(int borkerIndex) {
	public WrkRunner(int borkerIndex, String specFilename) {
		this.brokerIndex = borkerIndex;
		this.runSpecFilename = specFilename;
//		this.runSpecFilename = String.format("%s%s%s%d%s", 
//				CFG._logBrokerSpecFileFolder, File.separator, CFG._logBrokerSpecFilenamePrefix, brokerIndex + 1, CFG._logBrokerSpecFilenameSuffix);
		this.responseExperientalFilename = String.format("%s%s%s%d_exp%s", 
				CFG._logWrkOutputFoder, File.separator, CFG._logWrkOutputFilePrefix,
				brokerIndex + 1, CFG._logWrkOutputFileSuffix);
		this.responseStagingFilename = String.format("%s%s%s%d_stg%s", 
				CFG._logWrkOutputFoder, File.separator, CFG._logWrkOutputFilePrefix,
				brokerIndex + 1, CFG._logWrkOutputFileSuffix);
	}

//	void runWrkForBroker(String borker, List<WrkBlockRunInfo> runInfos) {
//	void runWrkForBroker() {
	@Override
	public void run() {
		Logger.info("[" + Thread.currentThread().getName() + "] Running wrk for broker: " + brokerIndex + " ...");
//		for (WrkBlockRunInfo runInfo : runInfos) {
//			runWrk(runInfo);
//		}
		runWrk(runSpecFilename);
//		deleteBrokerRunFiles(); // alternative, broker run files (spec and query set) can be removed here, right after wrk job has completed and before the analyzer starts.
		new Analyzer().analyze(brokerIndex, responseExperientalFilename);
	}

	private void deleteBrokerRunFiles() {
		if (CFG._logRemoveBrokerRunFiles) {
//			String runSpecFilename = String.format("%s%s%s%d%s", 
//					CFG._logBrokerSpecFileFolder, File.separator, CFG._logBrokerSpecFilenamePrefix, brokerIndex + 1, CFG._logBrokerSpecFilenameSuffix);
//			String runSpecFilename = specFilename;
			String runQuerySetFilename = String.format("%s%s%s%d%s", 
					CFG._logBrokerSpecFileFolder, File.separator, CFG._logBrokerQuerySetFilePrefix, brokerIndex + 1, CFG._logBrokerQuerySetFileSuffix);
			Utils.deleteFile("Delete broker run spec file", runSpecFilename);
			Utils.deleteFile("Delete broker run query set file", runQuerySetFilename);
		}

	}

//	private void runWrk(WrkBlockRunInfo specInfo) {
	private void runWrk(String specFilename) {
		String[] wrkCommand = runWrkCommand.clone();
		wrkCommand[5] = specFilename;
		wrkCommand[7] = CFG._logBrokerExperimentalServerName[brokerIndex];
		wrkCommand[9] = responseExperientalFilename;
//		wrkCommand[9] = String.format("%s%s%s%d_%d%s", 
//				CFG.wrkOutputFoder, File.separator, CFG.wrkOutputFilePrefix,
//				specInfo.brokerIndex, specInfo.blockIndex, CFG.wrkOutputFileSuffix);
		wrkCommand[11] = "Experiental";
		Runtime rt = Runtime.getRuntime();
        try {
            Process p = rt.exec(wrkCommand);
            p.waitFor();
            logProcessOutput(p);
//            String response = readProcessOutput(p);
//            logger.info(response);
    		wrkCommand[7] = CFG._logBrokerStagingServerName[brokerIndex];
    		wrkCommand[9] = responseStagingFilename;
    		wrkCommand[11] = "Staging";
            p = rt.exec(wrkCommand);
            p.waitFor();
            logProcessOutput(p);
        }catch(Exception ex) {
            ex.printStackTrace();
        }
	}
	
	private void logProcessOutput(Process p) throws Exception{
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String response = "";
        String line;
        while ((line = reader.readLine()) != null) {
//            response += line+"\r\n";
        	Logger.info(line);
        }
        reader.close();
        // 
        BufferedReader errReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        String errLine;
        while ((errLine = errReader.readLine()) != null) {
//        	response += errLine+"\r\n";
        	Logger.error(errLine);
        }
        errReader.close();
//        return response;
    }


}
