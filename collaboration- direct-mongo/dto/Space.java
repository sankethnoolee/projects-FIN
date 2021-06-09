package com.fintellix.framework.collaboration.dto;

import java.io.Serializable;

public class Space implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Integer spaceId;
	private String spaceName;
	private String spaceDesc;
	private Long createdTime;
	private Integer createdBy;
	
	public Integer getSpaceId() {
		return spaceId;
	}
	
	public void setSpaceId(Integer spaceId) {
		this.spaceId = spaceId;
	}
	
	public String getSpaceName() {
		return spaceName;
	}
	
	public void setSpaceName(String spaceName) {
		this.spaceName = spaceName;
	}
	
	public String getSpaceDesc() {
		return spaceDesc;
	}
	
	public void setSpaceDesc(String spaceDesc) {
		this.spaceDesc = spaceDesc;
	}
	
	public Long getCreatedTime() {
		return createdTime;
	}
	
	public void setCreatedTime(Long createdTime) {
		this.createdTime = createdTime;
	}
	
	public Integer getCreatedBy() {
		return createdBy;
	}
	
	public void setCreatedBy(Integer createdBy) {
		this.createdBy = createdBy;
	}

}
