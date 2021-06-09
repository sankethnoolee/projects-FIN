package com.fintellix.framework.validation.dao;

import com.fintellix.framework.validation.dto.ValidationCleanupRecord;
import com.fintellix.framework.validation.dto.ValidationComments;
import com.fintellix.framework.validation.dto.ValidationGroupCsvLinkage;
import com.fintellix.framework.validation.dto.ValidationLineItemLink;
import com.fintellix.framework.validation.dto.ValidationMaster;
import com.fintellix.framework.validation.dto.ValidationRequest;
import com.fintellix.framework.validation.dto.ValidationReturnResult;
import com.fintellix.framework.validation.dto.ValidationRunDetails;
import com.fintellix.framework.validation.dto.ValidationWaiverDetails;
import com.northconcepts.datapipeline.core.DataReader;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public interface ValidationDao {

    List<ValidationMaster> fetchAllQualifiedValidations(Integer solutionId, Date periodIdDate, Integer orgId, String returnCode, Integer regReportId, String groupIdCSV, String regReportSectionId);

    void registerValidationRequest(ValidationRequest vr);

    ValidationRequest getStatusOfTheExecutionByRunId(Integer runId);

    void registerValidationReturnResult(ValidationReturnResult vrr);

    List<ValidationRunDetails> getValidationRunDetailsForReport(Integer solutionId, Date periodIdDate, Integer orgId, Integer regReportId, String groupIdCSV, Integer versionNo, Integer periodId);

    List<Object[]> getValidationRunDetailsForReportBySection(Integer solutionId, Date periodIdDate, Integer orgId,
                                                             Integer regReportId, String groupIdCSV, Integer versionNo, Integer regReportVersion, Integer periodId);

    List<Object[]> returnValidationWarningsCommentsStatus(Integer solutionId, Date periodIdDate, Integer orgId,
                                                          Integer regReportId, String groupIdCSV, Integer versionNo, Integer periodId);

    List<Integer> fetchAllValidationIdOfTypeOptional(Integer solutionId, Date periodIdDate, Integer orgId,
                                                     Integer regReportId, String groupIdCSV, List<Integer> validationIdList);

    List<ValidationComments> fetchCommentsIfExist(Integer periodId, Integer orgId, Integer regReportId,
                                                  List<Integer> validValidationIds, List<String> hashValueList, Integer versionNo);

    void saveValidationComments(List<ValidationComments> commentsList);

    List<String> getRunIdAndValidationIdApplicable(Integer solutionId, Date periodIdDate, Integer orgId,
                                                   Integer regReportId, String groupIdCSV, Integer versionNo, Integer periodId);

    Boolean checkForCommentsAtOccurrenceLevel(Integer periodId, Integer orgId, Integer regReportId,
                                              List<String> hexVals, Integer versionNo);

    Integer getMaxRunIdForValidation(Integer solutionId, Date periodIdDate, Integer orgId, Integer regReportId,
                                     String groupIdCSV, Integer versionNo, Integer periodId, Integer validationId);

    Map<String, String> getCommentsAtOccurrenceLevel(Integer periodId, Integer orgId, Integer regReportId,
                                                     Integer versionNo, Integer validationId);

    List<Object[]> getValidationDetailsAtFormLevel(Integer solutionId, Date periodIdDate, Integer orgId,
                                                   Integer regReportId, String groupIdCSV, Integer versionNo, Integer periodId, Integer regReportVersion);

    Map<String, String> fetchCommentsIfExistAtValidation(Integer periodId, Integer orgId, Integer regReportId,
                                                         List<Integer> validValidationIds, List<String> hashValueList, Integer versionNo);

    Object[] getValidationDetails(Integer solutionId, Date periodIdDate, Integer orgId, Integer regReportId,
                                  String groupIdCSV, Integer versionNo, Integer periodId, Integer validationId);

    ValidationRunDetails getValidationRunDetailsByRunId(Integer runId, Integer validationId);

    ValidationReturnResult fetchStatusForTheRun(Integer periodId, Integer orgId, Integer regReportId,
                                                Integer versionNo, Integer solutionId);

    Map<String, String> getLiDetailsBySubExpression(String sqlQuery, Integer solutionId);

    ValidationRequest getValidationRequestByRunId(Integer runId);

    void markCurrentExecutionOfValidationasFailed(ValidationReturnResult vrr, ValidationRequest vr);

    List<Object[]> getValidationDetailsForAllFormsForDownload(Integer solutionId, Date periodIdDate, Integer orgId,
                                                              Integer regReportId, String groupNameCSV, Integer versionNo,
                                                              Integer periodId, Integer regReportVersion,
                                                              String validationResultType, String formNameCSV,
                                                              Set<Integer> validationIds);

    Date getLatestCommentModificationDate(Set<Integer> validationId);

    List<ValidationLineItemLink> getValidationLineItemLinkDetails(Integer solutionId, Date periodIdDate, Integer orgId,
                                                                  String returnCode, Integer regReportId, String groupNameCSV);

    public List<ValidationMaster> getValidationById(Integer validationId, Integer solutionId);

    public ValidationMaster getValidationBySequenceAndId(Integer validationId, Integer sequenceNo, Integer solutionId);

    public List<ValidationMaster> getAllValidations(Integer solutionId);

    List<Object[]> getValidationDetailsAtFormLevelByLineItem(Integer solutionId, Date periodIdDate, Integer orgId,
                                                             Integer regReportId, String groupIdCSV, Integer versionNo, Integer periodId, Integer regReportVersion,
                                                             String lineItemBusinessName, Integer sectionId);

    Object[] getReportAndVersionName(Integer periodId, Integer solutionId, Integer orgId, Integer versionNo,
                                     Integer regReportId);

    Integer getReportAndVersionName(Integer solutionId, Integer regReportId, Integer regReportVersion,
                                    String sectionType, String lineItemBusinessName);

    String getGroupByColumnForGrid(Integer solutionId, Integer orgId, Integer regReportId, Integer versionNo,
                                   Integer periodId, Integer regReportVersion, Integer liId, Integer mapId);

    Integer getMapIdFromGroupByColumn(Integer solutionId, Integer orgId, Integer regReportId, Integer versionNo,
                                      Integer periodId, Integer liId, String groupByColumn);

    void updateValidationReturnResult(ValidationReturnResult vrr);

    DataReader getDataReader(String dataSetQuery, Connection conn) throws Throwable;

    List<ValidationWaiverDetails> fetchAllTheVAlidationWaiver(Integer solutionId, Integer orgId, int regReportId);

    List<ValidationWaiverDetails> fetchAllTheValidationWaiverHistory(Integer solutionId, Integer orgId, Integer regReportId);

    Map<Integer, String> fetchAllValidationCode(List<String> vwValidationId);

    String getReportBkeyById(Integer reportId, Integer solutionId);

    JSONObject getValidationCode(String returnBkey, Date periodIdDate, Integer sectionId);

    JSONObject getErrorValidationCode(String returnBkey, Date periodIdDate, Integer orgId, Integer versionNo,
                                      Integer sectionId, Integer regReportId, Integer periodId, Integer solutionId);

    JSONObject getOccurrenceValidationCode(String returnBkey, Date periodIdDate, Integer orgId, Integer versionNo,
                                           String validationCode);

    Integer getReportId(String reportBkey);

    Integer getValidationIdByCode(String validationCode, String returnBkey);

    List<ValidationMaster> fetchAllQualifiedValidations(Integer solutionId, Date periodDate, String entityCode,
                                                        String type, String groupNameCSV);

    List<ValidationRunDetails> getValidationRunDetails(Integer runId, String validationIdCsv);

    void updateValidationCleanupRecords(List<ValidationCleanupRecord> validationCleanupRecords);

    List<ValidationCleanupRecord> getValidationCleanupRecords(String type, Boolean isDeleted, Date createdDate,
                                                              String dateFilterOperator, String path);

    void saveValidationGroupCsvLinkage(List<ValidationGroupCsvLinkage> linkages);

    List<Object[]> getValidationRunDetailsForSpark(Integer runId, String validationIdCsv, boolean distinctRecord);
}
