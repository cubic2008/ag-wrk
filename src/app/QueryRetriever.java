package app;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.SortedMap;

import javafx.util.Pair;
import logging.Logger;

public class QueryRetriever {
	
	private SortedMap<Long, Pair<String, String>> querySpecMap;
	private LineNumberReader currLineReader = null;
	private long lastRetrievedRequestId = 1;
	private String currQuerySetFilename;
	private long currStartRequestId = -1;
	private long currEndRequestId = -1;
	
	public QueryRetriever(SortedMap<Long, Pair<String, String>> querySpecMap) {
		this.querySpecMap = querySpecMap;
	}
	
	/**
	 * Retrieve query from query set files. It retrieves the query from the currently opened query set file, if the 
	 * requestId is within the currently opened query set file and is larger than the last retrieved requestId.
	 * It will automatically reopen the current query set file if the requestId is smaller thant the last retrieved
	 * requestId. It will automatically open a new query set file if the requestid belongs to another query set file. 
	 * @param requestId 1-based request id
	 * @return the retrieved query corresponding to the requestId.
	 * @throws IOException 
	 */
	public String getQuery(long requestId) throws IOException {
//		Logger.debug("requestId = " + requestId + " ==> ");
		if (requestId <= 0) {
			return null;
		}
		if (this.currLineReader == null || requestId < lastRetrievedRequestId || requestId > this.currEndRequestId) {
			locateQuerySetFile(requestId);
//			Logger.debug("currQuerySetFilename = " + currQuerySetFilename);
			if (currQuerySetFilename == null) {
				return null;
			}
			if (this.currLineReader != null) {
				this.currLineReader.close();
//				Logger.debug("Query set file closed.");
			}
			this.currLineReader = new LineNumberReader(new FileReader(this.currQuerySetFilename));
//			Logger.debug(String.format("Open query set file: %s", this.currQuerySetFilename));
			this.lastRetrievedRequestId = this.currStartRequestId;
		}
		String query = null;
		for (; this.lastRetrievedRequestId <= requestId; this.lastRetrievedRequestId++) {
			query = this.currLineReader.readLine();
		}
		return query;
	}

	private void locateQuerySetFile(long requestId) {
		for (SortedMap.Entry<Long, Pair<String, String>> entry : querySpecMap.entrySet()) {
			if (requestId < entry.getKey()) {
				this.currEndRequestId = entry.getKey() - 1;
				break;
			}
			this.currStartRequestId = entry.getKey();
			this.currQuerySetFilename = entry.getValue().getKey();
		}
	}

}
