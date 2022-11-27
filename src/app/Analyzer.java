package app;

import java.util.logging.Logger;

import wrk.WrkApp;

public class Analyzer {
	
//	private static Logger logger = Logger.getLogger(WrkApp.class.getName());
	private static logging.Logger logger = new logging.Logger();

	public void analyze(int brokerIndex, String responseFilename) {
		logger.info(String.format("Analyzing responses(%s) for broker - %s", responseFilename, brokerIndex));
	}

	public void postAnalyze() {
		logger.info("Analyzing responses for all brokers");
	}
}
