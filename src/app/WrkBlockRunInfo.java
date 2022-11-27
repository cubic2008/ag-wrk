package app;

public class WrkBlockRunInfo {
	int brokerIndex;
	int blockIndex;
	String specFilename;
	public WrkBlockRunInfo(int brokerIndex, int blockIndex, String specFilename) {
		this.brokerIndex = brokerIndex;
		this.blockIndex = blockIndex;
		this.specFilename = specFilename;
	}

}
