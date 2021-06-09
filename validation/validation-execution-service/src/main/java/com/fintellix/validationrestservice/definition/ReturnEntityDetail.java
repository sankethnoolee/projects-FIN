package com.fintellix.validationrestservice.definition;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ReturnEntityDetail extends ExpressionEntityDetail implements Serializable {
    private Integer reportId;
    private String sectionDesc;
    private Map<String, ReturnMetadataInfo> metaDataInfoMap = new HashMap<>();

    public ReturnEntityDetail(String entityType, String entityCode, Set<String> entityElements, Integer reportId,
                              String sectionDesc, Map<String, ReturnMetadataInfo> metaDataInfoMap) {
        super(entityType, entityCode, entityElements);
        this.reportId = reportId;
        this.sectionDesc = sectionDesc;
        this.metaDataInfoMap = metaDataInfoMap;
    }

    public ReturnEntityDetail(String entityType, String entityCode, Set<String> entityElements, boolean isMePresent,
                              Integer reportId, String sectionDesc, Map<String, ReturnMetadataInfo> metaDataInfoMap) {
        super(entityType, entityCode, entityElements, isMePresent);
        this.reportId = reportId;
        this.sectionDesc = sectionDesc;
        this.metaDataInfoMap = metaDataInfoMap;
    }

    public Integer getReportId() {
        return reportId;
    }

    public void setReportId(Integer reportId) {
        this.reportId = reportId;
    }

    public Map<String, ReturnMetadataInfo> getMetaDataInfoMap() {
        return metaDataInfoMap;
    }

    public void setMetaDataInfoMap(Map<String, ReturnMetadataInfo> metaDataInfoMap) {
        this.metaDataInfoMap = metaDataInfoMap;
    }

    public String getSectionDesc() {
        return sectionDesc;
    }

    public void setSectionDesc(String sectionDesc) {
        this.sectionDesc = sectionDesc;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ReturnEntityDetail that = (ReturnEntityDetail) o;
        return Objects.equals(reportId, that.reportId) &&
                Objects.equals(sectionDesc, that.sectionDesc) &&
                Objects.equals(metaDataInfoMap, that.metaDataInfoMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), reportId, sectionDesc, metaDataInfoMap);
    }

    @Override
    public String toString() {
        return "ReturnEntityDetail[" +
                "reportId=" + reportId +
                ", sectionDesc='" + sectionDesc + '\'' +
                ", metaDataInfoMap=" + metaDataInfoMap +
                ']';
    }
}
