package com.fintellix.framework.collaboration.dto;

import java.io.Serializable;

public class ContentPrivileges implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6027217038980528371L;
	
	private Integer contentPrivilegeId;
	private Integer contentPrivilegeName;
	public Integer getContentPrivilegeId() {
		return contentPrivilegeId;
	}
	
	public void setContentPrivilegeId(Integer contentPrivilegeId) {
		this.contentPrivilegeId = contentPrivilegeId;
	}
	
	public Integer getContentPrivilegeName() {
		return contentPrivilegeName;
	}
	
	public void setContentPrivilegeName(Integer contentPrivilegeName) {
		this.contentPrivilegeName = contentPrivilegeName;
	}
	

}
