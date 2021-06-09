package com.fintellix.validationrestservice.vo;

import java.io.Serializable;

/**
 * @author Deepak Moudgil
 */
public class DynamicFilter implements Serializable {
    private String columnName;
    private String condition;

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }
}
