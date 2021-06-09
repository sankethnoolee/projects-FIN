/**
 * 
 */
package com.fintellix.framework.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author sumeet.tripathi
 *
 */
public class ValidationExecutionGroups {

	private List<ValidationExecutionGroup> validationExecutionGroups = new ArrayList<>();
	private Map<String, String> columnUUIDMap;
	private Map<String, Map<String, String>> dimensionColumnData;
	private Integer runId;

	public List<ValidationExecutionGroup> getValidationExecutionGroups() {
		return validationExecutionGroups;
	}

	public void addValidationExecutionGroup(ValidationExecutionGroup validationExecutionGroup) {
		validationExecutionGroups.add(validationExecutionGroup);
	}

	public Integer getRunId() {
		return runId;
	}

	public void setRunId(Integer runId) {
		this.runId = runId;
	}

	public Map<String, String> getColumnUUIDMap() {
		return columnUUIDMap;
	}

	public void setColumnUUIDMap(Map<String, String> columnUUIDMap) {
		this.columnUUIDMap = columnUUIDMap;
	}

	public Map<String, Map<String, String>> getDimensionColumnData() {
		return dimensionColumnData;
	}

	public void setDimensionColumnData(Map<String, Map<String, String>> dimensionColumnData) {
		this.dimensionColumnData = dimensionColumnData;
	}

}
