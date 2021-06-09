/**
 * 
 */
package com.fintellix.framework.dto;

import java.io.Serializable;
import java.util.Map;

/**
 * @author sumeet.tripathi
 *
 */
public class ExpressionStatus implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer exprId;
	private Boolean hasError = Boolean.FALSE;

	private Integer totalCount = 0;

	private Integer errorCount = new Integer(0);

	private String replacedExpression = null;
	private String dimensionsCSV = null;

	private String query;
	private String expression;

	private String displayExpression;
	private String spelExpression;

	private ExpressionMetaData expressionMetaData;

	private Map<String, String> groupByCols;

	private Map<String, String> usedColumnsDataType;

	private Map<String, String> replacedColumnMapping;

	private String lineItemIdCSV;

	public ExpressionStatus(Integer exprId) {
		super();
		this.exprId = exprId;
	}

	public void setSparkMetaData(String query, String expression, String dimensionsCSV, String displayExpression,
			String spelExpression, String lineItemIdCSV, Map<String, String> groupByCols,
			Map<String, String> usedColumnsDataType, ExpressionMetaData expressionMetaData,
			Map<String, String> replacedColumnMapping) {
		this.query = query;
		this.expression = expression;
		this.displayExpression = displayExpression;
		this.spelExpression = spelExpression;
		this.lineItemIdCSV = lineItemIdCSV;
		this.groupByCols = groupByCols;
		this.usedColumnsDataType = usedColumnsDataType;
		this.expressionMetaData = expressionMetaData;
		this.replacedColumnMapping = replacedColumnMapping;
	}

	public ExpressionStatus(Integer exprId, Boolean hasError) {
		super();
		this.exprId = exprId;
		this.hasError = hasError;
	}

	public void incrementErrorCount() {
		errorCount+=1;
	}

	public Integer getExprId() {
		return exprId;
	}

	public void setExprId(Integer exprId) {
		this.exprId = exprId;
	}

	public Boolean getHasError() {
		return hasError;
	}

	public void setHasError(Boolean hasError) {
		this.hasError = hasError;
	}

	public Integer getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(Integer totalCount) {
		this.totalCount = totalCount;
	}

	public String getReplacedExpression() {
		if (replacedExpression == null) {
			replacedExpression = "";
		}
		return replacedExpression;
	}

	public void setReplacedExpression(String replacedExpression) {
		if (this.replacedExpression == null) {
			this.replacedExpression = replacedExpression;
		}
	}

	public String getDimensionsCSV() {
		if (dimensionsCSV == null) {
			dimensionsCSV = "";
		}
		return dimensionsCSV;
	}

	public void setDimensionsCSV(String dimensionsCSV) {
		if (this.dimensionsCSV == null) {
			this.dimensionsCSV = dimensionsCSV;
		}

	}

	public void setErrorCount(Integer errorCount) {
		this.errorCount = errorCount;
	}

	public Integer getErrorCount() {
		return errorCount;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	public ExpressionMetaData getExpressionMetaData() {
		return expressionMetaData;
	}

	public void setExpressionMetaData(ExpressionMetaData expressionMetaData) {
		this.expressionMetaData = expressionMetaData;
	}

	public String getDisplayExpression() {
		return displayExpression;
	}

	public void setDisplayExpression(String displayExpression) {
		this.displayExpression = displayExpression;
	}

	public String getSpelExpression() {
		return spelExpression;
	}

	public void setSpelExpression(String spelExpression) {
		this.spelExpression = spelExpression;
	}

	public Map<String, String> getGroupByCols() {
		return groupByCols;
	}

	public void setGroupByCols(Map<String, String> groupByCols) {
		this.groupByCols = groupByCols;
	}

	public Map<String, String> getUsedColumnsDataType() {
		return usedColumnsDataType;
	}

	public void setUsedColumnsDataType(Map<String, String> usedColumnsDataType) {
		this.usedColumnsDataType = usedColumnsDataType;
	}

	public String getLineItemIdCSV() {
		return lineItemIdCSV;
	}

	public void setLineItemIdCSV(String lineItemIdCSV) {
		this.lineItemIdCSV = lineItemIdCSV;
	}

	public Map<String, String> getReplacedColumnMapping() {
		return replacedColumnMapping;
	}

	public void setReplacedColumnMapping(Map<String, String> replacedColumnMapping) {
		this.replacedColumnMapping = replacedColumnMapping;
	}

	@Override
	public String toString() {
		return "ExpressionStatus [exprId=" + exprId + ", hasError=" + hasError + ", totalCount=" + totalCount
				+ ", errorCount=" + errorCount + ", replacedExpression=" + replacedExpression + ", dimensionsCSV="
				+ dimensionsCSV + ", query=" + query + ", expression=" + expression + ", displayExpression="
				+ displayExpression + ", spelExpression=" + spelExpression + ", expressionMetaData="
				+ expressionMetaData + ", groupByCols=" + groupByCols + ", usedColumnsDataType=" + usedColumnsDataType
				+ ", replacedColumnMapping=" + replacedColumnMapping + ", lineItemIdCSV=" + lineItemIdCSV + "]";
	}
	
	
}
