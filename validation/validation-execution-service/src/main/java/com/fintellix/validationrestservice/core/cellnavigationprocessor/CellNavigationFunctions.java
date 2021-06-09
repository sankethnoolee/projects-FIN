package com.fintellix.validationrestservice.core.cellnavigationprocessor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fintellix.validationrestservice.definition.ExpressionMetaData;
import com.fintellix.validationrestservice.definition.QueryBuilder;
import com.fintellix.validationrestservice.definition.SubExpressionMetaData;
import com.fintellix.validationrestservice.definition.ValidationConstants;
import com.fintellix.validationrestservice.util.connectionManager.PersistentStoreManager;

public class CellNavigationFunctions {

	private final Logger LOGGER = LoggerFactory.getLogger(getClass());

	private static final String ME = ValidationConstants.ME;
	private Map<String, Map<String, Object>> tableMetadata;
	private Map<String, Map<String, String>> dimensionColMap;
	private Map<String, String> tableQueryInfo;
	private Map<String, String> returnFormInfo = new ConcurrentHashMap<>();
	private Map<String, String> returnEntityNameInfo = new ConcurrentHashMap<>();
	private Map<String, String> returnTableLink = new ConcurrentHashMap<>();
	private ExpressionMetaData expressionMetaData = new ExpressionMetaData();

	private Map<String, Map<String, Map<String, String>>> aliaisedLineColumnData;

	private Pattern gridLineColumn = Pattern.compile("ME.A_?+[0-9]+");

	private Connection deleteConn = null;
	private Connection insertConn = null;

	private PreparedStatement deletePs = null;
	private PreparedStatement insertPs = null;
	private Integer insertCounter = 0;
	private Integer deleteCounter = 0;
	private Integer exprId;
	private Integer sequenceNo;
	private Integer solId;

	private Map<String, String> lineColumnMap = new ConcurrentHashMap<>();

	public void init(Map<String, Map<String, Object>> tableMetadata, Map<String, Map<String, String>> dimensionColMap,
			Map<String, String> tableQueryInfo, Map<String, String> returnTableLink,
			Map<String, String> returnEntityNameInfo, Map<String, String> returnFormInfo,
			Map<String, Map<String, Map<String, String>>> aliaisedLineColumnData, Integer exprId, Integer solId,
			Integer sequenceNo) {
		this.tableMetadata = tableMetadata;
		this.dimensionColMap = dimensionColMap;
		this.tableQueryInfo = tableQueryInfo;
		this.returnEntityNameInfo = returnEntityNameInfo;
		this.returnFormInfo = returnFormInfo;
		this.returnTableLink = returnTableLink;
		this.aliaisedLineColumnData = aliaisedLineColumnData;
		this.exprId = exprId;
		this.sequenceNo = sequenceNo;
		this.solId = solId;

		try {
			deleteConn = PersistentStoreManager.getConnection();
			insertConn = PersistentStoreManager.getConnection();

			deletePs = deleteConn.prepareStatement("DELETE FROM VALIDATION_LINE_ITEM_LINK " + "WHERE "
					+ "VALIDATION_ID =? AND SEQUENCE_NO =? AND SOLUTION_ID =? AND REG_REPORT_ID =? AND SECTION_ID =? AND LINE_ITEM_NAME =?");
			insertPs = insertConn.prepareStatement("INSERT INTO VALIDATION_LINE_ITEM_LINK ("
					+ "VALIDATION_ID,SEQUENCE_NO,SOLUTION_ID,REG_REPORT_ID,SECTION_ID,LINE_ITEM_NAME" + ") VALUES ("
					+ "?,?,?,?,?,?" + ")");
		} catch (Throwable e) {
			// do-nothing
		}

	}

	public void delete() {
		try {
			if (deleteCounter > 0) {

				deletePs.executeBatch();
				deleteConn.commit();
				insert();

			}
		} catch (SQLException e) {
			try {
				deleteConn.rollback();
			} catch (SQLException e1) {
				// do-nothing
			}
		} finally {
			closeConn();
		}
	}

	private void insert() {
		if (insertCounter > 0) {
			try {
				insertPs.executeBatch();
				insertConn.commit();
			} catch (SQLException e) {
				// do-nothing
			}

		}
	}

	public void closeConn() {

		if (deletePs != null) {
			try {
				deletePs.close();
			} catch (SQLException e) {
				// do-nothing
			}
		}

		if (insertPs != null) {
			try {
				insertPs.close();
			} catch (SQLException e) {
				// do-nothing
			}
		}

		if (deleteConn != null) {
			try {
				deleteConn.close();
			} catch (SQLException e) {
				// do-nothing
			}
		}

		if (insertConn != null) {
			try {
				insertConn.close();
			} catch (SQLException e) {
				// do-nothing
			}
		}

	}
	/*
	 * FOREACH
	 */

	public void forEachBuilder(String tableMetadata, List<String> groupByColumns, List<String> filters,
			QueryBuilder builder) {

		String[] metaData = tableMetadata.split("#");
		String tableName = tableMetadata.split("#")[metaData.length - 1];

		String query = "select * from " + tableName + " t1 ";
		String groupBy = "";
		String where = null;

		for (String filter : filters) {
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
			}
		}

		if (where == null) {
			where = "";
		}

		// is Navigable
		if (metaData.length > 1) {
			if (metaData[0].equalsIgnoreCase(ValidationConstants.TYPE_RETURN)) {
				List<String> sourceGrpCols = new ArrayList<>();
				List<String> baseGrpCols = new ArrayList<>();
				// sourceGrpCols.addAll(groupByColumns);
				for (String col : groupByColumns) {
					sourceGrpCols.add(col.replaceAll(tableName + ".", ""));
				}
				if (dimensionColMap.get(tableName) == null) {
					LOGGER.warn("meta data info not available for validation id :" + exprId + ", sequence no :"
							+ sequenceNo);
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
						expressionMetaData.getBasePrimaryColumns().put(grpCol,
								dimensionColMap.get(tableName).get(grpCol));
					}
					expressionMetaData.setReturnType("LIST");
				}

			}
		}

		expressionMetaData.setReportId((Integer) this.tableMetadata.get(tableName).get("reportId"));
		expressionMetaData.setSectionId((Integer) this.tableMetadata.get(tableName).get("sectionId"));
		expressionMetaData.setReportName((String) this.tableMetadata.get(tableName).get("reportName"));
		expressionMetaData.setSectionDesc((String) this.tableMetadata.get(tableName).get("sectionDesc"));
		expressionMetaData.setEntityName(returnEntityNameInfo.get(tableName));
		expressionMetaData.setFormName(returnFormInfo.get(tableName));
		expressionMetaData.setBaseTableName(tableName);
		expressionMetaData.setIsForEachPresent(Boolean.TRUE);
		expressionMetaData.setPeriodId(Integer.parseInt(metaData[4]));
		expressionMetaData.setRegReportVersion(Integer.parseInt(metaData[2]));
		expressionMetaData.setVersionNo(Integer.parseInt(metaData[3]));
		expressionMetaData.setOrgId(Integer.parseInt(metaData[1]));

		if (!groupByColumns.isEmpty()) {
			Integer counter = 0;
			query = "";

			// TODO add list logic handle primary/others/group
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
					} else if (aliaisedLineColumnData.get(tableName).get(col).get("DATA_TYPE")
							.equalsIgnoreCase("STRING")) {
						query = query + "max(" + col + ")" + " as " + col + " ";
					} else if (aliaisedLineColumnData.get(tableName).get(col).get("DATA_TYPE")
							.equalsIgnoreCase("NUMBER")) {
						query = query + "sum(" + col + ")" + " as " + col + " ";
					}
					counter++;
				}

				if (query.length() > 0) {
					query = query + ",";
				} else {
					query = " select ";
				}

				query = query
						+ "MAX(ORG_ENTITY_ID) as ORG_ENTITY_ID ,MAX(VERSION_NUMBER) as VERSION_NUMBER,MAX(Period_ID) as Period_ID,MAX(REG_REPORT_VERSION) as REG_REPORT_VERSION,MAX(GROUP_BY_DIMENSION) as GROUP_BY_DIMENSION,MAX(LINE_ITEM_MAP_ID) as LINE_ITEM_MAP_ID";

			} else {
				List<String> listPrimaryCols = new ArrayList<>();
				listPrimaryCols.addAll(dimensionColMap.get(tableName).keySet());
				List<String> allListCols = new ArrayList<>();
				allListCols.addAll(aliaisedLineColumnData.get(tableName).keySet());
				counter = 0;

				// fetch othercols how??

				for (String cols : groupByColumns) {
					String groubyColumn = cols.split("\\.")[1];

					allListCols.remove(groubyColumn);
					listPrimaryCols.remove(groubyColumn);
				}

				for (String pkCol : listPrimaryCols) {
					allListCols.remove(pkCol);
				}

				// builiding query
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

				// adding all columns
				for (String col : allListCols) {
					if (counter > 0) {
						query = query + ",";
					} else {
						query = " select ";
					}
					if (aliaisedLineColumnData.get(tableName).get(col).get("DATA_TYPE").equalsIgnoreCase("DATE")) {
						query = query + "max(" + col + ")" + " as " + col + " ";
					} else if (aliaisedLineColumnData.get(tableName).get(col).get("DATA_TYPE")
							.equalsIgnoreCase("STRING")) {
						query = query + "max(" + col + ")" + " as " + col + " ";
					} else if (aliaisedLineColumnData.get(tableName).get(col).get("DATA_TYPE")
							.equalsIgnoreCase("NUMBER")) {
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
				query = query + col.replaceAll(tableName + ".", "");

				groupBy = groupBy + " " + col.replaceAll(tableName + ".", "");

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

	public String lookUpBuilder(String source, String target, List<String> condition, List<String> outputColumns,
			List<String> filterClause, QueryBuilder builder, String nodeName, Map<String, String> columnMap) {

		String tName = "";
		String[] metaData;

		metaData = target.split("#");
		tName = metaData[metaData.length - 1];

		String tableName;
		if (tName.equalsIgnoreCase(ME)) {
			tableName = builder.getBaseTableName();
		} else {
			tableName = tName;
		}
		if (metaData[0].equalsIgnoreCase(ME)) {
			metaData = builder.getMetaData();
		}

		for (String outputCol : outputColumns) {

//        	VALIDATION_ID,SEQUENCE_NO,SOLUTION_ID,REG_REPORT_ID,SECTION_ID,LINE_ITEM_NAME

			outputCol = outputCol.trim();

			String[] outCol = outputCol.split("\\.");

			if (metaData[0].equalsIgnoreCase(ValidationConstants.TYPE_RETURN)) {
				try {
					String lineColKey = "";

					Integer deleteParamCounter = 1;
					Integer insertParamCounter = 1;
					deletePs.clearParameters();
					insertPs.clearParameters();

					deletePs.setInt(deleteParamCounter++, exprId);
					deletePs.setInt(deleteParamCounter++, sequenceNo);
					deletePs.setInt(deleteParamCounter++, solId);
					deletePs.setInt(deleteParamCounter++, (Integer) this.tableMetadata.get(tableName).get("reportId"));
					deletePs.setInt(deleteParamCounter++, (Integer) this.tableMetadata.get(tableName).get("sectionId"));

					insertPs.setInt(insertParamCounter++, exprId);
					insertPs.setInt(insertParamCounter++, sequenceNo);
					insertPs.setInt(insertParamCounter++, solId);
					insertPs.setInt(insertParamCounter++, (Integer) this.tableMetadata.get(tableName).get("reportId"));
					insertPs.setInt(insertParamCounter++, (Integer) this.tableMetadata.get(tableName).get("sectionId"));

					if (this.tableMetadata.containsKey(tableName) && !this.tableMetadata.get(tableName).isEmpty()
							&& Objects.equals(this.tableMetadata.get(tableName).get("isGrid"), Boolean.TRUE)) {
						deletePs.setString(deleteParamCounter++, outCol[outCol.length - 1].replace("A_", ""));
						insertPs.setString(insertParamCounter++, outCol[outCol.length - 1].replace("A_", ""));

						lineColKey = exprId + "_" + sequenceNo + "_" + solId + "_"
								+ (Integer) this.tableMetadata.get(tableName).get("reportId") + "_"
								+ this.tableMetadata.get(tableName).get("sectionId") + "_"
								+ outCol[outCol.length - 1].replace("A_", "");
					} else {
						deletePs.setString(deleteParamCounter++, outCol[outCol.length - 1].toUpperCase());
						insertPs.setString(insertParamCounter++, outCol[outCol.length - 1].toUpperCase());
						lineColKey = exprId + "_" + sequenceNo + "_" + solId + "_"
								+ (Integer) this.tableMetadata.get(tableName).get("reportId") + "_"
								+ this.tableMetadata.get(tableName).get("sectionId") + "_"
								+ outCol[outCol.length - 1].toUpperCase();
					}

					if (lineColumnMap.get(lineColKey) == null) {
						deletePs.addBatch();
						insertPs.addBatch();

						insertCounter++;
						deleteCounter++;
						lineColumnMap.put(lineColKey, lineColKey);

					}

				} catch (Exception e) {
					// do-nothing
				}

			}
		}

		return nodeName;
	}

	/*
	 * SUMIF
	 */

	public String aggregateBuilder(String tableMetadata, List<String> groupByColumns, List<String> filterCondition,
			String outputCol, String nodeName, QueryBuilder builder, String funcName, Map<String, String> columnMap) {

//    	VALIDATION_ID,SEQUENCE_NO,SOLUTION_ID,REG_REPORT_ID,SECTION_ID,LINE_ITEM_NAME

		String[] metaData = tableMetadata.split("#");
		String tName = tableMetadata.split("#")[metaData.length - 1];

		String tableName;
		if (tName.equalsIgnoreCase(ME)) {
			tableName = builder.getBaseTableName();
		} else {
			tableName = tName;
		}
		if (metaData[0].equalsIgnoreCase(ME)) {
			metaData = builder.getMetaData();
		}

		nodeName = nodeName.trim();
		outputCol = outputCol.trim();

		String[] outCol = outputCol.split("\\.");
		columnMap.put(nodeName, outCol[outCol.length - 1]);

		if (metaData[0].equalsIgnoreCase(ValidationConstants.TYPE_RETURN)) {
			try {
				String lineColKey;
				Integer deleteParamCounter = 1;
				Integer insertParamCounter = 1;
				deletePs.clearParameters();
				insertPs.clearParameters();

				deletePs.setInt(deleteParamCounter++, exprId);
				deletePs.setInt(deleteParamCounter++, sequenceNo);
				deletePs.setInt(deleteParamCounter++, solId);
				deletePs.setInt(deleteParamCounter++, (Integer) this.tableMetadata.get(tableName).get("reportId"));
				deletePs.setInt(deleteParamCounter++, (Integer) this.tableMetadata.get(tableName).get("sectionId"));

				insertPs.setInt(insertParamCounter++, exprId);
				insertPs.setInt(insertParamCounter++, sequenceNo);
				insertPs.setInt(insertParamCounter++, solId);
				insertPs.setInt(insertParamCounter++, (Integer) this.tableMetadata.get(tableName).get("reportId"));
				insertPs.setInt(insertParamCounter++, (Integer) this.tableMetadata.get(tableName).get("sectionId"));

				if (this.tableMetadata.containsKey(tableName) && !this.tableMetadata.get(tableName).isEmpty()
						&& Objects.equals(this.tableMetadata.get(tableName).get("isGrid"), Boolean.TRUE)) {
					deletePs.setString(deleteParamCounter++, outCol[outCol.length - 1].replace("A_", ""));
					insertPs.setString(insertParamCounter++, outCol[outCol.length - 1].replace("A_", ""));
					lineColKey = exprId + "_" + sequenceNo + "_" + solId + "_"
							+ (Integer) this.tableMetadata.get(tableName).get("reportId") + "_"
							+ this.tableMetadata.get(tableName).get("sectionId") + "_"
							+ outCol[outCol.length - 1].replace("A_", "");
				} else {
					deletePs.setString(deleteParamCounter++, outCol[outCol.length - 1].toUpperCase());
					insertPs.setString(insertParamCounter++, outCol[outCol.length - 1].toUpperCase());

					lineColKey = exprId + "_" + sequenceNo + "_" + solId + "_"
							+ (Integer) this.tableMetadata.get(tableName).get("reportId") + "_"
							+ this.tableMetadata.get(tableName).get("sectionId") + "_"
							+ outCol[outCol.length - 1].toUpperCase();
				}

				if (lineColumnMap.get(lineColKey) == null) {
					deletePs.addBatch();
					insertPs.addBatch();

					insertCounter++;
					deleteCounter++;
					lineColumnMap.put(lineColKey, lineColKey);
				}

				deletePs.addBatch();
				insertPs.addBatch();

				insertCounter++;
				deleteCounter++;
			} catch (Exception e) {
				// do-nothing
			}

		}

		return nodeName;
	}

	public void addIndividualCols(String expr, QueryBuilder builder) {
		if (builder.getBaseTableName() != null && builder.getBaseTableName().trim().length() > 0) {

			String[] metaData = builder.getMetaData();
			String tName = builder.getBaseTableName();

			String tableName;
			if (tName.equalsIgnoreCase(ME)) {
				tableName = builder.getBaseTableName();
			} else {
				tableName = tName;
			}

			if (metaData[0].equalsIgnoreCase(ValidationConstants.TYPE_RETURN)) {
				try {

					if (this.tableMetadata.containsKey(tableName) && !this.tableMetadata.get(tableName).isEmpty()
							&& Objects.equals(this.tableMetadata.get(tableName).get("isGrid"), Boolean.TRUE)) {

						Matcher m = gridLineColumn.matcher(expr);
						while (m.find()) {
							String lineColKey;
							Integer deleteParamCounter = 1;
							Integer insertParamCounter = 1;
							deletePs.clearParameters();
							insertPs.clearParameters();

							deletePs.setInt(deleteParamCounter++, exprId);
							deletePs.setInt(deleteParamCounter++, sequenceNo);
							deletePs.setInt(deleteParamCounter++, solId);
							deletePs.setInt(deleteParamCounter++,
									(Integer) this.tableMetadata.get(tableName).get("reportId"));
							deletePs.setInt(deleteParamCounter++,
									(Integer) this.tableMetadata.get(tableName).get("sectionId"));

							insertPs.setInt(insertParamCounter++, exprId);
							insertPs.setInt(insertParamCounter++, sequenceNo);
							insertPs.setInt(insertParamCounter++, solId);
							insertPs.setInt(insertParamCounter++,
									(Integer) this.tableMetadata.get(tableName).get("reportId"));
							insertPs.setInt(insertParamCounter++,
									(Integer) this.tableMetadata.get(tableName).get("sectionId"));
							deletePs.setString(deleteParamCounter++, m.group(0).replaceAll(ME + "." + "A_", ""));
							insertPs.setString(insertParamCounter++, m.group(0).replaceAll(ME + "." + "A_", ""));

							lineColKey = exprId + "_" + sequenceNo + "_" + solId + "_"
									+ (Integer) this.tableMetadata.get(tableName).get("reportId") + "_"
									+ this.tableMetadata.get(tableName).get("sectionId") + "_"
									+ m.group(0).replaceAll(ME + "." + "A_", "");
							if (lineColumnMap.get(lineColKey) == null) {
								deletePs.addBatch();
								insertPs.addBatch();

								insertCounter++;
								deleteCounter++;
								lineColumnMap.put(lineColKey, lineColKey);
							}
						}

					} else {

						String temExpr = expr;
						List<String> sortedStr = new ArrayList<>(
								aliaisedLineColumnData.get(builder.getBaseTableName()).keySet());
						sortedStr.sort(Collections
								.reverseOrder(Comparator.comparingInt(o -> o != null ? o.trim().length() : 0)));

						for (String key : sortedStr) {
							if (temExpr.contains(ME + "." + key)) {
								temExpr = temExpr.replaceAll(ME + "." + key, "");
								expressionMetaData.getEntityCols().add(key);
								String lineColKey;
								Integer deleteParamCounter = 1;
								Integer insertParamCounter = 1;
								deletePs.clearParameters();
								insertPs.clearParameters();

								deletePs.setInt(deleteParamCounter++, exprId);
								deletePs.setInt(deleteParamCounter++, sequenceNo);
								deletePs.setInt(deleteParamCounter++, solId);
								deletePs.setInt(deleteParamCounter++,
										(Integer) this.tableMetadata.get(tableName).get("reportId"));
								deletePs.setInt(deleteParamCounter++,
										(Integer) this.tableMetadata.get(tableName).get("sectionId"));

								insertPs.setInt(insertParamCounter++, exprId);
								insertPs.setInt(insertParamCounter++, sequenceNo);
								insertPs.setInt(insertParamCounter++, solId);
								insertPs.setInt(insertParamCounter++,
										(Integer) this.tableMetadata.get(tableName).get("reportId"));
								insertPs.setInt(insertParamCounter++,
										(Integer) this.tableMetadata.get(tableName).get("sectionId"));
								deletePs.setString(deleteParamCounter++, key.toUpperCase());
								insertPs.setString(insertParamCounter++, key.toUpperCase());

								lineColKey = exprId + "_" + sequenceNo + "_" + solId + "_"
										+ (Integer) this.tableMetadata.get(tableName).get("reportId") + "_"
										+ this.tableMetadata.get(tableName).get("sectionId") + "_" + key.toUpperCase();
								if (lineColumnMap.get(lineColKey) == null) {
									deletePs.addBatch();
									insertPs.addBatch();

									insertCounter++;
									deleteCounter++;
									lineColumnMap.put(lineColKey, lineColKey);
								}
							}
						}
					}

				} catch (Exception e) {
					// do-nothing
				}

			}
		}
	}

	private String createFilterString(String tableAlias, String tableName, String[] metaData) {
		String filterStr = " ";

		if (tableMetadata.containsKey(tableName) && !tableMetadata.get(tableName).isEmpty()
				&& Objects.equals(tableMetadata.get(tableName).get("isGrid"), Boolean.TRUE)) {
			filterStr = " " + tableAlias + ".ORG_ENTITY_ID=" + metaData[1] + " AND " + tableAlias + ".VERSION_NUMBER="
					+ metaData[3] + " AND " + tableAlias + ".Period_ID=" + metaData[4] + " AND " + tableAlias
					+ ".REG_REPORT_VERSION=" + metaData[2];
		} else {
			filterStr = " " + tableAlias + ".ORGENTITYID=" + metaData[1] + " AND " + tableAlias + ".VERSIONNUMBER="
					+ metaData[3] + " AND " + tableAlias + ".PERIODID=" + metaData[4];
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
							usedGroupBy.add(condition.replaceAll(tableName + ".", ""));
						}
					} else if (builder.getGroupBy().size() > 0) {
						for (String condition : builder.getGroupBy()) {
							usedGroupBy.add(condition.replaceAll(builder.getBaseTableName() + ".", ""));
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
							usedGroupBy.add(condition.replaceAll(tableName + ".", ""));
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

	public ExpressionMetaData getExpressionMetaData() {
		return expressionMetaData;
	}
}