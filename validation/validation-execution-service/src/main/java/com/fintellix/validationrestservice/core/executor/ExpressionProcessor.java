/**
 * 
 */
package com.fintellix.validationrestservice.core.executor;

import java.io.File;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import com.fintellix.validationrestservice.core.datatypehandler.ExpressionDataType;
import com.fintellix.validationrestservice.core.datatypehandler.ExpressionDataTypeManager;
import com.fintellix.validationrestservice.core.parser.ExpressionParser;
import com.fintellix.validationrestservice.core.resultwriter.ExpressionResultManager;
import com.fintellix.validationrestservice.core.resultwriter.ValidationResult;
import com.fintellix.validationrestservice.definition.ExpressionMetaData;
import com.fintellix.validationrestservice.definition.SubExpressionMetaData;
import com.fintellix.validationrestservice.definition.ValidationConstants;
import com.fintellix.validationrestservice.spark.SparkJob;
import com.fintellix.validationrestservice.util.ApplicationProperties;
import com.fintellix.validationrestservice.util.ValidationStringUtils;
import com.fintellix.validationrestservice.util.connectionManager.PersistentStoreManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author sumeet.tripathi
 *
 */

@Component
@Scope("prototype")
public class ExpressionProcessor implements Callable<ExpressionStatus> {

	private static Logger LOGGER = LoggerFactory.getLogger(ExpressionProcessor.class);

	@Autowired
	private ExpressionResultManager resultWriterManager;

	@Autowired
	private ExpressionDataTypeManager expressionDataTypeManager;

	@Autowired
	private Function<String, ExpressionBatchProcessor> expressionBatchProcessorBeanFactory;

	@Autowired
	private Function<String, ValidationResult> validationResultBeanFactory;

	private Integer systemSolutionId;
	private String query;
	private List<String> columnNames;
	private Map<String, String> columnData = new HashMap<>();
	private List<String> tableNames;
	private SpelExpressionParser parser;
	private String expression;
	private StandardEvaluationContext context;
	private ExpressionParser expressionParser;
	private Map<String, String> groupByCols;
	private Integer exprId;
	private Map<String, Integer> columnMetaData;
	private ExpressionMetaData expressionMetaData;
	private Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
//	private List<RunRecordDetail> runRecordDetails;
	private StringBuilder dimensionsCSV;
	private Integer runId;
	private String outputDirectory;
	private String fileName;
	private List<String> headerColumnSequence = new ArrayList<>();
	private Boolean headersAdded = Boolean.FALSE;
	private Map<String, String> usedColumnsDataType = new HashMap<>();
	private List<String> sortedColumnName = new ArrayList<>();

	private String spelExpression;
	private String displayExpression;

	private List<String> sortedUsedColumns;
	private Map<String, String> replacedColumnMapping;

	private StringBuilder lineItemIdCSV = null;

	public Boolean hasError = Boolean.FALSE;
	
	Boolean isSparkEnabled = Boolean.FALSE;

	private ExpressionStatus status;

	private Boolean ignoreValidRows;

	@Override
	public ExpressionStatus call() throws Exception {
		Integer count = 0;
		try {
			count = getRecordCount();
			if (isSparkEnabled) {
				status.setExpression(expression);
				status.setQuery(query);
				status.setExpressionMetaData(expressionMetaData);
			
				sparkEval();
				
				if(dimensionsCSV == null) {
					dimensionsCSV = new StringBuilder();
				}
				if(lineItemIdCSV == null) {
					lineItemIdCSV = new StringBuilder();
				}
				status.setSparkMetaData(query, expression, dimensionsCSV.toString(), displayExpression, spelExpression, lineItemIdCSV.toString(), groupByCols, usedColumnsDataType, expressionMetaData,replacedColumnMapping);
				
			}else {
			
			LOGGER.info("RECORD COUNT ::" + count + "");
			if(count>0) {
				
				eval();	
			}
			
//			if (count > bucketSize) {
//				multiThreadedEval(count);
//			} else {
//				eval();
//			}
			}

		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			e.printStackTrace();
			hasError = Boolean.TRUE;
			LOGGER.error(exprId + "--" + query);
		}
		status.setTotalCount(count);
		status.setHasError(hasError);
		return status;
	}

	public ExpressionBatchProcessor getExpressionProcessorInstance(String name) {
		ExpressionBatchProcessor bean = expressionBatchProcessorBeanFactory.apply(name);
		return bean;
	}

	public ValidationResult getValidationResultInstance(String name) {
		ValidationResult bean = validationResultBeanFactory.apply(name);
		return bean;
	}

	public void init(String query, Integer systemSolutionId, List<String> columnNames, String expression,
			SpelExpressionParser parser, StandardEvaluationContext context, ExpressionParser expressionParser,
			List<String> tableNames, Map<String, String> groupByCols, Integer exprId,
			Map<String, Integer> columnMetaData, ExpressionMetaData expressionMetaData,
			StringBuilder dimensionsCSV, Integer runId, Map<String, String> columnData) {
		this.systemSolutionId = systemSolutionId;
		this.query = query;
		this.columnNames = columnNames;
		this.columnData = columnData;
		this.columnMetaData = columnMetaData;
		this.context = context;
		this.parser = parser;
		this.expression = expression;
		this.expressionParser = expressionParser;
		this.tableNames = tableNames;
		this.groupByCols = groupByCols;
		this.exprId = exprId;
		this.expressionMetaData = expressionMetaData;

		this.dimensionsCSV = dimensionsCSV;
		this.runId = runId;

		outputDirectory = ApplicationProperties.getValue("app.validations.outputDirectory").trim() + runId
				+ File.separator;
		outputDirectory = ValidationStringUtils.replace(outputDirectory, "\\", "/", -1, true);
		status = new ExpressionStatus(exprId);

		String ignore = ApplicationProperties.getValue("app.ignore.validrows").trim();

		if (ignore == null) {
			ignore = "true";
		} else if (ignore.trim().length() == 0) {
			ignore = "true";
		}
		ignoreValidRows = Boolean.parseBoolean(ignore);
		
		String sparkEnabled = ApplicationProperties.getValue("app.spark.enabled").trim();

		if (sparkEnabled == null) {
			sparkEnabled = "false";
		} else if (sparkEnabled.trim().length() == 0) {
			sparkEnabled = "false";
		}
		isSparkEnabled = Boolean.parseBoolean(sparkEnabled);
		
		try {
			File directory = new File(outputDirectory);
			if (!directory.exists()) {
				directory.mkdir();
			}
		} catch (Exception e) {
			// do-nothing
		}
		fileName = "Validation_Result_" + runId + "_" + exprId + ".csv";
	}

	public Integer getRecordCount() {

		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = PersistentStoreManager.getSolutionDBConnection(systemSolutionId);
			ps = conn.prepareStatement("select count(*) from ( " + query + ") t101");
			rs = ps.executeQuery();
			if (rs.next()) {
				return rs.getInt(1);
			}

		} catch (Exception e) {
			LOGGER.error(
					exprId + "\n" + expression + " :: failed Query: select count(*) from ( " + query + ") as t101");
			e.printStackTrace();
			hasError = Boolean.TRUE;
		} catch (Throwable e) {
			LOGGER.error(
					exprId + "\n" + expression + " :: failed Query: select count(*) from ( " + query + ") as t101");
			e.printStackTrace();
			hasError = Boolean.TRUE;
		} finally {

			try {
				if (rs != null) {
					rs.close();
				}
			} catch (Exception e) {
				// do-nothing
			}

			try {
				if (ps != null) {
					ps.close();
				}
			} catch (Exception e) {
				// do-nothing
			}
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (Exception e) {
				// do-nothing
			}
		}
		return null;
	}

	public void eval() {

		LOGGER.info(exprId + " Execution started");
		long startTime = System.currentTimeMillis();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		ResultSetMetaData rsData = null;

		List<Map<String, Object>> rows = new ArrayList<>();
		try {
			conn = PersistentStoreManager.getSolutionDBConnection(systemSolutionId);
			ps = conn.prepareStatement(query);
			rs = ps.executeQuery();
			rsData = rs.getMetaData();
			Map<String, String> columnNameMap = new HashMap<>();
			for (int i = 0; i < rsData.getColumnCount(); i++) {
				columnNameMap.put(rsData.getColumnLabel(i + 1), rsData.getColumnLabel(i + 1));
				headerColumnSequence.add(rsData.getColumnLabel(i + 1));
				sortedColumnName.add(rsData.getColumnLabel(i + 1));
			}
			sortedColumnName
					.sort(Collections.reverseOrder(Comparator.comparingInt(o -> o != null ? o.trim().length() : 0)));

			//

			populateColumnsUsedInFinalExpr(columnNameMap);

			createPreFormattedSpeLExpression();
			ValidationResult validationResult = getValidationResultInstance(
					System.currentTimeMillis() + "_" + Math.random() + "_" + exprId);

			if (rs.next()) {
				Map<String, Object> row = processRow(columnNameMap, rs);
				validationResult.init(headerColumnSequence, columnNameMap, exprId, runId);
				if(ignoreValidRows && Boolean.parseBoolean(row.get("Validation").toString())) {
					//do-nothing
				}else {
					resultWriterManager.writeRow(validationResult, row);	
				}
				
				
				row.clear();

			}
			while (rs.next()) {
				Map<String, Object> row = processRow(columnNameMap, rs);
//				rows.add(row);
				if(ignoreValidRows && Boolean.parseBoolean(row.get("Validation").toString())) {
					//do-nothing
				}else {
					resultWriterManager.writeRow(validationResult, row);	
				}
				
				row.clear();
			}

			long csvCreationTime = System.currentTimeMillis();
//			resultWriterManager.write(exprId, runId, rows, columnNameMap, headerColumnSequence);
			resultWriterManager.writeFile(validationResult);
			LOGGER.info(exprId + " validation Execution time:" + ((System.currentTimeMillis() - startTime) / 1000));
			rows.clear();
			LOGGER.info(exprId + " Total CSV Creation time:" + ((System.currentTimeMillis() - csvCreationTime) / 1000));
		} catch (Exception e) {
			rows.clear();
			LOGGER.error(exprId+ "failed Query " + query);
			e.printStackTrace();
			hasError = Boolean.TRUE;
		} catch (Throwable e) {
			LOGGER.error(exprId + "failed Query " + query);
			rows.clear();
			e.printStackTrace();
			hasError = Boolean.TRUE;
		} finally {

			try {
				if (rs != null) {
					rs.close();
				}
			} catch (Exception e) {
				// do-nothing
			}

			try {
				if (ps != null) {
					ps.close();
				}
			} catch (Exception e) {
				// do-nothing
			}
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (Exception e) {
				// do-nothing
			}
			LOGGER.info(
					exprId + " Total validation Processing time:" + ((System.currentTimeMillis() - startTime) / 1000));
			LOGGER.info(exprId + " Execution ended");
		}

	}
	
	private void sparkEval() {


		LOGGER.info(exprId + " metadata gen for SPARK started");
		long startTime = System.currentTimeMillis();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		ResultSetMetaData rsData = null;

		try {
			conn = PersistentStoreManager.getSolutionDBConnection(systemSolutionId);
			ps = conn.prepareStatement("select * from ("+query+") t1 where 1=2");
			rs = ps.executeQuery();
			rsData = rs.getMetaData();
			Map<String, String> columnNameMap = new HashMap<>();
			for (int i = 0; i < rsData.getColumnCount(); i++) {
				columnNameMap.put(rsData.getColumnLabel(i + 1), rsData.getColumnLabel(i + 1));
				headerColumnSequence.add(rsData.getColumnLabel(i + 1));
				sortedColumnName.add(rsData.getColumnLabel(i + 1));
			}
			sortedColumnName
					.sort(Collections.reverseOrder(Comparator.comparingInt(o -> o != null ? o.trim().length() : 0)));

			//

			populateColumnsUsedInFinalExpr(columnNameMap);

			createPreFormattedSpeLExpression();
			
			
			LOGGER.info(exprId + " Total SPARK metadata Creation time:" + ((System.currentTimeMillis() - startTime) / 1000));
		} catch (Exception e) {
			LOGGER.error(exprId+ "failed Query " + query);
			e.printStackTrace();
			hasError = Boolean.TRUE;
		} catch (Throwable e) {
			LOGGER.error(exprId + "failed Query " + query);
			e.printStackTrace();
			hasError = Boolean.TRUE;
		} finally {

			try {
				if (rs != null) {
					rs.close();
				}
			} catch (Exception e) {
				// do-nothing
			}

			try {
				if (ps != null) {
					ps.close();
				}
			} catch (Exception e) {
				// do-nothing
			}
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (Exception e) {
				// do-nothing
			}
			LOGGER.info(
					exprId + " Total validation Processing time:" + ((System.currentTimeMillis() - startTime) / 1000));
			LOGGER.info(exprId + " Execution ended");
		}

		
	}

	private void createPreFormattedSpeLExpression() {
		spelExpression = expression;
		displayExpression = expression;
		replacedColumnMapping = new HashMap<>();
		for (String columnName : sortedColumnName) {
			String uuid = UUID.randomUUID().toString().replaceAll("-", "_");
			if(isSparkEnabled) {
				uuid =  expressionParser.getColumnUUID(columnName);
			}
			spelExpression = expressionDataTypeManager.getReplacedSpelExpression(columnName, spelExpression,
					usedColumnsDataType.get(columnName), uuid);
			replacedColumnMapping.put(columnName, uuid);
		}

		for (String tableName : tableNames) {
			spelExpression = ValidationStringUtils.replace(spelExpression, tableName + ".", "", -1, true);
			displayExpression = ValidationStringUtils.replace(displayExpression, tableName + ".", "", -1, true);
		}

		// handling ME table name
		spelExpression = ValidationStringUtils.replace(spelExpression, ValidationConstants.ME + ".", "", -1, true);
		displayExpression = ValidationStringUtils.replace(displayExpression, ValidationConstants.ME + ".", "", -1,
				true);

		spelExpression = expressionParser.convertIntoSpELExpression("(" + spelExpression + ")");

		//

	}

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

	public Map<String, Object> processRow(Map<String, String> columnNameMap, ResultSet rs) throws Throwable {

		String displayExpr = displayExpression;
		String spelExpressionCopy = spelExpression;

//		RunRecordDetail runRecordDetail = new RunRecordDetail();

		Map<String, Object> row = new HashMap<>();

		for (String columnName : sortedColumnName) {
			try {
				if(usedColumnsDataType.get(columnName) !=null && usedColumnsDataType.get(columnName).equalsIgnoreCase(ExpressionDataType.BIGDECIMAL.getValue())) {
					row.put(columnName, rs.getBigDecimal(columnName).toPlainString());
				}else {
					row.put(columnName, rs.getObject(columnName));	
				}
				
				if (sortedUsedColumns.contains(columnName)) {
					displayExpr = expressionDataTypeManager.getReplacedDisplayValue(rs.getObject(columnName),
							columnName, usedColumnsDataType.get(columnName), displayExpr);
					spelExpressionCopy = expressionDataTypeManager.getReplacedSpelValue(rs.getObject(columnName),
							replacedColumnMapping.get(columnName), usedColumnsDataType.get(columnName),
							spelExpressionCopy);
				}

			} catch (Exception e) {
				// do-nothing
			}
		}

		row.put("Evaluated Expression", displayExpr);

		String evaluatedValue;
		String evaluatedMsg = "OK";

		try {
			evaluatedValue = "" + parser.parseExpression(spelExpressionCopy).getValue(context, String.class);
		} catch (Throwable e) {

			e.printStackTrace();
			evaluatedValue = "false";
			evaluatedMsg = "Error during evaluation for SpEL Expression .";
			LOGGER.error("SpEL failed for exp with id : " + exprId);
		}

		row.put("Evaluation Message", evaluatedMsg);

		if (Boolean.parseBoolean(ApplicationProperties.getValue("app.ignore.expressionmetadata"))
				&& Boolean.parseBoolean(evaluatedValue)) {
			row.put("Expression_Meta_data", "");
		} else {
			// TODO Note May need in future to create query for underlying data
			Map<String, SubExpressionMetaData> groupDetails = expressionMetaData.getGroupByDetailsBySubExpr();
			for (String key : groupDetails.keySet()) {
				SubExpressionMetaData groupByDetailsPerSubExpr = groupDetails.get(key);
				String filter = null;
				String query = null;
				if (groupByDetailsPerSubExpr.getIsNavigable()) {
					query = "select "
							+ groupByDetailsPerSubExpr.getSelectCols().toString().replace("[", "").replace("]", "")
							+ " from (" + groupByDetailsPerSubExpr.getSelectQuery() + ") as t1";
					filter = groupByDetailsPerSubExpr.getFilterCondition();
					for (String columnName : columnNames) {
						if (columnNameMap.containsKey(columnName)) {
							if (rs.getObject(columnName) != null) {
								int type = columnMetaData.get(columnName);
								Object value;

								switch (type) {
								case Types.SMALLINT:
								case Types.INTEGER:
								case Types.TINYINT:
									value = rs.getInt(columnName);
									break;

								case Types.BIGINT:
								case Types.NUMERIC:
									value = rs.getLong(columnName);
									break;

								case Types.DECIMAL:
									value = rs.getBigDecimal(columnName);
									break;

								case Types.DOUBLE:
								case Types.FLOAT:
									value = rs.getDouble(columnName);
									break;

								case Types.CHAR:
								case Types.LONGVARCHAR:
								case Types.LONGNVARCHAR:
								case Types.NCHAR:
								case Types.NVARCHAR:
								case Types.VARCHAR:
									value = "'" + rs.getString(columnName) + "'";
									break;

								case Types.DATE:
								case Types.TIME:
								case Types.TIMESTAMP:
								case Types.TIME_WITH_TIMEZONE:
								case Types.TIMESTAMP_WITH_TIMEZONE:
									value = rs.getDate(columnName);
									break;

								default:
									value = "'" + rs.getString(columnName) + "'";
									break;
								}

								filter = filter.replaceAll(" " + columnName, " " + value.toString());
							}
						}
					}

					query = query + " WHERE " + filter;
				}

				groupByDetailsPerSubExpr.setFinalQuery(query != null ? query.replaceAll(" GRP_", " ") : "");
			}
			row.put("Expression_Meta_data", gson.toJson(expressionMetaData));
		}

		row.put("Validation", evaluatedValue);

//		runRecordDetail
//				.setValidationResult(evaluatedValue.trim().length() > 0 ? Boolean.valueOf(evaluatedValue) : false);
//		runRecordDetail.setDimensionsCSV(dimensionsCSV != null ? dimensionsCSV.toString() : "");
//		runRecordDetail.setReplacedExpression(displayExpr);

		if (!(Boolean.parseBoolean(evaluatedValue.trim()))) {
			status.incrementErrorCount();
		}

		// TODO
//		status.addRecordDetail(runRecordDetail);
		status.setDimensionsCSV(dimensionsCSV != null ? dimensionsCSV.toString() : "");
		status.setReplacedExpression(displayExpr);

		// to rename dimension columns in the result
		StringBuilder dimensionColumns = new StringBuilder();

		if (groupByCols != null && !groupByCols.isEmpty()) {
			groupByCols.forEach((k, v) -> {
				if (columnNameMap.containsKey((k.toUpperCase()))) {
					try {
						dimensionColumns.append((rs.getString(k.toUpperCase())).trim())
								.append(ValidationConstants.HASH_DELIMITER);
					} catch (SQLException e) {
						e.printStackTrace();
					}

					try {
						row.put(v, rs.getString(k.toUpperCase()));
					} catch (SQLException e) {
						e.printStackTrace();
					}
					columnNameMap.put(k.toUpperCase(), v.replace("\"", ""));
				} else if (columnNameMap.containsKey((k))) {
					try {
						dimensionColumns.append((row.get(k)).toString().trim())
								.append(ValidationConstants.HASH_DELIMITER);
					} catch (Exception e) {
						e.printStackTrace();
					}

					try {
						row.put(v, row.get(k));
					} catch (Exception e) {
						e.printStackTrace();
					}
					columnNameMap.put(k, v.replace("\"", ""));
				}
			});
		}

		row.put("LineItemId CSV", lineItemIdCSV);
		row.put("Hash Key", getHashValueForExp(dimensionColumns.append(exprId).toString()));

		if (!headersAdded) {
			columnNameMap.put("Expression_Meta_data", "Expression_Meta_data");
			columnNameMap.put("Evaluated Expression", "Evaluated Expression");
			columnNameMap.put("Evaluation Message", "Evaluation Message");
			columnNameMap.put("Validation", "Validation");
			columnNameMap.put("LineItemId CSV", "LineItemId CSV");
			columnNameMap.put("Hash Key", "Hash Key");

			if(columnData != null && !columnData.isEmpty()) {
				columnData.forEach((k,v) -> {
					if(columnNameMap.containsKey(k)) {
						columnNameMap.put(k, v.replace("\"", ""));
					} else if(columnNameMap.containsKey(k.toUpperCase())) {
						columnNameMap.put(k.toUpperCase(), v.replace("\"", ""));
					}
				});
			}

			headerColumnSequence.add("Expression_Meta_data");
			headerColumnSequence.add("Evaluated Expression");
			headerColumnSequence.add("Evaluation Message");
			headerColumnSequence.add("Validation");
			headerColumnSequence.add("LineItemId CSV");
			headerColumnSequence.add("Hash Key");

			headersAdded = Boolean.TRUE;
		}
		return row;
	}

	private void populateColumnsUsedInFinalExpr(Map<String, String> columnNameMap) {

		String expressionCopy = expression;

		for (String columnName : columnNames) {

			if (columnName.toUpperCase().startsWith("A_")) {
				if (lineItemIdCSV == null) {
					lineItemIdCSV = new StringBuilder();
				} else {
					lineItemIdCSV.append(",");
				}

				lineItemIdCSV.append(ValidationStringUtils.replace(columnName, "A_", "", -1, true));
			}

			String value = null;
			if (columnNameMap.containsKey(columnName) && (expressionCopy.contains(columnName))) {

				int type = columnMetaData.get(columnName);

				switch (type) {
				case Types.SMALLINT:
				case Types.INTEGER:
				case Types.TINYINT:
					value = ExpressionDataType.INTEGER.getValue();
					break;

				case Types.BIGINT:
				case Types.NUMERIC:
					value = ExpressionDataType.LONG.getValue();
					break;

				case Types.DECIMAL:
					value = ExpressionDataType.BIGDECIMAL.getValue();
					break;

				case Types.DOUBLE:
				case Types.FLOAT:
					value = ExpressionDataType.DOUBLE.getValue();
					break;

				case Types.CHAR:
				case Types.LONGVARCHAR:
				case Types.LONGNVARCHAR:
				case Types.NCHAR:
				case Types.NVARCHAR:
				case Types.VARCHAR:
					value = ExpressionDataType.STRING.getValue();
					break;

				case Types.DATE:
				case Types.TIME:
				case Types.TIMESTAMP:
				case Types.TIME_WITH_TIMEZONE:
				case Types.TIMESTAMP_WITH_TIMEZONE:
					value = ExpressionDataType.DATE.getValue();
					break;

				case Types.BOOLEAN:
				case Types.BIT:
					value = ExpressionDataType.BOOLEAN.getValue();
					break;

				default:
					value = ExpressionDataType.STRING.getValue();
					break;
				}

				usedColumnsDataType.put(columnName, value);
			}

		}

		sortedUsedColumns = new ArrayList<>();
		sortedUsedColumns.addAll(usedColumnsDataType.keySet());
		sortedUsedColumns
				.sort(Collections.reverseOrder(Comparator.comparingInt(o -> o != null ? o.trim().length() : 0)));
	}

	public ExpressionProcessor() {
		// do-nothing
	}

	public Integer getSystemSolutionId() {
		return systemSolutionId;
	}

	public String getQuery() {
		return query;
	}

	public List<String> getColumnNames() {
		return columnNames;
	}

	public List<String> getTableNames() {
		return tableNames;
	}

	public SpelExpressionParser getParser() {
		return parser;
	}

	public String getExpression() {
		return expression;
	}

	public StandardEvaluationContext getContext() {
		return context;
	}

	public ExpressionParser getExpressionParser() {
		return expressionParser;
	}

	public Map<String, String> getGroupByCols() {
		return groupByCols;
	}

	public Integer getExprId() {
		return exprId;
	}

	public Map<String, Integer> getColumnMetaData() {
		return columnMetaData;
	}

	public ExpressionMetaData getExpressionMetaData() {
		return expressionMetaData;
	}

	public Gson getGson() {
		return gson;
	}

	public StringBuilder getDimensionsCSV() {
		return dimensionsCSV;
	}

	public Integer getRunId() {
		return runId;
	}

	public String getOutputDirectory() {
		return outputDirectory;
	}

	public String getFileName() {
		return fileName;
	}

	public Boolean getHasError() {
		return hasError;
	}

}
