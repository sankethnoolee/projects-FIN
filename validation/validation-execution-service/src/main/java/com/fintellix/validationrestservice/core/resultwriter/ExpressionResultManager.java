/**
 * 
 */
package com.fintellix.validationrestservice.core.resultwriter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fintellix.validationrestservice.definition.ExpressionMetaData;
import com.fintellix.validationrestservice.util.ApplicationProperties;

/**
 * @author sumeet.tripathi
 *
 */
@Component
public class ExpressionResultManager {

	private Map<String, ExpressionResultHandler> resultHandlers = new HashMap<>();

	@Autowired
	public ExpressionResultManager(List<ExpressionResultHandler> providers) {
		for (ExpressionResultHandler writer : providers) {
			resultHandlers.put(writer.handlerType(), writer);
		}
	}

	public void write(Integer exprId, Integer runId, List<Map<String, Object>> rows, Map<String, String> columnNameMap,
			List<String> headerColumnSequence) throws Throwable {
			if (resultHandlers.get(ApplicationProperties.getValue("app.validations.resulthandler")) == null) {
				throw new IllegalArgumentException(
						"handler not available for " + ApplicationProperties.getValue("app.validations.resulthandler"));
			}
		
		resultHandlers.get(ApplicationProperties.getValue("app.validations.resulthandler"))
				.writeValidationResult(exprId, runId, rows, columnNameMap, headerColumnSequence);
	}

	public ExpressionMetaData getExpressionMetaData(Integer runId, Integer exprId, String metadata) throws Throwable {
		String handlerType = ApplicationProperties.getValue("app.validations.resulthandler");
		if (!(handlerType.equalsIgnoreCase(ExpressionResultHandlerType.CSV.getValue()))) {
			try {
				Integer.parseInt(metadata);
			} catch (Exception e) {
				// for old validation this will fail
				handlerType = ExpressionResultHandlerType.CSV.getValue();
			}
		}
		return resultHandlers.get(ApplicationProperties.getValue("app.validations.resulthandler"))
				.getExpressionMetaData(runId, exprId, metadata);

	}

	public ExpressionMetaData getExpressionMetaDataForZeroOccurrence(Integer runId, Integer exprId, String metadata) throws Throwable {
		String handlerType = ApplicationProperties.getValue("app.validations.resulthandler");
		if (!(handlerType.equalsIgnoreCase(ExpressionResultHandlerType.CSV.getValue()))) {
			try {
				Integer.parseInt(metadata);
			} catch (Exception e) {
				// for old validation this will fail
				handlerType = ExpressionResultHandlerType.CSV.getValue();
			}
		}
		return resultHandlers.get(ApplicationProperties.getValue("app.validations.resulthandler"))
				.getExpressionMetaDataForZeroOccurrence(runId, exprId, metadata);
	}

	public void deleteExpressionResultByRunId(Integer runId) {
		resultHandlers.get(ApplicationProperties.getValue("app.validations.resulthandler")).deleteExpressionResultByRunId(runId);
		
	}

	public void writeRow(ValidationResult validationResult, Map<String, Object> row) throws Throwable {
		if (resultHandlers.get(ApplicationProperties.getValue("app.validations.resulthandler")) == null) {
			throw new IllegalArgumentException(
					"handler not availabe for " + ApplicationProperties.getValue("app.validations.resulthandler"));
		}

		resultHandlers.get(ApplicationProperties.getValue("app.validations.resulthandler")).writeRow(validationResult,
				row);

	}

	public void writeFile(ValidationResult validationResult) {
		if (resultHandlers.get(ApplicationProperties.getValue("app.validations.resulthandler")) == null) {
			throw new IllegalArgumentException(
					"handler not availabe for " + ApplicationProperties.getValue("app.validations.resulthandler"));
		}

		resultHandlers.get(ApplicationProperties.getValue("app.validations.resulthandler")).writeFile(validationResult);
	}

}
