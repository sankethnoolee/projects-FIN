package com.fintellix.validationrestservice.definition.impl;

import com.fintellix.framework.validation.bo.ValidationExecutionBo;
import com.fintellix.framework.validation.dto.ValidationRequest;
import com.fintellix.framework.validation.dto.ValidationReturnResult;
import com.fintellix.validationrestservice.definition.ValidationConstants;
import com.fintellix.validationrestservice.definition.ValidationResultStatusManager;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class ValidationReturnResultStatusManager implements ValidationResultStatusManager {
    @Autowired
    ValidationExecutionBo validationExecutionBo;

    @Override
    public String getSupportedEntityType() {
        return ValidationConstants.TYPE_RETURN;
    }

    @Override
    public void addValidationResultStatus(ValidationRequest validationRequest) throws Exception {
        JSONObject payloadJson = (JSONObject) new JSONParser().parse(validationRequest.getPayload());
        Integer solutionId = Integer.parseInt(payloadJson.get("solutionId").toString());
        Integer periodId = Integer.parseInt(payloadJson.get("periodId").toString());
        Integer orgId = Integer.parseInt(payloadJson.get("orgId").toString());

        Integer regReportId = Integer.parseInt(payloadJson.get("regReportId").toString());
        Integer regReportVersion = Integer.parseInt(payloadJson.get("regReportVersion").toString());
        Integer versionNo = Integer.parseInt(payloadJson.get("versionNo").toString());

        Date requestStartDate = new Date();
        Date requestEndDate = new Date();

        ValidationReturnResult vrr = new ValidationReturnResult();
        vrr.setRunId(validationRequest.getRunId());
        vrr.setSolutionId(solutionId);
        vrr.setPeriodId(periodId);
        vrr.setRegReportId(regReportId);
        vrr.setRegReportVersionNumber(regReportVersion);
        vrr.setOrgId(orgId);
        vrr.setStartDate(requestStartDate);
        vrr.setEndDate(requestEndDate);
        vrr.setStatus(validationRequest.getRequestStatus());
        vrr.setVersionNumber(versionNo);

        validationExecutionBo.registerValidationReturnResult(vrr);
    }
}
