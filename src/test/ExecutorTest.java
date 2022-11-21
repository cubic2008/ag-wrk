package test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ExecutorTest {

	public static void main(String[] args) {
		ExecutorService executor = Executors.newFixedThreadPool(10);
//		Runnable runnableTask = () -> {
//		    try {
//		    	System.out.println("Task starts.");
//		        TimeUnit.MILLISECONDS.sleep(3000);
//		        System.out.println("Task completes.");
//		    } catch (InterruptedException e) {
//		        e.printStackTrace();
//		    }
//		};
//		Callable<String> callableTask = () -> {
//		    TimeUnit.MILLISECONDS.sleep(300);
//		    return "Task's execution";
//		};
//		List<Callable<String>> callableTasks = new ArrayList<>();
//		callableTasks.add(callableTask);
//		callableTasks.add(callableTask);
//		callableTasks.add(callableTask);
//		executor.execute(runnableTask);
//		executor.execute(runnableTask);
//		executor.execute(runnableTask);
//		executor.execute(runnableTask);
//		executor.execute(runnableTask);
//		executor.execute(runnableTask);
		for (int i = 0; i < 6; i ++ ) {
			executor.execute(new RunnableTask(i));
		}
		
		System.out.println("Shutting down...");
		executor.shutdown();
		try {
			System.out.println("Awaiting for termination . . .");
		    if (!executor.awaitTermination(3000, TimeUnit.MILLISECONDS)) {
		    	executor.shutdownNow();
		    	System.out.println("Shutdown.");
		    } 
		} catch (InterruptedException e) {
			executor.shutdownNow();
			System.out.println("Shutdown after timeout.");
		}
	}
	
	public String runBroker() {
		
		return "done";
		
	}

}

class RunnableTask implements Runnable {
	
	int brokerNo;

	public RunnableTask(int brokerNo) {
		this.brokerNo = brokerNo;
	}


	@Override
	public void run() {
	    try {
	    	System.out.println("Task starts for broker " + brokerNo + ".");
	        TimeUnit.MILLISECONDS.sleep(3000);
	        System.out.println("Task for broker " + brokerNo + " completes.");
	    } catch (InterruptedException e) {
	        e.printStackTrace();
	    }
	}
	
}
