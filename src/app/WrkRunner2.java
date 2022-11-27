package app;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.List;

import app.config.CFG;

public class WrkRunner2 implements Runnable{
//	private static Logger logger = Logger.getLogger(WrkApp.class.getName());
	private static logging.Logger logger = new logging.Logger();
	
	private static String runWrkCommand[] = {
			"java", "-cp", "C:\\work\\workspaces\\java-se3(2)\\ag-wrk\\bin", "wrk.WrkApp", 
			"-spec", "C:\\work\\workspaces\\java-se3(2)\\ag-wrk\\wrk.specs", 
			"-server", "https://server-info", 
			"-output", "C:\\work\\workspaces\\java-se3(2)\\ag-wrk\\response.txt"
	};

	private String broker;
	private List<WrkBlockRunInfo> runInfos;
	
	public WrkRunner2(String borker, List<WrkBlockRunInfo> runInfos) {
		this.broker = borker;
		this.runInfos = runInfos;
	}

//	void runWrkForBroker(String borker, List<WrkBlockRunInfo> runInfos) {
//	void runWrkForBroker() {
	@Override
	public void run() {
		logger.info("[" + Thread.currentThread().getName() + "] Running wrk for broker: " + broker + " ...");
		for (WrkBlockRunInfo runInfo : runInfos) {
			runWrk(runInfo);
		}
		new Analyzer().analyze(broker);
		
	}

	private void runWrk(WrkBlockRunInfo specInfo) {
		String[] wrkCommand = runWrkCommand.clone();
		wrkCommand[5] = specInfo.specFilename;
		wrkCommand[9] = String.format("%s%s%s%d_%d%s", 
				CFG._logWrkOutputFoder, File.separator, CFG._logWrkOutputFilePrefix,
				specInfo.brokerIndex, specInfo.blockIndex, CFG._logWrkOutputFileSuffix);
		Runtime rt = Runtime.getRuntime();
        try {
            Process p = rt.exec(wrkCommand);
            logProcessOutput(p);
//            String response = readProcessOutput(p);
//            logger.info(response);
            p.waitFor();
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
        	logger.info(line);
        }
        reader.close();
        // 
        BufferedReader errReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        String errLine;
        while ((errLine = errReader.readLine()) != null) {
//        	response += errLine+"\r\n";
        	logger.error(errLine);
        }
        errReader.close();
//        return response;
    }


}
