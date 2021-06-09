package com.fintellix.validationrestservice.core.resultwriter.pojo;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "expressionresultinfo")
public class ExpressionResultInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	public String id;

	public Integer runId;
	public Integer validationId;
	public Integer rowId;

	public String json;

	public String getId() {
		return id;
	}

	public ExpressionResultInfo(Integer runId, Integer validationId, Integer rowId, String json) {
		super();

		this.id = runId + "_" + validationId + "_" + rowId;

		this.runId = runId;
		this.validationId = validationId;
		this.rowId = rowId;
		if (json != null && json.trim().length() > 0) {
			this.json = json;
		} else {
			this.json = null;
		}

	}

	public void setId(String id) {
		this.id = id;
	}

	public Integer getRunId() {
		return runId;
	}

	public void setRunId(Integer runId) {
		this.runId = runId;
	}

	public Integer getValidationId() {
		return validationId;
	}

	public void setValidationId(Integer validationId) {
		this.validationId = validationId;
	}

	public Integer getRowId() {
		return rowId;
	}

	public void setRowId(Integer rowId) {
		this.rowId = rowId;
	}

	public String getJson() {
		return json;
	}

	public void setJson(String json) {
		this.json = json;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public ExpressionResultInfo() {
	}

}
