package com.fintellix.framework.validation.bo;

import net.sf.json.JSONArray;
import org.springframework.stereotype.Component;

@Component
public interface ValidationAPIBo {

    Integer getRecordCountForPagination(String returnBkey, String period, String organizationCode, String createdByUserOrganizationCode,
                                        String returnVersionNumber, String returnStatus, String returnValidationProcessStatus, String returnValidationGroup, String solution);

    JSONArray getReturnValidationResultSummary(String returnBkey, String period, String organizationCode, String createdByUserOrganizationCode,
                                               String returnVersionNumber, String returnStatus, String returnValidationProcessStatus, String returnValidationGroup, String solution,
                                               String page, String rows);

    JSONArray getReturnValidationResultGroupSummary(String returnBkey, String period, String organizationCode, String createdByUserOrganizationCode,
                                                    String returnVersionNumber, String returnStatus, String returnValidationProcessStatus, String returnValidationGroup, String solution,
                                                    String page, String rows);

    JSONArray getReturnValidationResultDetails(String returnBkey, String period, String organizationCode, String createdByUserOrganizationCode,
                                               String returnVersionNumber, String returnStatus, String returnValidationProcessStatus, String returnValidationGroup, String solution,
                                               String validationStatus, String validationType, String validationCode, String returnValidationCategory, String hashKey, String page,
                                               String rows) throws Throwable;
}
