package com.fintellix.validationrestservice.definition;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RefEntityDetail extends ExpressionEntityDetail implements Serializable {
    private Map<String, RefMetadataInfo> metaDataInfoMap = new HashMap<>();

    public RefEntityDetail(String entityType, String entityCode, Set<String> entityElements, Map<String, RefMetadataInfo> metaDataInfoMap) {
        super(entityType, entityCode, entityElements);
        this.metaDataInfoMap = metaDataInfoMap;
    }

    public RefEntityDetail(String entityType, String entityCode, Set<String> entityElements, boolean isMePresent, Map<String, RefMetadataInfo> metaDataInfoMap) {
        super(entityType, entityCode, entityElements, isMePresent);
        this.metaDataInfoMap = metaDataInfoMap;
    }

    public Map<String, RefMetadataInfo> getMetaDataInfoMap() {
        return metaDataInfoMap;
    }

    public void setMetaDataInfoMap(Map<String, RefMetadataInfo> metaDataInfoMap) {
        this.metaDataInfoMap = metaDataInfoMap;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((metaDataInfoMap == null) ? 0 : metaDataInfoMap.hashCode());
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
        RefEntityDetail other = (RefEntityDetail) obj;
        if (metaDataInfoMap == null) {
            if (other.metaDataInfoMap != null)
                return false;
        } else if (!metaDataInfoMap.equals(other.metaDataInfoMap))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "RefEntityDetail [metaDataInfoMap=" + metaDataInfoMap + "]";
    }
}
