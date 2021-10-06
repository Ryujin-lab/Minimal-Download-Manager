package com.lordUdin.downloader;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;

import com.lordUdin.object.Status;

/**
 * deskripsi metadata setip download file
 * @author lord udin
 *
 */
public class Metadata {
	private final long		id;
	private final String 	url;

	private Date	startTime;
	private Date	endTime;
	private String	filePath;

	private Status status;
	private long 	completed;

	private String 	fileName;
	private String 	fileType;
	private long 	fileSize;
	private boolean rangeAllowed;

	public Metadata(String link) {
		// Generate a new metadata ID
		id  = new Date().getTime();
		url = link;
		fileName = "";
		fileType = "";
	}

	public Metadata(long id, String url, Date startTime, Date endTime, String filePath, Status status,
			long downloaded, String fileName, String fileType, long fileSize, boolean rangeAllowed) {
		this.id = id;
		this.url 		= url;
		this.startTime	= startTime;
		this.endTime 	= endTime;
		this.filePath	= filePath;
		this.status		= status;
		completed = downloaded;

		this.fileName	= fileName;
		this.fileType	= fileType;
		this.fileSize	= fileSize;
		this.rangeAllowed = rangeAllowed;
	}

	/**
	 * metadata, seperti ukuran file, nama  file, format file, dll
	 * @throws IOException
	 */	
	public void getLoadFileMetadata() throws IOException {
		URL link = new URL(url);

		HttpURLConnection conn = (HttpURLConnection) link.openConnection();

		// set connection 
		conn.setRequestProperty("User-Agent", "Mozilla/5.0");
		conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

		final int responseCode = conn.getResponseCode();

		// jika response ok, (200), get metadat
		if(responseCode == HttpURLConnection.HTTP_OK) {
			System.out.println("[SUCCESS] Connected to server. Gathering file info. ");

			fileSize = conn.getContentLengthLong();
			completed = 0l;

			String contentDisposition = conn.getHeaderField("Content-Disposition");
			if(contentDisposition == null || contentDisposition.isEmpty()) {
				try {
					URI uri 	= link.toURI();
					String path = uri.getPath();

					path = path.substring(path.lastIndexOf("/") + 1);

					String fname = path.substring(0, path.lastIndexOf("."));
					String ftype = path.substring(path.lastIndexOf(".") + 1);

					fileName = fname;
					fileType = ftype;
				} catch (URISyntaxException e) {
					System.out.println("[ERROR] Unable to get file info.");

					throw new IOException(e);
				} // End of try catch

			} else {
				/*
				 * Get details dari header
				 */
				String[] details = contentDisposition.split(";");

				for(String detail : details) {
					if(detail.contains("filename")) {
						String fileDetails = detail.split("=")[1];

						String fname = fileDetails.substring(1, fileDetails.lastIndexOf("."));
						String ftype = fileDetails.substring(fileDetails.lastIndexOf(".") + 1, fileDetails.length() - 1);

						fileName = fname;
						fileType = ftype;
					}
				}
			} 

			String range = conn.getHeaderField("Accept-Ranges");

			if(range == null || range.isEmpty()) {
				rangeAllowed = false;
			} else {
				rangeAllowed = true;
			}
		}
	}

	public String getFileName() {
		return fileName;
	}

	public String getFileType() {
		return fileType;
	}

	public long getFileSize() {
		return fileSize;
	}


	public boolean isRangeAllowed() {
		return rangeAllowed;
	}


	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}


	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}


	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public long getId() {
		return id;
	}

	public String getUrl() {
		return url;
	}


	public Status getStatus() {
		return status;
	}

	public void setStatus(Status completed) {
		status = completed;
	}

	public long getCompleted() {
		return completed;
	}

	public void setCompleted(long completed) {
		this.completed = completed;
	}
}