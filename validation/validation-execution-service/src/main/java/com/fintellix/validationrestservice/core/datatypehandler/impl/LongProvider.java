/**
 *
 */
package com.fintellix.validationrestservice.core.datatypehandler.impl;

import com.fintellix.validationrestservice.core.datatypehandler.ExpressionDataType;
import com.fintellix.validationrestservice.core.datatypehandler.ExpressionDataTypeProvider;
import org.springframework.stereotype.Component;

/**
 * @author sumeet.tripathi
 *
 */
@Component
public class LongProvider implements ExpressionDataTypeProvider {

    @Override
    public String handlerType() {
        return ExpressionDataType.LONG.getValue();
    }

    @Override
    public String getReplacedSpelExpression(String columnName, String expression, String uuid) {
        return expression.replace(columnName, "(" + uuid + ")");
    }

    @Override
    public String getReplacedSpelValue(Object obj, String columnName, String expression) {
        Object value;

        if (isDecimalValue(obj)) {
            value = getSpelDecimalValue(obj);
        } else {
            if (obj != null) {
                if (obj.toString().length() > 8) {
                    value = " new java.math.BigInteger('" + obj + "') ";
                } else {
                    value = obj;
                }
            } else {
                value = "null";
            }
        }

        return expression.replace(columnName, value.toString());
    }

    @Override
    public String getReplacedDisplayValue(Object obj, String columnName, String expression) {
        Object value;
        if (obj != null) {
            value = "(" + obj + ")";
        } else {
            value = "null";
        }
        return expression.replace(columnName, value.toString());
    }

}
