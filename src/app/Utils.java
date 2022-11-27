package app;

import java.io.File;

import logging.Logger;

public class Utils {
	
	public static void deleteFile(String actionInfo, String filename) {
		deleteFile(actionInfo, new File(filename));
	}

	public static void deleteFile(String actionInfo, File file) {
		if (!file.delete()) {
			Logger.warn(String.format("[%s] \"%s\" cannot be deleted.", actionInfo, file.getAbsoluteFile()));
		} else {
			Logger.debug(String.format("[%s] \"%s\" has been deleted.", actionInfo, file.getAbsoluteFile()));
		}
	}
	
	private Utils() { }

}
