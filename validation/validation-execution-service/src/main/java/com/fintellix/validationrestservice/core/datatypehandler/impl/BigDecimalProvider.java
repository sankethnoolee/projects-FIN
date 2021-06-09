/**
 * 
 */
package com.fintellix.validationrestservice.core.datatypehandler.impl;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.fintellix.validationrestservice.core.datatypehandler.ExpressionDataType;
import com.fintellix.validationrestservice.core.datatypehandler.ExpressionDataTypeProvider;

/**
 * @author sumeet.tripathi
 *
 */
@Component
public class BigDecimalProvider implements ExpressionDataTypeProvider {

	@Override
	public String handlerType() {
		return ExpressionDataType.BIGDECIMAL.getValue();
	}

	@Override
	public String getReplacedSpelExpression(String columnName, String expression, String uuid) {
		return expression.replace(columnName, "(" + uuid + ")");
	}

	@Override
	public String getReplacedSpelValue(Object obj, String columnName, String expression) {
		return expression.replace(columnName, getSpelDecimalValue(obj));
	}

	@Override
	public String getReplacedDisplayValue(Object obj, String columnName, String expression) {
		String value;
		if (obj != null) {
			if (!obj.toString().contains(".")) {
				value = "(" + obj + ".0" + ")";
			} else {
				value = "(" + ((BigDecimal)obj).toPlainString() + ")";
			}
		} else {
			value = "null";
		}
		return expression.replace(columnName, value);
	}

}
