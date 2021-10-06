package com.lordUdin.downloader;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import com.lordUdin.object.Status;

public class Pool extends Thread implements Observer {
	private static Pool pool;

	private List<Manager> downloadManagers;
	private List<Thread> downloadThreads;
	private List<Manager> removedManagers;

	private Pool() {
		downloadManagers = new ArrayList<>();
		removedManagers = new ArrayList<>();
		downloadThreads = new ArrayList<>();
	}


	public static Pool getDownloadPool() {
		if (pool == null)
			pool = new Pool();

		return pool;
	}


	public Manager get(long downloadId) {
		for (Manager manager : downloadManagers) {
			if (manager.getDownloadId() == downloadId)
				return manager;
		}

		return null;
	}


	public synchronized void add(Manager manager) {
		downloadManagers.add(manager);
		manager.addObserver(this);
		start(manager);
	}

	
	private void start(Manager manager) {
		if (manager != null) {
			Thread t = new Thread(manager);
			t.setName(manager.getDownloadId() + "");

			downloadThreads.add(t);

			t.start();
		}
	}


	public synchronized void remove(Manager manager) {
		remove(manager, true);
	}


	public synchronized void remove(Manager manager, boolean stop) {
		if (manager != null) {
			if (stop)
				manager.pause();
			else {
				removedManagers.add(manager);
				downloadManagers.remove(manager);
			}
		}
	}

	public synchronized void remove(long downloadId) {
		Manager manager = get(downloadId);
		remove(manager);
	}


	public synchronized void removeAll() {
		List<Long> ids = new ArrayList<>();

		downloadManagers.forEach((manager) -> {
			ids.add(manager.getDownloadId());
		});

		ids.forEach((id) -> {
			remove(id);
		});
	}

	@SuppressWarnings("unused")
	private Thread getDownloadThread(long downloadId) {
		for (Thread t : downloadThreads) {
			if (t.getName().equals(downloadId + ""))
				return t;
		}

		return null;
	}

	public int activeDownloadCount() {
		return downloadManagers.size();
	}

	@Override
	public void update(Observable o, Object arg) {
		if (o instanceof Manager) {
			Manager manager = (Manager) o;

			Status status = manager.getStatus();
			if (status == Status.PAUSED || status == Status.ERROR) {
				remove(manager, false);
			}
		}
	}

	@Override
	protected void finalize() throws Throwable {
		removeAll();
		super.finalize();
	}
}
