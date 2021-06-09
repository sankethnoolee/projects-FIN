package com.fintellix.validationrestservice.vo;

import java.util.Map;

public class QueryWithColumnDetail {

	String query;
	Map<String, String> colBNameColKeyMap;
	Map<Integer, Map<String, String>> versionNumberPkMap;
	Map<String, String> colKeyColDescMap;
	Map<String, String> colKeyColDataTypeMap;
	private Boolean isSCDII = Boolean.FALSE;
	private String startDateColumnName;
	private String endDateColumnName;
	private Boolean isOrgIdColAvailable;
	private String orgIdColumnName;

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public Map<String, String> getColBNameColKeyMap() {
		return colBNameColKeyMap;
	}

	public void setColBNameColKeyMap(Map<String, String> colBNameColKeyMap) {
		this.colBNameColKeyMap = colBNameColKeyMap;
	}

	public Map<Integer, Map<String, String>> getVersionNumberPkMap() {
		return versionNumberPkMap;
	}

	public void setVersionNumberPkMap(Map<Integer, Map<String, String>> versionNumberPkMap) {
		this.versionNumberPkMap = versionNumberPkMap;
	}

	public Map<String, String> getColKeyColDescMap() {
		return colKeyColDescMap;
	}

	public void setColKeyColDescMap(Map<String, String> colKeyColDescMap) {
		this.colKeyColDescMap = colKeyColDescMap;
	}

	public Map<String, String> getColKeyColDataTypeMap() {
		return colKeyColDataTypeMap;
	}

	public void setColKeyColDataTypeMap(Map<String, String> colKeyColDataTypeMap) {
		this.colKeyColDataTypeMap = colKeyColDataTypeMap;
	}

	public Boolean getIsSCDII() {
		return isSCDII;
	}

	public void setIsSCDII(Boolean isSCDII) {
		this.isSCDII = isSCDII;
	}

	public String getStartDateColumnName() {
		return startDateColumnName;
	}

	public void setStartDateColumnName(String startDateColumnName) {
		this.startDateColumnName = startDateColumnName;
	}

	public String getEndDateColumnName() {
		return endDateColumnName;
	}

	public void setEndDateColumnName(String endDateColumnName) {
		this.endDateColumnName = endDateColumnName;
	}

	public Boolean getIsOrgIdColAvailable() {
		return isOrgIdColAvailable;
	}

	public void setIsOrgIdColAvailable(Boolean isOrgIdColAvailable) {
		this.isOrgIdColAvailable = isOrgIdColAvailable;
	}

	public String getOrgIdColumnName() {
		return orgIdColumnName;
	}

	public void setOrgIdColumnName(String orgIdColumnName) {
		this.orgIdColumnName = orgIdColumnName;
	}
}
