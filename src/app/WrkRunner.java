package app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
//	private String jobLogFilename = null;
//	private String jobErrFilename = null;
	private FileWriter jobLogWriter = null;
	private FileWriter jobErrWriter = null;
	
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
		if (!CFG._logWriteWrkLogsToMainLog) {
			String jobLogFilename = String.format("%s%s%s%d.%s", CFG._logWrkLogFolder, File.separator,
					CFG._logWrkLogFilePrefix, brokerIndex, CFG._logWrkLogFileSuffix);
			try {
				this.jobLogWriter = new FileWriter(jobLogFilename);
				if (CFG._logWrkErrFileSuffix.equals(CFG._logWrkLogFileSuffix)) {
					this.jobErrWriter = this.jobLogWriter;
				} else {
					String jobErrFilename = String.format("%s%s%s%d.%s", CFG._logWrkLogFolder, File.separator,
							CFG._logWrkLogFilePrefix, brokerIndex, CFG._logWrkErrFileSuffix);
					this.jobErrWriter = new FileWriter(jobErrFilename);
				}
			} catch (IOException ioe) {
				Logger.warn(String.format("Fail to write to job log file: %s", ioe.getMessage()));
			}
		}
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
        	writeToLog("Running wrk for Experiental environment ...");
            Process p = rt.exec(wrkCommand);
            p.waitFor();
            logProcessOutput(p);
//            String response = readProcessOutput(p);
//            logger.info(response);
        	writeToLog("Running wrk for Staging environment ...");
    		wrkCommand[7] = CFG._logBrokerStagingServerName[brokerIndex];
    		wrkCommand[9] = responseStagingFilename;
    		wrkCommand[11] = "Staging";
            p = rt.exec(wrkCommand);
            p.waitFor();
            logProcessOutput(p);
        }catch(Exception ex) {
            ex.printStackTrace();
        }
        if (this.jobLogWriter != null) {
        	try {
				this.jobLogWriter.close();
			} catch (IOException e) { }
        }
        if (this.jobErrWriter != null) {
        	try {
				this.jobErrWriter.close();
			} catch (IOException e) { }
        }
	}
	
	private void logProcessOutput(Process p) throws Exception{
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
//        String response = "";
        String line;
        while ((line = reader.readLine()) != null) {
//            response += line+"\r\n";
//        	Logger.info(line);
        	writeToLog(line);
        }
        reader.close();
        // 
        BufferedReader errReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        String errLine;
        while ((errLine = errReader.readLine()) != null) {
//        	response += errLine+"\r\n";
//        	Logger.error(errLine);
        	writeToErr(errLine);
        }
        errReader.close();
//        return response;
    }
	
	private void writeToLog(String message) {
		if (this.jobLogWriter != null) {
			try {
				this.jobLogWriter.write(message + "\n");
			} catch (IOException e) {
				Logger.info(message);
			}
		} else {
			Logger.info(message);
		}
	}
	
	private void writeToErr(String message) {
		if (this.jobErrWriter != null) {
			try {
				this.jobErrWriter.write(message + "\n");
			} catch (IOException e) {
				Logger.info(message);
			}
		} else if (this.jobLogWriter != null) {
			try {
				this.jobLogWriter.write(message + "\n");
			} catch (IOException e) {
				Logger.info(message);
			}
		} else {
			Logger.info(message);
		}
	}


}
