package wrk;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class SpecFileGenerator {

	public static void main(String[] args) {

		try {
			generateSpecFiles("C:\\Temp\\ag-wrk\\spec\\wrk.spec", "C:\\Temp\\ag-wrk\\spec\\", "reqset_", ".txt");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void generateSpecFiles(String specFile, String requestSetFileFolder, 
			String requestSetFilePrefix, String requestSetFileSuffix) throws IOException {
		int requestCount = 1;
		try (FileWriter writer = new FileWriter(specFile)) {
			for (int i = 0; i < 15; i ++) {
				System.out.println("Generating spec request set . . . " + (i+1) );
				writer.write(String.format("1.0,%s%d%s,XXXXXX\n", requestSetFilePrefix, i, requestSetFileSuffix));
				try (FileWriter reqWriter = new FileWriter(String.format("%s%s%s%d%s", 
						requestSetFileFolder, File.separator, requestSetFilePrefix, i, requestSetFileSuffix ))) {
					int totalLines = (int) (Math.random() * 20000);
					for (int j = 0; j < totalLines; j ++) {
						reqWriter.write(String.format("https://server.com/query?reqid=%d\n", requestCount++));
					}
					reqWriter.close();
				}
			}
			writer.close();
		}
		System.out.println("Completed.");
	}

}
