package com.fintellix.framework.validation.dao;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface ValidationAPIDao {

    Integer getRecordCountForPagination(String returnBkey, String period, String organizationCode, String createdByUserOrganizationCode,
                                        String returnVersionNumber, String returnStatus, String returnValidationProcessStatus, String returnValidationGroup, String solution);

    List<Object[]> getReturnValidationResultSummary(String returnBkey, String period, String organizationCode, String createdByUserOrganizationCode,
                                                    String returnVersionNumber, String returnStatus, String returnValidationProcessStatus, String returnValidationGroup, String solution,
                                                    String page, String rows);

    List<Object[]> getReturnValidationResultDetails(String returnBkey, String period, String organizationCode, String createdByUserOrganizationCode,
                                                    String returnVersionNumber, String returnStatus, String returnValidationProcessStatus, String returnValidationGroup, String solution,
                                                    String validationStatus, String validationType, String validationCode, String returnValidationCategory, String hashKey, String page,
                                                    String rows);
}
