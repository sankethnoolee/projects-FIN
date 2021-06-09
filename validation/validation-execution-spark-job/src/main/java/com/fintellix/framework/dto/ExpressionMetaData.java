/**
 * 
 */
package com.fintellix.framework.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.annotations.Expose;

/**
 * @author sumeet.tripathi
 *
 */
public class ExpressionMetaData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Expose
	private Map<String, SubExpressionMetaData> groupByDetailsBySubExpr = new HashMap<>();
	private Map<String, String> usedColumns = new HashMap<>();
	private Map<String, String> groupByColumns = new HashMap<>();
	@Expose
	private Map<String, String> basePrimaryColumns = new HashMap<>();
	@Expose
	private Boolean isNavigable = Boolean.FALSE;
	@Expose
	private String formName;
	@Expose
	private String entityName;
	@Expose
	private String reportName;
	@Expose
	private Integer sectionId;
	@Expose
	private Integer reportId;
	private String sectionDesc;
	@Expose
	private String returnType;
	@Expose
	private List<String> entityCols = new ArrayList<>();
	private Boolean isForEachPresent = Boolean.FALSE;
	private String baseTableName;
	@Expose
	private Map<String,Map<String,String>> columnInfo = new HashMap<>();
	@Expose
	private Integer periodId;
	@Expose
	private Integer regReportVersion;
	@Expose
	private Integer versionNo;
	@Expose
	private Integer orgId;

	public Map<String, SubExpressionMetaData> getGroupByDetailsBySubExpr() {
		return groupByDetailsBySubExpr;
	}

	public void setGroupByDetailsBySubExpr(Map<String, SubExpressionMetaData> groupByDetailsBySubExpr) {
		this.groupByDetailsBySubExpr = groupByDetailsBySubExpr;
	}

	public Map<String, String> getUsedColumns() {
		return usedColumns;
	}

	public void setUsedColumns(Map<String, String> usedColumns) {
		this.usedColumns = usedColumns;
	}

	public Map<String, String> getGroupByColumns() {
		return groupByColumns;
	}

	public void setGroupByColumns(Map<String, String> groupByColumns) {
		this.groupByColumns = groupByColumns;
	}

	public Map<String, String> getBasePrimaryColumns() {
		return basePrimaryColumns;
	}

	public void setBasePrimaryColumns(Map<String, String> basePrimaryColumns) {
		this.basePrimaryColumns = basePrimaryColumns;
	}

	public Boolean getIsNavigable() {
		return isNavigable;
	}

	public void setIsNavigable(Boolean isNavigable) {
		this.isNavigable = isNavigable;
	}

	public String getFormName() {
		return formName;
	}

	public void setFormName(String formName) {
		this.formName = formName;
	}

	public String getEntityName() {
		return entityName;
	}

	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}

	public String getReportName() {
		return reportName;
	}

	public void setReportName(String reportName) {
		this.reportName = reportName;
	}

	public Integer getSectionId() {
		return sectionId;
	}

	public void setSectionId(Integer sectionId) {
		this.sectionId = sectionId;
	}

	public String getReturnType() {
		return returnType;
	}

	public void setReturnType(String returnType) {
		this.returnType = returnType;
	}

	public Boolean getIsForEachPresent() {
		return isForEachPresent;
	}

	public void setIsForEachPresent(Boolean isForEachPresent) {
		this.isForEachPresent = isForEachPresent;
	}

	public List<String> getEntityCols() {
		return entityCols;
	}

	public void setEntityCols(List<String> entityCols) {
		this.entityCols = entityCols;
	}

	public String getBaseTableName() {
		return baseTableName;
	}

	public void setBaseTableName(String baseTableName) {
		this.baseTableName = baseTableName;
	}

	public Integer getReportId() {
		return reportId;
	}

	public void setReportId(Integer reportId) {
		this.reportId = reportId;
	}

	public String getSectionDesc() {
		return sectionDesc;
	}

	public void setSectionDesc(String sectionDesc) {
		this.sectionDesc = sectionDesc;
	}

	public Map<String, Map<String, String>> getColumnInfo() {
		return columnInfo;
	}

	public void setColumnInfo(Map<String, Map<String, String>> columnInfo) {
		this.columnInfo = columnInfo;
	}

	public Integer getPeriodId() {
		return periodId;
	}

	public void setPeriodId(Integer periodId) {
		this.periodId = periodId;
	}

	public Integer getRegReportVersion() {
		return regReportVersion;
	}

	public void setRegReportVersion(Integer regReportVersion) {
		this.regReportVersion = regReportVersion;
	}

	public Integer getVersionNo() {
		return versionNo;
	}

	public void setVersionNo(Integer versionNo) {
		this.versionNo = versionNo;
	}

	public Integer getOrgId() {
		return orgId;
	}

	public void setOrgId(Integer orgId) {
		this.orgId = orgId;
	}

}
