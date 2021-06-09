package com.fintellix.validationrestservice.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fintellix.redis.CacheCoordinator;
import com.fintellix.redis.RedisKeys;
import com.fintellix.validationrestservice.core.directoryhandler.DirectoryManager;
import com.fintellix.validationrestservice.core.metadataresolver.MetadataResolver;
import com.fintellix.validationrestservice.core.resultwriter.ExpressionResultManager;
import com.fintellix.validationrestservice.definition.ValidationConstants;
import com.fintellix.validationrestservice.util.ApplicationProperties;
import com.fintellix.validationrestservice.util.ValidationStringUtils;

@Component
public class ValidationCsvDeleteScheduler extends TimerTask {
	protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

	@Autowired
	private ExpressionResultManager expressionResultManager;
	
	@Autowired 
	private DirectoryManager directoryManager;
	
	private List<Integer> runIds;

	public ValidationCsvDeleteScheduler() {
		if (Boolean.parseBoolean(ApplicationProperties.getValue("app.validations.iscsvdeleterequired"))) {
			startCsvDeletionTimer();
			runIds = new ArrayList<>();
		}

	}

	public void addRunIds(Integer currentRunId) {
		if (Boolean.parseBoolean(ApplicationProperties.getValue("app.validations.iscsvdeleterequired"))) {
			runIds.add(currentRunId);
		}

	}

	private void startCsvDeletionTimer() {
		Timer timer = new Timer(true);

		Integer delay = null;
		Integer period = null;
		try {
			delay = Integer.parseInt(ApplicationProperties.getValue("app.csv.delete.timer.delay-in-minutes"));
			period = Integer.parseInt(ApplicationProperties.getValue("app.csv.delete.timer.period-in-minutes"));
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

	@Override
	public void run() {

		List<Integer> currentRunIds = new ArrayList<>();
		currentRunIds.addAll(runIds);
		for (Integer currentRunId : currentRunIds) {
			deleteValidationCsv(currentRunId);
		}
		runIds.removeAll(currentRunIds);
	}

	@SuppressWarnings("unchecked")
	private void deleteValidationCsv(Integer currentRunId) {
		MetadataResolver resolver = new MetadataResolver();
		Map<Integer, List<Integer>> csvDeleteDetails = new HashMap<>();
		// List<Integer> runIds = new ArrayList<Integer>();
		try {
			csvDeleteDetails = (Map<Integer, List<Integer>>) CacheCoordinator.get(
					RedisKeys.CONFIGURED_VALIDATION_CSV_RESULTS_DELETE.getKey(),
					ValidationConstants.CURRENT_RUN_ID + "_" + currentRunId);
			String outputDirectory = "";
			if (csvDeleteDetails != null) {
				for (Integer key : csvDeleteDetails.keySet()) {
					List<Integer> runIds = csvDeleteDetails.get(key);
					for (Integer runId : runIds) {
						outputDirectory = ApplicationProperties.getValue("app.validations.outputDirectory").trim()
								+ runId + File.separator;
						outputDirectory = ValidationStringUtils.replace(outputDirectory, "\\", "/", -1, true);
						File folder = new File(outputDirectory);
						try {
							FileUtils.cleanDirectory(folder);
						} catch (IOException e) {
							e.printStackTrace();
						}
						deleteFile(outputDirectory);
						
						expressionResultManager.deleteExpressionResultByRunId(runId);
						
						resolver.saveValidationResultsCsvDeleteDetails(key, runId);
					}
				}
				CacheCoordinator.delete(RedisKeys.CONFIGURED_VALIDATION_CSV_RESULTS_DELETE.getKey(),
						ValidationConstants.CURRENT_RUN_ID + "_" + currentRunId);
			}

		} catch (Throwable e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		// resolver.updateValidationRequest();
	}

	private void deleteFile(String uploadedFileLocation) {
		try {
			Files.deleteIfExists(Paths.get(uploadedFileLocation));
		} catch (NoSuchFileException e) {
			LOGGER.warn("No such file/directory exists");
		} catch (DirectoryNotEmptyException e) {
			LOGGER.warn("Directory is not empty.");
		} catch (IOException e) {
			LOGGER.warn("Invalid permissions.");
		}

	}

}
