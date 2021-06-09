package com.fintellix.framework.validation.bo;

import com.aspose.cells.Cell;
import com.aspose.cells.Cells;
import com.aspose.cells.Protection;
import com.aspose.cells.Row;
import com.aspose.cells.RowCollection;
import com.aspose.cells.Style;
import com.aspose.cells.Workbook;
import com.aspose.cells.Worksheet;
import com.fintellix.framework.validation.customtransformer.LookupValidationTransformer;
import com.fintellix.framework.validation.dao.DaoFactory;
import com.fintellix.framework.validation.dto.ValidationCleanupRecord;
import com.fintellix.framework.validation.dto.ValidationComments;
import com.fintellix.framework.validation.dto.ValidationGroupCsvLinkage;
import com.fintellix.framework.validation.dto.ValidationMaster;
import com.fintellix.framework.validation.dto.ValidationRequest;
import com.fintellix.framework.validation.dto.ValidationReturnResult;
import com.fintellix.framework.validation.dto.ValidationRunDetails;
import com.fintellix.framework.validation.dto.ValidationWaiverDetails;
import com.fintellix.redis.CacheCoordinator;
import com.fintellix.redis.RedisKeys;
import com.fintellix.validationrestservice.core.resultwriter.ExpressionResultManager;
import com.fintellix.validationrestservice.core.runprocessor.CellNavigationHandler;
import com.fintellix.validationrestservice.core.runprocessor.RequestHandler;
import com.fintellix.validationrestservice.definition.ExpressionMetaData;
import com.fintellix.validationrestservice.definition.SubExpressionMetaData;
import com.fintellix.validationrestservice.definition.ValidationConstants;
import com.fintellix.validationrestservice.exception.BaseValidationException;
import com.fintellix.validationrestservice.exception.DownloadFailureException;
import com.fintellix.validationrestservice.exception.IllegalOperationException;
import com.fintellix.validationrestservice.exception.InvalidRequestException;
import com.fintellix.validationrestservice.util.ApplicationProperties;
import com.fintellix.validationrestservice.util.Compressor;
import com.fintellix.validationrestservice.util.ObjectCloner;
import com.fintellix.validationrestservice.util.ValidationStringUtils;
import com.fintellix.validationrestservice.util.connectionManager.CalciteConnectionManager;
import com.fintellix.validationrestservice.util.connectionManager.PersistentStoreManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.northconcepts.datapipeline.core.DataReader;
import com.northconcepts.datapipeline.core.DataWriter;
import com.northconcepts.datapipeline.core.Field;
import com.northconcepts.datapipeline.core.FieldList;
import com.northconcepts.datapipeline.core.Record;
import com.northconcepts.datapipeline.core.RecordList;
import com.northconcepts.datapipeline.csv.CSVReader;
import com.northconcepts.datapipeline.csv.CSVWriter;
import com.northconcepts.datapipeline.excel.ExcelDocument;
import com.northconcepts.datapipeline.excel.ExcelReader;
import com.northconcepts.datapipeline.filter.FieldFilter;
import com.northconcepts.datapipeline.filter.FilterExpression;
import com.northconcepts.datapipeline.filter.FilteringReader;
import com.northconcepts.datapipeline.filter.rule.ValueMatch;
import com.northconcepts.datapipeline.jdbc.JdbcReader;
import com.northconcepts.datapipeline.job.Job;
import com.northconcepts.datapipeline.memory.MemoryWriter;
import com.northconcepts.datapipeline.transform.Transformer;
import com.northconcepts.datapipeline.transform.TransformingReader;
import com.northconcepts.datapipeline.transform.lookup.DataReaderLookup;
import com.northconcepts.datapipeline.transform.lookup.Lookup;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class ValidationExecutionBoImpl implements ValidationExecutionBo {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private static Properties validationProperties;
    private static SimpleDateFormat periodFormatter = new SimpleDateFormat("yyyyMMdd");

    private static final Integer HIDDEN_COMMENT_COLUMN_INDEX = 26;
    private static final Integer HIDDEN_HASHCODE_COLUMN_INDEX = 27;
    private static final Integer HIDDEN_VALIDATION_ID_COLUMN_INDEX = 28;
    private static final Integer HIDDEN_LATEST_COMMENT_MODIF_DATE_COL_INDEX = 29;
    private static final Integer OCC_HASHCODE_COLUMN_INDEX = 30;

    private static final Integer COMMENT_COLUMN_INDEX = 9;

    //TODO find alternative for this | Deepak
    /*private static AdminCacheHelper adminCacheUtil = AdminCacheHelper.getInstance();*/
    private static final List<String> listFixedColumns = new ArrayList<String>() {
        {
            add("ORG_ENTITY_ID");
            add("PERIOD_ID");
            add("REG_REPORT_ID");
            add("SOLUTION_ID");
            add("VERSION_NUMBER");
        }
    };
    private static final List<String> listFixedColumnKeys = new ArrayList<String>() {
        {
            add("versionNumber");
            add("orgEntityId");
            add("periodId");
            add("solutionId");
            add("reportId");
        }
    };

    private static String getHashValueForExp(String aliasName) {
        return "#" + new BigInteger(64, new Random(hash(aliasName)));
    }

    private static long hash(String string) {
        long h = 1125899906842597L; // prime
        int len = string.length();

        for (int i = 0; i < len; i++) {
            h = 31 * h + string.charAt(i);
        }
        return h;
    }

    @Autowired
    private RequestHandler requestHandler;
    @Autowired
    private CellNavigationHandler lineMetaDataHandler;

    @Autowired
    private ExpressionResultManager expressionResultManager;

    static {
        try {
            InputStream is = Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream("validationProperties.properties");
            validationProperties = new Properties();
            validationProperties.load(is);

        } catch (Exception e) {
            throw new RuntimeException("Coudnt read validationProperties  properties from class path", e);
        }
    }

    private static final Integer listDataStartIndex = Integer.parseInt(ApplicationProperties.getValue("app.editable.list.section.dataRowStartIndex"));

    @Autowired
    DaoFactory validationDaoFactory;

    @Override
    public List<ValidationMaster> fetchAllQualifiedValidations(Integer solutionId, Integer periodId, Integer orgId,
                                                               String returnCode, Integer regReportId, String groupIdCSV,
                                                               String regReportSectionId) {
        Date periodIdDate = null;
        try {
            periodIdDate = periodFormatter.parse(periodId.toString());
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return validationDaoFactory.getValidationDao().fetchAllQualifiedValidations(solutionId, periodIdDate, orgId,
                returnCode.replace("\"", ""), regReportId, groupIdCSV,
                null == regReportSectionId ? null : regReportSectionId.replace("\"", ""));
    }

    @Override
    public void registerValidationRequest(ValidationRequest vr) {
        logger.info("EXEFLOW --> ValidationExecutionBoImpl --> registerValidationRequest()");
        validationDaoFactory.getValidationDao().registerValidationRequest(vr);
    }

    @Override
    public void executeValidations(ValidationRequest vr, String type) {
        logger.info("EXEFLOW --> ValidationExecutionBoImpl --> executeValidations()");

        List<ValidationMaster> vmList = new ArrayList<>();
        JsonParser parser = new JsonParser();
        JsonObject pl = parser.parse(vr.getPayload()).getAsJsonObject();

        if (type.equalsIgnoreCase(ValidationConstants.TYPE_RETURN)) {
            List<ValidationMaster> vmListFinal = new ArrayList<>();
            List<ValidationWaiverDetails> vwList;
            Set<Integer> vwValidationId = new HashSet<>();
            JSONObject waiverInfo;

            vmList = fetchAllQualifiedValidations(vr.getSolutionId(), vr.getPeriodId(),
                    vr.getOrgId(), pl.get("regReportName").toString(), Integer.parseInt(pl.get("regReportId").toString()),
                    (pl.get("validationGroupIdCSV") == null ? null : pl.get("validationGroupIdCSV").toString()), (pl.get("sectionIdCSV") == null ? null : pl.get("sectionIdCSV").toString()));

            //VALIDATION WAIVER START
            vwList = validationDaoFactory.getValidationDao().fetchAllTheVAlidationWaiver(vr.getSolutionId(), vr.getOrgId(), Integer.parseInt(pl.get("regReportId").toString()));

            for (ValidationWaiverDetails vwi : vwList) {
                waiverInfo = JSONObject.fromObject(vwi.getWaiverInfo());
                if (null != waiverInfo.get("endDate") && !waiverInfo.get("endDate").toString().equals("") && !waiverInfo.get("endDate").toString().equalsIgnoreCase("null")
                        && null != waiverInfo.get("startDate") && !waiverInfo.get("startDate").toString().toString().equals("") && !waiverInfo.get("startDate").toString().toString().equalsIgnoreCase("null")) {
                    if (getPeriodIdFromLongDateFormat(waiverInfo.get("endDate").toString()) >= vr.getPeriodId() && getPeriodIdFromLongDateFormat(waiverInfo.get("startDate").toString()) <= vr.getPeriodId()) {
                        JSONArray vids = waiverInfo.getJSONArray("validationIds");
                        vwValidationId.addAll(vids);
                    }
                } else if (null != waiverInfo.get("effectiveDate") && !waiverInfo.get("effectiveDate").toString().equals("") && !waiverInfo.get("effectiveDate").toString().equalsIgnoreCase("null")) {
                    if (getPeriodIdFromLongDateFormat(waiverInfo.get("effectiveDate").toString()) <= vr.getPeriodId()) {
                        //vwValidationId.addAll(Stream.of((waiverInfo.get("validationIds").toString()).split(",")).map(Integer::parseInt).collect(Collectors.toList()));
                        if (null != waiverInfo.get("versionNo") && !waiverInfo.get("versionNo").toString().equals("") && !waiverInfo.get("versionNo").toString().equalsIgnoreCase("null")) {
                            if ((Integer.parseInt(waiverInfo.get("versionNo").toString()) == Integer.parseInt(pl.get("versionNo").toString()))) {
                                JSONArray vids = waiverInfo.getJSONArray("validationIds");
                                vwValidationId.addAll(vids);
                            }
                        } else {
                            JSONArray vids = waiverInfo.getJSONArray("validationIds");
                            vwValidationId.addAll(vids);
                        }
                    }
                }
            }

            vmListFinal.addAll(vmList);

            for (ValidationMaster vmi : vmListFinal) {
                if (vwValidationId.contains(vmi.getValidationId() + "")) {
                    vmList.remove(vmi);
                }
            }

            logger.info("Waived off validation ids ->" + vwValidationId);

        } else if (type.equalsIgnoreCase(ValidationConstants.TYPE_REFTABLE) || type.equalsIgnoreCase(ValidationConstants.TYPE_ENTITY)) {
            vmList = fetchAllQualifiedValidations(vr.getSolutionId(), vr.getPeriodId(), vr.getEntityCode(), type,
                    pl.get("validationGroupIdCSV") == null ? null : pl.get("validationGroupIdCSV").toString());
        }

        requestHandler.addRequest(vmList, vr);
    }

    @Override
    public ValidationRequest getStatusOfTheExecutionByRunId(Integer runId) {
        logger.info("EXEFLOW --> ValidationExecutionBoImpl --> executeValidations()");
        return validationDaoFactory.getValidationDao().getStatusOfTheExecutionByRunId(runId);
    }

    @Override
    public void registerValidationReturnResult(ValidationReturnResult vrr) {
        logger.info("EXEFLOW --> ValidationExecutionBoImpl --> registerValidationReturnResult()");
        validationDaoFactory.getValidationDao().registerValidationReturnResult(vrr);

    }

    @Override
    public List<ValidationRunDetails> getValidationRunDetailsForReport(Integer solutionId, Date periodIdDate, Integer orgId,
                                                                       Integer regReportId, String groupIdCSV, Integer versionNo, Integer periodId) {
        logger.info("EXEFLOW --> ValidationExecutionBoImpl --> getValidationRunDetailsForReport()");
        return validationDaoFactory.getValidationDao().getValidationRunDetailsForReport(solutionId, periodIdDate, orgId,
                regReportId, groupIdCSV, versionNo, periodId);
    }

    @Override
    public List<Object[]> getValidationRunDetailsForReportBySection(Integer solutionId, Date periodIdDate,
                                                                    Integer orgId, Integer regReportId, String groupIdCSV, Integer versionNo, Integer regReportVersion, Integer periodId) {
        logger.info("EXEFLOW --> ValidationExecutionBoImpl --> getValidationRunDetailsForReportBySection()");
        return validationDaoFactory.getValidationDao().getValidationRunDetailsForReportBySection(solutionId, periodIdDate, orgId,
                regReportId, groupIdCSV, versionNo, regReportVersion, periodId);
    }

    @Override
    public Boolean returnValidationWarningsCommentsStatus(Integer solutionId, Date periodIdDate, Integer orgId,
                                                          Integer regReportId, String groupIdCSV, Integer versionNo, Integer periodId) {
        logger.info("EXEFLOW --> ValidationExecutionBoImpl --> getValidationRunDetailsForReportBySection()");
        List<Object[]> resForValidationLevelComment = validationDaoFactory.getValidationDao().returnValidationWarningsCommentsStatus(solutionId, periodIdDate, orgId,
                regReportId, groupIdCSV, versionNo, periodId);
        Boolean commentPending = true;
        for (Object[] comment : resForValidationLevelComment) {
            if (null == comment[1] || comment[1].toString().trim().equalsIgnoreCase("")) {
                return true;
            }
        }
        commentPending = false;
        if (!commentPending) {
            return checkForCommentsAtOccurrenceLevel(solutionId, periodIdDate, orgId,
                    regReportId, groupIdCSV, versionNo, periodId);
        }
        return true;
    }

    private Boolean checkForCommentsAtOccurrenceLevel(Integer solutionId, Date periodIdDate, Integer orgId,
                                                      Integer regReportId, String groupIdCSV, Integer versionNo, Integer periodId) {
        List<String> runValId = validationDaoFactory.getValidationDao().getRunIdAndValidationIdApplicable(solutionId, periodIdDate, orgId,
                regReportId, groupIdCSV, versionNo, periodId);
        if (runValId.size() > 0) {
            return validationDaoFactory.getValidationDao().checkForCommentsAtOccurrenceLevel(periodId, orgId, regReportId, getHexValsByRunIdAndValidationId(runValId), versionNo);
        }
        return false;
    }

    @Override
    public void uploadComments(Integer solutionId, Date periodIdDate, Integer orgId, Integer regReportId,
                               String groupIdCSV, Integer versionNo, Integer periodId, List<ValidationComments> commentsList) {
        logger.info("EXEFLOW --> ValidationExecutionBoImpl --> uploadComments()");
        List<Integer> validValidationIds = validationDaoFactory.getValidationDao().
                fetchAllValidationIdOfTypeOptional(solutionId, periodIdDate, orgId, regReportId, groupIdCSV,
                        commentsList.stream().map(ValidationComments::getValidationId).collect(Collectors.toList()));

        commentsList = commentsList.stream().filter(vc -> validValidationIds.contains(vc.getValidationId())).collect(Collectors.toList());
        List<String> hashValueList = commentsList.stream().map(ValidationComments::getOccurrence).collect(Collectors.toList());
        hashValueList.add(ValidationConstants.NO_OCCURRENCE_HASHKEY);
        List<ValidationComments> existingCommentsList = validationDaoFactory.getValidationDao().fetchCommentsIfExist(periodId, orgId, regReportId, validValidationIds, hashValueList, versionNo);
        ValidationComments existingComment;
        for (ValidationComments vc : commentsList) {
            existingComment = existingCommentsList.stream().filter(v -> v.getValidationId().equals(vc.getValidationId())
                    && v.getOccurrence().equals(vc.getOccurrence())).findFirst().orElse(null);
            if (null != existingComment) {
                vc.setCommentHistory((existingComment.getCommentHistory() == null ? (existingComment.getComment()) : (existingComment.getCommentHistory() + "," + existingComment.getComment())));
            }
        }
        validationDaoFactory.getValidationDao().saveValidationComments(commentsList);
    }

    @Override
    public JSONObject getValidationDetailsForAllOccurrence(Integer solutionId, Date periodIdDate, Integer orgId,
                                                           Integer regReportId, String groupIdCSV, Integer versionNo, Integer periodId, Integer validationId, Boolean isCommentAtValidation) {
        logger.info("EXEFLOW --> ValidationExecutionBoImpl --> getValidationDetailsForAllOccurrence()");
        Integer runId = validationDaoFactory.getValidationDao().getMaxRunIdForValidation(solutionId, periodIdDate, orgId,
                regReportId, groupIdCSV, versionNo, periodId, validationId);
        ValidationRunDetails vrd = validationDaoFactory.getValidationDao().getValidationRunDetailsByRunId(runId, validationId);
        List<String> dCSV = new ArrayList<String>();
        if (vrd.getDimensionsCSV() != null && !vrd.getDimensionsCSV().equalsIgnoreCase("")) {
            dCSV.addAll(Arrays.asList(vrd.getDimensionsCSV().split(",")));

            for (String col : listFixedColumns) {
                if (dCSV.contains(col)) {
                    dCSV.remove(col);
                }
            }
        }

        if (isCommentAtValidation) {
            return getDataForAllOccurrenceByValidationId(runId, validationId, new HashMap<String, String>(), dCSV);
        } else {
            return getDataForAllOccurrenceByValidationId(runId, validationId, validationDaoFactory.getValidationDao().getCommentsAtOccurrenceLevel(periodId, orgId, regReportId, versionNo, validationId), dCSV);
        }
    }

    @Override
    public JSONObject getValidationDetailsAtFormLevel(Integer solutionId, Date periodIdDate, Integer orgId,
                                                      Integer regReportId, String groupIdCSV, Integer versionNo, Integer periodId, Integer regReportVersion) {
        logger.info("EXEFLOW --> ValidationExecutionBoImpl --> getValidationDetailsAtFormLevel()");
        JSONObject validationList = new JSONObject();
        JSONObject validationDetails;
        JSONArray validationArr = new JSONArray();
        List<Object[]> res = validationDaoFactory.getValidationDao().getValidationDetailsAtFormLevel(solutionId, periodIdDate, orgId,
                regReportId, groupIdCSV, versionNo, periodId, regReportVersion);
        List<Integer> validationIdList = new ArrayList<>();
        for (Object[] obj : res) {
            if ("Y".equalsIgnoreCase(obj[3].toString()) && ValidationConstants.VALIDATION_TYPE_OPTIONAL.equalsIgnoreCase(obj[5].toString())) {
                validationIdList.add(Integer.parseInt(obj[0].toString()));
            }
        }
        Map<String, String> commentCheck = new HashMap<String, String>();
        if (validationIdList.size() > 0) {
            List<String> hashList = new ArrayList<String>();
            hashList.add(ValidationConstants.NO_OCCURRENCE_HASHKEY);
            commentCheck = validationDaoFactory.getValidationDao().fetchCommentsIfExistAtValidation(periodId, orgId, regReportId, validationIdList, hashList, versionNo);
        }


        /*
         * 0 - validationId
         * 1 - validationCode
         * 2 - validationName
         * 3 - is comment at validation
         * 4 - status
         * 5 - validation type
         * 6 - sectionId
         * 7 - sectionDescription
         * 8 - form Name
         * 9 - occurrence
         * 10 - failed count
         * 11 - has group by
         * 12 - sectionName
         * 11 - sectionType
         * */
        for (Object[] obj : res) {
            if (obj[4].toString().equalsIgnoreCase("FAILED")) {
                validationDetails = new JSONObject();
                validationDetails.put("validationId", null == obj[0] ? "" : Integer.parseInt(obj[0].toString()));
                validationDetails.put("validationCode", null == obj[1] ? "" : (obj[1].toString()));
                validationDetails.put("validationName", null == obj[2] ? "" : (obj[2].toString()));
                validationDetails.put("isCommentAtValidation", null == obj[3] ? "" : (obj[3].toString()));
                validationDetails.put("status", null == obj[4] ? "" : (obj[4].toString()));
                validationDetails.put("type", null == obj[5] ? "" : (obj[5].toString()));
                validationDetails.put("sectionId", null == obj[6] ? "" : Integer.parseInt(obj[6].toString()));
                validationDetails.put("sectionName", null == obj[7] ? "" : (obj[7].toString()));
                validationDetails.put("formName", null == obj[8] ? "" : (obj[8].toString()));
                validationDetails.put("occurrence", null == obj[9] ? "" : Integer.parseInt(obj[9].toString()));
                validationDetails.put("failedCount", null == obj[10] ? "" : Integer.parseInt(obj[10].toString()));
                validationDetails.put("displayStatus", (obj[4].toString().equalsIgnoreCase("FAILED") ?
                        (obj[5].toString().equalsIgnoreCase(ValidationConstants.VALIDATION_TYPE_MANDATORY) ? "Error" : "Warning") : "Passed"));
                validationDetails.put("comment", "");
                if ("Y".equalsIgnoreCase(obj[3].toString())) {
                    validationDetails.put("comment", commentCheck.get(obj[0].toString() + ValidationConstants.HASH_DELIMITER + ValidationConstants.NO_OCCURRENCE_HASHKEY) == null ? "" : commentCheck.get(obj[0].toString() + ValidationConstants.HASH_DELIMITER + "-1"));
                }
                validationDetails.put("hasDimension", (null != obj[11] && !obj[11].toString().equalsIgnoreCase("")) ? "Y" : "N");
                validationDetails.put("sectionDisplayName", null == obj[12] ? "" : (obj[12].toString()));
                validationDetails.put("sectionType", null == obj[13] ? "" : (obj[13].toString()));
                validationArr.add(validationDetails);
            }
        }
        validationList.put("validationData", validationArr);
        return validationList;

    }

    @Override
    public JSONObject getValidationDetailsByOccurrence(Integer solutionId, Date periodIdDate, Integer orgId,
                                                       Integer regReportId, String groupIdCSV, Integer versionNo, Integer periodId, Integer validationId,
                                                       Boolean isCommentAtValidation, String occurrenceCSV) {
        logger.info("EXEFLOW --> ValidationExecutionBoImpl --> getValidationDetailsByOccurrence()");
        Object[] vm = validationDaoFactory.getValidationDao().getValidationDetails(solutionId, periodIdDate, orgId, regReportId, groupIdCSV, versionNo, periodId, validationId);
        List<Integer> vids = new ArrayList<Integer>();
        vids.add(validationId);
        List<String> occ = new ArrayList<String>();
        occ.add(occurrenceCSV);
        List<ValidationComments> vc = validationDaoFactory.getValidationDao().fetchCommentsIfExist(periodId, orgId, regReportId, vids, occ, versionNo);
        JSONObject detailJson = new JSONObject();
        detailJson.put("validationId", vm[1].toString());
        detailJson.put("validationName", vm[2].toString());
        detailJson.put("validationDesc", vm[3].toString());
        detailJson.put("validationExpression", vm[4].toString());
        detailJson.put("occurrence", "-");
        detailJson.put("evaluatedExpression", "-");
        JSONArray dataForNavigation = new JSONArray();
        detailJson.put("dataForNavigation", dataForNavigation);
        //if(!occurrenceCSV.equalsIgnoreCase(ValidationConstants.NO_OCCURRENCE_HASHKEY)) {
        ValidationRunDetails vrd = validationDaoFactory.getValidationDao().getValidationRunDetailsByRunId(Integer.parseInt(vm[0].toString()), validationId);
        List<String> dCSV = new ArrayList<String>();
        if (vrd.getDimensionsCSV() != null && !vrd.getDimensionsCSV().equalsIgnoreCase("")) {
            dCSV.addAll(Arrays.asList(vrd.getDimensionsCSV().split(",")));

            for (String col : listFixedColumns) {
                if (dCSV.contains(col)) {
                    dCSV.remove(col);
                }
            }
        }
        try {
            JSONObject occObj = getDataForOccurrence(Integer.parseInt(vm[0].toString()), validationId, occurrenceCSV, dCSV, periodId, orgId, regReportId, solutionId, versionNo);
            detailJson.put("occurrence", occObj.get("occurrence"));
            detailJson.put("evaluatedExpression", occObj.get("evaluatedExpression"));
            detailJson.put("dataForNavigation", occObj.get("dataForNavigation"));
            detailJson.put("message", occObj.get("message"));
        } catch (Throwable e) {
            e.printStackTrace();
        }

        //}
        if (vc.size() > 0) {
            detailJson.put("comment", vc.get(0).getComment());
        } else {
            detailJson.put("comment", "");
        }
        return detailJson;
    }

    private JSONArray getDataForNavigation(ExpressionMetaData emd, Record cr, Integer periodId, Integer orgId, Integer regReportId, Integer solutionId, Integer versionNo) {
        JSONArray dataForNavigation = new JSONArray();
        JSONObject liObject = new JSONObject();
        JSONObject primaryObj = new JSONObject();
        Set<Integer> uniqueLi = new HashSet<Integer>();
        Map<String, String> primaryKeyMap = new HashMap<String, String>();
        if (emd.getIsNavigable() && periodId.equals(emd.getPeriodId()) && orgId.equals(emd.getOrgId()) && regReportId.equals(emd.getReportId())) {
            //processsing only if navigable items are present.
            primaryObj.put("type", emd.getReturnType());
            primaryObj.put("sheetName", emd.getFormName());
            if (emd.getReturnType().equalsIgnoreCase("LIST")) {
                emd.getBasePrimaryColumns().forEach((key, val1) -> {
                    String val = ValidationStringUtils.replace(val1, "\"", "", -1, true);
                    primaryObj.put(val, cr.getField(val).getValueAsString());
                    primaryKeyMap.put(val, cr.getField(val).getValueAsString());
                });
            } else if (emd.getReturnType().equalsIgnoreCase("GRID")) {
                emd.getBasePrimaryColumns().forEach((key, val) -> {
                    primaryObj.put(key, cr.getField(key).getValueAsString());
                });
            }

            for (String eCol : emd.getEntityCols()) {
                //forming li details for normal column functions.
                liObject = new JSONObject();
                liObject.putAll(primaryObj);
                liObject.put("lineItemBusinessName", emd.getColumnInfo().get(eCol).get("BUSSINESS_NAME"));
                liObject.put("lineItemDesc", emd.getColumnInfo().get(eCol).get("DESC"));
                if (emd.getReturnType().equalsIgnoreCase("LIST")) {
                    if (emd.getBasePrimaryColumns().containsKey(eCol)) {
                        String fName = emd.getBasePrimaryColumns().get(ValidationStringUtils.replace(eCol, "\"", "", -1, true));
                        if (cr.containsField(fName)) {
                            liObject.put("value", cr.getField(fName).getValueAsString());
                        } else {
                            liObject.put("value", cr.getField(ValidationStringUtils.replace(fName, "\"", "", -1, true)).getValueAsString());
                        }

                    } else {
                        if (cr.containsField(eCol)) {
                            liObject.put("value", cr.getField(eCol).getValueAsString());
                        } else {
                            liObject.put("value", cr.getField(ValidationStringUtils.replace(eCol, "\"", "", -1, true)).getValueAsString());
                        }

                    }
                    liObject.put("entityName", emd.getEntityName());
                    liObject.put("primaryKeyMap", primaryKeyMap);

                } else if (emd.getReturnType().equalsIgnoreCase("GRID")) {
                    if (null == liObject.get("LINE_ITEM_MAP_ID") || liObject.get("LINE_ITEM_MAP_ID").toString().equalsIgnoreCase("")) {
                        if (null != liObject.get("GROUP_BY_DIMENSION") && !liObject.get("GROUP_BY_DIMENSION").toString().equalsIgnoreCase("")) {
                            Integer mapIdEval = validationDaoFactory.getValidationDao().getMapIdFromGroupByColumn(solutionId, orgId, regReportId, versionNo, periodId, Integer.parseInt(eCol.replace("A_", "")), liObject.get("GROUP_BY_DIMENSION").toString());
                            liObject.put("LINE_ITEM_MAP_ID", mapIdEval);
                        } else {
                            liObject.put("LINE_ITEM_MAP_ID", -1);
                        }
                    }

                    liObject.put("LINE_ITEM_ID", Integer.parseInt(eCol.replace("A_", "")));
                    liObject.put("value", cr.getField(eCol).getValueAsString());
                }
                if (!uniqueLi.contains((Integer) liObject.get("LINE_ITEM_ID"))) {
                    if (null != liObject.get("LINE_ITEM_ID")) {
                        dataForNavigation.add(liObject);
                        uniqueLi.add((Integer) liObject.get("LINE_ITEM_ID"));
                    } else {
                        dataForNavigation.add(liObject);
                    }
                }


            }
        }
        for (Entry<String, SubExpressionMetaData> entry : emd.getGroupByDetailsBySubExpr().entrySet()) {
            //forming li details for agg functions.
            SubExpressionMetaData subExp = entry.getValue();
            if (periodId.equals(subExp.getPeriodId()) && orgId.equals(subExp.getOrgId()) && regReportId.equals(subExp.getReportId())) {
                primaryObj.put("type", subExp.getReturnType());
                primaryObj.put("sheetName", subExp.getFormName());

                liObject = new JSONObject();
                liObject.put("lineItemBusinessName", subExp.getLineItemCode());
                liObject.put("lineItemDesc", subExp.getLineItemDesc());
                Map<String, String> valMap = validationDaoFactory.getValidationDao().getLiDetailsBySubExpression(subExp.getFinalQuery(), solutionId);
                if (subExp.getReturnType().equalsIgnoreCase("LIST")) {
                    subExp.getBasePrimaryColumns().forEach((key, val) -> {
                        primaryObj.put(val, valMap.get(val.toUpperCase()));
                        primaryKeyMap.put(val, valMap.get(val.toUpperCase()));
                    });
                    liObject.put("value", valMap.get(subExp.getTargetCol()));
                    liObject.put("entityName", subExp.getEntityName());
                    liObject.put("primaryKeyMap", primaryKeyMap);
                } else {
                    subExp.getBasePrimaryColumns().forEach((key, val) -> {
                        primaryObj.put(key, valMap.get(key));

                    });
                    if (null == liObject.get("LINE_ITEM_MAP_ID") || liObject.get("LINE_ITEM_MAP_ID").toString().equalsIgnoreCase("")) {
                        if (null != liObject.get("GROUP_BY_DIMENSION") && !liObject.get("GROUP_BY_DIMENSION").toString().equalsIgnoreCase("")) {
                            Integer mapIdEval = validationDaoFactory.getValidationDao().getMapIdFromGroupByColumn(solutionId, orgId, regReportId, versionNo, periodId, Integer.parseInt(valMap.get("LINE_ITEM_ID")), liObject.get("GROUP_BY_DIMENSION").toString());
                            liObject.put("LINE_ITEM_MAP_ID", mapIdEval);
                        } else {
                            liObject.put("LINE_ITEM_MAP_ID", -1);
                        }
                    }

                    liObject.put("value", valMap.get(subExp.getTargetCol()));
                }
                liObject.putAll(primaryObj);
                if (!uniqueLi.contains(Integer.parseInt((String) liObject.get("LINE_ITEM_ID")))) {
                    if (null != liObject.get("LINE_ITEM_ID")) {
                        dataForNavigation.add(liObject);
                        uniqueLi.add(Integer.parseInt((String) liObject.get("LINE_ITEM_ID")));
                    } else {
                        dataForNavigation.add(liObject);
                    }
                }
            }
        }

        return dataForNavigation;
    }

    @Override
    public ValidationReturnResult fetchStatusForTheRun(Integer periodId, Integer orgId, Integer regReportId,
                                                       Integer versionNo, Integer solutionId) {
        logger.info("EXEFLOW --> ValidationExecutionBoImpl --> ValidationReturnResult()");
        return validationDaoFactory.getValidationDao().fetchStatusForTheRun(periodId, orgId, regReportId, versionNo, solutionId);
    }


    private List<String> getHexValsByRunIdAndValidationId(List<String> runIdValidationIdDetails) {
        String outputDirectory = ApplicationProperties.getValue("app.validations.outputDirectory").trim();
        String fileName;
        List<String> hexVals = new ArrayList<String>();
        RecordList recordList;

        for (String rvId : runIdValidationIdDetails) {
            try {
                fileName = "Validation_Result_" + rvId.split("###")[0] + "_" + rvId.split("###")[1];// + ".csv";
                DataReader dataReader = getJdbcReader(outputDirectory + rvId.split("###")[0], fileName);

//				DataReader dataReader = new CSVReader(new File(outputDirectory + rvId.split("###")[0] + File.separator + fileName)).setFieldNamesInFirstRow(true);
                MemoryWriter memoryWriter = new MemoryWriter();

                Job.run(dataReader, memoryWriter);
                recordList = memoryWriter.getRecordList();

                for (int i = 0; i < recordList.getRecordCount(); i++) {
                    if (recordList.get(i).getField("Validation").getValueAsString().trim().equalsIgnoreCase("false")) {
                        hexVals.add(recordList.get(i).getField("Hash Key").getValueAsString());
                    }
                }

            } catch (Throwable e) {
                logger.error("ERROR -->Failed to get data for validation <ValidationID> --" + rvId.split("###")[1] + " <runId> -- " + rvId.split("###")[0]);
                e.printStackTrace();
            }
        }
        return hexVals;
    }

    private JSONObject getDataForAllOccurrenceByValidationId(Integer runId, Integer validationId, Map<String, String> hashCommentMap, List<String> dCSV) {
        String outputDirectory = ApplicationProperties.getValue("app.validations.outputDirectory").trim();
        String fileName;
        JSONObject res = new JSONObject();
        JSONObject displayActualName = new JSONObject();
        JSONObject occ;
        JSONArray columnNames = new JSONArray();

        List<String> tempColumnNames = new ArrayList<String>();
        JSONArray columnData = new JSONArray();
        RecordList recordList;
        fileName = "Validation_Result_" + runId + "_" + validationId;//+ ".csv";

//		DataReader dataReader = getJdbcReader(outputDirectory + runId, fileName);
//				
//				
//				new CSVReader(new File(outputDirectory + runId + File.separator + fileName)).setFieldNamesInFirstRow(true);
        DataReader dataReader = getJdbcReader(outputDirectory + runId, fileName);
        MemoryWriter memoryWriter = new MemoryWriter();

        Job.run(dataReader, memoryWriter);
        recordList = memoryWriter.getRecordList();
        if (recordList.getRecordCount() > 0) {

            tempColumnNames.addAll(dCSV);
            tempColumnNames.add("validationResult");
            tempColumnNames.add("hashValue");
            tempColumnNames.add("comment");
            for (String c : tempColumnNames) {
                displayActualName.put(ValidationStringUtils.replace(c, " ", "____", -1, true), c);
                columnNames.add(ValidationStringUtils.replace(c, " ", "____", -1, true));
            }
            res.put("columnDisplayActualMap", displayActualName);
            res.put("columnNames", columnNames);
            for (int i = 0; i < recordList.getRecordCount(); i++) {
                if (!Boolean.parseBoolean(recordList.get(i).getField("Validation").getValueAsString())) {
                    occ = new JSONObject();
                    for (String dimName : dCSV) {
                        occ.put(ValidationStringUtils.replace(dimName, " ", "____", -1, true), (recordList.get(i).getField(dimName).getValueAsString() == null ? "" : recordList.get(i).getField(dimName).getValueAsString()));
                    }
                    occ.put("validationResult", Boolean.parseBoolean(recordList.get(i).getField("Validation").getValueAsString()));
                    occ.put("hashValue", recordList.get(i).getField("Hash Key").getValueAsString());
                    occ.put("comment", hashCommentMap.get(validationId + ValidationConstants.HASH_DELIMITER + recordList.get(i).getField("Hash Key").getValueAsString()) == null ? "" :
                            hashCommentMap.get(validationId + ValidationConstants.HASH_DELIMITER + recordList.get(i).getField("Hash Key").getValueAsString()));
                    columnData.add(occ);
                }

            }
            res.put("columnData", columnData);
        }

        return res;
    }

    private JdbcReader getJdbcReader(String outputDirectory, String fileName) {
        String schema = "S_" + System.currentTimeMillis() + "_S";
        Connection conn = null;
        try {
            conn = CalciteConnectionManager.getCalciteConnection(outputDirectory, schema);
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
        }

        return new JdbcReader(conn, "select * from " + schema + "." + fileName).setAutoCloseConnection(Boolean.TRUE);
    }

    private JSONObject getDataForOccurrence(Integer runId, Integer validationId, String HashVal, List<String> dCSV, Integer periodId, Integer orgId, Integer regReportId, Integer solutionId, Integer versionNo) {
        String outputDirectory = ApplicationProperties.getValue("app.validations.outputDirectory").trim();
        String fileName;
        JSONObject occ = new JSONObject();
        occ.put("occurrence", "-");
        occ.put("evaluatedExpression", "-");
        occ.put("message", "-");
        occ.put("dataForNavigation", new JSONArray());
        List<String> tempColumnNames = new ArrayList<String>();
        RecordList recordList;
        fileName = "Validation_Result_" + runId + "_" + validationId;// + ".csv";
        DataReader dataReader = getJdbcReader(outputDirectory + runId, fileName);
//		DataReader dataReader = new CSVReader(new File(outputDirectory + runId + File.separator + fileName)).setFieldNamesInFirstRow(true);
        MemoryWriter memoryWriter = new MemoryWriter();

        Job.run(dataReader, memoryWriter);
        recordList = memoryWriter.getRecordList();
        if (recordList.getRecordCount() > 0) {
            tempColumnNames.addAll(dCSV);

            if (HashVal.equalsIgnoreCase(ValidationConstants.NO_OCCURRENCE_HASHKEY)) {

                occ = new JSONObject();
                if (tempColumnNames.size() > 0) {
                    occ.put("occurrence", "");
                    for (String s : tempColumnNames) {
                        occ.put("occurrence", occ.get("occurrence") + "," + s + "-" + recordList.get(0).getField(s).getValueAsString());
                    }
                    occ.put("occurrence", occ.get("occurrence").toString().substring(1));
                } else {
                    occ.put("occurrence", "-");
                }

                occ.put("evaluatedExpression", recordList.get(0).getField("Evaluated Expression").getValueAsString());
                occ.put("message", recordList.get(0).getField("Evaluation Message").getValueAsString());
                Gson g = new Gson();
                ExpressionMetaData emd;
                try {
                    String metadata = null;
                    for (int i = 0; i < recordList.getRecordCount(); i++) {
                        metadata = recordList.get(i).getField("Expression_Meta_data").getValueAsString();
                        if (metadata != null && metadata.trim().length() > 1) {
                            break;
                        }
                    }
                    emd = expressionResultManager.getExpressionMetaDataForZeroOccurrence(runId, validationId, metadata);
                } catch (Throwable e) {
                    emd = null;
                    e.printStackTrace();
                }

//              getExpressionMetaData(recordList.get(0).getField("Expression_Meta_data").getValueAsString());

                JSONArray dataForNavigation = getDataForNavigation(emd, recordList.get(0), periodId, orgId, regReportId, solutionId, versionNo);
                occ.put("dataForNavigation", dataForNavigation);

            } else {
                for (int i = 0; i < recordList.getRecordCount(); i++) {
                    if ((recordList.get(i).getField("Hash Key").getValueAsString().equalsIgnoreCase(HashVal))) {
                        occ = new JSONObject();
                        if (tempColumnNames.size() > 0) {
                            occ.put("occurrence", "");
                            for (String s : tempColumnNames) {
                                occ.put("occurrence", occ.get("occurrence") + "," + s + "-" + recordList.get(i).getField(s).getValueAsString());
                            }
                            occ.put("occurrence", occ.get("occurrence").toString().substring(1));
                        } else {
                            occ.put("occurrence", "-");
                        }

                        occ.put("evaluatedExpression", recordList.get(i).getField("Evaluated Expression").getValueAsString());
                        occ.put("message", recordList.get(i).getField("Evaluation Message").getValueAsString());

                        Gson g = new Gson();
                        ExpressionMetaData emd;
                        try {
                            emd = expressionResultManager.getExpressionMetaData(runId, validationId, recordList.get(i).getField("Expression_Meta_data").getValueAsString());
                        } catch (Throwable e) {
                            emd = null;
                            e.printStackTrace();
                        }

                        //getExpressionMetaData(recordList.get(i).getField("Expression_Meta_data").getValueAsString());

                        JSONArray dataForNavigation = getDataForNavigation(emd, recordList.get(i), periodId, orgId, regReportId, solutionId, versionNo);
                        occ.put("dataForNavigation", dataForNavigation);
                    }
                }
            }
        }
        return occ;
    }

    @Override
    public List<Map<String, Object>> getValidationDetailsForAllFormsForDownload(Integer solutionId, Date periodIdDate,
                                                                                Integer orgId, Integer regReportId,
                                                                                String groupIdCSV, Integer versionNo,
                                                                                Integer periodId, Integer regReportVersion,
                                                                                String validationResultType, String formNameCSV,
                                                                                Set<Integer> validationIds, Boolean isValidationForExportToPdf) {
        logger.info("EXEFLOW --> ValidationExecutionBoImpl --> getValidationDetailsForAllFormsForDownload()");

        List<Map<String, Object>> validationDetailsListWithOccurrences = new ArrayList<>();

        /*
         * 0 - validationId
         * 1 - validationCode
         * 2 - validationName
         * 3 - is comment at validation
         * 4 - status
         * 5 - validation type
         * 6 - sectionId
         * 7 - sectionDesc
         * 8 - form Name
         * 9 - total occurrence
         * 10 - total failed count
         * 11 - dimension CSV
         * 12 - validation desc
         * 13 - evaluated expression
         * 14 - validation expression
         * */

        List<Object[]> res = validationDaoFactory.getValidationDao().getValidationDetailsForAllFormsForDownload(solutionId,
                periodIdDate, orgId, regReportId, groupIdCSV, versionNo, periodId, regReportVersion, validationResultType,
                formNameCSV, validationIds);

        if (res != null && !res.isEmpty()) {
            List<Integer> validationIdList = new ArrayList<>();
            for (Object[] obj : res) {
                if ("Y".equalsIgnoreCase(obj[3].toString())) {
                    validationIdList.add(Integer.parseInt(obj[0].toString()));
                }
            }

            Map<String, String> commentCheck = new HashMap<>();
            if (validationIdList.size() > 0) {
                List<String> hashList = new ArrayList<>();
                hashList.add(ValidationConstants.NO_OCCURRENCE_HASHKEY);
                commentCheck = validationDaoFactory.getValidationDao().fetchCommentsIfExistAtValidation(periodId, orgId,
                        regReportId, validationIdList, hashList, versionNo);
            }

            List<Map<String, Object>> validationDetailsList = new ArrayList<>();
            Map<String, Object> validationDetails;

            for (Object[] obj : res) {
                validationDetails = new HashMap<>();
                validationDetails.put("validationId", null == obj[0] ? "" : obj[0].toString());
                validationDetails.put("validationCode", null == obj[1] ? "" : (obj[1].toString()));
                validationDetails.put("validationName", null == obj[2] ? "" : (obj[2].toString()));
                validationDetails.put("isCommentAtValidation", null == obj[3] ? "" : (obj[3].toString()));
                validationDetails.put("status", null == obj[4] ? "" : (obj[4].toString()));
                validationDetails.put("type", null == obj[5] ? "" : (obj[5].toString()));
                validationDetails.put("sectionId", null == obj[6] ? "" : obj[6].toString());
                validationDetails.put("sectionName", null == obj[7] ? "" : (obj[7].toString()));
                validationDetails.put("formName", null == obj[8] ? "" : (obj[8].toString()));
                validationDetails.put("occurrence", null == obj[9] ? "" : obj[9].toString());
                validationDetails.put("failedCount", null == obj[10] ? "" : obj[10].toString());
                validationDetails.put("displayStatus", obj[5].toString().equalsIgnoreCase(ValidationConstants.VALIDATION_TYPE_MANDATORY) ? "Error" : "Warning");
                validationDetails.put("dimensionCSV", null != obj[11] ? obj[11] : "");
                validationDetails.put("hasDimension", (null != obj[11] && !obj[11].toString().equalsIgnoreCase("")) ? "Y" : "N");
                validationDetails.put("validationDesc", obj[12] != null ? obj[12].toString() : "");
                validationDetails.put("evaluatedExp", obj[13] != null ? obj[13].toString() : ValidationConstants.NOT_APPLICABLE_VALUE);
                validationDetails.put("validationExp", obj[14] != null ? obj[14].toString() : "");
                validationDetails.put("occurrenceFor", ValidationConstants.NOT_APPLICABLE_VALUE);
                validationDetails.put("comment", "");
                validationDetails.put("hashKey", ValidationConstants.NO_OCCURRENCE_HASHKEY);

                if (validationDetails.get("type").toString().equalsIgnoreCase(ValidationConstants.VALIDATION_TYPE_OPTIONAL)
                        && validationDetails.get("isCommentAtValidation").toString().equalsIgnoreCase("Y")) {

                    validationDetails.put("comment", commentCheck.get(validationDetails.get("validationId").toString() +
                            ValidationConstants.HASH_DELIMITER + ValidationConstants.NO_OCCURRENCE_HASHKEY) == null
                            ? "" : commentCheck.get(validationDetails.get("validationId").toString() +
                            ValidationConstants.HASH_DELIMITER + ValidationConstants.NO_OCCURRENCE_HASHKEY));
                }

                validationDetailsList.add(validationDetails);
            }

            if (validationResultType.equalsIgnoreCase(ValidationConstants.ALL_VALIDATION_RESULTS)
                    || validationResultType.equalsIgnoreCase(ValidationConstants.ERROR_VALIDATION_RESULT)) {
                for (Map<String, Object> detail : validationDetailsList) {
                    if (isValidationForExportToPdf) {
                        if (detail.get("isCommentAtValidation").toString().equalsIgnoreCase("Y"))
                            validationDetailsListWithOccurrences.add(detail);
                        else if (detail.get("hasDimension").toString().equalsIgnoreCase("Y")) {
                            addOccurrencesToList(solutionId, periodIdDate, orgId, regReportId, groupIdCSV, versionNo,
                                    periodId, detail, validationDetailsListWithOccurrences, true, commentCheck);
                        } else {
                            validationDetailsListWithOccurrences.add(detail);
                        }
                    } else {
                        if (detail.get("hasDimension").toString().equalsIgnoreCase("Y")) {
                            addOccurrencesToList(solutionId, periodIdDate, orgId, regReportId, groupIdCSV, versionNo,
                                    periodId, detail, validationDetailsListWithOccurrences, true, commentCheck);
                        } else {
                            validationDetailsListWithOccurrences.add(detail);
                        }
                    }
                }
            } else if (validationResultType.equalsIgnoreCase(ValidationConstants.WARNING_VALIDATION_RESULT)) {
                for (Map<String, Object> detail : validationDetailsList) {
                    if (detail.get("isCommentAtValidation").toString().equalsIgnoreCase("Y")) {
                        validationDetailsListWithOccurrences.add(detail);
                    } else if (detail.get("hasDimension").toString().equalsIgnoreCase("Y")) {

                        /*
                         * Logic to add record in sheet if the comment is not at validation level :
                         * If the request is for download-all, then add
                         * If the record has dimension, then add
                         *
                         * The only scenario where we won't add the record is, when we are downloading for Warnings and
                         * the record has no dimensions but in configuration we have set 'isCommentAtValidation' to 'true'.
                         * This is wrong configuration.
                         * */
                        addOccurrencesToList(solutionId, periodIdDate, orgId, regReportId, groupIdCSV, versionNo,
                                periodId, detail, validationDetailsListWithOccurrences, false, commentCheck);
                    }
                }
            }
        }

        return validationDetailsListWithOccurrences;
    }

    private void addOccurrencesToList(Integer solutionId, Date periodIdDate, Integer orgId, Integer regReportId,
                                      String groupIdCSV, Integer versionNo, Integer periodId, Map<String, Object> detail,
                                      List<Map<String, Object>> validationDetailsListWithOccurrences,
                                      boolean addCommentForEachOcc, Map<String, String> commentCheck) {
        List<Map<String, Object>> occurrenceList = getValidationDetailsForAllOccurrenceForDownload(solutionId,
                periodIdDate, orgId, regReportId, groupIdCSV, versionNo, periodId,
                Integer.parseInt(detail.get("validationId").toString()), detail.get("dimensionCSV").toString());

        if (occurrenceList != null && !occurrenceList.isEmpty()) {
            for (Map<String, Object> occ : occurrenceList) {
                try {
                    Map<String, Object> occMap = (Map<String, Object>) ObjectCloner.deepCopy(detail);
                    occMap.put("evaluatedExp", occ.get("evaluatedExp"));

                    String occStr = (occ.get("occurrenceFor") != null && !occ.get("occurrenceFor").toString().equals("")) ? occ.get("occurrenceFor").toString() : ValidationConstants.NOT_APPLICABLE_VALUE;

                    List<String> occrList = new ArrayList<>();

                    for (String dim : occStr.split(",")) {
                        String[] temp = dim.split(":");
                        if (temp != null && temp.length > 0) {
                            if (!listFixedColumns.contains(temp[0])) {
                                occrList.add(dim);
                            }
                        }
                    }

                    occMap.put("occurrenceFor", String.join(",", occrList));
                    occMap.put("hashKey", occ.get("hashKey"));

                    if (detail.get("type").toString().equalsIgnoreCase(ValidationConstants.VALIDATION_TYPE_OPTIONAL)) {
                        if (detail.get("isCommentAtValidation").toString().equalsIgnoreCase("Y") && addCommentForEachOcc) {
                            String comment = commentCheck.get(detail.get("validationId").toString() + ValidationConstants.HASH_DELIMITER + ValidationConstants.NO_OCCURRENCE_HASHKEY) == null
                                    ? "" : commentCheck.get(detail.get("validationId").toString() + ValidationConstants.HASH_DELIMITER + ValidationConstants.NO_OCCURRENCE_HASHKEY);
                            occMap.put("comment", comment);
                        } else {
                            occMap.put("comment", occ.get("comment"));
                        }
                    }
                    validationDetailsListWithOccurrences.add(occMap);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private List<Map<String, Object>> getValidationDetailsForAllOccurrenceForDownload(Integer solutionId, Date periodIdDate,
                                                                                      Integer orgId, Integer regReportId,
                                                                                      String groupIdCSV, Integer versionNo,
                                                                                      Integer periodId, Integer validationId,
                                                                                      String dimensionCSV) {
        List<Map<String, Object>> occurrenceList = new ArrayList<>();
        try {
            Integer runId = validationDaoFactory.getValidationDao().getMaxRunIdForValidation(solutionId, periodIdDate, orgId,
                    regReportId, groupIdCSV, versionNo, periodId, validationId);

            String outputDirectory = ApplicationProperties.getValue("app.validations.outputDirectory").trim();
            String fileName = "Validation_Result_" + runId + "_" + validationId;// + ".csv";

            DataReader dataReader = getJdbcReader(outputDirectory + runId, fileName);
//			DataReader dataReader = new CSVReader(new File(outputDirectory + runId + File.separator + fileName)).setFieldNamesInFirstRow(true);
            MemoryWriter memoryWriter = new MemoryWriter();
            Job.run(dataReader, memoryWriter);
            RecordList recordList = memoryWriter.getRecordList();

            List<String> dCSV = new ArrayList<>();
            if (dimensionCSV != null && !dimensionCSV.trim().equals("")) {
                dCSV = Arrays.stream(dimensionCSV.split("\\s*,\\s*")).map(String::trim).collect(Collectors.toList());
            }

            Map<String, Object> occurrenceData;
            String groupByDimCSV;
            Map<String, String> hashCommentMap = validationDaoFactory.getValidationDao().getCommentsAtOccurrenceLevel(periodId,
                    orgId, regReportId, versionNo, validationId);

            if (recordList != null && recordList.getRecordCount() > 0) {
                for (int i = 0; i < recordList.getRecordCount(); i++) {
                    if (!Boolean.parseBoolean(recordList.get(i).getField("Validation").getValueAsString())) {
                        occurrenceData = new HashMap<>();
                        groupByDimCSV = null;

                        if (!dCSV.isEmpty()) {
                            for (String dimName : dCSV) {
                                if (recordList.get(i).containsField(dimName)) {
                                    if (groupByDimCSV == null) {
                                        groupByDimCSV = "";
                                    } else {
                                        groupByDimCSV = groupByDimCSV + ",";
                                    }

                                    groupByDimCSV = groupByDimCSV + dimName + ":" + (recordList.get(i).getField(dimName).getValueAsString() == null
                                            ? "" : recordList.get(i).getField(dimName).getValueAsString());
                                }
                            }
                        }

                        occurrenceData.put("occurrenceFor", groupByDimCSV != null ? groupByDimCSV : "");
                        occurrenceData.put("evaluatedExp", recordList.get(i).getField("Evaluated Expression").getValueAsString());
                        occurrenceData.put("comment", hashCommentMap.get(validationId + ValidationConstants.HASH_DELIMITER + recordList.get(i).getField("Hash Key").getValueAsString()) == null ? "" :
                                hashCommentMap.get(validationId + ValidationConstants.HASH_DELIMITER + recordList.get(i).getField("Hash Key").getValueAsString()));
                        occurrenceData.put("hashKey", recordList.get(i).getField("Hash Key").getValueAsString());
                        occurrenceList.add(occurrenceData);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to get occurrences for validation id : " + validationId + "--" + e);
            e.printStackTrace();
        }

        return occurrenceList;
    }

    @Override
    public ValidationRequest getValidationRequestByRunId(Integer runId) {
        logger.info("EXEFLOW --> ValidationExecutionBoImpl --> getValidationRequestByRunId()");
        return validationDaoFactory.getValidationDao().getValidationRequestByRunId(runId);
    }

    @Override
    public void markCurrentExecutionOfValidationasFailed(ValidationReturnResult vrr, ValidationRequest vr) {
        logger.info("EXEFLOW --> ValidationExecutionBoImpl --> markCurrentExecutionOfValidationasFailed()");
        validationDaoFactory.getValidationDao().markCurrentExecutionOfValidationasFailed(vrr, vr);

    }

    @Override
    public void getValidationReportWoorkbook(Integer solutionId, List<Map<String, Object>> validationDetailsList, Map<String, Object> indexDetails,
                                             String validationResultType, Map<String, Map<String, Object>> entityInfos, String filePath, String downloadFileStatusKey) throws Throwable {
        logger.info("EXEFLOW --> ValidationExecutionBoImpl --> getValidationReportWoorkbook()");
        Workbook validationReportWorkbookCopy = new Workbook();
        Worksheet resultWorksheet = null;
        Boolean isDownloadWarning = false;
        Integer indexStartRow = null;
        Integer indexStartCol = null;
        Set<String> formNames = new HashSet<>();
        Boolean isDownloadFormatPDF = false;
        String outputDirectory = ApplicationProperties.getValue("app.validations.outputDirectory").trim()
                + File.separator + "downloads" + File.separator + UUID.randomUUID();

        try {
            String templatePath = ((String) indexDetails.get(ValidationConstants.DOWNLOAD_FORMAT)).equalsIgnoreCase(ValidationConstants.DOWNLOAD_FORMAT_PDF) ? ApplicationProperties.getValue("app.validations.validationFilePathForReturnDataPDF") : ApplicationProperties.getValue("app.validations.validationFilePath");
            Workbook validationReportWorkbook = new Workbook(templatePath);
            validationReportWorkbookCopy.copy(validationReportWorkbook);

            Worksheet indexWorksheet = validationReportWorkbookCopy.getWorksheets().get(ApplicationProperties.getValue("app.validations.indexSheetName"));
            resultWorksheet = validationReportWorkbookCopy.getWorksheets().get(ApplicationProperties.getValue("app.validations.resultSheetName"));

            if (((String) indexDetails.get(ValidationConstants.DOWNLOAD_FORMAT)).equalsIgnoreCase(ValidationConstants.DOWNLOAD_FORMAT_PDF)) {
                isDownloadFormatPDF = true;
                for (String key : indexDetails.keySet()) {
                    String indexSheetStartPoint = validationProperties.getProperty("app.validation.indexSheet." + key + "Index");
                    indexStartRow = null;
                    indexStartCol = null;
                    if (indexSheetStartPoint != null && indexSheetStartPoint.split(",").length == 2) {
                        indexStartRow = Integer.parseInt(indexSheetStartPoint.split(",")[0]);
                        indexStartCol = Integer.parseInt(indexSheetStartPoint.split(",")[1]);
                    }
                    if (indexStartRow != null && indexStartCol != null) {
                        indexWorksheet.getCells().get(indexStartRow, indexStartCol).setValue(indexDetails.get(key));
                    }
                    if (ValidationConstants.ORGANIZATION.equalsIgnoreCase(key)) {
                        break;
                    }
                }
            } else {
                for (String key : indexDetails.keySet()) {
                    String indexSheetStartPoint = validationProperties.getProperty("app.validation.indexSheet." + key + "Index");
                    indexStartRow = null;
                    indexStartCol = null;
                    if (indexSheetStartPoint != null && indexSheetStartPoint.split(",").length == 2) {
                        indexStartRow = Integer.parseInt(indexSheetStartPoint.split(",")[0]);
                        indexStartCol = Integer.parseInt(indexSheetStartPoint.split(",")[1]);
                    }
                    if (indexStartRow != null && indexStartCol != null) {
                        indexWorksheet.getCells().get(indexStartRow, indexStartCol).setValue(indexDetails.get(key));
                    }
                }
            }

            if (validationDetailsList != null && !validationDetailsList.isEmpty()) {
                String resultSheetStartPoint = validationProperties.getProperty("app.validation.resultSheet.cellStartIndex");
                if (resultSheetStartPoint != null && resultSheetStartPoint.split(",").length == 2) {

                    indexStartRow = Integer.parseInt(resultSheetStartPoint.split(",")[0]);
                    indexStartCol = Integer.parseInt(resultSheetStartPoint.split(",")[1]);
                    Cell cell = null;
                    Style style = null;
                    boolean commentLockRequired = true;
                    if (validationResultType.equalsIgnoreCase(ValidationConstants.WARNING_VALIDATION_RESULT)) {
                        commentLockRequired = false;
                        isDownloadWarning = true;
                    }
                    Set<Integer> validationId = new HashSet<>();

                    for (int i = 0; i < validationDetailsList.size(); i++) {
                        Map<String, Object> row = validationDetailsList.get(i);
                        if (isDownloadFormatPDF) {
                            resultWorksheet.getCells().get(i + indexStartRow, indexStartCol + 0).setValue(row.get(ValidationConstants.FORM_NAME));
                            resultWorksheet.getCells().get(i + indexStartRow, indexStartCol + 1).setValue(row.get(ValidationConstants.DISPLAY_STATUS));
                            resultWorksheet.getCells().get(i + indexStartRow, indexStartCol + 2).setValue(row.get(ValidationConstants.VALIDATION_CODE));
                            resultWorksheet.getCells().get(i + indexStartRow, indexStartCol + 3).setValue(row.get(ValidationConstants.VALIDATION_NAME));
                            resultWorksheet.getCells().get(i + indexStartRow, indexStartCol + 4).setValue(row.get(ValidationConstants.VALIDATION_DESC));
                            resultWorksheet.getCells().get(i + indexStartRow, indexStartCol + 5).setValue(row.get(ValidationConstants.OCCURRENCE_FOR));
                            resultWorksheet.getCells().get(i + indexStartRow, indexStartCol + 6).setValue(row.get(ValidationConstants.VALIDATION_EXP));
                            resultWorksheet.getCells().get(i + indexStartRow, indexStartCol + 7).setValue(row.get(ValidationConstants.EVALUATED_EXP));

                            cell = resultWorksheet.getCells().get(i + indexStartRow, indexStartCol + 8);
                            style = cell.getStyle();
                            if (commentLockRequired) {
                                style.setLocked(true);
                            } else {
                                style.setLocked(false);
                            }
                            cell.setValue(row.get(ValidationConstants.COMMENT));
                            cell.setStyle(style);
                        } else {
                            formNames.add(row.get(ValidationConstants.FORM_NAME).toString());

                            resultWorksheet.getCells().get(i + indexStartRow, indexStartCol + 0).setValue(row.get(ValidationConstants.FORM_NAME));
                            resultWorksheet.getCells().get(i + indexStartRow, indexStartCol + 2).setValue(row.get(ValidationConstants.DISPLAY_STATUS));
                            resultWorksheet.getCells().get(i + indexStartRow, indexStartCol + 3).setValue(row.get(ValidationConstants.VALIDATION_CODE));
                            resultWorksheet.getCells().get(i + indexStartRow, indexStartCol + 4).setValue(row.get(ValidationConstants.VALIDATION_NAME));
                            resultWorksheet.getCells().get(i + indexStartRow, indexStartCol + 5).setValue(row.get(ValidationConstants.VALIDATION_DESC));
                            resultWorksheet.getCells().get(i + indexStartRow, indexStartCol + 6).setValue(row.get(ValidationConstants.OCCURRENCE_FOR));
                            resultWorksheet.getCells().get(i + indexStartRow, indexStartCol + 7).setValue(row.get(ValidationConstants.VALIDATION_EXP));
                            resultWorksheet.getCells().get(i + indexStartRow, indexStartCol + 8).setValue(row.get(ValidationConstants.EVALUATED_EXP));

                            cell = resultWorksheet.getCells().get(i + indexStartRow, indexStartCol + 9);
                            style = cell.getStyle();
                            if (commentLockRequired) {
                                style.setLocked(true);
                            } else {
                                style.setLocked(false);
                            }
                            cell.setValue(row.get(ValidationConstants.COMMENT));
                            cell.setStyle(style);

                            /* hidden column */
                            if (isDownloadWarning) {
                                cell = resultWorksheet.getCells().get(i + indexStartRow, indexStartCol + HIDDEN_COMMENT_COLUMN_INDEX);
                                style = cell.getStyle();
                                style.setLocked(true);

                                cell.setValue(row.get(ValidationConstants.COMMENT));
                                cell.setStyle(style);

                                cell = resultWorksheet.getCells().get(i + indexStartRow, indexStartCol + HIDDEN_HASHCODE_COLUMN_INDEX);
                                style = cell.getStyle();
                                style.setLocked(true);

                                cell.setValue(row.get(ValidationConstants.HASH_KEY));
                                cell.setStyle(style);

                                cell = resultWorksheet.getCells().get(i + indexStartRow, indexStartCol + HIDDEN_VALIDATION_ID_COLUMN_INDEX);
                                style = cell.getStyle();
                                style.setLocked(true);

                                cell.setValue(row.get(ValidationConstants.VALIDATION_ID));
                                cell.setStyle(style);

                                if (row.get(ValidationConstants.VALIDATION_ID) != null) {
                                    validationId.add(Integer.parseInt(row.get(ValidationConstants.VALIDATION_ID).toString()));
                                }
                            }
                        }
                    }

                    if (!isDownloadFormatPDF && isDownloadWarning) {
                        cell = resultWorksheet.getCells().get(indexStartRow, indexStartCol + HIDDEN_LATEST_COMMENT_MODIF_DATE_COL_INDEX);
                        style = cell.getStyle();
                        style.setLocked(true);
                        style.setNumber(1);

                        Date date = getLatestCommentModificationDate(validationId);
                        if (date != null) {
                            cell.setValue(date.getTime());
                        } else {
                            cell.setValue("");
                        }
                        cell.setStyle(style);
                    }

                    if (!isDownloadFormatPDF) {
                        Connection conn = null;
                        try {
                            conn = PersistentStoreManager.getSolutionDBConnection(solutionId);

                            // creating directory
                            new File(outputDirectory).mkdir();

                            for (String formName : formNames) {
                                Map<String, Object> entityInfo = entityInfos.get(formName);

                                if (entityInfo != null) {
                                    String dataSetQuery = entityInfo.get("query").toString();
                                    DataReader reader = validationDaoFactory.getValidationDao().getDataReader(dataSetQuery, conn);
                                    TransformingReader transformingReader = new TransformingReader(reader);
                                    Map<String, String> colNameToColKey = new HashMap<>();
                                    List<Map<String, Object>> columns = (List<Map<String, Object>>) entityInfo.get("columns");
                                    List<String> pkCols = new ArrayList<>();
                                    for (int i = 0; i < columns.size(); i++) {
                                        Map<String, Object> col = columns.get(i);
                                        if ((boolean) col.get("isKey") && !listFixedColumnKeys.contains(col.get("attributeKey"))) {
                                            pkCols.add(col.get("attributeName").toString().toUpperCase());
                                        }
                                        colNameToColKey.put(col.get("attributeName").toString().toUpperCase(), col.get("attributeKey").toString());
                                    }

                                    transformingReader.add(new Transformer() {
                                        private int num = 0;

                                        public boolean transform(Record record) throws Throwable {
                                            Field hash = record.getField("hash", true);
                                            Field lineNumber = record.getField("lineNumber", true);
                                            String hashKey = "";
                                            for (String col : pkCols) {
                                                if (record.getField(col).isNotNull()) {
                                                    hashKey = hashKey + "~~||~~" + record.getField(col).getValue().toString().trim().toUpperCase();
                                                }
                                            }

                                            hash.setValue("#_" + hashKey.hashCode());
                                            lineNumber.setValue(++num);
                                            return true;
                                        }
                                    });


                                    DataWriter writer = new CSVWriter(new File(outputDirectory + File.separator + formName + ".csv"));
                                    Job.run(transformingReader, writer);

                                    for (int i = 0; i < validationDetailsList.size(); i++) {
                                        Map<String, Object> row = validationDetailsList.get(i);
                                        if (row.get(ValidationConstants.FORM_NAME).toString().equalsIgnoreCase(formName)) {
                                            Map<String, Object> dimColNameToValue = new LinkedHashMap<>();
                                            String dimValues = (String) row.get(ValidationConstants.OCCURRENCE_FOR);
                                            if (dimValues != null && !dimValues.isEmpty() && !dimValues.equalsIgnoreCase(ValidationConstants.NOT_APPLICABLE_VALUE)) {
                                                dimValues = dimValues.toUpperCase();
                                                for (String dim : dimValues.split(",")) {
                                                    String[] tempStr = dim.split(":");
                                                    dimColNameToValue.put(tempStr[0], tempStr[1]);
                                                }

                                                if (pkCols.size() == dimColNameToValue.keySet().size() && pkCols.containsAll(dimColNameToValue.keySet())) {
                                                    String hashKey = "";
                                                    for (String col : pkCols) {
                                                        if (dimColNameToValue.get(col) != null && !dimColNameToValue.get(col).equals("")) {
                                                            hashKey = hashKey + "~~||~~" + dimColNameToValue.get(col).toString().trim().toUpperCase();
                                                        }
                                                    }
                                                    resultWorksheet.getCells().get(i + indexStartRow, indexStartCol + OCC_HASHCODE_COLUMN_INDEX).setValue("#_" + hashKey.hashCode());
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (Throwable e) {
                            throw new Exception(e);
                        } finally {
                            if (conn != null) {
                                try {
                                    conn.close();
                                } catch (SQLException e) {
                                    throw new Exception(e);
                                }
                            }
                        }
                    }
                }
            }

            /* Don't remove this save from here */
            validationReportWorkbookCopy.save(filePath);

            if (!isDownloadFormatPDF) {
                ExcelDocument document = new ExcelDocument().open(new File(filePath));
                Map<Object, Object> hashValueLineNumber = new HashMap<>();
                for (String formName : formNames) {
                    File lookupCsv = new File(outputDirectory + File.separator + formName + ".csv");
                    if (!lookupCsv.exists()) {
                        continue;
                    }
                    DataReader reader = new ExcelReader(document)
                            .setSheetName("ValidationResult")
                            .setFieldNamesInFirstRow(true);

                    Lookup lookup = new DataReaderLookup(
                            new CSVReader(lookupCsv)
                                    .setFieldNamesInFirstRow(true),
                            new FieldList("hash"),
                            new FieldList("lineNumber")
                    );

                    reader = new TransformingReader(reader)
                            .add(new LookupValidationTransformer(new FieldList("hash"), lookup));
                    MemoryWriter memoryWirter = new MemoryWriter();
                    Job.run(reader, memoryWirter);
                    RecordList records = memoryWirter.getRecordList();
                    if (records != null && records.getRecordCount() > 0) {
                        for (Record rec : records) {
                            hashValueLineNumber.put(rec.getField("hash").getValue() + "_" + formName, rec.getField("lineNumber").getValue());
                        }
                    }
                }

                Cells cells = resultWorksheet.getCells();
                RowCollection rows = cells.getRows();
                int occHashColInd = indexStartCol + OCC_HASHCODE_COLUMN_INDEX;
                int formNameColInd = indexStartCol + 0;
                Object lineNumber;
                for (int ri = 0; ri < rows.getCount(); ri++) {
                    Object hashValue = cells.get(ri, occHashColInd).getValue();
                    String formName = cells.get(ri, formNameColInd).getStringValue();
                    lineNumber = hashValueLineNumber.get(hashValue + "_" + formName);
                    if (lineNumber != null && !lineNumber.equals("NA")) {
                        resultWorksheet.getCells().get(ri, indexStartCol + 1).setValue(Integer.parseInt(lineNumber.toString()) + listDataStartIndex);
                    }
                }
            }

            resultWorksheet.getCells().deleteColumn(indexStartCol + OCC_HASHCODE_COLUMN_INDEX);

            if (isDownloadWarning) {
                resultWorksheet.getCells().hideColumns(26, 4);
                Protection prot = resultWorksheet.getProtection();
                prot.setAllowDeletingColumn(false);
                prot.setAllowDeletingRow(false);
                prot.setAllowEditingContent(false);
                prot.setAllowInsertingColumn(false);
                prot.setAllowInsertingRow(false);
                prot.setAllowFiltering(true);
                prot.setAllowSorting(true);
                prot.setAllowSelectingLockedCell(true);
                prot.setAllowSelectingUnlockedCell(true);
                prot.setAllowFormattingColumn(false);
                prot.setPassword(validationProperties.getProperty("app.validation.resultSheet.protection.password"));
            }

            validationReportWorkbookCopy.save(filePath);

            Map<String, Object> downloadValidationInfo = (Map<String, Object>) CacheCoordinator.get(RedisKeys.CONFIGURED_EXPORT_VALIDATION_RESULTS.getKey(), downloadFileStatusKey);
            downloadValidationInfo.put(ValidationConstants.STATUS, "COMPLETED");
            downloadValidationInfo.put(ValidationConstants.FILE_PATH, filePath);
            CacheCoordinator.save(RedisKeys.CONFIGURED_EXPORT_VALIDATION_RESULTS.getKey(), downloadFileStatusKey, downloadValidationInfo);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        } finally {
            FileUtils.deleteQuietly(new File(outputDirectory));
        }
    }

    @Async
    @Override
    public void uploadComment(Integer solutionId, Date periodIdDate, Integer orgId, Integer regReportId,
                              String groupIdCSV, Integer versionNo, Integer periodId, Integer regReportVersion,
                              String warningValidationResult, String formNameCSV, Worksheet resultWorksheet,
                              String[] headers, Integer userId, String uploadKey) throws Throwable {
        logger.info("EXEFLOW --> ValidationExecutionBoImpl --> uploadComment()");

        try {
            List<ValidationComments> validationComments = new ArrayList<>();
            Map<String, String> hashKeyMap = new HashMap<>();
            List<Row> rows = new ArrayList<>();
            Set<Integer> validationIds = new HashSet<>();
            String oldComment;
            String newComment;
            String[] resultStartCellIndex = validationProperties.getProperty("app.validation.resultSheet.cellStartIndex")
                    .trim().split("\\s*,\\s*");

            Iterator<Row> rowIterator = resultWorksheet.getCells().getRowEnumerator();
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                if (row.getIndex() >= Integer.parseInt(resultStartCellIndex[0])) {
                    oldComment = row.get(HIDDEN_COMMENT_COLUMN_INDEX).getValue() != null
                            ? row.get(HIDDEN_COMMENT_COLUMN_INDEX).getStringValue() : "";
                    newComment = row.get(COMMENT_COLUMN_INDEX).getValue() != null
                            ? row.get(COMMENT_COLUMN_INDEX).getStringValue() : "";

                    if (!oldComment.trim().equals(newComment.trim())) {
                        rows.add(row);
                        validationIds.add(Integer.parseInt(row.get(HIDDEN_VALIDATION_ID_COLUMN_INDEX).getStringValue()));
                    }
                }
            }

            List<Map<String, Object>> validationDetailsList = getValidationDetailsForAllFormsForDownload(
                    solutionId, periodIdDate, orgId, regReportId, groupIdCSV, versionNo, periodId, regReportVersion,
                    ValidationConstants.WARNING_VALIDATION_RESULT, formNameCSV, validationIds, false);
            Map<String, Object> result;

            for (Row row : rows) {
                final String hashKey = row.get(HIDDEN_HASHCODE_COLUMN_INDEX).getStringValue();

                if (!hashKeyMap.containsKey(hashKey)) {
                    final String oldComment1 = row.get(HIDDEN_COMMENT_COLUMN_INDEX).getValue() != null
                            ? row.get(HIDDEN_COMMENT_COLUMN_INDEX).getStringValue() : "";
                    final String newComment1 = row.get(COMMENT_COLUMN_INDEX).getValue() != null
                            ? row.get(COMMENT_COLUMN_INDEX).getStringValue() : "";
                    final String valId = row.get(HIDDEN_VALIDATION_ID_COLUMN_INDEX).getStringValue();

					/*
					Logic to get newly added comment:
					-> First checking whether comment has changed or not.
					-> if changed then checking whether the status is of type 'Warning' or not.
					-> If status is warning, then comparing validationId and hashKey.
					-> If both matches, then save it.
					 */
                    result = validationDetailsList.stream()
                            .filter(detail -> detail.get("displayStatus").toString().equalsIgnoreCase(ValidationConstants.WARNING_VALIDATION_RESULT)
                                    && detail.get("validationId").toString().equals(valId) && detail.get("hashKey").toString().equals(hashKey))
                            .findFirst().orElse(null);

                    if (result != null) {
                        if (!oldComment1.equals(newComment1)) {
                            validationComments.add(new ValidationComments(periodId, regReportId, versionNo,
                                    Integer.parseInt(valId), hashKey, orgId, newComment1,
                                    Calendar.getInstance().getTime(), userId, "N"));
                            if (!hashKey.equals(ValidationConstants.NO_OCCURRENCE_HASHKEY)) {
                                hashKeyMap.put(hashKey, hashKey);
                            }
                        }
                    } else {
                        throw new IllegalOperationException(validationProperties.getProperty("app.validation.uploadErrorCommentErrorMsg"));
                    }
                }
            }

            //save comments
            if (!validationComments.isEmpty()) {
                uploadComments(solutionId, periodIdDate, orgId, regReportId, groupIdCSV, versionNo, periodId, validationComments);
            }

            updateUploadCommentStatusInfo(uploadKey, true, "", false,
                    "Comments upload completed.", "");
        } catch (Exception e) {
            e.printStackTrace();
            updateUploadCommentStatusInfo(uploadKey, false,
                    validationProperties.getProperty("app.validation.commnetSaveFailedErrorMsg"), false, "", "");
        }
    }

    @Override
    public Date getLatestCommentModificationDate(Set<Integer> validationId) {
        logger.info("EXEFLOW --> ValidationExecutionBoImpl --> getLatestCommentModificationDate()");
        return validationDaoFactory.getValidationDao().getLatestCommentModificationDate(validationId);
    }

    @Override
    public boolean checkIfFileHasLatestComments(Worksheet resultWorksheet) {
        logger.info("EXEFLOW --> ValidationExecutionBoImpl --> checkIfFileHasLatestComments()");

        Set<Integer> allValidationIds = new HashSet<>();
        String[] resultStartCellIndex = validationProperties.getProperty("app.validation.resultSheet.cellStartIndex")
                .trim().split("\\s*,\\s*");

        Iterator<Row> rowIterator = resultWorksheet.getCells().getRowEnumerator();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            if (row.getIndex() >= Integer.parseInt(resultStartCellIndex[0])) {

                if (row.get(COMMENT_COLUMN_INDEX).getStyle().isLocked()) {
                    throw new IllegalOperationException(validationProperties.getProperty("app.validation.invalidFile"));
                }

                if (row.get(COMMENT_COLUMN_INDEX).getValue() != null &&
                        row.get(COMMENT_COLUMN_INDEX).getStringValue().length() >
                                Integer.parseInt(validationProperties.getProperty("app.validation.maxCommentLength"))) {
                    throw new IllegalOperationException(validationProperties.getProperty("app.validation.commentLengthExceedErrorMsg"));
                }

                if (row.get(HIDDEN_VALIDATION_ID_COLUMN_INDEX).getValue() == null ||
                        row.get(HIDDEN_HASHCODE_COLUMN_INDEX).getValue() == null) {
					/*
					If any of the hidden validation Id or hash key is not present, then the sheet might be corrupted.
					Don't proceed further in this case.
					 */
                    //throw new IllegalOperationException("File might have been compromised.");
                    throw new IllegalOperationException(validationProperties.getProperty("app.validation.invalidFile"));
                } else {
                    allValidationIds.add(Integer.parseInt(row.get(HIDDEN_VALIDATION_ID_COLUMN_INDEX).getStringValue()));
                }
            }
        }

        Date date = getLatestCommentModificationDate(allValidationIds);
        String latestDateInSheet = resultWorksheet.getCells().get(Integer.parseInt(resultStartCellIndex[0]),
                HIDDEN_LATEST_COMMENT_MODIF_DATE_COL_INDEX).getStringValue();

        if (date == null && (latestDateInSheet == null || latestDateInSheet.equals(""))) {
            return true;
        } else if (date != null && latestDateInSheet != null && !latestDateInSheet.equals("") && (date.getTime() == Long.parseLong(latestDateInSheet))) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void generateValidationMetaData(org.json.simple.JSONArray jsonObj, Integer solId) {

        List<ValidationMaster> vmList = new ArrayList<>();

        if (jsonObj == null) {
            List<ValidationMaster> vms = validationDaoFactory.getValidationDao().getAllValidations(solId);
            if (!vms.isEmpty()) {
                vmList.addAll(vms);
            }
        } else {
            for (int i = 0; i < jsonObj.size(); i++) {
                org.json.simple.JSONObject obj = (org.json.simple.JSONObject) jsonObj.get(i);

                Integer validationId = Integer.parseInt(obj.get("validationId").toString());

                if (obj.get("sequenceNo") != null && obj.get("sequenceNo").toString().trim().length() > 0) {
                    Integer sequenceNo = Integer.parseInt(obj.get("sequenceNo").toString());
                    ValidationMaster vm = validationDaoFactory.getValidationDao().getValidationBySequenceAndId(validationId, sequenceNo, solId);
                    if (vm != null) {
                        vmList.add(vm);
                    }
                } else {
                    List<ValidationMaster> vms = validationDaoFactory.getValidationDao().getValidationById(validationId, solId);
                    if (!vms.isEmpty()) {
                        vmList.addAll(vms);
                    }
                }

            }
        }

        if (vmList.size() > 0) {
            lineMetaDataHandler.addRequest(vmList);
        } else {
            logger.info("Validations not available for meta data generation");
        }
    }

    @Override
    public JSONObject getValidationOccurrenceDetailsByLineItemDetails(Integer solutionId, Date periodIdDate, Integer orgId,
                                                                      Integer regReportId, String groupIdCSV, Integer versionNo, Integer periodId, Integer regReportVersion,
                                                                      String lineItemBusinessName, Integer sectionId, String groupByColumn, Integer mapId, Integer validationId, Boolean isCommentAtValidation) {
        logger.info("EXEFLOW --> ValidationExecutionBoImpl --> getValidationDetailsByLineItemDetails() -- li type grid");
        Integer runId = validationDaoFactory.getValidationDao().getMaxRunIdForValidation(solutionId, periodIdDate, orgId, regReportId, groupIdCSV, versionNo, periodId, validationId);
        JSONObject temp = null;
        Boolean checkNavigable = getFirstRecordInCSVtoFind(runId, validationId, periodId, orgId, regReportId, solutionId, lineItemBusinessName);
        if (checkNavigable) {
            ValidationRunDetails vrd = validationDaoFactory.getValidationDao().getValidationRunDetailsByRunId(runId, validationId);
            List<String> dCSV = new ArrayList<String>();
            if (vrd.getDimensionsCSV() != null && !vrd.getDimensionsCSV().equalsIgnoreCase("")) {
                dCSV.addAll(Arrays.asList(vrd.getDimensionsCSV().split(",")));

                for (String col : listFixedColumns) {
                    if (dCSV.contains(col)) {
                        dCSV.remove(col);
                    }
                }
            }

            if (isCommentAtValidation) {
                temp = getDataForOccurrenceByValidationIdAndLiItemGrid(runId, validationId, dCSV, new HashMap<String, String>(), periodId, orgId, regReportId, solutionId, groupByColumn);
            } else {
                temp = getDataForOccurrenceByValidationIdAndLiItemGrid(runId, validationId, dCSV, (HashMap<String, String>) validationDaoFactory.getValidationDao().getCommentsAtOccurrenceLevel(periodId, orgId, regReportId, versionNo, validationId), periodId, orgId, regReportId, solutionId, groupByColumn);
            }
            if (null != temp) {
                return temp;
            }
        }
        return getValidationDetailsForAllOccurrence(solutionId, periodIdDate, orgId, regReportId, groupIdCSV, versionNo, periodId, validationId, isCommentAtValidation);

    }

    @Override
    public JSONObject getValidationOccurrenceDetailsByLineItemDetails(Integer solutionId, Date periodIdDate, Integer orgId,
                                                                      Integer regReportId, String groupIdCSV, Integer versionNo, Integer periodId, Integer regReportVersion,
                                                                      String lineItemBusinessName, Integer sectionId, String groupByColumn, JSONObject primaryKeyValue, Integer validationId, Boolean isCommentAtValidation) {
        logger.info("EXEFLOW --> ValidationExecutionBoImpl --> getValidationDetailsByLineItemDetails() -- li type list");
        Integer runId = validationDaoFactory.getValidationDao().getMaxRunIdForValidation(solutionId, periodIdDate, orgId, regReportId, groupIdCSV, versionNo, periodId, validationId);
        Boolean checkNavigable = getFirstRecordInCSVtoFind(runId, validationId, periodId, orgId, regReportId, solutionId, lineItemBusinessName);
        JSONObject temp;
        ValidationRunDetails vrd = validationDaoFactory.getValidationDao().getValidationRunDetailsByRunId(runId, validationId);
        List<String> dCSV = new ArrayList<String>();
        if (vrd.getDimensionsCSV() != null && !vrd.getDimensionsCSV().equalsIgnoreCase("")) {
            dCSV.addAll(Arrays.asList(vrd.getDimensionsCSV().split(",")));

            for (String col : listFixedColumns) {
                if (dCSV.contains(col)) {
                    dCSV.remove(col);
                }
            }
        }

        if (isCommentAtValidation) {
            temp = getDataForOccurrenceByValidationIdAndLiItemList(runId, validationId, dCSV, new HashMap<String, String>(), periodId, orgId, regReportId, solutionId, primaryKeyValue);
        } else {
            temp = getDataForOccurrenceByValidationIdAndLiItemList(runId, validationId, dCSV, (HashMap<String, String>) validationDaoFactory.getValidationDao().getCommentsAtOccurrenceLevel(periodId, orgId, regReportId, versionNo, validationId), periodId, orgId, regReportId, solutionId, primaryKeyValue);
        }
        if (null != temp) {
            return temp;
        }
        return getValidationDetailsForAllOccurrence(solutionId, periodIdDate, orgId, regReportId, groupIdCSV, versionNo, periodId, validationId, isCommentAtValidation);

    }

    @Override
    public JSONObject getValidationDetailsByLineItemDetails(Integer solutionId, Date periodIdDate, Integer orgId,
                                                            Integer regReportId, String groupIdCSV, Integer versionNo, Integer periodId, Integer regReportVersion, String lineItemBusinessName, Integer sectionId) {
        logger.info("EXEFLOW --> ValidationExecutionBoImpl --> getValidationDetailsAtFormLevelByLineItem()");
        JSONObject validationList = new JSONObject();
        JSONObject validationDetails;
        JSONArray validationArr = new JSONArray();
        List<Object[]> res = validationDaoFactory.getValidationDao().getValidationDetailsAtFormLevelByLineItem(solutionId, periodIdDate, orgId,
                regReportId, groupIdCSV, versionNo, periodId, regReportVersion, lineItemBusinessName, sectionId);
        List<Integer> validationIdList = new ArrayList<>();
        for (Object[] obj : res) {
            if ("Y".equalsIgnoreCase(obj[3].toString()) && ValidationConstants.VALIDATION_TYPE_OPTIONAL.equalsIgnoreCase(obj[5].toString())) {
                validationIdList.add(Integer.parseInt(obj[0].toString()));
            }
        }
        Map<String, String> commentCheck = new HashMap<String, String>();
        if (validationIdList.size() > 0) {
            List<String> hashList = new ArrayList<String>();
            hashList.add(ValidationConstants.NO_OCCURRENCE_HASHKEY);
            commentCheck = validationDaoFactory.getValidationDao().fetchCommentsIfExistAtValidation(periodId, orgId, regReportId, validationIdList, hashList, versionNo);
        }


        /*
         * 0 - validationId
         * 1 - validationCode
         * 2 - validationName
         * 3 - is comment at validation
         * 4 - status
         * 5 - validation type
         * 6 - sectionId
         * 7 - sectionName
         * 8 - form Name
         * 9 - occurrence
         * 10 - failed count
         * 11 - has group by
         * */
        for (Object[] obj : res) {
            if (obj[4].toString().equalsIgnoreCase("FAILED")) {
                validationDetails = new JSONObject();
                validationDetails.put("validationId", null == obj[0] ? "" : Integer.parseInt(obj[0].toString()));
                validationDetails.put("validationCode", null == obj[1] ? "" : (obj[1].toString()));
                validationDetails.put("validationName", null == obj[2] ? "" : (obj[2].toString()));
                validationDetails.put("isCommentAtValidation", null == obj[3] ? "" : (obj[3].toString()));
                validationDetails.put("status", null == obj[4] ? "" : (obj[4].toString()));
                validationDetails.put("type", null == obj[5] ? "" : (obj[5].toString()));
                validationDetails.put("sectionId", null == obj[6] ? "" : Integer.parseInt(obj[6].toString()));
                validationDetails.put("sectionName", null == obj[7] ? "" : (obj[7].toString()));
                validationDetails.put("formName", null == obj[8] ? "" : (obj[8].toString()));
                validationDetails.put("occurrence", null == obj[9] ? "" : Integer.parseInt(obj[9].toString()));
                validationDetails.put("failedCount", null == obj[10] ? "" : Integer.parseInt(obj[10].toString()));
                validationDetails.put("displayStatus", (obj[4].toString().equalsIgnoreCase("FAILED") ?
                        (obj[5].toString().equalsIgnoreCase(ValidationConstants.VALIDATION_TYPE_MANDATORY) ? "Error" : "Warning") : "Passed"));
                validationDetails.put("comment", "");
                if ("Y".equalsIgnoreCase(obj[3].toString())) {
                    validationDetails.put("comment", commentCheck.get(obj[0].toString() + ValidationConstants.HASH_DELIMITER + ValidationConstants.NO_OCCURRENCE_HASHKEY) == null ? "" : commentCheck.get(obj[0].toString() + ValidationConstants.HASH_DELIMITER + "-1"));
                }
                validationDetails.put("hasDimension", (null != obj[11] && !obj[11].toString().equalsIgnoreCase("")) ? "Y" : "N");
                validationDetails.put("dimensionCSV", (null != obj[11]) ? obj[11].toString() : "");
                validationArr.add(validationDetails);
            }
        }
        validationList.put("validationData", validationArr);
        return validationList;

    }


    private JSONObject getDataForOccurrenceByValidationIdAndLiItemGrid(Integer runId, Integer validationId, List<String> dCSV,
                                                                       HashMap<String, String> hashCommentMap, Integer periodId, Integer orgId, Integer regReportId, Integer solutionId, String groupByDimension) {
        String outputDirectory = ApplicationProperties.getValue("app.validations.outputDirectory").trim();
        String fileName;
        JSONObject res = new JSONObject();
        JSONObject displayActualName = new JSONObject();
        JSONObject occ;
        JSONArray columnNames = new JSONArray();

        List<String> tempColumnNames = new ArrayList<String>();
        JSONArray columnData = new JSONArray();
        RecordList recordList;
        fileName = "Validation_Result_" + runId + "_" + validationId;// + ".csv";
        DataReader dataReader = getJdbcReader(outputDirectory + runId, fileName);
//		DataReader dataReader = new CSVReader(new File(outputDirectory + runId + File.separator + fileName)).setFieldNamesInFirstRow(true);
        MemoryWriter memoryWriter = new MemoryWriter();

        Job.run(dataReader, memoryWriter);

        if (memoryWriter.getRecordCount() > 0) {
            Integer recordIndex = 0;
            recordList = memoryWriter.getRecordList();
            if (null != groupByDimension) {
                recordIndex = recordList.findFirst(
                        new FilterExpression("GROUP_BY_DIMENSION=='" + groupByDimension + "'"), 0);
            }
            if (recordIndex.equals(-1)) {
                return null;
            }

            tempColumnNames.addAll(dCSV);
            tempColumnNames.add("validationResult");
            tempColumnNames.add("hashValue");
            tempColumnNames.add("comment");
            for (String c : tempColumnNames) {
                displayActualName.put(ValidationStringUtils.replace(c, " ", "____", -1, true), c);
                columnNames.add(ValidationStringUtils.replace(c, " ", "____", -1, true));
            }
            res.put("columnDisplayActualMap", displayActualName);
            res.put("columnNames", columnNames);
            if (!Boolean.parseBoolean(recordList.get(recordIndex).getField("Validation").getValueAsString())) {
                occ = new JSONObject();
                for (String dimName : dCSV) {
                    occ.put(ValidationStringUtils.replace(dimName, " ", "____", -1, true), (recordList.get(recordIndex).getField(dimName).getValueAsString() == null ? "" : recordList.get(recordIndex).getField(dimName).getValueAsString()));
                }
                occ.put("validationResult", Boolean.parseBoolean(recordList.get(recordIndex).getField("Validation").getValueAsString()));
                occ.put("hashValue", recordList.get(recordIndex).getField("Hash Key").getValueAsString());
                occ.put("comment", hashCommentMap.get(validationId + ValidationConstants.HASH_DELIMITER + recordList.get(recordIndex).getField("Hash Key").getValueAsString()) == null ? "" :
                        hashCommentMap.get(validationId + ValidationConstants.HASH_DELIMITER + recordList.get(recordIndex).getField("Hash Key").getValueAsString()));
                //Gson g = new Gson();
                // ExpressionMetaData emd = getExpressionMetaData(recordList.get(0).getField("Expression_Meta_data").getValueAsString());
                //JSONArray dnArr = getDataForNavigation(emd, recordList.get(recordIndex), periodId, orgId, regReportId, solutionId);
                //occ.put("dataForNavigation", dnArr);
                columnData.add(occ);
            }

            res.put("columnData", columnData);
        }

        return res;
    }

    private Boolean checkNavigableStatusForTheValidation(ExpressionMetaData emd, Record cr, Integer periodId, Integer orgId, Integer regReportId, Integer solutionId, String liName) {
        JSONObject primaryObj = new JSONObject();
        Map<String, String> primaryKeyMap = new HashMap<String, String>();
        if (emd.getIsNavigable() && periodId.equals(emd.getPeriodId()) && orgId.equals(emd.getOrgId()) && regReportId.equals(emd.getReportId())) {
            //processsing only if navigable items are present.
            primaryObj.put("type", emd.getReturnType());
            primaryObj.put("sheetName", emd.getFormName());
            if (emd.getReturnType().equalsIgnoreCase("LIST")) {
                emd.getBasePrimaryColumns().forEach((key, val1) -> {
                    String val = ValidationStringUtils.replace(val1, "\"", "", -1, true);
                    primaryObj.put(val, cr.getField(val).getValueAsString());
                    primaryKeyMap.put(val, cr.getField(val).getValueAsString());
                });
            } else if (emd.getReturnType().equalsIgnoreCase("GRID")) {
                emd.getBasePrimaryColumns().forEach((key, val) -> {
                    primaryObj.put(key, cr.getField(key).getValueAsString());
                });
            }
            if (emd.getReturnType().equalsIgnoreCase("LIST")) {

            } else if (emd.getReturnType().equalsIgnoreCase("GRID")) {
                for (String eCol : emd.getEntityCols()) {
                    //forming li details for normal column functions.
                    //liObject = new JSONObject();
                    //liObject.put("lineItemBusinessName", emd.getColumnInfo().get(eCol).get("BUSSINESS_NAME"));
                    //liObject.put("lineItemDesc", emd.getColumnInfo().get(eCol).get("DESC"));

                    if (eCol.replace("A_", "").equalsIgnoreCase(liName)) {
                        return true;
                    }
                }
            }


        }

        return false;
    }


    private Boolean getFirstRecordInCSVtoFind(Integer runId, Integer validationId, Integer periodId, Integer orgId, Integer regReportId, Integer solutionId, String liName) {
        String outputDirectory = ApplicationProperties.getValue("app.validations.outputDirectory").trim();
        String fileName;
        RecordList recordList;
        try {
            fileName = "Validation_Result_" + runId + "_" + validationId;// + ".csv";
            DataReader dataReader = getJdbcReader(outputDirectory + runId, fileName);
//		DataReader dataReader = new CSVReader(new File(outputDirectory + runId + File.separator + fileName)).setFieldNamesInFirstRow(true);
            MemoryWriter memoryWriter = new MemoryWriter();

            Job.run(dataReader, memoryWriter);

            if (memoryWriter.getRecordCount() > 0) {
                recordList = memoryWriter.getRecordList();
                Gson g = new Gson();
                ExpressionMetaData emd = getExpressionMetaData(recordList.get(0).getField("Expression_Meta_data").getValueAsString());
                return checkNavigableStatusForTheValidation(emd, recordList.get(0), periodId, orgId, regReportId, solutionId, liName);
            }

        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }


    private String generateHashKeyByPrimaryColumnAndValidationId(JSONObject pk, Integer validationId) {
        Iterator<String> pkKeys = pk.keys();
        List<String> keys = new ArrayList<String>();
        while (pkKeys.hasNext()) {
            keys.add(pkKeys.next());
        }
        Collections.sort(keys);
        String pkVals = "";
        for (String k : keys) {
            pkVals = pkVals + pk.get(k) + ValidationConstants.HASH_DELIMITER;
        }
        pkVals = pkVals + validationId;
        return getHashValueForExp(pkVals);
    }

    private JSONObject getDataForOccurrenceByValidationIdAndLiItemList(Integer runId, Integer validationId, List<String> dCSV,
                                                                       HashMap<String, String> hashCommentMap, Integer periodId, Integer orgId, Integer regReportId, Integer solutionId, JSONObject primaryKeyMap) {
        String outputDirectory = ApplicationProperties.getValue("app.validations.outputDirectory").trim();
        String fileName;
        JSONObject res = new JSONObject();
        JSONObject displayActualName = new JSONObject();
        JSONObject occ;
        JSONArray columnNames = new JSONArray();

        List<String> tempColumnNames = new ArrayList<String>();
        JSONArray columnData = new JSONArray();
        RecordList recordList;
        fileName = "Validation_Result_" + runId + "_" + validationId;// + ".csv";
        DataReader dataReader = getJdbcReader(outputDirectory + runId, fileName);
//		DataReader dataReader = new CSVReader(new File(outputDirectory + runId + File.separator + fileName)).setFieldNamesInFirstRow(true);
        MemoryWriter memoryWriter = new MemoryWriter();
        FilteringReader filteringReader = new FilteringReader(dataReader);
        String hashValue = generateHashKeyByPrimaryColumnAndValidationId(primaryKeyMap, validationId);
        filteringReader.add(new FieldFilter("Hash Key").addRule(new ValueMatch<String>(hashValue)));
        Job.run(filteringReader, memoryWriter);

        if (memoryWriter.getRecordCount() > 0) {
            Integer recordIndex = 0;
            recordList = memoryWriter.getRecordList();

            //recordIndex = recordList.findFirst(
            // new FilterExpression("Hash Key=='"+hashValue+"'"), 0);
            if (recordIndex.equals(-1)) {
                return null;
            }

            tempColumnNames.addAll(dCSV);
            tempColumnNames.add("validationResult");
            tempColumnNames.add("hashValue");
            tempColumnNames.add("comment");
            for (String c : tempColumnNames) {
                displayActualName.put(ValidationStringUtils.replace(c, " ", "____", -1, true), c);
                columnNames.add(ValidationStringUtils.replace(c, " ", "____", -1, true));
            }
            res.put("columnDisplayActualMap", displayActualName);
            res.put("columnNames", columnNames);
            if (!Boolean.parseBoolean(recordList.get(recordIndex).getField("Validation").getValueAsString())) {
                occ = new JSONObject();
                for (String dimName : dCSV) {
                    occ.put(ValidationStringUtils.replace(dimName, " ", "____", -1, true), (recordList.get(recordIndex).getField(dimName).getValueAsString() == null ? "" : recordList.get(recordIndex).getField(dimName).getValueAsString()));
                }
                occ.put("validationResult", Boolean.parseBoolean(recordList.get(recordIndex).getField("Validation").getValueAsString()));
                occ.put("hashValue", recordList.get(recordIndex).getField("Hash Key").getValueAsString());
                occ.put("comment", hashCommentMap.get(validationId + ValidationConstants.HASH_DELIMITER + recordList.get(recordIndex).getField("Hash Key").getValueAsString()) == null ? "" :
                        hashCommentMap.get(validationId + ValidationConstants.HASH_DELIMITER + recordList.get(recordIndex).getField("Hash Key").getValueAsString()));
                //Gson g = new Gson();
                //ExpressionMetaData emd = getExpressionMetaData(recordList.get(0).getField("Expression_Meta_data").getValueAsString());
                //JSONArray dnArr = getDataForNavigation(emd, recordList.get(recordIndex), periodId, orgId, regReportId, solutionId);
                //occ.put("dataForNavigation", dnArr);
                columnData.add(occ);
            }

            res.put("columnData", columnData);
            return res;
        }

        return null;
    }

    @Override
    public Object[] getReportAndVersionName(Integer periodId, Integer solutionId, Integer orgId, Integer versionNo,
                                            Integer regReportId) {
        logger.info("EXEFLOW --> ValidationExecutionBoImpl --> getReportAndVersionName()");
        return validationDaoFactory.getValidationDao().getReportAndVersionName(periodId,
                solutionId, orgId, versionNo, regReportId);
    }

    @Override
    public Integer getSectionIdForList(Integer solutionId, Integer regReportId, Integer regReportVersion,
                                       String sectionType, String lineItemBusinessName) {
        logger.info("EXEFLOW --> ValidationExecutionBoImpl --> getSectionIdForList()");
        return validationDaoFactory.getValidationDao().getReportAndVersionName(solutionId, regReportId, regReportVersion, sectionType, lineItemBusinessName);
    }

    @Override
    public String getGroupByColumnForGrid(Integer solutionId, Integer orgId, Integer regReportId, Integer versionNo,
                                          Integer periodId, Integer regReportVersion, Integer liId, Integer mapId) {
        logger.info("EXEFLOW --> ValidationExecutionBoImpl --> getGroupByColumnForGrid()");
        return validationDaoFactory.getValidationDao().getGroupByColumnForGrid(solutionId, orgId, regReportId, versionNo,
                periodId, regReportVersion, liId, mapId);
    }

    @Override
    public void resetValidationStatusOnEdit(Integer periodId, Integer orgId, Integer regReportId, Integer versionNo,
                                            Integer solutionId) {
        ValidationReturnResult vrr = fetchStatusForTheRun(periodId, orgId, regReportId, versionNo, solutionId);
        if (null != vrr) {
            vrr.setStatus(ValidationConstants.NOT_VALIDATED);
            validationDaoFactory.getValidationDao().updateValidationReturnResult(vrr);
        }
    }

    @Override
    public void updateUploadCommentStatusInfo(String uploadKey, Boolean uploadSuccess, String uploadMsg,
                                              Boolean hasChanged, String status, String promptMsg) throws Throwable {
        Map<String, Object> commnetUploadStatusInfo = (Map<String, Object>)
                CacheCoordinator.get(RedisKeys.CONFIGURED_UPLOAD_COMMENT_KEY.getKey(), uploadKey);

        if (commnetUploadStatusInfo == null) {
            commnetUploadStatusInfo = new HashMap<>();
        }

        if (uploadSuccess != null) {
            commnetUploadStatusInfo.put("uploadSuccess", uploadSuccess);
        }

        if (uploadMsg != null) {
            commnetUploadStatusInfo.put("uploadMsg", uploadMsg);
        }

        if (hasChanged != null) {
            commnetUploadStatusInfo.put("hasChanged", hasChanged);
        }

        if (status != null) {
            commnetUploadStatusInfo.put("status", status);
        }

        if (promptMsg != null) {
            commnetUploadStatusInfo.put("promptMsg", promptMsg);
        }

        CacheCoordinator.save(RedisKeys.CONFIGURED_UPLOAD_COMMENT_KEY.getKey(), uploadKey, commnetUploadStatusInfo);
    }

    private ExpressionMetaData getExpressionMetaData(String json) {
        //handled = ! < > ' ;
        json = json.replace("u003d", "=").replace("u0021", "!").replace("u003C", "<").replace("u003E", ">").replace("u0027", "'").replace("u003B", ";");
        Gson g = new Gson();
        ExpressionMetaData emd = null;
        try {
            //grid
            emd = g.fromJson(json, ExpressionMetaData.class);
        } catch (Exception e) {
            try {
                //list
                emd = g.fromJson(json.replace("\"\"\"}", "\"\"}").replace("\"\"\"", "\"\\\"").replace("\"\"", "\\\"\""),
                        ExpressionMetaData.class);

            } catch (Exception ex) {
                try {
                    //grid with sub expr
                    emd = g.fromJson(json.replace("\"\"\"}", "\"\"}").replace("\"\"\"", "\"\\\"")
                            .replace("\"entityName\":\"\"", "\"entityName\":\" \"").replace("\"\"", "\\\"")
                            .replace("\"entityName\":\"\"", "\"entityName\":\"\""), ExpressionMetaData.class);
                } catch (Exception x) {
                    throw x;
                }
            }
        }
        return emd;
    }

    // TODO need to fix admin dependencies in this method | Deepak
    @Override
    public JSONObject getWaivedOffValidations(ValidationRequest vr, String type) {
        return null;
		/*logger.info("EXEFLOW --> ValidationExecutionBoImpl --> getWaivedOffValidations()");
		List<ValidationWaiverDetails> vwList = new ArrayList<ValidationWaiverDetails>();
		List<String> vwValidationId = new ArrayList<String>();
		Set<ValidationWaiverDetails> vwValidationActive = new HashSet<ValidationWaiverDetails>();
		JSONObject waiverInfo = new JSONObject(); 
		JSONObject waivedOffValidations = new JSONObject(); 
		JSONObject waiverInfoObj =  new JSONObject();
		JSONObject applicabilityObj =  new JSONObject();
		JSONArray waiverInfoOfAll =  new JSONArray();
		Boolean vidFlag=false;
		try {
			JsonParser parser = new JsonParser();
			JsonObject pl = parser.parse(vr.getPayload()).getAsJsonObject();
			if(type.equalsIgnoreCase(ValidationConstants.TYPE_RETURN)) {
				vwList = validationDaoFactory.getValidationDao().fetchAllTheValidationWaiverHistory(vr.getSolutionId(),vr.getOrgId(),null!=pl.get("regReportId")?Integer.parseInt(pl.get("regReportId").toString().replace("\"", "")):null);
				Map<String, ValidationWaiverDetails> activeVersionWaiver =vwList.stream().filter(x->(x.getIsActiveVersion()==1)).collect(Collectors.toMap(vwd->vwd.getValidationWaiverId().getWaiverId(),vwd->vwd)); 
				for(ValidationWaiverDetails i:vwList) {
					waiverInfo=JSONObject.fromObject(i.getWaiverInfo());
					JSONArray vids = waiverInfo.getJSONArray("validationIds");
					vwValidationId.addAll(vids);
				}
				Map<Integer, String> validationCodeIdMap = validationDaoFactory.getValidationDao().fetchAllValidationCode(vwValidationId);
			    
				//creating a valid list of active waivers
				
				for(Entry<String, ValidationWaiverDetails> avw:activeVersionWaiver.entrySet()) {
					vidFlag=true;
					waiverInfo=JSONObject.fromObject(avw.getValue().getWaiverInfo());
					if(null!=pl.get("startDate") && null!=pl.get("endDate")) {
						if(null!=waiverInfo.get("endDate").toString() && !waiverInfo.get("endDate").toString().equals("") && !waiverInfo.get("endDate").toString().equalsIgnoreCase("null") 
								&& null!=waiverInfo.get("startDate").toString() && waiverInfo.get("startDate").toString()!=null && !waiverInfo.get("startDate").toString().equalsIgnoreCase("null") && !(pl.get("startDate").toString().equals(pl.get("endDate").toString()))){
							if(getPeriodIdFromLongDateFormat(waiverInfo.get("endDate").toString())<=getPeriodIdFromLongDateFormat(pl.get("endDate").toString().replace("\"", "")) && getPeriodIdFromLongDateFormat(waiverInfo.get("startDate").toString())>=getPeriodIdFromLongDateFormat(pl.get("startDate").toString().replace("\"", ""))){
								vidFlag=true;
							}else {
								vidFlag=false;
							}
						}else if(null!=waiverInfo.get("effectiveDate").toString() && !waiverInfo.get("effectiveDate").toString().equals("") && !waiverInfo.get("effectiveDate").toString().equalsIgnoreCase("null")
								&& null!=waiverInfo.get("versionNo").toString() && !waiverInfo.get("versionNo").toString().equals("") && !waiverInfo.get("versionNo").toString().equalsIgnoreCase("null")){
							if(getPeriodIdFromLongDateFormat(waiverInfo.get("effectiveDate").toString()).equals(getPeriodIdFromLongDateFormat(pl.get("startDate").toString().replace("\"", ""))) ) {
								vidFlag=true;
							}else {
								vidFlag=false;
							}					
						}
					}
					if(null!=pl.get("versionNumber") && !pl.get("versionNumber").toString().equalsIgnoreCase("null") && pl.get("versionNumber").toString()!="") {
						if(waiverInfo.get("versionNo").toString().equalsIgnoreCase(pl.get("versionNumber").toString().replace("\"", ""))) {
							if(null!=pl.get("startDate") && null!=pl.get("endDate")) {
								if(getPeriodIdFromLongDateFormat(waiverInfo.get("effectiveDate").toString())<=getPeriodIdFromLongDateFormat(pl.get("endDate").toString().replace("\"", "")) && getPeriodIdFromLongDateFormat(waiverInfo.get("effectiveDate").toString())>=getPeriodIdFromLongDateFormat(pl.get("startDate").toString().replace("\"", ""))) {
									vidFlag=true;
								}else {
									vidFlag=false;
								}
							}else {
								vidFlag=true;	
							}
						}else {
							vidFlag=false;
						}
					}
					if(vidFlag && null!=waiverInfo.getJSONArray("validationIds") && !waiverInfo.getJSONArray("validationIds").toString().equals("") 
							&& null!=pl.get("validationCode") && !pl.get("validationCode").toString().equals("")) {
						JSONArray vid = waiverInfo.getJSONArray("validationIds");
						List<String> validationCodeList = Arrays.asList((pl.get("validationCode").toString().replace("\"", "")).split(","));
						vidFlag=false;
						for(int j=0;j<vid.size();j++) {						
							if(validationCodeList.contains(validationCodeIdMap.get(Integer.parseInt((String)vid.get(j))))) {
								vidFlag=true;
							}else if(vidFlag) {
								vidFlag=true;
							}else {
								vidFlag=false;
							}
						}
					}
					if(vidFlag && null!=pl.get("isActive")) {
						if(avw.getValue().getIsActive().equals(Integer.parseInt(pl.get("isActive").toString().replace("\"", "")))) {
							vidFlag=true;
						}else {
							vidFlag=false;
						}
					}
					if(vidFlag) {
						vwValidationActive.add(avw.getValue());
					}
				}
				
				//creating a object by processing active waivers
				for(ValidationWaiverDetails vwi:vwValidationActive) {
					waiverInfo=JSONObject.fromObject(vwi.getWaiverInfo());
					JSONArray vid = waiverInfo.getJSONArray("validationIds");
					JSONArray waivedCode = new JSONArray();
					JSONArray history = new JSONArray();
					JSONObject temp = new JSONObject();
					waiverInfoObj=new JSONObject();
					
					waiverInfoObj.put("Waiver ID", vwi.getValidationWaiverId().getWaiverId());
					waiverInfoObj.put("Waiver Title", vwi.getWaiverTitle());
					waiverInfoObj.put("Organization", vwi.getOrgId()==null?"":adminCacheUtil.getOrganisationById(vwi.getOrgId()).getOrgCode());
					waiverInfoObj.put("Return",getReportName(vwi.getRegReportId(),vwi.getSolutionId()));
					if(!waiverInfo.get("startDate").toString().equalsIgnoreCase("null") && !waiverInfo.get("endDate").toString().equalsIgnoreCase("null")) {
						applicabilityObj.put("Reporting Start Date", (null!=waiverInfo.get("startDate") && !waiverInfo.get("startDate").toString().equalsIgnoreCase("null"))?converToDateFormat(waiverInfo.get("startDate").toString()):"");
						applicabilityObj.put("Reporting End Date",  (null!=waiverInfo.get("endDate") && !waiverInfo.get("endDate").toString().equalsIgnoreCase("null"))?converToDateFormat(waiverInfo.get("endDate").toString()):"");
					}else if(!waiverInfo.get("effectiveDate").toString().equalsIgnoreCase("null")){
						applicabilityObj.put("Reporting Start Date", (null!=waiverInfo.get("effectiveDate") && !waiverInfo.get("effectiveDate").toString().equalsIgnoreCase("null"))?converToDateFormat(waiverInfo.get("effectiveDate").toString()):"");
						applicabilityObj.put("Reporting End Date",  (null!=waiverInfo.get("effectiveDate") && !waiverInfo.get("effectiveDate").toString().equalsIgnoreCase("null"))?converToDateFormat(waiverInfo.get("effectiveDate").toString()):"");

					}
					applicabilityObj.put("Version", waiverInfo.get("version"));						
					waiverInfoObj.put("Applicability",applicabilityObj);
	
					for(int i=0;i<vid.size();i++) {
						if(null!=validationCodeIdMap.get(Integer.parseInt((String)vid.get(i)))) {
							waivedCode.add(validationCodeIdMap.get(Integer.parseInt((String)vid.get(i))));
						}
					}
					waiverInfoObj.put("Validations Waived", waivedCode);
					waiverInfoObj.put("Created Date", null!=vwi.getCreatedTime()?converToDateFormat(vwi.getCreatedTime().toString()):"");
					waiverInfoObj.put("Last Updated Date", null!=vwi.getLastModifiedTime()?converToDateFormat(vwi.getLastModifiedTime().toString()):"");
					waiverInfoObj.put("Status", vwi.getIsActive()==1?"Active":"In Active");
					waiverInfoObj.put("Created by", vwi.getCreatedBy()==null?"":adminCacheUtil.getUserById(vwi.getCreatedBy()).getFirstName()+" "+adminCacheUtil.getUserById(vwi.getCreatedBy()).getLastName());
					waiverInfoObj.put("Created by Organization", vwi.getOrgId()==null?"":adminCacheUtil.getOrganisationById(vwi.getOrgId()).getOrgCode());
					for(ValidationWaiverDetails v:vwList) {
						if(v.getIsActiveVersion()!=1) {
							if(vwi.getValidationWaiverId().getWaiverId().equals(v.getValidationWaiverId().getWaiverId())) {
								waiverInfo=JSONObject.fromObject(v.getWaiverInfo());
								temp.put("Modification Date", null!=v.getLastModifiedTime()?converToDateFormat(v.getLastModifiedTime().toString()):"");
								temp.put("Modified By User", v.getLastModifiedBy()==null?"":adminCacheUtil.getUserById(v.getLastModifiedBy()).getFirstName()+" "+adminCacheUtil.getUserById(v.getLastModifiedBy()).getLastName());
								temp.put("Modified by Org", v.getOrgId()==null?"":adminCacheUtil.getOrganisationById(v.getOrgId()).getOrgCode());
								if(!waiverInfo.get("startDate").toString().equalsIgnoreCase("null") && !waiverInfo.get("endDate").toString().equalsIgnoreCase("null")) {
									applicabilityObj.put("Reporting Start Date", (null!=waiverInfo.get("startDate") && !waiverInfo.get("startDate").toString().equalsIgnoreCase("null"))?converToDateFormat(waiverInfo.get("startDate").toString()):"");
									applicabilityObj.put("Reporting End Date",  (null!=waiverInfo.get("endDate") && !waiverInfo.get("endDate").toString().equalsIgnoreCase("null"))?converToDateFormat(waiverInfo.get("endDate").toString()):"");
								}else if(!waiverInfo.get("effectiveDate").toString().equalsIgnoreCase("null")){
									applicabilityObj.put("Reporting Start Date", (null!=waiverInfo.get("effectiveDate") && !waiverInfo.get("effectiveDate").toString().equalsIgnoreCase("null"))?converToDateFormat(waiverInfo.get("effectiveDate").toString()):"");
									applicabilityObj.put("Reporting End Date",  (null!=waiverInfo.get("effectiveDate") && !waiverInfo.get("effectiveDate").toString().equalsIgnoreCase("null"))?converToDateFormat(waiverInfo.get("effectiveDate").toString()):"");

								}applicabilityObj.put("Version", waiverInfo.get("version"));	
								temp.put("Applicability", applicabilityObj);
								vid = waiverInfo.getJSONArray("validationIds");
								waivedCode=new JSONArray();
								for(int i=0;i<vid.size();i++) {
									if(null!=validationCodeIdMap.get(Integer.parseInt((String)vid.get(i)))) {
										if(!waivedCode.contains(validationCodeIdMap.get(Integer.parseInt((String)vid.get(i)))))
											waivedCode.add(validationCodeIdMap.get(Integer.parseInt((String)vid.get(i))));
									}
								}
								temp.put("Validations Waived", waivedCode);
								temp.put("Status", v.getIsActive()==1?"Active":"In Active");
								history.add(temp);
							}
						}
					}
					waiverInfoObj.put("History", history);
					waiverInfoOfAll.add(waiverInfoObj);
				}
				
				
			}else if(type.equalsIgnoreCase(ValidationConstants.TYPE_REFTABLE)) {
	
			}
			waivedOffValidations.put("Result", waiverInfoOfAll);
			return waivedOffValidations;
		}catch (Throwable e) {
			e.printStackTrace();
			return null;
		}*/
    }

    private Integer getPeriodIdFromLongDateFormat(String longDate) {
        Long currentDateTime = Long.parseLong(longDate);
        Date currentDate = new Date(currentDateTime);
        return Integer.parseInt(periodFormatter.format(currentDate));
    }

    private String converToDateFormat(String time) {
        if (!time.equalsIgnoreCase("")) {
            Long currentDateTime = Long.parseLong(time);
            DateFormat simple = new SimpleDateFormat("dd-MM-yyyy");
            Date result = new Date(currentDateTime);
            return simple.format(result);
        } else {
            return "";
        }

    }

    private String getReportName(Integer reportId, Integer solutionId) {
        String reportBkey = validationDaoFactory.getValidationDao().getReportBkeyById(reportId, solutionId);
        return reportBkey;
    }

    @Override
    public JSONObject getValidationCodeAttributeList(String returnBkey, Date periodIdDate, Integer sectionId) {
        logger.info("EXEFLOW --> ValidationExecutionBoImpl --> getValidationCodeAttributeList()");
        JSONObject validationCodes = validationDaoFactory.getValidationDao().getValidationCode(returnBkey, periodIdDate, sectionId);
        return validationCodes;
    }

    @Override
    public JSONObject getErrorValidationCodeAttributeList(String returnBkey, Date periodIdDate, Integer orgId,
                                                          Integer versionNo, Integer sectionId, Integer periodId, Integer solutionId) {
        logger.info("EXEFLOW --> ValidationExecutionBoImpl --> getErrorValidationCodeAttributeList()");
        Integer regReportId = validationDaoFactory.getValidationDao().getReportId(returnBkey);
        JSONObject errorValidationCodes = validationDaoFactory.getValidationDao().getErrorValidationCode(returnBkey, periodIdDate, orgId, versionNo, sectionId, regReportId, periodId, solutionId);
        return errorValidationCodes;
    }

    @Override
    public JSONObject getValidationCodeOccurrences(String returnBkey, Date periodIdDate, Integer orgId, Integer versionNo,
                                                   String validationCode, Integer solutionId, Integer periodId) {
        logger.info("EXEFLOW --> ValidationExecutionBoImpl --> getErrorValidationCodeOccurrences()");
        Integer regReportId = validationDaoFactory.getValidationDao().getReportId(returnBkey);
        Integer validationId = validationDaoFactory.getValidationDao().getValidationIdByCode(validationCode, returnBkey);
        JSONObject allOccurrenceObj = new JSONObject();
        if (null != regReportId && null != validationId) {
            allOccurrenceObj = getValidationDetailsForAllOccurrenceApi(solutionId, periodIdDate, orgId, regReportId, null, versionNo, periodId, validationId);
            return allOccurrenceObj;
        } else {
            allOccurrenceObj.put("Warning", "Validation Id And Reg Report Id cannot be null");
            return allOccurrenceObj;
        }

    }

    @Override
    public List<ValidationRunDetails> getValidationRunDetails(Integer runId, String validationIdCsv) {
        logger.info("EXEFLOW --> ValidationExecutionBoImpl --> getValidationRunDetails()");
        return validationDaoFactory.getValidationDao().getValidationRunDetails(runId, validationIdCsv);
    }

    @Override
    public List<Object[]> getValidationRunDetailsForSpark(Integer runId, String validationIdCsv, boolean distinctRecord) {
        logger.info("EXEFLOW --> ValidationExecutionBoImpl --> getValidationRunDetailsForSpark()");
        return validationDaoFactory.getValidationDao().getValidationRunDetailsForSpark(runId, validationIdCsv, distinctRecord);
    }

    @Override
    public void processRunResultDownload(Integer runId, Integer validationId, String outputDirPath,
                                         Boolean isSparkEnabled) throws IOException {
        logger.info("EXEFLOW --> ValidationExecutionBoImpl --> processRunResultDownload()");

        String inputDirPath = ApplicationProperties.getValue("app.validations.outputDirectory").trim()
                + File.separator + runId;
        File inputDir = new File(inputDirPath);
        if (!inputDir.exists()) {
            throw new InvalidRequestException("Path doesn't exist!");
        }

        File outputDir = new File(outputDirPath);

        /* Compressing files at source directory */
        Compressor.compressFiles(inputDirPath);

        /* Copying files from source to temp location */
        if (isSparkEnabled != null && isSparkEnabled) {
            if (validationId != null) {
                List<Object[]> runSummary = getValidationRunDetailsForSpark(runId, validationId + "", false);
                if (runSummary == null || runSummary.isEmpty()) {
                    throw new DownloadFailureException("Failed to find record for run: " + runId + ", validationId: " + validationId);
                }

                String filePath = runSummary.get(0)[10].toString() + File.separator + runSummary.get(0)[11].toString() + ".gz";
                FileUtils.copyFileToDirectory(new File(inputDirPath + File.separator + filePath), outputDir);
            } else {
                //get into each group folder and copy each file from source to destination folder
                List<Object[]> runSummary = getValidationRunDetailsForSpark(runId, null, true);
                if (runSummary == null || runSummary.isEmpty()) {
                    throw new DownloadFailureException("Failed to find record for run: " + runId);
                }

                for (Object[] r : runSummary) {
                    String filePath = r[0].toString() + File.separator + r[1].toString() + ".gz";
                    FileUtils.copyFileToDirectory(new File(inputDirPath + File.separator + filePath), outputDir);
                }
            }
        } else {
            if (validationId != null) {
                String fileName = "Validation_Result_" + runId + "_" + validationId + ".csv.gz";
                FileUtils.copyFileToDirectory(new File(inputDirPath + File.separator + fileName), outputDir);
            } else {
                FileUtils.copyDirectory(inputDir, outputDir);
            }
        }
    }

    @Async
    @Override
    public void triggerRunResultDownload(Integer runId, Integer validationId, String statusKey) throws Throwable {
        logger.info("EXEFLOW --> ValidationExecutionBoImpl --> triggerRunResultDownload()");

        String outputDirPath = ApplicationProperties.getValue("app.validations.outputDirectory").trim()
                + File.separator + ApplicationProperties.getValue("app.validations.resultSummaryFolder").trim()
                + File.separator + runId + "_" + statusKey;

        try {
            processRunResultDownload(runId, validationId, outputDirPath, false);

            OutputStream outputStream = new FileOutputStream(outputDirPath + ".zip");
            Compressor.zipFolder(outputDirPath, outputStream);

            updateRunResultDownloadStatusInfo(statusKey, "Completed", false, null, runId);
        } catch (BaseValidationException e) {
            e.printStackTrace();
            updateRunResultDownloadStatusInfo(statusKey, "Failed", true, e.getMessage(), null);
        } catch (Throwable e) {
            e.printStackTrace();
            updateRunResultDownloadStatusInfo(statusKey, "Failed", true, "Failed to process run result download!", null);
        } finally {
            FileUtils.deleteQuietly(new File(outputDirPath));
        }
    }

    @Override
    public void updateRunResultDownloadStatusInfo(String statusKey, String status, Boolean hasError, String errorMsg,
                                                  Integer runId) throws Throwable {
        logger.info("EXEFLOW --> ValidationExecutionBoImpl --> updateRunResultDownloadStatusInfo()");

        Map<String, Object> statusInfo = (Map<String, Object>)
                CacheCoordinator.get(RedisKeys.CONFIGURED_RUN_RESULT_DOWNLOAD_STATUS_KEY.getKey(), statusKey);

        if (statusInfo == null) {
            statusInfo = new HashMap<>();
        }

        if (status != null) {
            statusInfo.put("status", status);
        }

        if (hasError != null) {
            statusInfo.put("hasError", hasError);
        }

        if (errorMsg != null) {
            statusInfo.put("errorMsg", errorMsg);
        }

        if (runId != null) {
            statusInfo.put("runId", runId);
        }

        CacheCoordinator.save(RedisKeys.CONFIGURED_RUN_RESULT_DOWNLOAD_STATUS_KEY.getKey(), statusKey, statusInfo);
    }

    @Override
    public void updateValidationCleanupRecords(List<ValidationCleanupRecord> validationCleanupRecords) {
        logger.info("EXEFLOW --> ValidationExecutionBoImpl --> updateValidationCleanupRecords()");
        validationDaoFactory.getValidationDao().updateValidationCleanupRecords(validationCleanupRecords);
    }

    @Override
    public List<ValidationCleanupRecord> getValidationCleanupRecords(String type, boolean isDeleted, Date createdDate,
                                                                     String dateFilterOperator, String path) {
        logger.info("EXEFLOW --> ValidationExecutionBoImpl --> getValidationCleanupRecords()");
        return validationDaoFactory.getValidationDao().getValidationCleanupRecords(type, isDeleted, createdDate,
                dateFilterOperator, path);
    }

    @Override
    public void saveValidationGroupCsvLinkage(List<ValidationGroupCsvLinkage> linkages) {
        logger.info("EXEFLOW --> ValidationExecutionBoImpl --> saveValidationGroupCsvLinkage()");
        validationDaoFactory.getValidationDao().saveValidationGroupCsvLinkage(linkages);
    }

    private JSONObject getValidationDetailsForAllOccurrenceApi(Integer solutionId, Date periodIdDate, Integer orgId,
                                                               Integer regReportId, Object object, Integer versionNo, Integer periodId, Integer validationId) {
        logger.info("EXEFLOW --> ValidationExecutionBoImpl --> getValidationDetailsForAllOccurrenceApi()");
        Integer runId = validationDaoFactory.getValidationDao().getMaxRunIdForValidation(solutionId, periodIdDate, orgId,
                regReportId, null, versionNo, periodId, validationId);
        ValidationRunDetails vrd = validationDaoFactory.getValidationDao().getValidationRunDetailsByRunId(runId, validationId);
        List<String> dCSV = new ArrayList<String>();
        if (vrd.getDimensionsCSV() != null && !vrd.getDimensionsCSV().equalsIgnoreCase("")) {
            dCSV.addAll(Arrays.asList(vrd.getDimensionsCSV().split(",")));

            for (String col : listFixedColumns) {
                if (dCSV.contains(col)) {
                    dCSV.remove(col);
                }
            }
        }
        return getDataForAllOccurrenceByValidationIdApi(runId, validationId, new HashMap<String, String>(), dCSV);

    }

    private JSONObject getDataForAllOccurrenceByValidationIdApi(Integer runId, Integer validationId,
                                                                HashMap<String, String> hashCommentMap, List<String> dCSV) {
        String outputDirectory = ApplicationProperties.getValue("app.validations.outputDirectory").trim();
        String fileName;
        JSONObject res = new JSONObject();
        JSONObject displayActualName = new JSONObject();
        JSONObject occ;
        JSONArray columnNames = new JSONArray();

        List<String> tempColumnNames = new ArrayList<String>();
        JSONArray columnData = new JSONArray();
        RecordList recordList;
        fileName = "Validation_Result_" + runId + "_" + validationId;//+ ".csv";

//		DataReader dataReader = getJdbcReader(outputDirectory + runId, fileName);
//				
//				
//				new CSVReader(new File(outputDirectory + runId + File.separator + fileName)).setFieldNamesInFirstRow(true);
        DataReader dataReader = getJdbcReader(outputDirectory + runId, fileName);
        MemoryWriter memoryWriter = new MemoryWriter();

        Job.run(dataReader, memoryWriter);
        recordList = memoryWriter.getRecordList();
        if (recordList.getRecordCount() > 0) {

			/*tempColumnNames.addAll(dCSV);
			tempColumnNames.add("validationResult");
			tempColumnNames.add("hashValue");
			tempColumnNames.add("comment");
			for (String c : tempColumnNames) {
				displayActualName.put(ValidationStringUtils.replace(c, " ", "____", -1, true), c);
				columnNames.add(ValidationStringUtils.replace(c, " ", "____", -1, true));
			}
			res.put("columnDisplayActualMap", displayActualName);
			res.put("columnNames", columnNames);*/
            for (int i = 0; i < recordList.getRecordCount(); i++) {
                if (!Boolean.parseBoolean(recordList.get(i).getField("Validation").getValueAsString())) {
                    occ = new JSONObject();
                    String occurence = "";
                    for (String dimName : dCSV) {
                        //occ.put(dimName, " ", "____", (recordList.get(i).getField(dimName).getValueAsString()==null?"":recordList.get(i).getField(dimName).getValueAsString()));
                        if (occurence.equals("")) {
                            occurence = dimName + ":" + (recordList.get(i).getField(dimName).getValueAsString() == null ? "" : recordList.get(i).getField(dimName).getValueAsString().toString());
                        } else {
                            occurence = occurence + "," + dimName + ":" + (recordList.get(i).getField(dimName).getValueAsString() == null ? "" : recordList.get(i).getField(dimName).getValueAsString().toString());
                        }
                    }
                    occ.put("occurence", occurence);
                    occ.put("validationResult", Boolean.parseBoolean(recordList.get(i).getField("Validation").getValueAsString()));
                    occ.put("hashValue", recordList.get(i).getField("Hash Key").getValueAsString());
                    occ.put("comment", hashCommentMap.get(validationId + ValidationConstants.HASH_DELIMITER + recordList.get(i).getField("Hash Key").getValueAsString()) == null ? "" :
                            hashCommentMap.get(validationId + ValidationConstants.HASH_DELIMITER + recordList.get(i).getField("Hash Key").getValueAsString()));
                    columnData.add(occ);
                }

            }
            res.put("columnData", columnData);
            res.put("validationId", validationId);
        }

        return res;
    }

    private List<ValidationMaster> fetchAllQualifiedValidations(Integer solutionId, Integer periodId,
                                                                String entityCode, String type, String groupNameCSV) {
        Date periodDate = null;
        try {
            periodDate = periodFormatter.parse(periodId.toString());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return validationDaoFactory.getValidationDao().fetchAllQualifiedValidations(solutionId, periodDate, entityCode,
                type, groupNameCSV);
    }
}
