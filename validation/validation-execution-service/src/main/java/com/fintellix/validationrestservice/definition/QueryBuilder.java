package com.fintellix.validationrestservice.definition;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sumeet.tripathi
 */
public class QueryBuilder {
    private String query;
    private List<String> filter = new ArrayList<String>();
    private List<String> groupBy = new ArrayList<String>();
    private String baseGroupByColumn;
    private String baseTableName;
    private String expression;
    private String systemFilters;
    private String[] metaData;
    
    public String getBaseGroupByColumn() {
        return baseGroupByColumn;
    }

    public void setBaseGroupByColumn(String baseGroupByColumn) {
        this.baseGroupByColumn = baseGroupByColumn;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public List<String> getFilter() {
        return filter;
    }

    public void setFilter(List<String> filter) {
        this.filter = filter;
    }

    public List<String> getGroupBy() {
        return groupBy;
    }

    public void setGroupBy(List<String> groupBy) {
        this.groupBy = groupBy;
    }

    public String getBaseTableName() {
        return baseTableName;
    }

    public void setBaseTableName(String baseTableName) {
        this.baseTableName = baseTableName;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

	public String getSystemFilters() {
		return systemFilters;
	}

	public void setSystemFilters(String systemFilters) {
		this.systemFilters = systemFilters;
	}

	
	public String[] getMetaData() {
		return metaData;
	}

	public void setMetaData(String[] metaData) {
		this.metaData = metaData;
	}

}
