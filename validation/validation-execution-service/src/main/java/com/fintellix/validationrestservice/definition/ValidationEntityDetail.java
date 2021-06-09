package com.fintellix.validationrestservice.definition;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ValidationEntityDetail  extends ExpressionEntityDetail implements Serializable{
	private Map<String, EntityMetadataInfo> metaDataInfoMap = new HashMap<>();
	private String subjectArea;
	protected ValidationEntityDetail(String entityType, String entityCode, Set<String> entityElements, Map<String, EntityMetadataInfo> metaDataInfoMap, String subjectArea) {
		super(entityType, entityCode, entityElements);
		 this.metaDataInfoMap = metaDataInfoMap;
		 this.subjectArea=subjectArea;
	}
	public ValidationEntityDetail(String entityType, String entityCode, Set<String> entityElements, boolean isMePresent, Map<String, 
			EntityMetadataInfo> metaDataInfoMap,  String subjectArea, String ddTableName, String ddColumnName) {
        super( entityType,  entityCode, entityElements,isMePresent, subjectArea,  ddTableName,  ddColumnName);
        this.metaDataInfoMap = metaDataInfoMap;
        this.subjectArea=subjectArea;
    }

	 public Map<String, EntityMetadataInfo> getMetaDataInfoMap() {
	        return metaDataInfoMap;
	    }

	    public void setMetaDataInfoMap(Map<String, EntityMetadataInfo> metaDataInfoMap) {
	        this.metaDataInfoMap = metaDataInfoMap;
	    }
		
		public String getSubjectArea() {
			return subjectArea;
		}
		public void setSubjectArea(String subjectArea) {
			this.subjectArea = subjectArea;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((metaDataInfoMap == null) ? 0 : metaDataInfoMap.hashCode());
			result = prime * result + ((subjectArea == null) ? 0 : subjectArea.hashCode());
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
			ValidationEntityDetail other = (ValidationEntityDetail) obj;
			if (metaDataInfoMap == null) {
				if (other.metaDataInfoMap != null)
					return false;
			} else if (!metaDataInfoMap.equals(other.metaDataInfoMap))
				return false;
			if (subjectArea == null) {
				if (other.subjectArea != null)
					return false;
			} else if (!subjectArea.equals(other.subjectArea))
				return false;
			return true;
		}
		@Override
		public String toString() {
			return "ValidationEntityDetail [metaDataInfoMap=" + metaDataInfoMap + ", subjectArea=" + subjectArea + "]";
		}
		
		
}
