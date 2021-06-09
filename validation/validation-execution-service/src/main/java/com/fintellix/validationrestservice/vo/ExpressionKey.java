package com.fintellix.validationrestservice.vo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Deepak Moudgil
 */
public class ExpressionKey {
    String sourceExp;
    List<String> groupByExp = new ArrayList<>();
    List<String> filterExp = new ArrayList<>();

    public ExpressionKey() {
    }

    public ExpressionKey(String sourceExp, List<String> groupByExp, List<String> filterExp) {
        this.sourceExp = sourceExp;
        this.groupByExp = groupByExp;
        this.filterExp = filterExp;
    }

    public String getSourceExp() {
        return sourceExp;
    }

    public List<String> getGroupByExp() {
        return groupByExp;
    }

    public List<String> getFilterExp() {
        return filterExp;
    }

    @Override
    public String toString() {
        String str = "{" + sourceExp.toUpperCase();

        if (groupByExp != null && !groupByExp.isEmpty()) {
            List<String> groupByExpression = groupByExp.stream().map(String::toUpperCase)
                    .sorted().collect(Collectors.toList());
            str += "," + groupByExpression.toString();
        }

        // FIXME to upper case won't work in case of filter condition. Values could be case sensitive.
        //  find a way to fix that issue. | Deepak
        if (filterExp != null && !filterExp.isEmpty()) {
            List<String> filterExpression = filterExp.stream().map(String::toUpperCase)
                    .sorted().collect(Collectors.toList());
            str += "," + filterExpression.toString();
        }

        return str + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExpressionKey that = (ExpressionKey) o;
        return toString().equals(that.toString());
    }

    @Override
    public int hashCode() {
        return Objects.hash(toString());
    }
}