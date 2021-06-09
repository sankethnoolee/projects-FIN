package com.fintellix.framework.collaboration.dto;

import java.io.Serializable;

public class DocumentWrapperForSearch extends DocumentWrapper implements Serializable {

	private static final long serialVersionUID = 6643484341807167841L;
	public DocumentWrapperForSearch(DocumentWrapper dw){
		this.setIsPrivate(dw.getIsPrivate());
		this.setEntityId(dw.getEntityId());
		this.setEntityName(dw.getEntityName());
		this.setEntityType(dw.getEntityType());
		this.setPrivilegeId(dw.getPrivilegeId());
		this.setPrivilegeName(dw.getPrivilegeName());
		this.setEntityPath(dw.getEntityPath());
		this.setCreatedBy(dw.getCreatedBy());
		this.setLastModifiedBy(dw.getLastModifiedBy());
		this.setLastModified(dw.getLastModified());
		this.setSortName(dw.getSortName());
		this.setCreatedTime(dw.getCreatedTime());
		this.setVersionNumber(dw.getVersionNumber());
		this.setResultOrigin(dw.getResultOrigin());
		
	}
	private String fileLocationUUID;
	private String fileLocationDisplayName;
	
	public String getFileLocationUUID() {
		return fileLocationUUID;
	}
	public void setFileLocationUUID(String fileLocationUUID) {
		this.fileLocationUUID = fileLocationUUID;
	}
	public String getFileLocationDisplayName() {
		return fileLocationDisplayName;
	}
	public void setFileLocationDisplayName(String fileLocationDisplayName) {
		this.fileLocationDisplayName = fileLocationDisplayName;
	}
	@Override
	public String toString() {
		return "DocumentWrapperForSearch [fileLocationUUID=" + fileLocationUUID
				+ ", fileLocationDisplayName=" + fileLocationDisplayName + "]";
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime
				* result
				+ ((fileLocationDisplayName == null) ? 0
						: fileLocationDisplayName.hashCode());
		result = prime
				* result
				+ ((fileLocationUUID == null) ? 0 : fileLocationUUID.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		DocumentWrapperForSearch other = (DocumentWrapperForSearch) obj;
		if (fileLocationDisplayName == null) {
			if (other.fileLocationDisplayName != null)
				return false;
		} else if (!fileLocationDisplayName
				.equals(other.fileLocationDisplayName))
			return false;
		if (fileLocationUUID == null) {
			if (other.fileLocationUUID != null)
				return false;
		} else if (!fileLocationUUID.equals(other.fileLocationUUID))
			return false;
		return true;
	}
	
}
