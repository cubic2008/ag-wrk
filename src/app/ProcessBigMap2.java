package app;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.RandomAccessFile;
import java.util.Calendar;
import java.util.SortedMap;
import java.util.TreeMap;

import com.github.davidmoten.bigsorter.Sorter;

public class ProcessBigMap2 {

	public static void main(String[] args) {
		
//		try {
////			SortedMap<Long, Long> fileLocMap = buildFileLocationMap("C:\\Temp\\ag-wrk\\temp\\queryset-1.txt");
//			System.out.println("Free memory: " + Runtime.getRuntime().freeMemory());
//			sortFile("C:\\Temp\\ag-wrk\\temp\\queryset-1.txt", "C:\\Temp\\ag-wrk\\temp\\queryset-1b.txt");
//			System.out.println("Free memory: " + Runtime.getRuntime().freeMemory());
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		
		System.gc();
		System.out.println("Max memory: " + Runtime.getRuntime().maxMemory());
		System.out.println("Total memory: " + Runtime.getRuntime().totalMemory());
		System.out.println("Free memory: " + Runtime.getRuntime().freeMemory());
		
		Sorter
		  .serializerLinesUtf8()
		  .comparator((file1Line, file2Line) -> {
			  int a = Integer.parseInt(file1Line.split(" ")[0]);
			  int b = Integer.parseInt(file2Line.split(" ")[0]);			  
			  return Integer.compare(a, b);  
		  })
		  .input(new File("C:\\Temp\\ag-wrk\\temp\\queryset-1.txt"))
		  .filter(line -> !line.isEmpty())
		  .output(new File("C:\\Temp\\ag-wrk\\temp\\queryset-1b.txt"))
		  .sort();

	}

	private static void sortFile(String filename1, String filename2) throws FileNotFoundException, IOException {
		long timestarted = Calendar.getInstance().getTimeInMillis();
		SortedMap<Long, Long> file1LocMap = buildFileLocationMap(filename1);
//		System.out.println(file1LocMap);
		try (	RandomAccessFile file1 = new RandomAccessFile(filename1, "r");
				FileWriter writer = new FileWriter(filename2);) {
			int counter = 0;
			for (SortedMap.Entry<Long, Long> entry : file1LocMap.entrySet()) {
				file1.seek(entry.getValue());
				String line = file1.readLine();
				writer.write(line + "\n");
				if (counter % 500 == 0) {
//					System.gc();
					System.out.println("[M]Free memory @ " + counter + " : " + Runtime.getRuntime().freeMemory() + ", time elapsed: " + (Calendar.getInstance().getTimeInMillis() - timestarted));
				}
				counter++;
			}
		}
		System.out.println("Free memory: " + Runtime.getRuntime().freeMemory());
		System.gc();

	}

	private static SortedMap<Long, Long> buildFileLocationMap(String filename) throws FileNotFoundException, IOException {
		SortedMap<Long, Long> fileLocMap = new TreeMap<>();
		try (LineNumberReader lnReader = new LineNumberReader(new FileReader(filename))) {
			String line;
			long pos = 0;
//			int counter = 0;
			while ((line = lnReader.readLine()) != null) {
				String[] parts = line.split(" ");
				if (parts.length > 1) {
					fileLocMap.put(Long.parseLong(parts[0]), pos);
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
