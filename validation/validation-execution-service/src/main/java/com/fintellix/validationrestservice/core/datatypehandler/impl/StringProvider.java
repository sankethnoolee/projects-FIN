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
public class StringProvider implements ExpressionDataTypeProvider {

	@Override
	public String handlerType() {
		return ExpressionDataType.STRING.getValue();
	}

	@Override
	public String getReplacedSpelExpression(String columnName, String expression, String uuid) {
		return expression.replace(columnName, "\"" + uuid + "\"");
	}

	@Override
	public String getReplacedSpelValue(Object obj, String columnName, String expression) {
		String value;
		if (obj != null) {
			value = obj.toString();
			if (value.toString().contains("\"")) {
				value = value.replace("\"", "\"\"");
			}
			return expression.replace(columnName, value);
		} else {
			value = "null";
			return expression.replace("\"" + columnName + "\"", value.toString()).replace("'" + columnName + "'", value.toString());
		}

	}

	@Override
	public String getReplacedDisplayValue(Object obj, String columnName, String expression) {
		Object value;
		if (obj != null) {
			value = "\"" + obj + "\"";
		} else {
			value = "null";
		}
		return expression.replace(columnName, value.toString());
	}
}
