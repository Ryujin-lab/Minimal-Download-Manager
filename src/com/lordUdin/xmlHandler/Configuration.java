package com.lordUdin.xmlHandler;

import java.io.*;
import java.util.Properties;

public class Configuration {

	private final static String CONFIGURATION_FILE = "config.properties";
	
	private final static Properties prop;
	
	public final static String TEMP_DIRECTORY;
	public final static String DOWNLOAD_LIST_FILE;
	public final static String SAVED_DOWNLOADS_FILE;
	public final static String DEFAULT_DOWNLOAD_PATH;
	
	static {
		prop = new Properties();
		
		try {
			prop.load(new FileInputStream(CONFIGURATION_FILE));
		} catch (IOException e) {
			System.out.println("[ERROR] Could not load configuration file. " + e.getMessage());
			System.out.println("[ERROR] Exiting from application.");
			
			System.exit(-1);
		}
		
		TEMP_DIRECTORY 		 = prop.getProperty("TEMP_DIRECTORY");
		DOWNLOAD_LIST_FILE   = prop.getProperty("DOWNLOAD_LIST_FILE");
		SAVED_DOWNLOADS_FILE = prop.getProperty("SAVED_DOWNLOADS_FILE");
		DEFAULT_DOWNLOAD_PATH= prop.getProperty("DEFAULT_DOWNLOAD_PATH");
		

		// jika directory tidak ada, buat baru
		File file = new File(TEMP_DIRECTORY);
		if (!file.exists())
			file.mkdirs();
		
		// file xml untuk konfigursi, buffer, dan hasil download
		try {
			file = new File(DOWNLOAD_LIST_FILE);
			if(!file.exists()) 
				file.createNewFile();
			
			file = new File(SAVED_DOWNLOADS_FILE);
			if(!file.exists())
				file.createNewFile();
			
		} catch (IOException e) {
			System.out.println("[ERROR] Could not load configuration file. " + e.getMessage());
			System.out.println("[ERROR] Exiting from application.");
			
			System.exit(-1);
		}
	}

	public static void setProperty(String name, String value) {
		prop.setProperty(name, value);
		try {
			prop.store(new FileOutputStream(CONFIGURATION_FILE), "");
		} catch (IOException e) {
		}
	}
	
	public static void cleanTempFiles() {
		File dir = new File(TEMP_DIRECTORY);
		
		if(dir.exists()) {
			File[] files = dir.listFiles();
			
			for(File f : files) 
				f.delete();
		}
	}
}
