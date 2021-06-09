package com.fintellix.validationrestservice.definition;

import java.io.Serializable;
import java.util.Set;

public class ExpressionEntityDetail implements Serializable {
    private String entityType;
    private String entityCode;
    private Set<String> entityElements;
    private boolean isMePresent = false;
    private String subjectArea;
    private String ddTableName;
    private String columnName;

    protected ExpressionEntityDetail(String entityType, String entityCode, Set<String> entityElements) {
        this.entityType = entityType;
        this.entityCode = entityCode;
        this.entityElements = entityElements;
    }

    protected ExpressionEntityDetail(String entityType, String entityCode, Set<String> entityElements, boolean isMePresent) {
        this.entityType = entityType;
        this.entityCode = entityCode;
        this.entityElements = entityElements;
        this.isMePresent = isMePresent;
    }

    protected ExpressionEntityDetail(String entityType, String entityCode, Set<String> entityElements, boolean isMePresent, String subjectArea, String ddTableName, String columnName) {
        this.entityType = entityType;
        this.entityCode = entityCode;
        this.entityElements = entityElements;
        this.isMePresent = isMePresent;
        this.subjectArea = subjectArea;
        this.ddTableName = ddTableName;
        this.columnName = columnName;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getEntityCode() {
        return entityCode;
    }

    public void setEntityCode(String entityCode) {
        this.entityCode = entityCode;
    }

    public Set<String> getEntityElements() {
        return entityElements;
    }

    public void setEntityElements(Set<String> entityElements) {
        this.entityElements = entityElements;
    }

    public boolean isMePresent() {
        return isMePresent;
    }

    public void setMePresent(boolean isMePresent) {
        this.isMePresent = isMePresent;
    }

    public String getSubjectArea() {
        return subjectArea;
    }

    public void setSubjectArea(String subjectArea) {
        this.subjectArea = subjectArea;
    }

    public String getDdTableName() {
        return ddTableName;
    }

    public void setDdTableName(String ddTableName) {
        this.ddTableName = ddTableName;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((columnName == null) ? 0 : columnName.hashCode());
        result = prime * result + ((ddTableName == null) ? 0 : ddTableName.hashCode());
        result = prime * result + ((entityCode == null) ? 0 : entityCode.hashCode());
        result = prime * result + ((entityElements == null) ? 0 : entityElements.hashCode());
        result = prime * result + ((entityType == null) ? 0 : entityType.hashCode());
        result = prime * result + (isMePresent ? 1231 : 1237);
        result = prime * result + ((subjectArea == null) ? 0 : subjectArea.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ExpressionEntityDetail other = (ExpressionEntityDetail) obj;
        if (columnName == null) {
            if (other.columnName != null)
                return false;
        } else if (!columnName.equals(other.columnName))
            return false;
        if (ddTableName == null) {
            if (other.ddTableName != null)
                return false;
        } else if (!ddTableName.equals(other.ddTableName))
            return false;
        if (entityCode == null) {
            if (other.entityCode != null)
                return false;
        } else if (!entityCode.equals(other.entityCode))
            return false;
        if (entityElements == null) {
            if (other.entityElements != null)
                return false;
        } else if (!entityElements.equals(other.entityElements))
            return false;
        if (entityType == null) {
            if (other.entityType != null)
                return false;
        } else if (!entityType.equals(other.entityType))
            return false;
        if (isMePresent != other.isMePresent)
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
        return "ExpressionEntityDetail [entityType=" + entityType + ", entityCode=" + entityCode + ", entityElements="
                + entityElements + ", isMePresent=" + isMePresent + ", subjectArea=" + subjectArea + ", ddTableName="
                + ddTableName + ", columnName=" + columnName + "]";
    }
}
