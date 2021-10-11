package com.lordUdin.app;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.MouseInputListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.lordUdin.downloader.Manager;
import com.lordUdin.downloader.Metadata;
import com.lordUdin.downloader.Pool;
import com.lordUdin.downloader.Stat;
import com.lordUdin.object.PartsMetadata;
import com.lordUdin.object.Status;
import com.lordUdin.xmlHandler.Configuration;
import com.lordUdin.xmlHandler.XMLFactory;

public class HomeLayout extends JFrame implements Observer {
	private static final long serialVersionUID = 1L;

	public final static SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd MMM yyyy hh:mm:ss aa");

	private NewDownload uiNewDownload = null;
	private Pool pool = Pool.getDownloadPool();

	private JPanel header;
	private JPanel contentPane;
	private JTable table;
	private JPanel footer;

	@SuppressWarnings({ "unused", "serial" })
	public HomeLayout() {
		final int COLUMN_DOWNLOADID = 0;
		final int COLUMN_FILENAME = 1;
		final int COLUMN_FILEPATH = 2;
		final int COLUMN_URL = 3;
		final int COLUMN_SIZE = 4;
		final int COLUMN_STATUS = 5;
		final int COLUMN_DOWNLOADED = 6;
		final int COLUMN_SPEED = 7;
		final int COLUMN_TIMEREM = 8;
		final int COLUMN_STARTTIME = 9;
		final int COLUMN_ENDTIME = 10;

		final HomeLayout uiRef = this;

		setTitle("Minimal Download Manager");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 950, 570);
		setLocationByPlatform(true);
		addWindowListener(new WindowListener() {
			@Override
			public void windowOpened(WindowEvent e) {
			}

			@Override
			public void windowIconified(WindowEvent e) {
			}

			@Override
			public void windowDeiconified(WindowEvent e) {
			}

			@Override
			public void windowDeactivated(WindowEvent e) {
			}

			@Override
			public void windowClosing(WindowEvent e) {
				pool.removeAll();

				setVisible(false);
				try {
					Thread.sleep(5000);
				} catch (InterruptedException ex) {
				}
			}

			@Override
			public void windowClosed(WindowEvent e) {
			}

			@Override
			public void windowActivated(WindowEvent arg0) {
			}
		});

		JButton newDownload = new JButton("Download");

		newDownload.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				menuNewFileClick(arg0);
			}
		});

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		setContentPane(mainPanel);
		// BoxLayout mainLayout = new BoxLayout(, axis)

		// header
		header = new JPanel();
		header.setLayout(new FlowLayout(FlowLayout.LEFT));

		JButton pauseDownload = new JButton("Pause");
		JButton cancelDownload = new JButton("Cancel");
		JButton resumeDownload = new JButton("Resume");

		JButton openfile = new JButton("Open File");
		JButton openfileloc = new JButton("Open File Location");

		header.add(newDownload);

		contentPane = new JPanel();
		footer = new JPanel();

		mainPanel.add(header);
		mainPanel.add(contentPane);

		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[] { 200 };
		gbl_contentPane.rowHeights = new int[] { 363, 0, 0 };
		gbl_contentPane.columnWeights = new double[] { 1.0 };
		gbl_contentPane.rowWeights = new double[] { 1.0, 0.0, Double.MIN_VALUE };
		contentPane.setLayout(gbl_contentPane);

		JScrollPane scrollPane = new JScrollPane();

		JPopupMenu popupMenu = new JPopupMenu();

		JMenuItem popupProgress = new JMenuItem("Show Progress");
		JMenuItem popupPause = new JMenuItem("Pause");
		JMenuItem popupResume = new JMenuItem("Resume");
		JMenuItem popupCancel = new JMenuItem("Cancel");
		JMenuItem popupOpenFile = new JMenuItem("Open file");
		JMenuItem popupOpenDir = new JMenuItem("Open directory");
		JMenuItem popupClear = new JMenuItem("Clear download list");
		JMenuItem popupRemove = new JMenuItem("Remove from download list");

		popupMenu.add(popupProgress);
		popupMenu.addSeparator();
		popupMenu.add(popupPause);
		popupMenu.add(popupResume);
		popupMenu.add(popupCancel);
		popupMenu.add(popupOpenFile);
		popupMenu.add(popupOpenDir);
		popupMenu.addSeparator();
		popupMenu.add(popupRemove);
		popupMenu.add(popupClear);

		popupMenu.addPopupMenuListener(new PopupMenuListener() {
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				int selectedRowCount = table.getSelectedRowCount();

				if (selectedRowCount == 0) {
					popupPause.setEnabled(false);
					popupResume.setEnabled(false);
					popupCancel.setEnabled(false);
					popupRemove.setEnabled(false);
					popupOpenDir.setEnabled(false);
					popupOpenFile.setEnabled(false);
				} else {
					int selectedRow = table.getSelectedRow();

					popupOpenFile.setEnabled(false);

					/*
					 * Disable pause, resume and cancel button based on downloading status.
					 */
					String value = table.getValueAt(selectedRow, 5) + "";
					Status downStatus = Status.valueOf(value);

					switch (downStatus) {
						case DOWNLOADING:
							popupResume.setEnabled(false);
							break;
						case ERROR:
							popupProgress.setEnabled(false);
							popupCancel.setEnabled(false);
							popupPause.setEnabled(false);
							break;
						case COMPLETED:
							popupProgress.setEnabled(false);
							popupPause.setEnabled(false);
							popupResume.setEnabled(false);
							popupCancel.setEnabled(false);
							popupOpenFile.setEnabled(true);
							break;
						case NEW:
							popupProgress.setEnabled(false);
							popupPause.setEnabled(false);
							popupResume.setEnabled(false);
							popupCancel.setEnabled(false);
							break;
						case READY:
							popupProgress.setEnabled(false);
							popupPause.setEnabled(false);
							popupResume.setEnabled(false);
							break;
						case PAUSED:
							popupProgress.setEnabled(false);
							popupPause.setEnabled(false);
							popupCancel.setEnabled(false);
							break;
					}
				}

			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				popupProgress.setEnabled(true);
				popupPause.setEnabled(true);
				popupResume.setEnabled(true);
				popupCancel.setEnabled(true);
				popupRemove.setEnabled(true);
				popupOpenDir.setEnabled(true);
				popupOpenFile.setEnabled(true);
			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
				popupMenuWillBecomeInvisible(e);
			}
		});

		popupProgress.addActionListener((e) -> {
			int selectedRow = table.getSelectedRow();

			if (selectedRow >= 0) {
				Long downloadId = Long.parseLong((String) table.getValueAt(selectedRow, 0));
				new DownloadProgress(downloadId, pool, this);
			} else {
				System.out.println("no row selected");
			}
		});

		popupPause.addActionListener((e) -> {
			int selectedRow = table.getSelectedRow();

			if (selectedRow >= 0) {
				Long downloadId = Long.parseLong((String) table.getValueAt(selectedRow, 0));
				pool.remove(downloadId);

				// Set the current status as downloading
				table.setValueAt(Status.DOWNLOADING, selectedRow, COLUMN_STATUS);
			}
		});

		popupResume.addActionListener((e) -> {
			int selectedRow = table.getSelectedRow();

			if (selectedRow >= 0) {
				Long downloadId = Long.parseLong((String) table.getValueAt(selectedRow, COLUMN_DOWNLOADID));

				addNewDownload(downloadId);
			}
		});

		popupCancel.addActionListener((e) -> {
			int selectedRow = table.getSelectedRow();

			if (selectedRow >= 0) {
				Long downloadId = Long.parseLong((String) table.getValueAt(selectedRow, COLUMN_DOWNLOADID));

				pool.remove(downloadId);
			}
		});

		popupOpenFile.addActionListener((e) -> {
			int selectedRow = table.getSelectedRow();

			if (selectedRow != -1) {
				String path = (String) table.getValueAt(selectedRow, COLUMN_FILEPATH);

				try {
					if (Desktop.isDesktopSupported())
						Desktop.getDesktop().open(new File(path));
				} catch (IOException ex) {
					JOptionPane.showMessageDialog(uiRef, "Unable to open the selected file.\n" + ex.getMessage(), "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		popupOpenDir.addActionListener((e) -> {
			int selectedRow = table.getSelectedRow();

			if (selectedRow != -1) {
				String fname = (String) table.getValueAt(selectedRow, COLUMN_FILENAME);
				String fpath = (String) table.getValueAt(selectedRow, COLUMN_FILEPATH);

				String path = fpath.substring(0, fpath.lastIndexOf(fname));

				try {
					if (Desktop.isDesktopSupported())
						Desktop.getDesktop().open(new File(path));
				} catch (IOException ex) {
					JOptionPane.showMessageDialog(uiRef, "Unable to open the selected file.\n" + ex.getMessage(), "Error",
							JOptionPane.ERROR_MESSAGE);
				}

			}
		});

		popupClear.addActionListener((e) -> {
			XMLFactory factory = XMLFactory.newXMLFactory();
			try {
				int activeDownloads = pool.activeDownloadCount();

				if (activeDownloads >= 1) {
					int choice = JOptionPane.showConfirmDialog(uiRef, "This will stop all active downloads. Sure to clear list?",
							"Confirm", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

					if (choice != JOptionPane.YES_OPTION)
						return;
				}

				factory.clearDownloadList();

				Configuration.cleanTempFiles();

				DefaultTableModel model = (DefaultTableModel) table.getModel();
				model.getDataVector().clear();
				table.setModel(model);
				model.fireTableDataChanged();

			} catch (IOException ex) {
				JOptionPane.showMessageDialog(uiRef, "Unable to clear download list. \n" + ex.getMessage(), "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		});

		popupRemove.addActionListener((e) -> {
			int selectedRow = table.getSelectedRow();

			if (selectedRow != -1) {
				long downloadId = Long.parseLong((String) table.getValueAt(selectedRow, COLUMN_DOWNLOADID));

				XMLFactory factory = XMLFactory.newXMLFactory();

				try {
					factory.removeDownloadMetadata(downloadId);

					DefaultTableModel model = (DefaultTableModel) table.getModel();
					model.removeRow(selectedRow);
					table.setModel(model);
				} catch (IOException ex) {
					JOptionPane.showMessageDialog(uiRef, "Unable to remove selected from download list.\n" + ex.getMessage(),
							"Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		contentPane.add(scrollPane, gbc_scrollPane);

		table = new JTable();
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scrollPane.setViewportView(table);
		table.setSurrendersFocusOnKeystroke(true);
		table.setModel(new DefaultTableModel(new Object[][] {}, new String[] { "", "File name", "Path", "URL", "Size",
				"Status", "Downloaded", "Transfer rate", "Time Remaining", "Started on", "Completed on" }) {
			boolean[] columnEditables = new boolean[] { false, false, false, false, false, false, false, false, false, false,
					false };

			@Override
			public boolean isCellEditable(int row, int column) {
				return columnEditables[column];
			}

		});
		table.getColumnModel().getColumn(0).setResizable(false);
		table.getColumnModel().getColumn(0).setPreferredWidth(0);
		table.getColumnModel().getColumn(0).setMinWidth(0);
		table.getColumnModel().getColumn(0).setMaxWidth(0);
		table.getColumnModel().getColumn(1).setPreferredWidth(167);
		table.getColumnModel().getColumn(2).setPreferredWidth(265);
		table.getColumnModel().getColumn(5).setPreferredWidth(100);
		table.getColumnModel().getColumn(7).setPreferredWidth(90);
		table.getColumnModel().getColumn(8).setPreferredWidth(84);
		table.getColumnModel().getColumn(9).setPreferredWidth(150);
		table.getColumnModel().getColumn(10).setPreferredWidth(150);
		table.getTableHeader().setReorderingAllowed(false);
		table.setBackground(UIManager.getColor("inactiveCaptionBorder"));
		table.setComponentPopupMenu(popupMenu);
		table.setDefaultRenderer(Object.class, new CellRendered());
		table.setRowHeight(20);
		table.setAutoCreateRowSorter(true);

		JPanel panel = new JPanel();
		panel.setSize(new Dimension(0, 30));
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.fill = GridBagConstraints.HORIZONTAL;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 1;
		contentPane.add(panel, gbc_panel);
		panel.setLayout(new BorderLayout(0, 0));

		populateDownloadList();
	}

	@Override
	public void update(Observable o, Object arg) {
		final int COLUMN_STATUS = 5;
		final int COLUMN_DOWNLOADED = 6;
		final int COLUMN_TRANSFER_RATE = 7;

		if (o instanceof Manager) {
			Manager manager = (Manager) o;

			int rowToUpdate = getDownloadRow(manager.getDownloadId());

			String status = manager.getStatus().name();
			String completed = Stat.toMB(manager.getDownloadCompleted());
			String transferRate = Stat.toSpeed(manager.getDownloadSpeed());

			DefaultTableModel model = (DefaultTableModel) table.getModel();
			int totalRows = model.getRowCount();

			if (arg != null) {
				Metadata meta = manager.getMetadata();

				String downloadId = meta.getId() + "";
				String fileName = meta.getFileName() + "." + meta.getFileType();
				String filePath = meta.getFilePath();
				String url = meta.getUrl();
				String fileSize = Stat.toMB(meta.getFileSize());
				String timeRemaining = "";

				Date startTime = meta.getStartTime();
				String startedOn = (startTime == null) ? "" : DATE_FORMATTER.format(startTime);

				Date endTime = meta.getEndTime();
				String completedOn = (endTime == null) ? ""
						: (meta.getStatus() == Status.COMPLETED) ? DATE_FORMATTER.format(endTime) : "-";

				Object[] rowData = new Object[] { downloadId, fileName, filePath, url, fileSize, status, completed,
						transferRate, timeRemaining, startedOn, completedOn };

				if (rowToUpdate == -1) {
					model.addRow(rowData);
					model.fireTableRowsInserted(totalRows, totalRows);
				} else {
					model.setValueAt(fileName, rowToUpdate, 1);
					model.setValueAt(filePath, rowToUpdate, 2);
					model.setValueAt(fileSize, rowToUpdate, 4);
					model.setValueAt(status, rowToUpdate, 5);
					model.setValueAt(completed, rowToUpdate, 6);
					model.setValueAt(transferRate, rowToUpdate, 7);
					model.setValueAt(timeRemaining, rowToUpdate, 8);
					model.setValueAt(startedOn, rowToUpdate, 9);
					model.setValueAt(completedOn, rowToUpdate, 10);

					model.fireTableRowsUpdated(rowToUpdate, rowToUpdate);
				}
			} else {

				model.setValueAt(status, rowToUpdate, COLUMN_STATUS);
				model.setValueAt(completed, rowToUpdate, COLUMN_DOWNLOADED);
				model.setValueAt(transferRate, rowToUpdate, COLUMN_TRANSFER_RATE);

				model.fireTableCellUpdated(rowToUpdate, COLUMN_STATUS);
				model.fireTableCellUpdated(rowToUpdate, COLUMN_DOWNLOADED);
			}
		}
	}

	private void populateDownloadList() {
		XMLFactory factory = XMLFactory.newXMLFactory();

		List<Metadata> metadata = null;

		try {
			metadata = factory.getDownloadList();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this,
					"Oops! something went wrong while gathering download list.\n" + e.getMessage(), "Error loading",
					JOptionPane.ERROR_MESSAGE);
		}

		if (metadata != null) {
			DefaultTableModel model = (DefaultTableModel) table.getModel();

			metadata.forEach((meta) -> {
				// "", "File name", "Path", "URL", "Size", "Status", "Downloaded", "Transfer
				// rate", "Time Remaining", "Started on", "Completed on"
				String id = meta.getId() + "";
				String fname = meta.getFileName() + "." + meta.getFileType();
				String fpath = meta.getFilePath();
				String url = meta.getUrl();
				String size = Stat.toMB(meta.getFileSize());
				String status = meta.getStatus().name();
				String completed = Stat.toMB(meta.getCompleted());
				String transfer = "";
				String timeRem = "";

				Date startTime = meta.getStartTime();
				String startedOn = (startTime == null) ? "" : DATE_FORMATTER.format(startTime);

				Date endTime = meta.getEndTime();
				String complOn = (endTime == null) ? "" : DATE_FORMATTER.format(endTime);

				Object[] rowData = new Object[] { id, fname, fpath, url, size, status, completed, transfer, timeRem, startedOn,
						complOn };

				model.addRow(rowData);
			});

			table.setModel(model);
		}

	}

	private int getDownloadRow(long downloadId) {
		DefaultTableModel model = (DefaultTableModel) table.getModel();

		for (int i = 0; i < model.getRowCount(); i++) {
			String curId = (String) model.getValueAt(i, 0);

			if (curId.equals(downloadId + ""))
				return i;
		}

		return -1;
	}

	private void menuNewFileClick(ActionEvent evt) {
		if (uiNewDownload == null)
			uiNewDownload = new NewDownload(this);

		if (uiNewDownload.isVisible())
			uiNewDownload.requestFocus();
		else
			uiNewDownload.setVisible(true);
	}

	public void addNewDownload(String url, String filePath) {
		Manager downManager = new Manager(url, filePath);
		downManager.addObserver(this);
		pool.add(downManager);
		update(downManager, true);
		new DownloadProgress(downManager.getDownloadId(), pool, this);
	}

	public void addNewDownload(long downloadId) {
		XMLFactory factory = XMLFactory.newXMLFactory();

		try {
			Metadata meta = factory.getDownloadMetadata(downloadId);
			List<PartsMetadata> parts = factory.getDownloadPartsList(downloadId);

			Manager manager = new Manager(meta, parts);
			manager.addObserver(this);

			pool.add(manager);
			new DownloadProgress(downloadId, pool, this);
		} catch (IOException ex) {
			JOptionPane.showMessageDialog(this, "Unable to resume download. \n" + ex.getMessage(), "Error resuming",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Custom cell untk download tabel
	 * 
	 * @author lord udin
	 *
	 */
	private class CellRendered extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;

		private final Color COLOR_BACKGROUND = new Color(-2691076); // Light blue
		private final Color COLOR_BACKGROUND_ALT = new Color(-331279); // Light red
		private final Color COLOR_SELECTED = new Color(-13335895); // Blue

		private final int PADDING_HORIZONTAL = 5;
		private final int PADDING_VERTICAL = 10;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			Component cellComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

			if (row % 2 == 0)
				cellComponent.setBackground(COLOR_BACKGROUND);
			else
				cellComponent.setBackground(COLOR_BACKGROUND_ALT);

			if (isSelected)
				cellComponent.setBackground(COLOR_SELECTED);

			setBorder(
					BorderFactory.createEmptyBorder(PADDING_VERTICAL, PADDING_HORIZONTAL, PADDING_VERTICAL, PADDING_HORIZONTAL));

			return cellComponent;
		}
	}
}
