package com.fintellix.validationrestservice.core.evaulator;

import com.fintellix.validationrestservice.core.evaulator.spEL.ExpressionEvaluatorContext;
import com.fintellix.validationrestservice.core.executor.ExpressionProcessor;
import com.fintellix.validationrestservice.core.parser.ExpressionParser;
import com.fintellix.validationrestservice.definition.ExpressionMetaData;
import com.fintellix.validationrestservice.definition.QueryBuilder;
import com.fintellix.validationrestservice.definition.SubExpressionMetaData;
import com.fintellix.validationrestservice.definition.ValidationConstants;
import com.fintellix.validationrestservice.util.ApplicationProperties;
import com.fintellix.validationrestservice.util.connectionManager.PersistentStoreManager;
import com.northconcepts.datapipeline.core.DataReader;
import com.northconcepts.datapipeline.jdbc.JdbcReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ValidationFunctions {

    private static Logger LOGGER = LoggerFactory.getLogger(ValidationFunctions.class);

    private static final String ME = ValidationConstants.ME;
    private Map<String, Map<String, Object>> tableMetadata;
    private Map<String, Map<String, String>> dimensionColMap;
    private Map<String, String> tableQueryInfo;
    private Map<String, String> returnFormInfo = new ConcurrentHashMap<>();
    private Map<String, String> returnEntityNameInfo = new ConcurrentHashMap<>();
    private Map<String, String> returnTableLink = new ConcurrentHashMap<>();
    private ExpressionMetaData expressionMetaData = new ExpressionMetaData();

    private Map<String, Map<String, Map<String, String>>> aliaisedLineColumnData;
    
   // private Set<String> outputColumns = new HashSet<>();

    private Pattern gridLineColumn = Pattern.compile("ME.A_?+[0-9]+");
    private Boolean isSparkEnabled = Boolean.FALSE;

    public void init(Map<String, Map<String, Object>> tableMetadata, Map<String, Map<String, String>> dimensionColMap,
                     Map<String, String> tableQueryInfo, Map<String, String> returnTableLink,
                     Map<String, String> returnEntityNameInfo, Map<String, String> returnFormInfo,
                     Map<String, Map<String, Map<String, String>>> aliaisedLineColumnData) {
        this.tableMetadata = tableMetadata;
        this.dimensionColMap = dimensionColMap;
        this.tableQueryInfo = tableQueryInfo;
        this.returnEntityNameInfo = returnEntityNameInfo;
        this.returnFormInfo = returnFormInfo;
        this.returnTableLink = returnTableLink;
        this.aliaisedLineColumnData = aliaisedLineColumnData;
		String sparkEnabled = ApplicationProperties.getValue("app.spark.enabled").trim();

		if (sparkEnabled == null) {
			sparkEnabled = "false";
		} else if (sparkEnabled.trim().length() == 0) {
			sparkEnabled = "false";
		}
		isSparkEnabled = Boolean.parseBoolean(sparkEnabled);

    }

    public DataReader read(String query, Integer systemSolutionId) throws Throwable {
        return new JdbcReader(PersistentStoreManager.getSolutionDBConnection(systemSolutionId), query)
                .setAutoCloseConnection(true);
    }

    /*
     * FOREACH
     */

    public void forEachBuilder(String tableMetadata, List<String> groupByColumns, List<String> filters,
                               QueryBuilder builder, List<String> dynamicFilters) {

        String[] metaData = tableMetadata.split("#");
        String tableName = tableMetadata.split("#")[metaData.length - 1];

        String query = "select * from " + tableName + " t1 ";
        String groupBy = "";
        String where = null;

        List<String> filterList = new ArrayList<>(filters);
        filterList.addAll(dynamicFilters);

        for (String filter : filterList) {
            if (where == null) {
                where = " Where ";
            } else {
                where = where + " AND ";
            }

            where = where + filter.replace(tableName, "t1");
        }

        if (metaData.length > 1) {
            if (metaData[0].equalsIgnoreCase(ValidationConstants.TYPE_RETURN)) {
                if (where == null) {
                    where = " WHERE ";
                } else {
                    where = where + " AND ";
                }

                where = where + createFilterString("t1", tableName, metaData);

                builder.setSystemFilters(createFilterString("t2", tableName, metaData));
            } else if (metaData[0].equalsIgnoreCase(ValidationConstants.TYPE_REFTABLE) ||
                    metaData[0].equalsIgnoreCase(ValidationConstants.TYPE_ENTITY)) {

                String refFilter = createRefFilterString("t1", tableName, metaData);
                if (refFilter != null && refFilter.trim().length() > 0) {
                    if (where == null) {
                        where = " WHERE ";
                    } else {
                        where = where + " AND ";
                    }
                    where += " " + refFilter;
                }

                builder.setSystemFilters(createRefFilterString("t2", tableName, metaData));
            }
        }

        if (where == null) {
            where = "";
        }

        if (groupByColumns.isEmpty() && this.tableMetadata.containsKey(tableName)
                && ((!this.tableMetadata.get(tableName).isEmpty()
                && Objects.equals(this.tableMetadata.get(tableName).get("isGrid"), Boolean.FALSE))
                || this.tableMetadata.get(tableName).isEmpty())) {

            groupByColumns.addAll(dimensionColMap.get(tableName).keySet().stream()
                    .map(k -> tableName + "." + k).collect(Collectors.toList()));
        }

        //is Navigable
        if (metaData.length > 1) {
            if (metaData[0].equalsIgnoreCase(ValidationConstants.TYPE_RETURN) ||
                    metaData[0].equalsIgnoreCase(ValidationConstants.TYPE_REFTABLE) ||
                    metaData[0].equalsIgnoreCase(ValidationConstants.TYPE_ENTITY)) {
                List<String> sourceGrpCols = new ArrayList<>();
                List<String> baseGrpCols = new ArrayList<>();
                //sourceGrpCols.addAll(groupByColumns);
                for (String col : groupByColumns) {
                    sourceGrpCols.add(col.replace(tableName + ".", ""));
                }
                if (sourceGrpCols.isEmpty()) {
                    sourceGrpCols.addAll(dimensionColMap.get(tableName).keySet());
                }
                baseGrpCols.addAll(dimensionColMap.get(tableName).keySet());

                Boolean isNavigable = Boolean.FALSE;
                if (baseGrpCols.size() == 0) { // non group by
                    isNavigable = Boolean.TRUE;

                } else {
                    if (baseGrpCols.size() == sourceGrpCols.size()) {
                        sourceGrpCols.retainAll(baseGrpCols);
                        if (sourceGrpCols.size() == baseGrpCols.size()) {
                            isNavigable = Boolean.TRUE;
                            for (String grpCol : sourceGrpCols) {
                                expressionMetaData.getGroupByColumns().put(dimensionColMap.get(tableName).get(grpCol),
                                        grpCol);
                            }
                        }
                    } else {
                        isNavigable = Boolean.FALSE;
                    }

                }
                expressionMetaData.setIsNavigable(isNavigable);
                if (this.tableMetadata.containsKey(tableName) && !this.tableMetadata.get(tableName).isEmpty()
                        && Objects.equals(this.tableMetadata.get(tableName).get("isGrid"), Boolean.TRUE)) {
                    expressionMetaData.getBasePrimaryColumns().put("GROUP_BY_DIMENSION", "GROUP_BY_DIMENSION");
                    expressionMetaData.getBasePrimaryColumns().put("LINE_ITEM_MAP_ID", "LINE_ITEM_MAP_ID");

                    expressionMetaData.setReturnType("GRID");

                } else {
                    for (String grpCol : baseGrpCols) {
                        expressionMetaData.getBasePrimaryColumns().put(grpCol, dimensionColMap.get(tableName).get(grpCol));
                    }
                    expressionMetaData.setReturnType("LIST");
                }
            }
        }

        expressionMetaData.setEntityName(returnEntityNameInfo.get(tableName));
        expressionMetaData.setBaseTableName(tableName);
        expressionMetaData.setIsForEachPresent(Boolean.TRUE);

        if (metaData[0].equalsIgnoreCase(ValidationConstants.TYPE_RETURN)) {
            expressionMetaData.setReportId((Integer) this.tableMetadata.get(tableName).get("reportId"));
            expressionMetaData.setSectionId((Integer) this.tableMetadata.get(tableName).get("sectionId"));
            expressionMetaData.setReportName((String) this.tableMetadata.get(tableName).get("reportName"));
            expressionMetaData.setSectionDesc((String) this.tableMetadata.get(tableName).get("sectionDesc"));
            expressionMetaData.setFormName(returnFormInfo.get(tableName));
            expressionMetaData.setOrgId(Integer.parseInt(metaData[1]));
            expressionMetaData.setRegReportVersion(Integer.parseInt(metaData[2]));
            expressionMetaData.setVersionNo(Integer.parseInt(metaData[3]));
            expressionMetaData.setPeriodId(Integer.parseInt(metaData[4]));
        } else if (metaData[0].equalsIgnoreCase(ValidationConstants.TYPE_REFTABLE)
                || metaData[0].equalsIgnoreCase(ValidationConstants.TYPE_ENTITY)) {
            expressionMetaData.setOrgId(Integer.parseInt(metaData[1]));
            expressionMetaData.setPeriodId(Integer.parseInt(metaData[2]));
        }

        if (!groupByColumns.isEmpty()) {
            Integer counter = 0;
            query = "";

            //TODO add list logic handle primary/others/group
            // use max for primary, max/sum for others/ none for group cols
            if (this.tableMetadata.containsKey(tableName) && !this.tableMetadata.get(tableName).isEmpty()
                    && Objects.equals(this.tableMetadata.get(tableName).get("isGrid"), Boolean.TRUE)) {
                counter = 0;

                for (String col : aliaisedLineColumnData.get(tableName).keySet()) {
                    if (counter > 0) {
                        query = query + ",";
                    } else {
                        query = " select ";
                    }

                    if (aliaisedLineColumnData.get(tableName).get(col).get("DATA_TYPE").equalsIgnoreCase("DATE")) {
                        query = query + "max(" + col + ")" + " as " + col + " ";
                    } else if (aliaisedLineColumnData.get(tableName).get(col).get("DATA_TYPE").equalsIgnoreCase("STRING")) {
                        query = query + "max(" + col + ")" + " as " + col + " ";
                    } else if (aliaisedLineColumnData.get(tableName).get(col).get("DATA_TYPE").equalsIgnoreCase("NUMBER")) {
                        query = query + "sum(" + col + ")" + " as " + col + " ";
                    }
                    counter++;
                }

                if (query.length() > 0) {
                    query = query + ",";
                } else {
                    query = " select ";
                }

                query = query + "MAX(ORG_ENTITY_ID) as ORG_ENTITY_ID ,MAX(VERSION_NUMBER) as VERSION_NUMBER,MAX(Period_ID) as Period_ID,MAX(REG_REPORT_VERSION) as REG_REPORT_VERSION,MAX(GROUP_BY_DIMENSION) as GROUP_BY_DIMENSION,MAX(LINE_ITEM_MAP_ID) as LINE_ITEM_MAP_ID";

            } else {
                List<String> listPrimaryCols = new ArrayList<>();
                listPrimaryCols.addAll(dimensionColMap.get(tableName).keySet());
                List<String> allListCols = new ArrayList<>();
                allListCols.addAll(aliaisedLineColumnData.get(tableName).keySet());
                counter = 0;

                //fetch othercols how??

                for (String cols : groupByColumns) {
                    String groubyColumn = cols.split("\\.")[1];

                    allListCols.remove(groubyColumn);
                    listPrimaryCols.remove(groubyColumn);
                }

                for (String pkCol : listPrimaryCols) {
                    allListCols.remove(pkCol);
                }

                //builiding query
                // adding PK columns
                for (String pkCol : listPrimaryCols) {
                    if (counter > 0) {
                        query = query + ",";
                    } else {
                        query = " select ";
                    }

                    query = query + "MAX(" + pkCol + ") as " + pkCol;
                    counter++;
                }


                //adding all columns
                for (String col : allListCols) {
                    if (counter > 0) {
                        query = query + ",";
                    } else {
                        query = " select ";
                    }
                    if (aliaisedLineColumnData.get(tableName).get(col).get("DATA_TYPE").equalsIgnoreCase("DATE")) {
                        query = query + "max(" + col + ")" + " as " + col + " ";
                    } else if (aliaisedLineColumnData.get(tableName).get(col).get("DATA_TYPE").equalsIgnoreCase("STRING")) {
                        query = query + "max(" + col + ")" + " as " + col + " ";
                    } else if (aliaisedLineColumnData.get(tableName).get(col).get("DATA_TYPE").equalsIgnoreCase("NUMBER")) {
                        query = query + "sum(" + col + ")" + " as " + col + " ";
                    }
                    counter++;
                }


            }

            Integer groupByCounter = 0;
            for (String col : groupByColumns) {
                if (counter > 0) {

                    query = query + ",";
                } else {
                    query = " select ";
                }
                if (groupByCounter > 0) {
                    groupBy = groupBy + ",";
                } else {
                    groupBy = " GROUP BY ";
                }
                query = query + col.replace(tableName + ".", "");

                groupBy = groupBy + " " + col.replace(tableName + ".", "");

                counter++;
                groupByCounter++;
            }
            query = query + " from " + tableName + " t1 ";

        }
        where = where.replace("==", "=");
        builder.setQuery(query + " " + where + " " + groupBy);
        builder.setGroupBy(groupByColumns);
        builder.setFilter(filters);
        builder.setBaseTableName(tableName);
        builder.setMetaData(metaData);
    }

    /*
     * LOOKUP
     */

    private String lookUpJoinBuilder(List<String> condition, Map<String, String> tableAliasMap) {
        String joinCondition = null;
        for (String cond : condition) {
            if (joinCondition != null) {
                joinCondition = joinCondition + " AND ";
            } else {
                joinCondition = "";
            }

            for (String key : tableAliasMap.keySet()) {
                cond = cond.replace(key + ".", tableAliasMap.get(key) + ".");
            }

            joinCondition = joinCondition + cond;
        }

        return joinCondition.replace("==", "=");
    }

    private String lookUpFilterBuilder(List<String> filterClause, Map<String, String> tableAliasMap,
                                       String[] sourceMetaData, String[] targetMetaData) {
        String filterCondition = null;

        for (String cond : filterClause) {
            if (filterCondition != null) {
                filterCondition = filterCondition + " AND ";
            } else {
                filterCondition = "";
            }

            for (String key : tableAliasMap.keySet()) {
                cond = cond.replace(key+".", tableAliasMap.get(key)+".");
            }

            filterCondition = filterCondition + cond.replace("==", "=");
        }

        if (sourceMetaData != null && sourceMetaData.length > 1) {
            if (sourceMetaData[0].equalsIgnoreCase(ValidationConstants.TYPE_RETURN)) {
                if (filterCondition == null) {
                    filterCondition = "";
                } else {
                    filterCondition = filterCondition + " AND ";
                }

                String tableName = sourceMetaData[sourceMetaData.length - 1];
                //String sourceAlias = tableAliasMap.get(tableName);
                filterCondition = filterCondition + createFilterString("t1", tableName, sourceMetaData);
            } else if (sourceMetaData[0].equalsIgnoreCase(ValidationConstants.TYPE_REFTABLE)) {

                String tableName = sourceMetaData[sourceMetaData.length - 1];
                //String sourceAlias = tableAliasMap.get(tableName);
                String refFilter = createRefFilterString("t1", tableName, sourceMetaData);
                if (refFilter.trim().length() > 0) {
                    if (filterCondition == null) {
                        filterCondition = "";
                    } else {
                        filterCondition = filterCondition + " AND ";
                    }
                    filterCondition += " " + refFilter;
                }
            } else if (sourceMetaData[0].equalsIgnoreCase(ValidationConstants.TYPE_ENTITY)) {

                String tableName = sourceMetaData[sourceMetaData.length - 1];
                //String sourceAlias = tableAliasMap.get(tableName);
                String refFilter = createRefFilterString("t1", tableName, sourceMetaData);
                if (refFilter.trim().length() > 0) {
                    if (filterCondition == null) {
                        filterCondition = "";
                    } else {
                        filterCondition = filterCondition + " AND ";
                    }
                    filterCondition += " " + refFilter;
                }
            }
        }

        if (targetMetaData != null && targetMetaData.length > 1) {
            if (targetMetaData[0].equalsIgnoreCase(ValidationConstants.TYPE_RETURN)) {
                if (filterCondition == null) {
                    filterCondition = "";
                } else {
                    filterCondition = filterCondition + " AND ";
                }

                String tableName = targetMetaData[targetMetaData.length - 1];
                //String targetAlias = tableAliasMap.get(tableName);
                filterCondition = filterCondition + createFilterString("t2", tableName, targetMetaData);
            } else if (targetMetaData[0].equalsIgnoreCase(ValidationConstants.TYPE_REFTABLE)) {
                String tableName = targetMetaData[targetMetaData.length - 1];
                //String targetAlias = tableAliasMap.get(tableName);
                String refFilter = createRefFilterString("t2", tableName, targetMetaData);
                if (refFilter.trim().length() > 0) {
                    if (filterCondition == null) {
                        filterCondition = "";
                    } else {
                        filterCondition = filterCondition + " AND ";
                    }
                    filterCondition += " " + refFilter;
                }
            } else if (targetMetaData[0].equalsIgnoreCase(ValidationConstants.TYPE_ENTITY)) {
                String tableName = targetMetaData[targetMetaData.length - 1];
                //String targetAlias = tableAliasMap.get(tableName);
                String refFilter = createRefFilterString("t2", tableName, targetMetaData);
                if (refFilter.trim().length() > 0) {
                    if (filterCondition == null) {
                        filterCondition = "";
                    } else {
                        filterCondition = filterCondition + " AND ";
                    }
                    filterCondition += " " + refFilter;
                }
            }
        }

        if (filterCondition == null) {
            filterCondition = "";
        }

        return filterCondition;
    }

    private String createRefFilterString(String tableAlias, String tableName, String[] metaData) {
        String sysFilter = "";
        if (aliaisedLineColumnData.get(tableName).get(ValidationConstants.IS_ORG_FILTER_AVAILABLE) != null) {
            sysFilter = " " + tableAlias + "." + aliaisedLineColumnData.get(tableName).get(ValidationConstants.IS_ORG_FILTER_AVAILABLE).get(ValidationConstants.ORG_COLUMN) + " = " + metaData[1];
        }
        if (aliaisedLineColumnData.get(tableName).get(ValidationConstants.IS_SCDII) != null) {

            if (sysFilter.trim().length() > 0) {
                sysFilter = sysFilter + " AND ";
            }

            String toDate = "";
            if (ApplicationProperties.getValue("app.martDBType").trim().equalsIgnoreCase("MSSQL")) {
                String trDate = metaData[2];
                toDate = " '" + trDate.substring(0, 4) + "-" + trDate.substring(4, 6) + "-" + trDate.substring(6, 8) + "'";

            } else {
                toDate = " TO_DATE(" + metaData[2] + ", 'YYYYMMDD') ";
            }
            sysFilter = sysFilter + toDate + "  BETWEEN " + tableAlias + "." + aliaisedLineColumnData.get(tableName).get(ValidationConstants.IS_SCDII).get(ValidationConstants.START_DATE_COLUMN) + " AND " + tableAlias + "." + aliaisedLineColumnData.get(tableName).get(ValidationConstants.IS_SCDII).get(ValidationConstants.END_DATE_COLUMN);
        }
        return sysFilter;

//        return " " + tableAlias + ".ORGENTITYID=" + metaData[1] + " AND " + tableAlias + ".PERIODID=" + metaData[2] ;
    }

    private String lookUpSelectBuilder(List<String> outputColumn, Map<String, String> tableAliasMap,
                                       QueryBuilder builder, String source, String nodeName, Map<String, String> columnMap) {

        String select = null;

        if (outputColumn.size() > 0 && (source == null || source.trim().length() == 0)
                && (builder.getQuery() == null || builder.getQuery().trim().length() == 0)) {

            if (outputColumn.size() == 1) {
                for (String outCol : outputColumn) {
                    if (select != null) {
                        select = select + " , ";
                    } else {
                        select = " select t2.* ,";
                    }

                    for (String key : tableAliasMap.keySet()) {
                        outCol = outCol.replace(key + ".", tableAliasMap.get(key) + ".");
                    }

                    select = select + outCol + " as " + nodeName;

                    String[] colSplit = outCol.split("\\.");
                    columnMap.put(nodeName, colSplit[colSplit.length - 1]);
                }
            } else {
                for (String outCol : outputColumn) {
                    if (select != null) {
                        select = select + " , ";
                    } else {
                        select = " select t2.* ,";
                    }

                    for (String key : tableAliasMap.keySet()) {
                        outCol = outCol.replace(key + ".", tableAliasMap.get(key) + ".");
                    }

                    select = select + outCol;
                }
            }
        } else if (outputColumn.size() > 0) {
            if (outputColumn.size() == 1) {
                for (String outCol : outputColumn) {
                    if (select != null) {
                        select = select + " , ";
                    } else {
                        select = " select t1.* ,";
                    }

                    for (String key : tableAliasMap.keySet()) {
                        outCol = outCol.replace(key + ".", tableAliasMap.get(key) + ".");
                    }

                    select = select + outCol + " as " + nodeName;
                    //System.out.println("---Output Column ---" + outCol);
                    String[] colSplit = outCol.split("\\.");

                    columnMap.put(nodeName, colSplit[colSplit.length - 1]);
                }
            } else {
                for (String outCol : outputColumn) {
                    if (select != null) {
                        select = select + " , ";
                    } else {
                        select = " select t1.* ,";
                    }

                    for (String key : tableAliasMap.keySet()) {
                        outCol = outCol.replace(key + ".", tableAliasMap.get(key) + ".");
                    }

                    select = select + outCol;
                }
            }
        } else {
            if (ApplicationProperties.getValue("app.martDBType").trim().equalsIgnoreCase("MSSQL")) {
                select = "select t1.*, CAST(CASE WHEN t2." + nodeName + " is NULL THEN 0 ELSE 1 END AS bit) as " + nodeName;
            } else {
                select = "select t1.*, CAST(CASE WHEN t2." + nodeName + " is NULL THEN 0 ELSE 1 END AS NUMBER(1)) as " + nodeName;
            }


            columnMap.put(nodeName, nodeName);
        }

        return select;
    }

    public String lookUpBuilder(String source, String target, List<String> condition, List<String> outputColumn,
                                List<String> filterClause, QueryBuilder builder, String nodeName, Map<String, String> columnMap) {
        Map<String, String> tableAliasMap = new HashMap<String, String>();
        nodeName = nodeName.trim();
        //this.outputColumns.add(nodeName);
        
        String sourceTableName = "";
        String targetTableName = "";
        String[] sourceMetaData = null;
        String[] targetMetaData;

        if (source == null) {
            // throw exception
        } else if (source != null && source.trim().length() > 0
                && source.trim().equalsIgnoreCase(ValidationConstants.ME_IDENTIFIER)) {
            tableAliasMap.put(source.trim(), "t" + 1);
            tableAliasMap.put(builder.getBaseTableName().trim(), "t" + 1);
            sourceTableName = source.trim();
            sourceMetaData = builder.getMetaData();
        } else if (source != null && source.trim().length() > 0
                && !source.trim().equalsIgnoreCase(ValidationConstants.ME_IDENTIFIER)) {
            sourceMetaData = source.split("#");
            sourceTableName = sourceMetaData[sourceMetaData.length - 1];
            tableAliasMap.put(sourceTableName.trim(), "t" + 1);
        }

        targetMetaData = target.split("#");
        targetTableName = targetMetaData[targetMetaData.length - 1];
        tableAliasMap.put(targetTableName.trim(), "t" + 2);

        String joinCondition = null;
        joinCondition = lookUpJoinBuilder(condition, tableAliasMap);

        if (joinCondition != null) {
            String filter = lookUpFilterBuilder(filterClause, tableAliasMap, sourceMetaData, targetMetaData);
            if (filter.trim().length() > 0) {
                joinCondition = joinCondition + " AND "
                        + lookUpFilterBuilder(filterClause, tableAliasMap, sourceMetaData, targetMetaData);
            }

        } else {
            joinCondition = lookUpFilterBuilder(filterClause, tableAliasMap, sourceMetaData, targetMetaData);
        }

        if (sourceTableName == null || sourceTableName.trim().length() == 0
                && (builder.getQuery() == null || builder.getQuery().trim().length() == 0)) {
            if (joinCondition != null) {
                joinCondition = " WHERE " + joinCondition;
            }
        } else if (joinCondition == null || joinCondition.trim().length() == 0) {
            joinCondition = " on 1=1";
        } else {
            joinCondition = " on " + joinCondition;
        }

        String finalQuery = null;
        String targetQuery = null;

        if (outputColumn.isEmpty()) {
            targetQuery = "(select t1.*,t2." + nodeName + " from " + targetTableName
                    + " t1 left outer join  (select count(*) as " + nodeName + " from " + targetTableName
                    + ") t2 on 1=1) ";
        } else {
            targetQuery = targetTableName;
        }

        if (sourceTableName == null || sourceTableName.trim().length() == 0
                && (builder.getQuery() == null || builder.getQuery().trim().length() == 0)) {
            finalQuery = lookUpSelectBuilder(outputColumn, tableAliasMap, builder, sourceTableName, nodeName, columnMap)
                    + " from " + targetTableName + " t2" + joinCondition;
        } else if (sourceTableName == null || sourceTableName.trim().length() == 0
                || sourceTableName.equalsIgnoreCase("ME")) {
            finalQuery = lookUpSelectBuilder(outputColumn, tableAliasMap, builder, sourceTableName, nodeName, columnMap)
                    + " from (" + builder.getQuery() + " ) t1 left outer join " + targetQuery + " t2 "
                    + joinCondition;
        } else {
            finalQuery = lookUpSelectBuilder(outputColumn, tableAliasMap, builder, sourceTableName, nodeName, columnMap)
                    + " from " + sourceTableName + "  t1 left outer join " + targetQuery + " t2 " + joinCondition;
        }

        builder.setQuery(finalQuery);

        if (outputColumn.isEmpty() || outputColumn.size() == 1) {
            return nodeName;
        } else {
            return "";
        }
    }

    /*
     * SUMIF
     */

    private String aggregateFilterBuilder(List<String> filterCondition, String tableName, QueryBuilder builder,
                                          String[] metaData) {
        String filter = null;

        for (String condition : filterCondition) {
            if (filter == null) {
                filter = " WHERE ";
            } else {
                filter = filter + " AND ";
            }

            filter = filter + condition.replace(tableName+".", "t2.").replace("==", "=");
        }

        if (tableName.equalsIgnoreCase(ME)) {

            if (!builder.getFilter().isEmpty()) {
                for (String condition : builder.getFilter()) {
                    if (filter == null) {
                        filter = " WHERE ";
                    } else {
                        filter = filter + " AND ";
                    }

                    filter = filter + condition.replace(builder.getBaseTableName()+".", "t2.").replace("==", "=");
                }
            }

            if (builder.getSystemFilters() != null && builder.getSystemFilters().length() > 0) {
                if (filter == null) {
                    filter = " WHERE ";
                } else {
                    filter = filter + " AND ";
                }
                filter = filter + builder.getSystemFilters();
            }

        } else if (metaData.length > 1) {

            if (metaData[0].equalsIgnoreCase(ValidationConstants.TYPE_RETURN)) {

                if (filter == null) {
                    filter = " WHERE ";
                } else {
                    filter = filter + " AND ";
                }

                filter = filter + createFilterString("t2", tableName, metaData);
            }

        }

        if (filter == null) {
            filter = "";
        }
        return filter;
    }

    private String aggregateJoinBuilder(String tableName, List<String> groupByColumns, QueryBuilder builder) {

        String joinCondition = null;
        if (groupByColumns.size() == 1 && groupByColumns.get(0).trim().equalsIgnoreCase(ValidationConstants.NOGROUPBY_INDICATOR)) {
            return " ON 1=1";
        }

        if (groupByColumns.size() > 0) {
            for (String condition : groupByColumns) {
                if (joinCondition == null) {
                    joinCondition = " ON ";
                } else {
                    joinCondition = joinCondition + " AND ";
                }

                String patternString = "[\\s]+(?i)AS[\\s]+";
                Pattern pattern = Pattern.compile(patternString);
                Matcher matcher = pattern.matcher(condition);
                boolean matches = matcher.find();
                String conditionArr[] = condition.split("[\\s]+(?i)AS[\\s]+");
                if (matches) {
                    joinCondition = joinCondition + "t1" + conditionArr[1].replace("ME", "") + " = "
                            + conditionArr[0].replace(tableName+".", "t2.");
                } else {
                    joinCondition = joinCondition + condition.replace(tableName+".", "t1.") + " = "
                            + condition.replace(tableName+".", "t2.");
                }

            }

        } else if (builder.getGroupBy().size() > 0) {

            for (String condition : builder.getGroupBy()) {
                if (joinCondition == null) {
                    joinCondition = " ON ";
                } else {
                    joinCondition = joinCondition + " AND ";
                }

                String patternString = "[\\s]+(?i)AS[\\s]+";
                Pattern pattern = Pattern.compile(patternString);
                Matcher matcher = pattern.matcher(condition);
                boolean matches = matcher.find();
                String conditionArr[] = condition.split("[\\s]+(?i)AS[\\s]+");
                if (matches) {
                    joinCondition = joinCondition + "t1" + conditionArr[1].replace("ME", "") + " = "
                            + conditionArr[0].replace(builder.getBaseTableName()+".", "t2.");
                } else {
                    joinCondition = joinCondition + condition.replace(builder.getBaseTableName()+".", "t1.") + " = "
                            + condition.replace(builder.getBaseTableName()+".", "t2.");
                }

            }

        }

        if (joinCondition == null) {
            joinCondition = " ON 1=1";
        }
        return joinCondition;

    }

    private String aggregateGroupByBuilder(String tableName, List<String> groupByColumns, QueryBuilder builder) {
        String groupBy = null;

        if (groupByColumns.size() == 1 && groupByColumns.get(0).trim().equalsIgnoreCase(ValidationConstants.NOGROUPBY_INDICATOR)) {
            return "";
        }
        if (groupByColumns.size() > 0) {
            for (String condition : groupByColumns) {
                if (groupBy == null) {
                    groupBy = " GROUP BY ";
                } else {
                    groupBy = groupBy + " , ";
                }

                groupBy = groupBy
                        + condition.replace(tableName+".", "t2.");
            }
        } else if (//tableName.equalsIgnoreCase(ME) && 
                builder.getGroupBy().size() > 0) {
            for (String condition : builder.getGroupBy()) {
                if (groupBy == null) {
                    groupBy = " GROUP BY ";
                } else {
                    groupBy = groupBy + " , ";
                }

                groupBy = groupBy + condition.replace(builder.getBaseTableName()+".", "t2.");
            }
        }

        if (groupBy == null) {
            groupBy = "";
        }
        String patternString = "[\\s]+(?i)AS[\\s]+";
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(groupBy);
        boolean matches = matcher.find();
        if (matches) {
            groupBy = groupBy.split("[\\s]+(?i)AS[\\s]+")[0];
        }
        return groupBy;
    }

    private String aggregateSelectBuilder(String tableName, List<String> groupByColumns, QueryBuilder builder) {
        String select = null;
        if (!(groupByColumns.size() == 1 && groupByColumns.get(0).trim().equalsIgnoreCase(ValidationConstants.NOGROUPBY_INDICATOR))) {

            if (groupByColumns.size() > 0) {
                for (String condition : groupByColumns) {
                    if (select == null) {
                        select = "";
                    }
                    select = select + " , ";
                    select = select + condition.replace(tableName+".", "t2.");
                }
            } else if (//tableName.equalsIgnoreCase(ME) &&
                    builder.getGroupBy().size() > 0) {
                for (String condition : builder.getGroupBy()) {
                    if (select == null) {
                        select = "";
                    }
                    select = select + " , ";
                    select = select + condition.replace(builder.getBaseTableName()+".", "t2.");
                }
            }
        }

        if (select == null) {
            select = "";
        }

        String patternString = "[\\s]+(?i)AS[\\s]+";
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(select);
        boolean matches = matcher.find();
        if (matches) {
            select = select.split("[\\s]+(?i)AS[\\s]+")[0];
        }
        return select;
    }

    public String aggregateBuilder(String tableMetadata, List<String> groupByColumns, List<String> filterCondition,
                                   String outputCol, String nodeName, QueryBuilder builder, String funcName, Map<String, String> columnMap) {

    	
        nodeName = nodeName.trim();
        outputCol = outputCol.trim();
        //this.outputColumns.add(nodeName);

        String[] metaData = tableMetadata.split("#");
        String tableName = tableMetadata.split("#")[metaData.length - 1];
        String tableAliasName = tableMetadata.split("#")[metaData.length - 1];
        if (metaData[0].equalsIgnoreCase(ME)) {
            metaData = builder.getMetaData();
        }
        isNavigable(tableName, builder, groupByColumns, filterCondition, metaData, outputCol.replace(tableMetadata + ".", ""), nodeName);

        if (tableName.equalsIgnoreCase(ME)) {
            tableName = builder.getBaseTableName();
        }

        String selectQuery = null;
        if (builder.getQuery() != null && builder.getQuery().trim().length() > 0) {
            selectQuery = "select " + funcName.trim() + "(" + outputCol.replace(tableMetadata, "t2") + ") as "
                    + nodeName + aggregateSelectBuilder(tableAliasName, groupByColumns, builder) + " from " + tableName
                    + " t2 " + aggregateFilterBuilder(filterCondition, tableAliasName, builder, metaData)
                    + aggregateGroupByBuilder(tableAliasName, groupByColumns, builder);
            selectQuery = "select t1.*,t2." + nodeName + " from ( " + builder.getQuery() + ") t1 left outer join ("
                    + selectQuery + ") t2" + aggregateJoinBuilder(tableAliasName, groupByColumns, builder);
        } else {
            selectQuery = "select " + funcName.trim() + "(" + outputCol.replace(tableMetadata, "t2") + ") as "
                    + nodeName + aggregateSelectBuilder(tableAliasName, groupByColumns, builder) + " from " + tableName
                    + " t2 " + aggregateFilterBuilder(filterCondition, tableAliasName, builder, metaData)
                    + aggregateGroupByBuilder(tableAliasName, groupByColumns, builder);
        }

//        String[] outCol = outputCol.split("\\.");
        columnMap.put(nodeName, nodeName);

        builder.setQuery(selectQuery);
        return nodeName;
    }

/*    public DataReader addExpr(Set<String> columnsList, String expr, ExpressionParser expressionParser, Set<String> tablesList,
                              Map<String, String> groupByCols, Integer exprId, Map<String, Integer> columnMetaData,
                              DataReader dataReader, List<RunRecordDetail> runRecordDetail, StringBuilder dimensionsCSV) {

        if (expressionMetaData.getIsNavigable()) {
            if (expressionMetaData.getReturnType().equalsIgnoreCase("GRID")) {
                Matcher m = gridLineColumn.matcher(expr);
                while (m.find()) {
                    expressionMetaData.getEntityCols().add(m.group(0).replace(ME + "." + "A_", "A_"));
                    expressionMetaData.getColumnInfo().put(m.group(0).replace(ME + "." + "A_", "A_"),
                            aliaisedLineColumnData.get(expressionMetaData.getBaseTableName()).get(m.group(0).replace(ME + ".", "")));
                }
            } else {
                if (expressionMetaData.getIsForEachPresent()) {
                    String temExpr = expr;
                    List<String> sortedStr = new ArrayList<>(aliaisedLineColumnData.get(expressionMetaData.getBaseTableName()).keySet());
                    sortedStr.sort(Collections.reverseOrder(Comparator.comparingInt(o -> o != null ? o.trim().length() : 0)));

                    for (String key : sortedStr) {
                        if (temExpr.contains(ME + "." + key)) {
                            temExpr = temExpr.replace(ME + "." + key, "");
                            expressionMetaData.getEntityCols().add(key);
                            expressionMetaData.getColumnInfo().put(key, aliaisedLineColumnData.get(expressionMetaData.getBaseTableName()).get(key));
                        }
                    }
                }
                // add logic;
            }
        }

        SpelExpressionParser parser = new SpelExpressionParser();

        List<String> sortedColumnList = new ArrayList<>(columnsList);
        sortedColumnList.sort(Collections.reverseOrder(Comparator.comparingInt(o -> o != null ? o.trim().length() : 0)));

        List<String> sortedTablesList = new ArrayList<>(tablesList);
        sortedTablesList.sort(Collections.reverseOrder(Comparator.comparingInt(o -> o != null ? o.trim().length() : 0)));

        ExpressionTransformer transformer = new ExpressionTransformer(sortedColumnList, expr, parser,
                ExpressionEvaluatorContext.context, expressionParser, sortedTablesList, groupByCols, exprId, columnMetaData,
                expressionMetaData, runRecordDetail, dimensionsCSV);

        return new TransformingReader(dataReader).add(transformer);
    }*/

    private String createFilterString(String tableAlias, String tableName, String[] metaData) {
        String filterStr = " ";

        if (tableMetadata.containsKey(tableName) && !tableMetadata.get(tableName).isEmpty()
                && Objects.equals(tableMetadata.get(tableName).get("isGrid"), Boolean.TRUE)) {
            filterStr = " " + tableAlias + ".ORG_ENTITY_ID=" + metaData[1] + " AND " + tableAlias + ".VERSION_NUMBER=" + metaData[3]
                    + " AND " + tableAlias + ".Period_ID=" + metaData[4] + " AND " + tableAlias + ".REG_REPORT_VERSION=" + metaData[2];
        } else {
            filterStr = " " + tableAlias + ".ORGENTITYID=" + metaData[1] + " AND " + tableAlias + ".VERSIONNUMBER=" + metaData[3]
                    + " AND " + tableAlias + ".PERIODID=" + metaData[4];
        }

        return filterStr;
    }

    private void isNavigable(String tName, QueryBuilder builder, List<String> groupByColumns,
                             List<String> filterCondition, String[] metaData, String tCol, String nodeName) {
        Boolean isNavigable = Boolean.FALSE;
        String tableName;
        if (tName.equalsIgnoreCase(ME)) {
            tableName = builder.getBaseTableName();
        } else {
            tableName = tName;
        }

        Set<String> usedGroupBy = new HashSet<>();
        Set<String> entityGroupBy = new HashSet<>();

        // Set<String> forEachGroupBy = new HashSet<>();

        SubExpressionMetaData grpDetails = new SubExpressionMetaData();
        grpDetails.setIsNavigable(isNavigable);
        Set<String> selectCols = new HashSet<>();
//        List<String> groupByColumns = new ArrayList<>();
//        
//        if((groupByCols.size()==1 && groupByCols.get(0).equalsIgnoreCase(ValidationConstants.NOGROUPBY_INDICATOR))) {
//        	// don't have group by cols
//        }else {
//        	groupByColumns.addAll(groupByCols);
//        }

        String filters = null;
        if (metaData[0].equalsIgnoreCase(ME)) {
            metaData = builder.getMetaData();
        }
        if (metaData[0].equalsIgnoreCase(ValidationConstants.TYPE_RETURN)) {
            String targetCol = tCol.replace(tableName + ".", "");
            if (tName.equalsIgnoreCase(ME)) {
                targetCol = tCol.replace(builder.getBaseTableName() + ".", "").replace(ME + ".", "");
            }
            if (builder.getBaseTableName() != null) {
                if (groupByColumns.size() == 1
                        && groupByColumns.get(0).trim().equalsIgnoreCase(ValidationConstants.NOGROUPBY_INDICATOR)
                        && dimensionColMap.get(tableName) != null && dimensionColMap.get(tableName).size() == 0) {
                    isNavigable = Boolean.TRUE;
                } else if (groupByColumns.size() == 1
                        && groupByColumns.get(0).trim().equalsIgnoreCase(ValidationConstants.NOGROUPBY_INDICATOR)
                        && dimensionColMap.get(tableName) != null && dimensionColMap.get(tableName).size() != 0) {
                    isNavigable = Boolean.FALSE;
                } else {

                    if (groupByColumns.size() > 0) {
                        for (String condition : groupByColumns) {
                            usedGroupBy.add(condition.replace(tableName + ".", ""));
                        }
                    } else if (builder.getGroupBy().size() > 0) {
                        for (String condition : builder.getGroupBy()) {
                            usedGroupBy.add(condition.replace(builder.getBaseTableName() + ".", ""));
                        }
                    }
                    // don't remove this commented condition, required for groupby changes for
                    // future release
                    /*
                     * if (usedGroupBy.isEmpty()) { if (tName.equalsIgnoreCase(ME)) {
                     * usedGroupBy.addAll(dimensionColMap.get(builder.getBaseTableName()).keySet());
                     * } else { usedGroupBy.addAll(dimensionColMap.get(tableName).keySet()); } }
                     */

                    if (tName.equalsIgnoreCase(ME)) {
                        entityGroupBy.addAll(dimensionColMap.get(builder.getBaseTableName()).keySet());
                    } else {
                        entityGroupBy.addAll(dimensionColMap.get(tableName).keySet());
                    }

                    if (entityGroupBy.size() == usedGroupBy.size()) {
                        usedGroupBy.retainAll(entityGroupBy);
                        if (entityGroupBy.size() == usedGroupBy.size()) {
                            isNavigable = Boolean.TRUE;
                        }
                    } else {
                        isNavigable = Boolean.FALSE;
                    }
                }

                // Foreach group by TODO

                if (isNavigable) {
                    for (String col : usedGroupBy) {
                        if (filters == null) {
                            filters = "";
                        } else {
                            filters = filters + " AND ";
                        }
                        filters = filters + "GRP_" + col + " = " + col + " ";
                    }

                    for (String col : filterCondition) {
                        col = col.replace(ME + ".", "GRP_").replace(builder.getBaseTableName() + ".", "GRP_")
                                .replace(tableName + ".", "GRP_");

                        if (filters == null) {
                            filters = "";
                        } else {
                            filters = filters + " AND ";
                        }
                        filters = filters + col;
                    }

                    if (tName.equalsIgnoreCase(ME)) {// parent filter inherit for me
                        for (String col : builder.getFilter()) {
                            col = col.replace(ME + ".", "GRP_").replace(builder.getBaseTableName() + ".", "GRP_")
                                    .replace(tableName + ".", "GRP_");

                            if (filters == null) {
                                filters = "";
                            } else {
                                filters = filters + " AND ";
                            }
                            filters = filters + col;
                        }
                    }

                    if (tableMetadata.containsKey(tableName) && !tableMetadata.get(tableName).isEmpty()
                            && Objects.equals(tableMetadata.get(tableName).get("isGrid"), Boolean.TRUE)) {
                        if (targetCol.startsWith("A_")) {
                            selectCols.add(targetCol.replace("A_", "") + " AS LINE_ITEM_ID");
                            selectCols.add(targetCol);
                        } else {
                            isNavigable = Boolean.FALSE;
                        }
                        selectCols.add("GROUP_BY_DIMENSION");
                        selectCols.add("LINE_ITEM_MAP_ID");

                        if (filters == null) {
                            filters = "";
                        } else {
                            filters = filters + " AND ";
                        }
                        filters = filters + " GRP_ORG_ENTITY_ID=" + metaData[1] + " AND " + "GRP_VERSION_NUMBER="
                                + metaData[3] + " AND " + "GRP_Period_ID=" + metaData[4] + " AND "
                                + "GRP_REG_REPORT_VERSION=" + metaData[2];
                        grpDetails.getBasePrimaryColumns().put("GROUP_BY_DIMENSION", "GROUP_BY_DIMENSION");
                        grpDetails.getBasePrimaryColumns().put("LINE_ITEM_MAP_ID", "LINE_ITEM_MAP_ID");
                        grpDetails.getBasePrimaryColumns().put("LINE_ITEM_ID", "LINE_ITEM_ID");
                        grpDetails.setReturnType("GRID");

                    } else if (metaData[0].equalsIgnoreCase(ValidationConstants.TYPE_RETURN)) {
                        grpDetails.setReturnType("LIST");
                        selectCols.addAll(usedGroupBy);
                        selectCols.add(targetCol);

                        if (filters == null) {
                            filters = "";
                        } else {
                            filters = filters + " AND ";
                        }

                        filters = filters + "GRP_ORGENTITYID=" + metaData[1] + " AND " + "GRP_VERSIONNUMBER="
                                + metaData[3] + " AND " + "GRP_PERIODID=" + metaData[4];
                        for (String grpCol : usedGroupBy) {
                            grpDetails.getBasePrimaryColumns().put(dimensionColMap.get(tableName).get(grpCol), grpCol);
                        }
                    }
                    grpDetails.setIsNavigable(isNavigable);
                    grpDetails.setFilterCondition(filters);
                    grpDetails.setSelectCols(new ArrayList<>(selectCols));
                    grpDetails.setSelectQuery(tableQueryInfo.get(tableName));
                    grpDetails.setEntityName(returnEntityNameInfo.get(tableName));
                    grpDetails.setFormName(returnFormInfo.get(tableName));
                    grpDetails.setReportId((Integer) this.tableMetadata.get(tableName).get("reportId"));
                    grpDetails.setSectionId((Integer) this.tableMetadata.get(tableName).get("sectionId"));
                    grpDetails.setReportName((String) this.tableMetadata.get(tableName).get("reportName"));
                    grpDetails.setSectionDesc((String) this.tableMetadata.get(tableName).get("sectionDesc"));
                    grpDetails.setLineItemCode(
                            aliaisedLineColumnData.get(tableName).get(targetCol).get("BUSSINESS_NAME"));
                    grpDetails.setLineItemDesc(aliaisedLineColumnData.get(tableName).get(targetCol).get("DESC"));

                    grpDetails.setPeriodId(Integer.parseInt(metaData[4]));
                    grpDetails.setRegReportVersion(Integer.parseInt(metaData[2]));
                    grpDetails.setVersionNo(Integer.parseInt(metaData[3]));
                    grpDetails.setOrgId(Integer.parseInt(metaData[1]));
                    grpDetails.setTargetCol(targetCol);
                }
            } else {

                if (groupByColumns.size() == 1
                        && groupByColumns.get(0).trim().equalsIgnoreCase(ValidationConstants.NOGROUPBY_INDICATOR)
                        && dimensionColMap.get(tableName) != null && dimensionColMap.get(tableName).size() == 0) {
                    isNavigable = Boolean.TRUE;
                } else if (groupByColumns.size() == 1
                        && groupByColumns.get(0).trim().equalsIgnoreCase(ValidationConstants.NOGROUPBY_INDICATOR)
                        && dimensionColMap.get(tableName) != null && dimensionColMap.get(tableName).size() != 0) {
                    isNavigable = Boolean.FALSE;
                } else {

                    if (groupByColumns.size() > 0) {
                        for (String condition : groupByColumns) {
                            usedGroupBy.add(condition.replace(tableName + ".", ""));
                        }
                    }
                    entityGroupBy.addAll(dimensionColMap.get(tableName).keySet());

                    if (entityGroupBy.size() == usedGroupBy.size()) {
                        usedGroupBy.retainAll(entityGroupBy);
                        if (entityGroupBy.size() == usedGroupBy.size()) {
                            isNavigable = Boolean.TRUE;
                        }
                    } else {
                        isNavigable = Boolean.FALSE;
                    }
                }

                // Foreach group by TODO

                if (isNavigable) {
                    for (String col : usedGroupBy) {
                        if (filters == null) {
                            filters = "";
                        } else {
                            filters = filters + " AND ";
                        }
                        filters = filters + "GRP_" + col + " = " + col + " ";
                    }

                    if (tableMetadata.containsKey(tableName) && !tableMetadata.get(tableName).isEmpty()
                            && Objects.equals(tableMetadata.get(tableName).get("isGrid"), Boolean.TRUE)) {
                        if (targetCol.startsWith("A_")) {
                            selectCols.add(targetCol.replace("A_", "") + " AS LINE_ITEM_ID");
                            selectCols.add(targetCol);
                        } else {
                            isNavigable = Boolean.FALSE;
                        }
                        selectCols.add("GROUP_BY_DIMENSION");
                        selectCols.add("LINE_ITEM_MAP_ID");

                        if (filters == null) {
                            filters = "";
                        } else {
                            filters = filters + " AND ";
                        }
                        filters = filters + " GRP_ORG_ENTITY_ID=" + metaData[1] + " AND " + "GRP_VERSION_NUMBER="
                                + metaData[3] + " AND " + "GRP_Period_ID=" + metaData[4] + " AND "
                                + "GRP_REG_REPORT_VERSION=" + metaData[2];
                        grpDetails.getBasePrimaryColumns().put("GROUP_BY_DIMENSION", "GROUP_BY_DIMENSION");
                        grpDetails.getBasePrimaryColumns().put("LINE_ITEM_MAP_ID", "LINE_ITEM_MAP_ID");
                        grpDetails.getBasePrimaryColumns().put("LINE_ITEM_ID", "LINE_ITEM_ID");
                        grpDetails.setReturnType("GRID");

                    } else if (metaData[0].equalsIgnoreCase(ValidationConstants.TYPE_RETURN)) {
                        grpDetails.setReturnType("LIST");
                        selectCols.addAll(usedGroupBy);
                        selectCols.add(targetCol);

                        if (filters == null) {
                            filters = "";
                        } else {
                            filters = filters + " AND ";
                        }

                        filters = filters + "GRP_ORGENTITYID=" + metaData[1] + " AND " + "GRP_VERSIONNUMBER="
                                + metaData[3] + " AND " + "GRP_PERIODID=" + metaData[4];
                        for (String grpCol : usedGroupBy) {
                            grpDetails.getBasePrimaryColumns().put(dimensionColMap.get(tableName).get(grpCol), grpCol);
                        }
                    }
                    grpDetails.setIsNavigable(isNavigable);
                    grpDetails.setFilterCondition(filters);
                    grpDetails.setSelectCols(new ArrayList<>(selectCols));
                    grpDetails.setSelectQuery(tableQueryInfo.get(tableName));
                    grpDetails.setEntityName(returnEntityNameInfo.get(tableName));
                    grpDetails.setFormName(returnFormInfo.get(tableName));
                    grpDetails.setReportId((Integer) this.tableMetadata.get(tableName).get("reportId"));
                    grpDetails.setSectionId((Integer) this.tableMetadata.get(tableName).get("sectionId"));
                    grpDetails.setReportName((String) this.tableMetadata.get(tableName).get("reportName"));
                    grpDetails.setSectionDesc((String) this.tableMetadata.get(tableName).get("sectionDesc"));
                    grpDetails.setLineItemCode(
                            aliaisedLineColumnData.get(tableName).get(targetCol).get("BUSSINESS_NAME"));
                    grpDetails.setLineItemDesc(aliaisedLineColumnData.get(tableName).get(targetCol).get("DESC"));

                    grpDetails.setPeriodId(Integer.parseInt(metaData[4]));
                    grpDetails.setRegReportVersion(Integer.parseInt(metaData[2]));
                    grpDetails.setVersionNo(Integer.parseInt(metaData[3]));
                    grpDetails.setOrgId(Integer.parseInt(metaData[1]));
                    grpDetails.setTargetCol(targetCol);
                }

            }
            // check if group by matches

        }

        if (isNavigable) {
            expressionMetaData.getGroupByDetailsBySubExpr().put(nodeName, grpDetails);
        }
    }

    public String countFunctionBuilder(String tableMdata, List<String> outputColumns, List<String> groupByColumns,
                                       List<String> filterCondition, QueryBuilder builder, String nodeName, Map<String, String> columnMap,
                                       String funcName) {
        nodeName = nodeName.trim();
        //this.outputColumns.add(nodeName);

        String[] metaData = tableMdata.split("#");
        String tableName = tableMdata.split("#")[metaData.length - 1];
        String tableAliasName = tableMdata.split("#")[metaData.length - 1];
        if (metaData[0].equalsIgnoreCase(ME)) {
            metaData = builder.getMetaData();
        }

        columnMap.put(nodeName, nodeName);

        for (String outputCol : outputColumns) {
            /*outputCol = outputCol.trim();
            String[] outCol = outputCol.split("\\.");*/
            isNavigable(tableName, builder, groupByColumns, filterCondition, metaData,
                    outputCol.replace(tableName + ".", ""), nodeName);
        }

        if (tableName.equalsIgnoreCase(ME)) {
            tableName = builder.getBaseTableName();
        }

        String selectQuery = null;
        if (builder.getQuery() != null && builder.getQuery().trim().length() > 0) {
            selectQuery = "select " + countSelectBuilder(funcName, outputColumns, groupByColumns, builder) + " as "
                    + nodeName + aggregateSelectBuilder(tableAliasName, groupByColumns, builder) + " from " + tableName
                    + " t2 " + aggregateFilterBuilder(filterCondition, tableAliasName, builder, metaData)
                    + aggregateGroupByBuilder(tableAliasName, groupByColumns, builder);
            selectQuery = "select t1.*,t2." + nodeName + " from ( " + builder.getQuery() + ") t1 left outer join ("
                    + selectQuery + ") t2" + aggregateJoinBuilder(tableAliasName, groupByColumns, builder);
        } else {
            selectQuery = "select " + countSelectBuilder(funcName, outputColumns, groupByColumns, builder) + " as "
                    + nodeName + aggregateSelectBuilder(tableAliasName, groupByColumns, builder) + " from " + tableName
                    + " t2 " + aggregateFilterBuilder(filterCondition, tableAliasName, builder, metaData)
                    + aggregateGroupByBuilder(tableAliasName, groupByColumns, builder);
        }

        builder.setQuery(selectQuery);
        return nodeName;

    }

    private String countSelectBuilder(String funcName, List<String> outputColumns, List<String> groupByColumns,
                                      QueryBuilder builder) {
        String selectCount = "";
        String colString = null;
        String patternString = "[\\s]+(?i)AS[\\s]+";
        Pattern pattern = Pattern.compile(patternString);

        for (String outputCol : outputColumns) {
            outputCol = outputCol.trim();
            String[] outCol = outputCol.split("\\.");
            outputCol = "t2." + outCol[outCol.length - 1].trim();

            Matcher matcher = pattern.matcher(outputCol);
            boolean matches = matcher.find();
            if (matches) {
                outputCol = outputCol.split("[\\s]+(?i)AS[\\s]+")[0];
            }

            if (colString == null) {
                colString = "";
            } else {
                if (ApplicationProperties.getValue("app.martDBType").equalsIgnoreCase("oracle")) {
                    colString = colString + "||'~~'||";
                } else {
                    colString = colString + "+'~~'+";
                }
            }
            if (ApplicationProperties.getValue("app.martDBType").equalsIgnoreCase("oracle")) {
                colString = colString + "TO_CHAR(" + outputCol + ")";
            } else {
                colString = colString + "CONVERT(varchar(4000)," + outputCol + ")";
            }
        }

        if (funcName.equalsIgnoreCase("COUNT") || funcName.equalsIgnoreCase("COUNTIF")) {
            selectCount = " COUNT(" + colString + ") ";
        } else if (funcName.equalsIgnoreCase("DCOUNT")) {
            selectCount = " COUNT( distinct " + colString + ") ";
        } else if (funcName.equalsIgnoreCase("UNIQUE")) {
            if (ApplicationProperties.getValue("app.martDBType").trim().equalsIgnoreCase("MSSQL")) {
                selectCount = "CAST(CASE WHEN COUNT( distinct " + colString + ") =1 THEN 1 ELSE 0 END AS bit )";
            } else {
                selectCount = "CAST(CASE WHEN COUNT( distinct " + colString + ") =1 THEN 1 ELSE 0 END AS NUMBER(1) )";
            }

        }

        return selectCount;
    }

    public ExpressionProcessor initExpressionProcessor(String query, Integer systemSolutionId, Set<String> columnsList, String expr,
                                                       ExpressionParser expressionParser, Set<String> tablesList,
                                                       Map<String, String> groupByCols, Integer exprId, Map<String, Integer> columnMetaData,
                                                       StringBuilder dimensionsCSV, Integer runId, ExpressionProcessor expressionProcessor,
                                                       Map<String, String> columnData) {
        if (expressionMetaData.getIsNavigable()) {
            if (expressionMetaData.getReturnType().equalsIgnoreCase("GRID")) {
                Matcher m = gridLineColumn.matcher(expr);
                while (m.find()) {
                    expressionMetaData.getEntityCols().add(m.group(0).replace(ME + "." + "A_", "A_"));
                    expressionMetaData.getColumnInfo().put(m.group(0).replace(ME + "." + "A_", "A_"),
                            aliaisedLineColumnData.get(expressionMetaData.getBaseTableName()).get(m.group(0).replace(ME + ".", "")));
                }
            } else {
                if (expressionMetaData.getIsForEachPresent()) {
                    String temExpr = expr;
                    List<String> sortedStr = new ArrayList<>(aliaisedLineColumnData.get(expressionMetaData.getBaseTableName()).keySet());
                    sortedStr.sort(Collections.reverseOrder(Comparator.comparingInt(o -> o != null ? o.trim().length() : 0)));

                    for (String key : sortedStr) {
                        if (temExpr.contains(ME + "." + key)) {
                            temExpr = temExpr.replace(ME + "." + key, "");
                            expressionMetaData.getEntityCols().add(key);
                            expressionMetaData.getColumnInfo().put(key, aliaisedLineColumnData.get(expressionMetaData.getBaseTableName()).get(key));
                        }
                    }
                }
                // add logic;
            }
        }

        SpelExpressionParser parser = new SpelExpressionParser();

        List<String> sortedColumnList = new ArrayList<>(columnsList);
        sortedColumnList.sort(Collections.reverseOrder(Comparator.comparingInt(o -> o != null ? o.trim().length() : 0)));

        List<String> sortedTablesList = new ArrayList<>(tablesList);
        sortedTablesList.sort(Collections.reverseOrder(Comparator.comparingInt(o -> o != null ? o.trim().length() : 0)));

        if (expressionMetaData.getIsNavigable()) {
            List<String> sortedCols = new ArrayList<>();
            if (expressionMetaData.getReturnType().equalsIgnoreCase("GRID")) {
                Set<String> columns = new HashSet<>();
                columns.add("PERIOD_ID");
                columns.add("VERSION_NUMBER");
                columns.add("ORG_ENTITY_ID");
                columns.add("REG_REPORT_VERSION");
                columns.add("GROUP_BY_DIMENSION");
                columns.add("LINE_ITEM_MAP_ID");

                columns.addAll(groupByCols.keySet());

                String exprCopy = expr;
                for (String col : sortedColumnList) {
                    if (exprCopy.contains(col)) {
                        columns.add(col);
                        exprCopy = exprCopy.replace(col, "--");
                    }
                }


                sortedCols.addAll(columns);
                sortedCols.sort(Collections.reverseOrder(Comparator.comparingInt(o -> o != null ? o.trim().length() : 0)));
            } else if (expressionMetaData.getIsForEachPresent()) {
                Set<String> columns = new HashSet<>();

                try {
                    columns.addAll(expressionMetaData.getBasePrimaryColumns().keySet());
                    columns.addAll(groupByCols.keySet());

                } catch (Exception e) {
                    LOGGER.info("--");
                }
                String exprCopy = expr;
                for (String col : sortedColumnList) {
                    if (exprCopy.contains(col)) {
                        columns.add(col);
                        exprCopy = exprCopy.replace(col, "--");
                    }
                }

                sortedCols.addAll(columns);
                sortedCols.sort(Collections.reverseOrder(Comparator.comparingInt(o -> o != null ? o.trim().length() : 0)));
            } else {
                LOGGER.info("Navigable Special " + exprId + " :: type :" + expressionMetaData.getReturnType() + ",expr: " + expr + ",groupByCols:"
                        + groupByCols + ", sortedColumnList:" + sortedColumnList + ", pk:"
                        + expressionMetaData.getBasePrimaryColumns());
            }
            StringBuffer selectQuery = null;

            for (String col : sortedCols) {
                if (selectQuery == null) {
                    selectQuery = new StringBuffer("SELECT ");
                } else {
                    selectQuery.append(" , ");
                }
                selectQuery.append(col);
            }
            if (selectQuery != null && !isSparkEnabled) {
                selectQuery.append(" FROM ( ");
                selectQuery.append(query);
                selectQuery.append(" ) ta1");
                query = selectQuery.toString();

            }
        } else {
            LOGGER.info(exprId + " :: type :" + expressionMetaData.getReturnType() + ",expr: " + expr + ",groupByCols:"
                    + groupByCols + ", sortedColumnList:" + sortedColumnList + ", pk:"
                    + expressionMetaData.getBasePrimaryColumns());
        }
        expressionProcessor.init(query, systemSolutionId, sortedColumnList, expr, parser,
                ExpressionEvaluatorContext.context, expressionParser, sortedTablesList, groupByCols, exprId, columnMetaData,
                expressionMetaData, dimensionsCSV, runId, columnData);
        return expressionProcessor;

    }

}