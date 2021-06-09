package com.fintellix.validationrestservice.controller;

import com.fintellix.framework.validation.bo.ValidationExecutionBo;
import com.fintellix.framework.validation.dto.ValidationCleanupRecord;
import com.fintellix.framework.validation.dto.ValidationRunDetails;
import com.fintellix.redis.CacheCoordinator;
import com.fintellix.redis.RedisKeys;
import com.fintellix.validationrestservice.definition.ValidationConstants;
import com.fintellix.validationrestservice.exception.DownloadFailureException;
import com.fintellix.validationrestservice.exception.InvalidRequestException;
import com.fintellix.validationrestservice.util.ApplicationProperties;
import com.fintellix.validationrestservice.util.Compressor;
import com.fintellix.validationrestservice.util.PathDeleteTimerTask;
import net.sf.json.JSONObject;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author Deepak Moudgil
 */
@RestController
@RequestMapping("/services/validationapi")
public class ValidationRestController {

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    @Autowired
    private ValidationExecutionBo validationExecutionBo;

    @PostConstruct
    void init() {
        /* this is just to load the time task */
        PathDeleteTimerTask.getInstance();
    }

    @GetMapping("/getvalidationrunstatus")
    public ResponseEntity<JSONObject> getValidationRunStatus(@RequestParam("runId") Integer runId) {
        LOGGER.info("EXEFLOW -> ValidationRestController -> getValidationRunStatus()");
        JSONObject model = new JSONObject();
        try {
            model.put("success", true);
            model.put("msg", "success");
            model.put("runStatus", validationExecutionBo.getStatusOfTheExecutionByRunId(runId).getRequestStatus());
        } catch (Exception e) {
            e.printStackTrace();
            model.put("success", false);
            model.put("msg", "Failed to fetch run status!");
        }

        return new ResponseEntity<>(model, HttpStatus.OK);
    }

    @GetMapping("/getvalidationrunsummary")
    public ResponseEntity<JSONObject> getValidationRunSummary(@RequestParam("runId") Integer runId,
                                                              @RequestParam(value = "validationIdCsv", required = false) String validationIdCsv,
                                                              @RequestParam(value = "isSparkEnabled", required = false) Boolean isSparkEnabled) {
        LOGGER.info("EXEFLOW -> ValidationRestController -> getValidationRunSummary()");
        JSONObject model = new JSONObject();
        try {
            model.put("success", true);
            model.put("msg", "success");

            if (isSparkEnabled != null && isSparkEnabled) {
                List<Object[]> results = validationExecutionBo.getValidationRunDetailsForSpark(runId, validationIdCsv, false);
                model.put("runSummary", processResults(results));
            } else {
                model.put("runSummary", validationExecutionBo.getValidationRunDetails(runId, validationIdCsv));
            }
        } catch (Exception e) {
            e.printStackTrace();
            model.put("success", false);
            model.put("msg", "Failed to fetch run summary!");
        }

        return new ResponseEntity<>(model, HttpStatus.OK);
    }

    @PostMapping("/downloadrunresult")
    public void downloadRunResult(@RequestParam("runId") Integer runId,
                                  @RequestParam(value = "validationId", required = false) Integer validationId,
                                  @RequestParam(value = "isSparkEnabled", required = false) Boolean isSparkEnabled,
                                  HttpServletResponse response) {
        LOGGER.info("EXEFLOW -> ValidationRestController -> downloadRunResult()");
        String outputDirPath = ApplicationProperties.getValue("app.validations.outputDirectory").trim()
                + File.separator + ApplicationProperties.getValue("app.validations.resultSummaryFolder").trim()
                + File.separator + runId + "_" + UUID.randomUUID().toString();

        try {
            validationExecutionBo.processRunResultDownload(runId, validationId, outputDirPath, isSparkEnabled);

            response.setHeader("Content-Disposition", "attachment; " +
                    "filename=\"" + runId + ".zip\"");
            response.setContentType("application/    zip");

            ServletOutputStream servletOutputStream = response.getOutputStream();
            Compressor.zipFolder(outputDirPath, servletOutputStream);
        } catch (Exception e) {
            e.printStackTrace();
            throw new DownloadFailureException();
        } finally {
            populateValidationCleanupRecord(outputDirPath);
        }
    }

    @PostMapping("/triggerrunresultdownload")
    public ResponseEntity<JSONObject> triggerRunResultDownload(@RequestParam("runId") Integer runId,
                                                               @RequestParam(value = "validationId", required = false) Integer validationId) {
        LOGGER.info("EXEFLOW -> ValidationRestController -> triggerRunResultDownload()");
        JSONObject model = new JSONObject();

        try {
            String statusKey = UUID.randomUUID().toString();
            validationExecutionBo.updateRunResultDownloadStatusInfo(statusKey, "Processing", false, null, runId);
            validationExecutionBo.triggerRunResultDownload(runId, validationId, statusKey);

            model.put("success", true);
            model.put("msg", "success");
            model.put("runStatusKey", statusKey);
        } catch (Throwable e) {
            e.printStackTrace();
            model.put("success", true);
            model.put("msg", "Failed to trigger run result download!");
        }

        return new ResponseEntity<>(model, HttpStatus.OK);
    }

    @GetMapping("/getrunresultdownloadstatus")
    public ResponseEntity<JSONObject> getRunResultDownloadStatus(@RequestParam("runStatusKey") String statusKey) {
        LOGGER.info("EXEFLOW -> ValidationRestController -> getRunResultDownloadStatus()");
        JSONObject model = new JSONObject();

        try {
            Map<String, Object> statusInfo = (Map<String, Object>)
                    CacheCoordinator.get(RedisKeys.CONFIGURED_RUN_RESULT_DOWNLOAD_STATUS_KEY.getKey(), statusKey);

            model.put("success", true);
            model.put("msg", "success");
            model.put("runStatus", statusInfo.get("status"));
        } catch (Throwable e) {
            e.printStackTrace();
            model.put("success", false);
            model.put("msg", "Failed to fetch status!");
        }

        return new ResponseEntity<>(model, HttpStatus.OK);
    }

    @PostMapping("/downloadrunresultbykey")
    public void downloadRunResultByKey(@RequestParam("runStatusKey") String statusKey,
                                       HttpServletResponse response) throws Throwable {
        LOGGER.info("EXEFLOW -> ValidationRestController -> downloadRunResultByKey()");
        Map<String, Object> statusInfo = (Map<String, Object>)
                CacheCoordinator.get(RedisKeys.CONFIGURED_RUN_RESULT_DOWNLOAD_STATUS_KEY.getKey(), statusKey);

        String outputDirPath = ApplicationProperties.getValue("app.validations.outputDirectory").trim()
                + File.separator + ApplicationProperties.getValue("app.validations.resultSummaryFolder").trim()
                + File.separator + statusInfo.get("runId") + "_" + statusKey.trim() + ".zip";

        try {
            File outputFile = new File(outputDirPath);
            if (!outputFile.exists()) {
                throw new InvalidRequestException("File doesn't exist!");
            }

            response.setHeader("Content-Disposition", "attachment; " +
                    "filename=\"" + statusInfo.get("runId") + ".zip\"");
            response.setContentType("application/zip");

            ServletOutputStream servletOutputStream = response.getOutputStream();
            servletOutputStream.write(FileUtils.readFileToByteArray(new File(outputDirPath)));
            servletOutputStream.flush();
            servletOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw new DownloadFailureException();
        } finally {
            List<ValidationCleanupRecord> records = validationExecutionBo.getValidationCleanupRecords(
                    ValidationConstants.VALIDATION_RUN_RESULT_RECORD_TYPE, false, null,
                    null, outputDirPath.trim());

            if (records == null || records.isEmpty()) {
                populateValidationCleanupRecord(outputDirPath);
            }
        }
    }

    private void populateValidationCleanupRecord(String path) {
        ValidationCleanupRecord validationCleanupRecord = new ValidationCleanupRecord(
                ValidationConstants.VALIDATION_RUN_RESULT_RECORD_TYPE, path, new Date(), false);
        validationExecutionBo.updateValidationCleanupRecords(Collections.singletonList(validationCleanupRecord));
    }

    private List<ValidationRunDetails> processResults(List<Object[]> results) {
        List<ValidationRunDetails> validationRunDetails = new ArrayList<>();

        if (results != null && !results.isEmpty()) {
            results.forEach(r -> {
                final ValidationRunDetails details = new ValidationRunDetails();
                details.setRunId(Integer.parseInt(r[0].toString()));
                details.setValidationId(Integer.parseInt(r[1].toString()));
                details.setSequenceNumber(Integer.parseInt(r[2].toString()));
                details.setStatus((String) r[3]);
                details.setEvaluatedExpression((String) r[4]);
                details.setTotalOccurrence(Integer.parseInt(r[5].toString()));
                details.setTotalFailed(Integer.parseInt(r[6].toString()));
                details.setValidationType((String) r[7]);
                details.setDimensionsCSV((String) r[8]);
                details.setReplacedExpression((String) r[9]);
                details.setGroupFolderName((String) r[10]);
                details.setGroupCsvName((String) r[11]);

                validationRunDetails.add(details);
            });
        }

        return validationRunDetails;
    }
}