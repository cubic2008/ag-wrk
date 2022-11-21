package logging;

public class Logger {
	
	public enum LogLevel {
		DEBUG, INFO, WARN, ERROR, FATAL
	}
	
	public static void log (LogLevel level, String message) {
		System.out.println(String.format("[%s] [%s] %s", level, Thread.currentThread().getName(), message));
	}
	
	public static void debug (String message) {
		log(LogLevel.DEBUG, message);
	}

	public static void info (String message) {
		log(LogLevel.INFO, message);
	}

	public static void warn (String message) {
		log(LogLevel.WARN, message);
	}

	public static void error (String message) {
		log(LogLevel.ERROR, message);
	}

	public static void fatal (String message) {
		log(LogLevel.FATAL, message);
	}
}
