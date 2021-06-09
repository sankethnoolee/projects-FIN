package com.fintellix.dld.models;

import java.io.Serializable;
import java.sql.Date;

public class DldStatusDownload implements Serializable{

	private static final long serialVersionUID = 732184205797431924L;
	private String solutionName;
	private String consumerType;
	private String consumerName;
	private String subItemName;
	private String dataAvailabilityStatus;
	private String dataAvailabilityDueDate;
	private String dataAvailabilityCompletionDate;
	private String dataProcessingEndDueDate;
	private String dataProcessingLastProcessedDate;
	private String consumerRepository;
	private String consumerEntityName;
	private String consumerStatus;
	private String sourceSystem;
	private String sourceEntity;
	private String sourceStatus;
	
	public String getSolutionName() {
		return solutionName;
	}
	public void setSolutionName(String solutionName) {
		this.solutionName = solutionName;
	}
	public String getConsumerType() {
		return consumerType;
	}
	public void setConsumerType(String consumerType) {
		this.consumerType = consumerType;
	}
	public String getConsumerName() {
		return consumerName;
	}
	public void setConsumerName(String consumerName) {
		this.consumerName = consumerName;
	}
	public String getSubItemName() {
		return subItemName;
	}
	public void setSubItemName(String subItemName) {
		this.subItemName = subItemName;
	}
	public String getDataAvailabilityStatus() {
		return dataAvailabilityStatus;
	}
	public void setDataAvailabilityStatus(String dataAvailabilityStatus) {
		this.dataAvailabilityStatus = dataAvailabilityStatus;
	}
	public String getConsumerRepository() {
		return consumerRepository;
	}
	public void setConsumerRepository(String consumerRepository) {
		this.consumerRepository = consumerRepository;
	}
	public String getConsumerEntityName() {
		return consumerEntityName;
	}
	public void setConsumerEntityName(String consumerEntityName) {
		this.consumerEntityName = consumerEntityName;
	}
	public String getConsumerStatus() {
		return consumerStatus;
	}
	public void setConsumerStatus(String consumerStatus) {
		this.consumerStatus = consumerStatus;
	}
	public String getSourceSystem() {
		return sourceSystem;
	}
	public void setSourceSystem(String sourceSystem) {
		this.sourceSystem = sourceSystem;
	}
	public String getSourceEntity() {
		return sourceEntity;
	}
	public void setSourceEntity(String sourceEntity) {
		this.sourceEntity = sourceEntity;
	}
	public String getSourceStatus() {
		return sourceStatus;
	}
	public void setSourceStatus(String sourceStatus) {
		this.sourceStatus = sourceStatus;
	}
	public String getDataAvailabilityDueDate() {
		return dataAvailabilityDueDate;
	}
	public void setDataAvailabilityDueDate(String dataAvailabilityDueDate) {
		this.dataAvailabilityDueDate = dataAvailabilityDueDate;
	}
	public String getDataAvailabilityCompletionDate() {
		return dataAvailabilityCompletionDate;
	}
	public void setDataAvailabilityCompletionDate(
			String dataAvailabilityCompletionDate) {
		this.dataAvailabilityCompletionDate = dataAvailabilityCompletionDate;
	}
	public String getDataProcessingEndDueDate() {
		return dataProcessingEndDueDate;
	}
	public void setDataProcessingEndDueDate(String dataProcessingEndDueDate) {
		this.dataProcessingEndDueDate = dataProcessingEndDueDate;
	}
	public String getDataProcessingLastProcessedDate() {
		return dataProcessingLastProcessedDate;
	}
	public void setDataProcessingLastProcessedDate(
			String dataProcessingLastProcessedDate) {
		this.dataProcessingLastProcessedDate = dataProcessingLastProcessedDate;
	}

}
