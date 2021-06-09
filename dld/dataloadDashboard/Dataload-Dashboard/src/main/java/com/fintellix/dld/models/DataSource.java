package com.fintellix.dld.models;

import java.io.Serializable;

public class DataSource implements Serializable {

	private static final long serialVersionUID = -1185989421930835900L;

	private Integer dataSourceID;
	private String dataSourceName;
	private String isScglInd;
	private String scglCurrencyUcid;
	private Integer auditGlDataSourceId;
	private Integer financialYearTypeId;
	
	
	
	
	public Integer getDataSourceID() {
		return dataSourceID;
	}
	public void setDataSourceID(Integer dataSourceID) {
		this.dataSourceID = dataSourceID;
	}
	public String getDataSourceName() {
		return dataSourceName;
	}
	public void setDataSourceName(String dataSourceName) {
		this.dataSourceName = dataSourceName;
	}
	public String getIsScglInd() {
		return isScglInd;
	}
	public void setIsScglInd(String isScglInd) {
		this.isScglInd = isScglInd;
	}
	public String getScglCurrencyUcid() {
		return scglCurrencyUcid;
	}
	public void setScglCurrencyUcid(String scglCurrencyUcid) {
		this.scglCurrencyUcid = scglCurrencyUcid;
	}
	public Integer getAuditGlDataSourceId() {
		return auditGlDataSourceId;
	}
	public void setAuditGlDataSourceId(Integer auditGlDataSourceId) {
		this.auditGlDataSourceId = auditGlDataSourceId;
	}
	public Integer getFinancialYearTypeId() {
		return financialYearTypeId;
	}
	public void setFinancialYearTypeId(Integer financialYearTypeId) {
		this.financialYearTypeId = financialYearTypeId;
	}
	
	
	
}
