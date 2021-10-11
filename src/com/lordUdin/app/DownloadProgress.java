package com.lordUdin.app;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.event.*;

import com.lordUdin.downloader.Manager;
import com.lordUdin.downloader.Metadata;
import com.lordUdin.downloader.Pool;
import com.lordUdin.downloader.Stat;
import com.lordUdin.object.PartsMetadata;
import com.lordUdin.object.Status;
import com.lordUdin.xmlHandler.XMLFactory;

import java.awt.*;
import java.io.IOException;
import java.sql.Date;

/**
 * DownloadDetail
 */
public class DownloadProgress extends JFrame implements Observer {

  JLabel downloadURL, status, filesize, downloaded, speed, progress;
  JProgressBar bar;

  public DownloadProgress(long downloadId, Pool pool, HomeLayout uiHome) {
    setAlwaysOnTop(true);
    setTitle("Download Progress");
    setLocationRelativeTo(uiHome);
    setVisible(true);

    downloadURL = new JLabel();
    status = new JLabel();
    filesize = new JLabel();
    downloaded = new JLabel();
    speed = new JLabel();
    progress = new JLabel();
    bar = new JProgressBar();
    bar.setMaximum(100);

    Manager manager = pool.get(downloadId);
    manager.addObserver(this);

    JPanel contentPane = new JPanel();
    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();

    contentPane.setLayout(gbl);
    contentPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    setContentPane(contentPane);
    gbc.insets = new Insets(0, 1, 5, 0);
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.gridx = 0;
    gbc.gridy = 0;
    contentPane.add(new Label("Download URL"), gbc);
    gbc.gridx = 1;
    gbc.gridy = 0;
    contentPane.add(downloadURL, gbc);

    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.gridx = 0;
    gbc.gridy = 1;
    contentPane.add(new Label("Status"), gbc);
    gbc.gridx = 1;
    gbc.gridy = 1;
    contentPane.add(status, gbc);

    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.gridx = 0;
    gbc.gridy = 2;
    contentPane.add(new Label("Filesize"), gbc);
    gbc.gridx = 1;
    gbc.gridy = 2;
    contentPane.add(filesize, gbc);

    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.gridx = 0;
    gbc.gridy = 3;
    contentPane.add(new Label("Downloaded"), gbc);
    gbc.gridx = 1;
    gbc.gridy = 3;
    contentPane.add(downloaded, gbc);

    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.gridx = 0;
    gbc.gridy = 4;
    contentPane.add(new Label("Download Speed"), gbc);
    gbc.gridx = 1;
    gbc.gridy = 4;
    contentPane.add(speed, gbc);

    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.gridx = 0;
    gbc.gridy = 6;
    contentPane.add(new Label("Progress"), gbc);
    gbc.gridx = 1;
    gbc.gridy = 6;
    contentPane.add(progress, gbc);

    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.gridwidth = 2;
    gbc.gridx = 0;
    gbc.gridy = 7;
    contentPane.add(bar, gbc);

    JPanel btnPane = new JPanel();
    btnPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
    JButton pauseBtn = new JButton("Pause");
    JButton cancelBtn = new JButton("Cancel");
    btnPane.add(pauseBtn);
    btnPane.add(cancelBtn);

    pauseBtn.addActionListener((e) -> {
      if (manager.getStatus() == Status.DOWNLOADING) {
        pool.remove(downloadId);
        pauseBtn.setText("Resume");
      } 
      else if(manager.getStatus()== Status.PAUSED){
        dispose();
        uiHome.addNewDownload(downloadId);
      }
    });

    cancelBtn.addActionListener((e) -> {
      if(manager.getStatus() == Status.PAUSED || manager.getStatus() == Status.DOWNLOADING){
        pool.remove(downloadId);
      }
    });

    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.gridx = 1;
    gbc.gridy = 8;
    contentPane.add(btnPane, gbc);

    update(manager, true);
    pack();

  }

  @Override
  public void update(Observable o, Object arg1) {
    // JLabel downloadURL, status, filesize, downloaded, speed, progress;
    if (o instanceof Manager) {
      Manager manager = (Manager) o;

      Metadata meta = manager.getMetadata();

      String url = meta.getUrl();
      String fSize = Stat.toMB(meta.getFileSize());

      downloadURL.setText(url);
      status.setText(manager.getStatus().name());
      filesize.setText(fSize);
      downloaded.setText(Stat.toMB(manager.getDownloadCompleted()));
      speed.setText(Stat.toSpeed(manager.getDownloadSpeed()));
      progress.setText(Stat.toProgress(manager.getDownloadCompleted(), meta.getFileSize()) + "%");
      bar.setValue((int) Stat.toProgress(manager.getDownloadCompleted(), meta.getFileSize()));

    }

  }

}