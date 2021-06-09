package com.fintellix.validationrestservice.util;

import com.fintellix.framework.validation.bo.ValidationExecutionBo;
import com.fintellix.framework.validation.dto.ValidationCleanupRecord;
import com.fintellix.validationrestservice.definition.ValidationConstants;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Deepak Moudgil
 */
public class PathDeleteTimerTask extends TimerTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(PathDeleteTimerTask.class);
    private static final PathDeleteTimerTask timerTask = new PathDeleteTimerTask();

    // Singleton class
    private PathDeleteTimerTask() {
        scheduleTimerTask();
    }

    public static PathDeleteTimerTask getInstance() {
        return timerTask;
    }

    @Override
    public void run() {
        /* perform this operation only if it's a master instance */

        if (ApplicationProperties.getValue("app.validations.isMasterInstance").trim().equalsIgnoreCase("true")) {
            ValidationExecutionBo validationExecutionBo = BeanUtil.getBean(ValidationExecutionBo.class);
            List<ValidationCleanupRecord> cleanupRecords = validationExecutionBo
                    .getValidationCleanupRecords(ValidationConstants.VALIDATION_RUN_RESULT_RECORD_TYPE, false,
                            new Date(System.currentTimeMillis() - (30 * 60 * 1000)), "<=", null);

            if (cleanupRecords != null && !cleanupRecords.isEmpty()) {
                List<ValidationCleanupRecord> successfullyDeletedRecords = new ArrayList<>();

                for (ValidationCleanupRecord record : cleanupRecords) {
                    try {
                        FileUtils.forceDelete(new File(record.getPath()));
                        record.setIsDeleted(true);
                        successfullyDeletedRecords.add(record);
                    } catch (NullPointerException | FileNotFoundException | IllegalArgumentException e) {
                        record.setIsDeleted(true);
                        successfullyDeletedRecords.add(record);
                    } catch (Exception e) {
                        LOGGER.error("Failed to delete path: " + record.getPath(), e);
                    }
                }

                if (!successfullyDeletedRecords.isEmpty()) {
                    validationExecutionBo.updateValidationCleanupRecords(successfullyDeletedRecords);
                }
            }
        }
    }

    private void scheduleTimerTask() {
        Timer timer = new Timer(true);
        Integer delay = null;
        Integer period = null;

        try {
            delay = Integer.parseInt(ApplicationProperties.getValue("app.file.delete.timer.delay-in-minutes"));
        } catch (Exception ignore) {
        }

        try {
            period = Integer.parseInt(ApplicationProperties.getValue("app.file.delete.timer.period-in-minutes"));
        } catch (Exception ignore) {
        }

        if (delay == null) {
            delay = 2;
        }
        if (period == null) {
            period = 60;
        }

        timer.scheduleAtFixedRate(this, delay * 60 * 1000, period * 60 * 1000);
    }
}