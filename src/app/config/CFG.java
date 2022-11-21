package app.config;

public class CFG {
//	"java", "-cp", "C:\\work\\workspaces\\java-se3(2)\\ag-wrk\\bin", "wrk.WrkApp", 
//	"-spec", "C:\\work\\workspaces\\java-se3(2)\\ag-wrk\\wrk.specs", 
//	"-server", "https://abc.com/query?db=x", 
//	"-output", "C:\\work\\workspaces\\java-se3(2)\\ag-wrk\\response.txt"

	public static final String specFileFolder = "C:\\temp\\ag-wrk\\spec";
	public static final String originalSpecFilename = "wrk.spec";
	public static final String newSpecFilenamePrefix = "wrk_";
	public static final String newSpecFilenameSuffix = ".pecs";
	public static final String wrkResponseFolder = "c:\\temp\\ag-work\\responses";
	public static final String originalRequestSetFilePrefix = "reqset_";
	public static final String newRequestSetFilePrefix = "reqset_";
	public static final String requestSetFileSuffix = ".txt";
	
	public static final String wrkOutputFoder = "C:\\temp\\ag-wrk\\responses";
	public static final String wrkOutputFilePrefix = "response_";
	public static final String wrkOutputFileSuffix = ".txt";
	
	public static final String server = "https://abc.com/query?db=x";
	
	public static final int numOfBrokers = 6;
	public static final int numOfRequestPerBlock = 1500;

}
