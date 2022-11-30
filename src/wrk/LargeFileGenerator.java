package wrk;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LargeFileGenerator {

	public static void main(String[] args) {

		try {
			generateLargeFile("C:\\Temp\\ag-wrk\\temp\\queryset-1.txt");
			generateLargeFile("C:\\Temp\\ag-wrk\\temp\\queryset-2.txt");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void generateLargeFile(String querySetFile) throws IOException {
		List<Integer> requestIds = new ArrayList<>();
		for (int i = 0; i < 100000; i ++) {
			requestIds.add(i + 1);
		}
		StringBuilder sb = new StringBuilder("");
		for (int j = 0; j < 8000; j++) {
			sb.append("AAAAAAAAAA");
		}
		System.out.println("Generating large query set . . . " + querySetFile);
		try (FileWriter writer = new FileWriter(querySetFile)) {
			for (int i = 0; i < 100000; i ++) {
 				int requestId = requestIds.remove((int)(Math.random() * requestIds.size()));
 				writer.write(String.format("%d [https://server.com/query?reqid=%d%s]%n", requestId, requestId, sb.toString()));
			}
		}
		System.out.println("Completed.");
	}

}
