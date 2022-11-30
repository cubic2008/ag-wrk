package app;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.RandomAccessFile;
import java.util.SortedMap;
import java.util.TreeMap;

public class ProcessBigMap {

	public static void main(String[] args) {
		
		try {
//			SortedMap<String, Long> fileLocMap = buildFileLocationMap("C:\\Temp\\ag-wrk\\temp\\queryset-1.txt");
			System.out.println("Free memory: " + Runtime.getRuntime().freeMemory());
			matchFile("C:\\Temp\\ag-wrk\\temp\\queryset-1.txt", "C:\\Temp\\ag-wrk\\temp\\queryset-2.txt");
			System.out.println("Free memory: " + Runtime.getRuntime().freeMemory());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static void matchFile(String filename1, String filename2) throws FileNotFoundException, IOException {
		SortedMap<String, Long> file1LocMap = buildFileLocationMap(filename1);
//		System.out.println(file1LocMap);
		try (	RandomAccessFile file1 = new RandomAccessFile(filename1, "r");
				LineNumberReader lnReader = new LineNumberReader(new FileReader(filename2));) {
			String line;
			int counter = 0;
			while ((line = lnReader.readLine()) != null) {
				String[] parts = line.split(" ");
				if (parts.length > 1) {
//					System.out.println(">>>>>> " + line);
//					System.out.printf("%s @ %d\n", parts[0], file1LocMap.get(parts[0]));
					file1.seek(file1LocMap.get(parts[0]));
					String line2 = file1.readLine();
					if (!line.equals(line2)) {
						System.out.println("----------------------------------");
					}
//					System.out.println("<<<<<< " + line2);
//					System.out.println();
					if (counter % 500 == 0) {
//						System.gc();
						System.out.println("[M]Free memory @ " + counter + " : " + Runtime.getRuntime().freeMemory());
					}
					counter++;
				}
			}
		}
		System.out.println("Free memory: " + Runtime.getRuntime().freeMemory());
		System.gc();

	}

	private static SortedMap<String, Long> buildFileLocationMap(String filename) throws FileNotFoundException, IOException {
		SortedMap<String, Long> fileLocMap = new TreeMap<>();
		try (LineNumberReader lnReader = new LineNumberReader(new FileReader(filename))) {
			String line;
			long pos = 0;
//			int counter = 0;
			while ((line = lnReader.readLine()) != null) {
				String[] parts = line.split(" ");
				if (parts.length > 1) {
					fileLocMap.put(parts[0], pos);
					pos += (line.length() + System.lineSeparator().length());
				}
//				if (counter % 5000 == 0) {
//					System.out.println("[B]Free memory @ " + counter + " : " + Runtime.getRuntime().freeMemory());
//				}
//				counter++;
			}
		}
//		System.out.println(fileLocMap);
		return fileLocMap;
		
	}

}
