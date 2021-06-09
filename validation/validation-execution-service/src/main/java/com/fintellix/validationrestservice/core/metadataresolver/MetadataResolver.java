package com.fintellix.validationrestservice.core.metadataresolver;

import com.fintellix.framework.validation.dto.ValidationRequest;
import com.fintellix.framework.validation.dto.ValidationRunDetails;
import com.fintellix.platformcore.common.hibernate.VyasaHibernateDaoSupport;
import com.fintellix.validationrestservice.definition.EntityMetadataInfo;
import com.fintellix.validationrestservice.definition.ExpressionEntityDetail;
import com.fintellix.validationrestservice.definition.RefEntityDetail;
import com.fintellix.validationrestservice.definition.RefMetadataInfo;
import com.fintellix.validationrestservice.definition.ReturnEntityDetail;
import com.fintellix.validationrestservice.definition.ReturnMetadataInfo;
import com.fintellix.validationrestservice.definition.ValidationConstants;
import com.fintellix.validationrestservice.definition.ValidationEntityDetail;
import com.fintellix.validationrestservice.util.ApplicationProperties;
import com.fintellix.validationrestservice.util.connectionManager.PersistentStoreManager;
import com.fintellix.validationrestservice.vo.QueryWithColumnDetail;
import com.fintellix.validationrestservice.vo.RequestResponse;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;

public class MetadataResolver extends VyasaHibernateDaoSupport implements Callable<MetadataResolver> {
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    // variables used in MetadataBuilder
    public String regReportName;
    public String refTableName;
    public String type;
    public String sectionDesc;
    public String tableName;
    public Integer reportId;
    public Integer sectionId;
    public Map<String, String> allColumnData = new HashMap<>();
    public Map<String, String> lineColumnData = new HashMap<>();
    public Map<String, String> lineColumnDataType = new HashMap<>();
    public Map<String, String> dimensionColumnData = new HashMap<>();
    public Map<String, Map<String, String>> aliaisedLineColumnData = new HashMap<>();
    public String formName = "";
    public String entityName = "";
    public Boolean isGrid = Boolean.FALSE;
    public String query = "";
    public String subjectArea;
    public String ddTableName;
    public String columnName;


    // system variables | to be populated with system variables coming from
    // expression parser
    private Integer systemRegReportId;
    private Integer systemPeriodId;
    private Integer systemSolutionId;
    private Integer systemRegReportVersion;
    private Integer systemVersionNo;
    private Integer systemOrgId;
    private Integer runId;
    private Boolean hasError = Boolean.FALSE;
    private Boolean dropTables = Boolean.FALSE;

    // local variables
    private String repType;
    private ExpressionEntityDetail eed;

    @Override
    public MetadataResolver call() throws Exception {
        if (dropTables) {
            dropTable();
        } else {
            main();
            if (formName == null) {
                formName = "";
            }
        }
        return this;
    }

    public void setSolutionId(Integer solId) {
        systemSolutionId = solId;
    }

    public void init(Integer systemPeriodId, Integer systemSolutionId, Integer systemVersionNo,
                     Integer systemRegReportVersion, Integer systemOrgId, ExpressionEntityDetail eed, Integer systemRegReportId,
                     Integer runId) {
        if (eed.getEntityType().equalsIgnoreCase(ValidationConstants.TYPE_RETURN)) {
            ReturnEntityDetail returnEntityDetail = (ReturnEntityDetail) eed;
            this.sectionDesc = returnEntityDetail.getSectionDesc();
            this.regReportName = eed.getEntityCode();
            this.systemVersionNo = systemVersionNo;
            this.systemRegReportVersion = systemRegReportVersion;
        } else if (eed.getEntityType().equalsIgnoreCase(ValidationConstants.TYPE_REFTABLE)) {
            this.refTableName = eed.getEntityCode();
        } else if (eed.getEntityType().equalsIgnoreCase(ValidationConstants.TYPE_ENTITY)) {
            this.refTableName = eed.getEntityCode();
            this.ddTableName = eed.getDdTableName();
            this.columnName = eed.getColumnName();
            this.subjectArea = eed.getSubjectArea();
        }

        if (eed == null || eed.getEntityType() == null) {
            this.type = eed.getEntityType();
        }

        this.systemPeriodId = systemPeriodId;
        this.systemSolutionId = systemSolutionId;
        this.type = eed.getEntityType();
        this.systemOrgId = systemOrgId;
        this.eed = eed;
        this.systemRegReportId = systemRegReportId;
        this.runId = runId;
    }


    private void main() {
        if (eed.getEntityType().equalsIgnoreCase(ValidationConstants.TYPE_RETURN)) {
            Map<Integer, String> lineIdToCode = new HashMap<>();
            Map<String, Integer> codeToLineId = new HashMap<>();
            Map<String, String> lineCols = new HashMap<>();

            Integer regReportId = ((ReturnEntityDetail) eed).getReportId();
            Integer sectionId = null;
            Map<String, String> cols = null;

            if (eed.getEntityElements().isEmpty()) {
                throw new IllegalArgumentException("Line Items not available");
            }

            reportId = regReportId;
            if (regReportId.equals(systemRegReportId)) {
                sectionId = getSectionId(regReportId, systemSolutionId, systemRegReportVersion);
                this.sectionId = sectionId;
                if (isGrid) {
                    cols = getDimensionColumn(regReportId, systemSolutionId, systemRegReportVersion, sectionId);

                    getLineItems(regReportId, systemSolutionId, systemRegReportVersion, sectionId, lineIdToCode,
                            codeToLineId, lineCols, new ArrayList<String>(eed.getEntityElements()));
                    if (lineIdToCode.isEmpty()) {
                        getLineItems(regReportId, systemSolutionId, systemRegReportVersion, sectionId, lineIdToCode,
                                codeToLineId, lineCols, getLineItemsCodes(regReportId, systemPeriodId, systemSolutionId,
                                        systemRegReportVersion, sectionId, lineIdToCode, codeToLineId, cols));
                    }
                }
            } else {
                ReturnEntityDetail returnEntityDetail = (ReturnEntityDetail) eed;

                if (returnEntityDetail.getMetaDataInfoMap() == null
                        || returnEntityDetail.getMetaDataInfoMap().isEmpty()) {
                    ReturnMetadataInfo metaDataInfo = new ReturnMetadataInfo();
                    metaDataInfo.setPeriodId(systemPeriodId);
                    metaDataInfo.setOrgId(systemOrgId);

                    Object[] result = this.getReportVersionAndVersionNo(metaDataInfo.getOrgId(), regReportId, null,
                            metaDataInfo.getPeriodId());

                    if (result != null) {
                        metaDataInfo.setVersionNo(Integer.parseInt(result[0].toString()));
                        metaDataInfo.setReportVersion(Integer.parseInt(result[1].toString()));
                    }

                    Map<String, ReturnMetadataInfo> metaDataInfoMap = new HashMap<>();
                    metaDataInfoMap.put("dummyKey", metaDataInfo);

                    returnEntityDetail.setMetaDataInfoMap(metaDataInfoMap);
                }

                ReturnMetadataInfo dataInfo = new ArrayList<>(((ReturnEntityDetail) eed).getMetaDataInfoMap().values())
                        .get(0);

                sectionId = getSectionId(regReportId, systemSolutionId, dataInfo.getReportVersion());
                this.sectionId = sectionId;
                if (isGrid) {
                    cols = getDimensionColumn(regReportId, systemSolutionId, dataInfo.getReportVersion(), sectionId);

                    getLineItems(regReportId, systemSolutionId, dataInfo.getReportVersion(), sectionId, lineIdToCode,
                            codeToLineId, lineCols, new ArrayList<String>(eed.getEntityElements()));

                    if (lineIdToCode.isEmpty()) {
                        getLineItems(regReportId, systemSolutionId, dataInfo.getReportVersion(), sectionId, lineIdToCode,
                                codeToLineId, lineCols, getLineItemsCodes(regReportId, dataInfo.getPeriodId(), systemSolutionId,
                                        dataInfo.getReportVersion(), sectionId, lineIdToCode, codeToLineId, cols));
                    }
                }
            }

            if (isGrid) {
                //GRID
                if (cols.isEmpty()) {
                    piviotTable(lineIdToCode, regReportId, systemPeriodId, systemSolutionId, systemOrgId,
                            systemVersionNo, sectionId);
                } else {
                    piviotGroupByTable(lineIdToCode, regReportId, systemPeriodId, systemSolutionId, systemOrgId, cols,
                            systemVersionNo, sectionId);
                }
            } else {

                //LIST
                List<Map<String, Object>> filters = new ArrayList<>();

                String regReportVersions = null;
                ReturnEntityDetail returnEntityDetail = (ReturnEntityDetail) eed;
                List<String> elements = new ArrayList<String>(eed.getEntityElements());
                List<String> listCols = new ArrayList<>();
                for (String ele : elements) {
                    listCols.add(ele.replaceAll("\"", ""));
                }
                if (returnEntityDetail.getMetaDataInfoMap() != null
                        && !returnEntityDetail.getMetaDataInfoMap().isEmpty()) {
                    for (Entry<String, ReturnMetadataInfo> entry : returnEntityDetail.getMetaDataInfoMap().entrySet()) {
                        ReturnMetadataInfo value = entry.getValue();

                        Map<String, Object> filter = new HashMap<>();
                        filter.put("periodId", value.getPeriodId());
                        filter.put("solutionId", systemSolutionId);
                        filter.put("versionNumber", value.getVersionNo());
                        filter.put("reportId", regReportId);
                        filter.put("orgEntityId", value.getOrgId());
                        filters.add(filter);
                        if (regReportVersions == null) {
                            regReportVersions = "";
                        } else {
                            regReportVersions = regReportVersions + ",";
                        }
                        regReportVersions = regReportVersions + value.getReportVersion();

                    }
                }
                if (regReportId.equals(systemRegReportId)) {
                    Map<String, Object> filter = new HashMap<>();
                    filter.put("periodId", systemPeriodId);
                    filter.put("solutionId", systemSolutionId);
                    filter.put("versionNumber", systemVersionNo);
                    filter.put("reportId", systemRegReportId);
                    filter.put("orgEntityId", systemOrgId);
                    filters.add(filter);
                    if (regReportVersions != null) {
                        regReportVersions = regReportVersions + " , ";
                    } else {
                        regReportVersions = "";
                    }

                    regReportVersions = regReportVersions + systemRegReportVersion;
                }

                Map<String, List<Integer>> metaInfo = getListReportMetaDataInfo(regReportId, systemSolutionId,
                        regReportVersions);
                tableName = "A_" + systemPeriodId + "_" + regReportId + "_" + sectionId + "_" + runId;

                for (String entityName : metaInfo.keySet()) {
                    RequestResponse requestResponse = getQuery(entityName, metaInfo.get(entityName), listCols, filters,
                            tableName);
                    if (requestResponse == null) {
                        LOGGER.error("Failed to fetch query for entity : " + entityName);
                    }

                    if (requestResponse.getHasError()) {
                        hasError = Boolean.TRUE;
                        LOGGER.error(requestResponse.getMessage() + "\n");
                    } else {
                        try {
                            QueryWithColumnDetail queryWithColumnDetail = requestResponse.getModel();
                            createListTable(queryWithColumnDetail.getQuery());
                            createIndexingForListTable(queryWithColumnDetail);
                            Map<String, String> listColsMap = queryWithColumnDetail.getColBNameColKeyMap();
                            Map<Integer, Map<String, String>> versionNumberPkMap = queryWithColumnDetail.getVersionNumberPkMap();

                            Map<String, String> colKeyColDescMap = queryWithColumnDetail.getColKeyColDescMap();

                            for (String col : queryWithColumnDetail.getColKeyColDataTypeMap().keySet()) {
                                if (queryWithColumnDetail.getColKeyColDataTypeMap().get(col).equalsIgnoreCase("DATE")) {
                                    lineColumnDataType.put(col, "DATE");
                                } else if (queryWithColumnDetail.getColKeyColDataTypeMap().get(col).equalsIgnoreCase("INTEGER") ||
                                        queryWithColumnDetail.getColKeyColDataTypeMap().get(col).equalsIgnoreCase("NUMBER")) {
                                    lineColumnDataType.put(col, "NUMBER");
                                } else if (queryWithColumnDetail.getColKeyColDataTypeMap().get(col).equalsIgnoreCase("VARCHAR")) {
                                    lineColumnDataType.put(col, "STRING");
                                }

                            }


                            for (String key : listColsMap.keySet()) {
                                allColumnData.put("\"" + key.toUpperCase() + "\"", listColsMap.get(key));
                                lineColumnData.put("\"" + key.toUpperCase() + "\"", listColsMap.get(key));

                                aliaisedLineColumnData.put(listColsMap.get(key), new HashMap<>());
                                aliaisedLineColumnData.get(listColsMap.get(key)).put("DATA_TYPE", lineColumnDataType.get(listColsMap.get(key)));
                                aliaisedLineColumnData.get(listColsMap.get(key)).put("BUSSINESS_NAME", key);
                                aliaisedLineColumnData.get(listColsMap.get(key)).put("DESC", colKeyColDescMap.get(listColsMap.get(key)));
                                //    aliaisedLineColumnData.get(listColsMap.get(key)).put(key, value)
                            }

                            versionNumberPkMap.forEach((k, v) -> {
                                v.forEach((key, value) -> {
                                    allColumnData.put("\"" + value.toUpperCase() + "\"", key);
                                    dimensionColumnData.put(key, "\"" + value.toUpperCase() + "\"");
                                });
                            });

                        } catch (Throwable e) {
                            hasError = Boolean.TRUE;
                            e.printStackTrace();
                        }
                    }
                }

                LOGGER.info("List section " + sectionDesc);
            }
        } else if (eed.getEntityType().equalsIgnoreCase(ValidationConstants.TYPE_REFTABLE)) {
            List<Map<String, Object>> filters = new ArrayList<>();

            RefEntityDetail refEntityDetail = (RefEntityDetail) eed;

            entityName = refEntityDetail.getEntityCode();

            List<String> elements = new ArrayList<String>(eed.getEntityElements());
            List<String> listCols = new ArrayList<>();
            for (String ele : elements) {
                listCols.add(ele.replaceAll("\"", ""));
            }
            if (refEntityDetail.getMetaDataInfoMap() != null && !refEntityDetail.getMetaDataInfoMap().isEmpty()) {
                for (Entry<String, RefMetadataInfo> entry : refEntityDetail.getMetaDataInfoMap().entrySet()) {
                    RefMetadataInfo value = entry.getValue();

                    Map<String, Object> filter = new HashMap<>();
                    filter.put("periodId", value.getPeriodId());
                    filter.put("orgId", value.getOrgId());
                    filters.add(filter);
                }
            }

            if (filters.isEmpty()) {
                Map<String, Object> filter = new HashMap<>();
                filter.put("periodId", systemPeriodId);
                filter.put("orgId", systemOrgId);
                filters.add(filter);
            }

            tableName = createTempTableName(refEntityDetail.getEntityCode().replace(" ", ""), runId);

            Map<String, Object> body = new HashMap<String, Object>();
            body.put("solutionId", systemSolutionId);
            body.put("columns", listCols);
            body.put("filters", filters);
            body.put("dbType", ApplicationProperties.getValue("app.martDBType").trim());
            body.put("tempTableName", tableName);
            body.put("entityBusinessName", refEntityDetail.getEntityCode());

            RequestResponse requestResponse = getRefQuery(body);
            if (requestResponse == null) {
                LOGGER.error("Failed to fetch query for entity : " + refEntityDetail.getEntityCode());
            }

            if (requestResponse.getHasError()) {
                hasError = Boolean.TRUE;
                LOGGER.error(requestResponse.getMessage() + "\n");
            } else {
                try {
                    QueryWithColumnDetail queryWithColumnDetail = requestResponse.getModel();
                    createListTable(queryWithColumnDetail.getQuery());
//					createIndexingForListTable(queryWithColumnDetail);
                    Map<String, String> listColsMap = queryWithColumnDetail.getColBNameColKeyMap();
                    Map<Integer, Map<String, String>> versionNumberPkMap = queryWithColumnDetail
                            .getVersionNumberPkMap();

                    Map<String, String> colKeyColDescMap = queryWithColumnDetail.getColKeyColDescMap();

                    for (String col : queryWithColumnDetail.getColKeyColDataTypeMap().keySet()) {
                        if (queryWithColumnDetail.getColKeyColDataTypeMap().get(col).equalsIgnoreCase("DATE")) {
                            lineColumnDataType.put(col, "DATE");
                        } else if (queryWithColumnDetail.getColKeyColDataTypeMap().get(col).equalsIgnoreCase("INTEGER")
                                || queryWithColumnDetail.getColKeyColDataTypeMap().get(col)
                                .equalsIgnoreCase("NUMBER")) {
                            lineColumnDataType.put(col, "NUMBER");
                        } else if (queryWithColumnDetail.getColKeyColDataTypeMap().get(col).equalsIgnoreCase("VARCHAR")
                                || queryWithColumnDetail.getColKeyColDataTypeMap().get(col).equalsIgnoreCase("VARCHAR2")
                                || queryWithColumnDetail.getColKeyColDataTypeMap().get(col).equalsIgnoreCase("NVARCHAR")
                                || queryWithColumnDetail.getColKeyColDataTypeMap().get(col).equalsIgnoreCase("NVARCHAR2")
                                || queryWithColumnDetail.getColKeyColDataTypeMap().get(col).equalsIgnoreCase("CHAR")) {
                            lineColumnDataType.put(col, "STRING");
                        }
                    }

                    for (String key : listColsMap.keySet()) {
                        allColumnData.put("\"" + key.toUpperCase() + "\"", listColsMap.get(key));
                        lineColumnData.put("\"" + key.toUpperCase() + "\"", listColsMap.get(key));

                        aliaisedLineColumnData.put(listColsMap.get(key), new HashMap<>());
                        aliaisedLineColumnData.get(listColsMap.get(key)).put("DATA_TYPE",
                                lineColumnDataType.get(listColsMap.get(key)));
                        aliaisedLineColumnData.get(listColsMap.get(key)).put("BUSSINESS_NAME", key);
                        aliaisedLineColumnData.get(listColsMap.get(key)).put("DESC",
                                colKeyColDescMap.get(listColsMap.get(key)));
                        // aliaisedLineColumnData.get(listColsMap.get(key)).put(key, value)
                    }

                    versionNumberPkMap.forEach((k, v) -> {
                        v.forEach((key, value) -> {
                            allColumnData.put("\"" + value.toUpperCase() + "\"", key);
                            dimensionColumnData.put(key, "\"" + value.toUpperCase() + "\"");
                        });
                    });
                    if (queryWithColumnDetail.getIsSCDII()) {
                        aliaisedLineColumnData.put(ValidationConstants.IS_SCDII, new HashMap<>());

                        aliaisedLineColumnData.get(ValidationConstants.IS_SCDII).put(
                                ValidationConstants.START_DATE_COLUMN, queryWithColumnDetail.getStartDateColumnName());
                        aliaisedLineColumnData.get(ValidationConstants.IS_SCDII)
                                .put(ValidationConstants.END_DATE_COLUMN, queryWithColumnDetail.getEndDateColumnName());
                    }
                    if (queryWithColumnDetail.getIsOrgIdColAvailable()) {

                        aliaisedLineColumnData.put(ValidationConstants.IS_ORG_FILTER_AVAILABLE, new HashMap<>());

                        aliaisedLineColumnData.get(ValidationConstants.IS_ORG_FILTER_AVAILABLE).put(
                                ValidationConstants.ORG_COLUMN, queryWithColumnDetail.getOrgIdColumnName());

                    }
                    //lineColumnDataType
                } catch (Throwable e) {
                    hasError = Boolean.TRUE;
                    e.printStackTrace();
                }
            }
            // }
        } else if (eed.getEntityType().equalsIgnoreCase(ValidationConstants.TYPE_ENTITY)) {
            List<Map<String, Object>> filters = new ArrayList<>();

            ValidationEntityDetail ddEntityDetail = (ValidationEntityDetail) eed;

            entityName = ddEntityDetail.getEntityCode();

            List<String> elements = new ArrayList<String>(eed.getEntityElements());
            List<String> listCols = new ArrayList<>();
            for (String ele : elements) {
                listCols.add(ele.replaceAll("\"", ""));
            }
            if (ddEntityDetail.getMetaDataInfoMap() != null && !ddEntityDetail.getMetaDataInfoMap().isEmpty()) {
                for (Entry<String, EntityMetadataInfo> entry : ddEntityDetail.getMetaDataInfoMap().entrySet()) {
                    EntityMetadataInfo value = entry.getValue();

                    Map<String, Object> filter = new HashMap<>();
                    filter.put("periodId", value.getPeriodId());
                    filter.put("orgId", value.getOrgId());
                    filters.add(filter);
                }
            }

            if (filters.isEmpty()) {
                Map<String, Object> filter = new HashMap<>();
                filter.put("periodId", systemPeriodId);
                filter.put("orgId", systemOrgId);
                filters.add(filter);
            }

            ddTableName = createTempTableName(ddEntityDetail.getEntityCode().replace(" ", ""), runId);

            Map<String, Object> body = new HashMap<String, Object>();
            body.put("solutionId", systemSolutionId);
            body.put("columns", listCols);
            body.put("filters", filters);
            body.put("dbType", ApplicationProperties.getValue("app.martDBType").trim());
            body.put("tempTableName", ddTableName);
            body.put("entityBusinessName", ddEntityDetail.getEntityCode());
            body.put("subjectArea", ddEntityDetail.getSubjectArea());

            RequestResponse requestResponse = getDdEntityQuery(body);
            if (requestResponse == null) {
                LOGGER.error("Failed to fetch query for entity : " + ddEntityDetail.getEntityCode());
            }

            if (requestResponse.getHasError()) {
                hasError = Boolean.TRUE;
                LOGGER.error(requestResponse.getMessage() + "\n");
            } else {
                try {
                    QueryWithColumnDetail queryWithColumnDetail = requestResponse.getModel();
                    createListTable(queryWithColumnDetail.getQuery());
//					createIndexingForListTable(queryWithColumnDetail);
                    Map<String, String> listColsMap = queryWithColumnDetail.getColBNameColKeyMap();
                    Map<Integer, Map<String, String>> versionNumberPkMap = queryWithColumnDetail
                            .getVersionNumberPkMap();

                    Map<String, String> colKeyColDescMap = queryWithColumnDetail.getColKeyColDescMap();

                    for (String col : queryWithColumnDetail.getColKeyColDataTypeMap().keySet()) {
                        if (queryWithColumnDetail.getColKeyColDataTypeMap().get(col).equalsIgnoreCase("DATE")) {
                            lineColumnDataType.put(col, "DATE");
                        } else if (queryWithColumnDetail.getColKeyColDataTypeMap().get(col).equalsIgnoreCase("INTEGER")
                                || queryWithColumnDetail.getColKeyColDataTypeMap().get(col)
                                .equalsIgnoreCase("NUMBER")) {
                            lineColumnDataType.put(col, "NUMBER");
                        } else if (queryWithColumnDetail.getColKeyColDataTypeMap().get(col).equalsIgnoreCase("VARCHAR")
                                || queryWithColumnDetail.getColKeyColDataTypeMap().get(col).equalsIgnoreCase("VARCHAR2")
                                || queryWithColumnDetail.getColKeyColDataTypeMap().get(col).equalsIgnoreCase("NVARCHAR")
                                || queryWithColumnDetail.getColKeyColDataTypeMap().get(col).equalsIgnoreCase("NVARCHAR2")
                                || queryWithColumnDetail.getColKeyColDataTypeMap().get(col).equalsIgnoreCase("CHAR")) {
                            lineColumnDataType.put(col, "STRING");
                        }
                    }

                    for (String key : listColsMap.keySet()) {
                        allColumnData.put("\"" + key.toUpperCase() + "\"", listColsMap.get(key));
                        lineColumnData.put("\"" + key.toUpperCase() + "\"", listColsMap.get(key));

                        aliaisedLineColumnData.put(listColsMap.get(key), new HashMap<>());
                        aliaisedLineColumnData.get(listColsMap.get(key)).put("DATA_TYPE",
                                lineColumnDataType.get(listColsMap.get(key)));
                        aliaisedLineColumnData.get(listColsMap.get(key)).put("BUSSINESS_NAME", key);
                        aliaisedLineColumnData.get(listColsMap.get(key)).put("DESC",
                                colKeyColDescMap.get(listColsMap.get(key)));
                        // aliaisedLineColumnData.get(listColsMap.get(key)).put(key, value)
                    }

                    versionNumberPkMap.forEach((k, v) -> {
                        v.forEach((key, value) -> {
                            allColumnData.put("\"" + value.toUpperCase() + "\"", key);
                            dimensionColumnData.put(key, "\"" + value.toUpperCase() + "\"");
                        });
                    });
                    if (queryWithColumnDetail.getIsSCDII()) {
                        aliaisedLineColumnData.put(ValidationConstants.IS_SCDII, new HashMap<>());

                        aliaisedLineColumnData.get(ValidationConstants.IS_SCDII).put(
                                ValidationConstants.START_DATE_COLUMN, queryWithColumnDetail.getStartDateColumnName());
                        aliaisedLineColumnData.get(ValidationConstants.IS_SCDII)
                                .put(ValidationConstants.END_DATE_COLUMN, queryWithColumnDetail.getEndDateColumnName());
                    }
                    if (queryWithColumnDetail.getIsOrgIdColAvailable()) {

                        aliaisedLineColumnData.put(ValidationConstants.IS_ORG_FILTER_AVAILABLE, new HashMap<>());

                        aliaisedLineColumnData.get(ValidationConstants.IS_ORG_FILTER_AVAILABLE).put(
                                ValidationConstants.ORG_COLUMN, queryWithColumnDetail.getOrgIdColumnName());

                    }
                    //lineColumnDataType
                } catch (Throwable e) {
                    hasError = Boolean.TRUE;
                    e.printStackTrace();
                }
            }
            // }
        }
    }

    private String createTempTableName(String entityName, Integer runId) {
        String tableName;
        if (entityName.length() > 21) {
            tableName = RandomStringUtils.random(20, entityName);
        } else {
            tableName = entityName;
        }

        return "A_" + tableName + "_" + runId;
    }

    private void createIndexingForListTable(QueryWithColumnDetail queryWithColumnDetail) throws Throwable {

        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getMartConnection();
            List<String> systemKeys = new ArrayList<>();
            systemKeys.add("PERIOD_ID");
            systemKeys.add("REG_REPORT_ID");
            systemKeys.add("ORG_ENTITY_ID");
            systemKeys.add("SOLUTION_ID");
            systemKeys.add("VERSION_NUMBER");

            Set<String> uniquePkColumns = new HashSet<>();
            for (Integer version : queryWithColumnDetail.getVersionNumberPkMap().keySet()) {
                for (String key : queryWithColumnDetail.getVersionNumberPkMap().get(version).keySet()) {
                    String column = queryWithColumnDetail.getVersionNumberPkMap().get(version).get(key);
                    if (!systemKeys.contains(column.trim().toUpperCase())) {
                        uniquePkColumns.add(column.trim().toUpperCase());
                    }

                }
            }

            if (!uniquePkColumns.isEmpty()) {
                String indexQuery = null;

                for (String pk : uniquePkColumns) {
                    if (indexQuery == null) {
                        indexQuery = "";
                    } else {
                        indexQuery += ",";
                    }
                    indexQuery += pk;

                }
                indexQuery = " CREATE INDEX " + tableName.trim() + "_" + "IDX on " + tableName.trim() + " ("
                        + indexQuery + ")";
                LOGGER.info("\nTable index creation query :\n" + indexQuery);
                ps = conn.prepareStatement(indexQuery);
                ps.executeUpdate();
                conn.commit();
            }

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("Error Occured while creating index");
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private void piviotGroupByTable(Map<Integer, String> lineIdToCode, Integer regReportId, Integer periodId,
                                    Integer solutionId, Integer orgId, Map<String, String> cols, Integer versionNo, Integer sectionId) {

        String selectClause = "";
        String createStatement = "";

        int counter = 0;

        for (Integer li : lineIdToCode.keySet()) {
            if (counter > 0) {
                selectClause = selectClause + ",";
                createStatement = createStatement + ",";
            }

            if (lineColumnDataType.get("A_" + li).equalsIgnoreCase("DATE")) {
                createStatement = createStatement + "A_" + li + "  date";
                selectClause = selectClause + "max(CASE WHEN LINE_ITEM_ID =" + li + " THEN CLIV_DATE ELSE NULL END )"
                        + " as a_" + li;

                // max(CASE WHEN LINE_ITEM_ID = 12 THEN cliV ELSE NULL END )
                // col12

            } else if (lineColumnDataType.get("A_" + li).equalsIgnoreCase("STRING")) {
                createStatement = createStatement + "A_" + li + "  varchar(1000)";
                selectClause = selectClause + "max(CASE WHEN LINE_ITEM_ID =" + li + " THEN CLIV_STRING ELSE NULL END )"
                        + " as a_" + li;
            } else if (lineColumnDataType.get("A_" + li).equalsIgnoreCase("NUMBER")) {
                createStatement = createStatement + "A_" + li + "  decimal(23, 5)";
                selectClause = selectClause + "max(CASE WHEN LINE_ITEM_ID =" + li
                        + " THEN Actual_Line_Item_Value ELSE NULL END )" + " as a_" + li;
            }

            counter++;
        }
        String groupByColumns = "";
        String innerSelect = "";
        Integer groupCounter = 0;
        String pivitGroup = " GROUP BY ";
        for (String dimensionCol : cols.keySet()) {
            if (groupCounter > 0) {

                groupByColumns = groupByColumns + ",";
                pivitGroup = pivitGroup + ",";
            }
            if (counter > 0) {
                selectClause = selectClause + ",";
                createStatement = createStatement + ",";
            }
            innerSelect = innerSelect + ",\"" + dimensionCol + "\" as " + dimensionCol.replaceAll(" ", "");
            selectClause = selectClause + dimensionCol.replaceAll(" ", "");
            createStatement = createStatement + dimensionCol.replaceAll(" ", "") + "  nvarchar(3000)";
            groupByColumns = groupByColumns + "[" + dimensionCol + "]";
            pivitGroup = pivitGroup + dimensionCol.replaceAll(" ", "");
            counter++;
            groupCounter++;
        }
        createStatement = createStatement
                + ",PERIOD_ID bigint ,VERSION_NUMBER bigint ,ORG_ENTITY_ID bigint,REG_REPORT_VERSION bigint ,Line_Item_Map_ID bigint,GROUP_BY_DIMENSION nvarchar(3000)";
        selectClause = selectClause + ",PERIOD_ID,VERSION_NUMBER,ORG_ENTITY_ID,REG_REPORT_VERSION";
        createStatement = "CREATE TABLE " + "A_" + periodId + "_" + regReportId + "_" + sectionId + "_" + runId + " ("
                + createStatement + ")";

        String whereClasue = "";
        String orCondition = null;
        ReturnEntityDetail returnEntityDetail = (ReturnEntityDetail) eed;

        if (returnEntityDetail.getMetaDataInfoMap() != null && !returnEntityDetail.getMetaDataInfoMap().isEmpty()) {
            for (Entry<String, ReturnMetadataInfo> entry : returnEntityDetail.getMetaDataInfoMap().entrySet()) {
                ReturnMetadataInfo value = entry.getValue();

                if (orCondition != null) {
                    orCondition = orCondition + " OR ";
                } else {
                    orCondition = "";
                }

                orCondition = orCondition + " (" + " flib.Period_ID =   " + value.getPeriodId()
                        + " and flib.Reg_Report_Id =   " + regReportId + "   and flib.ORG_ENTITY_ID=   "
                        + value.getOrgId() + " and flib.VERSION_NUMBER=   " + value.getVersionNo()
                        + " AND flib.REG_REPORT_VERSION=" + value.getReportVersion() + ") ";
            }
        }

        if (regReportId.equals(systemRegReportId)) {
            whereClasue = " flib.Period_ID =   " + periodId + " and flib.Reg_Report_Id =   " + regReportId
                    + "   and flib.ORG_ENTITY_ID=   " + orgId + " and flib.VERSION_NUMBER=   " + versionNo
                    + " AND flib.REG_REPORT_VERSION=" + systemRegReportVersion;
            if (orCondition != null) {
                whereClasue = " AND ( (" + whereClasue + ") OR " + orCondition + ")";
            } else {
                whereClasue = " AND " + whereClasue;
            }
        } else {
            whereClasue = " AND (" + orCondition + ")";
        }

        String query = "select " + selectClause + ",Line_Item_Map_ID,GROUP_BY_DIMENSION"
                + " from ( select Line_Item_ID,CLIV_DATE,CLIV_STRING,Actual_Line_Item_Value" + innerSelect
                + ",PERIOD_ID,VERSION_NUMBER,ORG_ENTITY_ID,REG_REPORT_VERSION,Line_Item_Map_ID,GROUP_BY_DIMENSION "
                + " from ( select   flidm.Line_Item_ID"
                // + ",flidm.Line_Item_Map_ID"
                + ",flidm.Dimension_Value,flidm.Dimension_Column,flib.GROUP_BY_DIMENSION" + ",  flib.CLIV_DATE " + ",  flib.CLIV_STRING "
                + ",  flib.Actual_Line_Item_Value ,flib.PERIOD_ID,flib.VERSION_NUMBER,flib.ORG_ENTITY_ID,flib.REG_REPORT_VERSION,flib.Line_Item_Map_ID "
                // + ", flib.Actual_Line_Item_Value,
                // flib.Org_Considered_Line_Item_Value,
                // flib.Org_Actual_Line_Item_Value "
                + "from FCT_Group_Line_Item_Balance flib inner join  FCT_Line_Item_Dimension_Map flidm on    flib.Reg_Report_ID = flidm.Reg_Report_ID   and flib.REG_REPORT_VERSION=flidm.REG_REPORT_VERSION   and flib.Period_ID=flidm.Period_ID     and flib.ORG_ENTITY_ID=flidm.ORG_ENTITY_ID  and flidm.Line_Item_ID=flib.Line_Item_ID  and flib.Line_Item_Map_ID =flidm.Line_Item_Map_ID  and flib.Solution_ID=flidm.Solution_ID    and flib.VERSION_NUMBER=flidm.VERSION_NUMBER  "
                + "WHERE   flib.Solution_ID =   " + solutionId + " " + whereClasue;
        if (!lineIdToCode.keySet().isEmpty()) {
            query = query + " and flib.Line_Item_ID in ("
                    + lineIdToCode.keySet().toString().replace("[", "").replace("]", "") + ") ";
        }
        query = query + ") as t1  " + "pivot( max(Dimension_value) for dimension_column in (" + groupByColumns
                + ")) as cli ) as t1 " + "" + pivitGroup + ",PERIOD_ID,VERSION_NUMBER,ORG_ENTITY_ID,REG_REPORT_VERSION,Line_Item_Map_ID,GROUP_BY_DIMENSION";

        try {
            createTable("A_" + periodId + "_" + regReportId + "_" + sectionId + "_" + runId, createStatement, query);
        } catch (Exception e) {
            LOGGER.error("ERROR ::" + query);
            e.printStackTrace();
        }
    }

    private void piviotTable(Map<Integer, String> lineIdToCode, Integer regReportId, Integer periodId,
                             Integer solutionId, Integer orgId, Integer versionNo, Integer sectionId) {
        int counter = 0;
        String lisPiviot = "";
        String selectClause = "";
        String createStatement = "";

        for (Integer li : lineIdToCode.keySet()) {
            if (counter > 0) {
                lisPiviot = lisPiviot + ",";
                selectClause = selectClause + ",";
                createStatement = createStatement + ",";
            }
            lisPiviot = lisPiviot + "[" + li + "]";

            if (lineColumnDataType.get("A_" + li).equalsIgnoreCase("DATE")) {
                createStatement = createStatement + "A_" + li + "  date";
                selectClause = selectClause + "max(CASE WHEN LINE_ITEM_ID =" + li + " THEN CLIV_DATE ELSE NULL END )"
                        + " as a_" + li;

                // max(CASE WHEN LINE_ITEM_ID = 12 THEN cliV ELSE NULL END )
                // col12

            } else if (lineColumnDataType.get("A_" + li).equalsIgnoreCase("STRING")) {
                createStatement = createStatement + "A_" + li + "  varchar(1000)";
                selectClause = selectClause + "max(CASE WHEN LINE_ITEM_ID =" + li + " THEN CLIV_STRING ELSE NULL END )"
                        + " as a_" + li;
            } else if (lineColumnDataType.get("A_" + li).equalsIgnoreCase("NUMBER")) {
                createStatement = createStatement + "A_" + li + "  decimal(23, 5)";
                selectClause = selectClause + "max(CASE WHEN LINE_ITEM_ID =" + li
                        + " THEN Actual_Line_Item_Value ELSE NULL END )" + " as a_" + li;
            }

            counter++;
        }

        createStatement = createStatement
                + ",PERIOD_ID bigint ,VERSION_NUMBER bigint ,ORG_ENTITY_ID bigint,REG_REPORT_VERSION bigint,GROUP_BY_DIMENSION nvarchar(3000),Line_Item_Map_ID bigint";
        selectClause = selectClause + ",PERIOD_ID,VERSION_NUMBER,ORG_ENTITY_ID,REG_REPORT_VERSION";
        if (selectClause.trim().equalsIgnoreCase("")) {
            selectClause = selectClause.substring(1);
            createStatement = createStatement.substring(1);
        }
        createStatement = "CREATE TABLE " + "A_" + periodId + "_" + regReportId + "_" + sectionId + "_" + runId + " ("
                + createStatement + ")";

        String whereClasue = "";
        String orCondition = null;
        ReturnEntityDetail returnEntityDetail = (ReturnEntityDetail) eed;

        if (returnEntityDetail.getMetaDataInfoMap() != null && !returnEntityDetail.getMetaDataInfoMap().isEmpty()) {
            for (Entry<String, ReturnMetadataInfo> entry : returnEntityDetail.getMetaDataInfoMap().entrySet()) {
                ReturnMetadataInfo value = entry.getValue();

                if (orCondition != null) {
                    orCondition = orCondition + " OR ";
                } else {
                    orCondition = "";
                }
                orCondition = orCondition + " (" + " Period_ID =   " + value.getPeriodId() + " and Reg_Report_Id =   "
                        + regReportId + "   and ORG_ENTITY_ID=   " + value.getOrgId() + " and VERSION_NUMBER=   "
                        + value.getVersionNo() + " AND REG_REPORT_VERSION =" + value.getReportVersion() + ") ";
            }
        }

        if (regReportId.equals(systemRegReportId)) {
            whereClasue = " Period_ID =   " + periodId + " and Reg_Report_Id =   " + regReportId
                    + "   and ORG_ENTITY_ID=   " + orgId + " and VERSION_NUMBER=   " + versionNo
                    + " AND REG_REPORT_VERSION =" + systemRegReportVersion;
            if (orCondition != null) {
                whereClasue = " AND ( (" + whereClasue + ") OR " + orCondition + ")";
            } else {
                whereClasue = " AND " + whereClasue;
            }
        } else {
            whereClasue = " AND (" + orCondition + ")";
        }
        String query = "";
        query = "select " + selectClause + " ,GROUP_BY_DIMENSION,Line_Item_Map_ID "
                + " from  ( select    Line_Item_ID,CLIV_DATE,CLIV_STRING,Actual_Line_Item_Value,PERIOD_ID,VERSION_NUMBER,ORG_ENTITY_ID,REG_REPORT_VERSION,Line_Item_Map_ID,GROUP_BY_DIMENSION"

                + " from FCT_Group_Line_Item_Balance " + "WHERE" + " Solution_ID =   " + solutionId + whereClasue;
        if (!lineIdToCode.keySet().isEmpty()) {
            query = query + " and Line_Item_ID in ("
                    + lineIdToCode.keySet().toString().replace("[", "").replace("]", "") + ")";
        }
        query = query + " ) as t1  group by PERIOD_ID,VERSION_NUMBER,ORG_ENTITY_ID,REG_REPORT_VERSION,Line_Item_Map_ID,GROUP_BY_DIMENSION";

        try {
            createTable("A_" + periodId + "_" + regReportId + "_" + sectionId + "_" + runId, createStatement, query);
        } catch (Exception e) {
            LOGGER.error("ERROR:::" + query);
            e.printStackTrace();
        }
    }

    private void createTable(String tableName, String createStatement, String selectQuery) throws Exception {
        query = selectQuery;
        LOGGER.info("\nTable creation query :\n" + createStatement);
        LOGGER.info("\nMain data select query :\n" + selectQuery);
        Connection conn = null;
        this.tableName = tableName;
        PreparedStatement ps = null;
        try {
            try {
                conn = getMartConnection();
                ps = conn.prepareStatement("DROP TABLE " + tableName);
                ps.executeUpdate();
                conn.commit();
            } catch (Exception e) {
                // do-nothing
            }
            ps.close();
            ps = conn.prepareStatement(createStatement.toUpperCase());
            ps.executeUpdate();
            conn.commit();

            ps.close();
            ps = conn.prepareStatement("INSERT INTO " + tableName + " " + selectQuery.toUpperCase());
            ps.executeUpdate();
            conn.commit();

        } catch (Exception e) {
            throw e;
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private List<String> getLineItemsCodes(Integer regReportId, Integer periodId, Integer solutionId,
                                           Integer regReportVersion, Integer sectionId, Map<Integer, String> lineIdToCode,
                                           Map<String, Integer> codeToLineId, Map<String, String> cols) {
        List<String> lineCodes = new ArrayList<>();
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(
                    "select line_item_id,LINE_ITEM_CODE,LINE_ITEM_DATA_TYPE from DIM_REG_REPORT_LINE_ITEM where "
                            + "REG_REPORT_SECTION_ID = ? and SOLUTION_ID = ? and REG_REPORT_ID = ? and REG_REPORT_VERSION = ? and LINE_ITEM_CODE is not NULL");
            ps.setInt(1, sectionId);
            ps.setInt(2, solutionId);
            ps.setInt(3, regReportId);
            ps.setInt(4, regReportVersion);
            rs = ps.executeQuery();
            while (rs.next()) {
                lineCodes.add(rs.getString(2));
                // dimensionCols.put(rs.getString(1),
                // rs.getString(1).replaceAll(" ", "").toUpperCase());
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return lineCodes;
    }

    private void getLineItems(Integer regReportId, Integer solutionId, Integer regReportVersion, Integer sectionId,
                              Map<Integer, String> lineIdToCode, Map<String, Integer> codeToLineId, Map<String, String> cols,
                              List<String> lineCodes) {

        if (lineCodes.isEmpty()) {
            return;
        }

        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement ps = null;

        try {
            String selectQuery = "";
            String lineCodeQuery = "";
            int counter = 0;

            for (String lineCode : lineCodes) {
                if (counter > 0) {
                    lineCodeQuery = lineCodeQuery + ",";
                }
                lineCodeQuery = lineCodeQuery + "'" + lineCode.trim().replaceAll("\"", "") + "'";
                counter++;
            }

            selectQuery = "select line_item_id,LINE_ITEM_CODE,LINE_ITEM_DATA_TYPE,LINE_ITEM_DESC from DIM_REG_REPORT_LINE_ITEM where " +
                    "REG_REPORT_SECTION_ID = ? and SOLUTION_ID = ? and REG_REPORT_ID = ? and REG_REPORT_VERSION = ? " +
                    "and LINE_ITEM_CODE is not NULL and LTRIM(RTRIM(LINE_ITEM_CODE)) IN (" + lineCodeQuery + ")";
            conn = getConnection();
            ps = conn.prepareStatement(selectQuery);

            ps.setInt(1, sectionId);
            ps.setInt(2, solutionId);
            ps.setInt(3, regReportId);
            ps.setInt(4, regReportVersion);

            rs = ps.executeQuery();

            while (rs.next()) {
                String col = rs.getString(2);
                String liCol = "A_" + rs.getInt(1);

                aliaisedLineColumnData.put(liCol, new HashMap<>());
                aliaisedLineColumnData.get(liCol).put("DATA_TYPE", rs.getString(3).toUpperCase());
                aliaisedLineColumnData.get(liCol).put("BUSSINESS_NAME", col);
                aliaisedLineColumnData.get(liCol).put("DESC", rs.getString(4));

                allColumnData.put("\"" + col.toUpperCase() + "\"", liCol);
                lineColumnData.put("\"" + col.toUpperCase() + "\"", liCol);

                lineColumnDataType.put(liCol, rs.getString(3).toUpperCase());

                lineIdToCode.put(rs.getInt(1), rs.getString(2));
                codeToLineId.put(rs.getString(2), rs.getInt(1));
                cols.put(rs.getString(2), rs.getString(2).replaceAll(" ", "").toUpperCase());
                // dimensionCols.put(rs.getString(1),
                // rs.getString(1).replaceAll(" ", "").toUpperCase());
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Map<String, String> getDimensionColumn(Integer regReportId, Integer solutionId, Integer regReportVersion,
                                                   Integer sectionId) {
        Map<String, String> dimensionCols = new HashMap<>();
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(
                    "select DIMENSION_COLUMN from DIM_REG_REPORT_DIMENSION_MAP where " + "SOLUTION_ID = ? "
                            + " and REG_REPORT_ID = ? and REG_REPORT_VERSION = ? and REG_REPORT_SECTION_ID = ?");
            ps.setInt(1, solutionId);
            ps.setInt(2, regReportId);
            ps.setInt(3, regReportVersion);
            ps.setInt(4, sectionId);
            rs = ps.executeQuery();
            while (rs.next()) {
                dimensionCols.put(rs.getString(1), rs.getString(1).replaceAll(" ", "").toUpperCase());
                String col = rs.getString(1);
                String liCol = rs.getString(1).replaceAll(" ", "").toUpperCase();

                allColumnData.put("\"" + col.toUpperCase() + "\"", liCol);
                dimensionColumnData.put(liCol, "\"" + col.toUpperCase() + "\"");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return dimensionCols;
    }

    public Integer getRegReportId(String regReportName, Integer periodId, Integer solutionId) {
        Integer regReportId = null;
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            String query = "select REG_REPORT_ID from DIM_REG_REPORT where REPORT_BKEY = ? and is_ACTIVE = 'Y' ";

            if (periodId != null) {
                query = query + " and ? between START_DATE_PERIOD_ID and END_DATE_PERIOD_ID ";
            }
            if (solutionId != null) {
                query = query + " and SOLUTION_ID = ? ";
            }

            ps = conn.prepareStatement(query);

            ps.setString(1, regReportName);

            if (periodId != null) {
                ps.setInt(2, periodId);
            }
            if (solutionId != null && periodId != null) {
                ps.setInt(3, solutionId);
            } else if (solutionId != null && periodId == null) {
                ps.setInt(2, solutionId);
            }

            rs = ps.executeQuery();
            if (rs.next()) {
                regReportId = rs.getInt(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return regReportId;
    }

    private Integer getSectionId(Integer regReportId, Integer solutionId, Integer regReportVersion) {
        Integer sectionId = null;
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement ps = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement("select REG_REPORT_SECTION_ID,SECTION_TYPE,FORM_NAME from DIM_REG_REPORT_SECTION"
                    + " where REG_REPORT_ID=? and SOLUTION_ID=? and REG_REPORT_VERSION=? "
                    + "and REG_REPORT_SECTION_DESC=?");
            ps.setInt(1, regReportId);
            ps.setInt(2, solutionId);
            ps.setInt(3, regReportVersion);

            if (sectionDesc.startsWith("\"") && sectionDesc.endsWith("\"")) {
                ps.setString(4, sectionDesc.replaceAll("\"", ""));
            } else {
                ps.setString(4, sectionDesc);
            }

            rs = ps.executeQuery();

            if (rs.next()) {
                sectionId = rs.getInt(1);
                repType = rs.getString(2).trim();
                if (rs.getString(2).trim().equalsIgnoreCase("GRID")) {
                    isGrid = Boolean.TRUE;
                } else {
                    isGrid = Boolean.FALSE;
                }
                formName = rs.getString(3);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return sectionId;
    }

    private Map<String, List<Integer>> getListReportMetaDataInfo(Integer regReportId, Integer solutionId,
                                                                 String regReportVersions) {
        Map<String, List<Integer>> listMetaData = new HashMap<>();
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement ps = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement("select ENTITY_NAME,ENTITY_VERSION,FORM_NAME from DIM_REG_REPORT_SECTION"
                    + " where REG_REPORT_ID=? and SOLUTION_ID=? and REG_REPORT_VERSION in(" + regReportVersions + ") "
                    + "and REG_REPORT_SECTION_DESC=?");
            ps.setInt(1, regReportId);
            ps.setInt(2, solutionId);

            if (sectionDesc.startsWith("\"") && sectionDesc.endsWith("\"")) {
                ps.setString(3, sectionDesc.replaceAll("\"", ""));
            } else {
                ps.setString(3, sectionDesc);
            }

            rs = ps.executeQuery();

            if (rs.next()) {
                if (listMetaData.get(rs.getString(1)) == null) {
                    listMetaData.put(rs.getString(1), new ArrayList<Integer>());
                }
                listMetaData.get(rs.getString(1)).add(rs.getInt(2));
                entityName = rs.getString(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return listMetaData;
    }

    public Integer getOrgId(String orgCode, Integer solId) {
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        Integer orgId = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(
                    "select ORG_ID from ORGANISATION_UNIT " + " where SOLUTION_ID = ? and ORG_CODE = ?");
            ps.setInt(1, solId);
            ps.setString(2, orgCode);

            rs = ps.executeQuery();

            while (rs.next()) {
                orgId = rs.getInt(1);
            }
        } catch (Exception e) {
            //LOGGER.error(e.getMessage(), e);

            // FIXME remove this code piece from here while porting to 4.x environment.
            //  this code shouldn't be present in 4.x and above environment.
            /* setting dummy value for orgId for 3.x env.*/
            orgId = Integer.parseInt(ApplicationProperties.getValue("app.refData.dummyOrgIdValue"));
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        return orgId;
    }

    public Object[] getReportVersionAndVersionNo(Integer orgId, Integer regReportId, String returnStatus,
                                                 Integer periodId) {

        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        Object[] result = null;

        try {
            conn = getConnection();
            String query = " select max(VERSION_NUMBER) as VERSION_NUMBER, REG_REPORT_VERSION "
                    + " from REG_REPORT_VERSION where ENTITY_ID=? AND REG_REPORT_ID=? and YEAR_OF_ACCOUNT=? and solution_id=? ";

            if (returnStatus != null && !returnStatus.trim().equals("")) {
                query = query + " and STATUS=? ";
            }
            query = query + " and REG_REPORT_VERSION = "
                    + " (select max(REG_REPORT_VERSION) from REG_REPORT_VERSION where REG_REPORT_ID=? "
                    + " and YEAR_OF_ACCOUNT=? and ENTITY_ID=? and solution_id=?";

            if (returnStatus != null && !returnStatus.trim().equals("")) {
                query = query + " and STATUS=? ";
            }
            query = query + " ) " + " group by REG_REPORT_VERSION, YEAR_OF_ACCOUNT, REG_REPORT_ID, STATUS ";

            ps = conn.prepareStatement(query);

            int i = 1;
            ps.setInt(i++, orgId);
            ps.setInt(i++, regReportId);
            ps.setInt(i++, periodId);
            ps.setInt(i++, systemSolutionId);

            if (returnStatus != null && !returnStatus.trim().equals("")) {
                ps.setString(i++, returnStatus);
            }
            ps.setInt(i++, regReportId);
            ps.setInt(i++, periodId);
            ps.setInt(i++, orgId);
            ps.setInt(i++, systemSolutionId);

            if (returnStatus != null && !returnStatus.trim().equals("")) {
                ps.setString(i++, returnStatus);
            }
            rs = ps.executeQuery();

            if (rs.next()) {
                result = new Object[rs.getMetaData().getColumnCount()];
                result[0] = rs.getInt(1);
                result[1] = rs.getInt(2);
            } else {
                LOGGER.error("----");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        return result;
    }

    public void updateRequestStatus(ValidationRequest vr, String status) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = PersistentStoreManager.getConnection();
            ps = conn.prepareStatement(
                    "update VALIDATION_REQUEST SET REQUEST_STATUS = ? , REQUEST_END_DATE_TIME = ? where RUN_ID = ? ");
            ps.setString(1, status);
            ps.setTimestamp(2, new java.sql.Timestamp((new Date()).getTime()));
            ps.setInt(3, vr.getRunId());
            ps.executeUpdate();
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Map<String, String> getAuthenticationToken() {
        Map<String, String> respMap = new HashMap<>();
        String url = ApplicationProperties.getValue("app.oauth.endpoint").trim() + ValidationConstants.URL_SEPARATOR
                + "oauth/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();
        body.add("username", ApplicationProperties.getValue("app.listAPI.username").trim());
        body.add("password", ApplicationProperties.getValue("app.listAPI.password").trim());
        body.add("client_id", ApplicationProperties.getValue("app.listAPI.clientId").trim());
        body.add("client_secret", ApplicationProperties.getValue("app.listAPI.clientSecret").trim());
        body.add("grant_type", ApplicationProperties.getValue("app.listAPI.grantType").trim());

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = null;
        try {
            response = getRestTemplate().exchange(url, HttpMethod.POST, entity, String.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (response != null && response.getStatusCode().equals(HttpStatus.OK)) {
            try {
                if (response.getBody() != null) {
                    JsonObject payloadObj = new JsonParser().parse(response.getBody()).getAsJsonObject();
                    respMap.put("access_token", payloadObj.get("access_token").getAsString());
                    respMap.put("token_type", payloadObj.get("token_type").getAsString());
                }
            } catch (Exception e) {
                LOGGER.error("\nFailed to parse response");
                e.printStackTrace();
            }
        }

        return respMap;
    }

    private RestTemplate getRestTemplate() {
        return new RestTemplate();
    }

    private Connection getConnection() {
        try {
            return PersistentStoreManager.getConnection();
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return null;
    }

    private Connection getMartConnection() {
        try {
            return PersistentStoreManager.getSolutionDBConnection(systemSolutionId);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return null;
    }

    public void updateReturnResultStatus(Integer runId, Integer solutionId, String status) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = PersistentStoreManager.getConnection();
            ps = conn.prepareStatement(
                    "update VALIDATION_RETURN_RESULT SET STATUS = ? , END_DATE_TIME = ? where RUN_ID = ? and SOLUTION_ID=?");
            ps.setString(1, status);
            ps.setTimestamp(2, new java.sql.Timestamp((new Date()).getTime()));
            ps.setInt(3, runId);
            ps.setInt(4, solutionId);
            ps.executeUpdate();
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void saveOrUpdateReturnResultStatus(List<ValidationRunDetails> vrdList) {
        int counter = 999;
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = PersistentStoreManager.getConnection();
            ps = conn.prepareStatement(
                    "INSERT INTO VALIDATION_RUN_DETAILS(RUN_ID,VALIDATION_ID,SEQUENCE_NUMBER,STATUS,EVALUATED_EXPRESSION,TOTAL_OCCURRENCE,TOTAL_FAILED,VALIDATION_TYPE,DIMENSIONS_CSV,REPLACED_EXPRESSION)VALUES(?,?,?,?,?,?,?,?,?,?)");
            for (ValidationRunDetails vrd : vrdList) {
                ps.setInt(1, vrd.getRunId());
                ps.setInt(2, vrd.getValidationId());
                ps.setInt(3, vrd.getSequenceNumber());
                ps.setString(4, vrd.getStatus());
                ps.setString(5, vrd.getEvaluatedExpression());
                ps.setInt(6, vrd.getTotalOccurrence());
                ps.setInt(7, vrd.getTotalFailed());
                ps.setString(8, vrd.getValidationType());
                ps.setString(9, vrd.getDimensionsCSV());
                ps.setString(10, vrd.getReplacedExpression());
                ps.addBatch();
                if (counter == Integer.parseInt(ApplicationProperties.getValue("app.validations.insertBatchSize"))) {
                    ps.executeBatch();
                    counter = 0;
                    ps.clearBatch();
                }
                counter++;
            }
            ps.executeBatch();
            ps.close();
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void createListTable(String query) throws Throwable {
        LOGGER.info("\nTable creation query :\n" + query);

        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getMartConnection();

            ps = conn.prepareStatement(query);
            ps.executeUpdate();
            conn.commit();

        } catch (Exception e) {
            hasError = Boolean.TRUE;
            throw e;
        } finally {
            this.query = query.replace(" Into ", " ").replace(" INTO ", " ").replace(((tableName == null || tableName.equalsIgnoreCase("")) ? ddTableName : tableName), "");
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private RequestResponse getQuery(String entityName, List<Integer> versionNumbers, List<String> columns,
                                     List<Map<String, Object>> filters, String tempTableName) {
        Map<String, String> tokenMap = getAuthenticationToken();
        String url = ApplicationProperties.getValue("app.listAPI.endpoint").trim() + ValidationConstants.URL_SEPARATOR
                + ApplicationProperties.getValue("app.listAPI.endpointGetList").trim();
        try {
            if (tokenMap != null && !tokenMap.isEmpty()) {
                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", tokenMap.get("token_type") + " " + tokenMap.get("access_token"));
                headers.setContentType(MediaType.APPLICATION_JSON);

                Map<String, Object> body = new HashMap<String, Object>();
                body.put("solutionId", systemSolutionId);
                body.put("entityName", entityName);
                body.put("versionNumbers", versionNumbers);
                body.put("columns", columns);
                body.put("filters", filters);
                body.put("dbType", ApplicationProperties.getValue("app.martDBType").trim());
                body.put("tempTableName", tempTableName);

                HttpEntity<?> entity = new HttpEntity<>(body, headers);
                ResponseEntity<String> response = getRestTemplate().exchange(url, HttpMethod.POST, entity,
                        String.class);

                if (response.getStatusCode().equals(HttpStatus.OK)) {
                    try {
                        if (response.getBody() != null) {
                            return new Gson().fromJson(response.getBody(), RequestResponse.class);
                        }
                    } catch (Exception e) {
                        LOGGER.error("\nFailed to parse response");
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private RequestResponse getRefQuery(Map<String, Object> body) {
        LOGGER.info("EXECFLOW -> MetadataResolver -> getRefQuery -> " + body.get("entityBusinessName"));
        String url = ApplicationProperties.getValue("app.listAPI.endpoint").trim() + ValidationConstants.URL_SEPARATOR
                + ApplicationProperties.getValue("app.refData.api.endpoint").trim();

        try {
            if (ApplicationProperties.getValue("app.refData.api.security.enable").trim().equalsIgnoreCase("false")) {
                HttpEntity<?> entity = new HttpEntity<>(body);
                ResponseEntity<String> response = getRestTemplate().exchange(url, HttpMethod.POST, entity, String.class);

                if (response.getStatusCode().equals(HttpStatus.OK)) {
                    try {
                        if (response.getBody() != null) {
                            return new Gson().fromJson(response.getBody(), RequestResponse.class);
                        }
                    } catch (Exception e) {
                        LOGGER.error("\nFailed to parse response");
                        e.printStackTrace();
                    }
                }
            } else {
                // TODO need to handle security here | Deepak
            }
        } catch (Exception e) {
            LOGGER.error("Failed to get ref query for entity: " + body.get("entityBusinessName") + "\n" + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    private RequestResponse getDdEntityQuery(Map<String, Object> body) {
        String url = ApplicationProperties.getValue("app.listAPI.endpoint").trim() + ValidationConstants.URL_SEPARATOR
                + ApplicationProperties.getValue("app.refData.api.endpoint").trim();

        try {
            if (ApplicationProperties.getValue("app.refData.api.security.enable").trim().equalsIgnoreCase("false")) {
                HttpEntity<?> entity = new HttpEntity<>(body);
                ResponseEntity<String> response = getRestTemplate().exchange(url, HttpMethod.POST, entity, String.class);

                if (response.getStatusCode().equals(HttpStatus.OK)) {
                    try {
                        if (response.getBody() != null) {
                            return new Gson().fromJson(response.getBody(), RequestResponse.class);
                        }
                    } catch (Exception e) {
                        LOGGER.error("\nFailed to parse response");
                        e.printStackTrace();
                    }
                }
            } else {
                // TODO need to handle security here | Deepak
            }
        } catch (Exception e) {
            LOGGER.error("Failed to get ref query for entity: " + body.get("entityBusinessName") + "\n" + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    public void init(Integer solutionId, String tableName, Boolean dropTables) {
        this.tableName = tableName;
        this.systemSolutionId = solutionId;
        this.dropTables = dropTables;
    }

    private void dropTable() throws Exception {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            try {
                conn = getMartConnection();
                ps = conn.prepareStatement("DROP TABLE " + tableName);
                ps.executeUpdate();
                conn.commit();
            } catch (Exception e) {
                // do-nothing
            }

            conn.commit();

        } catch (Exception e) {
            throw e;
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public Map<String, Integer> getMetadata(String sqlQuery, Integer systemSolutionId) throws Exception {
        this.systemSolutionId = systemSolutionId;
        Connection conn = null;
        ResultSet rs = null;
        ResultSetMetaData rsmd = null;
        Statement ps = null;
        try {
            conn = getMartConnection();
            ps = conn.createStatement();
            rs = ps.executeQuery(sqlQuery);

            rsmd = rs.getMetaData();

            Map<String, Integer> metadata = new LinkedHashMap<String, Integer>();

            for (int i = 0; i < rsmd.getColumnCount(); ++i) {
                metadata.put(rsmd.getColumnName(i + 1), rsmd.getColumnType(i + 1));
            }

            return metadata;

        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        
    }

    public String getValidationRequestPayloadByRunId(Integer runId) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String payload = null;
        String query = "select payload from VALIDATION_REQUEST where run_id = " + runId;
        try {
            conn = PersistentStoreManager.getConnection();
            ps = conn.prepareStatement(query);
            rs = ps.executeQuery();
            while (rs.next()) {
                payload = rs.getString("payload");
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        return payload;
    }

    public List<Integer> getObsoleteRunIdsWithGroupFilter(String validationPayload) {
        Connection conn = null;
        ResultSetMetaData rsmd = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Integer> runIds = new ArrayList<>();
        Gson g = new Gson();
        JsonObject p = g.fromJson(validationPayload, JsonObject.class);
        Integer solutionId = Integer.parseInt(p.get("solutionId").toString());
        Integer versionNo = Integer.parseInt(p.get("versionNo").toString());
        Integer regReportId = Integer.parseInt(p.get("regReportId").toString());
        Integer orgId = Integer.parseInt(p.get("orgId").toString());
        Integer periodId = Integer.parseInt(p.get("periodId").toString());

        String query =
                "SELECT distinct(VRD.RUN_ID) as runid"
                        + " FROM VALIDATION_RUN_DETAILS VRD"
                        + " INNER JOIN VALIDATION_RETURN_LINKAGE VRL "
                        + " ON VRD.VALIDATION_ID = VRL.VALIDATION_ID"
                        + " AND VRD.SEQUENCE_NUMBER = VRL.SEQUENCE_NO"
                        + " INNER JOIN VALIDATION_RETURN_RESULT VRR"
                        + " ON VRD.RUN_ID = VRR.RUN_ID"
                        + " LEFT JOIN validationresultsdelete VCSV"
                        + " ON VRD.RUN_ID = VCSV.deleteable_run_id"
                        + " WHERE VCSV.deleteable_run_id IS NULL"
                        + " AND VRL.REG_REPORT_ID = " + regReportId + ""
                        + " AND VRL.SOLUTION_ID = " + solutionId + ""
                        + " AND VRR.PERIOD_ID = '" + periodId + "'"
                        + " AND VRR.REG_REPORT_ID = " + regReportId + ""
                        + " AND VRR.ORG_ID = " + orgId + ""
                        + " AND VRR.VERSION_NO = " + versionNo + ""
                        + " AND VRR.STATUS  like 'COMPLETED%'"
                        + " GROUP BY VRL.VALIDATION_GROUP_ID,VRD.RUN_ID";
        String finalQuery = "select runId from (select Row_number() OVER (ORDER BY runId DESC) AS rnum , runId from (" + query + ") t1"
                + ") t2 where rnum > 1";
        try {
            conn = PersistentStoreManager.getConnection();
            ps = conn.prepareStatement(finalQuery);
            rs = ps.executeQuery();
            rsmd = rs.getMetaData();
            while (rs.next()) {
                for (int i = 0; i < rsmd.getColumnCount(); i++) {
                    runIds.add(rs.getInt(rsmd.getColumnName(i + 1).toLowerCase()));
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return runIds;
    }

    public void saveValidationResultsCsvDeleteDetails(Integer currentRunId, Integer runId) {
        Connection conn = null;
        PreparedStatement ps = null;
        String query = "insert into validationresultsdelete values(?,?,?)";
        try {
            conn = PersistentStoreManager.getConnection();
            ps = conn.prepareStatement(query);
            ps.setInt(1, getMaxId() + 1);
            ps.setInt(2, currentRunId);
            ps.setInt(3, runId);
            ps.executeUpdate();
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public Integer getMaxId() {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Integer maxId = null;
        String q = "select max(validationcsvid) as maxid from validationresultsdelete";
        try {
            conn = PersistentStoreManager.getConnection();
            ps = conn.prepareStatement(q);
            rs = ps.executeQuery();

            while (rs.next()) {
                maxId = rs.getInt("maxid");
            }
            if (maxId == null) {
                return 0;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        return maxId;
    }

    public List<Map<String, Object>> fetchValidationResultsCsvDeleteDetails() {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        ResultSetMetaData rm = null;
        List<Map<String, Object>> csvResults = new ArrayList<Map<String, Object>>();
        String q = "select current_run_id as currentRunId,deleteable_run_id as deletableRunId from validationresultsdelete";
        try {
            conn = PersistentStoreManager.getConnection();
            ps = conn.prepareStatement(q);
            rs = ps.executeQuery();
            rm = rs.getMetaData();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<String, Object>();
                for (int i = 0; i < rm.getColumnCount(); i++) {
                    row.put(rm.getColumnName(i + 1).toLowerCase(), rs.getObject(i + 1));
                }
                csvResults.add(row);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return csvResults;
    }
}