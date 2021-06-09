/**
 * 
 */
package com.fintellix.validationrestservice.core.executor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author sumeet.tripathi
 *
 */
public class ValidationExecutionGroup {

	private String groupName;
	private Map<Integer, ExpressionStatus> expressionMap = new HashMap<>();
	private String query;
	private Map<Integer, ExpressionStatus> failedExpressionMap = new HashMap<>();
	private Map<String, String> replacedColumnMapping = new HashMap<>();
	private String baseTableName = null;
	private List<String> csvNames;

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public Map<Integer, ExpressionStatus> getExpressionMap() {
		return expressionMap;
	}

	public void addExpression(ExpressionStatus expression, Integer exprId) {
		expressionMap.put(exprId, expression);
		replacedColumnMapping.putAll(expression.getReplacedColumnMapping());
		if (baseTableName == null) {
			baseTableName = expression.getExpressionMetaData().getBaseTableName();
		}
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public Map<Integer, ExpressionStatus> getFailedExpressionMap() {
		return failedExpressionMap;
	}

	public void addFailedExpression(ExpressionStatus expression, Integer exprId) {
		failedExpressionMap.put(exprId, expression);
	}

	public Map<String, String> getReplacedColumnMapping() {
		return replacedColumnMapping;
	}

	public void setReplacedColumnMapping(Map<String, String> replacedColumnMapping) {
		this.replacedColumnMapping = replacedColumnMapping;
	}

	public List<String> getCsvNames() {
		return csvNames;
	}

	public void setCsvNames(List<String> csvNames) {
		this.csvNames = csvNames;
	}

}
