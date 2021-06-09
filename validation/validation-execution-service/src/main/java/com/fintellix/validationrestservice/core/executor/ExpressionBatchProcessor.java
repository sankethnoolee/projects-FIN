/**
 * 
 */
package com.fintellix.validationrestservice.core.executor;

import java.math.BigInteger;
import java.sql.Types;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import com.fintellix.validationrestservice.core.datatypehandler.ExpressionDataTypeManager;
import com.fintellix.validationrestservice.core.parser.ExpressionParser;
import com.fintellix.validationrestservice.definition.ExpressionMetaData;
import com.fintellix.validationrestservice.definition.SubExpressionMetaData;
import com.fintellix.validationrestservice.definition.ValidationConstants;
import com.fintellix.validationrestservice.util.ApplicationProperties;
import com.fintellix.validationrestservice.vo.RunRecordDetail;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author sumeet.tripathi
 *
 */

@Component
@Scope("prototype")
public class ExpressionBatchProcessor implements Callable<ExpressionBatchProcessor> {

	private static Logger LOGGER = LoggerFactory.getLogger(ExpressionBatchProcessor.class);

	@Autowired
	private ExpressionDataTypeManager expressionDataTypeManager;

	private List<String> tableNames;
	private SpelExpressionParser parser;
	private String expression;
	private StandardEvaluationContext context;
	private ExpressionParser expressionParser;
	private Map<String, String> groupByCols;
	private Integer exprId;
	private ExpressionMetaData expressionMetaData;
	private Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
	private List<RunRecordDetail> runRecordDetails;
	private StringBuilder dimensionsCSV;

	private Map<String, String> usedColumnsDataType;
	private List<String> sortedColumnName;

	public Boolean hasError = Boolean.FALSE;

	public List<Map<String, Object>> rows;
	private Map<String, String> columnNameMap;

	private String spelExpression;
	private String displayExpression;

	private List<String> sortedUsedColumns;

	private StringBuilder lineItemIdCSV;

	private Map<String, String> replacedColumnMapping;
	
	private List<String> columnNames;
	
	private Map<String, Integer> columnMetaData;

	@Override
	public ExpressionBatchProcessor call() throws Exception {
		try {
			for (Map<String, Object> row : rows) {
				processRow(columnNameMap, row);
			}
		} catch (Throwable e) {
			e.printStackTrace();
			hasError = Boolean.TRUE;
		}
		return this;
	}

	public void init(List<String> tableNames, SpelExpressionParser parser, String expression,
			StandardEvaluationContext context, ExpressionParser expressionParser, Map<String, String> groupByCols,
			Integer exprId, ExpressionMetaData expressionMetaData, List<RunRecordDetail> runRecordDetails,
			StringBuilder dimensionsCSV, Map<String, String> usedColumns, List<String> sortedColumnName,
			Boolean hasError, List<Map<String, Object>> rows, Map<String, String> columnNameMap, String spelExpression,
			String displayExpression, StringBuilder lineItemIdCSV, List<String> sortedUsedColumns,
			Map<String, String> replacedColumnMapping, List<String> columnNames, Map<String, Integer> columnMetaData) {
		this.tableNames = tableNames;
		this.parser = parser;
		this.expression = expression;
		this.context = context;
		this.expressionParser = expressionParser;
		this.groupByCols = groupByCols;
		this.exprId = exprId;
		this.expressionMetaData = expressionMetaData;
		this.runRecordDetails = runRecordDetails;
		this.dimensionsCSV = dimensionsCSV;
		this.usedColumnsDataType = usedColumns;
		this.sortedColumnName = sortedColumnName;
		this.hasError = hasError;
		this.rows = rows;
		this.columnNameMap = columnNameMap;
		this.spelExpression = spelExpression;
		this.displayExpression = displayExpression;
		this.sortedUsedColumns = sortedUsedColumns;
		this.lineItemIdCSV = lineItemIdCSV;
		this.replacedColumnMapping = replacedColumnMapping;
		this.columnNames = columnNames;
		this.columnMetaData = columnMetaData;
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

	public Map<String, Object> processRow(Map<String, String> columnNameMap, Map<String, Object> row) throws Throwable {
		// String expressionCopy = expression;

		String displayExpr = displayExpression;
		String spelExpressionCopy = spelExpression;

		RunRecordDetail runRecordDetail = new RunRecordDetail();

		for (String columnName : sortedUsedColumns) {
			try {

				displayExpr = expressionDataTypeManager.getReplacedDisplayValue(row.get(columnName), columnName,
						usedColumnsDataType.get(columnName), displayExpr);
				spelExpressionCopy = expressionDataTypeManager.getReplacedSpelValue(row.get(columnName),
						replacedColumnMapping.get(columnName), usedColumnsDataType.get(columnName), spelExpressionCopy);
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
							if (row.get(columnName) != null) {
								int type = columnMetaData.get(columnName);
								Object value;

								switch (type) {
								case Types.SMALLINT:
								case Types.INTEGER:
								case Types.TINYINT:
									value = row.get(columnName);
									break;

								case Types.BIGINT:
								case Types.NUMERIC:
									value = row.get(columnName);
									break;

								case Types.DECIMAL:
									value = row.get(columnName);
									break;

								case Types.DOUBLE:
								case Types.FLOAT:
									value = row.get(columnName);
									break;

								case Types.CHAR:
								case Types.LONGVARCHAR:
								case Types.LONGNVARCHAR:
								case Types.NCHAR:
								case Types.NVARCHAR:
								case Types.VARCHAR:
									value = "'" + row.get(columnName)+ "'";
									break;

								case Types.DATE:
								case Types.TIME:
								case Types.TIMESTAMP:
								case Types.TIME_WITH_TIMEZONE:
								case Types.TIMESTAMP_WITH_TIMEZONE:
									value = row.get(columnName);
									break;

								default:
									value = "'" + row.get(columnName)+ "'";
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

		runRecordDetail
				.setValidationResult(evaluatedValue.trim().length() > 0 ? Boolean.valueOf(evaluatedValue) : false);
		runRecordDetail.setDimensionsCSV(dimensionsCSV != null ? dimensionsCSV.toString() : "");
		runRecordDetail.setReplacedExpression(displayExpr);
		runRecordDetails.add(runRecordDetail);

		// to rename dimension columns in the result
		StringBuilder dimensionColumns = new StringBuilder();

		if (groupByCols != null && !groupByCols.isEmpty()) {
			groupByCols.forEach((k, v) -> {
				if (columnNameMap.containsKey((k.toUpperCase()))) {
					try {
						dimensionColumns.append((row.get(k.toUpperCase())).toString().trim())
								.append(ValidationConstants.HASH_DELIMITER);
					} catch (Exception e) {
						e.printStackTrace();
					}

					try {
						row.put(v, row.get(k.toUpperCase()));
					} catch (Exception e) {
						e.printStackTrace();
					}
					columnNameMap.put(k.toUpperCase(), v.replace("\"", ""));
				}
				else if (columnNameMap.containsKey((k))) {
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

		return row;
	}

	public ExpressionDataTypeManager getExpressionDataTypeManager() {
		return expressionDataTypeManager;
	}

	public void setExpressionDataTypeManager(ExpressionDataTypeManager expressionDataTypeManager) {
		this.expressionDataTypeManager = expressionDataTypeManager;
	}

	public List<String> getTableNames() {
		return tableNames;
	}

	public void setTableNames(List<String> tableNames) {
		this.tableNames = tableNames;
	}

	public SpelExpressionParser getParser() {
		return parser;
	}

	public void setParser(SpelExpressionParser parser) {
		this.parser = parser;
	}

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	public StandardEvaluationContext getContext() {
		return context;
	}

	public void setContext(StandardEvaluationContext context) {
		this.context = context;
	}

	public ExpressionParser getExpressionParser() {
		return expressionParser;
	}

	public void setExpressionParser(ExpressionParser expressionParser) {
		this.expressionParser = expressionParser;
	}

	public Map<String, String> getGroupByCols() {
		return groupByCols;
	}

	public void setGroupByCols(Map<String, String> groupByCols) {
		this.groupByCols = groupByCols;
	}

	public Integer getExprId() {
		return exprId;
	}

	public void setExprId(Integer exprId) {
		this.exprId = exprId;
	}

	public ExpressionMetaData getExpressionMetaData() {
		return expressionMetaData;
	}

	public void setExpressionMetaData(ExpressionMetaData expressionMetaData) {
		this.expressionMetaData = expressionMetaData;
	}

	public List<RunRecordDetail> getRunRecordDetails() {
		return runRecordDetails;
	}

	public void setRunRecordDetails(List<RunRecordDetail> runRecordDetails) {
		this.runRecordDetails = runRecordDetails;
	}

	public StringBuilder getDimensionsCSV() {
		return dimensionsCSV;
	}

	public void setDimensionsCSV(StringBuilder dimensionsCSV) {
		this.dimensionsCSV = dimensionsCSV;
	}

	public Map<String, String> getUsedColumns() {
		return usedColumnsDataType;
	}

	public void setUsedColumns(Map<String, String> usedColumns) {
		this.usedColumnsDataType = usedColumns;
	}

	public List<String> getSortedColumnName() {
		return sortedColumnName;
	}

	public void setSortedColumnName(List<String> sortedColumnName) {
		this.sortedColumnName = sortedColumnName;
	}

	public Boolean getHasError() {
		return hasError;
	}

	public void setHasError(Boolean hasError) {
		this.hasError = hasError;
	}

}
