package com.fintellix.validationrestservice.controller;

import com.aspose.cells.Workbook;
import com.aspose.cells.Worksheet;
import com.fintellix.framework.validation.bo.ValidationExecutionBo;
import com.fintellix.framework.validation.dto.ValidationComments;
import com.fintellix.framework.validation.dto.ValidationRequest;
import com.fintellix.framework.validation.dto.ValidationReturnResult;
import com.fintellix.framework.validation.dto.ValidationRunDetails;
import com.fintellix.redis.CacheCoordinator;
import com.fintellix.redis.RedisKeys;
import com.fintellix.validationrestservice.definition.ValidationConstants;
import com.fintellix.validationrestservice.definition.ValidationResultStatusManager;
import com.fintellix.validationrestservice.exception.BaseValidationException;
import com.fintellix.validationrestservice.util.ApplicationProperties;
import com.fintellix.validationrestservice.util.Compressor;
import com.fintellix.validationrestservice.util.ValidationStringUtils;
import net.sf.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

@RestController
public class ValidationExecutionController {

    @Autowired
    ValidationExecutionBo validationExecutionBo;

    private List<ValidationResultStatusManager> resultStatusManagers;

    private static Properties validationProperties;

    static {
        InputStream is = null;
        try {
            is = Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream("validationProperties.properties");
            validationProperties = new Properties();
            validationProperties.load(is);

        } catch (Exception e) {
            throw new RuntimeException("Couldn't read validationProperties  properties from class path", e);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static SimpleDateFormat periodFormater = new SimpleDateFormat("yyyyMMdd");
    private static final Logger LOGGER = LoggerFactory.getLogger(ValidationExecutionController.class);

    @Autowired
    public ValidationExecutionController(List<ValidationResultStatusManager> resultStatusManagers) {
        this.resultStatusManagers = resultStatusManagers;
    }

    @RequestMapping(value = "/compressfiles", method = {RequestMethod.GET})
    public ResponseEntity<String> compressFiles(HttpServletRequest request) throws Exception {
        LOGGER.info("EXEFLOW-> ValidationExecutionController ->compressFiles");
        ResponseEntity<String> reply = null;
        try {
/*			StringBuffer sb = new StringBuffer("");
			BufferedReader in = new BufferedReader(new InputStreamReader(request.getInputStream()));
			String inputLine;
			while ((inputLine = in.readLine()) != null)
				sb.append(inputLine);
			in.close();

			org.json.simple.JSONObject header = (org.json.simple.JSONObject) new JSONParser().parse(sb.toString());*/
            Compressor.compressFiles(ApplicationProperties.getValue("app.validations.outputDirectory").trim());
            reply = new ResponseEntity<String>("Files compressed", HttpStatus.OK);
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
            reply = new ResponseEntity<String>("", HttpStatus.NOT_FOUND);
        }
        return reply;
    }

	/*@RequestMapping(value = "/validationexecution/test", method = { RequestMethod.GET })
	public ResponseEntity<String> test(HttpServletRequest request) throws Exception {
		LOGGER.info("EXEFLOW-> ValidationExecutionController ->test");
		ResponseEntity<String> reply = null;
		try {
			reply = new ResponseEntity<String>("Request Added to Queue", HttpStatus.OK);
		} catch (Throwable e) {
			LOGGER.error(e.getMessage(), e);
			reply = new ResponseEntity<String>("", HttpStatus.NOT_FOUND);
		}
		return reply;
	}*/


    @RequestMapping(value = "/triggercellnavigationdatagen", method = {RequestMethod.POST, RequestMethod.GET})
    public ResponseEntity<String> triggerCellNavigationMetaDataGen(HttpServletResponse response, HttpServletRequest request) {
        LOGGER.info("EXEFLOW-> ValidationExecutionController -> triggerCellNavigationMetaDataGen");
        String json = null;
        try {
            StringBuffer sb = new StringBuffer("");
            BufferedReader in = new BufferedReader(new InputStreamReader(request.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null)
                sb.append(inputLine);
            in.close();

            org.json.simple.JSONObject header = (org.json.simple.JSONObject) new JSONParser().parse(sb.toString());
            Integer solId = Integer.parseInt(header.get("solutionId").toString());

            org.json.simple.JSONArray data = null;
            if (header.get("data") != null) {
                data = (org.json.simple.JSONArray) header.get("data");
            }
            validationExecutionBo.generateValidationMetaData(data, solId);
            json = "Navigation meta data generation in progress please wait for sometime...";
            return new ResponseEntity<String>(json, HttpStatus.OK);

        } catch (Throwable t) {
            LOGGER.error("Error", t);
            return new ResponseEntity<String>(json, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/executevalidation", method = {RequestMethod.POST, RequestMethod.GET})
    public ResponseEntity<Integer> triggerValidationExec(HttpServletResponse response, HttpServletRequest request) {
        LOGGER.info("EXEFLOW-> ValidationExecutionController -> triggerValidationExec");
        Integer json = null;
        try {
            StringBuffer sb = new StringBuffer("");
            BufferedReader in = new BufferedReader(new InputStreamReader(request.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null)
                sb.append(inputLine);
            in.close();

            org.json.simple.JSONObject header = (org.json.simple.JSONObject) new JSONParser().parse(sb.toString());

            Integer solutionId = Integer.parseInt(header.get("solutionId").toString());
            Integer periodId = Integer.parseInt(header.get("periodId").toString());
            Integer orgId = Integer.parseInt(header.get("orgId").toString());
            String entityType = header.get("entityType").toString();
            Integer userId = Integer.parseInt(header.get("userId").toString());
            String payload = header.toString();
            String orgCode = header.get("orgCode").toString();

            String requestStatus = ValidationConstants.VALIDATION_STATUS_INITIATED;

            Date requestStartDate = new Date();
            Date requestEndDate = new Date();

            ValidationRequest vr = new ValidationRequest();
            vr.setSolutionId(solutionId);
            vr.setPeriodId(periodId);
            vr.setOrgId(orgId);
            vr.setRequestStartDate(requestStartDate);
            vr.setRequestEndDate(requestEndDate);
            vr.setEntityType(entityType);
            vr.setUserId(userId);
            vr.setRequestStatus(requestStatus);
            vr.setOrgCode(orgCode);
            vr.setPayload(payload);
            validationExecutionBo.registerValidationRequest(vr);

            getValidationResultStatusManagerForEntityType(entityType).addValidationResultStatus(vr);

            validationExecutionBo.executeValidations(vr, entityType);
            json = vr.getRunId();
            return new ResponseEntity<>(json, HttpStatus.OK);

        } catch (Throwable t) {
            LOGGER.error("Error", t);
            return new ResponseEntity<>(json, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/executeValidationFromworkflow", method = {RequestMethod.POST, RequestMethod.GET})
    public ResponseEntity<Integer> executeValidationFromWorkflow(HttpServletResponse response,
                                                                 HttpServletRequest request) {
        LOGGER.info("EXEFLOW-> ValidationExecutionController -> executeValidationFromWorkflow");
        Integer json = null;
        try {
            StringBuffer sb = new StringBuffer("");
            BufferedReader in = new BufferedReader(new InputStreamReader(request.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null)
                sb.append(inputLine);
            in.close();

            org.json.simple.JSONObject header = (org.json.simple.JSONObject) new JSONParser().parse(sb.toString());
            Integer solutionId = Integer.parseInt(header.get("solutionId").toString());
            Integer periodId = Integer.parseInt(header.get("periodId").toString());
            Integer orgId = Integer.parseInt(header.get("orgId").toString());
            String entityType = header.get("entityType").toString();
            Integer userId = -1;
            Integer regReportId = Integer.parseInt(header.get("regReportId").toString());
            String regReportName = header.get("regReportName").toString();
            String orgCode = header.get("orgCode").toString();
            ;
            Integer regReportVersion = Integer.parseInt(header.get("regReportVersion").toString());
            ;
            Integer versionNo = Integer.parseInt(header.get("versionNo").toString());
            ;
            Integer instanceId = Integer.parseInt(header.get("regReportInstanceId").toString());
            ;
            String returnStatus = header.get("returnStatus") == null ? "" : header.get("returnStatus").toString();
            String sectionIdCSV = getListOfVisibleSections(instanceId, regReportId, solutionId, regReportVersion);
            header.put("sectionIdCSV", sectionIdCSV);
            String payload = header.toString();
            Date requestStartDate = new Date();
            Date requestEndDate = new Date();
            String requestStatus = ValidationConstants.VALIDATION_STATUS_INITIATED;

            ValidationRequest vr = new ValidationRequest();
            vr.setSolutionId(solutionId);
            vr.setPeriodId(periodId);
            vr.setOrgId(orgId);
            vr.setRequestStartDate(requestStartDate);
            vr.setRequestEndDate(requestEndDate);
            vr.setEntityType(entityType);
            vr.setUserId(userId);
            vr.setRequestStatus(requestStatus);
            vr.setOrgCode(orgCode);
            vr.setPayload(payload);
            vr.setEntityCode(regReportName);
            validationExecutionBo.registerValidationRequest(vr);

            ValidationReturnResult vrr = new ValidationReturnResult();
            vrr.setRunId(vr.getRunId());
            vrr.setSolutionId(solutionId);
            vrr.setPeriodId(periodId);
            vrr.setRegReportId(regReportId);
            vrr.setRegReportVersionNumber(regReportVersion);
            vrr.setOrgId(orgId);
            vrr.setStartDate(requestStartDate);
            vrr.setEndDate(requestEndDate);
            vrr.setStatus(vr.getRequestStatus());
            vrr.setVersionNumber(versionNo);

            validationExecutionBo.registerValidationReturnResult(vrr);
            validationExecutionBo.executeValidations(vr, ValidationConstants.TYPE_RETURN);
            json = vr.getRunId();
            return new ResponseEntity<Integer>(json, HttpStatus.OK);

        } catch (Throwable t) {
            LOGGER.error("Error", t);
            return new ResponseEntity<Integer>(json, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/getstatusoftheexecutionbyrunid", method = {RequestMethod.POST, RequestMethod.GET})
    public ResponseEntity<JSONObject> getStatusOfTheExecutionByRunId(HttpServletResponse response, HttpServletRequest request) {
        LOGGER.info("EXEFLOW-> ValidationExecutionController ->getStatusOfTheExecutionByRunId");
        JSONObject json = new JSONObject();
        try {
            StringBuffer sb = new StringBuffer("");
            BufferedReader in = new BufferedReader(new InputStreamReader(request.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null)
                sb.append(inputLine);
            in.close();
            org.json.simple.JSONObject header = (org.json.simple.JSONObject) new JSONParser().parse(sb.toString());
            Integer runId = Integer.parseInt(header.get("runId").toString());

            ValidationRequest vr = validationExecutionBo.getStatusOfTheExecutionByRunId(runId);
            json.put("runId", vr);
            return new ResponseEntity<JSONObject>(json, HttpStatus.OK);

        } catch (Throwable t) {
            LOGGER.error("Error", t);
            return new ResponseEntity<JSONObject>(json, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @RequestMapping(value = "/getvalidationrundetailsforreport", method = {RequestMethod.POST, RequestMethod.GET}, consumes = "application/json")
    public ResponseEntity<JSONObject> getValidationRunDetailsForReport(@RequestBody org.json.simple.JSONObject payload) {
        LOGGER.info("EXEFLOW -> ValidationExecutionController -> getValidationRunDetailsForReport");
        JSONObject json = new JSONObject();
        try {
            Integer solutionId = Integer.parseInt(payload.get("solutionId").toString());
            Integer periodId = Integer.parseInt(payload.get("periodId").toString());
            Integer orgId = Integer.parseInt(payload.get("orgId").toString());
            String groupIdCSV = (null == payload.get("validationGroupIdCSV") ? null : payload.get("validationGroupIdCSV").toString());
            Integer regReportId = Integer.parseInt(payload.get("regReportId").toString());
            Integer versionNo = Integer.parseInt(payload.get("versionNo").toString());
            Date periodIdDate = null;
            periodIdDate = periodFormater.parse(periodId.toString());
            List<ValidationRunDetails> vrdList = validationExecutionBo.
                    getValidationRunDetailsForReport(solutionId, periodIdDate, orgId, regReportId, groupIdCSV, versionNo, periodId);

            List<ValidationRunDetails> tempVrdList = vrdList.stream()
                    .filter(vrd -> vrd.getStatus().equalsIgnoreCase("FAILED") && vrd.getValidationType().equalsIgnoreCase(ValidationConstants.VALIDATION_TYPE_MANDATORY))
                    .collect(Collectors.toList());
            Integer errorCount = null == tempVrdList ? 0 : tempVrdList.size();
            tempVrdList = vrdList.stream()
                    .filter(vrd -> vrd.getStatus().equalsIgnoreCase("FAILED") && !vrd.getValidationType().equalsIgnoreCase(ValidationConstants.VALIDATION_TYPE_MANDATORY))
                    .collect(Collectors.toList());
            Integer warningCount = null == tempVrdList ? 0 : tempVrdList.size();
            ValidationReturnResult vrr = validationExecutionBo.fetchStatusForTheRun(periodId, orgId, regReportId,
                    versionNo, solutionId);
            String validationExecStatus = (vrr == null || vrr.getStatus() == null) ? ValidationConstants.NOT_VALIDATED : vrr.getStatus();
            json.put("returnValidationExecutionStatus", validationExecStatus);
            json.put("returnTotalValidations", vrdList.size());
            json.put("returnValidationErrors", errorCount);
            json.put("returnValidationWarnings", warningCount);
            json.put("returnValidationPassed", vrdList.size() - errorCount - warningCount);
            if (vrr != null && vrr.getEndDate() != null) {
                json.put("completedTime", vrr.getEndDate().getTime());
            }
            Boolean commentStatus = false;
            if (ValidationConstants.VALIDATION_STATUS_COMPLETED.equalsIgnoreCase(validationExecStatus)) {
                commentStatus = validationExecutionBo.
                        returnValidationWarningsCommentsStatus(solutionId, periodIdDate, orgId, regReportId, groupIdCSV, versionNo, periodId);
            }
            json.put("returnValidationWarningsCommentsPending", commentStatus);
            return new ResponseEntity<JSONObject>(json, HttpStatus.OK);

        } catch (Throwable t) {
            LOGGER.error("Error", t);
            return new ResponseEntity<JSONObject>(json, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/getvalidationrundetailsforreportbysection", method = {RequestMethod.POST, RequestMethod.GET})
    public ResponseEntity<Map<String, Map<Integer, JSONObject>>> getValidationRunDetailsForReportBySection(HttpServletResponse response, HttpServletRequest request, @RequestBody org.json.simple.JSONObject payload) {
        LOGGER.info("EXEFLOW-> ValidationExecutionController ->getValidationRunDetailsForReportBySection");
        Map<String, Map<Integer, JSONObject>> mapForFormData = new HashMap<String, Map<Integer, JSONObject>>();
        try {
            Integer solutionId = Integer.parseInt(payload.get("solutionId").toString());
            Integer periodId = Integer.parseInt(payload.get("periodId").toString());
            Integer orgId = Integer.parseInt(payload.get("orgId").toString());
            String groupIdCSV = (null == payload.get("validationGroupIdCSV") ? null : payload.get("validationGroupIdCSV").toString());
            Integer regReportId = Integer.parseInt(payload.get("regReportId").toString());
            Integer regReportVersion = Integer.parseInt(payload.get("regReportVersion").toString());
            ;
            Integer versionNo = Integer.parseInt(payload.get("versionNo").toString());
            ;
            Date periodIdDate = null;
            periodIdDate = periodFormater.parse(periodId.toString());
            List<Object[]> res = validationExecutionBo.
                    getValidationRunDetailsForReportBySection(solutionId, periodIdDate, orgId, regReportId,
                            groupIdCSV, versionNo, regReportVersion, periodId);
            JSONObject jsObj;
            String formName;
            Integer sectionId;
            Map<Integer, JSONObject> mapOfSectionDetails;
            for (Object[] elm : res) {
                formName = elm[5] == null ? "" : elm[5].toString();
                sectionId = Integer.parseInt(elm[3].toString());
                //check for forms
                if (null != mapForFormData.get(formName)) {
                    mapOfSectionDetails = mapForFormData.get(formName);
                    if (null != mapOfSectionDetails.get(sectionId)) {
                        jsObj = mapOfSectionDetails.get(sectionId);
                        jsObj.put("validationCount", ((Integer) jsObj.get("validationCount")) + 1);
                        if (ValidationConstants.VALIDATION_STATUS_FAILED.equalsIgnoreCase(elm[1].toString())) {
                            if (ValidationConstants.VALIDATION_TYPE_MANDATORY.equalsIgnoreCase(elm[2].toString())) {
                                jsObj.put("errorCount", ((Integer) jsObj.get("errorCount")) + 1);
                            } else {
                                jsObj.put("warningCount", ((Integer) jsObj.get("warningCount")) + 1);
                            }
                        }

                        mapOfSectionDetails.put(sectionId, jsObj);
                    } else {
                        jsObj = new JSONObject();
                        jsObj.put("sectionName", elm[4].toString());
                        jsObj.put("sectionId", sectionId);
                        jsObj.put("validationCount", 1);
                        jsObj.put("errorCount", 0);
                        jsObj.put("warningCount", 0);
                        if (ValidationConstants.VALIDATION_STATUS_FAILED.equalsIgnoreCase(elm[1].toString())) {
                            if (ValidationConstants.VALIDATION_TYPE_MANDATORY.equalsIgnoreCase(elm[2].toString())) {
                                jsObj.put("errorCount", ((Integer) jsObj.get("errorCount")) + 1);
                            } else {
                                jsObj.put("warningCount", ((Integer) jsObj.get("warningCount")) + 1);
                            }
                        }
                        mapOfSectionDetails.put(sectionId, jsObj);
                    }


                } else {
                    mapOfSectionDetails = new HashMap<Integer, JSONObject>();
                    jsObj = new JSONObject();
                    jsObj.put("sectionName", elm[4].toString());
                    jsObj.put("sectionId", sectionId);
                    jsObj.put("validationCount", 1);
                    jsObj.put("errorCount", 0);
                    jsObj.put("warningCount", 0);
                    if (ValidationConstants.VALIDATION_STATUS_FAILED.equalsIgnoreCase(elm[1].toString())) {
                        if (ValidationConstants.VALIDATION_TYPE_MANDATORY.equalsIgnoreCase(elm[2].toString())) {
                            jsObj.put("errorCount", ((Integer) jsObj.get("errorCount")) + 1);
                        } else {
                            jsObj.put("warningCount", ((Integer) jsObj.get("warningCount")) + 1);
                        }
                    }

                    mapOfSectionDetails.put(sectionId, jsObj);

                    mapForFormData.put(formName, mapOfSectionDetails);
                }
            }

            return new ResponseEntity<Map<String, Map<Integer, JSONObject>>>(mapForFormData, HttpStatus.OK);

        } catch (Throwable t) {
            LOGGER.error("Error", t);
            return new ResponseEntity<Map<String, Map<Integer, JSONObject>>>(mapForFormData, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/uploadcommentbyoccurrence", method = {RequestMethod.POST, RequestMethod.GET}, consumes = "application/json")
    public ResponseEntity<JSONObject> uploadCommentByOccurence(HttpServletResponse response, HttpServletRequest request, @RequestBody org.json.simple.JSONObject payload) {
        LOGGER.info("EXEFLOW-> ValidationExecutionController ->uploadCommentByOccerence");
        JSONObject json = new JSONObject();
        try {
            Integer solutionId = Integer.parseInt(payload.get("solutionId").toString());
            Integer periodId = Integer.parseInt(payload.get("periodId").toString());
            Integer orgId = Integer.parseInt(payload.get("orgId").toString());
            String groupIdCSV = (null == payload.get("validationGroupIdCSV") ? null : payload.get("validationGroupIdCSV").toString());
            Integer regReportId = Integer.parseInt(payload.get("regReportId").toString());
            Integer versionNo = Integer.parseInt(payload.get("versionNo").toString());
            Integer validationId = Integer.parseInt(payload.get("validationId").toString());
            String occurrenceCSV = (null == payload.get("occurrenceCSV") ? ValidationConstants.NO_OCCURRENCE_HASHKEY : payload.get("occurrenceCSV").toString());
            String comment = payload.get("comment").toString();
            Integer userId = Integer.parseInt(payload.get("userId").toString());
            Date periodIdDate = null;
            periodIdDate = periodFormater.parse(periodId.toString());

            List<ValidationComments> commentsList = new ArrayList<ValidationComments>();
            ValidationComments vc;
            for (String occurrence : occurrenceCSV.split(",")) {
                vc = new ValidationComments();
                vc.setPeriodId(periodId);
                vc.setOrgId(orgId);
                vc.setRegReportId(regReportId);
                vc.setValidationId(validationId);
                vc.setVersionNumber(versionNo);
                vc.setComment(comment);
                vc.setOccurrence(occurrence);
                vc.setLastModificationDate(Calendar.getInstance().getTime());
                vc.setLastModifiedByUserId(userId);
                vc.setIsMigrated("N");

                commentsList.add(vc);
            }
            validationExecutionBo.
                    uploadComments(solutionId, periodIdDate, orgId, regReportId, groupIdCSV, versionNo, periodId, commentsList);
            json.put("status", true);
            json.put("message", "Comment uploaded successfully.");
            return new ResponseEntity<JSONObject>(json, HttpStatus.OK);

        } catch (Throwable t) {
            LOGGER.error("Error", t);
            json.put("status", false);
            json.put("message", "Failed to upload comment.");
            return new ResponseEntity<JSONObject>(json, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/getvalidationdetailsforalloccurrence", method = {RequestMethod.POST, RequestMethod.GET}, consumes = "application/json")
    public ResponseEntity<JSONObject> getValidationDetailsForAllOccurrence(HttpServletResponse response, HttpServletRequest request, @RequestBody org.json.simple.JSONObject payload) {
        LOGGER.info("EXEFLOW-> ValidationExecutionController ->getValidationDetailsForAllOccurrence");
        JSONObject json = new JSONObject();
        try {
            Integer solutionId = Integer.parseInt(payload.get("solutionId").toString());
            Integer periodId = Integer.parseInt(payload.get("periodId").toString());
            Integer orgId = Integer.parseInt(payload.get("orgId").toString());
            String groupIdCSV = (null == payload.get("validationGroupIdCSV") ? null : payload.get("validationGroupIdCSV").toString());
            Integer regReportId = Integer.parseInt(payload.get("regReportId").toString());
            Integer versionNo = Integer.parseInt(payload.get("versionNo").toString());
            Integer validationId = Integer.parseInt(payload.get("validationId").toString());
            Boolean isCommentAtValidation = payload.get("isCommentAtValidation").toString().equalsIgnoreCase("Y") ? Boolean.TRUE : Boolean.FALSE;
            Date periodIdDate = null;
            periodIdDate = periodFormater.parse(periodId.toString());

            json = validationExecutionBo.
                    getValidationDetailsForAllOccurrence(solutionId, periodIdDate, orgId, regReportId, groupIdCSV, versionNo,
                            periodId, validationId, isCommentAtValidation);
            return new ResponseEntity<JSONObject>(json, HttpStatus.OK);

        } catch (Throwable t) {
            LOGGER.error("Error", t);
            return new ResponseEntity<JSONObject>(json, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/getvalidationdetailsatformlevel", method = {RequestMethod.POST, RequestMethod.GET}, consumes = "application/json")
    public ResponseEntity<JSONObject> getValidationDetailsAtFormLevel(HttpServletResponse response, HttpServletRequest request, @RequestBody org.json.simple.JSONObject payload) {
        LOGGER.info("EXEFLOW->  ValidationExecutionController  ->getValidationDetailsAtFormLevel");
        JSONObject json = new JSONObject();
        try {
            Integer solutionId = Integer.parseInt(payload.get("solutionId").toString());
            Integer periodId = Integer.parseInt(payload.get("periodId").toString());
            Integer orgId = Integer.parseInt(payload.get("orgId").toString());
            String groupIdCSV = (null == payload.get("validationGroupIdCSV") ? null : payload.get("validationGroupIdCSV").toString());
            Integer regReportId = Integer.parseInt(payload.get("regReportId").toString());
            Integer versionNo = Integer.parseInt(payload.get("versionNo").toString());
            Integer regReportVersion = Integer.parseInt(payload.get("regReportVersion").toString());
            Date periodIdDate = null;
            periodIdDate = periodFormater.parse(periodId.toString());


            json = validationExecutionBo.
                    getValidationDetailsAtFormLevel(solutionId, periodIdDate, orgId, regReportId, groupIdCSV, versionNo,
                            periodId, regReportVersion);
            return new ResponseEntity<JSONObject>(json, HttpStatus.OK);

        } catch (Throwable t) {
            LOGGER.error("Error", t);
            return new ResponseEntity<JSONObject>(json, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/getvalidationdetailsbyoccurrence", method = {RequestMethod.POST, RequestMethod.GET}, consumes = "application/json")
    public ResponseEntity<JSONObject> getValidationDetailsByOccurrence(HttpServletResponse response, HttpServletRequest request, @RequestBody org.json.simple.JSONObject payload) {
        LOGGER.info("EXEFLOW-> ValidationExecutionController ->getValidationDetailsByOccurrence");
        JSONObject json = new JSONObject();
        try {
            Integer solutionId = Integer.parseInt(payload.get("solutionId").toString());
            Integer periodId = Integer.parseInt(payload.get("periodId").toString());
            Integer orgId = Integer.parseInt(payload.get("orgId").toString());
            String groupIdCSV = (null == payload.get("validationGroupIdCSV") ? null : payload.get("validationGroupIdCSV").toString());
            Integer regReportId = Integer.parseInt(payload.get("regReportId").toString());
            Integer versionNo = Integer.parseInt(payload.get("versionNo").toString());
            Integer validationId = Integer.parseInt(payload.get("validationId").toString());
            Boolean isCommentAtValidation = Boolean.parseBoolean(payload.get("isCommentAtValidation").toString());
            String occurrenceCSV = (null == payload.get("occurrenceCSV") ? ValidationConstants.NO_OCCURRENCE_HASHKEY : payload.get("occurrenceCSV").toString());
            Date periodIdDate = null;
            periodIdDate = periodFormater.parse(periodId.toString());


            json = validationExecutionBo.
                    getValidationDetailsByOccurrence(solutionId, periodIdDate, orgId, regReportId, groupIdCSV, versionNo,
                            periodId, validationId, isCommentAtValidation, occurrenceCSV);
            return new ResponseEntity<JSONObject>(json, HttpStatus.OK);

        } catch (Throwable t) {
            LOGGER.error("Error", t);
            return new ResponseEntity<JSONObject>(json, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Async
    @RequestMapping(value = "/downloadvalidationresult", method = {RequestMethod.POST, RequestMethod.GET}, consumes = "application/json")
    public ResponseEntity<Map<String, String>> downloadValidationResult(HttpServletResponse response, HttpServletRequest request,
                                                                        @RequestBody org.json.simple.JSONObject payload) throws Throwable {
        LOGGER.info("EXEFLOW-> ValidationExecutionController -> downloadValidationResult");

        Map<String, String> resultMap = new HashMap<>();
        String downloadFileStatusKey = null;
        try {

            Integer solutionId = Integer.parseInt(payload.get(ValidationConstants.SOLUTION_ID).toString());
            Integer periodId = Integer.parseInt(payload.get(ValidationConstants.PERIOD_ID).toString());
            Integer orgId = Integer.parseInt(payload.get(ValidationConstants.ORG_ID).toString());
            Integer currentOrgId = Integer.parseInt(payload.get(ValidationConstants.CURRENT_ORG_ID).toString());

            Integer userId = Integer.parseInt(payload.get(ValidationConstants.USER_ID).toString());
            String groupIdCSV = (null == payload.get(ValidationConstants.VALIDATION_GROUP_ID_CSV) ? null : payload.get(ValidationConstants.VALIDATION_GROUP_ID_CSV).toString());
            Integer regReportId = Integer.parseInt(payload.get(ValidationConstants.REG_REPORT_ID).toString());
            Integer versionNo = Integer.parseInt(payload.get(ValidationConstants.VERSION_NO).toString());
            Integer regReportVersion = Integer.parseInt(payload.get(ValidationConstants.REG_REPORT_VERSION).toString());
            String formNameCSV = payload.get(ValidationConstants.FORM_NAME_CSV) != null ? payload.get(ValidationConstants.FORM_NAME_CSV).toString() : "";
            String validationResultType = (payload.get(ValidationConstants.VALIDATION_RESULT_TYPE) != null && !payload.get(ValidationConstants.VALIDATION_RESULT_TYPE).toString().trim().equals(""))
                    ? payload.get(ValidationConstants.VALIDATION_RESULT_TYPE).toString()
                    : ValidationConstants.ALL_VALIDATION_RESULTS;

            String userFullName = payload.get(ValidationConstants.USER_FULL_NAME) != null ? payload.get(ValidationConstants.USER_FULL_NAME).toString() : "";
            String userName = payload.get(ValidationConstants.USER_NAME) != null ? payload.get(ValidationConstants.USER_NAME).toString() : "";
            Date periodIdDate = periodFormater.parse(periodId.toString());

            String versionName = payload.get(ValidationConstants.VERSION_NAME) != null ? payload.get(ValidationConstants.VERSION_NAME).toString() : "";
            String returnCode = payload.get(ValidationConstants.RETURN_CODE) != null ? payload.get(ValidationConstants.RETURN_CODE).toString() : "";
            String period = payload.get(ValidationConstants.PERIOD) != null ? payload.get(ValidationConstants.PERIOD).toString() : "";
            String orgCode = payload.get(ValidationConstants.ORG_CODE) != null ? payload.get(ValidationConstants.ORG_CODE).toString() : "";
            String orgName = payload.get(ValidationConstants.ORG_NAME) != null ? payload.get(ValidationConstants.ORG_NAME).toString() : "";


            String filterOptions = payload.get(ValidationConstants.FILTER_OPTIONS) != null ? payload.get(ValidationConstants.FILTER_OPTIONS).toString() : "";
            String section = payload.get(ValidationConstants.SECTION) != null ? payload.get(ValidationConstants.SECTION).toString() : "";
            String validationCode = payload.get(ValidationConstants.VALIDATION_CODE) != null ? payload.get(ValidationConstants.VALIDATION_CODE).toString() : "";
            String validationName = payload.get(ValidationConstants.VALIDATION_NAME) != null ? payload.get(ValidationConstants.VALIDATION_NAME).toString() : "";

            String lineItem = payload.get(ValidationConstants.LINE_ITEM) != null ? payload.get(ValidationConstants.LINE_ITEM).toString() : "";
            String comments = payload.get(ValidationConstants.COMMENTS) != null ? payload.get(ValidationConstants.COMMENTS).toString() : "";
            String format = payload.get(ValidationConstants.DOWNLOAD_FORMAT) != null ? payload.get(ValidationConstants.DOWNLOAD_FORMAT).toString() : ValidationConstants.DOWNLOAD_FORMAT_XLSX;
            Map<String, Map<String, Object>> entityInfos = (Map<String, Map<String, Object>>) payload.get("entityInfos");

            String fileName = returnCode + "_" + period + "_" + versionNo + "_" + orgCode + "_ValidationResults";
            Boolean isValidationForExportToPdf = payload.get(ValidationConstants.IS_VALIDATION_FOR_EXPORT_TO_PDF) != null ? (boolean) payload.get(ValidationConstants.IS_VALIDATION_FOR_EXPORT_TO_PDF) : false;

            downloadFileStatusKey = "_" + (userId + "_" + orgId + "_" + currentOrgId + "_" + solutionId + "_" + regReportId + "_" + regReportVersion + "_" + versionNo + "_" + periodId + "_" + "ValidationResultStatus").hashCode();
            Map<String, Object> downloadValidationInfo = new HashMap<>();
            downloadValidationInfo.put(ValidationConstants.USER_ID, userId);
            downloadValidationInfo.put(ValidationConstants.ORG_ID, orgId);
            downloadValidationInfo.put(ValidationConstants.CURRENT_ORG_ID, currentOrgId);
            downloadValidationInfo.put(ValidationConstants.SOLUTION_ID, solutionId);
            downloadValidationInfo.put(ValidationConstants.REG_REPORT_ID, regReportId);
            downloadValidationInfo.put(ValidationConstants.REG_REPORT_VERSION, regReportVersion);
            downloadValidationInfo.put(ValidationConstants.VERSION_NO, versionNo);
            downloadValidationInfo.put(ValidationConstants.PERIOD_ID, periodId);
            downloadValidationInfo.put(ValidationConstants.STATUS, "STARTED");
            downloadValidationInfo.put(ValidationConstants.FILE_NAME, fileName);


            CacheCoordinator.save(RedisKeys.CONFIGURED_EXPORT_VALIDATION_RESULTS.getKey(), downloadFileStatusKey, downloadValidationInfo);
            List<Map<String, Object>> validationDetailsList = validationExecutionBo.getValidationDetailsForAllFormsForDownload(solutionId,
                    periodIdDate, orgId, regReportId, groupIdCSV, versionNo, periodId, regReportVersion,
                    validationResultType, formNameCSV, null, isValidationForExportToPdf);
            Object[] repDetailsRec = validationExecutionBo.getReportAndVersionName(periodId,
                    solutionId, orgId, versionNo, regReportId);
            String downloadOption = "";
            String status = "";

            if (validationResultType.equalsIgnoreCase(ValidationConstants.ALL_VALIDATION_RESULTS)) {
                downloadOption = validationProperties.getProperty("app.validation.indexSheet.downloadOptionAll");
                status = validationProperties.getProperty("app.validation.indexSheet.resultStatusError") + ", " +
                        validationProperties.getProperty("app.validation.indexSheet.resultStatusWarnings");
            } else if (validationResultType.equalsIgnoreCase(ValidationConstants.WARNING_VALIDATION_RESULT)) {
                downloadOption = validationProperties.getProperty("app.validation.indexSheet.downloadOptionPartial");
                status = validationProperties.getProperty("app.validation.indexSheet.resultStatusWarnings");
            } else if (validationResultType.equalsIgnoreCase(ValidationConstants.ERROR_VALIDATION_RESULT)) {
                downloadOption = validationProperties.getProperty("app.validation.indexSheet.downloadOptionPartial");
                status = validationProperties.getProperty("app.validation.indexSheet.resultStatusError");
            }

            String versionNoAndName = versionNo + " / " + versionName;
            String generatedBy = userFullName + "( " + userName + " )";
            String organization = orgCode + " - " + orgName;

            Map<String, Object> indexDetails = new LinkedHashMap<>();
            indexDetails.put(ValidationConstants.RETURN_CODE, returnCode);
            indexDetails.put(ValidationConstants.PERIOD, period);
            indexDetails.put(ValidationConstants.VERSION_NO_AND_NAME, versionNoAndName);
            indexDetails.put(ValidationConstants.GENERATED_ON, new Date());
            indexDetails.put(ValidationConstants.GENERATED_BY, generatedBy);
            indexDetails.put(ValidationConstants.RETURN_STATUS, null == repDetailsRec[3] ? "-" : repDetailsRec[3].toString());
            indexDetails.put(ValidationConstants.ORGANIZATION, organization);
            indexDetails.put(ValidationConstants.DOWNLOAD_OPTION, downloadOption);
            indexDetails.put(ValidationConstants.FILTER_OPTIONS, filterOptions);
            indexDetails.put(ValidationConstants.FORM, formNameCSV);
            indexDetails.put(ValidationConstants.SECTION, section);
            indexDetails.put(ValidationConstants.VALIDATION_CODE, validationCode);
            indexDetails.put(ValidationConstants.VALIDATION_NAME, validationName);
            indexDetails.put(ValidationConstants.STATUS, status);
            indexDetails.put(ValidationConstants.LINE_ITEM, lineItem);
            indexDetails.put(ValidationConstants.COMMENTS, comments);
            indexDetails.put(ValidationConstants.DOWNLOAD_FORMAT, format);

            String outputDirectory = ApplicationProperties.getValue("app.validations.outputDirectory").trim() + File.separator + "downloads";
            outputDirectory = ValidationStringUtils.replace(outputDirectory, "\\", "/", -1, true);

            File directory = new File(outputDirectory);
            if (!directory.exists()) {
                directory.mkdir();
            }


            String filePath = outputDirectory + File.separator + fileName + ".xlsx";
            filePath = ValidationStringUtils.replace(filePath, "\\", "/", -1, true);

            validationExecutionBo.getValidationReportWoorkbook(solutionId, validationDetailsList, indexDetails, validationResultType, entityInfos, filePath, downloadFileStatusKey);

            resultMap.put("downloadFileKey", downloadFileStatusKey);
            resultMap.put("filePath", filePath);
            return new ResponseEntity<Map<String, String>>(resultMap, HttpStatus.OK);
        } catch (Throwable t) {
            Map<String, Object> downloadValidationInfo = (Map<String, Object>) CacheCoordinator.get(RedisKeys.CONFIGURED_EXPORT_VALIDATION_RESULTS.getKey(), downloadFileStatusKey);
            downloadValidationInfo.put(ValidationConstants.STATUS, "FAILED");
            CacheCoordinator.save(RedisKeys.CONFIGURED_EXPORT_VALIDATION_RESULTS.getKey(), downloadFileStatusKey, downloadValidationInfo);
            LOGGER.error("Error", t);
            return new ResponseEntity<Map<String, String>>(resultMap, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/markcurrentexecutionofvalidationasfailed", method = {RequestMethod.POST, RequestMethod.GET}, consumes = "application/json")
    public ResponseEntity<JSONObject> markCurrentExecutionOfValidationasFailed(HttpServletResponse response, HttpServletRequest request, @RequestBody org.json.simple.JSONObject payload) {
        LOGGER.info("EXEFLOW-> ValidationExecutionController ->markCurrentExecutionOfValidationasFailed");
        JSONObject json = new JSONObject();
        try {
            Integer solutionId = Integer.parseInt(payload.get("solutionId").toString());
            Integer periodId = Integer.parseInt(payload.get("periodId").toString());
            Integer orgId = Integer.parseInt(payload.get("orgId").toString());
            Integer regReportId = Integer.parseInt(payload.get("regReportId").toString());
            Integer versionNo = Integer.parseInt(payload.get("versionNo").toString());
            ValidationReturnResult vrr = validationExecutionBo.fetchStatusForTheRun(periodId, orgId, regReportId,
                    versionNo, solutionId);
            ValidationRequest vr = validationExecutionBo.getValidationRequestByRunId(vrr.getRunId());

            vrr.setStatus(ValidationConstants.VALIDATION_STATUS_FAILED);
            vr.setRequestStatus(ValidationConstants.VALIDATION_STATUS_FAILED);

            validationExecutionBo.
                    markCurrentExecutionOfValidationasFailed(vrr, vr);
            return new ResponseEntity<JSONObject>(json, HttpStatus.OK);

        } catch (Throwable t) {
            LOGGER.error("Error", t);
            return new ResponseEntity<JSONObject>(json, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(value = "/uploadwarningcomments")
    public ResponseEntity<Map<String, Object>> handleUploadWarningComments(HttpServletRequest request,
                                                                           HttpServletResponse response,
                                                                           @RequestBody org.json.simple.JSONObject payload) {
        LOGGER.info("EXEFLOW-> ValidationExecutionController -> handleUploadWarningComments");
        Map<String, Object> model = new HashMap<>();

        try {
            Integer solutionId = Integer.parseInt(payload.get("solutionId").toString());
            Integer periodId = Integer.parseInt(payload.get("periodId").toString());
            Integer orgId = Integer.parseInt(payload.get("orgId").toString());
            //String orgCode = payload.get("orgCode").toString();
            Integer regReportId = Integer.parseInt(payload.get("regReportId").toString());
            Integer versionNo = Integer.parseInt(payload.get("versionNo").toString());
            Integer regReportVersion = Integer.parseInt(payload.get("regReportVersion").toString());
            String formNameCSV = payload.get("formNameCSV").toString();
            //String returnCode = payload.get("returnCode").toString();
            Integer userId = Integer.parseInt(payload.get("userId").toString());
            //String period = payload.get("period").toString();
            String groupIdCSV = payload.get("validationGroupIdCSV").toString();
            String filePath = payload.get("filePath").toString();
            String uploadKey = payload.get("uploadKey").toString();
            Date periodIdDate = periodFormater.parse(periodId.toString());

            InputStream is = new FileInputStream(new File(filePath));
            Workbook warningsWorkbook = new Workbook(is);
            is.close();
            Worksheet resultWorksheet = warningsWorkbook.getWorksheets()
                    .get(ApplicationProperties.getValue("app.validations.resultSheetName"));
            String[] headers = validationProperties.getProperty("app.validation.resultHeaders").trim()
                    .split("\\s*,\\s*");

            //checking header
            for (int i = 0; i < headers.length; i++) {
                if (!resultWorksheet.getCells().get(0, i).getStringValue().equals(headers[i])) {
                    model.put("success", false);
                    model.put("errorMsg", "Invalid headers.");
                    return new ResponseEntity<>(model, HttpStatus.OK);
                }
            }

            boolean hasLatestComments = validationExecutionBo.checkIfFileHasLatestComments(resultWorksheet);
            if (hasLatestComments) {
                validationExecutionBo.uploadComment(solutionId, periodIdDate, orgId, regReportId, groupIdCSV, versionNo,
                        periodId, regReportVersion, ValidationConstants.WARNING_VALIDATION_RESULT, formNameCSV,
                        resultWorksheet, headers, userId, uploadKey);
            } else {
                validationExecutionBo.updateUploadCommentStatusInfo(uploadKey, true,
                        "", true, "", validationProperties.getProperty("app.validation.promptMsg"));
            }
            model.put("success", true);
            return new ResponseEntity<>(model, HttpStatus.OK);
        } catch (BaseValidationException e) {
            LOGGER.error(e.getMessage(), e);
            model.put("success", false);
            if (e.getMessage() != null && e.getMessage().trim().length() > 0) {
                model.put("errorMsg", e.getMessage());
            } else {
                model.put("errorMsg", validationProperties.getProperty("app.validation.commnetSaveFailedErrorMsg"));
            }
            return new ResponseEntity<>(model, HttpStatus.OK);
        } catch (Throwable t) {
            LOGGER.error("Error", t);
            model.put("success", false);
            model.put("errorMsg", validationProperties.getProperty("app.validation.commnetSaveFailedErrorMsg"));
            return new ResponseEntity<>(model, HttpStatus.OK);
        }
    }

    @PostMapping(value = "/uploadwarningcommentsdiscardingnewcomments")
    public ResponseEntity<Map<String, Object>> handleUploadWarningCommentsDiscardingNewComments(HttpServletRequest request,
                                                                                                HttpServletResponse response,
                                                                                                @RequestBody org.json.simple.JSONObject payload) {
        LOGGER.info("EXEFLOW-> ValidationExecutionController -> handleUploadWarningCommentsDiscardingNewComments");
        Map<String, Object> model = new HashMap<>();

        try {
            Integer solutionId = Integer.parseInt(payload.get("solutionId").toString());
            Integer periodId = Integer.parseInt(payload.get("periodId").toString());
            Integer orgId = Integer.parseInt(payload.get("orgId").toString());
            //String orgCode = payload.get("orgCode").toString();
            Integer regReportId = Integer.parseInt(payload.get("regReportId").toString());
            Integer versionNo = Integer.parseInt(payload.get("versionNo").toString());
            Integer regReportVersion = Integer.parseInt(payload.get("regReportVersion").toString());
            String formNameCSV = payload.get("formNameCSV").toString();
            //String returnCode = payload.get("returnCode").toString();
            Integer userId = Integer.parseInt(payload.get("userId").toString());
            //String period = payload.get("period").toString();
            String groupIdCSV = payload.get("validationGroupIdCSV").toString();
            String filePath = payload.get("filePath").toString();
            String uploadKey = payload.get("uploadKey").toString();
            Date periodIdDate = periodFormater.parse(periodId.toString());

            InputStream is = new FileInputStream(new File(filePath));
            Workbook warningsWorkbook = new Workbook(is);
            is.close();
            Worksheet resultWorksheet = warningsWorkbook.getWorksheets()
                    .get(ApplicationProperties.getValue("app.validations.resultSheetName"));
            String[] headers = validationProperties.getProperty("app.validation.resultHeaders").trim()
                    .split("\\s*,\\s*");

            validationExecutionBo.uploadComment(solutionId, periodIdDate, orgId, regReportId, groupIdCSV, versionNo,
                    periodId, regReportVersion, ValidationConstants.WARNING_VALIDATION_RESULT, formNameCSV,
                    resultWorksheet, headers, userId, uploadKey);

            model.put("success", true);
            return new ResponseEntity<>(model, HttpStatus.OK);
        } catch (BaseValidationException e) {
            LOGGER.error(e.getMessage(), e);
            model.put("success", false);
            if (e.getMessage() != null && e.getMessage().trim().length() > 0) {
                model.put("errorMsg", e.getMessage());
            } else {
                model.put("errorMsg", validationProperties.getProperty("app.validation.commnetSaveFailedErrorMsg"));
            }
            return new ResponseEntity<>(model, HttpStatus.OK);
        } catch (Throwable t) {
            LOGGER.error("Error", t);
            model.put("success", false);
            model.put("errorMsg", validationProperties.getProperty("app.validation.commnetSaveFailedErrorMsg"));
            return new ResponseEntity<>(model, HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/getvalidationoccurrencedetailsbylineitemdetails", method = {RequestMethod.POST, RequestMethod.GET}, consumes = "application/json")
    public ResponseEntity<JSONObject> getValidationOccurenceDetailsByLineItemDetails(HttpServletResponse response, HttpServletRequest request, @RequestBody org.json.simple.JSONObject payload) {
        LOGGER.info("EXEFLOW->  ValidationExecutionController  ->getValidationDetailsByLineItemDetails");
        JSONObject json = new JSONObject();
        try {
            Integer solutionId = Integer.parseInt(payload.get("solutionId").toString());
            Integer periodId = Integer.parseInt(payload.get("periodId").toString());
            Integer orgId = Integer.parseInt(payload.get("orgId").toString());
            String groupIdCSV = (null == payload.get("validationGroupIdCSV") ? null : payload.get("validationGroupIdCSV").toString());
            Integer regReportId = Integer.parseInt(payload.get("regReportId").toString());
            Integer versionNo = Integer.parseInt(payload.get("versionNo").toString());
            Integer regReportVersion = Integer.parseInt(payload.get("regReportVersion").toString());
            Date periodIdDate = null;
            String lineItemBusinessName = (null == payload.get("lineItemBusinessName") ? null : payload.get("lineItemBusinessName").toString().toUpperCase());
            Integer sectionId = null;
            String sectionType = (null == payload.get("sectionType") ? null : payload.get("sectionType").toString());
            String groupByColumn = (null == payload.get("groupByColumn") ? null : payload.get("groupByColumn").toString());
            Integer validationId = Integer.parseInt(payload.get("validationId").toString());
            Boolean isCommentAtValidation = payload.get("isCommentAtValidation").toString().equalsIgnoreCase("Y") ? Boolean.TRUE : Boolean.FALSE;
            String entityColumn = (null == payload.get("entityColumn") ? null : payload.get("entityColumn").toString());
            periodIdDate = periodFormater.parse(periodId.toString());
            if (sectionType.equalsIgnoreCase(ValidationConstants.LINE_ITEM_TYPE_GRID)) {
                Integer mapId = null != payload.get("mapId") ? Integer.parseInt(payload.get("mapId").toString()) : null;
                sectionId = Integer.parseInt(payload.get("sectionId").toString());
                if (null != mapId && !mapId.equals(-1)) {
                    groupByColumn = validationExecutionBo.
                            getGroupByColumnForGrid(solutionId, orgId, regReportId, versionNo,
                                    periodId, regReportVersion, Integer.parseInt(lineItemBusinessName), mapId);
                }
                json = validationExecutionBo.
                        getValidationOccurrenceDetailsByLineItemDetails(solutionId, periodIdDate, orgId, regReportId, groupIdCSV, versionNo,
                                periodId, regReportVersion, lineItemBusinessName, sectionId, groupByColumn, mapId, validationId, isCommentAtValidation);
            } else if (sectionType.equalsIgnoreCase(ValidationConstants.LINE_ITEM_TYPE_LIST)) {
                sectionId = validationExecutionBo.
                        getSectionIdForList(solutionId, regReportId, regReportVersion, sectionType, lineItemBusinessName);
                JSONObject primaryKeyValue = null;
                if (payload.get("primaryKeyValue") != null) {
                    primaryKeyValue = new JSONObject();
                    primaryKeyValue.putAll((Map<String, String>) payload.get("primaryKeyValue"));
                }
                json = validationExecutionBo.
                        getValidationOccurrenceDetailsByLineItemDetails(solutionId, periodIdDate, orgId, regReportId, groupIdCSV, versionNo,
                                periodId, regReportVersion, entityColumn, sectionId, groupByColumn, primaryKeyValue, validationId, isCommentAtValidation);
            }

            return new ResponseEntity<JSONObject>(json, HttpStatus.OK);

        } catch (Throwable t) {
            LOGGER.error("Error", t);
            return new ResponseEntity<JSONObject>(json, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/getvalidationdetailsbylineitemdetails", method = {RequestMethod.POST, RequestMethod.GET}, consumes = "application/json")
    public ResponseEntity<JSONObject> getValidationDetailsByLineItemDetails(HttpServletResponse response, HttpServletRequest request, @RequestBody org.json.simple.JSONObject payload) {
        LOGGER.info("EXEFLOW->  ValidationExecutionController  ->getValidationDetailsByLineItemDetails");
        JSONObject json = new JSONObject();
        try {
            Integer solutionId = Integer.parseInt(payload.get("solutionId").toString());
            Integer periodId = Integer.parseInt(payload.get("periodId").toString());
            Integer orgId = Integer.parseInt(payload.get("orgId").toString());
            String groupIdCSV = (null == payload.get("validationGroupIdCSV") ? null : payload.get("validationGroupIdCSV").toString());
            Integer regReportId = Integer.parseInt(payload.get("regReportId").toString());
            Integer versionNo = Integer.parseInt(payload.get("versionNo").toString());
            Integer regReportVersion = Integer.parseInt(payload.get("regReportVersion").toString());
            String sectionType = (null == payload.get("sectionType") ? null : payload.get("sectionType").toString());
            Date periodIdDate = null;
            String lineItemBusinessName = (null == payload.get("lineItemBusinessName") ? null : payload.get("lineItemBusinessName").toString().toUpperCase());
            Integer sectionId = Integer.parseInt(payload.get("sectionId").toString());
            String entityColumn = (null == payload.get("entityColumn") ? null : payload.get("entityColumn").toString());
            periodIdDate = periodFormater.parse(periodId.toString());
            if (sectionType.equalsIgnoreCase(ValidationConstants.LINE_ITEM_TYPE_GRID)) {
                sectionId = Integer.parseInt(payload.get("sectionId").toString());
                json = validationExecutionBo.
                        getValidationDetailsByLineItemDetails(solutionId, periodIdDate, orgId, regReportId, groupIdCSV, versionNo,
                                periodId, regReportVersion, lineItemBusinessName, sectionId);
            } else if (sectionType.equalsIgnoreCase(ValidationConstants.LINE_ITEM_TYPE_LIST)) {
                sectionId = validationExecutionBo.
                        getSectionIdForList(solutionId, regReportId, regReportVersion, sectionType, lineItemBusinessName);
                json = validationExecutionBo.
                        getValidationDetailsByLineItemDetails(solutionId, periodIdDate, orgId, regReportId, groupIdCSV, versionNo,
                                periodId, regReportVersion, entityColumn, sectionId);
            }
            return new ResponseEntity<JSONObject>(json, HttpStatus.OK);

        } catch (Throwable t) {
            LOGGER.error("Error", t);
            return new ResponseEntity<JSONObject>(json, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/resetvalidationstatusonedit", method = {RequestMethod.POST, RequestMethod.GET}, consumes = "application/json")
    public ResponseEntity<JSONObject> resetValidationStatusOnEdit(HttpServletResponse response, HttpServletRequest request, @RequestBody org.json.simple.JSONObject payload) {
        LOGGER.info("EXEFLOW-> ValidationExecutionController ->getValidationRunDetailsForReport");
        JSONObject json = new JSONObject();
        try {
            Integer solutionId = Integer.parseInt(payload.get("solutionId").toString());
            Integer periodId = Integer.parseInt(payload.get("periodId").toString());
            Integer orgId = Integer.parseInt(payload.get("orgId").toString());
            String groupIdCSV = (null == payload.get("validationGroupIdCSV") ? null : payload.get("validationGroupIdCSV").toString());
            Integer regReportId = Integer.parseInt(payload.get("regReportId").toString());
            Integer versionNo = Integer.parseInt(payload.get("versionNo").toString());
            Date periodIdDate = null;
            periodIdDate = periodFormater.parse(periodId.toString());

            validationExecutionBo.resetValidationStatusOnEdit(periodId, orgId, regReportId,
                    versionNo, solutionId);
            json.put("resetStatus", true);
            json.put("msg", "OK");

            return new ResponseEntity<JSONObject>(json, HttpStatus.OK);

        } catch (Throwable t) {
            LOGGER.error("Error", t);
            json.put("resetStatus", false);
            json.put("msg", "Something went wrong during reset of status");
            return new ResponseEntity<JSONObject>(json, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/check", method = {RequestMethod.GET})
    public ResponseEntity<String> checkStatus() throws Exception {
        LOGGER.info("EXEFLOW-> ValidationExecutionController -> checkStatus");
        ResponseEntity<String> reply;
        try {
            reply = new ResponseEntity<String>("OK", HttpStatus.OK);
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
            reply = new ResponseEntity<String>("", HttpStatus.NOT_FOUND);
        }
        return reply;
    }

    private ValidationResultStatusManager getValidationResultStatusManagerForEntityType(String entityType) {
        ValidationResultStatusManager entityValidationResultStatusManager = null;
        for (ValidationResultStatusManager validationResultStatusManager : resultStatusManagers) {
            if (validationResultStatusManager.getSupportedEntityType().equalsIgnoreCase(entityType)) {
                entityValidationResultStatusManager = validationResultStatusManager;
            }
        }

        return entityValidationResultStatusManager;
    }

    @RequestMapping(value = "/getwaivedoffvalidationdetails", method = {RequestMethod.POST, RequestMethod.GET})
    public ResponseEntity<JSONObject> getWaivedOffValidationDetails(HttpServletResponse response, HttpServletRequest request) {
        LOGGER.info("EXEFLOW-> ValidationExecutionController -> getwaivedoffvalidationdetails");
        JSONObject resultObject = null;
        try {
            StringBuffer sb = new StringBuffer("");
            BufferedReader in = new BufferedReader(new InputStreamReader(request.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null)
                sb.append(inputLine);
            in.close();

            org.json.simple.JSONObject header = (org.json.simple.JSONObject) new JSONParser().parse(sb.toString());
            //OTPAuthenticator.getInstance().assertToken(header.get("OTP").toString());

            Integer solutionId = Integer.parseInt(header.get("solutionId").toString());
            Integer orgId = null != header.get("orgId") ? Integer.parseInt(header.get("orgId").toString()) : null;
            String entityType = header.get("entityType").toString();

            String payload = header.toString();

            String requestStatus = ValidationConstants.VALIDATION_STATUS_INITIATED;

            ValidationRequest vr = new ValidationRequest();
            vr.setSolutionId(solutionId);
            vr.setOrgId(orgId);
            vr.setEntityType(entityType);
            vr.setUserId(null);
            vr.setRequestStatus(requestStatus);
            vr.setPayload(payload);

            resultObject = validationExecutionBo.getWaivedOffValidations(vr, entityType);
            return new ResponseEntity<JSONObject>(resultObject, HttpStatus.OK);

        } catch (Throwable t) {
            LOGGER.error("Error", t);
            return new ResponseEntity<JSONObject>(resultObject, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/getattributelistofvalidationcodes", method = {RequestMethod.POST, RequestMethod.GET}, consumes = "application/json")
    public ResponseEntity<JSONObject> getAttributeListOfValidationCodes(@RequestBody org.json.simple.JSONObject payload) {
        LOGGER.info("EXEFLOW-> ValidationExecutionController -> getAttributeListOfValidationCodes()");
        JSONObject validationCodes = new JSONObject();
        try {
            Date periodIdDate = null;
            String returnBkey = null != payload.get("returnBkey") ? payload.get("returnBkey").toString() : null;
            Integer periodId = null != payload.get("periodId") ? Integer.parseInt(payload.get("periodId").toString()) : null;
            Integer sectionId = null != payload.get("sectionId") ? Integer.parseInt(payload.get("sectionId").toString()) : null;
            periodIdDate = periodFormater.parse(periodId.toString());
            if (null != returnBkey && null != periodId) {
                validationCodes = validationExecutionBo.getValidationCodeAttributeList(returnBkey, periodIdDate, sectionId);
            } else {
                validationCodes.put("Warning", "returnBkey and periodId cannot be null");
            }

        } catch (Throwable t) {
            LOGGER.error("Error", t);
            return new ResponseEntity<JSONObject>(validationCodes, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<JSONObject>(validationCodes, HttpStatus.OK);
    }

    @RequestMapping(value = "/getattributelistoferrorvalidationcodes", method = {RequestMethod.POST, RequestMethod.GET}, consumes = "application/json")
    public ResponseEntity<JSONObject> getAttributeListOfErrorValidationCodes(@RequestBody org.json.simple.JSONObject payload) {
        LOGGER.info("EXEFLOW-> ValidationExecutionController -> getAttributeListOfErrorValidationCodes()");
        JSONObject errorValidationCodes = new JSONObject();
        try {
            Date periodIdDate = null;
            String returnBkey = null != payload.get("returnBkey") ? payload.get("returnBkey").toString() : null;
            Integer periodId = null != payload.get("periodId") ? Integer.parseInt(payload.get("periodId").toString()) : null;
            Integer sectionId = null != payload.get("sectionId") ? Integer.parseInt(payload.get("sectionId").toString()) : null;
            Integer orgId = null != payload.get("orgId") ? Integer.parseInt(payload.get("orgId").toString()) : null;
            Integer versionNo = null != payload.get("versionNo") ? Integer.parseInt(payload.get("versionNo").toString()) : null;
            Integer solutionId = null != payload.get("solutionId") ? Integer.parseInt(payload.get("solutionId").toString()) : null;
            periodIdDate = periodFormater.parse(periodId.toString());
            if (null != returnBkey && null != periodId && null != orgId && null != versionNo) {
                errorValidationCodes = validationExecutionBo.getErrorValidationCodeAttributeList(returnBkey, periodIdDate, orgId, versionNo, sectionId, periodId, solutionId);
            } else {
                errorValidationCodes.put("Warning", "returnBkey,periodIdDate,orgId and versionNo cannot be null");
            }
        } catch (Throwable t) {
            LOGGER.error("Error", t);
            return new ResponseEntity<JSONObject>(errorValidationCodes, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<JSONObject>(errorValidationCodes, HttpStatus.OK);
    }

    @RequestMapping(value = "/getoccurrences", method = {RequestMethod.POST, RequestMethod.GET}, consumes = "application/json")
    public ResponseEntity<JSONObject> getOccurrences(@RequestBody org.json.simple.JSONObject payload) {
        LOGGER.info("EXEFLOW-> ValidationExecutionController -> getOccurrences()");
        JSONObject occurrenceObject = null;
        try {
            Date periodIdDate = null;
            String returnBkey = null != payload.get("returnBkey") ? payload.get("returnBkey").toString() : null;
            Integer periodId = null != payload.get("periodId") ? Integer.parseInt(payload.get("periodId").toString()) : null;
            Integer orgId = null != payload.get("orgId") ? Integer.parseInt(payload.get("orgId").toString()) : null;
            String validationCode = null != payload.get("validationCode") ? payload.get("validationCode").toString() : null;
            Integer versionNo = null != payload.get("versionNo") ? Integer.parseInt(payload.get("versionNo").toString()) : null;
            Integer solutionId = null != payload.get("solutionId") ? Integer.parseInt(payload.get("solutionId").toString()) : null;
            periodIdDate = periodFormater.parse(periodId.toString());

            occurrenceObject = validationExecutionBo.getValidationCodeOccurrences(returnBkey, periodIdDate, orgId, versionNo, validationCode, solutionId, periodId);
        } catch (Throwable t) {
            LOGGER.error("Error", t);
            return new ResponseEntity<JSONObject>(occurrenceObject, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<JSONObject>(occurrenceObject, HttpStatus.OK);
    }

    @PostMapping("/executevalidationforrefentity")
    public ResponseEntity<Integer> executeValidationForRefEntity(HttpServletRequest request) {
        LOGGER.info("EXEFLOW-> ValidationExecutionController -> executeValidationForRefEntity");
        try {
            StringBuilder sb = new StringBuilder();
            BufferedReader in = new BufferedReader(new InputStreamReader(request.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null)
                sb.append(inputLine);
            in.close();

            org.json.simple.JSONObject body = (org.json.simple.JSONObject) new JSONParser().parse(sb.toString());
            Integer solutionId = Integer.parseInt(body.get("solutionId").toString());
            Integer periodId = Integer.parseInt(body.get("periodId").toString());
            Integer orgId = Integer.parseInt(body.get("orgId").toString());
            String orgCode = body.get("orgCode").toString();
            String entityType = body.get("entityType").toString().toUpperCase();
            String entityCode = body.get("entityCode").toString();
            Integer userId = Integer.parseInt(body.get("userId").toString());
            Date currentDate = new Date();

            ValidationRequest vr = new ValidationRequest();
            vr.setSolutionId(solutionId);
            vr.setPeriodId(periodId);
            vr.setOrgId(orgId);
            vr.setRequestStartDate(currentDate);
            vr.setRequestEndDate(currentDate);
            vr.setEntityType(entityType);
            vr.setUserId(userId);
            vr.setRequestStatus(ValidationConstants.VALIDATION_STATUS_INITIATED);
            vr.setOrgCode(orgCode);
            vr.setPayload(body.toString());
            vr.setEntityCode(entityCode);
            validationExecutionBo.registerValidationRequest(vr);

            validationExecutionBo.executeValidations(vr, entityType);

            return new ResponseEntity<>(vr.getRunId(), HttpStatus.OK);
        } catch (Throwable t) {
            LOGGER.error("Error", t);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private RestTemplate getRestTemplate() {
        return new RestTemplate();
    }

    private String getListOfVisibleSections(Integer instanceId, Integer regReportId, Integer solutionId, Integer regReportVersion) {
        // todo figure out a way to fetch this info to trigger validations for return | Deepak
		/*try{
			String sectionIdCSV = "";
			org.json.simple.JSONObject visibleSectionDetails = reportGenerationClient.getVisibleSections(instanceId, regReportId, solutionId, regReportVersion, instanceId.toString()).getBody();

			if (null==visibleSectionDetails) {
				return null;
			}

			List<String> sectionIds = new ArrayList<String>();
			for(Iterator iterator = visibleSectionDetails.keySet().iterator(); iterator.hasNext();) {
				sectionIds.add((iterator.next()).toString());
			}
			sectionIdCSV = String.join(",",sectionIds);
			return sectionIdCSV.equalsIgnoreCase("")?null:sectionIdCSV;
		}catch(Throwable e) {
			e.printStackTrace();
			System.err.println("EXEFLOW - ValidationExecutionController -> getApplicableSectionIdCSV() -> Section list is empty.");
			return null;
		}*/

        return null;
    }
}