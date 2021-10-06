package com.lordUdin.object;

/**
 * metadata
 * 
 * @author lord udin
 *
 */
public class PartsMetadata {
	private long start;

	private final long downloadId;
	private final int id;
	private final long end;
	private final String path;

	public PartsMetadata(long downloadId, int id, long start, long end, String path) {
		this.downloadId = downloadId;
		this.id = id;
		this.start = start;
		this.end = end;
		this.path = path;
	}

	public int getId() {
		return id;
	}

	public long getStart() {
		return start;
	}

	public long getEnd() {
		return end;
	}

	public String getPath() {
		return path;
	}

	public long getDownloadId() {
		return downloadId;
	}

	public void setStart(long start) {
		this.start = start;
	}
}