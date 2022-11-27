package app.config;

public class CFG {

	public final static boolean _logRemoveTempFiles = true;
	public final static boolean _logRemoveBrokerRunFiles = true;
	public final static boolean _logCleanResponseFolderBeforeRun = true;

	public static final String _logSpecFileFolder = "C:\\temp\\ag-wrk\\spec";
	public static final String _logSpecFilename = "wrk.spec";

	public static final String _logTempFolder = "c:\\temp\\ag-wrk\\temp";
	public static final String _logBrokerSpecFileFolder = "c:\\temp\\ag-wrk\\run";
	public static final String _logBrokerSpecFilenamePrefix = "wrk_brk";
	public static final String _logBrokerSpecFilenameSuffix = ".pecs";
	
//	public static final String wrkResponseFolder = "c:\\temp\\ag-work\\responses";
//	public static final String originalRequestSetFilePrefix = "reqset_";
	public static final String _logBrokerQuerySetFilePrefix = "queryset_brk";
	public static final String _logBrokerQuerySetFileSuffix = ".txt";

	// This will not be required.
	public static final String _logWrkOutputFoder = "C:\\temp\\ag-wrk\\responses";
	public static final String _logWrkOutputFilePrefix = "response_brk";
	public static final String _logWrkOutputFileSuffix = ".txt";
	////////////////////////////////////
	
//	public static final String server = "https://abc.com/query?db=x";
	
	public static final int _logNumOfBrokers = 6;
//	public static final int numOfRequestPerBlock = 1500;
	
	public static final String[] _logBrokerExperimentalServerName = {
			"lva1-app98992-1.prod.linkedin.com",
			"lva1-app98992-2.prod.linkedin.com",
			"lva1-app98992-3.prod.linkedin.com",
			"lva1-app98992-4.prod.linkedin.com",
			"lva1-app98992-5.prod.linkedin.com",
			"lva1-app98992-6.prod.linkedin.com",
			"lva1-app98992-7.prod.linkedin.com",
			"lva1-app98992-8.prod.linkedin.com",
			"lva1-app98992-9.prod.linkedin.com",
			"lva1-app98992-10.prod.linkedin.com",
			"lva1-app98992-11.prod.linkedin.com",
			"lva1-app98992-12.prod.linkedin.com",
	};
	public static final String[] _logBrokerStagingServerName = {
			"lva1-app00000-1.prod.linkedin.com",
			"lva1-app00000-2.prod.linkedin.com",
			"lva1-app00000-3.prod.linkedin.com",
			"lva1-app00000-4.prod.linkedin.com",
			"lva1-app00000-5.prod.linkedin.com",
			"lva1-app00000-6.prod.linkedin.com",
			"lva1-app00000-7.prod.linkedin.com",
			"lva1-app00000-8.prod.linkedin.com",
			"lva1-app00000-9.prod.linkedin.com",
			"lva1-app00000-10.prod.linkedin.com",
			"lva1-app00000-11.prod.linkedin.com",
			"lva1-app00000-12.prod.linkedin.com",
	};
	
	private CFG() { }

}
