/**
 *
 */
package com.fintellix.validationrestservice.core.datatypehandler;

import java.math.BigDecimal;

/**
 * @author sumeet.tripathi
 *
 */
public interface ExpressionDataTypeProvider {

    public String handlerType();

    public String getReplacedSpelExpression(String columnName, String expression, String uuid);

    public String getReplacedSpelValue(Object value, String columnName, String expression);

    public String getReplacedDisplayValue(Object value, String columnName, String expression);

    default boolean isDecimalValue(Object obj) {
        return obj != null && obj.toString().contains(".");
    }

    default String getSpelDecimalValue(Object obj) {
        String value;

        if (obj != null) {
            value = ((BigDecimal) obj).toPlainString();
            if (!value.contains(".")) {
                value += ".0";
            }
            value = " new java.math.BigDecimal('" + value + "') ";
        } else {
            value = "null";
        }

        return value;
    }
}
