package com.fintellix.framework.validation.bo;

import com.aspose.cells.Worksheet;
import com.fintellix.framework.validation.dto.ValidationCleanupRecord;
import com.fintellix.framework.validation.dto.ValidationComments;
import com.fintellix.framework.validation.dto.ValidationGroupCsvLinkage;
import com.fintellix.framework.validation.dto.ValidationMaster;
import com.fintellix.framework.validation.dto.ValidationRequest;
import com.fintellix.framework.validation.dto.ValidationReturnResult;
import com.fintellix.framework.validation.dto.ValidationRunDetails;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public interface ValidationExecutionBo {

    List<ValidationMaster> fetchAllQualifiedValidations(Integer solutionId, Integer periodId, Integer orgId, String returnCode, Integer regReportId, String groupIdCSV, String regReportSectionId);

    void registerValidationRequest(ValidationRequest vr);

    void executeValidations(ValidationRequest vr, String Type);

    ValidationRequest getStatusOfTheExecutionByRunId(Integer runId);

    void registerValidationReturnResult(ValidationReturnResult vrr);

    List<ValidationRunDetails> getValidationRunDetailsForReport(Integer solutionId, Date periodIdDate, Integer orgId,
                                                                Integer regReportId, String groupIdCSV, Integer versionNo, Integer periodId);

    List<Object[]> getValidationRunDetailsForReportBySection(Integer solutionId, Date periodIdDate, Integer orgId,
                                                             Integer regReportId, String groupIdCSV, Integer versionNo, Integer regReportVersion, Integer periodId);

    Boolean returnValidationWarningsCommentsStatus(Integer solutionId, Date periodIdDate, Integer orgId,
                                                   Integer regReportId, String groupIdCSV, Integer versionNo, Integer periodId);

    void uploadComments(Integer solutionId, Date periodIdDate, Integer orgId, Integer regReportId, String groupIdCSV,
                        Integer versionNo, Integer periodId, List<ValidationComments> commentsList);

    JSONObject getValidationDetailsForAllOccurrence(Integer solutionId, Date periodIdDate, Integer orgId,
                                                    Integer regReportId, String groupIdCSV, Integer versionNo, Integer periodId, Integer validationId, Boolean isCommentAtValidation);

    JSONObject getValidationDetailsAtFormLevel(Integer solutionId, Date periodIdDate, Integer orgId,
                                               Integer regReportId, String groupIdCSV, Integer versionNo, Integer periodId, Integer regReportVersion);

    JSONObject getValidationDetailsByOccurrence(Integer solutionId, Date periodIdDate, Integer orgId,
                                                Integer regReportId, String groupIdCSV, Integer versionNo, Integer periodId, Integer validationId,
                                                Boolean isCommentAtValidation, String occurrenceCSV);

    ValidationReturnResult fetchStatusForTheRun(Integer periodId, Integer orgId, Integer regReportId,
                                                Integer versionNo, Integer solutionId);

    ValidationRequest getValidationRequestByRunId(Integer runId);

    void markCurrentExecutionOfValidationasFailed(ValidationReturnResult vrr, ValidationRequest vr);

    List<Map<String, Object>> getValidationDetailsForAllFormsForDownload(Integer solutionId, Date periodIdDate, Integer orgId,
                                                                         Integer regReportId, String groupIdCSV, Integer versionNo,
                                                                         Integer periodId, Integer regReportVersion,
                                                                         String validationResultType, String formNameCSV, Set<Integer> validationIds, Boolean isValidationForExportToPdf);

    void getValidationReportWoorkbook(Integer solutionId, List<Map<String, Object>> validationDetailsList,
                                      Map<String, Object> indexDetails, String validationResultType,
                                      Map<String, Map<String, Object>> entityInfos, String filePath, String downloadFileKey) throws Throwable;

    void uploadComment(Integer solutionId, Date periodIdDate, Integer orgId, Integer regReportId, String groupIdCSV,
                       Integer versionNo, Integer periodId, Integer regReportVersion, String warningValidationResult,
                       String formNameCSV, Worksheet resultWorksheet, String[] headers, Integer userId,
                       String uploadKey) throws Throwable;

    Date getLatestCommentModificationDate(Set<Integer> validationId);

    boolean checkIfFileHasLatestComments(Worksheet resultWorksheet);

    public void generateValidationMetaData(org.json.simple.JSONArray jsonData, Integer solId);

    JSONObject getValidationDetailsByLineItemDetails(Integer solutionId, Date periodIdDate, Integer orgId,
                                                     Integer regReportId, String groupIdCSV, Integer versionNo, Integer periodId, Integer regReportVersion,
                                                     String lineItemBusinessName, Integer sectionId);

    //-------------overloaded methods------------------
    JSONObject getValidationOccurrenceDetailsByLineItemDetails(Integer solutionId, Date periodIdDate, Integer orgId,
                                                               Integer regReportId, String groupIdCSV, Integer versionNo, Integer periodId, Integer regReportVersion,
                                                               String lineItemBusinessName, Integer sectionId, String groupByColumn, Integer mapId, Integer validationId, Boolean isCommentAtValidation);

    JSONObject getValidationOccurrenceDetailsByLineItemDetails(Integer solutionId, Date periodIdDate, Integer orgId,
                                                               Integer regReportId, String groupIdCSV, Integer versionNo, Integer periodId, Integer regReportVersion,
                                                               String lineItemBusinessName, Integer sectionId, String groupByColumn, JSONObject primaryKeyValue, Integer validationId, Boolean isCommentAtValidation);

    Object[] getReportAndVersionName(Integer periodId, Integer solutionId, Integer orgId, Integer versionNo,
                                     Integer regReportId);

    Integer getSectionIdForList(Integer solutionId, Integer regReportId, Integer regReportVersion, String sectionType,
                                String lineItemBusinessName);

    String getGroupByColumnForGrid(Integer solutionId, Integer orgId, Integer regReportId, Integer versionNo,
                                   Integer periodId, Integer regReportVersion, Integer liId, Integer mapId);

    void resetValidationStatusOnEdit(Integer periodId, Integer orgId, Integer regReportId, Integer versionNo,
                                     Integer solutionId);

    void updateUploadCommentStatusInfo(String uploadKey, Boolean uploadSuccess, String uploadMsg,
                                       Boolean hasChanged, String status, String promptMsg) throws Throwable;

    JSONObject getWaivedOffValidations(ValidationRequest vr, String entityType);

    JSONObject getValidationCodeAttributeList(String returnBkey, Date periodIdDate, Integer sectionId);

    JSONObject getErrorValidationCodeAttributeList(String returnBkey, Date periodIdDate, Integer orgId,
                                                   Integer versionNo, Integer sectionId, Integer periodId, Integer solutionId);

    JSONObject getValidationCodeOccurrences(String returnBkey, Date periodIdDate, Integer orgId, Integer versionNo,
                                            String validationCode, Integer solutionId, Integer periodId);

    List<ValidationRunDetails> getValidationRunDetails(Integer runId, String validationIdCsv);

    void processRunResultDownload(Integer runId, Integer validationId, String outputDirPath, Boolean isSparkEnabled) throws IOException;

    void triggerRunResultDownload(Integer runId, Integer validationId, String statusKey) throws Throwable;

    void updateRunResultDownloadStatusInfo(String statusKey, String status, Boolean hasError, String errorMsg, Integer runId) throws Throwable;

    void updateValidationCleanupRecords(List<ValidationCleanupRecord> validationCleanupRecords);

    List<ValidationCleanupRecord> getValidationCleanupRecords(String type, boolean isDeleted, Date createdDate,
                                                              String dateFilterOperator, String path);

    void saveValidationGroupCsvLinkage(List<ValidationGroupCsvLinkage> linkages);

    List<Object[]> getValidationRunDetailsForSpark(Integer runId, String validationIdCsv, boolean distinctRecord);
}