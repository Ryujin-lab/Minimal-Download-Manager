package com.lordUdin.downloader;

public class Stat {
  public static  String toMB(long bytes) {
		if (bytes == 0)
			return "";

		float mb = bytes / (1024.0f * 1024.0f);

		return String.format("%.2f MB", mb);
	}

	public static  String toSpeed(float rate) {
		String[] SPEED = new String[] { "Bps", "KBps", "MBps", "GBps", "TBps" };

		if (rate == Float.POSITIVE_INFINITY || rate == Float.NEGATIVE_INFINITY) {

			return String.format("0 %s", SPEED[0]);
		}

		int i = 0;

		while (i < SPEED.length && rate > 1024) {
			if (rate != 0)
				rate = rate / 1024.0f;
			i++;
		}

		return String.format("%.2f %s", rate, SPEED[i]);
	}

  public static int toProgress(double current, double all) {
    if (current != 0 && all != 0) {
      return (int) (current * 100 / all);
    } else {
      return 0;
    }
  }

}
