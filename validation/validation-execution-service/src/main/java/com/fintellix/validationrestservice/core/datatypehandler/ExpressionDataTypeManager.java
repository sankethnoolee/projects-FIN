/**
 * 
 */
package com.fintellix.validationrestservice.core.datatypehandler;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author sumeet.tripathi
 *
 */
@Component
public class ExpressionDataTypeManager {

	private Map<String, ExpressionDataTypeProvider> dataTypeProviders = new HashMap<>();

	@Autowired
	public ExpressionDataTypeManager(List<ExpressionDataTypeProvider> providers) {
		for (ExpressionDataTypeProvider provider : providers) {
			dataTypeProviders.put(provider.handlerType(), provider);
		}
	}

	public String getReplacedSpelExpression(String columnName, String expression, String providerType, String uuid) {
		if (providerType == null) {
			return expression;
		}
		return dataTypeProviders.get(providerType).getReplacedSpelExpression(columnName, expression,uuid);
	}

	public String getReplacedSpelValue(Object value, String columnName, String providerType, String expression) {
		if (providerType == null) {
			return expression;
		}
		return dataTypeProviders.get(providerType).getReplacedSpelValue(value, columnName, expression);
	}

	public String getReplacedDisplayValue(Object value, String columnName, String providerType, String expression) {
		if (providerType == null) {
			return expression;
		}
		return dataTypeProviders.get(providerType).getReplacedDisplayValue(value, columnName, expression);
	}

}
