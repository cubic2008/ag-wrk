package wrk;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class WrkApp {
	
	
	private String specFilename;
	private String outputFile;
	private String server;

	public static void main(String[] args) throws IOException {
		WrkApp app = new WrkApp();
		app.readConfig(args);
//		app.process();
		System.out.println("Wrk job has completed.");
		
	}
	
	private void readConfig (String[] args) {
		for (int i = 0; i < args.length; i ++) {
			if ("-spec".equalsIgnoreCase(args[i])) {
				specFilename = args[++i];
			} else if ("-server".equalsIgnoreCase(args[i])) {
				server = args[++i];
			} else if ("-output".equalsIgnoreCase(args[i])) {
				outputFile = args[++i];
			} 
		}
		System.out.println("specFilename = " + specFilename);
		System.out.println("outputFile = " + outputFile);
		System.out.println("server = " + server);
	}
	
	private void process ( ) throws IOException {
		System.out.println("Start processing . . .");
		String dataSetFilename;
		try ( LineNumberReader specReader = new LineNumberReader(new FileReader(specFilename))){
			dataSetFilename = new File(specFilename).getParent() + File.separator + specReader.readLine().split(",")[1];
		}
		System.out.println(String.format("Wrk - processing spec file %s and request set file %s . . .", specFilename, dataSetFilename));
		try ( LineNumberReader reqSetReader = new LineNumberReader(new FileReader(dataSetFilename));
			  FileWriter fwriter = new FileWriter(outputFile); ) {
			String line = reqSetReader.readLine();
			while( line != null ) {
				fwriter.write( String.format("Response[%s]: for %s\n", Calendar.getInstance().getTime().toString(), line ) );
				line = reqSetReader.readLine();
			}
		}
		
		try {
			TimeUnit.MILLISECONDS.sleep((long) (Math.random()*1000));
		} catch (InterruptedException e) {}
		System.out.println(String.format("Wrk - processing spec file %s: completed", specFilename));
	}

}
