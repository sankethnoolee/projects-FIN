/**
 * 
 */
package com.fintellix.validationrestservice.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author sumeet.tripathi
 *
 */
public class FileCompressor extends TimerTask {

	private static FileCompressor instance = new FileCompressor();

	private List<String> folderPath = new ArrayList<>();

	/**
	 * 
	 */
	private FileCompressor() {
		if(Boolean.parseBoolean(ApplicationProperties.getValue("app.compression.enabled"))) {
			startTimerTask();	
		}
	}

	public static FileCompressor getInstance() {
		return instance;
	}

	@Override
	public void run() {

		List<String> folders = new ArrayList<>();
		folders.addAll(folderPath);

		for (String folder : folders) {
			Compressor.compressFiles(folder);
		}

		folderPath.removeAll(folders);
	}

	public void addFolder(String folder) {
		if(Boolean.parseBoolean(ApplicationProperties.getValue("app.compression.enabled"))) {
			folderPath.add(folder);
		}
	}

	private void startTimerTask() {
		Timer timer = new Timer(true);

		Integer delay = null;
		Integer period = null;
		try {
			delay = Integer.parseInt(ApplicationProperties.getValue("app.csv.compression.timer.delay-in-minutes"));
		} catch (Exception e) {
			// do-nothing
		}

		try {
			period = Integer.parseInt(ApplicationProperties.getValue("app.csv.compression.timer.period-in-minutes"));
		} catch (Exception e) {
			// do-nothing
		}
		
		if (delay == null) {
			delay = 2;
		}
		if (period == null) {
			period = 2;
		}
		timer.scheduleAtFixedRate(this, delay * 60 * 1000, period * 60 * 1000);
	}

}
