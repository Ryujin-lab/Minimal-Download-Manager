package com.lordUdin.downloader;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Observable;

import javax.swing.JOptionPane;

import com.lordUdin.object.PartsMetadata;
import com.lordUdin.object.Status;
import com.lordUdin.xmlHandler.Configuration;
import com.lordUdin.xmlHandler.XMLFactory;

/**
 * Manages download of single file.
 *
 * @author lord udin
 *
 */
public class Manager extends Observable implements Runnable {

	private final static int MAX_WORKER	= 8;
	private final static int MIN_WORKER_DOWNLOAD_SIZE = 1024 * 1024; // 1MB

	private long downloadId;

	private Worker[] workers		= null;
	private Thread[] 		 workerThreads	= null;
	private int 			 workersAtWork	= 0;

	private String 	downloadURL			= null;
	private String 	filePath			= "";

	private long 	downloadSize		= 0l;
	private Long 	downloadCompleted	= 0l;
	private float	downloadSpeed		= 0.0f;

	private Status 	 status		= null;
	private Metadata metadata	= null;
	private List<PartsMetadata>	partsMetaList = null;


	public Manager(String downloadURL) throws IOException {
		this(downloadURL, Configuration.DEFAULT_DOWNLOAD_PATH + "");
	}

	public Manager(String downloadURL, String filePath) {
		workers 		= new Worker[MAX_WORKER];
		workerThreads 	= new Thread[MAX_WORKER];

		this.downloadURL	= downloadURL;
		status			= Status.NEW;
		this.filePath		= filePath;

		metadata = new Metadata(downloadURL);

		downloadId = metadata.getId();
	}

	public Manager(Metadata metadata, List<PartsMetadata> partsMeta) {
		workers		= new Worker[MAX_WORKER];
		workerThreads	= new Thread[MAX_WORKER];

		this.metadata		= metadata;
		downloadURL	= metadata.getUrl();

		partsMetaList = new ArrayList<>(partsMeta);

		downloadId 	= metadata.getId();
		downloadSize	= metadata.getFileSize();
		status 		= metadata.getStatus();

		workersAtWork	= partsMeta.size();

		downloadCompleted	= metadata.getCompleted();
	}

	@Override
	public void run() {
		try {
			if (status != Status.PAUSED) {
				downloadFileMetaInformation();
				updateDownloadStatus();
			}

			employWorkers();

		} catch (IOException e) {
			System.out.println("[ERROR] " + e.getMessage());

			status = Status.ERROR;
			updateDownloadStatus();

			return;
		}

		status = Status.DOWNLOADING;

		initDownloadSpeedCalculator();
		startAllWorkers();
		waitForWorkers();
		updateDownloadGUI();
		if(downloadCompleted == downloadSize) {
			status = Status.COMPLETED;

			mergeDownloadFile();
		}

		updateDownloadStatus();
	}

	public void pause() {
		status = Status.PAUSED;
	}

	public Status getStatus() {
		return status;
	}

	public Metadata getMetadata() {
		return metadata;
	}

	public long getDownloadCompleted() {
		return downloadCompleted;
	}

	public long getDownloadId() {
		return downloadId;
	}

	public float getDownloadSpeed() {
		return downloadSpeed;
	}

	private void initDownloadSpeedCalculator() {
		Thread t = new Thread(()->{
			final Date startTime = new Date();
			Date endTime;

			while(status == Status.DOWNLOADING) {
				endTime = new Date();
				if(downloadCompleted != 0)
					downloadSpeed = downloadCompleted / ((endTime.getTime() - startTime.getTime()) / 1000.0f) ;

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					break;
				}
			}
		});
		t.setName(downloadId + "Speed");
		t.start();
	}

	private void downloadFileMetaInformation() throws IOException {
		metadata.getLoadFileMetadata();

		if(metadata == null)
			throw new IOException("Unable to get download file information.");

		status	= Status.READY;
		downloadCompleted	= 0l;
		downloadSize		= metadata.getFileSize();
		downloadId	= metadata.getId();

		filePath += metadata.getFileName() + "." + metadata.getFileType();

		metadata.setFilePath(filePath);
		metadata.setStatus(status);
		metadata.setStartTime(new Date());
		metadata.setEndTime(new Date());
	}

	private void employWorkers() throws IOException {
		// If it is a new download
		if (partsMetaList == null) {
			// Create new part meta list
			partsMetaList = new ArrayList<>();

			// Calculate number of parts to be done
			int parts = (int) (downloadSize / MIN_WORKER_DOWNLOAD_SIZE);

			int totalParts = (parts > MAX_WORKER) ? MAX_WORKER : parts;

			long partDownloadSize = downloadSize / totalParts;
			long startRange = 0l;
			long endRange 	= 0l;
			for (int i = 0; i < totalParts; i++) {
				endRange = (i == totalParts - 1) ? downloadSize : startRange + partDownloadSize;

				String tempPartFile = Configuration.TEMP_DIRECTORY + metadata.getId() + ".part" + i;

				PartsMetadata partMeta = new PartsMetadata(metadata.getId(), i, startRange, endRange, tempPartFile);

				partsMetaList.add(partMeta);
				workers[i] = new Worker(partMeta);

				startRange += partDownloadSize + 1;
			}

			workersAtWork = totalParts;

		} else {
			int i = 0;
			for (PartsMetadata partMeta : partsMetaList) {
				workers[i] = new Worker(partMeta);
				i++;
			}

			workersAtWork = partsMetaList.size();
		}

		for(int i=0; i<workersAtWork; i++) {
			Thread t = new Thread(workers[i]);
			t.setName(metadata.getId() + "" + (i+1));

			workerThreads[i] = t;
		}
	}

	private void startAllWorkers()  {
		System.out.println("[INFO] Starting download threads.");

		for(int i=0; i<workersAtWork; i++)
			workerThreads[i].start();
	}


	private void waitForWorkers() {
		for(int i=0; i<workersAtWork; i++) {

			try {
				workerThreads[i].join();
			} catch (InterruptedException e) {
				System.out.println("[WARN] Download worker thread was interrupted.");
			}
		}
	}

	private void updateDownloadStatus() {
		XMLFactory factory = XMLFactory.newXMLFactory();

		try {
			metadata.setEndTime(new Date());
			metadata.setStatus(status);
			metadata.setCompleted(downloadCompleted);
			factory.updateDownloadList(metadata);
		} catch (IOException e) {
			System.out.println("[ERROR] Unable to update download metadata to file. " + e.getMessage());
		}

		setChanged();
		notifyObservers(true);
	}


	private synchronized void updatePartDownloadStatus(Worker downWorker) {
		XMLFactory factory = XMLFactory.newXMLFactory();

		PartsMetadata meta = downWorker.getPartMeta();

		long startRange		= meta.getStart();
		long totalDownloaded= downWorker.getCompleted();
		long downloadSize 	= downWorker.getDownloadSize();

		boolean completed = (totalDownloaded == downloadSize);

		// Update the start range of current part
		meta.setStart(startRange + totalDownloaded);

		try {
			if (completed)
				factory.removeSavedDownloadParts(meta);
			else
				factory.updateSavedDownloadParts(meta);
		} catch (IOException e) {
			System.out.println("[ERROR] Unable to update the download part XML. " + e.getMessage());
		}

		setChanged();
		notifyObservers();
	}

	private synchronized void updateDownloadGUI() {
		setChanged();
		notifyObservers();
	}

	private void mergeDownloadFile() {
		String filePath = metadata.getFilePath();

		System.out.println("[WAIT] Merging files please wait.");

		Collections.sort(partsMetaList, new Comparator<PartsMetadata>(){
			@Override
			public int compare(PartsMetadata o1, PartsMetadata o2) {
				if(o1.getId() > o2.getId())
					return 1;
				else if(o1.getId() == o2.getId())
					return 0;
				else
					return -1;
			}
		});

		try(BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath))) {
			for(PartsMetadata partMeta : partsMetaList) {
				String partPath = partMeta.getPath();

				try (FileInputStream fis = new FileInputStream(partPath)) {
					final int BUFFER_SIZE = 1024;

					byte[] buff = new byte[BUFFER_SIZE];
					int len;

					while((len = fis.read(buff)) != -1) {
						bos.write(buff, 0, len);
						bos.flush();
					}
				}
			}

			System.out.println("[SUCCESS] File merged successfully at - " + filePath);
		} catch (IOException e) {
			System.out.println("[ERROR] Unable to merge file at the given location.");
		}

		partsMetaList.forEach((e)->{
			File file = new File(e.getPath());

			if(file.exists())
				file.delete();
		});
	}



	/**
	 * manage ininvidual file
	 * @author lord udin
	 *
	 */
	private class Worker implements Runnable {
		private final static int BUFFER_SIZE = 1024 * 250; // 250 KB

		private final String TEMP_PATH;

		private PartsMetadata partMeta;

		@SuppressWarnings("unused")
		private int part;
		private long startRange, endRange;
		private long downloadSize;
		private long completed;

		private byte[] buffer;

		public Worker(PartsMetadata partMeta) throws IOException {
			this.partMeta	= partMeta;

			startRange = partMeta.getStart();
			endRange	= partMeta.getEnd();
			downloadSize=endRange - startRange;

			completed	= 0l;
			buffer 	= new byte[BUFFER_SIZE];
			part		= partMeta.getId();

			TEMP_PATH = partMeta.getPath();

			File tempFile = new File(TEMP_PATH);
			if(!tempFile.exists()) {
				try {
					tempFile.createNewFile();
				} catch(IOException e) {
					throw new IOException("Unable to create temporary download part file.", e);
				}
			}
		}

		@Override
		public void run() {
			updatePartDownloadStatus(this);

			try {
				URL link = new URL(downloadURL);
				HttpURLConnection conn = (HttpURLConnection) link.openConnection();

				conn.setRequestProperty("User-Agent", "Mozilla/5.0");
				conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

				if (metadata.isRangeAllowed())
					conn.setRequestProperty("Range", "bytes=" + startRange + "-" + endRange);

				final int responseCode = conn.getResponseCode();

				if (responseCode == HttpURLConnection.HTTP_OK ||
					responseCode == HttpURLConnection.HTTP_PARTIAL) {

					downloadFilePart(conn.getInputStream());

				} else {
					throw new IOException("Could not connect with server. " + responseCode);
				}

			} catch (MalformedURLException e) {
				System.out.println("[ERROR] Invalid download URL. " + e.getMessage());

				JOptionPane.showMessageDialog(null, "Invalid download URL. " + e.getMessage(), "Error downloading", JOptionPane.ERROR_MESSAGE);

			} catch (IOException e) {
				status = Status.ERROR;

				System.out.println("[ERROR] Unable to download part. " + e.getMessage());

				JOptionPane.showMessageDialog(null, e.getMessage(), "Error downloading", JOptionPane.ERROR_MESSAGE);
			}

			updatePartDownloadStatus(this);
		}


		private void downloadFilePart(InputStream stream) throws IOException {
			int len;

			try (FileOutputStream writer = new FileOutputStream(TEMP_PATH, true)) {
				while ((len = stream.read(buffer)) != -1) {

					writer.write(buffer, 0, len);
					writer.flush();

					completed += len;
					synchronized (downloadCompleted) {
						downloadCompleted += len;
					}

					updateDownloadGUI();

					if(status == Status.PAUSED || status == Status.ERROR)
						break;
				}
			} catch (IOException e) {
				status = Status.ERROR;

				System.out.println("[ERROR] Unable to read file contents. " + e.getMessage());

				updatePartDownloadStatus(this);

				throw e;
			} finally {
				stream.close();
			}
		}

		public long getCompleted() {
			return completed;
		}

		public PartsMetadata getPartMeta() {
			return partMeta;
		}

		public long getDownloadSize() {
			return downloadSize;
		}
	}
}