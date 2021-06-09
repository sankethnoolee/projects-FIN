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

public class SubExpressionMetaData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String selectQuery;
	private String filterCondition;
	@Expose
	private List<String> selectCols = new ArrayList<>();
	private Boolean isNavigable;
	@Expose
	private String entityCode;
	@Expose
	private String entityType;
	private String joinCondition = null;
	@Expose
	private String finalQuery = null;
	@Expose
	private String entityName = null;
	@Expose
	private Integer reportId = null;
	@Expose
	private Integer sectionId = null;
	@Expose
	private String formName = null;
	@Expose
	private String sectionDesc;
	@Expose
	private String reportName;
	@Expose
	private Map<String, String> basePrimaryColumns = new HashMap<>();
	@Expose
	private String returnType;
	@Expose
	private String lineItemCode;
	@Expose
	private String lineItemDesc;
	@Expose
	private String targetCol;
	@Expose
	private Integer periodId;
	@Expose
	private Integer regReportVersion;
	@Expose
	private Integer versionNo;
	@Expose
	private Integer orgId;
	
	public String getSelectQuery() {
		return selectQuery;
	}

	public void setSelectQuery(String selectQuery) {
		this.selectQuery = selectQuery;
	}

	public String getFilterCondition() {
		return filterCondition;
	}

	public void setFilterCondition(String filterCondition) {
		this.filterCondition = filterCondition;
	}

	public Boolean getIsNavigable() {
		return isNavigable;
	}

	public void setIsNavigable(Boolean isNavigable) {
		this.isNavigable = isNavigable;
	}

	public List<String> getSelectCols() {
		return selectCols;
	}

	public void setSelectCols(List<String> selectCols) {
		this.selectCols = selectCols;
	}

	public String getEntityCode() {
		return entityCode;
	}

	public void setEntityCode(String entityCode) {
		this.entityCode = entityCode;
	}

	public String getEntityType() {
		return entityType;
	}

	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}

	public String getJoinCondition() {
		return joinCondition;
	}

	public void setJoinCondition(String joinCondition) {
		this.joinCondition = joinCondition;
	}

	public String getFinalQuery() {
		return finalQuery;
	}

	public void setFinalQuery(String finalQuery) {
		this.finalQuery = finalQuery;
	}

	public String getEntityName() {
		return entityName;
	}

	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}

	public Integer getReportId() {
		return reportId;
	}

	public void setReportId(Integer reportId) {
		this.reportId = reportId;
	}

	public Integer getSectionId() {
		return sectionId;
	}

	public void setSectionId(Integer sectionId) {
		this.sectionId = sectionId;
	}

	public String getFormName() {
		return formName;
	}

	public void setFormName(String formName) {
		this.formName = formName;
	}

	public Map<String, String> getBasePrimaryColumns() {
		return basePrimaryColumns;
	}

	public void setBasePrimaryColumns(Map<String, String> basePrimaryColumns) {
		this.basePrimaryColumns = basePrimaryColumns;
	}

	public String getReturnType() {
		return returnType;
	}

	public void setReturnType(String returnType) {
		this.returnType = returnType;
	}

	public String getSectionDesc() {
		return sectionDesc;
	}

	public void setSectionDesc(String sectionDesc) {
		this.sectionDesc = sectionDesc;
	}

	public String getReportName() {
		return reportName;
	}

	public void setReportName(String reportName) {
		this.reportName = reportName;
	}

	public String getLineItemCode() {
		return lineItemCode;
	}

	public void setLineItemCode(String lineItemCode) {
		this.lineItemCode = lineItemCode;
	}

	public String getLineItemDesc() {
		return lineItemDesc;
	}

	public void setLineItemDesc(String lineItemDesc) {
		this.lineItemDesc = lineItemDesc;
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

	public String getTargetCol() {
		return targetCol;
	}

	public void setTargetCol(String targetCol) {
		this.targetCol = targetCol;
	}

}
