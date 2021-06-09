package com.fintellix.validationrestservice.definition;

public class ExpressionResultDetail {
    private Boolean expressionProcessed;
    private String expression;
    private String expressionWithValues;
    private String expressionOutput;
    private String expressionOutputDataType;

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public String getExpressionWithValues() {
        return expressionWithValues;
    }

    public void setExpressionWithValues(String expressionWithValues) {
        this.expressionWithValues = expressionWithValues;
    }

    public String getExpressionOutput() {
        return expressionOutput;
    }

    public void setExpressionOutput(String expressionOutput) {
        this.expressionOutput = expressionOutput;
    }

    public String getExpressionOutputDataType() {
        return expressionOutputDataType;
    }

    public void setExpressionOutputDataType(String expressionOutputDataType) {
        this.expressionOutputDataType = expressionOutputDataType;
    }

    public Boolean getExpressionProcessed() {
        return expressionProcessed;
    }

    public void setExpressionProcessed(Boolean expressionProcessed) {
        this.expressionProcessed = expressionProcessed;
    }
}
