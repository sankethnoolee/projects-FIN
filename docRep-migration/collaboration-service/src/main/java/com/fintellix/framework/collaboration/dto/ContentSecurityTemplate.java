package com.fintellix.framework.collaboration.dto;

import java.io.Serializable;

public class ContentSecurityTemplate implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7313356186004782722L;
	
	private String securityTemplateName;
	private Integer contentPrivilegeId;
	public String getSecurityTemplateName() {
		return securityTemplateName;
	}
	
	public void setSecurityTemplateName(String securityTemplateName) {
		this.securityTemplateName = securityTemplateName;
	}
	
	public Integer getContentPrivilege() {
		return contentPrivilegeId;
	}
	
	public void setContentPrivilege(Integer contentPrivilege) {
		this.contentPrivilegeId = contentPrivilege;
	}
	
	
	

}
