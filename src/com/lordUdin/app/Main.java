package com.lordUdin.app;

import java.awt.EventQueue;

/**
 * @author lord udin
 *
 */
public class Main {


	public static void main(String[] args) {

		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					HomeLayout frame = new HomeLayout();
					frame.setVisible(true);
				} catch (Exception e) {
					System.err.print("[ERROR] Unable to start application. " + e.getMessage());
					System.exit(-1);
				}
			}
		});
	}

}