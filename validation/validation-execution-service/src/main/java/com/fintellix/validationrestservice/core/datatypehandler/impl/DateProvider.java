/**
 * 
 */
package com.fintellix.validationrestservice.core.datatypehandler.impl;

import org.springframework.stereotype.Component;

import com.fintellix.validationrestservice.core.datatypehandler.ExpressionDataType;
import com.fintellix.validationrestservice.core.datatypehandler.ExpressionDataTypeProvider;
import com.fintellix.validationrestservice.util.ValidationProperties;

/**
 * @author sumeet.tripathi
 *
 */
@Component
public class DateProvider implements ExpressionDataTypeProvider {

	@Override
	public String handlerType() {
		return ExpressionDataType.DATE.getValue();
	}

	@Override
	public String getReplacedSpelExpression(String columnName, String expression, String uuid) {
		return expression.replace(columnName, "#DATE(\"" + uuid + "\", \""
				+ ValidationProperties.getValue("app.validation.dateFunction.defaultDateFormat") + "\")");
	}

	@Override
	public String getReplacedSpelValue(Object obj, String columnName, String expression) {
		Object value;
		if (obj != null) {
			value = obj;
			return expression.replace(columnName, value.toString());
		} else {
			value = "null";
			return expression.replace(
					"#DATE(\"" + columnName + "\", \""
							+ ValidationProperties.getValue("app.validation.dateFunction.defaultDateFormat") + "\")",
					value.toString()).replace(
							"#DATE(\"" + columnName + "\",\""
									+ ValidationProperties.getValue("app.validation.dateFunction.defaultDateFormat") + "\")",
							value.toString());
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
