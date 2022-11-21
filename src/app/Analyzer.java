package app;

import java.util.logging.Logger;

import wrk.WrkApp;

public class Analyzer {
	
//	private static Logger logger = Logger.getLogger(WrkApp.class.getName());
	private static logging.Logger logger = new logging.Logger();

	public void analyze(String broker) {
		logger.info(String.format("Analyzing responses for broker - %s", broker));
	}

	public void postAnalyze() {
		logger.info("Analyzing responses for all brokers");
	}
}
