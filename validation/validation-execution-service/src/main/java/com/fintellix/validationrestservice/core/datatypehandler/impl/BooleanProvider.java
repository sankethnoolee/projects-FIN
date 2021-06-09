/**
 * 
 */
package com.fintellix.validationrestservice.core.datatypehandler.impl;

import org.springframework.stereotype.Component;

import com.fintellix.validationrestservice.core.datatypehandler.ExpressionDataType;
import com.fintellix.validationrestservice.core.datatypehandler.ExpressionDataTypeProvider;

/**
 * @author sumeet.tripathi
 *
 */
@Component
public class BooleanProvider implements ExpressionDataTypeProvider {

	@Override
	public String handlerType() {
		return ExpressionDataType.BOOLEAN.getValue();
	}

	@Override
	public String getReplacedSpelExpression(String columnName, String expression, String uuid) {
		return expression.replace(columnName, uuid);
	}

	@Override
	public String getReplacedSpelValue(Object obj, String columnName, String expression) {
		Object value;
		if (obj != null) {
			value = obj;
			return expression.replace(columnName, value.toString());
		} else {
			value = "null";
			return expression.replace(columnName, value.toString());
		}

	}

	@Override
	public String getReplacedDisplayValue(Object obj, String columnName, String expression) {
		Object value;
		if (obj != null) {
			value = obj;
		} else {
			value = "null";
		}
		return expression.replace(columnName, value.toString());
	}
}
