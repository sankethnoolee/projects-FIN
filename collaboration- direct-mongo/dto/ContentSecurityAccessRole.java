package com.fintellix.framework.collaboration.dto;

import java.io.Serializable;

public class ContentSecurityAccessRole implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 598344082056633034L;
	private String contentSecurityId;
	private String contentTypeId;
	private String contentId;
	private Integer roleId;
	private Integer solutionId;
	private Integer orgId;
	private String securityTemplateName;
	
	public String getContentSecurityId() {
		return contentSecurityId;
	}
	
	public void setContentSecurityId(String contentSecurityId) {
		this.contentSecurityId = contentSecurityId;
	}
	
	public String getContentTypeId() {
		return contentTypeId;
	}
	
	public void setContentTypeId(String contentTypeId) {
		this.contentTypeId = contentTypeId;
	}
	
	public String getContentId() {
		return contentId;
	}
	
	public void setContentId(String contentId) {
		this.contentId = contentId;
	}
	
	public Integer getRoleId() {
		return roleId;
	}
	
	public void setRoleId(Integer roleId) {
		this.roleId = roleId;
	}
	
	public Integer getSolutionId() {
		return solutionId;
	}
	
	public void setSolutionId(Integer solutionId) {
		this.solutionId = solutionId;
	}
	
	public Integer getOrgId() {
		return orgId;
	}
	
	public void setOrgId(Integer orgId) {
		this.orgId = orgId;
	}
	
	public String getSecurityTemplateName() {
		return securityTemplateName;
	}
	
	public void setSecurityTemplateName(String securityTemplateName) {
		this.securityTemplateName = securityTemplateName;
	}
	

}
