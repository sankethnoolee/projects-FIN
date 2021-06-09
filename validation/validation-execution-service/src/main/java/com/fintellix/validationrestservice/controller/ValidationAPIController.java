package com.fintellix.validationrestservice.controller;

import com.fintellix.framework.validation.bo.ValidationAPIBo;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class ValidationAPIController {

    @Autowired
    ValidationAPIBo validationAPIBo;

    private static final Logger LOGGER = LoggerFactory.getLogger(ValidationAPIController.class);

    @RequestMapping(value = "/validationapi/getTotalRecordsForPagination", method = {RequestMethod.GET}, consumes = "application/json")
    public ResponseEntity<JSONObject> getTotalRecordsForPagination(HttpServletResponse response, HttpServletRequest request) {
        LOGGER.info("EXEFLOW-> ValidationAPIController -> getTotalRecordsForPagination");
        JSONObject obj = new JSONObject();
        try {
            String returnBkey = request.getParameter("return") != null ? request.getParameter("return").toString() : null;
            String period = request.getParameter("period") != null ? request.getParameter("period").toString() : null;
            String organizationCode = request.getParameter("organizationCode") != null ? request.getParameter("organizationCode").toString() : null;
            String createdByUserOrganizationCode = request.getParameter("createdByUserOrganizationCode") != null ? request.getParameter("createdByUserOrganizationCode").toString() : null;
            String returnVersionNumber = request.getParameter("returnVersionNumber") != null ? request.getParameter("returnVersionNumber").toString() : null;
            String returnStatus = request.getParameter("returnStatus") != null ? request.getParameter("returnStatus").toString() : null;
            String returnValidationProcessStatus = request.getParameter("returnValidationProcessStatus") != null ? request.getParameter("returnValidationProcessStatus").toString() : null;
            String returnValidationGroup = request.getParameter("returnValidationGroup") != null ? request.getParameter("returnValidationGroup").toString() : null;
            String solution = request.getParameter("solutionName") != null ? request.getParameter("solutionName").toString() : null;

            Integer count = validationAPIBo.getRecordCountForPagination(returnBkey, period, organizationCode, createdByUserOrganizationCode,
                    returnVersionNumber, returnStatus, returnValidationProcessStatus, returnValidationGroup, solution);

            obj.put("count", count);
            return new ResponseEntity<JSONObject>(obj, HttpStatus.OK);
        } catch (Throwable t) {
            LOGGER.error("Error", t);
            return new ResponseEntity<JSONObject>(obj, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/validationapi/getReturnValidationResultSummary", method = {RequestMethod.GET}, consumes = "application/json")
    public ResponseEntity<JSONArray> getReturnValidationResultSummary(HttpServletResponse response, HttpServletRequest request) {
        LOGGER.info("EXEFLOW-> ValidationAPIController -> getReturnValidationResultSummary");
        JSONArray array = new JSONArray();
        try {
            String returnBkey = request.getParameter("return") != null ? request.getParameter("return").toString() : null;
            String period = request.getParameter("period") != null ? request.getParameter("period").toString() : null;
            String organizationCode = request.getParameter("organizationCode") != null ? request.getParameter("organizationCode").toString() : null;
            String createdByUserOrganizationCode = request.getParameter("createdByUserOrganizationCode") != null ? request.getParameter("createdByUserOrganizationCode").toString() : null;
            String returnVersionNumber = request.getParameter("returnVersionNumber") != null ? request.getParameter("returnVersionNumber").toString() : null;
            String returnStatus = request.getParameter("returnStatus") != null ? request.getParameter("returnStatus").toString() : null;
            String returnValidationProcessStatus = request.getParameter("returnValidationProcessStatus") != null ? request.getParameter("returnValidationProcessStatus").toString() : null;
            String solution = request.getParameter("solutionName") != null ? request.getParameter("solutionName").toString() : null;
            String page = request.getParameter("pageNum") != null ? request.getParameter("pageNum").toString() : null;
            String rows = request.getParameter("pageSize") != null ? request.getParameter("pageSize").toString() : null;

            array = validationAPIBo.getReturnValidationResultSummary(returnBkey, period, organizationCode, createdByUserOrganizationCode,
                    returnVersionNumber, returnStatus, returnValidationProcessStatus, null, solution, page, rows);

            return new ResponseEntity<JSONArray>(array, HttpStatus.OK);
        } catch (Throwable t) {
            LOGGER.error("Error", t);
            return new ResponseEntity<JSONArray>(array, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/validationapi/getReturnValidationResultGroupSummary", method = {RequestMethod.GET}, consumes = "application/json")
    public ResponseEntity<JSONArray> getReturnValidationResultGroupSummary(HttpServletResponse response, HttpServletRequest request) {
        LOGGER.info("EXEFLOW-> ValidationAPIController -> getReturnValidationResultGroupSummary");
        JSONArray array = new JSONArray();
        try {
            String returnBkey = request.getParameter("return") != null ? request.getParameter("return").toString() : null;
            String period = request.getParameter("period") != null ? request.getParameter("period").toString() : null;
            String organizationCode = request.getParameter("organizationCode") != null ? request.getParameter("organizationCode").toString() : null;
            String createdByUserOrganizationCode = request.getParameter("createdByUserOrganizationCode") != null ? request.getParameter("createdByUserOrganizationCode").toString() : null;
            String returnVersionNumber = request.getParameter("returnVersionNumber") != null ? request.getParameter("returnVersionNumber").toString() : null;
            String returnStatus = request.getParameter("returnStatus") != null ? request.getParameter("returnStatus").toString() : null;
            String returnValidationProcessStatus = request.getParameter("returnValidationProcessStatus") != null ? request.getParameter("returnValidationProcessStatus").toString() : null;
            String returnValidationGroup = request.getParameter("returnValidationGroup") != null ? request.getParameter("returnValidationGroup").toString() : null;
            String solution = request.getParameter("solutionName") != null ? request.getParameter("solutionName").toString() : null;
            String page = request.getParameter("pageNum") != null ? request.getParameter("pageNum").toString() : null;
            String rows = request.getParameter("pageSize") != null ? request.getParameter("pageSize").toString() : null;

            array = validationAPIBo.getReturnValidationResultGroupSummary(returnBkey, period, organizationCode, createdByUserOrganizationCode,
                    returnVersionNumber, returnStatus, returnValidationProcessStatus, returnValidationGroup, solution, page, rows);

            return new ResponseEntity<JSONArray>(array, HttpStatus.OK);
        } catch (Throwable t) {
            LOGGER.error("Error", t);
            return new ResponseEntity<JSONArray>(array, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/validationapi/getReturnValidationResultDetails", method = {RequestMethod.GET}, consumes = "application/json")
    public ResponseEntity<JSONArray> getReturnValidationResultDetails(HttpServletResponse response, HttpServletRequest request) {
        LOGGER.info("EXEFLOW-> ValidationAPIController -> getReturnValidationResultDetails");
        JSONArray array = new JSONArray();
        try {
            String returnBkey = request.getParameter("return") != null ? request.getParameter("return").toString() : null;
            String period = request.getParameter("period") != null ? request.getParameter("period").toString() : null;
            String organizationCode = request.getParameter("organizationCode") != null ? request.getParameter("organizationCode").toString() : null;
            String createdByUserOrganizationCode = request.getParameter("createdByUserOrganizationCode") != null ? request.getParameter("createdByUserOrganizationCode").toString() : null;
            String returnVersionNumber = request.getParameter("returnVersionNumber") != null ? request.getParameter("returnVersionNumber").toString() : null;
            String returnStatus = request.getParameter("returnStatus") != null ? request.getParameter("returnStatus").toString() : null;
            String returnValidationProcessStatus = request.getParameter("returnValidationProcessStatus") != null ? request.getParameter("returnValidationProcessStatus").toString() : null;
            String returnValidationGroup = request.getParameter("returnValidationGroup") != null ? request.getParameter("returnValidationGroup").toString() : null;
            String solution = request.getParameter("solutionName") != null ? request.getParameter("solutionName").toString() : null;
            String validationStatus = request.getParameter("validationStatus") != null ? request.getParameter("validationStatus").toString() : null;
            String validationType = request.getParameter("validationType") != null ? request.getParameter("validationType").toString() : null;
            String validationCode = request.getParameter("validationCode") != null ? request.getParameter("validationCode").toString() : null;
            String returnValidationCategory = request.getParameter("returnValidationCategory") != null ? request.getParameter("returnValidationCategory").toString() : null;
            String hashKey = request.getParameter("hashKey") != null ? request.getParameter("hashKey").toString() : null;
            String page = request.getParameter("pageNum") != null ? request.getParameter("pageNum").toString() : null;
            String rows = request.getParameter("pageSize") != null ? request.getParameter("pageSize").toString() : null;

            array = validationAPIBo.getReturnValidationResultDetails(returnBkey, period, organizationCode, createdByUserOrganizationCode,
                    returnVersionNumber, returnStatus, returnValidationProcessStatus, returnValidationGroup, solution, validationStatus, validationType,
                    validationCode, returnValidationCategory, hashKey, page, rows);

            return new ResponseEntity<JSONArray>(array, HttpStatus.OK);
        } catch (Throwable t) {
            LOGGER.error("Error", t);
            return new ResponseEntity<JSONArray>(array, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
